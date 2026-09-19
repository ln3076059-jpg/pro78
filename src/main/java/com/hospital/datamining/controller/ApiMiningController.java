package com.hospital.datamining.controller;

import com.hospital.datamining.dto.BenchmarkComparisonDTO;
import com.hospital.datamining.dto.MiningParametersDTO;
import com.hospital.datamining.dto.PreprocessSummaryDTO;
import com.hospital.datamining.dto.RuleDTO;
import com.hospital.datamining.entity.AssociationRule;
import com.hospital.datamining.entity.MiningRun;
import com.hospital.datamining.repository.AssociationRuleRepository;
import com.hospital.datamining.service.importer.MimicImportService;
import com.hospital.datamining.service.mining.AprioriMiningService;
import com.hospital.datamining.service.mining.FPGrowthMiningService;
import com.hospital.datamining.service.mining.MiningEvaluationService;
import com.hospital.datamining.service.mining.MiningRunService;
import com.hospital.datamining.service.mining.model.MiningParameters;
import com.hospital.datamining.service.mining.model.MiningResult;
import com.hospital.datamining.service.preprocessing.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class ApiMiningController {

    private final MimicImportService importService;
    private final TransactionService transactionService;
    private final AprioriMiningService aprioriService;
    private final FPGrowthMiningService fpGrowthService;
    private final MiningRunService miningRunService;
    private final MiningEvaluationService evaluationService;
    private final AssociationRuleRepository ruleRepository;

    public ApiMiningController(MimicImportService importService,
                               TransactionService transactionService,
                               AprioriMiningService aprioriService,
                               FPGrowthMiningService fpGrowthService,
                               MiningRunService miningRunService,
                               MiningEvaluationService evaluationService,
                               AssociationRuleRepository ruleRepository) {
        this.importService = importService;
        this.transactionService = transactionService;
        this.aprioriService = aprioriService;
        this.fpGrowthService = fpGrowthService;
        this.miningRunService = miningRunService;
        this.evaluationService = evaluationService;
        this.ruleRepository = ruleRepository;
    }

    /**
     * POST /api/dataset/import
     * Nhập file CSV MIMIC-III hoặc nạp bộ demo
     */
    @PostMapping("/dataset/import")
    public ResponseEntity<PreprocessSummaryDTO> importDataset(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "useDemo", defaultValue = "false") boolean useDemo) {

        PreprocessSummaryDTO summary;
        if (useDemo || file == null || file.isEmpty()) {
            summary = importService.importDemoDataset();
        } else {
            summary = importService.importUploadedCsv(file);
        }
        return ResponseEntity.ok(summary);
    }

    /**
     * POST /api/mining/preprocess
     * Chạy lại tiền xử lý trên tập demo
     */
    @PostMapping("/mining/preprocess")
    public ResponseEntity<PreprocessSummaryDTO> preprocess() {
        PreprocessSummaryDTO summary = importService.importDemoDataset();
        return ResponseEntity.ok(summary);
    }

    /**
     * POST /api/mining/apriori
     * Chạy thuật toán Apriori
     */
    @PostMapping("/mining/apriori")
    public ResponseEntity<MiningResult> runApriori(@RequestBody(required = false) MiningParametersDTO dto) {
        MiningParametersDTO paramsDto = (dto != null) ? dto : new MiningParametersDTO();
        MiningParameters params = MiningParameters.builder()
                .minSupport(paramsDto.getMinSupport())
                .minConfidence(paramsDto.getMinConfidence())
                .minLift(paramsDto.getMinLift())
                .maxItemsetSize(paramsDto.getMaxItemsetSize())
                .build();

        List<Set<String>> transactions = transactionService.getTransactions();
        MiningResult result = aprioriService.mine(transactions, params);

        // Lưu vào CSDL
        miningRunService.saveMiningRun(result, params, transactionService.getCurrentDatasetName());

        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/mining/fpgrowth
     * Chạy thuật toán FP-Growth
     */
    @PostMapping("/mining/fpgrowth")
    public ResponseEntity<MiningResult> runFpGrowth(@RequestBody(required = false) MiningParametersDTO dto) {
        MiningParametersDTO paramsDto = (dto != null) ? dto : new MiningParametersDTO();
        MiningParameters params = MiningParameters.builder()
                .minSupport(paramsDto.getMinSupport())
                .minConfidence(paramsDto.getMinConfidence())
                .minLift(paramsDto.getMinLift())
                .maxItemsetSize(paramsDto.getMaxItemsetSize())
                .build();

        List<Set<String>> transactions = transactionService.getTransactions();
        MiningResult result = fpGrowthService.mine(transactions, params);

        // Lưu vào CSDL
        miningRunService.saveMiningRun(result, params, transactionService.getCurrentDatasetName());

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/mining/runs
     * Lấy danh sách lịch sử các lần chạy khai phá
     */
    @GetMapping("/mining/runs")
    public ResponseEntity<List<MiningRun>> getRuns() {
        return ResponseEntity.ok(miningRunService.getAllRuns());
    }

    /**
     * GET /api/mining/runs/{id}/rules
     * Lấy danh sách luật kết hợp của một lần chạy
     */
    @GetMapping("/mining/runs/{id}/rules")
    public ResponseEntity<List<RuleDTO>> getRunRules(@PathVariable("id") Long runId) {
        List<AssociationRule> rules = ruleRepository.findByMiningRunIdOrderByConfidenceDesc(runId);
        List<RuleDTO> dtos = new ArrayList<>();
        for (AssociationRule r : rules) {
            dtos.add(RuleDTO.builder()
                    .id(r.getId())
                    .miningRunId(runId)
                    .antecedent(r.getAntecedent())
                    .consequent(r.getConsequent())
                    .support(r.getSupport())
                    .confidence(r.getConfidence())
                    .lift(r.getLift())
                    .antecedentSize(r.getAntecedentSize())
                    .consequentSize(r.getConsequentSize())
                    .algorithm(r.getMiningRun().getAlgorithm())
                    .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : "")
                    .build());
        }
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/mining/compare
     * So sánh thực nghiệm Apriori vs FP-Growth
     */
    @GetMapping("/mining/compare")
    public ResponseEntity<BenchmarkComparisonDTO> compareAlgorithms(
            @RequestParam(value = "minSupport", defaultValue = "0.10") double minSupport,
            @RequestParam(value = "minConfidence", defaultValue = "0.50") double minConfidence,
            @RequestParam(value = "minLift", defaultValue = "1.0") double minLift) {

        MiningParameters params = MiningParameters.builder()
                .minSupport(minSupport)
                .minConfidence(minConfidence)
                .minLift(minLift)
                .build();

        List<Set<String>> transactions = transactionService.getTransactions();
        BenchmarkComparisonDTO comparison = evaluationService.runBenchmark(
                transactions, params, transactionService.getCurrentDatasetName()
        );

        return ResponseEntity.ok(comparison);
    }
}
