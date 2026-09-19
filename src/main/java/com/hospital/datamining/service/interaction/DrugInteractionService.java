package com.hospital.datamining.service.interaction;

import com.hospital.datamining.dto.DrugInteractionDTO;
import com.hospital.datamining.entity.DrugInteraction;
import com.hospital.datamining.repository.DrugInteractionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service kiểm tra tương tác thuốc (Drug-Drug Interaction - DDI) dựa trên nguồn FDA DDI / DailyMed.
 * Mục 28 đề tài: Tách biệt hoàn toàn giữa đồng xuất hiện (Association Rules) và cảnh báo tương tác (FDA DDI).
 */
@Service
public class DrugInteractionService {

    private static final Logger log = LoggerFactory.getLogger(DrugInteractionService.class);

    private final DrugInteractionRepository interactionRepository;

    public DrugInteractionService(DrugInteractionRepository interactionRepository) {
        this.interactionRepository = interactionRepository;
    }

    /**
     * Tra cứu tương tác giữa hai loại thuốc
     */
    public DrugInteractionDTO checkPair(String drugA, String drugB) {
        if (drugA == null || drugB == null || drugA.trim().isEmpty() || drugB.trim().isEmpty()) {
            return DrugInteractionDTO.builder()
                    .drugA(drugA)
                    .drugB(drugB)
                    .status("UNKNOWN")
                    .severity("None")
                    .description("Tên thuốc không hợp lệ.")
                    .source("FDA DDI")
                    .build();
        }

        List<DrugInteraction> list = interactionRepository.findInteractionsBetween(drugA.trim(), drugB.trim());
        if (!list.isEmpty()) {
            DrugInteraction match = list.get(0);
            return DrugInteractionDTO.builder()
                    .drugA(match.getDrugNameA())
                    .drugB(match.getDrugNameB())
                    .status(match.getInteractionStatus() != null ? match.getInteractionStatus() : "KNOWN")
                    .severity(match.getSeverity() != null ? match.getSeverity() : "Moderate")
                    .description(match.getDescription())
                    .source(match.getSource() != null ? match.getSource() : "FDA DDI")
                    .build();
        }

        return DrugInteractionDTO.builder()
                .drugA(drugA)
                .drugB(drugB)
                .status("NOT FOUND")
                .severity("None")
                .description("Không ghi nhận tương tác thuốc nghiêm trọng trong cơ sở dữ liệu FDA DDI.")
                .source("FDA DDI")
                .build();
    }

    /**
     * Kiểm tra tương tác giữa một thuốc ứng viên được gợi ý với toàn bộ danh sách thuốc hiện có trong đơn
     */
    public List<DrugInteractionDTO> checkCandidateWithExistingDrugs(String candidateDrug, List<String> currentDrugs) {
        List<DrugInteractionDTO> results = new ArrayList<>();
        if (candidateDrug == null || currentDrugs == null) {
            return results;
        }

        for (String current : currentDrugs) {
            if (current != null && !current.equalsIgnoreCase(candidateDrug)) {
                DrugInteractionDTO ddi = checkPair(candidateDrug, current);
                if ("KNOWN".equalsIgnoreCase(ddi.getStatus())) {
                    results.add(ddi);
                }
            }
        }
        return results;
    }

    /**
     * Kiểm tra tương tác giữa tất cả các cặp thuốc trong đơn hiện tại
     */
    public List<DrugInteractionDTO> checkAllPrescriptionPairs(List<String> drugs) {
        List<DrugInteractionDTO> knownInteractions = new ArrayList<>();
        if (drugs == null || drugs.size() < 2) {
            return knownInteractions;
        }

        for (int i = 0; i < drugs.size(); i++) {
            for (int j = i + 1; j < drugs.size(); j++) {
                DrugInteractionDTO dto = checkPair(drugs.get(i), drugs.get(j));
                if ("KNOWN".equalsIgnoreCase(dto.getStatus())) {
                    knownInteractions.add(dto);
                }
            }
        }
        return knownInteractions;
    }
}
