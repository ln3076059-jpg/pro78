package com.hospital.datamining.preprocessing;

import com.hospital.datamining.dto.PreprocessSummaryDTO;
import com.hospital.datamining.service.importer.DatasetImportService;
import com.hospital.datamining.service.preprocessing.DataPreprocessingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử DatasetImportService (Đọc và nạp bộ dữ liệu y tế)")
class DatasetImportServiceTest {

    @Test
    @DisplayName("Kiểm tra nạp tệp mẫu rút gọn diabetic_data_sample.csv")
    void testImportDemoDataset() {
        DataPreprocessingService preprocessingService = Mockito.mock(DataPreprocessingService.class);
        Mockito.when(preprocessingService.processCsv(Mockito.any(), Mockito.anyString(), Mockito.anyLong()))
                .thenReturn(PreprocessSummaryDTO.builder()
                        .fileName("diabetic_data_sample.csv")
                        .status("SUCCESS")
                        .initialRecords(2000)
                        .validRecords(2000)
                        .finalTransactionsCount(612)
                        .build());

        DatasetImportService importService = new DatasetImportService(preprocessingService);

        PreprocessSummaryDTO summary = importService.importDemoDataset();

        assertNotNull(summary);
        assertEquals("diabetic_data_sample.csv", summary.getFileName());
    }
}
