package com.hospital.datamining.service.hospital;

import com.hospital.datamining.entity.Medicine;
import com.hospital.datamining.repository.MedicineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public MedicineService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public Optional<Medicine> getMedicineById(Long id) {
        return medicineRepository.findById(id);
    }

    public Optional<Medicine> getMedicineByCode(String code) {
        return medicineRepository.findByDrugCode(code);
    }

    public Optional<Medicine> getMedicineByGenericName(String genericName) {
        return medicineRepository.findByGenericNameIgnoreCase(genericName);
    }

    @Transactional
    public Medicine saveMedicine(Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    @Transactional
    public void deleteMedicine(Long id) {
        medicineRepository.deleteById(id);
    }
}
