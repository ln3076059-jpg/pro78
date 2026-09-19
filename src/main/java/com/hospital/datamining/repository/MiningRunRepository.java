package com.hospital.datamining.repository;

import com.hospital.datamining.entity.MiningRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MiningRunRepository extends JpaRepository<MiningRun, Long> {
    List<MiningRun> findAllByOrderByCreatedAtDesc();
    List<MiningRun> findByAlgorithmOrderByCreatedAtDesc(String algorithm);
    Optional<MiningRun> findFirstByStatusOrderByCreatedAtDesc(String status);
}
