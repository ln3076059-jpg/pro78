package com.hospital.datamining.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "algorithm_benchmarks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlgorithmBenchmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "benchmark_name", length = 100)
    private String benchmarkName;

    @Column(name = "dataset_name", length = 100)
    private String datasetName;

    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount;

    @Column(name = "min_support", nullable = false)
    private Double minSupport;

    @Column(name = "min_confidence", nullable = false)
    private Double minConfidence;

    @Column(name = "min_lift", nullable = false)
    private Double minLift;

    @Column(name = "apriori_runtime_ms", nullable = false)
    private Long aprioriRuntimeMs;

    @Column(name = "fpgrowth_runtime_ms", nullable = false)
    private Long fpgrowthRuntimeMs;

    @Column(name = "apriori_itemset_count", nullable = false)
    private Integer aprioriItemsetCount;

    @Column(name = "fpgrowth_itemset_count", nullable = false)
    private Integer fpgrowthItemsetCount;

    @Column(name = "apriori_rule_count", nullable = false)
    private Integer aprioriRuleCount;

    @Column(name = "fpgrowth_rule_count", nullable = false)
    private Integer fpgrowthRuleCount;

    @Column(name = "apriori_memory_mb")
    private Double aprioriMemoryMb;

    @Column(name = "fpgrowth_memory_mb")
    private Double fpgrowthMemoryMb;

    @Column(name = "rule_overlap_percentage")
    private Double ruleOverlapPercentage;

    @Column(name = "recommended_algorithm", length = 50)
    private String recommendedAlgorithm;

    @Column(name = "conclusion_notes", columnDefinition = "TEXT")
    private String conclusionNotes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
