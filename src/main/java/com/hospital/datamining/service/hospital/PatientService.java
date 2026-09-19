package com.hospital.datamining.service.hospital;

import com.hospital.datamining.entity.Diagnosis;
import com.hospital.datamining.entity.Patient;
import com.hospital.datamining.repository.DiagnosisRepository;
import com.hospital.datamining.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final DiagnosisRepository diagnosisRepository;

    public PatientService(PatientRepository patientRepository,
                          DiagnosisRepository diagnosisRepository) {
        this.patientRepository = patientRepository;
        this.diagnosisRepository = diagnosisRepository;
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    public Optional<Patient> getPatientByCode(String code) {
        return patientRepository.findByPatientCode(code);
    }

    @Transactional
    public Patient savePatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public List<Diagnosis> getAllDiagnoses() {
        return diagnosisRepository.findAll();
    }
}
