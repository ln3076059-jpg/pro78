package com.hospital.datamining.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mining_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiningRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_name", length = 100)
    private String runName;

    @Column(nullable = false, length = 50)
    private String algorithm; // APRIORI, FP_GROWTH

    @Column(name = "dataset_source", length = 255)
    private String datasetSource;

    @Column(name = "min_support", nullable = false)
    private Double minSupport;

    @Column(name = "min_confidence", nullable = false)
    private Double minConfidence;

    @Column(name = "min_lift", nullable = false)
    private Double minLift;

    @Column(name = "max_itemset_size")
    @Builder.Default
    private Integer maxItemsetSize = 10;

    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount;

    @Column(name = "unique_drug_count", nullable = false)
    private Integer uniqueDrugCount;

    @Column(name = "frequent_itemset_count", nullable = false)
    private Integer frequentItemsetCount;

    @Column(name = "rule_count", nullable = false)
    private Integer ruleCount;

    @Column(name = "runtime_ms", nullable = false)
    private Long runtimeMs;

    @Column(name = "memory_usage_mb")
    @Builder.Default
    private Double memoryUsageMb = 0.0;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(length = 30)
    @Builder.Default
    private String status = "SUCCESS";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_import_id")
    private DatasetImport datasetImport;

    @Column(name = "selected_for_recommendation", nullable = false)
    @Builder.Default
    private Boolean selectedForRecommendation = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "miningRun", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FrequentItemset> frequentItemsets = new ArrayList<>();

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "miningRun", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AssociationRule> associationRules = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
