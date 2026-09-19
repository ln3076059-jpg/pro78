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

    public void validate() {
        if (Double.isNaN(minSupport) || Double.isInfinite(minSupport) || minSupport <= 0.0 || minSupport > 1.0) {
            throw new IllegalArgumentException("minSupport phải thuộc khoảng (0, 1]. Nhận được: " + minSupport);
        }
        if (Double.isNaN(minConfidence) || Double.isInfinite(minConfidence) || minConfidence < 0.0 || minConfidence > 1.0) {
            throw new IllegalArgumentException("minConfidence phải thuộc khoảng [0, 1]. Nhận được: " + minConfidence);
        }
        if (Double.isNaN(minLift) || Double.isInfinite(minLift) || minLift < 0.0) {
            throw new IllegalArgumentException("minLift phải >= 0. Nhận được: " + minLift);
        }
        if (maxItemsetSize < 2 || maxItemsetSize > 20) {
            throw new IllegalArgumentException("maxItemsetSize phải trong khoảng [2, 20]. Nhận được: " + maxItemsetSize);
        }
    }
}
