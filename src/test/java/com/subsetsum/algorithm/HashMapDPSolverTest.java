package com.subsetsum.algorithm;

import com.subsetsum.model.SubsetSumInput;
import com.subsetsum.model.SubsetSumResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для HashMapDPSolver.
 *
 * Технологии проектирования тестов:
 * 1. Equivalence Partitioning (EP) — разбиение на классы эквивалентности
 * 2. Branch Testing — покрытие ветвей
 */
@DisplayName("HashMapDPSolver Tests")
class HashMapDPSolverTest {

    private HashMapDPSolver solver;

    @BeforeEach
    void setUp() {
        solver = new HashMapDPSolver();
    }

    // ==================== Equivalence Partitioning ====================

    @Nested
    @DisplayName("EP: Разбиение на классы эквивалентности")
    class EquivalencePartitioning {

        // Класс 1: Допустимый ввод, решение существует (положительные числа)
        @Test
        @DisplayName("EP-1: положительные числа, решение существует")
        void validInput_positiveNumbers_solutionExists() {
            SubsetSumInput input = new SubsetSumInput(List.of(3, 7, 1, 8, 5), 11);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertEquals(11, result.getSubsetSum());
            assertThat(result.getSubset(), is(not(empty())));
        }

        // Класс 2: Допустимый ввод, решение не существует
        @Test
        @DisplayName("EP-2: положительные числа, решение не существует")
        void validInput_positiveNumbers_noSolution() {
            SubsetSumInput input = new SubsetSumInput(List.of(2, 4, 6, 8), 3);
            SubsetSumResult result = solver.solve(input);

            assertFalse(result.isFound());
            assertThat(result.getSubset(), is(empty()));
        }

        // Класс 3: Отрицательные числа, решение существует
        @Test
        @DisplayName("EP-3: отрицательные числа, решение существует")
        void validInput_negativeNumbers_solutionExists() {
            SubsetSumInput input = new SubsetSumInput(List.of(-3, 7, -1, 8, 5), 4);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertEquals(4, result.getSubsetSum());
        }

        // Класс 4: Смешанные числа, отрицательный target
        @Test
        @DisplayName("EP-4: смешанные числа, отрицательный targetSum")
        void validInput_mixedNumbers_negativeTarget() {
            SubsetSumInput input = new SubsetSumInput(List.of(-5, 3, -2, 7), -7);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertEquals(-7, result.getSubsetSum());
        }

        // Класс 5: Один элемент
        @Test
        @DisplayName("EP-5: один элемент, совпадает с target")
        void singleElement_matchesTarget() {
            SubsetSumInput input = new SubsetSumInput(List.of(42), 42);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertThat(result.getSubset(), hasSize(1));
            assertThat(result.getSubset(), contains(42));
        }

        // Класс 6: Один элемент, не совпадает
        @Test
        @DisplayName("EP-6: один элемент, не совпадает с target")
        void singleElement_doesNotMatchTarget() {
            SubsetSumInput input = new SubsetSumInput(List.of(42), 10);
            SubsetSumResult result = solver.solve(input);

            assertFalse(result.isFound());
        }

        // Класс 7: targetSum = 0
        @Test
        @DisplayName("EP-7: targetSum = 0 (пустое подмножество)")
        void targetZero_emptySubset() {
            SubsetSumInput input = new SubsetSumInput(List.of(1, 2, 3), 0);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertThat(result.getSubset(), is(empty()));
        }

        // Класс 8: Большой набор данных
        @Test
        @DisplayName("EP-8: большой набор данных (50 элементов)")
        void largeDataSet() {
            List<Integer> numbers = new java.util.ArrayList<>();
            for (int i = 1; i <= 50; i++) {
                numbers.add(i);
            }
            int target = 100;
            SubsetSumInput input = new SubsetSumInput(numbers, target);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertEquals(target, result.getSubsetSum());
        }

        // Класс 9: Недопустимый ввод — null
        @Test
        @DisplayName("EP-9: null input → исключение")
        void nullInput_throwsException() {
            assertThrows(IllegalArgumentException.class, () -> solver.solve(null));
        }

        // Класс 10: Недопустимый ввод — пустой список
        @Test
        @DisplayName("EP-10: пустой список → исключение")
        void emptyList_throwsException() {
            SubsetSumInput input = new SubsetSumInput(Collections.emptyList(), 5);
            assertThrows(IllegalArgumentException.class, () -> solver.solve(input));
        }
    }

    // ==================== Branch Testing ====================

