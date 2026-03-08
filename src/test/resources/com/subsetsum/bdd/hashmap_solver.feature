Feature: Solving Subset Sum problem with HashMapDP algorithm
  As an application user
  I want to solve the Subset Sum problem using a HashMap-based DP algorithm
  So that I can find subsets even when negative numbers are present

  Background:
    Given the solver is "HashMapDP"

  Rule: Support for negative numbers

    Scenario: Finding a subset with negative numbers
      Given the input numbers are "5, -3, 8, -2, 1"
      And the target sum is -5
      When the solver runs
      Then the result should be "found"
      And the subset sum equals -5

    Scenario: Mixed numbers — subset not found
      Given the input numbers are "10, -20, 5"
      And the target sum is 100
      When the solver runs
      Then the result should be "not found"

  Scenario: Data table with expected results
    When the solver is run for each row of the table:
      | numbers            | targetSum | expected  |
      | 1, 2, 3            | 6         | found     |
      | -1, -2, -3         | -3        | found     |
      | 5, 10, 15          | 7         | not found |
      | -5, 10, -3, 8      | 2         | found     |
      | 100, 200, 300      | 50        | not found |
    Then all table results match expectations

  Scenario Outline: Parameterized search with negative numbers
    Given the input numbers are "<numbers>"
    And the target sum is <target>
    When the solver runs
    Then the result should be "<result>"

    Examples:
      | numbers       | target | result    |
      | -1, 2, -3, 4  | 3      | found     |
      | -1, -2, -3    | -6     | found     |
      | -1, -2, -3    | 1      | not found |
