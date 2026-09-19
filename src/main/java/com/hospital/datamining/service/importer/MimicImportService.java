package com.hospital.datamining.service.importer;

import com.hospital.datamining.dto.PreprocessSummaryDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Lớp bọc tương thích ngược (Legacy / Optional compatibility service).
 * Bộ dữ liệu nghiên cứu chính thức của đề tài là UCI Diabetes 130-US Hospitals (DatasetImportService).
 */
@Deprecated(since = "1.0.0", forRemoval = false)
@Service
public class MimicImportService {

    private final DatasetImportService datasetImportService;

    public MimicImportService(DatasetImportService datasetImportService) {
        this.datasetImportService = datasetImportService;
    }

    public PreprocessSummaryDTO importDemoDataset() {
        return datasetImportService.importDemoDataset();
    }

    public PreprocessSummaryDTO importLargeDemoDataset() {
        return datasetImportService.importUciDiabetesDataset();
    }

    public PreprocessSummaryDTO importUploadedCsv(MultipartFile file) {
        return datasetImportService.importUploadedCsv(file);
    }
}
