package com.hospital.datamining.service.hospital;

import com.hospital.datamining.entity.MedicalVisit;
import com.hospital.datamining.repository.MedicalVisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VisitService {

    private final MedicalVisitRepository visitRepository;

    public VisitService(MedicalVisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    public List<MedicalVisit> getAllVisits() {
        return visitRepository.findAllByOrderByVisitDateDesc();
    }

    public Optional<MedicalVisit> getVisitById(Long id) {
        return visitRepository.findById(id);
    }

    public List<MedicalVisit> getVisitsByPatient(Long patientId) {
        return visitRepository.findByPatientIdOrderByVisitDateDesc(patientId);
    }

    @Transactional
    public MedicalVisit saveVisit(MedicalVisit visit) {
        return visitRepository.save(visit);
    }
}
