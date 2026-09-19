package com.hospital.datamining.repository;

import com.hospital.datamining.entity.MedicalVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalVisitRepository extends JpaRepository<MedicalVisit, Long> {
    Optional<MedicalVisit> findByVisitCode(String visitCode);
    List<MedicalVisit> findByPatientIdOrderByVisitDateDesc(Long patientId);
    List<MedicalVisit> findAllByOrderByVisitDateDesc();
}
