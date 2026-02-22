package com.subsetsum.algorithm;

import com.subsetsum.model.SubsetSumInput;
import com.subsetsum.model.SubsetSumResult;

public interface SubsetSumSolver {

    SubsetSumResult solve(SubsetSumInput input);

    String getName();
}
