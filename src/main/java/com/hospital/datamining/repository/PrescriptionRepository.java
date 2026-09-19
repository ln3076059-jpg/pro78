package com.hospital.datamining.repository;

import com.hospital.datamining.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByPrescriptionCode(String prescriptionCode);
    List<Prescription> findByVisitPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Prescription> findAllByOrderByCreatedAtDesc();
}
