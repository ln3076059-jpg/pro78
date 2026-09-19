package com.hospital.datamining.service.preprocessing;

import com.hospital.datamining.dto.PreprocessSummaryDTO;
import com.hospital.datamining.entity.DatasetImport;
import com.hospital.datamining.entity.DatasetStatistics;
import com.hospital.datamining.repository.DatasetImportRepository;
import com.hospital.datamining.repository.DatasetStatisticsRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service tiền xử lý tệp dữ liệu y tế UCI Diabetes 130-US Hospitals.
 * Triển khai đầy đủ các bước trong CRISP-DM:
 * - Streaming parser (không nạp toàn bộ CSV vào RAM)
 * - Validation & Cleaning: lọc record thiếu encounter_id hoặc sai định dạng
 * - Medicine Extraction: Quy ước No = không dùng; Steady, Up, Down = có sử dụng trong encounter
 * - Transaction Building: Mỗi encounter_id là 1 transaction, lọc >= 2 thuốc
 * - Data Understanding: Thống kê số encounter, số bệnh nhân, tỷ lệ tuổi, top chẩn đoán,
 *   số thuốc TB/min/max, số transaction 0 thuốc, 1 thuốc, >= 2 thuốc.
 */
@Service
public class DataPreprocessingService {

    private static final Logger log = LoggerFactory.getLogger(DataPreprocessingService.class);

    // 24 cột thuốc chuẩn từ bộ dữ liệu UCI Diabetes 130-US Hospitals
    public static final List<String> UCI_DRUG_COLUMNS = List.of(
            "metformin", "repaglinide", "nateglinide", "chlorpropamide", "glimepiride",
            "acetohexamide", "glipizide", "glyburide", "tolbutamide", "pioglitazone",
            "rosiglitazone", "acarbose", "miglitol", "troglitazone", "tolazamide",
            "examide", "citoglipton", "insulin", "glyburide-metformin", "glipizide-metformin",
            "glimepiride-pioglitazone", "metformin-rosiglitazone", "metformin-pioglitazone"
    );

    private final TransactionBuilderService transactionBuilderService;
    private final DatasetImportRepository datasetImportRepository;
    private final DatasetStatisticsRepository datasetStatisticsRepository;

    @Autowired
    public DataPreprocessingService(TransactionBuilderService transactionBuilderService,
                                   @Autowired(required = false) DatasetImportRepository datasetImportRepository,
                                   @Autowired(required = false) DatasetStatisticsRepository datasetStatisticsRepository) {
        this.transactionBuilderService = transactionBuilderService;
        this.datasetImportRepository = datasetImportRepository;
        this.datasetStatisticsRepository = datasetStatisticsRepository;
    }

