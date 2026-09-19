package com.hospital.datamining.repository;

import com.hospital.datamining.entity.AssociationRuleAntecedent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssociationRuleAntecedentRepository extends JpaRepository<AssociationRuleAntecedent, Long> {

    List<AssociationRuleAntecedent> findByRuleId(Long ruleId);

    List<AssociationRuleAntecedent> findByMedicineId(Long medicineId);
}
