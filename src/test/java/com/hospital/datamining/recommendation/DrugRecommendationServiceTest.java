package com.hospital.datamining.recommendation;

import com.hospital.datamining.dto.RecommendationResponseDTO;
import com.hospital.datamining.entity.AssociationRule;
import com.hospital.datamining.entity.Medicine;
import com.hospital.datamining.entity.MiningRun;
import com.hospital.datamining.repository.AssociationRuleRepository;
import com.hospital.datamining.repository.MedicineRepository;
import com.hospital.datamining.service.mining.MiningRunService;
import com.hospital.datamining.service.prescription.DrugRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@DisplayName("Kiểm thử Tích Hợp Kê Đơn & Gợi Ý Thuốc (DrugRecommendationService)")
class DrugRecommendationServiceTest {

    private DrugRecommendationService recommendationService;
    private MiningRunService miningRunService;
    private AssociationRuleRepository ruleRepository;
    private MedicineRepository medicineRepository;

    @BeforeEach
    void setUp() {
        miningRunService = Mockito.mock(MiningRunService.class);
        ruleRepository = Mockito.mock(AssociationRuleRepository.class);
        medicineRepository = Mockito.mock(MedicineRepository.class);

        recommendationService = new DrugRecommendationService(
                miningRunService, ruleRepository, medicineRepository
        );
    }

    @Test
    @DisplayName("Kiểm tra gợi ý thuốc khi bác sĩ chọn Aspirin và lọc Lift > 1.0 kèm disclaimer lâm sàng")
    void testDrugRecommendation() {
        MiningRun fakeRun = MiningRun.builder().id(1L).algorithm("APRIORI").status("SUCCESS").build();
        Mockito.when(miningRunService.getActiveMiningRun()).thenReturn(Optional.of(fakeRun));
        Mockito.when(miningRunService.getLatestSuccessfulRun()).thenReturn(Optional.of(fakeRun));

        // Giả lập 2 luật:
        // Rule 1: {Aspirin} -> {Metoprolol} (Conf 0.75, Lift 1.45 > 1.0)
        // Rule 2: {Aspirin} -> {Pantoprazole} (Conf 0.60, Lift 1.20 > 1.0)
        AssociationRule r1 = AssociationRule.builder()
                .id(101L)
                .antecedent("Aspirin")
                .consequent("Metoprolol")
                .support(0.15)
                .confidence(0.75)
                .lift(1.45)
                .build();

        AssociationRule r2 = AssociationRule.builder()
                .id(102L)
                .antecedent("Aspirin")
                .consequent("Pantoprazole")
                .support(0.12)
                .confidence(0.60)
                .lift(1.20)
                .build();

        Mockito.when(ruleRepository.findTopConfidentRules(eq(1L), anyDouble(), anyDouble()))
                .thenReturn(Arrays.asList(r1, r2));

        Medicine medMetoprolol = Medicine.builder().id(2L).genericName("Metoprolol").build();
        Mockito.when(medicineRepository.findByGenericNameIgnoreCase("Metoprolol"))
                .thenReturn(Optional.of(medMetoprolol));

        List<RecommendationResponseDTO> recs = recommendationService.recommendByDrugNames(
                Collections.singletonList("Aspirin"), 5
        );

        assertNotNull(recs);
        assertEquals(2, recs.size(), "Mong đợi 2 thuốc được gợi ý");

        // Kiểm tra vị trí số 1 (Confidence cao hơn: Metoprolol 75%)
        RecommendationResponseDTO top1 = recs.get(0);
        assertEquals("Metoprolol", top1.getDrug());
        assertEquals(0.75, top1.getConfidence());
        assertEquals(1.45, top1.getLift());
        assertTrue(top1.getLift() > 1.0, "Lift phải lớn hơn 1.0");
        assertNotNull(top1.getClinicalDisclaimer());
        assertTrue(top1.getClinicalDisclaimer().contains("tham khảo"), "Phải có tuyên bố miễn trừ trách nhiệm lâm sàng");
    }

