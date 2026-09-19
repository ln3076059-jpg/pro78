package com.hospital.datamining.repository;

import com.hospital.datamining.entity.DatasetImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetImportRepository extends JpaRepository<DatasetImport, Long> {
    List<DatasetImport> findAllByOrderByCreatedAtDesc();
}
