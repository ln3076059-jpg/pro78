package com.hospital.datamining.service.mining;

import com.hospital.datamining.entity.*;
import com.hospital.datamining.repository.*;
import com.hospital.datamining.service.mining.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MiningRunService {

    private static final Logger log = LoggerFactory.getLogger(MiningRunService.class);

    private final MiningRunRepository miningRunRepository;
    private final FrequentItemsetRepository frequentItemsetRepository;
    private final FrequentItemsetItemRepository frequentItemsetItemRepository;
    private final AssociationRuleRepository associationRuleRepository;
    private final AssociationRuleItemRepository associationRuleItemRepository;
    private final AssociationRuleAntecedentRepository antecedentRepository;
    private final AssociationRuleConsequentRepository consequentRepository;
    private final MedicineRepository medicineRepository;
    private final DatasetImportRepository datasetImportRepository;

    public MiningRunService(MiningRunRepository miningRunRepository,
                            FrequentItemsetRepository frequentItemsetRepository,
                            FrequentItemsetItemRepository frequentItemsetItemRepository,
                            AssociationRuleRepository associationRuleRepository,
                            AssociationRuleItemRepository associationRuleItemRepository,
                            AssociationRuleAntecedentRepository antecedentRepository,
                            AssociationRuleConsequentRepository consequentRepository,
                            MedicineRepository medicineRepository,
                            @org.springframework.beans.factory.annotation.Autowired(required = false) DatasetImportRepository datasetImportRepository) {
        this.miningRunRepository = miningRunRepository;
        this.frequentItemsetRepository = frequentItemsetRepository;
        this.frequentItemsetItemRepository = frequentItemsetItemRepository;
        this.associationRuleRepository = associationRuleRepository;
        this.associationRuleItemRepository = associationRuleItemRepository;
        this.antecedentRepository = antecedentRepository;
        this.consequentRepository = consequentRepository;
        this.medicineRepository = medicineRepository;
        this.datasetImportRepository = datasetImportRepository;
    }

    /**
     * Lưu vết kết quả lần chạy khai phá và các luật vào CSDL (không ghi đè kết quả cũ)
     */
    @Transactional
    public MiningRun saveMiningRun(MiningResult result, MiningParameters params, String datasetSource) {
        return saveMiningRun(result, params, datasetSource, null);
    }

    @Transactional
    public MiningRun saveMiningRun(MiningResult result, MiningParameters params, String datasetSource, Long datasetImportId) {
        log.info("Lưu kết quả khai phá {} vào CSDL...", result.getAlgorithm());

        DatasetImport linkedImport = null;
        if (datasetImportRepository != null) {
            if (datasetImportId != null) {
                linkedImport = datasetImportRepository.findById(datasetImportId).orElse(null);
            } else {
                linkedImport = datasetImportRepository.findFirstByStatusOrderByCreatedAtDesc("COMPLETED").orElse(null);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startedAt = now.minusNanos(result.getRuntimeMs() * 1_000_000L);

        // Kiểm tra xem đã có Active model chưa. Nếu chưa có model nào active, model thành công đầu tiên sẽ active.
        boolean shouldBeActive = miningRunRepository.findFirstBySelectedForRecommendationTrueOrderByCreatedAtDesc().isEmpty();

        MiningRun run = MiningRun.builder()
                .runName(result.getAlgorithm() + " - " + System.currentTimeMillis())
                .algorithm(result.getAlgorithm())
                .datasetSource(datasetSource)
                .datasetImport(linkedImport)
                .selectedForRecommendation(shouldBeActive)
                .minSupport(params.getMinSupport())
                .minConfidence(params.getMinConfidence())
                .minLift(params.getMinLift())
                .maxItemsetSize(params.getMaxItemsetSize())
                .transactionCount(result.getTransactionCount())
                .uniqueDrugCount(result.getUniqueDrugCount())
                .frequentItemsetCount(result.getFrequentItemsetCount())
                .ruleCount(result.getRuleCount())
                .runtimeMs(result.getRuntimeMs())
                .memoryUsageMb(result.getMemoryUsageMb())
                .startedAt(startedAt)
                .finishedAt(now)
                .status("SUCCESS")
                .build();

        MiningRun savedRun = miningRunRepository.save(run);

        // 1. Lưu các tập phổ biến (Frequent Itemsets) và các FrequentItemsetItem
        for (FrequentItemsetResult fir : result.getFrequentItemsets()) {
            FrequentItemset fi = FrequentItemset.builder()
                    .miningRun(savedRun)
                    .itemsetString(fir.getItemsAsString())
                    .itemCount(fir.getItemCount())
                    .support(fir.getSupport())
                    .supportCount(fir.getSupportCount())
                    .build();
            FrequentItemset savedFi = frequentItemsetRepository.save(fi);

            for (String itemName : fir.getItems()) {
                Medicine med = medicineRepository.findByGenericNameIgnoreCase(itemName).orElse(null);
                FrequentItemsetItem item = FrequentItemsetItem.builder()
                        .itemset(savedFi)
                        .drugName(itemName)
                        .medicine(med)
                        .build();
                frequentItemsetItemRepository.save(item);
            }
        }

        // 2. Lưu các luật kết hợp (Association Rules), Antecedents, Consequents
        for (AssociationRuleResult arr : result.getAssociationRules()) {
            AssociationRule rule = AssociationRule.builder()
                    .miningRun(savedRun)
                    .antecedent(arr.getAntecedentAsString())
                    .consequent(arr.getConsequentAsString())
                    .support(arr.getSupport())
                    .confidence(arr.getConfidence())
                    .lift(arr.getLift())
                    .antecedentSize(arr.getAntecedentSize())
                    .consequentSize(arr.getConsequentSize())
                    .build();

            AssociationRule savedRule = associationRuleRepository.save(rule);

            // Lưu các item trong tiền đề (Antecedent)
            for (String item : arr.getAntecedent()) {
                Medicine med = medicineRepository.findByGenericNameIgnoreCase(item).orElse(null);

                AssociationRuleItem ruleItem = AssociationRuleItem.builder()
                        .rule(savedRule)
                        .itemName(item)
                        .roleType(AssociationRuleItem.ItemRoleType.ANTECEDENT)
                        .build();
                associationRuleItemRepository.save(ruleItem);

                AssociationRuleAntecedent ant = AssociationRuleAntecedent.builder()
                        .rule(savedRule)
                        .drugName(item)
                        .medicine(med)
                        .build();
                antecedentRepository.save(ant);
            }

            // Lưu các item trong hệ quả (Consequent)
            for (String item : arr.getConsequent()) {
                Medicine med = medicineRepository.findByGenericNameIgnoreCase(item).orElse(null);

                AssociationRuleItem ruleItem = AssociationRuleItem.builder()
                        .rule(savedRule)
                        .itemName(item)
                        .roleType(AssociationRuleItem.ItemRoleType.CONSEQUENT)
                        .build();
                associationRuleItemRepository.save(ruleItem);

                AssociationRuleConsequent cons = AssociationRuleConsequent.builder()
                        .rule(savedRule)
                        .drugName(item)
                        .medicine(med)
                        .build();
                consequentRepository.save(cons);
            }
        }

        log.info("Đã lưu thành công MiningRun ID {} với {} itemsets và {} rules",
                savedRun.getId(), result.getFrequentItemsetCount(), result.getRuleCount());

        return savedRun;
    }

    /**
     * Lấy lần chạy thành công gần nhất để phục vụ tích hợp gợi ý thuốc
     */
    public Optional<MiningRun> getLatestSuccessfulRun() {
        return miningRunRepository.findFirstByStatusOrderByCreatedAtDesc("SUCCESS");
    }

    /**
     * Lấy mô hình đang hoạt động (Active Model) được chỉ định phục vụ kê đơn
     */
    public Optional<MiningRun> getActiveMiningRun() {
        Optional<MiningRun> active = miningRunRepository.findFirstBySelectedForRecommendationTrueOrderByCreatedAtDesc();
        if (active.isPresent()) {
            return active;
        }
        return getLatestSuccessfulRun();
    }

    /**
     * Đặt một MiningRun làm Active Model cho chức năng gợi ý kê đơn thuốc
     */
    @Transactional
    public boolean setActiveMiningRun(Long runId) {
        Optional<MiningRun> optRun = miningRunRepository.findById(runId);
        if (optRun.isEmpty()) {
            return false;
        }
        MiningRun target = optRun.get();
        if (!"SUCCESS".equalsIgnoreCase(target.getStatus())) {
            log.warn("MiningRunService: Không thể kích hoạt MiningRun #{} vì trạng thái không phải SUCCESS ({})", runId, target.getStatus());
            return false;
        }
        miningRunRepository.resetAllSelectedForRecommendation();
        target.setSelectedForRecommendation(true);
        miningRunRepository.save(target);
        log.info("MiningRunService: Đã kích hoạt MiningRun #{} ({}) làm Active Model cho chức năng kê đơn!",
                runId, target.getAlgorithm());
        return true;
    }

    public List<MiningRun> getAllRuns() {
        return miningRunRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<MiningRun> getRunById(Long id) {
        return miningRunRepository.findById(id);
    }
}
