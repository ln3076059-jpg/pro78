package com.hospital.datamining.service.importer;

import com.hospital.datamining.dto.PreprocessSummaryDTO;
import com.hospital.datamining.service.preprocessing.DataPreprocessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class DatasetImportService {

    private static final Logger log = LoggerFactory.getLogger(DatasetImportService.class);

    private final DataPreprocessingService preprocessingService;

    @Value("${dataset.path:dataset/diabetic_data.csv}")
    private String datasetPath;

    @Value("${dataset.sample-path:dataset/diabetic_data_sample.csv}")
    private String sampleDatasetPath;

    @Value("${dataset.upload-dir:uploads/datasets}")
    private String uploadDir;

    public DatasetImportService(DataPreprocessingService preprocessingService) {
        this.preprocessingService = preprocessingService;
    }

    /**
     * Nạp và tiền xử lý bộ dữ liệu demo rút gọn (2,000 ca điều trị từ UCI Diabetes)
     */
    public PreprocessSummaryDTO importDemoDataset() {
        File demoFile = findFile(sampleDatasetPath, "dataset/diabetic_data_sample.csv");

        if (demoFile == null || !demoFile.exists()) {
            return PreprocessSummaryDTO.builder()
                    .fileName("diabetic_data_sample.csv")
                    .status("ERROR")
                    .message("Không tìm thấy tệp mẫu diabetic_data_sample.csv")
                    .build();
        }

        try (FileInputStream fis = new FileInputStream(demoFile)) {
            return preprocessingService.processCsv(fis, demoFile.getName(), demoFile.length());
        } catch (IOException e) {
            log.error("Lỗi khi đọc file demo: {}", e.getMessage(), e);
            return PreprocessSummaryDTO.builder()
                    .fileName(demoFile.getName())
                    .status("ERROR")
                    .message("Lỗi khi nạp file demo: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Nạp và tiền xử lý toàn bộ tập dữ liệu chuẩn UCI Diabetes 130-US Hospitals (100,000+ encounters)
     */
    public PreprocessSummaryDTO importUciDiabetesDataset() {
        File dataFile = findFile(datasetPath, "dataset/diabetic_data.csv", "diabetic_data.csv");

        if (dataFile == null || !dataFile.exists()) {
            // Fallback sang sample nếu chưa có full
            return importDemoDataset();
        }

        try (FileInputStream fis = new FileInputStream(dataFile)) {
            return preprocessingService.processCsv(fis, dataFile.getName(), dataFile.length());
        } catch (IOException e) {
            log.error("Lỗi khi nạp dataset UCI Diabetes: {}", e.getMessage(), e);
            return PreprocessSummaryDTO.builder()
                    .fileName(dataFile.getName())
                    .status("ERROR")
                    .message("Lỗi khi nạp dataset UCI: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Tương thích ngược: nạp bộ dữ liệu lớn
     */
    public PreprocessSummaryDTO importLargeDemoDataset() {
        return importUciDiabetesDataset();
    }

    /**
     * Nhập và tiền xử lý file CSV do người dùng tải lên từ giao diện
     */
    public PreprocessSummaryDTO importUploadedCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return PreprocessSummaryDTO.builder()
                    .status("ERROR")
                    .message("Tệp tải lên rỗng hoặc không tồn tại.")
                    .build();
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "uploaded_dataset.csv";
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path targetLocation = uploadPath.resolve(System.currentTimeMillis() + "_" + originalFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            try (InputStream is = new BufferedInputStream(Files.newInputStream(targetLocation))) {
                return preprocessingService.processCsv(is, originalFilename, file.getSize());
            }

        } catch (IOException e) {
            log.error("Lỗi khi lưu và xử lý file tải lên: {}", e.getMessage(), e);
            return PreprocessSummaryDTO.builder()
                    .fileName(originalFilename)
                    .status("ERROR")
                    .message("Lỗi xử lý file upload: " + e.getMessage())
                    .build();
        }
    }

    private File findFile(String primaryPath, String... fallbacks) {
        if (primaryPath != null) {
            File f = new File(primaryPath);
            if (f.exists()) return f;
        }
        for (String fb : fallbacks) {
            File f = new File(fb);
            if (f.exists()) return f;
        }
        return null;
    }
}
