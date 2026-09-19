package com.hospital.datamining.repository;

import com.hospital.datamining.entity.DatasetImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetImportRepository extends JpaRepository<DatasetImport, Long> {
    List<DatasetImport> findAllByOrderByCreatedAtDesc();
    List<DatasetImport> findByFileNameOrderByCreatedAtDesc(String fileName);
    Optional<DatasetImport> findFirstByStatusOrderByCreatedAtDesc(String status);
}
