Feature: Reading and writing data in JSON format
  As an application user
  I want to load input data from a JSON file and save results
  So that I can automate batch processing of tasks

  Scenario: Successfully reading input data from a JSON file
    Given a JSON file exists with content:
      """
      {
        "numbers": [1, 2, 3, 4, 5],
        "targetSum": 9
      }
      """
    When the file is read via JsonFileReader
    Then the input data contains numbers "1, 2, 3, 4, 5"
    And the input target sum equals 9

  Scenario: Successfully writing a result to JSON
    Given a solver result with subset "3, 4, 2" and sum 9
    When the result is written via JsonFileWriter
    Then the JSON string contains "found"
    And the JSON string contains "subset"

  Scenario: Error when reading a non-existent file
    When reading file "nonexistent.json" is attempted
    Then an IO error should occur

  Scenario: Full cycle — read, solve, write
    Given a JSON file exists with content:
      """
      {
        "numbers": [10, 20, 30, 40, 50],
        "targetSum": 60
      }
      """
    When the file is read via JsonFileReader
    And the data is solved with "ArrayDP" algorithm
    And the result is written via JsonFileWriter
    Then the JSON string contains "found"
