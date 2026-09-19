package com.hospital.datamining.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transaction_items", indexes = {
        @Index(name = "idx_tx_item_trans", columnList = "transaction_id"),
        @Index(name = "idx_tx_item_med", columnList = "medicine_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @Column(name = "drug_name", nullable = false, length = 150)
    private String drugName;
}
