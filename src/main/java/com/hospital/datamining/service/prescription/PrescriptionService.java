package com.hospital.datamining.service.prescription;

import com.hospital.datamining.entity.*;
import com.hospital.datamining.repository.MedicineRepository;
import com.hospital.datamining.repository.PrescriptionItemRepository;
import com.hospital.datamining.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final MedicineRepository medicineRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               PrescriptionItemRepository prescriptionItemRepository,
                               MedicineRepository medicineRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionItemRepository = prescriptionItemRepository;
        this.medicineRepository = medicineRepository;
    }

    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Prescription> getPrescriptionById(Long id) {
        return prescriptionRepository.findById(id);
    }

    public Optional<Prescription> getPrescriptionByCode(String code) {
        return prescriptionRepository.findByPrescriptionCode(code);
    }

    @Transactional
    public Prescription savePrescription(Prescription prescription) {
        return prescriptionRepository.save(prescription);
    }

    @Transactional
    public Prescription createPrescription(MedicalVisit visit,
                                           Doctor doctor,
                                           String advice,
                                           List<PrescriptionItemInput> items) {
        String code = "RX" + System.currentTimeMillis();

        Prescription prescription = Prescription.builder()
                .prescriptionCode(code)
                .visit(visit)
                .doctor(doctor)
                .advice(advice)
                .status("COMPLETED")
                .build();

        Prescription saved = prescriptionRepository.save(prescription);

        if (items != null) {
            for (PrescriptionItemInput input : items) {
                Optional<Medicine> medOpt = medicineRepository.findById(input.getMedicineId());
                if (medOpt.isPresent()) {
                    PrescriptionItem item = PrescriptionItem.builder()
                            .prescription(saved)
                            .medicine(medOpt.get())
                            .dosage(input.getDosage() != null ? input.getDosage() : "Theo chỉ định")
                            .frequency(input.getFrequency() != null ? input.getFrequency() : "2 lần/ngày")
                            .durationDays(input.getDurationDays() != null ? input.getDurationDays() : 7)
                            .instructions(input.getInstructions())
                            .build();
                    prescriptionItemRepository.save(item);
                }
            }
        }
        return saved;
    }

    public static class PrescriptionItemInput {
        private Long medicineId;
        private String dosage;
        private String frequency;
        private Integer durationDays;
        private String instructions;

        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public Integer getDurationDays() { return durationDays; }
        public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
        public String getInstructions() { return instructions; }
        public void setInstructions(String instructions) { this.instructions = instructions; }
    }
}
