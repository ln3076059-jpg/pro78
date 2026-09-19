package com.hospital.datamining.repository;

import com.hospital.datamining.entity.DrugInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrugInteractionRepository extends JpaRepository<DrugInteraction, Long> {

    @Query("SELECT d FROM DrugInteraction d WHERE " +
           "(LOWER(d.drugNameA) = LOWER(:drugA) AND LOWER(d.drugNameB) = LOWER(:drugB)) OR " +
           "(LOWER(d.drugNameA) = LOWER(:drugB) AND LOWER(d.drugNameB) = LOWER(:drugA))")
    List<DrugInteraction> findInteractionsBetween(String drugA, String drugB);

    @Query("SELECT d FROM DrugInteraction d WHERE " +
           "LOWER(d.drugNameA) = LOWER(:drugName) OR LOWER(d.drugNameB) = LOWER(:drugName)")
    List<DrugInteraction> findByDrugName(String drugName);
}
