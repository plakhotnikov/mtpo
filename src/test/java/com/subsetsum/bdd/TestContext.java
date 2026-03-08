package com.subsetsum.bdd;

import com.subsetsum.model.SubsetSumInput;
import com.subsetsum.model.SubsetSumResult;

/**
 * Shared state between step definition classes within a single Cucumber scenario.
 * Uses ThreadLocal to ensure thread safety.
 */
public final class TestContext {

    private static final ThreadLocal<TestContext> INSTANCE = ThreadLocal.withInitial(TestContext::new);

    private SubsetSumInput generatedInput;
    private SubsetSumResult arrayResult;
    private SubsetSumResult hashMapResult;
    private String datasetName;

    private TestContext() {}

    public static TestContext get() {
        return INSTANCE.get();
    }

    public static void reset() {
        INSTANCE.remove();
    }

    public SubsetSumInput getGeneratedInput() { return generatedInput; }
    public void setGeneratedInput(SubsetSumInput input) { this.generatedInput = input; }

    public SubsetSumResult getArrayResult() { return arrayResult; }
    public void setArrayResult(SubsetSumResult result) { this.arrayResult = result; }

    public SubsetSumResult getHashMapResult() { return hashMapResult; }
    public void setHashMapResult(SubsetSumResult result) { this.hashMapResult = result; }

    public String getDatasetName() { return datasetName; }
    public void setDatasetName(String name) { this.datasetName = name; }
}
