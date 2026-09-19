package com.hospital.datamining.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleDTO {
    private Long id;
    private Long miningRunId;
    private String antecedent;
    private String consequent;
    private Double support;
    private Double confidence;
    private Double lift;
    private Integer antecedentSize;
    private Integer consequentSize;
    private String algorithm;
    private String createdAt;
}
