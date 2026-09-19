package com.hospital.datamining.repository;

import com.hospital.datamining.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByEncounterId(String encounterId);

    List<Transaction> findBySourceImportId(Long sourceImportId);

    Page<Transaction> findBySourceImportId(Long sourceImportId, Pageable pageable);

    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.items WHERE t.sourceImportId = :importId")
    List<Transaction> findAllWithItemsByImportId(Long importId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.sourceImportId = :importId")
    long countByImportId(Long importId);
}