    @Nested
    @DisplayName("Branch Testing: покрытие ветвей")
    class BranchTesting {

        @Test
        @DisplayName("BT: ветвь input == null → true")
        void branch_inputNull() {
            assertThrows(IllegalArgumentException.class, () -> solver.solve(null));
        }

        @Test
        @DisplayName("BT: ветвь input != null → false (нормальный путь)")
        void branch_inputNotNull() {
            SubsetSumInput input = new SubsetSumInput(List.of(1), 1);
            assertDoesNotThrow(() -> solver.solve(input));
        }

        @Test
        @DisplayName("BT: ветвь target == 0 → true")
        void branch_targetZero() {
            SubsetSumInput input = new SubsetSumInput(List.of(1, 2), 0);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertThat(result.getSubset(), is(empty()));
        }

        @Test
        @DisplayName("BT: ветвь target != 0 → false (основной цикл)")
        void branch_targetNotZero() {
            SubsetSumInput input = new SubsetSumInput(List.of(1, 2), 3);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
        }

        @Test
        @DisplayName("BT: ветвь newSum не в reachable → добавляется")
        void branch_newSumNotInReachable() {
            SubsetSumInput input = new SubsetSumInput(List.of(5, 3), 8);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertEquals(8, result.getSubsetSum());
        }

        @Test
        @DisplayName("BT: ветвь newSum уже в reachable → пропускается")
        void branch_newSumAlreadyInReachable() {
            // [5, 3, 5] — сумма 5 достигается дважды (элемент 0 и элемент 2)
            SubsetSumInput input = new SubsetSumInput(List.of(5, 3, 5), 8);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertEquals(8, result.getSubsetSum());
        }

        @Test
        @DisplayName("BT: ветвь found → true (реконструкция подмножества)")
        void branch_foundTrue_reconstruction() {
            SubsetSumInput input = new SubsetSumInput(List.of(2, 3, 7, 8), 10);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertEquals(10, result.getSubsetSum());
            assertThat(result.getSubset(), is(not(empty())));
        }

        @Test
        @DisplayName("BT: ветвь found → false (пустое подмножество)")
        void branch_foundFalse() {
            SubsetSumInput input = new SubsetSumInput(List.of(2, 4, 6), 1);
            SubsetSumResult result = solver.solve(input);

            assertFalse(result.isFound());
            assertThat(result.getSubset(), is(empty()));
        }

        @Test
        @DisplayName("BT: ветвь раннее прерывание цикла при нахождении target")
        void branch_earlyBreakWhenTargetFound() {
            // target 3 достигается после первых двух элементов
            SubsetSumInput input = new SubsetSumInput(List.of(1, 2, 100, 200), 3);
            SubsetSumResult result = solver.solve(input);

            assertTrue(result.isFound());
            assertEquals(3, result.getSubsetSum());
        }

        @Test
        @DisplayName("BT: validate() → пустой список")
        void branch_validateEmptyList() {
            SubsetSumInput input = new SubsetSumInput(Collections.emptyList(), 5);
            assertThrows(IllegalArgumentException.class, () -> solver.solve(input));
        }
    }

    // ==================== Параметризованные тесты ====================

    @Nested
    @DisplayName("Параметризованные тесты")
    class ParameterizedTests {

        static Stream<Arguments> subsetSumCases() {
            return Stream.of(
                    Arguments.of(List.of(1, 2, 3), 6, true),
                    Arguments.of(List.of(1, 2, 3), 4, true),
                    Arguments.of(List.of(1, 2, 3), 7, false),
                    Arguments.of(List.of(-1, 2, -3, 5), 1, true),
                    Arguments.of(List.of(-1, -2, -3), -6, true),
                    Arguments.of(List.of(-1, -2, -3), 1, false),
                    Arguments.of(List.of(10), 10, true),
                    Arguments.of(List.of(10), 5, false)
            );
        }

        @ParameterizedTest(name = "numbers={0}, target={1}, expected={2}")
        @MethodSource("subsetSumCases")
        @DisplayName("Параметризованный тест: различные входные данные")
        void parameterizedSubsetSum(List<Integer> numbers, int target, boolean expected) {
            SubsetSumInput input = new SubsetSumInput(numbers, target);
            SubsetSumResult result = solver.solve(input);

            assertEquals(expected, result.isFound());
            if (expected) {
                assertEquals(target, result.getSubsetSum());
            }
        }
    }

    @Test
    @DisplayName("Проверка имени алгоритма")
    void algorithmNameIsCorrect() {
        assertThat(solver.getName(), containsString("HashMap"));
    }
}
