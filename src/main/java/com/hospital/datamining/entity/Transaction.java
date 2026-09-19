package com.hospital.datamining.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_tx_encounter", columnList = "encounter_id"),
        @Index(name = "idx_tx_patient", columnList = "patient_reference"),
        @Index(name = "idx_tx_import", columnList = "source_import_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "encounter_id", nullable = false, length = 64)
    private String encounterId;

    @Column(name = "patient_reference", length = 64)
    private String patientReference;

    @Column(name = "source_import_id")
    private Long sourceImportId;

    @Column(name = "item_count", nullable = false)
    @Builder.Default
    private Integer itemCount = 0;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransactionItem> items = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void addItem(TransactionItem item) {
        items.add(item);
        item.setTransaction(this);
    }
}
