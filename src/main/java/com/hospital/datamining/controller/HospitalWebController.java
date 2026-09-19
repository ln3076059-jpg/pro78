package com.hospital.datamining.controller;

import com.hospital.datamining.entity.Medicine;
import com.hospital.datamining.service.hospital.MedicineService;
import com.hospital.datamining.service.hospital.PatientService;
import com.hospital.datamining.service.hospital.VisitService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HospitalWebController {

    private final PatientService patientService;
    private final MedicineService medicineService;
    private final VisitService visitService;

    public HospitalWebController(PatientService patientService,
                                 MedicineService medicineService,
                                 VisitService visitService) {
        this.patientService = patientService;
        this.medicineService = medicineService;
        this.visitService = visitService;
    }

    @GetMapping("/patients")
    public String listPatients(Model model) {
        model.addAttribute("patients", patientService.getAllPatients());
        return "hospital/patients";
    }

    @GetMapping("/medicines")
    public String listMedicines(Model model) {
        model.addAttribute("medicines", medicineService.getAllMedicines());
        return "hospital/medicines";
    }

    @PostMapping("/medicines/save")
    public String saveMedicine(@ModelAttribute Medicine medicine, RedirectAttributes ra) {
        medicineService.saveMedicine(medicine);
        ra.addFlashAttribute("successMessage", "Lưu thông tin thuốc thành công!");
        return "redirect:/medicines";
    }

    @GetMapping("/visits")
    public String listVisits(Model model) {
        model.addAttribute("visits", visitService.getAllVisits());
        return "hospital/visits";
    }
}
