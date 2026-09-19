package com.hospital.datamining.repository;

import com.hospital.datamining.entity.AssociationRuleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssociationRuleItemRepository extends JpaRepository<AssociationRuleItem, Long> {
    List<AssociationRuleItem> findByRuleId(Long ruleId);
    List<AssociationRuleItem> findByItemNameIgnoreCaseAndRoleType(String itemName, AssociationRuleItem.ItemRoleType roleType);
}
