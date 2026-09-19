package com.hospital.datamining.mining;

import com.hospital.datamining.dto.BenchmarkComparisonDTO;
import com.hospital.datamining.dto.PreprocessSummaryDTO;
import com.hospital.datamining.repository.DatasetImportRepository;
import com.hospital.datamining.repository.DatasetStatisticsRepository;
import com.hospital.datamining.service.mining.AprioriMiningService;
import com.hospital.datamining.service.mining.FPGrowthMiningService;
import com.hospital.datamining.service.mining.MiningEvaluationService;
import com.hospital.datamining.service.mining.model.MiningParameters;
import com.hospital.datamining.service.mining.model.MiningResult;
import com.hospital.datamining.service.preprocessing.DrugNormalizationService;
import com.hospital.datamining.service.preprocessing.MimicPreprocessingService;
import com.hospital.datamining.service.preprocessing.TransactionBuilderService;
import com.hospital.datamining.service.preprocessing.TransactionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Set;

public class BenchmarkReportGeneratorTest {

    @Test
    public void generateRealExperimentData() throws Exception {
        File file = new File("dataset/diabetic_data.csv");
        if (!file.exists()) {
            file = new File("dataset/diabetic_data_sample.csv");
        }
        if (!file.exists()) {
            System.out.println("UCI dataset file not found: " + file.getAbsolutePath());
            return;
        }

        DrugNormalizationService drugNorm = new DrugNormalizationService();
        TransactionService txService = new TransactionService();
        TransactionBuilderService txBuilder = new TransactionBuilderService(drugNorm, txService);
        DatasetImportRepository importRepo = Mockito.mock(DatasetImportRepository.class);
        DatasetStatisticsRepository statsRepo = Mockito.mock(DatasetStatisticsRepository.class);

        com.hospital.datamining.service.preprocessing.DataPreprocessingService prepService =
                new com.hospital.datamining.service.preprocessing.DataPreprocessingService(
                        txBuilder, importRepo, statsRepo
                );

        try (FileInputStream fis = new FileInputStream(file)) {
            PreprocessSummaryDTO summary = prepService.processCsv(fis, file.getName(), file.length());
            System.out.println("=== UCI DIABETES PREPROCESSING SUMMARY ===");
            System.out.println("File: " + summary.getFileName());
            System.out.println("Total records: " + summary.getInitialRecords());
            System.out.println("Valid records: " + summary.getValidRecords());
            System.out.println("Unique Encounters: " + summary.getUniqueHadmCount());
            System.out.println("Unique Drugs: " + summary.getUniqueDrugCount());
            System.out.println("Transactions (>=2 drugs): " + summary.getFinalTransactionsCount());
            System.out.println("Avg drugs/Encounter: " + summary.getAvgDrugsPerHadm());
            System.out.println("Min drugs/Encounter: " + summary.getMinDrugsPerHadm());
            System.out.println("Max drugs/Encounter: " + summary.getMaxDrugsPerHadm());
        }

        List<Set<String>> transactions = txService.getTransactions();

        AprioriMiningService apriori = new AprioriMiningService();
        FPGrowthMiningService fpGrowth = new FPGrowthMiningService();
        com.hospital.datamining.repository.AlgorithmBenchmarkRepository benchRepo =
                Mockito.mock(com.hospital.datamining.repository.AlgorithmBenchmarkRepository.class);
        MiningEvaluationService evalService = new MiningEvaluationService(apriori, fpGrowth, benchRepo);

        double[] supports = new double[]{0.01, 0.02, 0.05, 0.10};

        System.out.println("\n=== UCI DIABETES BENCHMARK EXPERIMENT RESULTS (2 WARM-UPS + 5 ITERATIONS MEDIAN) ===");
        System.out.println(String.format("%-10s | %-12s | %-8s | %-10s | %-15s | %-12s | %-17s | %-8s | %-15s | %-15s",
                "Algorithm", "Transactions", "Support", "Confidence", "Median Time(ms)", "Approx RAM", "Frequent Itemsets", "Rules", "Rule Jaccard", "Itemset Jaccard"));
        System.out.println("-".repeat(130));

        for (double s : supports) {
            MiningParameters params = MiningParameters.builder()
                    .minSupport(s)
                    .minConfidence(0.30)
                    .minLift(1.0)
                    .maxItemsetSize(5)
                    .build();

            BenchmarkComparisonDTO bench = evalService.runBenchmark(transactions, params, "UCI Diabetes 130-US Hospitals");

            System.out.println(String.format("%-10s | %-12d | %-8.2f | %-10.2f | %-15d | %-12s | %-17d | %-8d | %-15.1f | %-15.1f",
                    "Apriori", bench.getTransactionCount(), s, 0.30, bench.getAprioriRuntimeMs(),
                    String.format("~%.2f MB", bench.getAprioriMemoryMb()),
                    bench.getAprioriItemsetCount(), bench.getAprioriRuleCount(),
                    bench.getRuleOverlapPercentage(), bench.getItemsetOverlapPercentage()));

            System.out.println(String.format("%-10s | %-12d | %-8.2f | %-10.2f | %-15d | %-12s | %-17d | %-8d | %-15.1f | %-15.1f",
                    "FP-Growth", bench.getTransactionCount(), s, 0.30, bench.getFpgrowthRuntimeMs(),
                    String.format("~%.2f MB", bench.getFpgrowthMemoryMb()),
                    bench.getFpgrowthItemsetCount(), bench.getFpgrowthRuleCount(),
                    bench.getRuleOverlapPercentage(), bench.getItemsetOverlapPercentage()));

            System.out.println("  => Recommended: " + bench.getRecommendedAlgorithm());
            System.out.println("  => Note: " + bench.getConclusionNotes());
            System.out.println("-".repeat(130));
        }
    }
}
