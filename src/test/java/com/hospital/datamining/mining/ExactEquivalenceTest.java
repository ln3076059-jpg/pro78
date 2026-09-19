package com.hospital.datamining.mining;

import com.hospital.datamining.service.mining.AprioriMiningService;
import com.hospital.datamining.service.mining.FPGrowthMiningService;
import com.hospital.datamining.service.mining.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Đối Chiếu Tuyệt Đối Apriori vs FP-Growth (Exact Equivalence Test)")
class ExactEquivalenceTest {

    private final AprioriMiningService aprioriService = new AprioriMiningService();
    private final FPGrowthMiningService fpGrowthService = new FPGrowthMiningService();

    @Test
    @DisplayName("Kiểm tra 100% trùng khớp từng itemset, support count, rule, confidence và lift")
    void testExactEquivalenceAcrossMetrics() {
        List<Set<String>> transactions = List.of(
                Set.of("Metformin", "Insulin", "Glipizide", "Atorvastatin"),
                Set.of("Metformin", "Insulin", "Lisinopril"),
                Set.of("Metformin", "Glipizide", "Lisinopril"),
                Set.of("Insulin", "Glipizide", "Atorvastatin"),
                Set.of("Metformin", "Insulin", "Glipizide"),
                Set.of("Metformin", "Atorvastatin"),
                Set.of("Insulin", "Lisinopril"),
                Set.of("Metformin", "Insulin", "Glipizide", "Lisinopril")
        );

        MiningParameters params = MiningParameters.builder()
                .minSupport(0.25)
                .minConfidence(0.50)
                .minLift(1.0)
                .maxItemsetSize(4)
                .build();

        MiningResult aprioriResult = aprioriService.mine(transactions, params);
        MiningResult fpResult = fpGrowthService.mine(transactions, params);

        // 1. Kiểm tra số lượng tập phổ biến
        assertEquals(aprioriResult.getFrequentItemsetCount(), fpResult.getFrequentItemsetCount(),
                "Tổng số lượng Frequent Itemsets phải bằng nhau");

        // 2. Đối chiếu chi tiết từng Frequent Itemset và Support Count
        Map<Set<String>, Integer> aprioriItemsets = new HashMap<>();
        for (FrequentItemsetResult fir : aprioriResult.getFrequentItemsets()) {
            aprioriItemsets.put(fir.getItems(), fir.getSupportCount());
        }

        Map<Set<String>, Integer> fpItemsets = new HashMap<>();
        for (FrequentItemsetResult fir : fpResult.getFrequentItemsets()) {
            fpItemsets.put(fir.getItems(), fir.getSupportCount());
        }

        assertEquals(aprioriItemsets.keySet(), fpItemsets.keySet(), "Các tập phần tử phổ biến phải hoàn toàn trùng khớp");

        for (Set<String> itemset : aprioriItemsets.keySet()) {
            assertEquals(aprioriItemsets.get(itemset), fpItemsets.get(itemset),
                    "Support count của itemset " + itemset + " phải trùng khớp chính xác");
        }

        // 3. Đối chiếu chi tiết từng Association Rule: Support, Confidence, Lift
        assertEquals(aprioriResult.getRuleCount(), fpResult.getRuleCount(), "Tổng số lượng Association Rules phải bằng nhau");

        Map<String, AssociationRuleResult> aprioriRuleMap = new HashMap<>();
        for (AssociationRuleResult arr : aprioriResult.getAssociationRules()) {
            String key = arr.getAntecedentAsString() + " => " + arr.getConsequentAsString();
            aprioriRuleMap.put(key, arr);
        }

        Map<String, AssociationRuleResult> fpRuleMap = new HashMap<>();
        for (AssociationRuleResult arr : fpResult.getAssociationRules()) {
            String key = arr.getAntecedentAsString() + " => " + arr.getConsequentAsString();
            fpRuleMap.put(key, arr);
        }

        assertEquals(aprioriRuleMap.keySet(), fpRuleMap.keySet(), "Tập chữ ký của các luật kết hợp phải hoàn toàn trùng khớp");

        double EPSILON = 1e-6;
        for (String ruleKey : aprioriRuleMap.keySet()) {
            AssociationRuleResult aRule = aprioriRuleMap.get(ruleKey);
            AssociationRuleResult fRule = fpRuleMap.get(ruleKey);

            assertNotNull(fRule, "Luật " + ruleKey + " phải có mặt trong kết quả FP-Growth");
            assertEquals(aRule.getSupport(), fRule.getSupport(), EPSILON, "Support của luật " + ruleKey + " phải bằng nhau");
            assertEquals(aRule.getConfidence(), fRule.getConfidence(), EPSILON, "Confidence của luật " + ruleKey + " phải bằng nhau");
            assertEquals(aRule.getLift(), fRule.getLift(), EPSILON, "Lift của luật " + ruleKey + " phải bằng nhau");
        }
    }
}
