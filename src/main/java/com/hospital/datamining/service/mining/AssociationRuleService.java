package com.hospital.datamining.service.mining;

import com.hospital.datamining.entity.*;
import com.hospital.datamining.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service quản lý, truy vấn, sắp xếp và xuất dữ liệu Association Rules.
 * Hỗ trợ lọc theo chỉ số (Support, Confidence, Lift), sắp xếp đa chiều và liên kết theo Medicine ID.
 */
@Service
public class AssociationRuleService {

    private static final Logger log = LoggerFactory.getLogger(AssociationRuleService.class);

    private final AssociationRuleRepository ruleRepository;
    private final AssociationRuleAntecedentRepository antecedentRepository;
    private final AssociationRuleConsequentRepository consequentRepository;
    private final DatasetStatisticsRepository statisticsRepository;
    private final AlgorithmBenchmarkRepository benchmarkRepository;

    public AssociationRuleService(AssociationRuleRepository ruleRepository,
                                  AssociationRuleAntecedentRepository antecedentRepository,
                                  AssociationRuleConsequentRepository consequentRepository,
                                  DatasetStatisticsRepository statisticsRepository,
                                  AlgorithmBenchmarkRepository benchmarkRepository) {
        this.ruleRepository = ruleRepository;
        this.antecedentRepository = antecedentRepository;
        this.consequentRepository = consequentRepository;
        this.statisticsRepository = statisticsRepository;
        this.benchmarkRepository = benchmarkRepository;
    }

