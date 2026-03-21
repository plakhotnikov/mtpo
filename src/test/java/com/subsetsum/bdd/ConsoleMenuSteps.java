package com.subsetsum.bdd;

import com.subsetsum.algorithm.ArrayDPSolver;
import com.subsetsum.algorithm.HashMapDPSolver;
import com.subsetsum.io.JsonFileReader;
import com.subsetsum.io.JsonFileWriter;
import com.subsetsum.ui.ConsoleMenu;

import io.cucumber.java.en.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class ConsoleMenuSteps {

    private ConsoleMenu menu;
    private ByteArrayOutputStream outputStream;
    private String capturedOutput;
    private String pendingNumbers;
    private String pendingTarget;

    @Given("the console menu is initialized")
    public void theConsoleMenuIsInitialized() {
        // Will be initialized per scenario with specific input
    }

    private void runMenu(String simulatedInput) {
        Scanner scanner = new Scanner(simulatedInput);
        outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        menu = new ConsoleMenu(scanner, printStream,
                new ArrayDPSolver(), new HashMapDPSolver(),
                new JsonFileReader(), new JsonFileWriter());
        menu.run();
        capturedOutput = outputStream.toString();
    }

    // Imperative style steps
    @When("the user selects menu option {string}")
    public void theUserSelectsMenuOption(String choice) {
        if ("0".equals(choice)) {
            runMenu("0\n");
        } else if ("3".equals(choice)) {
            runMenu("3\n0\n");
        }
    }

    @And("the user enters numbers {string}")
    public void theUserEntersNumbers(String numbers) {
        this.pendingNumbers = numbers;
    }

    @And("the user enters target sum {string}")
    public void theUserEntersTargetSum(String targetSum) {
        this.pendingTarget = targetSum;
    }

    @And("the user declines to save the result")
    public void theUserDeclinesToSaveTheResult() {
        String input = "1\n" + pendingNumbers + "\n" + pendingTarget + "\nn\n0\n";
        runMenu(input);
    }

    // Declarative style step
    @When("the user solves a problem with numbers {string} and sum {string}")
    public void theUserSolvesAProblemWithNumbersAndSum(String numbers, String targetSum) {
        String input = "1\n" + numbers + "\n" + targetSum + "\nn\n0\n";
        runMenu(input);
    }

    @Then("the output contains {string}")
    public void theOutputContains(String expected) {
        assertNotNull(capturedOutput, "No captured output");
        assertTrue(capturedOutput.contains(expected),
                "Output does not contain '" + expected + "'.\nOutput:\n" + capturedOutput);
    }

    @And("the output does not contain {string}")
    public void theOutputDoesNotContain(String unexpected) {
        assertFalse(capturedOutput.contains(unexpected),
                "Output unexpectedly contains '" + unexpected + "'");
    }

    @Then("both algorithm results are displayed")
    public void bothAlgorithmResultsAreDisplayed() {
        assertTrue(capturedOutput.contains("ArrayDP") || capturedOutput.contains("boolean[][]"),
                "No ArrayDP results.\nOutput:\n" + capturedOutput);
        assertTrue(capturedOutput.contains("HashMapDP") || capturedOutput.contains("HashMap"),
                "No HashMapDP results.\nOutput:\n" + capturedOutput);
    }

    @And("both algorithms find a solution")
    public void bothAlgorithmsFindASolution() {
        int foundCount = capturedOutput.split("Решение найдено: Да", -1).length - 1;
        assertTrue(foundCount >= 2,
                "Expected both algorithms to report 'Решение найдено: Да'.\nOutput:\n" + capturedOutput);
    }

    @Then("the application stops running")
    public void theApplicationStopsRunning() {
        assertFalse(menu.isRunning());
    }
}
