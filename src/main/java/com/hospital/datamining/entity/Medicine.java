package com.hospital.datamining.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drug_code", nullable = false, unique = true, length = 50)
    private String drugCode;

    @Column(name = "generic_name", nullable = false, length = 150)
    private String genericName;

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Column(name = "brand_name", length = 150)
    private String brandName;

    @Column(name = "dosage_form", length = 50)
    private String dosageForm;

    @Column(length = 50)
    private String strength;

    @Column(length = 50)
    private String route;

    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = genericName;
        }
    }

    public String getCode() {
        return drugCode;
    }

    public void setCode(String code) {
        this.drugCode = code;
    }

    public String getName() {
        return genericName;
    }

    public void setName(String name) {
        this.genericName = name;
    }

    public String getDisplayNameOrName() {
        return displayName != null && !displayName.isEmpty() ? displayName : genericName;
    }
}
