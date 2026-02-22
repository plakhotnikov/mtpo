package com.subsetsum.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SubsetSumResult {

    private boolean found;
    private List<Integer> subset;
    private long executionTimeNs;
    private long memoryUsedBytes;
    private String algorithmName;

    public SubsetSumResult() {
        this.subset = new ArrayList<>();
    }

    public SubsetSumResult(boolean found, List<Integer> subset, String algorithmName) {
        this.found = found;
        this.subset = subset != null ? new ArrayList<>(subset) : new ArrayList<>();
        this.algorithmName = algorithmName;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public List<Integer> getSubset() {
        return Collections.unmodifiableList(subset);
    }

    public void setSubset(List<Integer> subset) {
        this.subset = subset != null ? new ArrayList<>(subset) : new ArrayList<>();
    }

    public int getSubsetSum() {
        return subset.stream().mapToInt(Integer::intValue).sum();
    }

    public long getExecutionTimeNs() {
        return executionTimeNs;
    }

    public void setExecutionTimeNs(long executionTimeNs) {
        this.executionTimeNs = executionTimeNs;
    }

    public double getExecutionTimeMs() {
        return executionTimeNs / 1_000_000.0;
    }

    public long getMemoryUsedBytes() {
        return memoryUsedBytes;
    }

    public void setMemoryUsedBytes(long memoryUsedBytes) {
        this.memoryUsedBytes = memoryUsedBytes;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    @Override
    public String toString() {
        return "SubsetSumResult{found=" + found +
                ", subset=" + subset +
                ", algorithmName='" + algorithmName + '\'' +
                ", executionTimeMs=" + String.format("%.3f", getExecutionTimeMs()) +
                ", memoryUsedBytes=" + memoryUsedBytes + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubsetSumResult that = (SubsetSumResult) o;
        return found == that.found && Objects.equals(subset, that.subset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(found, subset);
    }
}
