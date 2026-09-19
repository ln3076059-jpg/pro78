package com.hospital.datamining.mining;

import com.hospital.datamining.service.mining.AprioriMiningService;
import com.hospital.datamining.service.mining.model.AssociationRuleResult;
import com.hospital.datamining.service.mining.model.MiningParameters;
import com.hospital.datamining.service.mining.model.MiningResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AssociationRuleMetricTest {

    @Test
    @DisplayName("Kiểm tra công thức Support, Confidence và Lift theo tính toán tay")
    void testRuleMetricsCalculation() {
        // T1: A, B
        // T2: A, B
        // T3: A, B
        // T4: A
        // T5: C
        // Total = 5
        // count(A) = 4 -> Support(A) = 0.8
        // count(B) = 3 -> Support(B) = 0.6
        // count({A, B}) = 3 -> Support({A, B}) = 0.6
        // Rule A -> B:
        // Support = 0.6
        // Confidence = Support({A, B}) / Support(A) = 3 / 4 = 0.75
        // Lift = Confidence / Support(B) = 0.75 / 0.6 = 1.25 > 1.0 (Đồng xuất hiện dương tính!)
        List<Set<String>> transactions = Arrays.asList(
                new HashSet<>(Arrays.asList("A", "B")),
                new HashSet<>(Arrays.asList("A", "B")),
                new HashSet<>(Arrays.asList("A", "B")),
                new HashSet<>(Collections.singletonList("A")),
                new HashSet<>(Collections.singletonList("C"))
        );

        AprioriMiningService service = new AprioriMiningService();
        MiningParameters params = MiningParameters.builder()
                .minSupport(0.40)
                .minConfidence(0.50)
                .minLift(1.0)
                .maxItemsetSize(3)
                .build();

        MiningResult result = service.mine(transactions, params);

        assertNotNull(result);
        assertFalse(result.getAssociationRules().isEmpty());

        Optional<AssociationRuleResult> ruleOpt = result.getAssociationRules().stream()
                .filter(r -> r.getAntecedent().equals(Collections.singleton("A")) &&
                             r.getConsequent().equals(Collections.singleton("B")))
                .findFirst();

        assertTrue(ruleOpt.isPresent(), "Luật A -> B phải xuất hiện");
        AssociationRuleResult rule = ruleOpt.get();

        assertEquals(0.6, rule.getSupport(), 1e-4, "Support(A -> B) phải là 0.6");
        assertEquals(0.75, rule.getConfidence(), 1e-4, "Confidence(A -> B) phải là 0.75 (75%)");
        assertEquals(1.25, rule.getLift(), 1e-4, "Lift(A -> B) phải là 1.25 (> 1.0)");
    }

    @Test
    @DisplayName("Kiểm tra thủ công chính xác theo Section 40: T1=A,B,C; T2=A,B; T3=A,C; T4=B,C; T5=A,B,C")
    void testExactSection40DatasetManualCalculations() {
        // T1 = A,B,C
        // T2 = A,B
        // T3 = A,C
        // T4 = B,C
        // T5 = A,B,C
        // Total = 5 transactions
        List<Set<String>> transactions = Arrays.asList(
                new HashSet<>(Arrays.asList("A", "B", "C")),
                new HashSet<>(Arrays.asList("A", "B")),
                new HashSet<>(Arrays.asList("A", "C")),
                new HashSet<>(Arrays.asList("B", "C")),
                new HashSet<>(Arrays.asList("A", "B", "C"))
        );

        AprioriMiningService service = new AprioriMiningService();
        MiningParameters params = MiningParameters.builder()
                .minSupport(0.40)
                .minConfidence(0.50)
                .minLift(0.0)
                .maxItemsetSize(3)
                .build();

        MiningResult result = service.mine(transactions, params);
        assertNotNull(result);

        // 1. Kiểm tra luật {A} -> {B}
        // count(A,B) = 3 -> Support = 3/5 = 0.60
        // count(A) = 4 -> Support(A) = 4/5 = 0.80
        // Confidence(A -> B) = 3/4 = 0.75
        // count(B) = 4 -> Support(B) = 4/5 = 0.80
        // Lift(A -> B) = 0.75 / 0.80 = 0.9375
        Optional<AssociationRuleResult> ruleAB = result.getAssociationRules().stream()
                .filter(r -> r.getAntecedent().equals(Collections.singleton("A")) &&
                             r.getConsequent().equals(Collections.singleton("B")))
                .findFirst();
        assertTrue(ruleAB.isPresent(), "Luật {A} -> {B} phải tồn tại");
        assertEquals(0.60, ruleAB.get().getSupport(), 1e-4);
        assertEquals(0.75, ruleAB.get().getConfidence(), 1e-4);
        assertEquals(0.9375, ruleAB.get().getLift(), 1e-4);

        // 2. Kiểm tra luật {A, B} -> {C}
        // count(A,B,C) = 2 -> Support = 2/5 = 0.40
        // count(A,B) = 3 -> Support(A,B) = 3/5 = 0.60
        // Confidence({A,B} -> C) = 2/3 = 0.6667
        // count(C) = 4 -> Support(C) = 4/5 = 0.80
        // Lift({A,B} -> C) = (2/3) / 0.80 = 5/6 = 0.8333
        Optional<AssociationRuleResult> ruleABC = result.getAssociationRules().stream()
                .filter(r -> r.getAntecedent().equals(new HashSet<>(Arrays.asList("A", "B"))) &&
                             r.getConsequent().equals(Collections.singleton("C")))
                .findFirst();
        assertTrue(ruleABC.isPresent(), "Luật {A, B} -> {C} phải tồn tại");
        assertEquals(0.40, ruleABC.get().getSupport(), 1e-4);
        assertEquals(2.0 / 3.0, ruleABC.get().getConfidence(), 1e-4);
        assertEquals((2.0 / 3.0) / 0.80, ruleABC.get().getLift(), 1e-4);
    }
}
