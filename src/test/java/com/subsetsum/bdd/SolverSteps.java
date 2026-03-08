package com.subsetsum.bdd;

import com.subsetsum.algorithm.ArrayDPSolver;
import com.subsetsum.algorithm.HashMapDPSolver;
import com.subsetsum.algorithm.SubsetSumSolver;
import com.subsetsum.model.SubsetSumInput;
import com.subsetsum.model.SubsetSumResult;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class SolverSteps {

    private SubsetSumSolver solver;
    private SubsetSumInput input;
    private SubsetSumResult result;
    private Exception caughtException;
    private final List<Boolean> tableResults = new ArrayList<>();
    private final List<Boolean> tableExpectations = new ArrayList<>();

    @Given("the solver is {string}")
    public void theSolverIs(String algorithmName) {
        if ("ArrayDP".equals(algorithmName)) {
            solver = new ArrayDPSolver();
        } else if ("HashMapDP".equals(algorithmName)) {
            solver = new HashMapDPSolver();
        } else {
            throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        }
    }

    @Given("the input numbers are {string}")
    public void theInputNumbersAre(String numbersStr) {
        List<Integer> numbers = Arrays.stream(numbersStr.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        input = new SubsetSumInput(numbers, 0);
    }

    @Given("an empty number set")
    public void anEmptyNumberSet() {
        input = new SubsetSumInput(new ArrayList<>(), 0);
    }

    @And("the target sum is {int}")
    public void theTargetSumIs(int targetSum) {
        input.setTargetSum(targetSum);
    }

    @When("the solver runs")
    public void theSolverRuns() {
        try {
            caughtException = null;
            result = solver.solve(input);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("the result should be {string}")
    public void theResultShouldBe(String expected) {
        assertNull(caughtException, "Unexpected exception: " + caughtException);
        if ("found".equals(expected)) {
            assertTrue(result.isFound(), "Expected subset to be found");
        } else {
            assertFalse(result.isFound(), "Expected subset to not be found");
        }
    }

    @And("the subset sum equals {int}")
    public void theSubsetSumEquals(int expectedSum) {
        assertNotNull(result.getSubset());
        int actualSum = result.getSubset().stream().mapToInt(Integer::intValue).sum();
        assertEquals(expectedSum, actualSum,
                "Subset " + result.getSubset() + " sum does not equal " + expectedSum);
    }

    @And("the found subset is empty")
    public void theFoundSubsetIsEmpty() {
        assertTrue(result.getSubset().isEmpty(),
                "Expected empty subset but got: " + result.getSubset());
    }

    @Then("an error should occur with message containing {string}")
    public void anErrorShouldOccurWithMessageContaining(String messagePart) {
        assertNotNull(caughtException, "Expected an exception");
        assertTrue(caughtException.getMessage().toLowerCase().contains(messagePart.toLowerCase()),
                "Error message '" + caughtException.getMessage()
                        + "' does not contain '" + messagePart + "'");
    }

    @Then("a validation error should occur")
    public void aValidationErrorShouldOccur() {
        assertNotNull(caughtException, "Expected a validation exception");
    }

    @When("the solver is run for each row of the table:")
    public void theSolverIsRunForEachRowOfTheTable(DataTable dataTable) {
        tableResults.clear();
        tableExpectations.clear();
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            List<Integer> numbers = Arrays.stream(row.get("numbers").split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            int targetSum = Integer.parseInt(row.get("targetSum").trim());
            SubsetSumInput tableInput = new SubsetSumInput(numbers, targetSum);
            SubsetSumResult tableResult = solver.solve(tableInput);
            tableResults.add(tableResult.isFound());
            tableExpectations.add(row.get("expected").trim().equals("found"));
        }
    }

    @Then("all table results match expectations")
    public void allTableResultsMatchExpectations() {
        assertEquals(tableExpectations.size(), tableResults.size());
        for (int i = 0; i < tableResults.size(); i++) {
            assertEquals(tableExpectations.get(i), tableResults.get(i),
                    "Mismatch at row " + (i + 1));
        }
    }
}
