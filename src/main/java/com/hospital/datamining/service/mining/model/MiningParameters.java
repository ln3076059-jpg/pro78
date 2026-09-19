package com.hospital.datamining.service.mining.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiningParameters {
    private double minSupport;
    private double minConfidence;
    private double minLift;
    @Builder.Default
    private int maxItemsetSize = 10;
}
