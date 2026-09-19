package com.hospital.datamining.service.mining.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiningResult {
    private String algorithm;
    @Builder.Default
    private List<FrequentItemsetResult> frequentItemsets = new ArrayList<>();
    @Builder.Default
    private List<AssociationRuleResult> associationRules = new ArrayList<>();
    private long runtimeMs;
    private double memoryUsageMb;
    private int transactionCount;
    private int uniqueDrugCount;

    public int getFrequentItemsetCount() {
        return frequentItemsets != null ? frequentItemsets.size() : 0;
    }

    public int getRuleCount() {
        return associationRules != null ? associationRules.size() : 0;
    }
}
