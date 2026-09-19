package com.hospital.datamining.repository;

import com.hospital.datamining.entity.FrequentItemset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FrequentItemsetRepository extends JpaRepository<FrequentItemset, Long> {
    List<FrequentItemset> findByMiningRunIdOrderBySupportDesc(Long miningRunId);
    List<FrequentItemset> findByMiningRunIdAndItemCountOrderBySupportDesc(Long miningRunId, Integer itemCount);
    long countByMiningRunId(Long miningRunId);
}