    /**
     * Tìm kiếm và lọc luật kết hợp kèm theo tùy chọn sắp xếp
     */
    public List<AssociationRule> getFilteredRules(Long runId,
                                                  Double minSupport,
                                                  Double minConfidence,
                                                  Double minLift,
                                                  String drug,
                                                  String sortBy) {
        double sup = (minSupport != null) ? minSupport : 0.0;
        double conf = (minConfidence != null) ? minConfidence : 0.0;
        double lift = (minLift != null) ? minLift : 0.0;

        List<AssociationRule> rules = ruleRepository.findRulesWithFilter(runId, sup, conf, lift, drug);

        if (sortBy != null) {
            switch (sortBy.toUpperCase()) {
                case "SUPPORT_DESC":
                    rules.sort((a, b) -> Double.compare(b.getSupport(), a.getSupport()));
                    break;
                case "LIFT_DESC":
                    rules.sort((a, b) -> Double.compare(b.getLift(), a.getLift()));
                    break;
                case "CONFIDENCE_DESC":
                default:
                    rules.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));
                    break;
            }
        }
        return rules;
    }

    /**
     * Tìm các luật có chứa Medicine ID cụ thể trong tiền đề hoặc hệ quả
     */
    public List<AssociationRule> getRulesByMedicineId(Long runId, Long medicineId) {
        Set<Long> ruleIds = new HashSet<>();
        for (AssociationRuleAntecedent a : antecedentRepository.findByMedicineId(medicineId)) {
            if (a.getRule().getMiningRun().getId().equals(runId)) {
                ruleIds.add(a.getRule().getId());
            }
        }
        for (AssociationRuleConsequent c : consequentRepository.findByMedicineId(medicineId)) {
            if (c.getRule().getMiningRun().getId().equals(runId)) {
                ruleIds.add(c.getRule().getId());
            }
        }
        return ruleRepository.findAllById(ruleIds);
    }

    /**
     * Xuất danh sách luật kết hợp ra định dạng CSV (RFC 4180) phục vụ báo cáo
     */
    public String exportRulesToCsv(Long runId) {
        List<AssociationRule> rules = (runId != null)
                ? ruleRepository.findByMiningRunIdOrderByConfidenceDesc(runId)
                : ruleRepository.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("id,antecedent,consequent,support,confidence,lift,antecedent_size,consequent_size,algorithm,mining_run_id\n");

        for (AssociationRule r : rules) {
            String algo = (r.getMiningRun() != null) ? r.getMiningRun().getAlgorithm() : "UNKNOWN";
            sb.append(String.format("%d,\"%s\",\"%s\",%.4f,%.4f,%.4f,%d,%d,%s,%d\n",
                    r.getId(),
                    escapeCsv(r.getAntecedent()),
                    escapeCsv(r.getConsequent()),
                    r.getSupport(),
                    r.getConfidence(),
                    r.getLift(),
                    r.getAntecedentSize(),
                    r.getConsequentSize(),
                    algo,
                    (r.getMiningRun() != null ? r.getMiningRun().getId() : 0)));
        }
        return sb.toString();
    }

    /**
     * Xuất bảng so sánh Benchmark Apriori vs FP-Growth ra CSV
     */
    public String exportBenchmarksToCsv() {
        List<AlgorithmBenchmark> list = benchmarkRepository.findAllByOrderByCreatedAtDesc();
        StringBuilder sb = new StringBuilder();
        sb.append("id,benchmark_name,dataset_name,transaction_count,min_support,min_confidence,min_lift,apriori_runtime_ms,fpgrowth_runtime_ms,apriori_itemset_count,fpgrowth_itemset_count,apriori_rule_count,fpgrowth_rule_count,apriori_memory_mb,fpgrowth_memory_mb,rule_overlap_percentage,recommended_algorithm\n");

        for (AlgorithmBenchmark b : list) {
            sb.append(String.format("%d,\"%s\",\"%s\",%d,%.4f,%.4f,%.4f,%d,%d,%d,%d,%d,%d,%.2f,%.2f,%.1f,%s\n",
                    b.getId(),
                    escapeCsv(b.getBenchmarkName()),
                    escapeCsv(b.getDatasetName()),
                    b.getTransactionCount(),
                    b.getMinSupport(),
                    b.getMinConfidence(),
                    b.getMinLift(),
                    b.getAprioriRuntimeMs(),
                    b.getFpgrowthRuntimeMs(),
                    b.getAprioriItemsetCount(),
                    b.getFpgrowthItemsetCount(),
                    b.getAprioriRuleCount(),
                    b.getFpgrowthRuleCount(),
                    b.getAprioriMemoryMb() != null ? b.getAprioriMemoryMb() : 0.0,
                    b.getFpgrowthMemoryMb() != null ? b.getFpgrowthMemoryMb() : 0.0,
                    b.getRuleOverlapPercentage() != null ? b.getRuleOverlapPercentage() : 100.0,
                    b.getRecommendedAlgorithm()));
        }
        return sb.toString();
    }

    /**
     * Xuất dữ liệu thống kê Data Understanding ra CSV
     */
    public String exportStatisticsToCsv() {
        List<DatasetStatistics> list = statisticsRepository.findAllByOrderByCreatedAtDesc();
        StringBuilder sb = new StringBuilder();
        sb.append("id,dataset_name,total_records,valid_records,missing_hadm_count,missing_drug_count,unique_subject_count,unique_hadm_count,unique_drug_count,avg_drugs_per_hadm,min_drugs_per_hadm,max_drugs_per_hadm,final_transaction_count,created_at\n");

        for (DatasetStatistics s : list) {
            sb.append(String.format("%d,\"%s\",%d,%d,%d,%d,%d,%d,%d,%.2f,%d,%d,%d,%s\n",
                    s.getId(),
                    escapeCsv(s.getDatasetName()),
                    s.getTotalRecords(),
                    s.getValidRecords(),
                    s.getMissingHadmCount(),
                    s.getMissingDrugCount(),
                    s.getUniqueSubjectCount(),
                    s.getUniqueHadmCount(),
                    s.getUniqueDrugCount(),
                    s.getAvgDrugsPerHadm(),
                    s.getMinDrugsPerHadm(),
                    s.getMaxDrugsPerHadm(),
                    s.getFinalTransactionCount(),
                    s.getCreatedAt() != null ? s.getCreatedAt().toString() : ""));
        }
        return sb.toString();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }
}
