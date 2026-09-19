package com.hospital.datamining.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "association_rule_consequents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssociationRuleConsequent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private AssociationRule rule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @Column(name = "drug_name", nullable = false, length = 150)
    private String drugName;
}
