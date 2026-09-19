package com.hospital.datamining.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "association_rule_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssociationRuleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private AssociationRule rule;

    @Column(name = "item_name", nullable = false, length = 150)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 20)
    private ItemRoleType roleType; // ANTECEDENT, CONSEQUENT

    public enum ItemRoleType {
        ANTECEDENT,
        CONSEQUENT
    }
}
