package com.hospital.datamining.service.preprocessing;

import com.hospital.datamining.dto.PreprocessSummaryDTO;
import com.hospital.datamining.repository.DatasetImportRepository;
import com.hospital.datamining.repository.DatasetStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Lớp bọc tương thích ngược (Legacy / Optional compatibility service).
 * Bộ dữ liệu nghiên cứu chính thức của đề tài là UCI Diabetes 130-US Hospitals (DataPreprocessingService).
 */
@Deprecated(since = "1.0.0", forRemoval = false)
@Service
public class MimicPreprocessingService {

    private final DataPreprocessingService preprocessingService;
    private final DrugNormalizationService drugNormalizationService;

    @Autowired
    public MimicPreprocessingService(DataPreprocessingService preprocessingService) {
        this.preprocessingService = preprocessingService;
        this.drugNormalizationService = null;
    }

    public MimicPreprocessingService(DrugNormalizationService drugNormalizationService,
                                     TransactionBuilderService transactionBuilderService,
                                     DatasetImportRepository datasetImportRepository,
                                     DatasetStatisticsRepository datasetStatisticsRepository) {
        this.drugNormalizationService = drugNormalizationService;
        this.preprocessingService = new DataPreprocessingService(transactionBuilderService, datasetImportRepository, datasetStatisticsRepository);
    }

    public PreprocessSummaryDTO processPrescriptionsCsv(InputStream inputStream, String fileName, long fileSizeBytes) {
        return preprocessingService.processCsv(inputStream, fileName, fileSizeBytes);
    }

    public String normalizeDrugName(String raw) {
        if (drugNormalizationService != null) {
            return drugNormalizationService.normalize(raw);
        }
        return DataPreprocessingService.formatDrugName(raw);
    }
}