    /**
     * Tiền xử lý dữ liệu theo luồng streaming CSV
     */
    @Transactional
    public PreprocessSummaryDTO processCsv(InputStream inputStream, String fileName, long fileSizeBytes) {
        log.info("Bắt đầu tiền xử lý streaming tệp dữ liệu: {} (kích thước {} bytes)", fileName, fileSizeBytes);

        int totalRecords = 0;
        int validRecords = 0;
        int invalidRecords = 0;

        Set<String> uniquePatients = new HashSet<>();
        Map<String, Set<String>> encounterToDrugs = new LinkedHashMap<>();
        Map<String, String> encounterToPatient = new HashMap<>();
        Map<String, Integer> drugGlobalFrequencies = new HashMap<>();

        // Thống kê Data Understanding
        Map<String, Integer> ageDistribution = new TreeMap<>();
        Map<String, Integer> diagnosisDistribution = new HashMap<>();
        Map<Integer, Integer> medicationCountDistribution = new TreeMap<>();

        int zeroDrugCount = 0;
        int oneDrugCount = 0;
        int multiDrugCount = 0;

        int minDrugs = Integer.MAX_VALUE;
        int maxDrugs = 0;
        long totalDrugsSum = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build();

            try (CSVParser parser = new CSVParser(reader, format)) {
                Map<String, Integer> headerMap = parser.getHeaderMap();

                // Kiểm tra xem đây có phải định dạng UCI Diabetes không
                boolean isUciFormat = headerMap.containsKey("encounter_id") ||
                                      headerMap.containsKey("ENCOUNTER_ID") ||
                                      findMatchingHeader(headerMap, "metformin") != null;

                if (isUciFormat) {
                    String encCol = findMatchingHeader(headerMap, "encounter_id");
                    String patCol = findMatchingHeader(headerMap, "patient_nbr");
                    String ageCol = findMatchingHeader(headerMap, "age");
                    String diag1Col = findMatchingHeader(headerMap, "diag_1");
                    String diag2Col = findMatchingHeader(headerMap, "diag_2");
                    String diag3Col = findMatchingHeader(headerMap, "diag_3");

                    // Map các cột thuốc có mặt trong CSV
                    Map<String, String> presentDrugCols = new HashMap<>();
                    for (String drug : UCI_DRUG_COLUMNS) {
                        String matched = findMatchingHeader(headerMap, drug);
                        if (matched != null) {
                            presentDrugCols.put(drug, matched);
                        }
                    }

                    for (CSVRecord record : parser) {
                        totalRecords++;

                        if (encCol == null || !record.isSet(encCol)) {
                            invalidRecords++;
                            continue;
                        }

                        String encId = record.get(encCol).trim();
                        if (encId.isEmpty() || "?".equals(encId) || "NA".equalsIgnoreCase(encId)) {
                            invalidRecords++;
                            continue;
                        }

                        validRecords++;

                        // Bệnh nhân
                        if (patCol != null && record.isSet(patCol)) {
                            String pat = record.get(patCol).trim();
                            if (!pat.isEmpty() && !"?".equals(pat)) {
                                uniquePatients.add(pat);
                                encounterToPatient.put(encId, pat);
                            }
                        }

                        // Tuổi
                        if (ageCol != null && record.isSet(ageCol)) {
                            String age = record.get(ageCol).trim();
                            if (!age.isEmpty() && !"?".equals(age)) {
                                ageDistribution.put(age, ageDistribution.getOrDefault(age, 0) + 1);
                            }
                        }

                        // Chẩn đoán (ICD-9)
                        extractDiagnosis(record, diag1Col, diagnosisDistribution);
                        extractDiagnosis(record, diag2Col, diagnosisDistribution);
                        extractDiagnosis(record, diag3Col, diagnosisDistribution);

                        // Trích xuất thuốc: No = không dùng; Steady, Up, Down = có dùng
                        Set<String> activeDrugs = new LinkedHashSet<>();
                        for (Map.Entry<String, String> entry : presentDrugCols.entrySet()) {
                            String rawDrugName = entry.getKey();
                            String colName = entry.getValue();
                            if (record.isSet(colName)) {
                                String val = record.get(colName).trim();
                                if (isDrugActive(val)) {
                                    String canonicalDrug = formatDrugName(rawDrugName);
                                    activeDrugs.add(canonicalDrug);
                                    drugGlobalFrequencies.put(canonicalDrug, drugGlobalFrequencies.getOrDefault(canonicalDrug, 0) + 1);
                                }
                            }
                        }

                        encounterToDrugs.put(encId, activeDrugs);

                        int drugCount = activeDrugs.size();
                        medicationCountDistribution.put(drugCount, medicationCountDistribution.getOrDefault(drugCount, 0) + 1);

                        if (drugCount == 0) {
                            zeroDrugCount++;
                        } else if (drugCount == 1) {
                            oneDrugCount++;
                        } else {
                            multiDrugCount++;
                        }

                        if (drugCount < minDrugs) minDrugs = drugCount;
                        if (drugCount > maxDrugs) maxDrugs = drugCount;
                        totalDrugsSum += drugCount;
                    }
                } else {
                    // Fallback cho định dạng bảng thuốc có HADM_ID / DRUG (tương thích tệp kê đơn tùy chọn)
                    String hadmCol = findMatchingHeader(headerMap, "HADM_ID");
                    String drugCol = findMatchingHeader(headerMap, "DRUG");
                    String subjCol = findMatchingHeader(headerMap, "SUBJECT_ID");

                    for (CSVRecord record : parser) {
                        totalRecords++;
                        if (hadmCol == null || !record.isSet(hadmCol) || drugCol == null || !record.isSet(drugCol)) {
                            invalidRecords++;
                            continue;
                        }
                        String hadm = record.get(hadmCol).trim();
                        String drug = record.get(drugCol).trim();
                        if (hadm.isEmpty() || drug.isEmpty()) {
                            invalidRecords++;
                            continue;
                        }
                        validRecords++;
                        if (subjCol != null && record.isSet(subjCol)) {
                            uniquePatients.add(record.get(subjCol).trim());
                            encounterToPatient.put(hadm, record.get(subjCol).trim());
                        }
                        encounterToDrugs.computeIfAbsent(hadm, k -> new LinkedHashSet<>()).add(drug);
                        drugGlobalFrequencies.put(drug, drugGlobalFrequencies.getOrDefault(drug, 0) + 1);
                    }

                    for (Set<String> drugs : encounterToDrugs.values()) {
                        int count = drugs.size();
                        if (count == 0) zeroDrugCount++;
                        else if (count == 1) oneDrugCount++;
                        else multiDrugCount++;
                        if (count < minDrugs) minDrugs = count;
                        if (count > maxDrugs) maxDrugs = count;
                        totalDrugsSum += count;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi phân tích streaming tệp CSV: {}", e.getMessage(), e);
            return PreprocessSummaryDTO.builder()
                    .fileName(fileName)
                    .status("ERROR")
                    .message("Lỗi khi đọc và tiền xử lý CSV: " + e.getMessage())
                    .build();
        }

        if (minDrugs == Integer.MAX_VALUE) {
            minDrugs = 0;
        }
        double avgDrugs = validRecords > 0 ? (double) totalDrugsSum / Math.max(1, encounterToDrugs.size()) : 0.0;
        avgDrugs = Math.round(avgDrugs * 100.0) / 100.0;

        DatasetImport datasetImport = DatasetImport.builder()
                .fileName(fileName)
                .source("UCI Diabetes 130-US Hospitals")
                .fileType("CSV")
                .fileSizeBytes(fileSizeBytes)
                .totalRecords(totalRecords)
                .validRecords(validRecords)
                .invalidRecords(invalidRecords)
                .uniqueHadmCount(encounterToDrugs.size())
                .uniqueDrugCount(drugGlobalFrequencies.size())
                .finalTransactionsCount(multiDrugCount)
                .status("COMPLETED")
                .build();

        // Lưu DatasetImport nếu có repository
        if (datasetImportRepository != null) {
            try {
                DatasetImport saved = datasetImportRepository.save(datasetImport);
                if (saved != null) {
                    datasetImport = saved;
                }
            } catch (Exception e) {
                log.warn("Không thể lưu dataset import vào DB: {}", e.getMessage());
            }
        }

        Long importId = (datasetImport != null) ? datasetImport.getId() : null;

        // Xây dựng transaction và lưu vào CSDL MySQL (chỉ lấy encounter có >= 2 thuốc cho Data Mining)
        List<Set<String>> finalTransactions = transactionBuilderService.buildTransactionsAndPersist(
                encounterToDrugs, encounterToPatient, 2, fileName, importId);

        // Chuẩn bị các chuỗi JSON thống kê
        String topDrugsJson = buildTopDrugsJson(drugGlobalFrequencies, 10);
        String ageDistJson = mapToJson(ageDistribution);
        String diagDistJson = buildTopDiagnosisJson(diagnosisDistribution, 10);
        String medCountDistJson = mapToJson(medicationCountDistribution);

        // Lưu DatasetStatistics nếu có repository
        if (datasetStatisticsRepository != null) {
            try {
                DatasetStatistics statistics = DatasetStatistics.builder()
                        .datasetImport(datasetImport)
                        .datasetName(fileName)
                        .totalRecords(totalRecords)
                        .validRecords(validRecords)
                        .missingHadmCount(invalidRecords)
                        .uniqueSubjectCount(uniquePatients.size())
                        .uniqueHadmCount(encounterToDrugs.size())
                        .uniqueDrugCount(drugGlobalFrequencies.size())
                        .zeroDrugEncounterCount(zeroDrugCount)
                        .oneDrugEncounterCount(oneDrugCount)
                        .multiDrugTransactionCount(multiDrugCount)
                        .uniqueDiagnosisCount(diagnosisDistribution.size())
                        .avgDrugsPerHadm(avgDrugs)
                        .minDrugsPerHadm(minDrugs)
                        .maxDrugsPerHadm(maxDrugs)
                        .finalTransactionCount(finalTransactions.size())
                        .topDrugsJson(topDrugsJson)
                        .ageDistributionJson(ageDistJson)
                        .diagnosisDistributionJson(diagDistJson)
                        .medicationCountDistributionJson(medCountDistJson)
                        .build();
                datasetStatisticsRepository.save(statistics);
            } catch (Exception e) {
                log.warn("Không thể lưu dataset statistics vào DB: {}", e.getMessage());
            }
        }

        log.info("Tiền xử lý hoàn tất! Tổng: {}, Hợp lệ: {}, Bệnh nhân: {}, Encounters: {}, Thuốc: {}, Transactions (>=2 thuốc): {}",
                totalRecords, validRecords, uniquePatients.size(), encounterToDrugs.size(), drugGlobalFrequencies.size(), finalTransactions.size());

        return PreprocessSummaryDTO.builder()
                .fileName(fileName)
                .fileSizeBytes(fileSizeBytes)
                .initialRecords(totalRecords)
                .validRecords(validRecords)
                .missingValuesCount(invalidRecords)
                .uniqueSubjectCount(uniquePatients.size())
                .uniqueHadmCount(encounterToDrugs.size())
                .uniqueDrugCount(drugGlobalFrequencies.size())
                .zeroDrugEncounterCount(zeroDrugCount)
                .oneDrugEncounterCount(oneDrugCount)
                .multiDrugTransactionCount(multiDrugCount)
                .avgDrugsPerHadm(avgDrugs)
                .minDrugsPerHadm(minDrugs)
                .maxDrugsPerHadm(maxDrugs)
                .finalTransactionsCount(finalTransactions.size())
                .topDrugsJson(topDrugsJson)
                .ageDistributionJson(ageDistJson)
                .diagnosisDistributionJson(diagDistJson)
                .medicationCountDistributionJson(medCountDistJson)
                .status("SUCCESS")
                .message("Tiền xử lý thành công tệp " + fileName)
                .build();
    }

    /**
     * Quy ước xác định thuốc có trong transaction (Mục 7 đề tài):
     * No -> không sử dụng (false)
     * Steady, Up, Down -> có sử dụng (true)
     */
    public static boolean isDrugActive(String value) {
        if (value == null) return false;
        String v = value.trim().toLowerCase();
        return "steady".equals(v) || "up".equals(v) || "down".equals(v);
    }

    /**
     * Chuẩn hóa tên thuốc hiển thị
     */
    public static String formatDrugName(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        if (raw.contains("-")) {
            String[] parts = raw.split("-");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) sb.append("-");
                sb.append(capitalize(parts[i]));
            }
            return sb.toString();
        }
        return capitalize(raw);
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }

