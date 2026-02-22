package com.subsetsum.algorithm;

import com.subsetsum.model.SubsetSumInput;
import com.subsetsum.model.SubsetSumResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumingThat;

/**
 * Тесты для ArrayDPSolver.
 *
 * Технологии проектирования тестов:
 * 1. Boundary Value Analysis (BVA) — граничные значения
 * 2. Statement Testing — покрытие операторов
 */
@DisplayName("ArrayDPSolver Tests")
class ArrayDPSolverTest {

    private ArrayDPSolver solver;

    @BeforeEach
    void setUp() {
        solver = new ArrayDPSolver();
    }

    // ==================== Boundary Value Analysis ====================

    @Nested
    @DisplayName("BVA: Граничные значения")
    class BoundaryValueAnalysis {

        @Test
        @DisplayName("BVA: targetSum = 0 → всегда найдено (пустое подмножество)")
        void targetSumZero_alwaysFound() {
            SubsetSumInput input = new SubsetSumInput(List.of(1, 2, 3), 0);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertEquals(0, result.getSubsetSum());
            assertThat(result.getSubset(), is(empty()));
        }

        @Test
        @DisplayName("BVA: один элемент = targetSum")
        void singleElementEqualTarget() {
            SubsetSumInput input = new SubsetSumInput(List.of(5), 5);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertThat(result.getSubset(), contains(5));
        }

        @Test
        @DisplayName("BVA: один элемент ≠ targetSum")
        void singleElementNotEqualTarget() {
            SubsetSumInput input = new SubsetSumInput(List.of(3), 5);
            SubsetSumResult result = solver.solve(input);

            assertFalse(result.isFound());
            assertThat(result.getSubset(), is(empty()));
        }

        @Test
        @DisplayName("BVA: targetSum = сумме всех элементов")
        void targetEqualsFullSum() {
            List<Integer> numbers = List.of(1, 2, 3, 4);
            int target = 10; // 1+2+3+4
            SubsetSumInput input = new SubsetSumInput(numbers, target);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertEquals(target, result.getSubsetSum());
        }

        @Test
        @DisplayName("BVA: targetSum > суммы всех элементов → не найдено")
        void targetExceedsFullSum() {
            SubsetSumInput input = new SubsetSumInput(List.of(1, 2, 3), 100);
            SubsetSumResult result = solver.solve(input);

            assertFalse(result.isFound());
        }

        @Test
        @DisplayName("BVA: targetSum = 1 при элементах [0, 1]")
        void targetOneWithZeroAndOne() {
            SubsetSumInput input = new SubsetSumInput(List.of(0, 1), 1);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertEquals(1, result.getSubsetSum());
        }

        @Test
        @DisplayName("BVA: все элементы равны нулю, targetSum = 0")
        void allZerosTargetZero() {
            SubsetSumInput input = new SubsetSumInput(List.of(0, 0, 0), 0);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
        }

        @Test
        @DisplayName("BVA: все элементы равны 1, targetSum = n (количество элементов)")
        void allOnesTargetEqualsCount() {
            SubsetSumInput input = new SubsetSumInput(List.of(1, 1, 1, 1, 1), 5);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertThat(result.getSubset(), hasSize(5));
        }

        @Test
        @DisplayName("BVA: targetSum = минимальному элементу")
        void targetEqualsMinElement() {
            SubsetSumInput input = new SubsetSumInput(List.of(3, 5, 7, 9), 3);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertThat(result.getSubset(), contains(3));
        }

        @Test
        @DisplayName("BVA: targetSum = максимальному элементу")
        void targetEqualsMaxElement() {
            SubsetSumInput input = new SubsetSumInput(List.of(3, 5, 7, 9), 9);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertThat(result.getSubset(), contains(9));
        }
    }

    // ==================== Statement Testing ====================

    @Nested
    @DisplayName("Statement Testing: покрытие операторов")
    class StatementTesting {

        @Test
        @DisplayName("ST: null input → IllegalArgumentException")
        void nullInput_throwsException() {
            assertThrows(IllegalArgumentException.class, () -> solver.solve(null));
        }

        @Test
        @DisplayName("ST: пустой список → IllegalArgumentException из validate()")
        void emptyList_throwsException() {
            SubsetSumInput input = new SubsetSumInput(Collections.emptyList(), 5);
            assertThrows(IllegalArgumentException.class, () -> solver.solve(input));
        }

        @Test
        @DisplayName("ST: отрицательный targetSum → IllegalArgumentException")
        void negativeTarget_throwsException() {
            SubsetSumInput input = new SubsetSumInput(List.of(1, 2, 3), -1);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> solver.solve(input));
            assertThat(ex.getMessage(), containsString("targetSum >= 0"));
        }

