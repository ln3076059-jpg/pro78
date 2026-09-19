package com.hospital.datamining.mining;

import com.hospital.datamining.service.mining.AprioriMiningService;
import com.hospital.datamining.service.mining.model.AssociationRuleResult;
import com.hospital.datamining.service.mining.model.MiningParameters;
import com.hospital.datamining.service.mining.model.MiningResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Công Thức Toán Học: Support, Confidence, Lift (Yêu cầu mục 27)")
class AssociationMetricTest {

    /**
     * Bộ dữ liệu kiểm thử chuẩn quy định tại Mục 27 đề bài:
     * T1 = {A, B, C}
     * T2 = {A, B}
     * T3 = {A, C}
     * T4 = {B, C}
     * T5 = {A, B, C}
     * Tổng số transactions N = 5
     *
     * Tần suất thực tế:
     * count(A) = 4  -> support(A) = 4/5 = 0.80
     * count(B) = 4  -> support(B) = 4/5 = 0.80
     * count(C) = 4  -> support(C) = 4/5 = 0.80
     *
     * count({A, B}) = 3 -> support({A, B}) = 3/5 = 0.60
     * count({A, C}) = 3 -> support({A, C}) = 3/5 = 0.60
     * count({B, C}) = 3 -> support({B, C}) = 3/5 = 0.60
     * count({A, B, C}) = 2 -> support({A, B, C}) = 2/5 = 0.40
     *
     * Đánh giá luật {A, B} -> {C}:
     * support({A, B} -> {C}) = count({A, B, C}) / 5 = 2/5 = 0.40
     * confidence({A, B} -> {C}) = count({A, B, C}) / count({A, B}) = 2/3 ≈ 0.6667
     * lift({A, B} -> {C}) = confidence / support(C) = (2/3) / 0.80 = (2/3) / (4/5) = 10/12 ≈ 0.8333
     *
     * Đánh giá luật {A} -> {B}:
     * support({A} -> {B}) = count({A, B}) / 5 = 3/5 = 0.60
     * confidence({A} -> {B}) = count({A, B}) / count(A) = 3/4 = 0.75
     * lift({A} -> {B}) = confidence / support(B) = 0.75 / 0.80 = 0.9375
     */
    @Test
    @DisplayName("Đối chiếu chính xác Support, Confidence, Lift với tính toán tay trên dataset T1..T5")
    void testExactManualCalculationOnStandardDataset() {
        List<Set<String>> transactions = Arrays.asList(
                new HashSet<>(Arrays.asList("A", "B", "C")), // T1
                new HashSet<>(Arrays.asList("A", "B")),       // T2
                new HashSet<>(Arrays.asList("A", "C")),       // T3
                new HashSet<>(Arrays.asList("B", "C")),       // T4
                new HashSet<>(Arrays.asList("A", "B", "C"))  // T5
        );

        AprioriMiningService apriori = new AprioriMiningService();
        MiningParameters params = MiningParameters.builder()
                .minSupport(0.35)     // chấp nhận các tập có support >= 0.40
                .minConfidence(0.50)  // chấp nhận confidence >= 50%
                .minLift(0.0)         // không lọc lift để kiểm tra giá trị toán học
                .maxItemsetSize(5)
                .build();

        MiningResult result = apriori.mine(transactions, params);

        assertNotNull(result);
        assertEquals(5, result.getTransactionCount());
        assertEquals(3, result.getUniqueDrugCount());

        // 1. Kiểm tra luật {A, B} -> {C}
        Set<String> anteAB = new TreeSet<>(Arrays.asList("A", "B"));
        Set<String> consC = Collections.singleton("C");

        Optional<AssociationRuleResult> ruleABtoC = result.getAssociationRules().stream()
                .filter(r -> r.getAntecedent().equals(anteAB) && r.getConsequent().equals(consC))
                .findFirst();

        assertTrue(ruleABtoC.isPresent(), "Phải tìm thấy luật {A, B} -> {C}");
        AssociationRuleResult r1 = ruleABtoC.get();
        assertEquals(0.40, r1.getSupport(), 1e-4, "Support({A, B} -> {C}) = 2/5 = 0.40");
        assertEquals(2.0 / 3.0, r1.getConfidence(), 1e-4, "Confidence({A, B} -> {C}) = 2/3 ≈ 0.6667");
        assertEquals((2.0 / 3.0) / 0.80, r1.getLift(), 1e-4, "Lift({A, B} -> {C}) = (2/3)/0.80 ≈ 0.8333");

        // 2. Kiểm tra luật {A} -> {B}
        Set<String> anteA = Collections.singleton("A");
        Set<String> consB = Collections.singleton("B");

        Optional<AssociationRuleResult> ruleAtoB = result.getAssociationRules().stream()
                .filter(r -> r.getAntecedent().equals(anteA) && r.getConsequent().equals(consB))
                .findFirst();

        assertTrue(ruleAtoB.isPresent(), "Phải tìm thấy luật {A} -> {B}");
        AssociationRuleResult r2 = ruleAtoB.get();
        assertEquals(0.60, r2.getSupport(), 1e-4, "Support({A} -> {B}) = 3/5 = 0.60");
        assertEquals(0.75, r2.getConfidence(), 1e-4, "Confidence({A} -> {B}) = 3/4 = 0.75");
        assertEquals(0.75 / 0.80, r2.getLift(), 1e-4, "Lift({A} -> {B}) = 0.75/0.80 = 0.9375");
    }
}
