package com.hospital.datamining.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "frequent_itemsets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FrequentItemset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mining_run_id", nullable = false)
    private MiningRun miningRun;

    @Column(name = "itemset_string", nullable = false, columnDefinition = "TEXT")
    private String itemsetString;

    @Column(name = "item_count", nullable = false)
    private Integer itemCount;

    @Column(nullable = false)
    private Double support;

    @Column(name = "support_count", nullable = false)
    private Integer supportCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "itemset", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<FrequentItemsetItem> items = new java.util.ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void addItem(FrequentItemsetItem item) {
        items.add(item);
        item.setItemset(this);
    }
}
