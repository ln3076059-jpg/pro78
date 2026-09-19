package com.hospital.datamining.controller;

import com.hospital.datamining.dto.BenchmarkComparisonDTO;
import com.hospital.datamining.dto.MiningParametersDTO;
import com.hospital.datamining.dto.PreprocessSummaryDTO;
import com.hospital.datamining.entity.AssociationRule;
import com.hospital.datamining.entity.FrequentItemset;
import com.hospital.datamining.entity.MiningRun;
import com.hospital.datamining.entity.Transaction;
import com.hospital.datamining.repository.DatasetImportRepository;
import com.hospital.datamining.repository.DatasetStatisticsRepository;
import com.hospital.datamining.repository.FrequentItemsetRepository;
import com.hospital.datamining.repository.TransactionRepository;
import com.hospital.datamining.service.importer.DatasetImportService;
import com.hospital.datamining.service.mining.AprioriMiningService;
import com.hospital.datamining.service.mining.AssociationRuleService;
import com.hospital.datamining.service.mining.FPGrowthMiningService;
import com.hospital.datamining.service.mining.MiningEvaluationService;
import com.hospital.datamining.service.mining.MiningRunService;
import com.hospital.datamining.service.mining.model.MiningParameters;
import com.hospital.datamining.service.mining.model.MiningResult;
import com.hospital.datamining.service.preprocessing.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/mining")
public class MiningController {

    private final DatasetImportService importService;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final AprioriMiningService aprioriService;
    private final FPGrowthMiningService fpGrowthService;
    private final MiningRunService miningRunService;
    private final MiningEvaluationService evaluationService;
    private final AssociationRuleService associationRuleService;
    private final DatasetImportRepository importRepository;
    private final DatasetStatisticsRepository statisticsRepository;
    private final FrequentItemsetRepository itemsetRepository;

    public MiningController(DatasetImportService importService,
                            TransactionService transactionService,
                            TransactionRepository transactionRepository,
                            AprioriMiningService aprioriService,
                            FPGrowthMiningService fpGrowthService,
                            MiningRunService miningRunService,
                            MiningEvaluationService evaluationService,
                            AssociationRuleService associationRuleService,
                            DatasetImportRepository importRepository,
                            DatasetStatisticsRepository statisticsRepository,
                            FrequentItemsetRepository itemsetRepository) {
        this.importService = importService;
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
        this.aprioriService = aprioriService;
        this.fpGrowthService = fpGrowthService;
        this.miningRunService = miningRunService;
        this.evaluationService = evaluationService;
        this.associationRuleService = associationRuleService;
        this.importRepository = importRepository;
        this.statisticsRepository = statisticsRepository;
        this.itemsetRepository = itemsetRepository;
    }

    // 1. Quản lý Dataset
    @GetMapping("/dataset")
    public String datasetPage(Model model) {
        model.addAttribute("currentDataset", transactionService.getCurrentDatasetName());
        model.addAttribute("transactionCount", transactionService.getTransactionCount());
        model.addAttribute("uniqueDrugCount", transactionService.getUniqueDrugCount());
        model.addAttribute("importHistory", importRepository.findAllByOrderByCreatedAtDesc());
        statisticsRepository.findFirstByOrderByCreatedAtDesc().ifPresent(s -> model.addAttribute("latestStats", s));
        return "mining/dataset";
    }

    @PostMapping("/dataset/upload")
    public String uploadDataset(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        PreprocessSummaryDTO result = importService.importUploadedCsv(file);
        if ("SUCCESS".equals(result.getStatus())) {
            ra.addFlashAttribute("successMessage", "Đã nạp và tiền xử lý tệp: " + result.getFileName() + " (" + result.getFinalTransactionsCount() + " transactions)");
        } else {
            ra.addFlashAttribute("errorMessage", result.getMessage());
        }
        return "redirect:/mining/dataset";
    }

    @PostMapping("/dataset/demo")
    public String loadDemoDataset(RedirectAttributes ra) {
        PreprocessSummaryDTO result = importService.importDemoDataset();
        if ("SUCCESS".equals(result.getStatus())) {
            ra.addFlashAttribute("successMessage", "Đã nạp thành công bộ dữ liệu mẫu UCI Diabetes 130-US Hospitals (" + result.getFinalTransactionsCount() + " transactions)!");
        } else {
            ra.addFlashAttribute("errorMessage", result.getMessage());
        }
        return "redirect:/mining/dataset";
    }

