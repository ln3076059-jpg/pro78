package com.hospital.datamining.service.prescription;

import com.hospital.datamining.dto.DrugInteractionDTO;
import com.hospital.datamining.dto.RecommendationResponseDTO;
import com.hospital.datamining.entity.AssociationRule;
import com.hospital.datamining.entity.Medicine;
import com.hospital.datamining.entity.MiningRun;
import com.hospital.datamining.repository.AssociationRuleRepository;
import com.hospital.datamining.repository.MedicineRepository;
import com.hospital.datamining.service.interaction.DrugInteractionService;
import com.hospital.datamining.service.mining.MiningRunService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DrugRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(DrugRecommendationService.class);

    // Disclaimer y khoa chuẩn theo mục 27 đề tài
    public static final String CLINICAL_DISCLAIMER =
            "Gợi ý này dựa trên các mẫu đồng sử dụng thuốc trong dữ liệu lịch sử và chỉ dùng để tham khảo. " +
            "Hệ thống không tự động xác nhận hiệu quả hoặc độ an toàn của phối hợp thuốc. Quyết định kê đơn cuối cùng thuộc về bác sĩ.";

    private final MiningRunService miningRunService;
    private final AssociationRuleRepository ruleRepository;
    private final MedicineRepository medicineRepository;
    private final DrugInteractionService drugInteractionService;

    public DrugRecommendationService(MiningRunService miningRunService,
                                     AssociationRuleRepository ruleRepository,
                                     MedicineRepository medicineRepository) {
        this(miningRunService, ruleRepository, medicineRepository, null);
    }

    @Autowired
    public DrugRecommendationService(MiningRunService miningRunService,
                                     AssociationRuleRepository ruleRepository,
                                     MedicineRepository medicineRepository,
                                     @Autowired(required = false) DrugInteractionService drugInteractionService) {
        this.miningRunService = miningRunService;
        this.ruleRepository = ruleRepository;
        this.medicineRepository = medicineRepository;
        this.drugInteractionService = drugInteractionService;
    }

    /**
     * Gợi ý các thuốc thường được kê cùng dựa trên danh sách ID thuốc hiện có trong đơn
     */
    public List<RecommendationResponseDTO> recommendByMedicineIds(List<Long> medicineIds, int topN) {
        if (medicineIds == null || medicineIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Medicine> medicines = medicineRepository.findAllById(medicineIds);
        List<String> drugNames = new ArrayList<>();
        for (Medicine m : medicines) {
            drugNames.add(m.getGenericName());
        }

        return recommendByDrugNames(drugNames, topN);
    }

    /**
     * Gợi ý các thuốc thường được kê cùng dựa trên danh sách tên thuốc hiện có trong đơn.
     * Quy trình xếp hạng ưu tiên 2 tầng (Two-tier ranking theo mục 25 & 26 đề tài):
     * - Tầng 1: Ưu tiên luật đa phần tử {A, B} -> X (Antecedent có >= 2 thuốc và là tập con của đơn thuốc hiện tại)
     * - Tầng 2: Fallback sang các luật đơn phần tử {A} -> X hoặc {B} -> X nếu tầng 1 chưa đủ Top-N
     * - Xếp hạng theo Lift DESC, sau đó Confidence DESC, sau đó Support DESC
     * - Tích hợp kiểm tra tương tác thuốc FDA DDI (Mục 28 đề tài)
     *
     * @param selectedDrugs Danh sách tên thuốc bác sĩ đã thêm vào đơn
     * @param topN Số lượng thuốc tối đa cần gợi ý (mặc định 5)
     */
    public List<RecommendationResponseDTO> recommendByDrugNames(List<String> selectedDrugs, int topN) {
        if (selectedDrugs == null || selectedDrugs.isEmpty()) {
            return Collections.emptyList();
        }

        int limit = topN > 0 ? topN : 5;

        // Chuẩn hóa tên các thuốc đã chọn
        Set<String> normalizedSelected = new HashSet<>();
        for (String d : selectedDrugs) {
            if (d != null && !d.trim().isEmpty()) {
                normalizedSelected.add(d.trim().toLowerCase());
            }
        }

        if (normalizedSelected.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Lấy mô hình khai phá gần nhất
        Optional<MiningRun> latestRunOpt = miningRunService.getLatestSuccessfulRun();
        if (latestRunOpt.isEmpty()) {
            log.warn("Chưa có MiningRun nào được lưu để gợi ý thuốc!");
            return Collections.emptyList();
        }

        MiningRun run = latestRunOpt.get();

        // 2. Lấy danh sách luật có confidence cao và lift > 1.0 (Lift > 1 biểu thị đồng xuất hiện tích cực)
        List<AssociationRule> candidateRules = ruleRepository.findTopConfidentRules(run.getId(), 0.10, 1.0);

        // 3. Khớp tiền đề theo 2 tầng:
        // Tier 1: Multi-item antecedent subset: Antecedent size >= 2 AND Antecedent ⊆ selectedDrugs
        // Tier 2: Single-item antecedent subset: Antecedent size == 1 AND Antecedent ⊆ selectedDrugs
        Map<String, RecommendationResponseDTO> tier1Recommendations = new HashMap<>();
        Map<String, RecommendationResponseDTO> tier2Recommendations = new HashMap<>();

        for (AssociationRule rule : candidateRules) {
            Set<String> ruleAntecedents = parseItems(rule.getAntecedent());
            Set<String> ruleConsequents = parseItems(rule.getConsequent());

            boolean isFullSubset = !ruleAntecedents.isEmpty() && containsAllCaseInsensitive(normalizedSelected, ruleAntecedents);

            if (isFullSubset) {
                boolean isMultiItem = ruleAntecedents.size() >= 2;

                for (String consequentDrug : ruleConsequents) {
                    // Không gợi ý thuốc đã có sẵn trong đơn
                    if (normalizedSelected.contains(consequentDrug.toLowerCase())) {
                        continue;
                    }

                    Long medId = null;
                    Optional<Medicine> medOpt = medicineRepository.findByGenericNameIgnoreCase(consequentDrug);
                    if (medOpt.isPresent()) {
                        medId = medOpt.get().getId();
                    }

                    RecommendationResponseDTO candidate = RecommendationResponseDTO.builder()
                            .medicineId(medId)
                            .drug(consequentDrug)
                            .support(rule.getSupport())
                            .confidence(rule.getConfidence())
                            .lift(rule.getLift())
                            .matchingAntecedent(rule.getAntecedent())
                            .clinicalDisclaimer(CLINICAL_DISCLAIMER)
                            .build();

                    // Kiểm tra tương tác thuốc FDA DDI nếu có service
                    enrichDdiInfo(candidate, selectedDrugs);

                    Map<String, RecommendationResponseDTO> targetTier = isMultiItem ? tier1Recommendations : tier2Recommendations;
                    String drugKey = consequentDrug.toLowerCase();

                    if (!targetTier.containsKey(drugKey)) {
                        targetTier.put(drugKey, candidate);
                    } else {
                        RecommendationResponseDTO existing = targetTier.get(drugKey);
                        // Ưu tiên theo Lift, sau đó Confidence
                        if (candidate.getLift() > existing.getLift() ||
                           (Double.compare(candidate.getLift(), existing.getLift()) == 0 && candidate.getConfidence() > existing.getConfidence())) {
                            targetTier.put(drugKey, candidate);
                        }
                    }
                }
            }
        }

        // 4. Sắp xếp danh sách từng tầng theo mục 26 đề tài: Lift DESC, Confidence DESC, Support DESC
        Comparator<RecommendationResponseDTO> comparator = (a, b) -> {
            int cmpLift = Double.compare(b.getLift(), a.getLift());
            if (cmpLift != 0) return cmpLift;
            int cmpConf = Double.compare(b.getConfidence(), a.getConfidence());
            if (cmpConf != 0) return cmpConf;
            return Double.compare(b.getSupport(), a.getSupport());
        };

        List<RecommendationResponseDTO> tier1List = new ArrayList<>(tier1Recommendations.values());
        tier1List.sort(comparator);

        List<RecommendationResponseDTO> tier2List = new ArrayList<>(tier2Recommendations.values());
        tier2List.sort(comparator);

        // 5. Gom kết quả: Ưu tiên Tầng 1 (Multi-item), nếu chưa đủ limit thì bổ sung từ Tầng 2 (Single-item fallback)
        List<RecommendationResponseDTO> combinedList = new ArrayList<>(tier1List);
        Set<String> includedDrugs = new HashSet<>();
        for (RecommendationResponseDTO dto : tier1List) {
            includedDrugs.add(dto.getDrug().toLowerCase());
        }

        for (RecommendationResponseDTO dto : tier2List) {
            if (combinedList.size() >= limit) {
                break;
            }
            if (!includedDrugs.contains(dto.getDrug().toLowerCase())) {
                combinedList.add(dto);
                includedDrugs.add(dto.getDrug().toLowerCase());
            }
        }

        if (combinedList.size() > limit) {
            combinedList = combinedList.subList(0, limit);
        }

        log.info("DrugRecommendationService: Gợi ý {} thuốc cho đơn {} (Tầng 1 đa tiền đề: {}, Tầng 2 đơn tiền đề: {})",
                combinedList.size(), selectedDrugs, tier1List.size(), tier2List.size());

        return combinedList;
    }

    private void enrichDdiInfo(RecommendationResponseDTO dto, List<String> selectedDrugs) {
        if (drugInteractionService == null || dto == null || dto.getDrug() == null) {
            dto.setInteractionStatus("UNKNOWN");
            dto.setInteractionSeverity("None");
            return;
        }

        List<DrugInteractionDTO> interactions = drugInteractionService.checkCandidateWithExistingDrugs(dto.getDrug(), selectedDrugs);
        if (!interactions.isEmpty()) {
            DrugInteractionDTO topDdi = interactions.get(0);
            dto.setInteractionStatus("KNOWN");
            dto.setInteractionSeverity(topDdi.getSeverity());
            dto.setInteractionDescription(topDdi.getDescription());
            dto.setInteractionWarning(String.format("Cảnh báo tương tác (%s): %s", topDdi.getSeverity(), topDdi.getDescription()));
        } else {
            dto.setInteractionStatus("NOT FOUND");
            dto.setInteractionSeverity("None");
            dto.setInteractionDescription("Không tìm thấy cảnh báo tương tác trong FDA DDI.");
        }
    }

    private Set<String> parseItems(String commaSeparated) {
        Set<String> items = new HashSet<>();
        if (commaSeparated != null && !commaSeparated.trim().isEmpty()) {
            String[] parts = commaSeparated.split(",");
            for (String p : parts) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) {
                    items.add(trimmed);
                }
            }
        }
        return items;
    }

    private boolean containsAllCaseInsensitive(Set<String> sourceLower, Set<String> target) {
        for (String t : target) {
            if (!sourceLower.contains(t.toLowerCase())) {
                return false;
            }
        }
        return true;
    }
}
