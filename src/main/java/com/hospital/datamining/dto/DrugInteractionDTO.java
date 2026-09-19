package com.hospital.datamining.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugInteractionDTO {
    private String drugA;
    private String drugB;
    private String status; // KNOWN, NOT FOUND, UNKNOWN
    private String severity; // Major, Moderate, Minor, None
    private String description;
    private String source;
}
