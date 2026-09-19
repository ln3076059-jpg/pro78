package com.hospital.datamining.mining;

import com.hospital.datamining.service.mining.AprioriMiningService;
import com.hospital.datamining.service.mining.FPGrowthMiningService;
import com.hospital.datamining.service.mining.model.FrequentItemsetResult;
import com.hospital.datamining.service.mining.model.MiningParameters;
import com.hospital.datamining.service.mining.model.MiningResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Thuật toán FP-Growth & So Sánh Tương Thích Với Apriori (Mục 28 đề bài)")
class FPGrowthMiningServiceTest {

    private FPGrowthMiningService fpGrowthService;
    private AprioriMiningService aprioriService;

    @BeforeEach
    void setUp() {
        fpGrowthService = new FPGrowthMiningService();
        aprioriService = new AprioriMiningService();
    }

    @Test
    @DisplayName("FP-Growth và Apriori sinh ra tập phổ biến (Frequent Itemsets) TRÙNG KHỚP 100% trên cùng dataset")
    void testFPGrowthMatchesAprioriExactly() {
        // Dataset chuẩn T1..T5
        List<Set<String>> transactions = Arrays.asList(
                new HashSet<>(Arrays.asList("A", "B", "C")),
                new HashSet<>(Arrays.asList("A", "B")),
                new HashSet<>(Arrays.asList("A", "C")),
                new HashSet<>(Arrays.asList("B", "C")),
                new HashSet<>(Arrays.asList("A", "B", "C"))
        );

        MiningParameters params = MiningParameters.builder()
                .minSupport(0.40)
                .minConfidence(0.50)
                .minLift(0.0)
                .maxItemsetSize(5)
                .build();

        MiningResult aprioriResult = aprioriService.mine(transactions, params);
        MiningResult fpGrowthResult = fpGrowthService.mine(transactions, params);

        assertNotNull(aprioriResult);
        assertNotNull(fpGrowthResult);

        // 1. Số lượng tập phổ biến phải bằng nhau
        assertEquals(aprioriResult.getFrequentItemsetCount(), fpGrowthResult.getFrequentItemsetCount(),
                "Số lượng tập phổ biến của Apriori và FP-Growth phải bằng nhau");
        assertEquals(7, fpGrowthResult.getFrequentItemsetCount());

        // 2. Từng itemset và support count phải trùng khớp hoàn toàn
        Map<String, Integer> aprioriItemsetMap = new HashMap<>();
        for (FrequentItemsetResult fi : aprioriResult.getFrequentItemsets()) {
            aprioriItemsetMap.put(fi.getItemsAsString(), fi.getSupportCount());
        }

        Map<String, Integer> fpItemsetMap = new HashMap<>();
        for (FrequentItemsetResult fi : fpGrowthResult.getFrequentItemsets()) {
            fpItemsetMap.put(fi.getItemsAsString(), fi.getSupportCount());
        }

        assertEquals(aprioriItemsetMap.keySet(), fpItemsetMap.keySet(),
                "Tập các itemset sinh ra từ 2 thuật toán phải hoàn toàn giống nhau");

        for (String itemsetStr : aprioriItemsetMap.keySet()) {
            assertEquals(aprioriItemsetMap.get(itemsetStr), fpItemsetMap.get(itemsetStr),
                    "Support count của itemset " + itemsetStr + " phải bằng nhau giữa Apriori và FP-Growth");
        }

        // 3. Số lượng luật kết hợp sinh ra phải tương thích
        assertEquals(aprioriResult.getRuleCount(), fpGrowthResult.getRuleCount(),
                "Số lượng luật kết hợp từ 2 thuật toán phải hoàn toàn trùng khớp");
    }

    @Test
    @DisplayName("FP-Growth xử lý an toàn khi không có giao dịch")
    void testFPGrowthEmptyInput() {
        MiningResult res = fpGrowthService.mine(Collections.emptyList(),
                MiningParameters.builder().minSupport(0.1).minConfidence(0.5).build());

        assertNotNull(res);
        assertEquals(0, res.getTransactionCount());
        assertEquals(0, res.getFrequentItemsetCount());
        assertEquals(0, res.getRuleCount());
    }
}
