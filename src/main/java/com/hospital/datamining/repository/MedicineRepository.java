package com.hospital.datamining.repository;

import com.hospital.datamining.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Optional<Medicine> findByDrugCode(String drugCode);
    Optional<Medicine> findByGenericNameIgnoreCase(String genericName);
}