    @PostMapping("/dataset/demo-large")
    public String loadLargeDemoDataset(RedirectAttributes ra) {
        PreprocessSummaryDTO result = importService.importUciDiabetesDataset();
        if ("SUCCESS".equals(result.getStatus())) {
            ra.addFlashAttribute("successMessage", "Đã nạp thành công toàn bộ tập dữ liệu UCI Diabetes 130-US Hospitals (" + result.getFinalTransactionsCount() + " transactions, " + result.getUniqueDrugCount() + " nhóm thuốc)!");
        } else {
            ra.addFlashAttribute("errorMessage", result.getMessage());
        }
        return "redirect:/mining/dataset";
    }

    // 2. Tiền xử lý dữ liệu (ETL & Data Understanding)
    @GetMapping("/preprocess")
    public String preprocessPage(Model model) {
        model.addAttribute("transactionCount", transactionService.getTransactionCount());
        model.addAttribute("uniqueDrugCount", transactionService.getUniqueDrugCount());
        model.addAttribute("currentDataset", transactionService.getCurrentDatasetName());
        model.addAttribute("drugFrequencies", transactionService.getDrugFrequencies());
        statisticsRepository.findFirstByOrderByCreatedAtDesc().ifPresent(s -> model.addAttribute("latestStats", s));
        return "mining/preprocess";
    }

