package com.hospital.datamining.controller;

import com.hospital.datamining.entity.*;
import com.hospital.datamining.repository.DoctorRepository;
import com.hospital.datamining.repository.MedicalVisitRepository;
import com.hospital.datamining.repository.MedicineRepository;
import com.hospital.datamining.service.prescription.PrescriptionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/prescriptions")
public class PrescriptionWebController {

    private final PrescriptionService prescriptionService;
    private final MedicalVisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final MedicineRepository medicineRepository;

    public PrescriptionWebController(PrescriptionService prescriptionService,
                                     MedicalVisitRepository visitRepository,
                                     DoctorRepository doctorRepository,
                                     MedicineRepository medicineRepository) {
        this.prescriptionService = prescriptionService;
        this.visitRepository = visitRepository;
        this.doctorRepository = doctorRepository;
        this.medicineRepository = medicineRepository;
    }

    @GetMapping
    public String listPrescriptions(Model model) {
        model.addAttribute("prescriptions", prescriptionService.getAllPrescriptions());
        return "prescription/list";
    }

    @GetMapping("/create")
    public String createPrescriptionForm(@RequestParam(value = "visitId", required = false) Long visitId,
                                         Authentication authentication,
                                         Model model) {
        List<MedicalVisit> activeVisits = visitRepository.findAllByOrderByVisitDateDesc();
        model.addAttribute("visits", activeVisits);

        MedicalVisit selectedVisit = null;
        if (visitId != null) {
            selectedVisit = visitRepository.findById(visitId).orElse(null);
        } else if (!activeVisits.isEmpty()) {
            selectedVisit = activeVisits.get(0);
        }
        model.addAttribute("selectedVisit", selectedVisit);

        // Lấy thông tin bác sĩ đang đăng nhập hoặc mặc định
        Doctor currentDoctor = null;
        if (authentication != null) {
            currentDoctor = doctorRepository.findByUserUsername(authentication.getName()).orElse(null);
        }
        if (currentDoctor == null) {
            currentDoctor = doctorRepository.findAll().stream().findFirst().orElse(null);
        }
        model.addAttribute("currentDoctor", currentDoctor);
        model.addAttribute("medicines", medicineRepository.findAll());

        return "prescription/create";
    }

    @PostMapping("/save")
    public String savePrescription(@RequestParam("visitId") Long visitId,
                                   @RequestParam("doctorId") Long doctorId,
                                   @RequestParam(value = "advice", required = false) String advice,
                                   @RequestParam(value = "medicineIds", required = false) List<Long> medicineIds,
                                   @RequestParam(value = "dosages", required = false) List<String> dosages,
                                   @RequestParam(value = "frequencies", required = false) List<String> frequencies,
                                   @RequestParam(value = "durations", required = false) List<Integer> durations,
                                   @RequestParam(value = "instructions", required = false) List<String> instructions,
                                   RedirectAttributes ra) {

        MedicalVisit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca khám ID: " + visitId));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ ID: " + doctorId));

        List<PrescriptionService.PrescriptionItemInput> items = new ArrayList<>();
        if (medicineIds != null) {
            for (int i = 0; i < medicineIds.size(); i++) {
                PrescriptionService.PrescriptionItemInput item = new PrescriptionService.PrescriptionItemInput();
                item.setMedicineId(medicineIds.get(i));
                item.setDosage((dosages != null && i < dosages.size()) ? dosages.get(i) : "Theo chỉ định");
                item.setFrequency((frequencies != null && i < frequencies.size()) ? frequencies.get(i) : "2 lần/ngày");
                item.setDurationDays((durations != null && i < durations.size()) ? durations.get(i) : 7);
                item.setInstructions((instructions != null && i < instructions.size()) ? instructions.get(i) : "");
                items.add(item);
            }
        }

        Prescription saved = prescriptionService.createPrescription(visit, doctor, advice, items);
        ra.addFlashAttribute("successMessage", "Kê đơn thuốc thành công! Mã đơn: " + saved.getPrescriptionCode());
        return "redirect:/prescriptions/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String viewPrescription(@PathVariable("id") Long id, Model model) {
        Optional<Prescription> pOpt = prescriptionService.getPrescriptionById(id);
        if (pOpt.isEmpty()) {
            return "redirect:/prescriptions";
        }
        model.addAttribute("prescription", pOpt.get());
        return "prescription/detail";
    }
}
