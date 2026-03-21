Feature: Performance comparison of two Subset Sum algorithms
  As an algorithm researcher
  I want to compare execution time of ArrayDP and HashMapDP on various datasets
  So that I can identify performance dependencies on input size and characteristics

  Rule: Both algorithms produce the same result on non-negative data

    Scenario: Results match on a small dataset
      Given a generated dataset "SMALL_POS_10" of 10 non-negative numbers in range [1, 50]
      When both algorithms solve the problem
      Then both algorithms agree on found or not-found result

    Scenario: Results match on a medium dataset
      Given a generated dataset "MED_POS_100" of 100 non-negative numbers in range [1, 500]
      When both algorithms solve the problem
      Then both algorithms agree on found or not-found result

  Rule: Load testing — time and memory profiling

    Scenario: Load test on a small dataset (warm-up)
      Given a generated dataset "LOAD_POS_50" of 50 non-negative numbers in range [1, 100]
      When both algorithms solve the problem
      Then time and memory of both algorithms are measured
      And each algorithm completes within 5000 milliseconds

    Scenario: Load test on a medium dataset
      Given a generated dataset "LOAD_POS_500" of 500 non-negative numbers in range [1, 200]
      When both algorithms solve the problem
      Then time and memory of both algorithms are measured
      And each algorithm completes within 30000 milliseconds

    Scenario: Load test on a large dense dataset
      Given a generated dataset "LOAD_DENSE_1000" of 1000 non-negative numbers in range [1, 100]
      When both algorithms solve the problem
      Then time and memory of both algorithms are measured

    Scenario: Load test on a large sparse dataset
      Given a generated dataset "LOAD_SPARSE_1000" of 1000 non-negative numbers in range [1, 10000]
      When both algorithms solve the problem
      Then time and memory of both algorithms are measured

    Scenario Outline: Performance comparison as input size grows
      Given a generated dataset "<dataset>" of <size> non-negative numbers in range [1, <max>]
      When both algorithms solve the problem
      Then time and memory of both algorithms are measured
      And both algorithms agree on found or not-found result

      Examples:
        | dataset          | size | max   |
        | SCALE_POS_20     | 20   | 50    |
        | SCALE_POS_50     | 50   | 100   |
        | SCALE_POS_100    | 100  | 200   |
        | SCALE_POS_200    | 200  | 500   |
        | SCALE_POS_500    | 500  | 1000  |
