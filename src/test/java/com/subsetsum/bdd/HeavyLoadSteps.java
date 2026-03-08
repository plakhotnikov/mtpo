package com.subsetsum.bdd;

import com.subsetsum.algorithm.ArrayDPSolver;
import com.subsetsum.algorithm.HashMapDPSolver;
import com.subsetsum.algorithm.SubsetSumSolver;
import com.subsetsum.io.JsonFileReader;
import com.subsetsum.io.JsonFileWriter;
import com.subsetsum.model.SubsetSumInput;
import com.subsetsum.model.SubsetSumResult;

import io.cucumber.java.en.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class HeavyLoadSteps {

    private final SubsetSumSolver arraySolver = new ArrayDPSolver();
    private final SubsetSumSolver hashMapSolver = new HashMapDPSolver();
    private final TestContext ctx = TestContext.get();

    private long saveTimeNs;
    private long loadTimeNs;
    private long arraySolveTimeNs;
    private long hashMapSolveTimeNs;
    private boolean arraySkippedOom;

    private static final StringBuilder fullReport = new StringBuilder();

    static {
        fullReport.append("\n========== HEAVY LOAD TEST REPORT ==========\n");
        fullReport.append(String.format("%-25s | %6s | %12s | %14s | %14s | %12s | %12s%n",
                "Dataset", "Size", "Target", "ArrayDP(ms)", "HashMapDP(ms)", "ArrayMem", "HashMem"));
        fullReport.append("-".repeat(115) + "\n");
    }

    @When("both algorithms solve the problem with detailed profiling")
    public void bothAlgorithmsSolveWithDetailedProfiling() {
        SubsetSumInput input = ctx.getGeneratedInput();
        arraySkippedOom = false;

        System.gc();
        long start = System.nanoTime();
        ctx.setArrayResult(arraySolver.solve(input));
        arraySolveTimeNs = System.nanoTime() - start;

        System.gc();
        start = System.nanoTime();
        ctx.setHashMapResult(hashMapSolver.solve(input));
        hashMapSolveTimeNs = System.nanoTime() - start;
    }

    @Then("profiling results are logged for {string}")
    public void profilingResultsAreLoggedFor(String name) {
        SubsetSumInput input = ctx.getGeneratedInput();
        SubsetSumResult arrayResult = ctx.getArrayResult();
        SubsetSumResult hashMapResult = ctx.getHashMapResult();

        double arrayMs = arraySkippedOom ? -1 : arraySolveTimeNs / 1_000_000.0;
        double hashMapMs = hashMapSolveTimeNs / 1_000_000.0;
        String arrayMsStr = arraySkippedOom ? "OOM" : String.format("%.2f", arrayMs);
        long arrayMem = arrayResult != null ? arrayResult.getMemoryUsedBytes() : 0;

        String entry = String.format("%-25s | %6d | %12d | %14s | %14.2f | %12d | %12d%n",
                name, input.getNumbers().size(), input.getTargetSum(),
                arrayMsStr, hashMapMs,
                arrayMem, hashMapResult.getMemoryUsedBytes());
        fullReport.append(entry);

        System.out.println("=== HEAVY: " + name + " ===");
        System.out.printf("  Size: %d, Target: %d%n", input.getNumbers().size(), input.getTargetSum());
        if (arraySkippedOom) {
            System.out.println("  ArrayDP solve:   SKIPPED (OutOfMemoryError)");
        } else {
            System.out.printf("  ArrayDP solve:   %12.2f ms (%6.2f s), memory=%d bytes%n",
                    arrayMs, arrayMs / 1000, arrayMem);
        }
        System.out.printf("  HashMapDP solve: %12.2f ms (%6.2f s), memory=%d bytes%n",
                hashMapMs, hashMapMs / 1000, hashMapResult.getMemoryUsedBytes());
        if (!arraySkippedOom) {
            double ratio = hashMapMs > 0 ? arrayMs / hashMapMs : Double.NaN;
            System.out.printf("  Ratio ArrayDP/HashMapDP: %.2fx%n", ratio);
        }
        System.out.println();

        saveReport();
    }

    @When("the full profiling pipeline runs for both algorithms")
    public void theFullProfilingPipelineRunsForBothAlgorithms() {
        runFullPipeline(false);
    }

    @When("the full profiling pipeline runs with OOM protection")
    public void theFullProfilingPipelineRunsWithOomProtection() {
        runFullPipeline(true);
    }

    private void runFullPipeline(boolean protectOom) {
        SubsetSumInput input = ctx.getGeneratedInput();
        String datasetName = ctx.getDatasetName();
        JsonFileWriter writer = new JsonFileWriter();
        JsonFileReader reader = new JsonFileReader();
        arraySkippedOom = false;

        // Step 1: Save dataset to JSON file
        String filePath = "target/datasets/" + datasetName + ".json";
        long start = System.nanoTime();
        try {
            File dir = new File("target/datasets");
            dir.mkdirs();
            File file = new File(filePath);
            try (FileWriter fw = new FileWriter(file)) {
                fw.write("{\"numbers\":" + input.getNumbers()
                        + ",\"targetSum\":" + input.getTargetSum() + "}");
            }
        } catch (IOException e) {
            fail("Failed to save dataset: " + e.getMessage());
        }
        saveTimeNs = System.nanoTime() - start;

        // Step 2: Load dataset from JSON file
        start = System.nanoTime();
        try {
            SubsetSumInput loaded = reader.readFromFile(filePath);
            assertEquals(input.getNumbers().size(), loaded.getNumbers().size());
        } catch (IOException e) {
            fail("Failed to load dataset: " + e.getMessage());
        }
        loadTimeNs = System.nanoTime() - start;

        // Step 3: Solve with ArrayDP (with OOM protection for large datasets)
        System.gc();
        if (protectOom) {
            try {
                start = System.nanoTime();
                ctx.setArrayResult(arraySolver.solve(input));
                arraySolveTimeNs = System.nanoTime() - start;
            } catch (OutOfMemoryError e) {
                arraySkippedOom = true;
                arraySolveTimeNs = 0;
                ctx.setArrayResult(null);
                System.gc();
                System.out.println("  [!] ArrayDP: OutOfMemoryError — skipping (table too large)");
            }
        } else {
            start = System.nanoTime();
            ctx.setArrayResult(arraySolver.solve(input));
            arraySolveTimeNs = System.nanoTime() - start;
        }

        // Step 4: Solve with HashMapDP
        System.gc();
        start = System.nanoTime();
        ctx.setHashMapResult(hashMapSolver.solve(input));
        hashMapSolveTimeNs = System.nanoTime() - start;
    }

    @Then("full profiling results are logged for {string}")
    public void fullProfilingResultsAreLoggedFor(String name) {
        SubsetSumInput input = ctx.getGeneratedInput();
        SubsetSumResult arrayResult = ctx.getArrayResult();
        SubsetSumResult hashMapResult = ctx.getHashMapResult();

        double arrayMs = arraySkippedOom ? -1 : arraySolveTimeNs / 1_000_000.0;
        double hashMapMs = hashMapSolveTimeNs / 1_000_000.0;
        double saveMs = saveTimeNs / 1_000_000.0;
        double loadMs = loadTimeNs / 1_000_000.0;
        String arrayMsStr = arraySkippedOom ? "OOM" : String.format("%.2f", arrayMs);
        long arrayMem = arrayResult != null ? arrayResult.getMemoryUsedBytes() : 0;

        System.out.println("=== FULL PROFILING: " + name + " ===");
        System.out.printf("  Size: %d, Target: %d%n", input.getNumbers().size(), input.getTargetSum());
        System.out.printf("  [1] Save to JSON:    %12.2f ms%n", saveMs);
        System.out.printf("  [2] Load from JSON:  %12.2f ms%n", loadMs);
        if (arraySkippedOom) {
            System.out.println("  [3] ArrayDP solve:   OutOfMemoryError (table size would be "
                    + ((long) input.getNumbers().size() * input.getTargetSum()) + " bytes)");
        } else {
            System.out.printf("  [3] ArrayDP solve:   %12.2f ms (%8.2f s), memory=%d bytes%n",
                    arrayMs, arrayMs / 1000, arrayMem);
        }
        System.out.printf("  [4] HashMapDP solve: %12.2f ms (%8.2f s), memory=%d bytes%n",
                hashMapMs, hashMapMs / 1000, hashMapResult.getMemoryUsedBytes());
        System.out.printf("  Total pipeline:      %12.2f ms%n",
                saveMs + loadMs + (arraySkippedOom ? 0 : arrayMs) + hashMapMs);

        String entry = String.format("%-25s | %6d | %12d | %14s | %14.2f | %12d | %12d%n",
                name, input.getNumbers().size(), input.getTargetSum(),
                arrayMsStr, hashMapMs, arrayMem, hashMapResult.getMemoryUsedBytes());
        fullReport.append(entry);

        saveReport();
    }

    private void saveReport() {
        try {
            File reportFile = new File("target/datasets/heavy_load_report.txt");
            reportFile.getParentFile().mkdirs();
            try (FileWriter fw = new FileWriter(reportFile, false)) {
                fw.write(fullReport.toString());
            }
        } catch (IOException ignored) {
        }
    }

    public static String getFullReport() {
        return fullReport.toString();
    }
}
