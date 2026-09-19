package com.hospital.datamining.preprocessing;

import com.hospital.datamining.service.preprocessing.DrugNormalizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Chuẩn Hóa Tên Thuốc Y Tế (DrugNormalizationService)")
class DrugNormalizationServiceTest {

    private DrugNormalizationService normalizationService;

    @BeforeEach
    void setUp() {
        normalizationService = new DrugNormalizationService();
    }

    @Test
    @DisplayName("Chuẩn hóa các biến thể của Aspirin (Yêu cầu mục 9 đề bài)")
    void testAspirinVariationsNormalizeToSameEntity() {
        String n1 = normalizationService.normalize("Aspirin");
        String n2 = normalizationService.normalize("aspirin");
        String n3 = normalizationService.normalize("ASPIRIN");
        String n4 = normalizationService.normalize("Aspirin 81mg");
        String n5 = normalizationService.normalize("Aspirin 81 mg EC");
        String n6 = normalizationService.normalize("Aspirin 325 mg (Oral)");
        String n7 = normalizationService.normalize("ASPIRIN [Aspilets] 81MG");

        assertEquals("Aspirin", n1);
        assertEquals("Aspirin", n2);
        assertEquals("Aspirin", n3);
        assertEquals("Aspirin", n4);
        assertEquals("Aspirin", n5);
        assertEquals("Aspirin", n6);
        assertEquals("Aspirin", n7);
    }

    @Test
    @DisplayName("Chuẩn hóa các thuốc tim mạch và chống đông phổ biến")
    void testCardiovascularDrugNormalization() {
        assertEquals("Metoprolol", normalizationService.normalize("Metoprolol Tartrate 25mg"));
        assertEquals("Metoprolol", normalizationService.normalize("Metoprolol Succinate 50 mg ER"));
        assertEquals("Heparin", normalizationService.normalize("Heparin Sodium 5000 Units/ml"));
        assertEquals("Pantoprazole", normalizationService.normalize("Pantoprazole 40mg IV"));
        assertEquals("Atorvastatin", normalizationService.normalize("Atorvastatin Calcium 40mg Tablet"));
        assertEquals("Furosemide", normalizationService.normalize("Furosemide 40 mg PO"));
    }

    @Test
    @DisplayName("Ưu tiên DRUG_NAME_GENERIC khi có, fallback DRUG khi generic rỗng")
    void testGenericPreferenceOverBrandName() {
        // generic = "Aspirin", drug = "Aspilets 81mg" -> "Aspirin"
        String r1 = normalizationService.normalize("Aspirin", "Aspilets 81mg");
        assertEquals("Aspirin", r1);

        // generic = null, drug = "Lipitor 40mg" -> "Lipitor" (hoặc chuẩn hóa)
        String r2 = normalizationService.normalize(null, "Atorvastatin 40mg");
        assertEquals("Atorvastatin", r2);

        // generic = "NA", drug = "Metformin 500mg" -> "Metformin"
        String r3 = normalizationService.normalize("NA", "Metformin 500mg");
        assertEquals("Metformin", r3);
    }

    @Test
    @DisplayName("Xử lý chuỗi rỗng và chuỗi bất thường không văng ngoại lệ")
    void testEmptyAndEdgeCases() {
        assertEquals("", normalizationService.normalize(null));
        assertEquals("", normalizationService.normalize(""));
        assertEquals("", normalizationService.normalize("   "));
        assertEquals("", normalizationService.normalize("NA"));
    }
}
