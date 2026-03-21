@heavy
Feature: Heavy load testing and performance profiling of Subset Sum algorithms
  As an algorithm researcher
  I want to run heavy load tests with detailed profiling
  So that I can determine maximum performance boundaries and compare algorithms under stress

  Rule: Preliminary experiment — finding maximum dataset sizes

    Scenario Outline: Preliminary experiment to find t1 boundary
      Given a generated dataset "<dataset>" of <size> non-negative numbers in range [1, <max>]
      When both algorithms solve the problem with detailed profiling
      Then profiling results are logged for "<dataset>"
      And both algorithms agree on found or not-found result

      Examples:
        | dataset              | size  | max   |
        | PRELIM_DENSE_500     | 500   | 100   |
        | PRELIM_DENSE_1000    | 1000  | 100   |
        | PRELIM_DENSE_2000    | 2000  | 100   |
        | PRELIM_DENSE_3000    | 3000  | 100   |
        | PRELIM_DENSE_5000    | 5000  | 100   |
        | PRELIM_SPARSE_500    | 500   | 1000  |
        | PRELIM_SPARSE_1000   | 1000  | 1000  |
        | PRELIM_SPARSE_2000   | 2000  | 1000  |
        | PRELIM_SPARSE_3000   | 3000  | 1000  |

  Rule: Full load test plan — 2 data types x 2 sizes (medium + maximum)

    Scenario: Full profiling — Dense medium (DENSE_MED)
      Given a generated dataset "DENSE_MED_1000" of 1000 non-negative numbers in range [1, 100]
      When the full profiling pipeline runs for both algorithms
      Then full profiling results are logged for "DENSE_MED_1000"

    Scenario: Full profiling — Dense maximum (DENSE_MAX)
      Given a generated dataset "DENSE_MAX_60000" of 60000 non-negative numbers in range [1, 100]
      When the full profiling pipeline runs with OOM protection
      Then full profiling results are logged for "DENSE_MAX_60000"

    Scenario: Full profiling — Sparse medium (SPARSE_MED)
      Given a generated dataset "SPARSE_MED_1000" of 1000 non-negative numbers in range [1, 1000]
      When the full profiling pipeline runs for both algorithms
      Then full profiling results are logged for "SPARSE_MED_1000"

    Scenario: Full profiling — Sparse maximum (SPARSE_MAX)
      Given a generated dataset "SPARSE_MAX_25000" of 25000 non-negative numbers in range [1, 500]
      When the full profiling pipeline runs with OOM protection
      Then full profiling results are logged for "SPARSE_MAX_25000"

  Rule: Comparison of BDD test speed vs unit test speed

    Scenario: Measure BDD overhead by running same data as SolverComparisonTest
      Given a generated dataset "BDD_VS_UNIT_500" of 500 non-negative numbers in range [1, 200]
      When both algorithms solve the problem with detailed profiling
      Then profiling results are logged for "BDD_VS_UNIT_500"
      And both algorithms agree on found or not-found result
