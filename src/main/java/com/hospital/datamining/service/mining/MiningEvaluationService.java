package com.hospital.datamining.service.mining;

import com.hospital.datamining.dto.BenchmarkComparisonDTO;
import com.hospital.datamining.entity.AlgorithmBenchmark;
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
     * Chạy thực nghiệm đo lường so sánh Apriori và FP-Growth đa lần (multi-run) với warm-up
     * nhằm loại bỏ nhiễu JVM JIT Compiler & Garbage Collection.
     */
    @Transactional
    public BenchmarkComparisonDTO runBenchmark(List<Set<String>> transactions,
                                               MiningParameters params,
                                               String datasetName) {
        log.info("Bắt đầu Benchmark so sánh Apriori vs FP-Growth trên {} transactions (minSupport={})...",
                transactions.size(), params.getMinSupport());

        // 1. Warm-up runs (2 lần chạy khởi động JVM JIT compiler)
        if (transactions.size() > 0) {
            for (int w = 0; w < 2; w++) {
                aprioriService.mine(transactions, params);
                fpGrowthService.mine(transactions, params);
            }
        }

        // 2. Chạy đo lường nhiều lần (5 lần lặp) để lấy Median runtime và độ lệch chuẩn
        int iterations = 5;
        List<Long> aprioriTimes = new ArrayList<>();
        List<Long> fpGrowthTimes = new ArrayList<>();

        MiningResult lastAprioriResult = null;
        MiningResult lastFpResult = null;

        for (int i = 0; i < iterations; i++) {
            lastAprioriResult = aprioriService.mine(transactions, params);
            aprioriTimes.add(lastAprioriResult.getRuntimeMs());

            lastFpResult = fpGrowthService.mine(transactions, params);
            fpGrowthTimes.add(lastFpResult.getRuntimeMs());
        }

        long aprioriMedianMs = calculateMedian(aprioriTimes);
        long fpGrowthMedianMs = calculateMedian(fpGrowthTimes);

        double aprioriAvgMs = calculateAverage(aprioriTimes);
        double fpGrowthAvgMs = calculateAverage(fpGrowthTimes);

        double aprioriStdDev = calculateStdDev(aprioriTimes, aprioriAvgMs);
        double fpGrowthStdDev = calculateStdDev(fpGrowthTimes, fpGrowthAvgMs);

        // 3. Đo độ tương đồng đối xứng tập luật bằng hệ số Jaccard Similarity: |A ∩ B| / |A ∪ B|
        Set<String> aprioriRuleSignatures = new HashSet<>();
        if (lastAprioriResult != null) {
            for (AssociationRuleResult rule : lastAprioriResult.getAssociationRules()) {
                aprioriRuleSignatures.add(rule.getAntecedentAsString() + " => " + rule.getConsequentAsString());
            }
        }

        Set<String> fpRuleSignatures = new HashSet<>();
        if (lastFpResult != null) {
            for (AssociationRuleResult rule : lastFpResult.getAssociationRules()) {
                fpRuleSignatures.add(rule.getAntecedentAsString() + " => " + rule.getConsequentAsString());
            }
        }

        Set<String> unionRules = new HashSet<>(aprioriRuleSignatures);
        unionRules.addAll(fpRuleSignatures);

        Set<String> intersectRules = new HashSet<>(aprioriRuleSignatures);
        intersectRules.retainAll(fpRuleSignatures);

        double jaccardOverlapPercentage = 100.0;
        if (!unionRules.isEmpty()) {
            jaccardOverlapPercentage = Math.round(((double) intersectRules.size() / unionRules.size()) * 10000.0) / 100.0;
        }

        // 4. Đo độ tương đồng đối xứng tập phổ biến (Frequent Itemset Jaccard Similarity)
        Set<String> aprioriItemsetSignatures = new HashSet<>();
        if (lastAprioriResult != null) {
            for (FrequentItemsetResult itemset : lastAprioriResult.getFrequentItemsets()) {
                aprioriItemsetSignatures.add(itemset.getItemsAsString());
            }
        }

        Set<String> fpItemsetSignatures = new HashSet<>();
        if (lastFpResult != null) {
            for (FrequentItemsetResult itemset : lastFpResult.getFrequentItemsets()) {
                fpItemsetSignatures.add(itemset.getItemsAsString());
            }
        }

        Set<String> unionItemsets = new HashSet<>(aprioriItemsetSignatures);
        unionItemsets.addAll(fpItemsetSignatures);

        Set<String> intersectItemsets = new HashSet<>(aprioriItemsetSignatures);
        intersectItemsets.retainAll(fpItemsetSignatures);

        double itemsetOverlapPercentage = 100.0;
        if (!unionItemsets.isEmpty()) {
            itemsetOverlapPercentage = Math.round(((double) intersectItemsets.size() / unionItemsets.size()) * 10000.0) / 100.0;
        }

        boolean itemsetsEquivalent = (itemsetOverlapPercentage >= 95.0) &&
                (lastAprioriResult != null && lastFpResult != null &&
                lastAprioriResult.getFrequentItemsetCount() == lastFpResult.getFrequentItemsetCount());

        // 5. Đánh giá lựa chọn thuật toán dựa trên cơ sở thực nghiệm khách quan
        String recommendedAlgo;
        String conclusionNotes;

        if (!itemsetsEquivalent || jaccardOverlapPercentage < 95.0) {
            recommendedAlgo = "CẦN RÀ SOÁT LẠI (DISCREPANCY)";
            conclusionNotes = String.format(
                    "CẢNH BÁO: Phát hiện sự khác biệt giữa hai thuật toán (Độ tương đồng luật Jaccard: %.1f%%, Itemsets Jaccard: %.1f%%, Apriori: %d vs FP-Growth: %d). " +
                    "Cần kiểm tra lại các ngưỡng cắt tỉa và xử lý thứ tự sắp xếp item.",
                    jaccardOverlapPercentage, itemsetOverlapPercentage,
                    lastAprioriResult != null ? lastAprioriResult.getFrequentItemsetCount() : 0,
                    lastFpResult != null ? lastFpResult.getFrequentItemsetCount() : 0);
        } else if (fpGrowthMedianMs < aprioriMedianMs) {
            recommendedAlgo = "FP-Growth";
            double speedup = (double) Math.max(1, aprioriMedianMs) / Math.max(1, fpGrowthMedianMs);
            conclusionNotes = String.format(
                    "Thực nghiệm qua %d lần đo lặp (sau 2 lần warm-up) ghi nhận FP-Growth nhanh hơn Apriori %.1fx " +
                    "(Thời gian trung vị Median: %d ms so với %d ms; StdDev: ±%.1f ms so với ±%.1f ms). " +
                    "Thuật toán tiết kiệm bộ nhớ nhờ cấu trúc nén cây tiền tố FP-Tree, không sinh tổ hợp ứng viên khổng lồ (candidate generation). " +
                    "Độ trùng khớp tập luật Jaccard đạt %.1f%%, tập phổ biến Jaccard đạt %.1f%%, bảo đảm tính toàn vẹn toán học. " +
                    "Khuyến nghị sử dụng FP-Growth khi dữ liệu kê đơn bệnh viện mở rộng quy mô lớn.",
                    iterations, speedup, fpGrowthMedianMs, aprioriMedianMs, fpGrowthStdDev, aprioriStdDev, jaccardOverlapPercentage, itemsetOverlapPercentage);
        } else {
            recommendedAlgo = "Apriori";
            conclusionNotes = String.format(
                    "Trên tập dữ liệu kích thước nhỏ, Apriori hoàn thành với thời gian trung vị %d ms (so với %d ms của FP-Growth). " +
                    "Apriori có ưu điểm về tính tường minh (explainability) theo từng bước sinh ứng viên k-itemset. " +
                    "Độ trùng khớp tập luật Jaccard đạt %.1f%%, tập phổ biến Jaccard đạt %.1f%%.",
                    aprioriMedianMs, fpGrowthMedianMs, jaccardOverlapPercentage, itemsetOverlapPercentage);
        }

        // 6. Lưu kết quả vào CSDL AlgorithmBenchmark
        AlgorithmBenchmark benchmark = AlgorithmBenchmark.builder()
                .benchmarkName(String.format("Benchmark s=%.2f c=%.2f (%s)", params.getMinSupport(), params.getMinConfidence(), datasetName))
                .datasetName(datasetName)
                .transactionCount(transactions.size())
                .minSupport(params.getMinSupport())
                .minConfidence(params.getMinConfidence())
                .minLift(params.getMinLift())
                .aprioriRuntimeMs(aprioriMedianMs)
                .fpgrowthRuntimeMs(fpGrowthMedianMs)
                .aprioriItemsetCount(lastAprioriResult != null ? lastAprioriResult.getFrequentItemsetCount() : 0)
                .fpgrowthItemsetCount(lastFpResult != null ? lastFpResult.getFrequentItemsetCount() : 0)
                .aprioriRuleCount(lastAprioriResult != null ? lastAprioriResult.getRuleCount() : 0)
                .fpgrowthRuleCount(lastFpResult != null ? lastFpResult.getRuleCount() : 0)
                .aprioriMemoryMb(lastAprioriResult != null ? lastAprioriResult.getMemoryUsageMb() : 0.0)
                .fpgrowthMemoryMb(lastFpResult != null ? lastFpResult.getMemoryUsageMb() : 0.0)
                .ruleOverlapPercentage(jaccardOverlapPercentage)
                .itemsetOverlapPercentage(itemsetOverlapPercentage)
                .recommendedAlgorithm(recommendedAlgo)
                .conclusionNotes(conclusionNotes)
                .build();

        benchmarkRepository.save(benchmark);

        log.info("Benchmark hoàn tất! Apriori Median: {} ms, FP-Growth Median: {} ms, Rule Jaccard: {}%, Itemset Jaccard: {}%. Thuật toán khuyến nghị: {}",
                aprioriMedianMs, fpGrowthMedianMs, jaccardOverlapPercentage, itemsetOverlapPercentage, recommendedAlgo);

        return BenchmarkComparisonDTO.builder()
                .datasetName(datasetName)
                .transactionCount(transactions.size())
                .minSupport(params.getMinSupport())
                .minConfidence(params.getMinConfidence())
                .minLift(params.getMinLift())
                .aprioriRuntimeMs(aprioriMedianMs)
                .fpgrowthRuntimeMs(fpGrowthMedianMs)
                .aprioriItemsetCount(lastAprioriResult != null ? lastAprioriResult.getFrequentItemsetCount() : 0)
                .fpgrowthItemsetCount(lastFpResult != null ? lastFpResult.getFrequentItemsetCount() : 0)
                .aprioriRuleCount(lastAprioriResult != null ? lastAprioriResult.getRuleCount() : 0)
                .fpgrowthRuleCount(lastFpResult != null ? lastFpResult.getRuleCount() : 0)
                .aprioriMemoryMb(lastAprioriResult != null ? lastAprioriResult.getMemoryUsageMb() : 0.0)
                .fpgrowthMemoryMb(lastFpResult != null ? lastFpResult.getMemoryUsageMb() : 0.0)
                .ruleOverlapPercentage(jaccardOverlapPercentage)
                .itemsetOverlapPercentage(itemsetOverlapPercentage)
                .recommendedAlgorithm(recommendedAlgo)
                .conclusionNotes(conclusionNotes)
                .build();
    }

    private long calculateMedian(List<Long> values) {
        if (values == null || values.isEmpty()) return 0;
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int mid = sorted.size() / 2;
        if (sorted.size() % 2 == 1) {
            return sorted.get(mid);
        } else {
            return (sorted.get(mid - 1) + sorted.get(mid)) / 2;
        }
    }

    private double calculateAverage(List<Long> values) {
        if (values == null || values.isEmpty()) return 0.0;
        long sum = 0;
        for (long v : values) sum += v;
        return (double) sum / values.size();
    }

    private double calculateStdDev(List<Long> values, double avg) {
        if (values == null || values.size() <= 1) return 0.0;
        double sumSq = 0.0;
        for (long v : values) {
            sumSq += Math.pow(v - avg, 2);
        }
        return Math.round(Math.sqrt(sumSq / (values.size() - 1)) * 10.0) / 10.0;
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
