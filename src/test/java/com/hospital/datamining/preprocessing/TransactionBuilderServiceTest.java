package com.hospital.datamining.preprocessing;

import com.hospital.datamining.service.preprocessing.DrugNormalizationService;
import com.hospital.datamining.service.preprocessing.TransactionBuilderService;
import com.hospital.datamining.service.preprocessing.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử TransactionBuilderService (Gom HADM_ID -> Transaction)")
class TransactionBuilderServiceTest {

    private TransactionBuilderService builderService;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        DrugNormalizationService normalizationService = new DrugNormalizationService();
        transactionService = new TransactionService();
        builderService = new TransactionBuilderService(normalizationService, transactionService);
    }

    @Test
    @DisplayName("Gom thuốc theo HADM_ID, loại bỏ trùng thuốc trong cùng 1 HADM và loại transaction < 2 thuốc")
    void testBuildTransactionsFromRawHadmMap() {
        Map<String, List<String>> rawData = new LinkedHashMap<>();

        // HADM 10001: Aspirin 81mg, aspirin, Metoprolol, Heparin -> {Aspirin, Metoprolol, Heparin} (size 3 >= 2)
        rawData.put("10001", Arrays.asList("Aspirin 81mg", "aspirin", "Metoprolol 25mg", "Heparin"));

        // HADM 10002: Pantoprazole, Atorvastatin -> {Pantoprazole, Atorvastatin} (size 2 >= 2)
        rawData.put("10002", Arrays.asList("Pantoprazole 40mg", "Atorvastatin"));

        // HADM 10003: Chỉ có 1 loại thuốc (Aspirin) -> Phải bị LOẠI BỎ (size < 2)
        rawData.put("10003", Collections.singletonList("Aspirin 81mg"));

        // HADM 10004: Danh sách rỗng -> Bỏ qua
        rawData.put("10004", Collections.emptyList());

        List<Set<String>> transactions = builderService.buildTransactions(rawData, 2, "test_dataset.csv");

        assertNotNull(transactions);
        assertEquals(2, transactions.size(), "Chỉ có 2 transaction hợp lệ (HADM 10001 và 10002) đạt >= 2 loại thuốc");

        // Kiểm tra HADM 10001 không bị trùng lặp Aspirin
        Set<String> t1 = transactions.get(0);
        assertEquals(3, t1.size());
        assertTrue(t1.contains("Aspirin"));
        assertTrue(t1.contains("Metoprolol"));
        assertTrue(t1.contains("Heparin"));

        // Kiểm tra TransactionService đã nhận dữ liệu
        assertEquals(2, transactionService.getTransactionCount());
        assertTrue(transactionService.getUniqueDrugs().contains("Pantoprazole"));
    }

    @Test
    @DisplayName("Gom thuốc theo encounter_id trong UCI Diabetes, loại transaction < 2 thuốc theo Mục 9")
    void testBuildTransactionsFromEncounters() {
        Map<String, List<String>> encounterMap = new LinkedHashMap<>();
        // Encounter 1001: metformin, insulin, glipizide -> 3 thuốc -> GIỮ
        encounterMap.put("1001", Arrays.asList("metformin", "insulin", "glipizide"));
        // Encounter 1002: insulin, glyburide -> 2 thuốc -> GIỮ
        encounterMap.put("1002", Arrays.asList("insulin", "glyburide"));
        // Encounter 1003: metformin -> 1 thuốc -> LOẠI VÌ < 2 THUỐC
        encounterMap.put("1003", Collections.singletonList("metformin"));
        // Encounter 1004: 0 thuốc -> LOẠI
        encounterMap.put("1004", Collections.emptyList());

        List<Set<String>> transactions = builderService.buildTransactionsFromEncounters(encounterMap, 2);

        assertNotNull(transactions);
        assertEquals(2, transactions.size(), "Chỉ giữ 2 transactions có >= 2 thuốc");
        assertTrue(transactions.get(0).contains("Metformin"));
        assertTrue(transactions.get(0).contains("Insulin"));
        assertTrue(transactions.get(0).contains("Glipizide"));
    }
}
