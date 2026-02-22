package com.subsetsum.algorithm;

import com.subsetsum.model.SubsetSumInput;
import com.subsetsum.model.SubsetSumResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Решение задачи Subset Sum методом динамического программирования
 * с использованием двумерного булевого массива boolean[][].
 *
 * Временная сложность: O(n * target)
 * Пространственная сложность: O(n * target)
 *
 * Ограничения: работает только с неотрицательными числами и targetSum >= 0.
 */
public class ArrayDPSolver implements SubsetSumSolver {

    private static final String NAME = "ArrayDP (boolean[][])";

    @Override
    public SubsetSumResult solve(SubsetSumInput input) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        input.validate();

        List<Integer> numbers = input.getNumbers();
        int target = input.getTargetSum();
        int n = numbers.size();

        if (target < 0) {
            throw new IllegalArgumentException("ArrayDP solver requires targetSum >= 0");
        }
        for (int num : numbers) {
            if (num < 0) {
                throw new IllegalArgumentException("ArrayDP solver requires non-negative numbers, got: " + num);
            }
        }

        if (target == 0) {
            return buildResult(true, new ArrayList<>());
        }

        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memBefore = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        boolean[][] dp = new boolean[n + 1][target + 1];
        dp[0][0] = true;

        for (int i = 1; i <= n; i++) {
            int num = numbers.get(i - 1);
            for (int j = 0; j <= target; j++) {
                dp[i][j] = dp[i - 1][j];
                if (j >= num && dp[i - 1][j - num]) {
                    dp[i][j] = true;
                }
            }
        }

        List<Integer> subset = new ArrayList<>();
        if (dp[n][target]) {
            int j = target;
            for (int i = n; i > 0 && j > 0; i--) {
                if (!dp[i - 1][j]) {
                    subset.add(numbers.get(i - 1));
                    j -= numbers.get(i - 1);
                }
            }
        }

        long endTime = System.nanoTime();
        long memAfter = runtime.totalMemory() - runtime.freeMemory();

        SubsetSumResult result = buildResult(dp[n][target], subset);
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
