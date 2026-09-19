package com.hospital.datamining.repository;

import com.hospital.datamining.entity.AlgorithmBenchmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlgorithmBenchmarkRepository extends JpaRepository<AlgorithmBenchmark, Long> {
    List<AlgorithmBenchmark> findAllByOrderByCreatedAtDesc();
}
