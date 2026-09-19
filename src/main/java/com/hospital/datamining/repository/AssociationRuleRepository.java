package com.hospital.datamining.repository;

import com.hospital.datamining.entity.AssociationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssociationRuleRepository extends JpaRepository<AssociationRule, Long> {

    List<AssociationRule> findByMiningRunIdOrderByConfidenceDesc(Long miningRunId);

    List<AssociationRule> findByMiningRunIdOrderByLiftDesc(Long miningRunId);

    List<AssociationRule> findByMiningRunIdOrderBySupportDesc(Long miningRunId);

    long countByMiningRunId(Long miningRunId);

    @Query("SELECT DISTINCT r FROM AssociationRule r " +
           "LEFT JOIN r.antecedents a " +
           "LEFT JOIN r.consequents c " +
           "WHERE r.miningRun.id = :miningRunId " +
           "AND r.support >= :minSupport " +
           "AND r.confidence >= :minConfidence " +
           "AND r.lift >= :minLift " +
           "AND (:drugName IS NULL OR :drugName = '' " +
           "     OR LOWER(a.drugName) LIKE LOWER(CONCAT('%', :drugName, '%')) " +
           "     OR LOWER(c.drugName) LIKE LOWER(CONCAT('%', :drugName, '%')) " +
           "     OR LOWER(r.antecedent) LIKE LOWER(CONCAT('%', :drugName, '%')) " +
           "     OR LOWER(r.consequent) LIKE LOWER(CONCAT('%', :drugName, '%'))) " +
           "ORDER BY r.confidence DESC, r.lift DESC")
    List<AssociationRule> findRulesWithFilter(@Param("miningRunId") Long miningRunId,
                                             @Param("minSupport") Double minSupport,
                                             @Param("minConfidence") Double minConfidence,
                                             @Param("minLift") Double minLift,
                                             @Param("drugName") String drugName);

    @Query("SELECT r FROM AssociationRule r WHERE r.miningRun.id = :miningRunId " +
           "AND r.confidence >= :minConfidence " +
           "AND r.lift > :minLift " +
           "ORDER BY r.confidence DESC, r.lift DESC")
    List<AssociationRule> findTopConfidentRules(@Param("miningRunId") Long miningRunId,
                                                @Param("minConfidence") Double minConfidence,
                                                @Param("minLift") Double minLift);

    @Query("SELECT DISTINCT r FROM AssociationRule r " +
           "JOIN r.antecedents a " +
           "WHERE r.miningRun.id = :miningRunId " +
           "AND a.medicine.id IN :medicineIds " +
           "AND r.confidence >= :minConfidence " +
           "AND r.lift > :minLift " +
           "ORDER BY r.confidence DESC, r.lift DESC")
    List<AssociationRule> findRulesByAntecedentMedicineIds(
            @Param("miningRunId") Long miningRunId,
            @Param("medicineIds") java.util.Collection<Long> medicineIds,
            @Param("minConfidence") Double minConfidence,
            @Param("minLift") Double minLift);

    @Query("SELECT DISTINCT r FROM AssociationRule r " +
           "JOIN r.antecedents a " +
           "WHERE r.miningRun.id = :miningRunId " +
           "AND LOWER(a.drugName) IN :drugNames " +
           "AND r.confidence >= :minConfidence " +
           "AND r.lift > :minLift " +
           "ORDER BY r.confidence DESC, r.lift DESC")
    List<AssociationRule> findRulesByAntecedentDrugNames(
            @Param("miningRunId") Long miningRunId,
            @Param("drugNames") java.util.Collection<String> drugNames,
            @Param("minConfidence") Double minConfidence,
            @Param("minLift") Double minLift);
}
