# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Lab 1: "Development of Code Through Unit Testing" (TDD). Console Java application solving the NP-complete **Subset Sum** problem with two DP algorithms on different data structures.

## Build & Test Commands

```bash
mvn compile                    # Build
mvn test                       # Run all 128 tests
mvn test -Dtest=ArrayDPSolverTest          # Single test class
mvn test -Dtest=ArrayDPSolverTest#targetSumZero_alwaysFound  # Single method
mvn test-compile org.pitest:pitest-maven:mutationCoverage    # Mutation testing
mvn package                    # Build JAR
```

## Architecture

Package `com.subsetsum`:
- **algorithm/** — `SubsetSumSolver` interface + two implementations:
  - `ArrayDPSolver` — DP on `boolean[][]`, non-negative numbers only, O(n×target)
  - `HashMapDPSolver` — DP on `HashMap<Integer,Integer>`, supports negatives, O(n×|reachable sums|)
- **model/** — `SubsetSumInput` (numbers + targetSum), `SubsetSumResult` (found, subset, timing, memory)
- **io/** — `JsonFileReader`/`JsonFileWriter` using Jackson ObjectMapper (injectable for mocking)
- **ui/** — `ConsoleMenu` with constructor DI for all dependencies (solvers, reader, writer, Scanner, PrintStream)
- **App.java** — entry point

## Testing Structure

128 tests across 8 test classes. 4 test design techniques:
- **BVA** (Boundary Value Analysis) → `ArrayDPSolverTest.BoundaryValueAnalysis`
- **EP** (Equivalence Partitioning) → `HashMapDPSolverTest.EquivalencePartitioning`
- **Statement Testing** → `ArrayDPSolverTest.StatementTesting`
- **Branch Testing** → `HashMapDPSolverTest.BranchTesting`

Required features: 7 assertion types, 2 assumption types, 3 mocking types (@Mock, @Spy, inline mock()), parameterized tests (@CsvSource, @MethodSource), 9+ Hamcrest matchers.

## Key Dependencies

Java 17 source (Maven uses Java 23 runtime). JUnit 5.10.2, Mockito 5.17.0, Hamcrest 2.2, Jackson 2.17.0, Pitest 1.15.8. Explicit Byte Buddy 1.17.4 for Java 23 compatibility.

## Notes

- `ConsoleMenu` uses constructor injection — all external deps are mockable
- `SolverComparisonTest` validates both algorithms agree on all test cases
- Pitest HTML report in `target/pit-reports/`
