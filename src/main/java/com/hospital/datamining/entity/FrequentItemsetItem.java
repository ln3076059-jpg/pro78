package com.hospital.datamining.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "frequent_itemset_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FrequentItemsetItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemset_id", nullable = false)
    private FrequentItemset itemset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @Column(name = "drug_name", nullable = false, length = 150)
    private String drugName;
}
