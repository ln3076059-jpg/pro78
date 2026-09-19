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

        double[] supports = new double[]{0.01, 0.02, 0.05, 0.10};

        System.out.println("=== UCI DIABETES BENCHMARK EXPERIMENT RESULTS ===");
        System.out.println(String.format("%-10s | %-12s | %-8s | %-10s | %-10s | %-10s | %-17s | %-8s",
                "Algorithm", "Transactions", "Support", "Confidence", "Runtime(ms)", "Memory(MB)", "Frequent Itemsets", "Rules"));
        System.out.println("-".repeat(95));

        for (double s : supports) {
            MiningParameters params = MiningParameters.builder()
                    .minSupport(s)
                    .minConfidence(0.30)
                    .minLift(1.0)
                    .maxItemsetSize(5)
                    .build();

            MiningResult apResult = apriori.mine(transactions, params);
            MiningResult fpResult = fpGrowth.mine(transactions, params);

            System.out.println(String.format("%-10s | %-12d | %-8.2f | %-10.2f | %-10d | %-10.2f | %-17d | %-8d",
                    "Apriori", transactions.size(), s, 0.30, apResult.getRuntimeMs(), apResult.getMemoryUsageMb(),
                    apResult.getFrequentItemsetCount(), apResult.getRuleCount()));

            System.out.println(String.format("%-10s | %-12d | %-8.2f | %-10.2f | %-10d | %-10.2f | %-17d | %-8d",
                    "FP-Growth", transactions.size(), s, 0.30, fpResult.getRuntimeMs(), fpResult.getMemoryUsageMb(),
                    fpResult.getFrequentItemsetCount(), fpResult.getRuleCount()));

            if (s == 0.02 || (s == 0.01 && fpResult.getAssociationRules().size() > 0)) {
                System.out.println(String.format("--- TOP RULES AT minSupport=%.2f, minConfidence=0.30 ---", s));
                for (int i = 0; i < Math.min(6, fpResult.getAssociationRules().size()); i++) {
                    var r = fpResult.getAssociationRules().get(i);
                    System.out.println(String.format("  {%s} => {%s} (Support: %.4f, Conf: %.4f, Lift: %.4f)",
                            r.getAntecedentAsString(), r.getConsequentAsString(), r.getSupport(), r.getConfidence(), r.getLift()));
                }
            }
        }
    }
}
