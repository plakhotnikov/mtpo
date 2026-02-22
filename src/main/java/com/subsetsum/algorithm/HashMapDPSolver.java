package com.subsetsum.algorithm;

import com.subsetsum.model.SubsetSumInput;
import com.subsetsum.model.SubsetSumResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Решение задачи Subset Sum методом динамического программирования
 * с использованием HashMap для хранения достижимых сумм.
 *
 * Временная сложность: O(n * |достижимые суммы|)
 * Пространственная сложность: O(|достижимые суммы|)
 *
 * Преимущество: поддерживает отрицательные числа и произвольный targetSum.
 * HashMap хранит только реально достижимые суммы, что может быть
 * значительно эффективнее по памяти для разреженных наборов данных.
 */
public class HashMapDPSolver implements SubsetSumSolver {

    private static final String NAME = "HashMapDP (HashMap<Integer, Integer>)";

    @Override
    public SubsetSumResult solve(SubsetSumInput input) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        input.validate();

        List<Integer> numbers = input.getNumbers();
        int target = input.getTargetSum();

        if (target == 0) {
            return buildResult(true, new ArrayList<>());
        }

        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memBefore = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        // reachable: сумма -> индекс элемента, который был добавлен последним для достижения этой суммы
        Map<Integer, Integer> reachable = new HashMap<>();
        // prevSumMap: сумма -> предыдущая сумма (до добавления элемента)
        Map<Integer, Integer> prevSumMap = new HashMap<>();

        reachable.put(0, -1);

        boolean found = false;

        for (int i = 0; i < numbers.size(); i++) {
            int num = numbers.get(i);
            Map<Integer, Integer> newEntries = new HashMap<>();

            for (Map.Entry<Integer, Integer> entry : reachable.entrySet()) {
                int existingSum = entry.getKey();
                int newSum = existingSum + num;

                if (!reachable.containsKey(newSum) && !newEntries.containsKey(newSum)) {
                    newEntries.put(newSum, i);
                    prevSumMap.put(newSum, existingSum);
                }
            }

            reachable.putAll(newEntries);

            if (reachable.containsKey(target)) {
                found = true;
                break;
            }
        }

        List<Integer> subset = new ArrayList<>();
        if (found) {
            int currentSum = target;
            while (currentSum != 0) {
                int elementIndex = reachable.get(currentSum);
                subset.add(numbers.get(elementIndex));
                currentSum = prevSumMap.get(currentSum);
            }
        }

        long endTime = System.nanoTime();
        long memAfter = runtime.totalMemory() - runtime.freeMemory();

        SubsetSumResult result = buildResult(found, subset);
        result.setExecutionTimeNs(endTime - startTime);
        result.setMemoryUsedBytes(Math.max(0, memAfter - memBefore));
        return result;
    }

    private SubsetSumResult buildResult(boolean found, List<Integer> subset) {
        return new SubsetSumResult(found, subset, NAME);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
