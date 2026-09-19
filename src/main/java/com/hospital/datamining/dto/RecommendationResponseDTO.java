package com.hospital.datamining.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponseDTO {
    private Long medicineId;
    private String drug;
    private Double support;
    private Double confidence;
    private Double lift;
    private String matchingAntecedent;
    private String clinicalDisclaimer;
    // Tích hợp FDA DDI (Mục 28)
    private String interactionStatus; // KNOWN, NOT FOUND, UNKNOWN
    private String interactionSeverity; // Major, Moderate, Minor, None
    private String interactionDescription;
    private String interactionWarning;
}
