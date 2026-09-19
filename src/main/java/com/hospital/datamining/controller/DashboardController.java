package com.hospital.datamining.controller;

import com.hospital.datamining.entity.AssociationRule;
import com.hospital.datamining.entity.MiningRun;
import com.hospital.datamining.repository.*;
import com.hospital.datamining.service.mining.MiningRunService;
import com.hospital.datamining.service.preprocessing.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class DashboardController {

    private final PatientRepository patientRepository;
    private final MedicineRepository medicineRepository;
    private final MedicalVisitRepository visitRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final TransactionService transactionService;
    private final MiningRunService miningRunService;
    private final AssociationRuleRepository ruleRepository;
    private final AlgorithmBenchmarkRepository benchmarkRepository;
    private final DatasetStatisticsRepository statisticsRepository;

    public DashboardController(PatientRepository patientRepository,
                               MedicineRepository medicineRepository,
                               MedicalVisitRepository visitRepository,
                               PrescriptionRepository prescriptionRepository,
                               TransactionService transactionService,
                               MiningRunService miningRunService,
                               AssociationRuleRepository ruleRepository,
                               AlgorithmBenchmarkRepository benchmarkRepository,
                               DatasetStatisticsRepository statisticsRepository) {
        this.patientRepository = patientRepository;
        this.medicineRepository = medicineRepository;
        this.visitRepository = visitRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.transactionService = transactionService;
        this.miningRunService = miningRunService;
        this.ruleRepository = ruleRepository;
        this.benchmarkRepository = benchmarkRepository;
        this.statisticsRepository = statisticsRepository;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalPatients", patientRepository.count());
        model.addAttribute("totalMedicines", medicineRepository.count());
        model.addAttribute("totalVisits", visitRepository.count());
        model.addAttribute("totalPrescriptions", prescriptionRepository.count());

        model.addAttribute("transactionCount", transactionService.getTransactionCount());
        model.addAttribute("uniqueDrugCount", transactionService.getUniqueDrugCount());
        model.addAttribute("currentDataset", transactionService.getCurrentDatasetName());

        // Lấy thống kê Data Understanding mới nhất
        statisticsRepository.findFirstByOrderByCreatedAtDesc().ifPresent(s -> model.addAttribute("latestStats", s));

        Optional<MiningRun> latestRunOpt = miningRunService.getLatestSuccessfulRun();
        if (latestRunOpt.isPresent()) {
            MiningRun run = latestRunOpt.get();
            model.addAttribute("latestRun", run);

            // Lấy Top 10 luật có Confidence cao
            List<AssociationRule> topConfRules = ruleRepository.findByMiningRunIdOrderByConfidenceDesc(run.getId());
            if (topConfRules.size() > 10) topConfRules = topConfRules.subList(0, 10);
            model.addAttribute("topConfRules", topConfRules);

            // Lấy Top 10 luật có Lift cao
            List<AssociationRule> topLiftRules = ruleRepository.findByMiningRunIdOrderByLiftDesc(run.getId());
            if (topLiftRules.size() > 10) topLiftRules = topLiftRules.subList(0, 10);
            model.addAttribute("topLiftRules", topLiftRules);

            // Phân phối số lượng luật theo ngưỡng min support (Sensitivity distribution)
            List<AssociationRule> allRules = ruleRepository.findByMiningRunIdOrderByConfidenceDesc(run.getId());
            double[] supportBrackets = new double[]{0.05, 0.10, 0.15, 0.20, 0.25};
            List<String> supportLabels = new ArrayList<>();
            List<Long> supportCounts = new ArrayList<>();

            for (double s : supportBrackets) {
                long count = allRules.stream().filter(r -> r.getSupport() >= s - 1e-6).count();
                supportLabels.add(String.format(">= %.0f%%", s * 100));
                supportCounts.add(count);
            }
            model.addAttribute("supportLabels", supportLabels);
            model.addAttribute("supportCounts", supportCounts);
        }

        // Top 10 thuốc xuất hiện nhiều nhất trong tập transactions
        Map<String, Integer> drugFreq = transactionService.getDrugFrequencies();
        List<Map.Entry<String, Integer>> sortedFreq = new ArrayList<>(drugFreq.entrySet());
        sortedFreq.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        if (sortedFreq.size() > 10) sortedFreq = sortedFreq.subList(0, 10);

        List<String> topDrugNames = new ArrayList<>();
        List<Integer> topDrugCounts = new ArrayList<>();
        for (Map.Entry<String, Integer> e : sortedFreq) {
            topDrugNames.add(e.getKey());
            topDrugCounts.add(e.getValue());
        }
        model.addAttribute("topDrugNames", topDrugNames);
        model.addAttribute("topDrugCounts", topDrugCounts);

        // Lấy Benchmark gần nhất
        benchmarkRepository.findAllByOrderByCreatedAtDesc().stream().findFirst().ifPresent(b -> model.addAttribute("latestBenchmark", b));

        return "dashboard/index";
    }
}
