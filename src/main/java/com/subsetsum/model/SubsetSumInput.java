package com.subsetsum.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SubsetSumInput {

    private List<Integer> numbers;
    private int targetSum;

    public SubsetSumInput() {
        this.numbers = new ArrayList<>();
        this.targetSum = 0;
    }

    public SubsetSumInput(List<Integer> numbers, int targetSum) {
        Objects.requireNonNull(numbers, "numbers must not be null");
        this.numbers = new ArrayList<>(numbers);
        this.targetSum = targetSum;
    }

    public List<Integer> getNumbers() {
        return Collections.unmodifiableList(numbers);
    }

    public void setNumbers(List<Integer> numbers) {
        Objects.requireNonNull(numbers, "numbers must not be null");
        this.numbers = new ArrayList<>(numbers);
    }

    public int getTargetSum() {
        return targetSum;
    }

    public void setTargetSum(int targetSum) {
        this.targetSum = targetSum;
    }

    public int size() {
        return numbers.size();
    }

    public void validate() {
        if (numbers.isEmpty()) {
            throw new IllegalArgumentException("numbers list must not be empty");
        }
    }

    @Override
    public String toString() {
        return "SubsetSumInput{numbers=" + numbers + ", targetSum=" + targetSum + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubsetSumInput that = (SubsetSumInput) o;
        return targetSum == that.targetSum && Objects.equals(numbers, that.numbers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numbers, targetSum);
    }
}
