package com.subsetsum.fuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.subsetsum.algorithm.HashMapDPSolver;
import com.subsetsum.model.SubsetSumInput;
import com.subsetsum.model.SubsetSumResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Фаззинг-тест для HashMapDPSolver.
 *
 * Цель: обнаружить ошибки при обработке случайных входных данных,
 * включая отрицательные числа и граничные значения.
 * Ожидаемые находки:
 * - Целочисленное переполнение при сложении больших чисел (existingSum + num)
 * - Некорректная реконструкция подмножества при переполнении
 */
class HashMapDPSolverFuzzTest {

    private final HashMapDPSolver solver = new HashMapDPSolver();

    @FuzzTest(maxDuration = "5m")
    void fuzzHashMapDPSolver(FuzzedDataProvider data) {
        int size = data.consumeInt(1, 30);
        int target = data.consumeInt();

        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            numbers.add(data.consumeInt());
        }

        SubsetSumInput input = new SubsetSumInput(numbers, target);

        try {
            SubsetSumResult result = solver.solve(input);

            // Проверка инварианта: если решение найдено, сумма подмножества должна равняться target
            if (result.isFound()) {
                int actualSum = result.getSubset().stream().mapToInt(Integer::intValue).sum();
                if (actualSum != target) {
                    throw new AssertionError(
                            "Нарушение инварианта: сумма подмножества " + actualSum +
                                    " != целевая сумма " + target +
                                    ", подмножество: " + result.getSubset() +
                                    ", входные числа: " + numbers);
                }
            }
        } catch (IllegalArgumentException e) {
            // Ожидаемое исключение — игнорируем
        }
    }
}
