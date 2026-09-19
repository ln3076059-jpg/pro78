package com.hospital.datamining.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreprocessSummaryDTO {
    private String fileName;
    private Long fileSizeBytes;
    private Integer initialRecords;
    private Integer validRecords;
    private Integer missingValuesCount;
    private Integer uniqueSubjectCount;
    private Integer uniqueHadmCount;
    private Integer uniqueDrugCount;
    private Integer zeroDrugEncounterCount;
    private Integer oneDrugEncounterCount;
    private Integer multiDrugTransactionCount;
    private Double avgDrugsPerHadm;
    private Integer minDrugsPerHadm;
    private Integer maxDrugsPerHadm;
    private Integer finalTransactionsCount;
    private String topDrugsJson;
    private String ageDistributionJson;
    private String diagnosisDistributionJson;
    private String medicationCountDistributionJson;
    private String status;
    private String message;
}
