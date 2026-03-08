package com.subsetsum.bdd;

import com.subsetsum.algorithm.ArrayDPSolver;
import com.subsetsum.algorithm.HashMapDPSolver;
import com.subsetsum.algorithm.SubsetSumSolver;
import com.subsetsum.model.SubsetSumInput;
import com.subsetsum.model.SubsetSumResult;

import io.cucumber.java.en.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class ComparisonSteps {

    private final SubsetSumSolver arraySolver = new ArrayDPSolver();
    private final SubsetSumSolver hashMapSolver = new HashMapDPSolver();
    private SubsetSumInput generatedInput;
    private SubsetSumResult arrayResult;
    private SubsetSumResult hashMapResult;
    private String datasetName;

    private static final StringBuilder performanceLog = new StringBuilder();

    @Given("a generated dataset {string} of {int} non-negative numbers in range [{int}, {int}]")
    public void aGeneratedDataset(String name, int size, int min, int max) {
        this.datasetName = name;
        Random random = new Random(42); // Fixed seed for reproducibility
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            numbers.add(min + random.nextInt(max - min + 1));
        }
        int totalSum = numbers.stream().mapToInt(Integer::intValue).sum();
        int targetSum = totalSum / 2;
        generatedInput = new SubsetSumInput(numbers, targetSum);

        saveDataset(name, numbers, targetSum);
    }

    @When("both algorithms solve the problem")
    public void bothAlgorithmsSolveTheProblem() {
        arrayResult = arraySolver.solve(generatedInput);
        hashMapResult = hashMapSolver.solve(generatedInput);
    }

    @Then("both algorithms agree on found or not-found result")
    public void bothAlgorithmsAgreeOnResult() {
        assertEquals(arrayResult.isFound(), hashMapResult.isFound(),
                String.format("Mismatch: ArrayDP=%s, HashMapDP=%s (dataset %s)",
                        arrayResult.isFound(), hashMapResult.isFound(), datasetName));
    }

    @Then("time and memory of both algorithms are measured")
    public void timeAndMemoryOfBothAlgorithmsAreMeasured() {
        assertNotNull(arrayResult);
        assertNotNull(hashMapResult);

        String logEntry = String.format(
                "| %-20s | %6d | %12.2f | %12.2f | %10d | %10d | %8s | %8s |%n",
                datasetName,
                generatedInput.getNumbers().size(),
                arrayResult.getExecutionTimeMs(),
                hashMapResult.getExecutionTimeMs(),
                arrayResult.getMemoryUsedBytes(),
                hashMapResult.getMemoryUsedBytes(),
                arrayResult.isFound(),
                hashMapResult.isFound()
        );
        performanceLog.append(logEntry);

        System.out.println("=== Performance results: " + datasetName + " ===");
        System.out.printf("  Dataset size: %d%n", generatedInput.getNumbers().size());
        System.out.printf("  Target sum: %d%n", generatedInput.getTargetSum());
        System.out.printf("  ArrayDP:   time=%.2f ms, memory=%d bytes, found=%s%n",
                arrayResult.getExecutionTimeMs(), arrayResult.getMemoryUsedBytes(), arrayResult.isFound());
        System.out.printf("  HashMapDP: time=%.2f ms, memory=%d bytes, found=%s%n",
                hashMapResult.getExecutionTimeMs(), hashMapResult.getMemoryUsedBytes(), hashMapResult.isFound());

        double speedup = hashMapResult.getExecutionTimeMs() > 0
                ? arrayResult.getExecutionTimeMs() / hashMapResult.getExecutionTimeMs()
                : Double.NaN;
        System.out.printf("  Ratio ArrayDP/HashMapDP: %.2fx%n%n", speedup);
    }

    @And("each algorithm completes within {int} milliseconds")
    public void eachAlgorithmCompletesWithinMilliseconds(int maxMs) {
        assertTrue(arrayResult.getExecutionTimeMs() < maxMs,
                String.format("ArrayDP exceeded limit: %.2f ms > %d ms",
                        arrayResult.getExecutionTimeMs(), maxMs));
        assertTrue(hashMapResult.getExecutionTimeMs() < maxMs,
                String.format("HashMapDP exceeded limit: %.2f ms > %d ms",
                        hashMapResult.getExecutionTimeMs(), maxMs));
    }

    public static String getPerformanceLog() {
        return performanceLog.toString();
    }

    private void saveDataset(String name, List<Integer> numbers, int targetSum) {
        try {
            File dir = new File("target/datasets");
            dir.mkdirs();
            File file = new File(dir, name + ".json");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("{\n");
                writer.write("  \"numbers\": " + numbers + ",\n");
                writer.write("  \"targetSum\": " + targetSum + "\n");
                writer.write("}\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving dataset " + name + ": " + e.getMessage());
        }
    }
}
