package com.hospital.datamining.service.mining;

import com.hospital.datamining.dto.BenchmarkComparisonDTO;
import com.hospital.datamining.entity.AlgorithmBenchmark;
import com.hospital.datamining.entity.DatasetStatistics;
import com.hospital.datamining.repository.AlgorithmBenchmarkRepository;
import com.hospital.datamining.service.mining.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MiningEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(MiningEvaluationService.class);

    private final AprioriMiningService aprioriService;
    private final FPGrowthMiningService fpGrowthService;
    private final AlgorithmBenchmarkRepository benchmarkRepository;

    public MiningEvaluationService(AprioriMiningService aprioriService,
                                   FPGrowthMiningService fpGrowthService,
                                   AlgorithmBenchmarkRepository benchmarkRepository) {
        this.aprioriService = aprioriService;
        this.fpGrowthService = fpGrowthService;
        this.benchmarkRepository = benchmarkRepository;
    }

    /**
     * Chạy thực nghiệm đo lường so sánh Apriori và FP-Growth trên cùng bộ dữ liệu và cùng tham số
     */
    @Transactional
    public BenchmarkComparisonDTO runBenchmark(List<Set<String>> transactions,
                                               MiningParameters params,
                                               String datasetName) {
        log.info("Bắt đầu Benchmark so sánh Apriori vs FP-Growth trên {} transactions (minSupport={})...",
                transactions.size(), params.getMinSupport());

        // 1. Chạy Apriori
        MiningResult aprioriResult = aprioriService.mine(transactions, params);

        // 2. Chạy FP-Growth trên cùng bộ dữ liệu và tham số
        MiningResult fpGrowthResult = fpGrowthService.mine(transactions, params);

        // 3. Đo độ tương đồng / ổn định của tập luật (Rule Overlap)
        Set<String> aprioriRuleSignatures = new HashSet<>();
        for (AssociationRuleResult rule : aprioriResult.getAssociationRules()) {
            aprioriRuleSignatures.add(rule.getAntecedentAsString() + " => " + rule.getConsequentAsString());
        }

        Set<String> fpRuleSignatures = new HashSet<>();
        for (AssociationRuleResult rule : fpGrowthResult.getAssociationRules()) {
            fpRuleSignatures.add(rule.getAntecedentAsString() + " => " + rule.getConsequentAsString());
        }

        double overlapPercentage = 100.0;
        if (!aprioriRuleSignatures.isEmpty()) {
            int commonCount = 0;
            for (String sig : aprioriRuleSignatures) {
                if (fpRuleSignatures.contains(sig)) {
                    commonCount++;
                }
            }
            overlapPercentage = Math.round(((double) commonCount / aprioriRuleSignatures.size()) * 10000.0) / 100.0;
        }

        // 4. Lựa chọn thuật toán khuyến nghị và kết luận khoa học dựa trên số liệu thực tế
        String recommendedAlgo;
        String conclusionNotes;

        if (fpGrowthResult.getRuntimeMs() < aprioriResult.getRuntimeMs()) {
            recommendedAlgo = "FP-Growth";
            conclusionNotes = String.format(
                    "Thực nghiệm cho thấy FP-Growth vượt trội về hiệu năng thời gian (%d ms so với %d ms của Apriori, nhanh hơn %.1fx) " +
                    "nhờ cơ chế nén cây tiền tố FP-Tree và không phải sinh tổ hợp ứng viên candidate (k-itemset). " +
                    "Cả 2 thuật toán đạt độ tương đồng tập luật %.1f%%, chứng minh tính đúng đắn toán học. " +
                    "Khuyến nghị sử dụng FP-Growth khi dữ liệu MIMIC-III mở rộng quy mô lớn.",
                    fpGrowthResult.getRuntimeMs(), aprioriResult.getRuntimeMs(),
                    (double) Math.max(1, aprioriResult.getRuntimeMs()) / Math.max(1, fpGrowthResult.getRuntimeMs()),
                    overlapPercentage);
        } else {
            recommendedAlgo = "Apriori";
            conclusionNotes = String.format(
                    "Trên tập dữ liệu kích thước nhỏ/vừa, Apriori hoàn thành trong %d ms so với %d ms của FP-Growth. " +
                    "Apriori có ưu điểm vượt trội về tính tường minh (explainability), cho phép quan sát trực tiếp quá trình " +
                    "sinh ứng viên theo từng cấp độ k-itemset. Độ tương đồng tập luật đạt %.1f%%.",
                    aprioriResult.getRuntimeMs(), fpGrowthResult.getRuntimeMs(), overlapPercentage);
        }

        // 5. Lưu vào CSDL AlgorithmBenchmark
        AlgorithmBenchmark benchmark = AlgorithmBenchmark.builder()
                .benchmarkName(String.format("Benchmark s=%.2f c=%.2f (%s)", params.getMinSupport(), params.getMinConfidence(), datasetName))
                .datasetName(datasetName)
                .transactionCount(transactions.size())
                .minSupport(params.getMinSupport())
                .minConfidence(params.getMinConfidence())
                .minLift(params.getMinLift())
                .aprioriRuntimeMs(aprioriResult.getRuntimeMs())
                .fpgrowthRuntimeMs(fpGrowthResult.getRuntimeMs())
                .aprioriItemsetCount(aprioriResult.getFrequentItemsetCount())
                .fpgrowthItemsetCount(fpGrowthResult.getFrequentItemsetCount())
                .aprioriRuleCount(aprioriResult.getRuleCount())
                .fpgrowthRuleCount(fpGrowthResult.getRuleCount())
                .aprioriMemoryMb(aprioriResult.getMemoryUsageMb())
                .fpgrowthMemoryMb(fpGrowthResult.getMemoryUsageMb())
                .ruleOverlapPercentage(overlapPercentage)
                .recommendedAlgorithm(recommendedAlgo)
                .conclusionNotes(conclusionNotes)
                .build();

        benchmarkRepository.save(benchmark);

        log.info("Benchmark hoàn tất! Apriori: {} ms, FP-Growth: {} ms. Thuật toán khuyến nghị: {}",
                aprioriResult.getRuntimeMs(), fpGrowthResult.getRuntimeMs(), recommendedAlgo);

        return BenchmarkComparisonDTO.builder()
                .datasetName(datasetName)
                .transactionCount(transactions.size())
                .minSupport(params.getMinSupport())
                .minConfidence(params.getMinConfidence())
                .minLift(params.getMinLift())
                .aprioriRuntimeMs(aprioriResult.getRuntimeMs())
                .fpgrowthRuntimeMs(fpGrowthResult.getRuntimeMs())
                .aprioriItemsetCount(aprioriResult.getFrequentItemsetCount())
                .fpgrowthItemsetCount(fpGrowthResult.getFrequentItemsetCount())
                .aprioriRuleCount(aprioriResult.getRuleCount())
                .fpgrowthRuleCount(fpGrowthResult.getRuleCount())
                .aprioriMemoryMb(aprioriResult.getMemoryUsageMb())
                .fpgrowthMemoryMb(fpGrowthResult.getMemoryUsageMb())
                .ruleOverlapPercentage(overlapPercentage)
                .recommendedAlgorithm(recommendedAlgo)
                .conclusionNotes(conclusionNotes)
                .build();
    }

    /**
     * Chạy Benchmark qua chuỗi nhiều ngưỡng minSupport (vd 0.01, 0.02, 0.05 hoặc 0.05, 0.10, 0.15)
     */
    @Transactional
    public List<BenchmarkComparisonDTO> runMultiThresholdBenchmark(List<Set<String>> transactions,
                                                                   double[] supportThresholds,
                                                                   double minConfidence,
                                                                   double minLift,
                                                                   String datasetName) {
        List<BenchmarkComparisonDTO> results = new ArrayList<>();
        for (double s : supportThresholds) {
            MiningParameters params = MiningParameters.builder()
                    .minSupport(s)
                    .minConfidence(minConfidence)
                    .minLift(minLift)
                    .maxItemsetSize(6)
                    .build();
            results.add(runBenchmark(transactions, params, datasetName));
        }
        return results;
    }

    public List<AlgorithmBenchmark> getAllBenchmarks() {
        return benchmarkRepository.findAllByOrderByCreatedAtDesc();
    }
}
