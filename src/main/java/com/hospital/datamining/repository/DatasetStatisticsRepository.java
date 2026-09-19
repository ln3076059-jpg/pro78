package com.hospital.datamining.repository;

import com.hospital.datamining.entity.DatasetStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetStatisticsRepository extends JpaRepository<DatasetStatistics, Long> {

    List<DatasetStatistics> findAllByOrderByCreatedAtDesc();

    Optional<DatasetStatistics> findFirstByOrderByCreatedAtDesc();

    Optional<DatasetStatistics> findByDatasetImportId(Long importId);
}
