package com.hospital.datamining.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "drug_interactions", indexes = {
        @Index(name = "idx_ddi_drugs", columnList = "drug_name_a, drug_name_b")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drug_name_a", nullable = false, length = 150)
    private String drugNameA;

    @Column(name = "drug_name_b", nullable = false, length = 150)
    private String drugNameB;

    @Column(name = "interaction_status", length = 30)
    @Builder.Default
    private String interactionStatus = "KNOWN"; // KNOWN, NOT FOUND, UNKNOWN

    @Column(length = 30)
    @Builder.Default
    private String severity = "Moderate"; // Major, Moderate, Minor

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 150)
    @Builder.Default
    private String source = "Demo interaction knowledge base (DailyMed ref)";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
