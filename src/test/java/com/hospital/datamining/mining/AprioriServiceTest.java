package com.hospital.datamining.mining;

import com.hospital.datamining.service.mining.AprioriMiningService;
import com.hospital.datamining.service.mining.model.FrequentItemsetResult;
import com.hospital.datamining.service.mining.model.MiningParameters;
import com.hospital.datamining.service.mining.model.MiningResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AprioriServiceTest {

    private AprioriMiningService aprioriService;
    private List<Set<String>> sampleTransactions;

    @BeforeEach
    void setUp() {
        aprioriService = new AprioriMiningService();

        // Dataset kiểm thử chuẩn tính toán bằng tay (Mục 19):
        // T1 = A,B,C
        // T2 = A,B
        // T3 = A,C
        // T4 = B,C
        // T5 = A,B,C
        sampleTransactions = Arrays.asList(
                new HashSet<>(Arrays.asList("A", "B", "C")),
                new HashSet<>(Arrays.asList("A", "B")),
                new HashSet<>(Arrays.asList("A", "C")),
                new HashSet<>(Arrays.asList("B", "C")),
                new HashSet<>(Arrays.asList("A", "B", "C"))
        );
    }

    @Test
    @DisplayName("Kiểm tra Apriori với minSupport 0.5 (count >= 3)")
    void testAprioriWithMinSupport05() {
        MiningParameters params = MiningParameters.builder()
                .minSupport(0.50)
                .minConfidence(0.50)
                .minLift(0.0) // Chấp nhận mọi lift để đối chiếu tay
                .maxItemsetSize(5)
                .build();

        MiningResult result = aprioriService.mine(sampleTransactions, params);

        assertNotNull(result);
        assertEquals(5, result.getTransactionCount());
        assertEquals(3, result.getUniqueDrugCount());

        // Frequent 1-itemsets: {A} (count 4), {B} (count 4), {C} (count 4)
        // Frequent 2-itemsets: {A,B} (count 3), {A,C} (count 3), {B,C} (count 3)
        // {A,B,C} count là 2 < 2.5 (50%), bị tỉa!
        // Tổng số frequent itemsets mong đợi: 3 + 3 = 6
        assertEquals(6, result.getFrequentItemsetCount(), "Số lượng frequent itemset mong đợi là 6");

        // Kiểm tra Support của {A, B} = 3/5 = 0.6
        Optional<FrequentItemsetResult> abItemset = result.getFrequentItemsets().stream()
                .filter(fi -> fi.getItems().equals(new HashSet<>(Arrays.asList("A", "B"))))
                .findFirst();

        assertTrue(abItemset.isPresent());
        assertEquals(0.6, abItemset.get().getSupport(), 1e-4);
        assertEquals(3, abItemset.get().getSupportCount());
    }

    @Test
    @DisplayName("Kiểm tra Apriori với minSupport 0.3 để giữ lại {A, B, C}")
    void testAprioriWithMinSupport03() {
        MiningParameters params = MiningParameters.builder()
                .minSupport(0.30)
                .minConfidence(0.50)
                .minLift(0.0)
                .maxItemsetSize(5)
                .build();

        MiningResult result = aprioriService.mine(sampleTransactions, params);

        // Lúc này {A, B, C} có support = 2/5 = 0.4 >= 0.3 -> Frequent!
        // Tổng số: 3 (1-itemset) + 3 (2-itemset) + 1 (3-itemset) = 7
        assertEquals(7, result.getFrequentItemsetCount());

        Optional<FrequentItemsetResult> abcItemset = result.getFrequentItemsets().stream()
                .filter(fi -> fi.getItems().equals(new HashSet<>(Arrays.asList("A", "B", "C"))))
                .findFirst();

        assertTrue(abcItemset.isPresent());
        assertEquals(0.4, abcItemset.get().getSupport(), 1e-4);
        assertEquals(2, abcItemset.get().getSupportCount());
    }
}
