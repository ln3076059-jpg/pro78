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

class FPGrowthServiceTest {

    private FPGrowthMiningService fpGrowthService;
    private AprioriMiningService aprioriService;
    private List<Set<String>> sampleTransactions;

    @BeforeEach
    void setUp() {
        fpGrowthService = new FPGrowthMiningService();
        aprioriService = new AprioriMiningService();

        // T1 = A,B,C; T2 = A,B; T3 = A,C; T4 = B,C; T5 = A,B,C
        sampleTransactions = Arrays.asList(
                new HashSet<>(Arrays.asList("A", "B", "C")),
                new HashSet<>(Arrays.asList("A", "B")),
                new HashSet<>(Arrays.asList("A", "C")),
                new HashSet<>(Arrays.asList("B", "C")),
                new HashSet<>(Arrays.asList("A", "B", "C"))
        );
    }

    @Test
    @DisplayName("Kiểm tra FP-Growth khớp 100% kết quả Frequent Itemsets với tính tay và Apriori")
    void testFPGrowthMatchesApriori() {
        MiningParameters params = MiningParameters.builder()
                .minSupport(0.40)
                .minConfidence(0.50)
                .minLift(0.0)
                .maxItemsetSize(5)
                .build();

        MiningResult fpResult = fpGrowthService.mine(sampleTransactions, params);
        MiningResult aprioriResult = aprioriService.mine(sampleTransactions, params);

        assertEquals(aprioriResult.getFrequentItemsetCount(), fpResult.getFrequentItemsetCount());
        assertEquals(aprioriResult.getRuleCount(), fpResult.getRuleCount());

        // Kiểm tra tập {A, B}
        Optional<FrequentItemsetResult> abOpt = fpResult.getFrequentItemsets().stream()
                .filter(fi -> fi.getItems().equals(new HashSet<>(Arrays.asList("A", "B"))))
                .findFirst();

        assertTrue(abOpt.isPresent());
        assertEquals(0.6, abOpt.get().getSupport(), 1e-4);
        assertEquals(3, abOpt.get().getSupportCount());
    }
}
