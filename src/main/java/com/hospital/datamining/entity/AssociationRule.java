package com.hospital.datamining.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "association_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssociationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mining_run_id", nullable = false)
    private MiningRun miningRun;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String antecedent;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String consequent;

    @Column(nullable = false)
    private Double support;

    @Column(nullable = false)
    private Double confidence;

    @Column(nullable = false)
    private Double lift;

    @Column(name = "antecedent_size", nullable = false)
    private Integer antecedentSize;

    @Column(name = "consequent_size", nullable = false)
    private Integer consequentSize;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<AssociationRuleItem> items = new LinkedHashSet<>();

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<AssociationRuleAntecedent> antecedents = new LinkedHashSet<>();

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<AssociationRuleConsequent> consequents = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void addItem(AssociationRuleItem item) {
        items.add(item);
        item.setRule(this);
    }

    public void addAntecedent(AssociationRuleAntecedent item) {
        antecedents.add(item);
        item.setRule(this);
    }

    public void addConsequent(AssociationRuleConsequent item) {
        consequents.add(item);
        item.setRule(this);
    }
}
