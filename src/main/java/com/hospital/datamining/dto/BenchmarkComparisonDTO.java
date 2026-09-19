package com.hospital.datamining.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BenchmarkComparisonDTO {
    private String datasetName;
    private Integer transactionCount;
    private Double minSupport;
    private Double minConfidence;
    private Double minLift;

    private Long aprioriRuntimeMs;
    private Long fpgrowthRuntimeMs;

    private Integer aprioriItemsetCount;
    private Integer fpgrowthItemsetCount;

    private Integer aprioriRuleCount;
    private Integer fpgrowthRuleCount;

    private Double aprioriMemoryMb;
    private Double fpgrowthMemoryMb;

    private Double ruleOverlapPercentage;
    private Double itemsetOverlapPercentage;
    private String recommendedAlgorithm;
    private String conclusionNotes;
}
