package com.subsetsum.fuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.subsetsum.algorithm.ArrayDPSolver;
import com.subsetsum.model.SubsetSumInput;
import com.subsetsum.model.SubsetSumResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Фаззинг-тест для ArrayDPSolver.
 *
 * Цель: обнаружить ошибки при обработке граничных и случайных входных данных.
 * Ожидаемые находки:
 * - NegativeArraySizeException при target = Integer.MAX_VALUE (целочисленное переполнение target + 1)
 * - OutOfMemoryError при очень больших значениях target
 */
class ArrayDPSolverFuzzTest {

    private final ArrayDPSolver solver = new ArrayDPSolver();

    @FuzzTest(maxDuration = "5m")
    void fuzzArrayDPSolver(FuzzedDataProvider data) {
        int size = data.consumeInt(1, 50);
        int target = data.consumeInt(0, Integer.MAX_VALUE);

        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            numbers.add(data.consumeInt(0, 10000));
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
                                    " != целевая сумма " + target);
                }
            }
        } catch (IllegalArgumentException e) {
            // Ожидаемое исключение для невалидных входных данных — игнорируем
        }
        // NegativeArraySizeException и OutOfMemoryError НЕ перехватываются —
        // Jazzer их обнаружит как ошибки
    }
}