    private void extractDiagnosis(CSVRecord record, String colName, Map<String, Integer> dist) {
        if (colName != null && record.isSet(colName)) {
            String diag = record.get(colName).trim();
            if (!diag.isEmpty() && !"?".equals(diag)) {
                dist.put(diag, dist.getOrDefault(diag, 0) + 1);
            }
        }
    }

    private String findMatchingHeader(Map<String, Integer> headerMap, String target) {
        for (String key : headerMap.keySet()) {
            if (key.equalsIgnoreCase(target)) {
                return key;
            }
        }
        return null;
    }

    private String buildTopDrugsJson(Map<String, Integer> drugFrequencies, int limit) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(drugFrequencies.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        List<Map<String, Object>> topList = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, Integer> entry : list) {
            if (count++ >= limit) break;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", entry.getKey());
            map.put("count", entry.getValue());
            topList.add(map);
        }
        return toJson(topList);
    }

    private String buildTopDiagnosisJson(Map<String, Integer> diagnosisMap, int limit) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(diagnosisMap.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        List<Map<String, Object>> topList = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, Integer> entry : list) {
            if (count++ >= limit) break;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("code", entry.getKey());
            map.put("count", entry.getValue());
            topList.add(map);
        }
        return toJson(topList);
    }

    private <K, V> String mapToJson(Map<K, V> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<K, V> e : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":").append(e.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String toJson(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            Map<String, Object> m = list.get(i);
            sb.append("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : m.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":");
                if (entry.getValue() instanceof Number) {
                    sb.append(entry.getValue());
                } else {
                    sb.append("\"").append(entry.getValue()).append("\"");
                }
                first = false;
            }
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}
