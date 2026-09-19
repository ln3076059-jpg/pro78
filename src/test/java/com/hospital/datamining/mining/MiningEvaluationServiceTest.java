package com.hospital.datamining.mining;

import com.hospital.datamining.dto.BenchmarkComparisonDTO;
import com.hospital.datamining.entity.AlgorithmBenchmark;
import com.hospital.datamining.repository.AlgorithmBenchmarkRepository;
import com.hospital.datamining.service.mining.AprioriMiningService;
import com.hospital.datamining.service.mining.FPGrowthMiningService;
import com.hospital.datamining.service.mining.MiningEvaluationService;
import com.hospital.datamining.service.mining.model.MiningParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@DisplayName("Kiểm thử MiningEvaluationService (Benchmark Đa Lần Chạy, Median & Jaccard Overlap)")
class MiningEvaluationServiceTest {

    private MiningEvaluationService evaluationService;
    private AlgorithmBenchmarkRepository benchmarkRepository;

    @BeforeEach
    void setUp() {
        AprioriMiningService aprioriService = new AprioriMiningService();
        FPGrowthMiningService fpGrowthService = new FPGrowthMiningService();
        benchmarkRepository = Mockito.mock(AlgorithmBenchmarkRepository.class);
        Mockito.when(benchmarkRepository.save(any(AlgorithmBenchmark.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        evaluationService = new MiningEvaluationService(aprioriService, fpGrowthService, benchmarkRepository);
    }

    @Test
    @DisplayName("Kiểm tra Benchmark chạy đa lần, tính Median và Jaccard Overlap 100%")
    void testBenchmarkMultiRunAndJaccard() {
        // Dataset chuẩn T1..T5
        List<Set<String>> transactions = List.of(
                Set.of("Metformin", "Insulin", "Glipizide"),
                Set.of("Metformin", "Insulin"),
                Set.of("Metformin", "Glipizide"),
                Set.of("Insulin", "Glipizide"),
                Set.of("Metformin", "Insulin", "Glipizide")
        );

        MiningParameters params = MiningParameters.builder()
                .minSupport(0.40)
                .minConfidence(0.50)
                .minLift(1.0)
                .maxItemsetSize(5)
                .build();

        BenchmarkComparisonDTO result = evaluationService.runBenchmark(transactions, params, "Test-Dataset");

        assertNotNull(result);
        assertEquals("Test-Dataset", result.getDatasetName());
        assertEquals(5, result.getTransactionCount());
        assertEquals(result.getAprioriItemsetCount(), result.getFpgrowthItemsetCount(), "Số lượng itemsets phải trùng khớp");
        assertEquals(result.getAprioriRuleCount(), result.getFpgrowthRuleCount(), "Số lượng luật phải trùng khớp");
        assertEquals(100.0, result.getRuleOverlapPercentage(), 0.01, "Jaccard similarity phải đạt 100%");
        assertTrue(result.getAprioriRuntimeMs() >= 0);
        assertTrue(result.getFpgrowthRuntimeMs() >= 0);
        assertNotNull(result.getRecommendedAlgorithm());
        assertFalse(result.getConclusionNotes().contains("MIMIC"), "Không được chứa từ khóa MIMIC trong kết luận");
    }
}
