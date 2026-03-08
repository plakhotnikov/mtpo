Feature: Solving Subset Sum problem with ArrayDP algorithm
  As an application user
  I want to solve the Subset Sum problem using a tabular DP algorithm on boolean[][]
  So that I can quickly find a subset with a given sum for non-negative numbers

  Background:
    Given the solver is "ArrayDP"

  Rule: Correctly finding subsets for non-negative numbers

    Scenario: Finding a subset in a simple set
      Given the input numbers are "3, 7, 1, 8, 2"
      And the target sum is 11
      When the solver runs
      Then the result should be "found"
      And the subset sum equals 11
      But the found subset size is less than 5

    Scenario: Subset does not exist
      Given the input numbers are "5, 10, 15"
      And the target sum is 7
      When the solver runs
      Then the result should be "not found"

    Scenario: Target sum is zero — empty subset is always found
      Given the input numbers are "1, 2, 3"
      And the target sum is 0
      When the solver runs
      Then the result should be "found"
      And the found subset is empty

  Rule: Input data validation

    Scenario: Negative numbers are not supported
      Given the input numbers are "1, -2, 3"
      And the target sum is 2
      When the solver runs
      Then an error should occur with message containing "negative"

    Scenario: Empty number set causes validation error
      Given an empty number set
      And the target sum is 5
      When the solver runs
      Then a validation error should occur

  Scenario Outline: Parameterized subset search
    Given the input numbers are "<numbers>"
    And the target sum is <target>
    When the solver runs
    Then the result should be "<result>"

    Examples:
      | numbers        | target | result    |
      | 1, 2, 3, 4, 5  | 15     | found     |
      | 1, 2, 3, 4, 5  | 16     | not found |
      | 10             | 10     | found     |
      | 10             | 5      | not found |
      | 1, 1, 1, 1     | 3      | found     |
