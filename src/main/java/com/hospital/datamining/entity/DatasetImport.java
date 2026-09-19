package com.hospital.datamining.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dataset_imports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetImport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "source", length = 150)
    @Builder.Default
    private String source = "UCI Diabetes 130-US Hospitals";

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "total_records")
    @Builder.Default
    private Integer totalRecords = 0;

    @Column(name = "valid_records")
    @Builder.Default
    private Integer validRecords = 0;

    @Column(name = "invalid_records")
    @Builder.Default
    private Integer invalidRecords = 0;

    @Column(name = "unique_hadm_count")
    @Builder.Default
    private Integer uniqueHadmCount = 0; // Số encounter duy nhất

    @Column(name = "unique_drug_count")
    @Builder.Default
    private Integer uniqueDrugCount = 0;

    @Column(name = "final_transactions_count")
    @Builder.Default
    private Integer finalTransactionsCount = 0;

    @Column(length = 30)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PROCESSING, COMPLETED, FAILED

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Integer getTotalRows() {
        return totalRecords;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRecords = totalRows;
    }

    public Integer getValidRows() {
        return validRecords;
    }

    public void setValidRows(Integer validRows) {
        this.validRecords = validRows;
    }

    public Integer getInvalidRows() {
        return invalidRecords;
    }

    public void setInvalidRows(Integer invalidRows) {
        this.invalidRecords = invalidRows;
    }

    public Integer getTransactionCount() {
        return finalTransactionsCount;
    }

    public void setTransactionCount(Integer transactionCount) {
        this.finalTransactionsCount = transactionCount;
    }

    public Integer getMedicineCount() {
        return uniqueDrugCount;
    }

    public void setMedicineCount(Integer medicineCount) {
        this.uniqueDrugCount = medicineCount;
    }
}
