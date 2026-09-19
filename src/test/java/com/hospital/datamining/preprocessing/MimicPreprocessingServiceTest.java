package com.hospital.datamining.preprocessing;

import com.hospital.datamining.dto.PreprocessSummaryDTO;
import com.hospital.datamining.repository.DatasetImportRepository;
import com.hospital.datamining.repository.DatasetStatisticsRepository;
import com.hospital.datamining.service.preprocessing.DrugNormalizationService;
import com.hospital.datamining.service.preprocessing.MimicPreprocessingService;
import com.hospital.datamining.service.preprocessing.TransactionBuilderService;
import com.hospital.datamining.service.preprocessing.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Streaming Preprocessing & Data Understanding (MimicPreprocessingService)")
class MimicPreprocessingServiceTest {

    private MimicPreprocessingService preprocessingService;
    private TransactionService transactionService;
    private DatasetImportRepository datasetImportRepository;
    private DatasetStatisticsRepository datasetStatisticsRepository;
    private DrugNormalizationService drugNormalizationService;
    private TransactionBuilderService transactionBuilderService;

    @BeforeEach
    void setUp() {
        drugNormalizationService = new DrugNormalizationService();
        transactionService = new TransactionService();
        transactionBuilderService = new TransactionBuilderService(drugNormalizationService, transactionService);
        datasetImportRepository = Mockito.mock(DatasetImportRepository.class);
        datasetStatisticsRepository = Mockito.mock(DatasetStatisticsRepository.class);

        preprocessingService = new MimicPreprocessingService(
                drugNormalizationService,
                transactionBuilderService,
                datasetImportRepository,
                datasetStatisticsRepository
        );
    }

    @Test
    @DisplayName("Kiểm tra chuẩn hóa tên thuốc qua DrugNormalizationService")
    void testNormalizeDrugName() {
        assertEquals("Aspirin", preprocessingService.normalizeDrugName("aspirin"));
        assertEquals("Aspirin", preprocessingService.normalizeDrugName("Aspirin 81mg"));
        assertEquals("Metoprolol", preprocessingService.normalizeDrugName("METOPROLOL TARTRATE 25MG"));
        assertEquals("Heparin", preprocessingService.normalizeDrugName("  \"Heparin Sodium 5000 IU\"  "));
        assertEquals("", preprocessingService.normalizeDrugName(""));
        assertEquals("", preprocessingService.normalizeDrugName(null));
    }

    @Test
    @DisplayName("Kiểm tra luồng tiền xử lý CSV: Lọc thiếu HADM_ID, trùng thuốc và giao dịch < 2 thuốc")
    void testProcessPrescriptionsCsv() {
        String csvContent = "ROW_ID,SUBJECT_ID,HADM_ID,DRUG_NAME_GENERIC,DRUG\n" +
                "1,101,1001,Aspirin,Aspirin\n" +
                "2,101,1001,Metoprolol,Metoprolol\n" +
                "3,101,1001,Aspirin,Aspilets 81mg\n" + // Trùng Aspirin trong HADM_ID 1001 -> Tự deduplicate!
                "4,102,,Heparin,Heparin\n" +             // Thiếu HADM_ID -> Bị loại!
                "5,103,1002,,Pantoprazole 40mg\n" +     // DRUG_NAME_GENERIC thiếu -> Fallback sang DRUG!
                "6,103,1002,Aspirin,Aspirin\n" +
                "7,104,1003,Atorvastatin,Lipitor\n";    // Chỉ có 1 thuốc -> Giao dịch kích thước < 2 -> Bị loại!

        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        PreprocessSummaryDTO summary = preprocessingService.processPrescriptionsCsv(inputStream, "test.csv", csvContent.length());

        assertEquals("SUCCESS", summary.getStatus());
        assertEquals(7, summary.getInitialRecords());
        // HADM_ID 1001 có 2 thuốc duy nhất (Aspirin, Metoprolol) -> Giữ lại (size >= 2)
        // HADM_ID 1002 có 2 thuốc duy nhất (Pantoprazole, Aspirin) -> Giữ lại (size >= 2)
        // HADM_ID 1003 có 1 thuốc (Atorvastatin) -> Bị loại (< 2)
        assertEquals(2, summary.getFinalTransactionsCount(), "Chỉ có 2 transaction thỏa mãn >= 2 thuốc");
        assertEquals(2, transactionService.getTransactionCount());
        assertEquals(1, summary.getMissingValuesCount(), "1 bản ghi thiếu HADM_ID");
    }
}
