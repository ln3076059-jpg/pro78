package com.hospital.datamining.repository;

import com.hospital.datamining.entity.FrequentItemsetItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FrequentItemsetItemRepository extends JpaRepository<FrequentItemsetItem, Long> {

    List<FrequentItemsetItem> findByItemsetId(Long itemsetId);

    List<FrequentItemsetItem> findByMedicineId(Long medicineId);
}