        @Test
        @DisplayName("ST: отрицательное число в списке → IllegalArgumentException")
        void negativeNumber_throwsException() {
            SubsetSumInput input = new SubsetSumInput(List.of(1, -2, 3), 2);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> solver.solve(input));
            assertThat(ex.getMessage(), containsString("non-negative"));
        }

        @Test
        @DisplayName("ST: стандартный случай — решение существует (путь через ДП + реконструкция)")
        void standardCaseWithSolution() {
            SubsetSumInput input = new SubsetSumInput(List.of(3, 7, 1, 8, 5), 11);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertEquals(11, result.getSubsetSum());
            assertNotNull(result.getSubset());
            assertThat(result.getSubset().size(), greaterThan(0));
            assertNotNull(result.getAlgorithmName());
        }

        @Test
        @DisplayName("ST: стандартный случай — решение не существует")
        void standardCaseWithoutSolution() {
            SubsetSumInput input = new SubsetSumInput(List.of(2, 4, 6, 8), 3);
            SubsetSumResult result = solver.solve(input);

            assertFalse(result.isFound());
            assertThat(result.getSubset(), is(empty()));
        }

        @Test
        @DisplayName("ST: проверка имени алгоритма")
        void algorithmNameIsCorrect() {
            assertEquals("ArrayDP (boolean[][])", solver.getName());
        }

        @Test
        @DisplayName("ST: время выполнения >= 0")
        void executionTimeIsNonNegative() {
            SubsetSumInput input = new SubsetSumInput(List.of(1, 2, 3, 4, 5), 10);
            SubsetSumResult result = solver.solve(input);

            assertThat(result.getExecutionTimeNs(), greaterThanOrEqualTo(0L));
        }

        @Test
        @DisplayName("ST: использование памяти >= 0")
        void memoryUsedIsNonNegative() {
            SubsetSumInput input = new SubsetSumInput(List.of(1, 2, 3, 4, 5), 10);
            SubsetSumResult result = solver.solve(input);

            assertThat(result.getMemoryUsedBytes(), greaterThanOrEqualTo(0L));
        }
    }

    // ==================== Параметризованные тесты ====================

    @Nested
    @DisplayName("Параметризованные тесты")
    class ParameterizedTests {

        @ParameterizedTest(name = "numbers=[{0}], target={1}, expected={2}")
        @CsvSource({
                "'1,2,3', 6, true",
                "'1,2,3', 5, true",
                "'1,2,3', 7, false",
                "'10,20,30', 50, true",
                "'10,20,30', 15, false",
                "'5', 5, true",
                "'5', 3, false"
        })
        @DisplayName("Параметризованный тест: различные входные данные (CsvSource)")
        void parameterizedCsv(String numbersStr, int target, boolean expected) {
            List<Integer> numbers = Arrays.stream(numbersStr.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();

            SubsetSumInput input = new SubsetSumInput(numbers, target);
            SubsetSumResult result = solver.solve(input);

            assertEquals(expected, result.isFound());
            if (expected) {
                assertEquals(target, result.getSubsetSum());
            }
        }

        record TestCase(List<Integer> numbers, int target, boolean expectedFound) {}

        static Stream<TestCase> largerTestCases() {
            return Stream.of(
                    new TestCase(List.of(1, 5, 11, 5), 11, true),
                    new TestCase(List.of(1, 2, 3, 4, 5, 6, 7), 20, true),
                    new TestCase(List.of(2, 4, 6, 8, 10), 21, false),
                    new TestCase(List.of(100, 200, 300, 400, 500), 700, true),
                    new TestCase(List.of(1, 1, 1, 1, 1, 1, 1, 1, 1, 1), 10, true)
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("largerTestCases")
        @DisplayName("Параметризованный тест: более сложные случаи (MethodSource)")
        void parameterizedMethod(TestCase tc) {
            SubsetSumInput input = new SubsetSumInput(tc.numbers, tc.target);
            SubsetSumResult result = solver.solve(input);

            assertEquals(tc.expectedFound, result.isFound());
        }
    }

    // ==================== Assumptions ====================

    @Nested
    @DisplayName("Тесты с Assumptions")
    class AssumptionTests {

        @Test
        @DisplayName("assumeTrue: тест выполняется только если solver не null")
        void assumeSolverNotNull() {
            assumeTrue(solver != null, "Solver must be initialized");
            SubsetSumInput input = new SubsetSumInput(List.of(1, 2, 3), 3);
            SubsetSumResult result = solver.solve(input);
            assertTrue(result.isFound());
        }

        @Test
        @DisplayName("assumingThat: дополнительная проверка только если найдено решение")
        void assumingThatSolutionFound() {
            SubsetSumInput input = new SubsetSumInput(List.of(3, 7, 1, 8, 5), 15);
            SubsetSumResult result = solver.solve(input);

            assumingThat(result.isFound(), () -> {
                assertThat(result.getSubsetSum(), equalTo(15));
                assertThat(result.getSubset(), is(not(empty())));
            });
        }
    }
}