    @Test
    @DisplayName("Ưu tiên luật tiền đề đa phần tử {Aspirin, Heparin} -> X trước fallback đơn phần tử (Mục 21 đề bài)")
    void testMultiItemAntecedentPriorityOverSingleItemFallback() {
        MiningRun fakeRun = MiningRun.builder().id(1L).algorithm("FP_GROWTH").status("SUCCESS").build();
        Mockito.when(miningRunService.getActiveMiningRun()).thenReturn(Optional.of(fakeRun));
        Mockito.when(miningRunService.getLatestSuccessfulRun()).thenReturn(Optional.of(fakeRun));

        // Rule 1: {Aspirin, Heparin} -> {Metoprolol} (Tier 1: Multi-item antecedent)
        AssociationRule rMulti = AssociationRule.builder()
                .id(201L)
                .antecedent("Aspirin, Heparin")
                .consequent("Metoprolol")
                .support(0.10)
                .confidence(0.70)
                .lift(1.50)
                .build();

        // Rule 2: {Aspirin} -> {Pantoprazole} (Tier 2: Single-item fallback)
        AssociationRule rSingle = AssociationRule.builder()
                .id(202L)
                .antecedent("Aspirin")
                .consequent("Pantoprazole")
                .support(0.18)
                .confidence(0.85) // Confidence cao hơn rMulti nhưng là đơn phần tử
                .lift(1.30)
                .build();

        Mockito.when(ruleRepository.findTopConfidentRules(eq(1L), anyDouble(), anyDouble()))
                .thenReturn(Arrays.asList(rSingle, rMulti));

        Mockito.when(medicineRepository.findByGenericNameIgnoreCase("Metoprolol"))
                .thenReturn(Optional.of(Medicine.builder().id(2L).genericName("Metoprolol").build()));
        Mockito.when(medicineRepository.findByGenericNameIgnoreCase("Pantoprazole"))
                .thenReturn(Optional.of(Medicine.builder().id(4L).genericName("Pantoprazole").build()));

        // Bác sĩ đang chọn cả 2 thuốc: Aspirin và Heparin
        List<RecommendationResponseDTO> recs = recommendationService.recommendByDrugNames(
                Arrays.asList("Aspirin", "Heparin"), 5
        );

        assertNotNull(recs);
        assertEquals(2, recs.size());

        // Theo Mục 21 đề bài: {Aspirin, Heparin} -> X được ưu tiên hàng đầu (Tier 1)
        assertEquals("Metoprolol", recs.get(0).getDrug(),
                "Luật đa phần tử {Aspirin, Heparin} -> Metoprolol phải được xếp hạng ưu tiên trước luật đơn phần tử");
        assertEquals("Pantoprazole", recs.get(1).getDrug(),
                "Luật đơn phần tử {Aspirin} -> Pantoprazole được bổ sung tiếp theo");
    }

    @Test
    @DisplayName("Kiểm tra gợi ý kê đơn truy vấn trực tiếp bằng Medicine ID qua quan hệ association_rule_antecedents")
    void testRecommendByMedicineIdsDirectQuery() {
        MiningRun fakeRun = MiningRun.builder().id(1L).algorithm("FP_GROWTH").status("SUCCESS").build();
        Mockito.when(miningRunService.getActiveMiningRun()).thenReturn(Optional.of(fakeRun));
        Mockito.when(miningRunService.getLatestSuccessfulRun()).thenReturn(Optional.of(fakeRun));

        Medicine medAspirin = Medicine.builder().id(10L).genericName("Aspirin").build();
        Medicine medMetoprolol = Medicine.builder().id(20L).genericName("Metoprolol").build();
        Mockito.when(medicineRepository.findAllById(Collections.singletonList(10L)))
                .thenReturn(Collections.singletonList(medAspirin));
        Mockito.when(medicineRepository.findByGenericNameIgnoreCase("Metoprolol"))
                .thenReturn(Optional.of(medMetoprolol));

        AssociationRule r = AssociationRule.builder()
                .id(301L)
                .antecedent("Aspirin")
                .consequent("Metoprolol")
                .support(0.12)
                .confidence(0.70)
                .lift(1.40)
                .build();

        Mockito.when(ruleRepository.findRulesByAntecedentMedicineIds(eq(1L), eq(Collections.singletonList(10L)), anyDouble(), anyDouble()))
                .thenReturn(Collections.singletonList(r));

        List<RecommendationResponseDTO> recs = recommendationService.recommendByMedicineIds(
                Collections.singletonList(10L), 5
        );

        assertNotNull(recs);
        assertEquals(1, recs.size());
        assertEquals("Metoprolol", recs.get(0).getDrug());
        assertEquals(20L, recs.get(0).getMedicineId());
    }
}
