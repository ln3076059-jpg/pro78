package com.hospital.datamining.repository;

import com.hospital.datamining.entity.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionItemRepository extends JpaRepository<TransactionItem, Long> {

    List<TransactionItem> findByTransactionId(Long transactionId);

    List<TransactionItem> findByMedicineId(Long medicineId);
}
