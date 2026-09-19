package com.hospital.datamining.service.importer;

import com.hospital.datamining.dto.PreprocessSummaryDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Lớp bọc tương thích ngược (deprecated) chuyển tiếp tới DatasetImportService
 */
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
