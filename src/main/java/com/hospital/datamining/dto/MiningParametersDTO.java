package com.hospital.datamining.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiningParametersDTO {
    @Builder.Default
    private Double minSupport = 0.10;

    @Builder.Default
    private Double minConfidence = 0.50;

    @Builder.Default
    private Double minLift = 1.0;

    @Builder.Default
    private Integer maxItemsetSize = 6;

    @Builder.Default
    private String algorithm = "APRIORI"; // APRIORI, FP_GROWTH, BOTH

    @Builder.Default
    private String datasetSource = "demo-data/PRESCRIPTIONS_sample.csv";
}