    // 2b. Xem danh sách Transactions đã trích xuất (Mục 29 đề tài)
    @GetMapping("/transactions")
    public String transactionsPage(@RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "size", defaultValue = "20") int size,
                                   Model model) {
        long totalInDb = transactionRepository.count();
        if (totalInDb > 0) {
            Page<Transaction> txPage = transactionRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
            model.addAttribute("transactionsPage", txPage);
            model.addAttribute("totalTransactions", totalInDb);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", txPage.getTotalPages());
            model.addAttribute("isDbSource", true);
        } else {
            List<Set<String>> memTx = transactionService.getTransactions();
            model.addAttribute("totalTransactions", memTx.size());
            model.addAttribute("isDbSource", false);
            model.addAttribute("inMemoryTransactions", memTx.size() > 100 ? memTx.subList(0, 100) : memTx);
        }
        model.addAttribute("currentDataset", transactionService.getCurrentDatasetName());
        return "mining/transactions";
    }

    // 3. Chạy thuật toán Apriori
    @GetMapping("/apriori")
    public String aprioriPage(Model model) {
        model.addAttribute("params", MiningParametersDTO.builder()
                .minSupport(0.05)
                .minConfidence(0.40)
                .minLift(1.0)
                .maxItemsetSize(6)
                .build());
        model.addAttribute("runs", miningRunService.getAllRuns());
        return "mining/apriori";
    }

    @PostMapping("/apriori/run")
    public String runApriori(@ModelAttribute("params") MiningParametersDTO paramsDto,
                             Model model,
                             RedirectAttributes ra) {
        if (!transactionService.hasTransactions()) {
            ra.addFlashAttribute("errorMessage", "Chưa có dữ liệu transaction. Vui lòng nạp dataset trước!");
            return "redirect:/mining/dataset";
        }

        MiningParameters params = MiningParameters.builder()
                .minSupport(paramsDto.getMinSupport())
                .minConfidence(paramsDto.getMinConfidence())
                .minLift(paramsDto.getMinLift())
                .maxItemsetSize(paramsDto.getMaxItemsetSize())
                .build();

        List<Set<String>> transactions = transactionService.getTransactions();
        MiningResult result = aprioriService.mine(transactions, params);

        MiningRun run = miningRunService.saveMiningRun(result, params, transactionService.getCurrentDatasetName());

        ra.addFlashAttribute("successMessage",
                String.format("Khai phá Apriori thành công! Sinh được %d tập phổ biến, %d luật kết hợp trong %d ms.",
                        result.getFrequentItemsetCount(), result.getRuleCount(), result.getRuntimeMs()));
        return "redirect:/mining/rules?runId=" + run.getId();
    }

    // 4. Chạy thuật toán FP-Growth
    @GetMapping("/fpgrowth")
    public String fpGrowthPage(Model model) {
        model.addAttribute("params", MiningParametersDTO.builder()
                .minSupport(0.05)
                .minConfidence(0.40)
                .minLift(1.0)
                .maxItemsetSize(6)
                .build());
        model.addAttribute("runs", miningRunService.getAllRuns());
        return "mining/fpgrowth";
    }

    @PostMapping("/fpgrowth/run")
    public String runFpGrowth(@ModelAttribute("params") MiningParametersDTO paramsDto,
                              Model model,
                              RedirectAttributes ra) {
        if (!transactionService.hasTransactions()) {
            ra.addFlashAttribute("errorMessage", "Chưa có dữ liệu transaction. Vui lòng nạp dataset trước!");
            return "redirect:/mining/dataset";
        }

        MiningParameters params = MiningParameters.builder()
                .minSupport(paramsDto.getMinSupport())
                .minConfidence(paramsDto.getMinConfidence())
                .minLift(paramsDto.getMinLift())
                .maxItemsetSize(paramsDto.getMaxItemsetSize())
                .build();

        List<Set<String>> transactions = transactionService.getTransactions();
        MiningResult result = fpGrowthService.mine(transactions, params);

        MiningRun run = miningRunService.saveMiningRun(result, params, transactionService.getCurrentDatasetName());

        ra.addFlashAttribute("successMessage",
                String.format("Khai phá FP-Growth thành công! Sinh được %d tập phổ biến, %d luật kết hợp trong %d ms.",
                        result.getFrequentItemsetCount(), result.getRuleCount(), result.getRuntimeMs()));
        return "redirect:/mining/rules?runId=" + run.getId();
    }

    // 5. Xem Frequent Itemsets
    @GetMapping("/itemsets")
    public String itemsetsPage(@RequestParam(value = "runId", required = false) Long runId,
                               @RequestParam(value = "itemCount", required = false) Integer itemCount,
                               Model model) {
        List<MiningRun> runs = miningRunService.getAllRuns();
        model.addAttribute("runs", runs);

        Long selectedRunId = runId;
        if (selectedRunId == null && !runs.isEmpty()) {
            selectedRunId = runs.get(0).getId();
        }

        model.addAttribute("selectedRunId", selectedRunId);

        if (selectedRunId != null) {
            List<FrequentItemset> itemsets;
            if (itemCount != null && itemCount > 0) {
                itemsets = itemsetRepository.findByMiningRunIdAndItemCountOrderBySupportDesc(selectedRunId, itemCount);
            } else {
                itemsets = itemsetRepository.findByMiningRunIdOrderBySupportDesc(selectedRunId);
            }
            model.addAttribute("itemsets", itemsets);
            model.addAttribute("itemCountFilter", itemCount);
            miningRunService.getRunById(selectedRunId).ifPresent(r -> model.addAttribute("currentRun", r));
        }
        return "mining/itemsets";
    }

    // 6. Xem và Lọc Association Rules
    @GetMapping("/rules")
    public String rulesPage(@RequestParam(value = "runId", required = false) Long runId,
                            @RequestParam(value = "minSupport", defaultValue = "0.0") double minSupport,
                            @RequestParam(value = "minConfidence", defaultValue = "0.0") double minConfidence,
                            @RequestParam(value = "minLift", defaultValue = "0.0") double minLift,
                            @RequestParam(value = "drug", required = false) String drug,
                            @RequestParam(value = "sortBy", defaultValue = "CONFIDENCE_DESC") String sortBy,
                            Model model) {
        List<MiningRun> runs = miningRunService.getAllRuns();
        model.addAttribute("runs", runs);

        Long selectedRunId = runId;
        if (selectedRunId == null && !runs.isEmpty()) {
            selectedRunId = runs.get(0).getId();
        }

        model.addAttribute("selectedRunId", selectedRunId);

        if (selectedRunId != null) {
            List<AssociationRule> rules = associationRuleService.getFilteredRules(
                    selectedRunId, minSupport, minConfidence, minLift, drug, sortBy
            );
            model.addAttribute("rules", rules);
            model.addAttribute("minSupportFilter", minSupport);
            model.addAttribute("minConfidenceFilter", minConfidence);
            model.addAttribute("minLiftFilter", minLift);
            model.addAttribute("drugFilter", drug);
            model.addAttribute("sortBy", sortBy);
            miningRunService.getRunById(selectedRunId).ifPresent(r -> model.addAttribute("currentRun", r));
        }
        return "mining/rules";
    }

    // 7. Benchmark So Sánh Mô Hình (Apriori vs FP-Growth)
    @GetMapping("/benchmark")
    public String benchmarkPage(@RequestParam(value = "minSupport", defaultValue = "0.05") double minSupport,
                                @RequestParam(value = "minConfidence", defaultValue = "0.40") double minConfidence,
                                @RequestParam(value = "minLift", defaultValue = "1.0") double minLift,
                                Model model) {
        model.addAttribute("minSupport", minSupport);
        model.addAttribute("minConfidence", minConfidence);
        model.addAttribute("minLift", minLift);
        model.addAttribute("benchmarks", evaluationService.getAllBenchmarks());

        if (transactionService.hasTransactions()) {
            MiningParameters params = MiningParameters.builder()
                    .minSupport(minSupport)
                    .minConfidence(minConfidence)
                    .minLift(minLift)
                    .build();

            BenchmarkComparisonDTO currentComparison = evaluationService.runBenchmark(
                    transactionService.getTransactions(), params, transactionService.getCurrentDatasetName()
            );
            model.addAttribute("currentComparison", currentComparison);
        }
        return "mining/benchmark";
    }

    @PostMapping("/benchmark/multi")
    public String runMultiBenchmark(@RequestParam(value = "minConfidence", defaultValue = "0.40") double minConfidence,
                                    @RequestParam(value = "minLift", defaultValue = "1.0") double minLift,
                                    RedirectAttributes ra) {
        if (!transactionService.hasTransactions()) {
            ra.addFlashAttribute("errorMessage", "Chưa có transaction để benchmark. Vui lòng nạp dataset trước!");
            return "redirect:/mining/dataset";
        }
        double[] thresholds = new double[]{0.02, 0.05, 0.10};
        evaluationService.runMultiThresholdBenchmark(
                transactionService.getTransactions(), thresholds, minConfidence, minLift, transactionService.getCurrentDatasetName()
        );
        ra.addFlashAttribute("successMessage", "Đã chạy thực nghiệm Benchmark đa ngưỡng thành công trên các mức support: 2%, 5%, 10%!");
        return "redirect:/mining/benchmark";
    }

    // 8. Mining History (Lịch sử các lượt khai phá)
    @GetMapping("/history")
    public String historyPage(Model model) {
        model.addAttribute("runs", miningRunService.getAllRuns());
        return "mining/history";
    }

    @PostMapping("/set-active/{id}")
    public String setActiveModel(@PathVariable("id") Long runId, RedirectAttributes ra) {
        boolean success = miningRunService.setActiveMiningRun(runId);
        if (success) {
            ra.addFlashAttribute("successMessage", "Đã kích hoạt mô hình #" + runId + " làm Active Model cho chức năng gợi ý kê đơn thuốc!");
        } else {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy MiningRun với ID #" + runId);
        }
        return "redirect:/mining/history";
    }

    // 9. Xuất dữ liệu CSV phục vụ báo cáo khoa học
    @GetMapping(value = "/export/rules", produces = "text/csv; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<byte[]> exportRulesCsv(@RequestParam(value = "runId", required = false) Long runId) {
        String csvContent = associationRuleService.exportRulesToCsv(runId);
        byte[] bytes = ("\ufeff" + csvContent).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"association_rules_" + (runId != null ? runId : "all") + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping(value = "/export/benchmark", produces = "text/csv; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<byte[]> exportBenchmarkCsv() {
        String csvContent = associationRuleService.exportBenchmarksToCsv();
        byte[] bytes = ("\ufeff" + csvContent).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"algorithm_benchmark.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping(value = "/export/statistics", produces = "text/csv; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<byte[]> exportStatisticsCsv() {
        String csvContent = associationRuleService.exportStatisticsToCsv();
        byte[] bytes = ("\ufeff" + csvContent).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"dataset_statistics.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }
}
