package com.hospital.datamining.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dataset_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_id")
    private DatasetImport datasetImport;

    @Column(name = "dataset_name", nullable = false)
    private String datasetName;

    @Column(name = "total_records")
    @Builder.Default
    private Integer totalRecords = 0; // Tổng số encounter

    @Column(name = "valid_records")
    @Builder.Default
    private Integer validRecords = 0;

    @Column(name = "missing_hadm_count")
    @Builder.Default
    private Integer missingHadmCount = 0;

    @Column(name = "missing_drug_count")
    @Builder.Default
    private Integer missingDrugCount = 0;

    @Column(name = "unique_subject_count")
    @Builder.Default
    private Integer uniqueSubjectCount = 0; // Số bệnh nhân (patient_nbr)

    @Column(name = "unique_hadm_count")
    @Builder.Default
    private Integer uniqueHadmCount = 0; // Số encounter duy nhất

    @Column(name = "unique_drug_count")
    @Builder.Default
    private Integer uniqueDrugCount = 0; // Số thuốc được xem xét

    @Column(name = "zero_drug_encounter_count")
    @Builder.Default
    private Integer zeroDrugEncounterCount = 0; // Số transaction không có thuốc

    @Column(name = "one_drug_encounter_count")
    @Builder.Default
    private Integer oneDrugEncounterCount = 0; // Số transaction có 1 thuốc

    @Column(name = "multi_drug_transaction_count")
    @Builder.Default
    private Integer multiDrugTransactionCount = 0; // Số transaction có >= 2 thuốc

    @Column(name = "unique_diagnosis_count")
    @Builder.Default
    private Integer uniqueDiagnosisCount = 0;

    @Column(name = "avg_drugs_per_hadm")
    @Builder.Default
    private Double avgDrugsPerHadm = 0.0; // Số thuốc trung bình / encounter

    @Column(name = "min_drugs_per_hadm")
    @Builder.Default
    private Integer minDrugsPerHadm = 0; // Min số thuốc / encounter

    @Column(name = "max_drugs_per_hadm")
    @Builder.Default
    private Integer maxDrugsPerHadm = 0; // Max số thuốc / encounter

    @Column(name = "final_transaction_count")
    @Builder.Default
    private Integer finalTransactionCount = 0;

    @Column(name = "top_drugs_json", columnDefinition = "TEXT")
    private String topDrugsJson; // Top 10 thuốc xuất hiện nhiều nhất

    @Column(name = "age_distribution_json", columnDefinition = "TEXT")
    private String ageDistributionJson; // Tỷ lệ bệnh nhân theo nhóm tuổi

    @Column(name = "diagnosis_distribution_json", columnDefinition = "TEXT")
    private String diagnosisDistributionJson; // Top chẩn đoán (diag_1, diag_2, diag_3)

    @Column(name = "medication_count_distribution_json", columnDefinition = "TEXT")
    private String medicationCountDistributionJson; // Phân phối số thuốc / encounter

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
