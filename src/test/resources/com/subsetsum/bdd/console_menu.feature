@business-idea
Feature: Interactive console menu for solving the Subset Sum problem
  As a student studying algorithms
  I want a convenient interface for entering data and viewing results
  So that I can visually compare two algorithms for solving an NP-complete problem

  Background:
    Given the console menu is initialized

  # Imperative style — detailed step-by-step interaction
  Scenario: Manual data input via console (imperative style)
    When the user selects menu option "1"
    And the user enters numbers "5 10 15 20"
    And the user enters target sum "25"
    And the user declines to save the result
    Then the output contains "Результат"
    And the output contains "ArrayDP"
    And the output contains "HashMapDP"
    And the output does not contain "Ошибка"

  # Declarative style — describing the result without implementation details
  Scenario: Solving a problem with both algorithm results displayed (declarative style)
    When the user solves a problem with numbers "5 10 15 20" and sum "25"
    Then both algorithm results are displayed
    And both algorithms find a solution

  Scenario: Displaying help
    When the user selects menu option "3"
    Then the output contains "ArrayDP"
    And the output contains "HashMapDP"
    And the output contains "Справка"

  Scenario: Exiting the application
    When the user selects menu option "0"
    Then the application stops running
