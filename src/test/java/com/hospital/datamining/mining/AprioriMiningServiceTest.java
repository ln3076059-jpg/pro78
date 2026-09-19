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

@DisplayName("Kiểm thử Thuật toán Apriori (AprioriMiningService) trên Dataset Chuẩn")
class AprioriMiningServiceTest {

    private AprioriMiningService aprioriService;

    @BeforeEach
    void setUp() {
        aprioriService = new AprioriMiningService();
    }

    @Test
    @DisplayName("Khai phá đúng 7 tập phổ biến trên dataset T1..T5 với minSupport = 0.40")
    void testAprioriOnStandardDataset() {
        // Dataset kiểm thử quy định tại Mục 27:
        // T1 = {A, B, C}
        // T2 = {A, B}
        // T3 = {A, C}
        // T4 = {B, C}
        // T5 = {A, B, C}
        List<Set<String>> transactions = Arrays.asList(
                new HashSet<>(Arrays.asList("A", "B", "C")),
                new HashSet<>(Arrays.asList("A", "B")),
                new HashSet<>(Arrays.asList("A", "C")),
                new HashSet<>(Arrays.asList("B", "C")),
                new HashSet<>(Arrays.asList("A", "B", "C"))
        );

        MiningParameters params = MiningParameters.builder()
                .minSupport(0.40) // ngưỡng đếm tối thiểu = 2
                .minConfidence(0.50)
                .minLift(0.0)
                .maxItemsetSize(5)
                .build();

        MiningResult result = aprioriService.mine(transactions, params);

        assertNotNull(result);
        assertEquals("APRIORI", result.getAlgorithm());

        // Tổng số tập phổ biến:
        // 1-itemsets: {A}: 4, {B}: 4, {C}: 4 (3 tập)
        // 2-itemsets: {A, B}: 3, {A, C}: 3, {B, C}: 3 (3 tập)
        // 3-itemset: {A, B, C}: 2 (1 tập)
        // Tổng cộng = 7 tập phổ biến
        assertEquals(7, result.getFrequentItemsetCount(), "Dataset T1..T5 với minSupport=0.40 phải sinh chính xác 7 tập phổ biến");

        // Kiểm tra tập kích thước 3 {A, B, C}
        Optional<FrequentItemsetResult> itemset3 = result.getFrequentItemsets().stream()
                .filter(fi -> fi.getItemCount() == 3)
                .findFirst();

        assertTrue(itemset3.isPresent());
        assertEquals(2, itemset3.get().getSupportCount());
        assertEquals(0.40, itemset3.get().getSupport(), 1e-4);
    }

    @Test
    @DisplayName("Xử lý an toàn khi danh sách transaction rỗng")
    void testEmptyTransactions() {
        MiningResult result = aprioriService.mine(Collections.emptyList(),
                MiningParameters.builder().minSupport(0.1).minConfidence(0.5).build());

        assertNotNull(result);
        assertEquals(0, result.getTransactionCount());
        assertEquals(0, result.getFrequentItemsetCount());
        assertEquals(0, result.getRuleCount());
    }

    @Test
    @DisplayName("Ném NullPointerException rõ ràng khi parameters là null (Req #18)")
    void testNullMiningParametersThrowsException() {
        List<Set<String>> transactions = Collections.singletonList(Collections.singleton("A"));
        NullPointerException ex = assertThrows(NullPointerException.class, () ->
                aprioriService.mine(transactions, null));
        assertTrue(ex.getMessage().contains("không được null"));
    }
}
