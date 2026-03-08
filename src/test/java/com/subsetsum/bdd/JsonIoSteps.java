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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class JsonIoSteps {

    private final JsonFileReader jsonReader = new JsonFileReader();
    private final JsonFileWriter jsonWriter = new JsonFileWriter();
    private SubsetSumInput readInput;
    private SubsetSumResult solverResult;
    private String jsonOutputString;
    private Exception caughtException;
    private File tempFile;

    @Given("a JSON file exists with content:")
    public void aJsonFileExistsWithContent(String content) throws IOException {
        tempFile = File.createTempFile("bdd-test-", ".json");
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
    }

    @When("the file is read via JsonFileReader")
    public void theFileIsReadViaJsonFileReader() throws IOException {
        readInput = jsonReader.readFromFile(tempFile.getAbsolutePath());
    }

    @Then("the input data contains numbers {string}")
    public void theInputDataContainsNumbers(String numbersStr) {
        List<Integer> expected = Arrays.stream(numbersStr.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        assertEquals(expected, readInput.getNumbers());
    }

    @And("the input target sum equals {int}")
    public void theInputTargetSumEquals(int expected) {
        assertEquals(expected, readInput.getTargetSum());
    }

    @Given("a solver result with subset {string} and sum {int}")
    public void aSolverResultWithSubsetAndSum(String subsetStr, int sum) {
        List<Integer> subset = Arrays.stream(subsetStr.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        solverResult = new SubsetSumResult(true, subset, "TestAlgorithm");
    }

    @When("the result is written via JsonFileWriter")
    public void theResultIsWrittenViaJsonFileWriter() throws IOException {
        jsonOutputString = jsonWriter.writeToString(solverResult);
    }

    @Then("the JSON string contains {string}")
    public void theJsonStringContains(String expected) {
        assertNotNull(jsonOutputString);
        assertTrue(jsonOutputString.contains(expected),
                "JSON does not contain '" + expected + "': " + jsonOutputString);
    }

    @When("reading file {string} is attempted")
    public void readingFileIsAttempted(String filePath) {
        try {
            caughtException = null;
            jsonReader.readFromFile(filePath);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("an IO error should occur")
    public void anIoErrorShouldOccur() {
        assertNotNull(caughtException, "Expected an IO error");
    }

    @And("the data is solved with {string} algorithm")
    public void theDataIsSolvedWithAlgorithm(String algorithmName) {
        SubsetSumSolver solver;
        if ("ArrayDP".equals(algorithmName)) {
            solver = new ArrayDPSolver();
        } else {
            solver = new HashMapDPSolver();
        }
        solverResult = solver.solve(readInput);
    }
}
