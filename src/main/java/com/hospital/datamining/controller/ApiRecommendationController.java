package com.hospital.datamining.controller;

import com.hospital.datamining.dto.RecommendationResponseDTO;
import com.hospital.datamining.service.prescription.DrugRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class ApiRecommendationController {

    private final DrugRecommendationService recommendationService;

    public ApiRecommendationController(DrugRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * GET /api/recommendations/drugs?drugIds=1,5 hoặc ?drugNames=Aspirin,Heparin
     * Trả về danh sách thuốc thường được kê cùng dựa trên luật kết hợp khai phá được
     */
    @GetMapping("/drugs")
    public ResponseEntity<List<RecommendationResponseDTO>> getRecommendations(
            @RequestParam(value = "drugIds", required = false) String drugIdsStr,
            @RequestParam(value = "drugNames", required = false) String drugNamesStr,
            @RequestParam(value = "drugs", required = false) String drugsStr,
            @RequestParam(value = "topN", defaultValue = "5") int topN) {

        List<RecommendationResponseDTO> recommendations;

        if (drugNamesStr == null || drugNamesStr.trim().isEmpty()) {
            drugNamesStr = drugsStr;
        }

        if (drugIdsStr != null && !drugIdsStr.trim().isEmpty()) {
            List<Long> ids = new ArrayList<>();
            for (String idPart : drugIdsStr.split(",")) {
                try {
                    ids.add(Long.parseLong(idPart.trim()));
                } catch (NumberFormatException ignored) {}
            }
            recommendations = recommendationService.recommendByMedicineIds(ids, topN);
        } else if (drugNamesStr != null && !drugNamesStr.trim().isEmpty()) {
            List<String> names = Arrays.stream(drugNamesStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            recommendations = recommendationService.recommendByDrugNames(names, topN);
        } else {
            recommendations = new ArrayList<>();
        }

        return ResponseEntity.ok(recommendations);
    }
}
