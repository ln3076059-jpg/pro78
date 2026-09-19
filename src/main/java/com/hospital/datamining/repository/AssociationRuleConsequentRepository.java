package com.hospital.datamining.repository;

import com.hospital.datamining.entity.AssociationRuleConsequent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssociationRuleConsequentRepository extends JpaRepository<AssociationRuleConsequent, Long> {

    List<AssociationRuleConsequent> findByRuleId(Long ruleId);

    List<AssociationRuleConsequent> findByMedicineId(Long medicineId);
}
