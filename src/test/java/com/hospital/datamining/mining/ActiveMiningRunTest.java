package com.hospital.datamining.mining;

import com.hospital.datamining.entity.MiningRun;
import com.hospital.datamining.repository.*;
import com.hospital.datamining.service.mining.MiningRunService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@DisplayName("Kiểm thử Quản lý Mô hình Kê đơn Hoạt động (Active Mining Run Selection)")
class ActiveMiningRunTest {

    private MiningRunService miningRunService;
    private MiningRunRepository miningRunRepository;

    @BeforeEach
    void setUp() {
        miningRunRepository = Mockito.mock(MiningRunRepository.class);
        FrequentItemsetRepository frequentItemsetRepository = Mockito.mock(FrequentItemsetRepository.class);
        FrequentItemsetItemRepository frequentItemsetItemRepository = Mockito.mock(FrequentItemsetItemRepository.class);
        AssociationRuleRepository associationRuleRepository = Mockito.mock(AssociationRuleRepository.class);
        AssociationRuleItemRepository associationRuleItemRepository = Mockito.mock(AssociationRuleItemRepository.class);
        AssociationRuleAntecedentRepository antecedentRepository = Mockito.mock(AssociationRuleAntecedentRepository.class);
        AssociationRuleConsequentRepository consequentRepository = Mockito.mock(AssociationRuleConsequentRepository.class);
        MedicineRepository medicineRepository = Mockito.mock(MedicineRepository.class);
        DatasetImportRepository datasetImportRepository = Mockito.mock(DatasetImportRepository.class);

        miningRunService = new MiningRunService(
                miningRunRepository,
                frequentItemsetRepository,
                frequentItemsetItemRepository,
                associationRuleRepository,
                associationRuleItemRepository,
                antecedentRepository,
                consequentRepository,
                medicineRepository,
                datasetImportRepository
        );
    }

    @Test
    @DisplayName("Kiểm tra kích hoạt Active Model và reset các model khác")
    void testSetActiveMiningRun() {
        MiningRun run1 = MiningRun.builder().id(1L).algorithm("APRIORI").status("SUCCESS").selectedForRecommendation(false).build();
        MiningRun run2 = MiningRun.builder().id(2L).algorithm("FP_GROWTH").status("SUCCESS").selectedForRecommendation(false).build();

        Mockito.when(miningRunRepository.findById(2L)).thenReturn(Optional.of(run2));
        Mockito.when(miningRunRepository.save(any(MiningRun.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean success = miningRunService.setActiveMiningRun(2L);

        assertTrue(success);
        assertTrue(run2.getSelectedForRecommendation(), "Model #2 phải được đánh dấu selectedForRecommendation = true");
        Mockito.verify(miningRunRepository).resetAllSelectedForRecommendation();
    }

    @Test
    @DisplayName("Kiểm tra fallback sang latest run khi chưa có active model tường minh")
    void testGetActiveMiningRunFallback() {
        MiningRun latest = MiningRun.builder().id(5L).algorithm("FP_GROWTH").status("SUCCESS").selectedForRecommendation(false).build();

        Mockito.when(miningRunRepository.findFirstBySelectedForRecommendationTrueOrderByCreatedAtDesc())
                .thenReturn(Optional.empty());
        Mockito.when(miningRunRepository.findFirstByStatusOrderByCreatedAtDesc("SUCCESS"))
                .thenReturn(Optional.of(latest));

        Optional<MiningRun> active = miningRunService.getActiveMiningRun();

        assertTrue(active.isPresent());
        assertEquals(5L, active.get().getId());
    }

    @Test
    @DisplayName("Từ chối kích hoạt model nếu trạng thái không phải SUCCESS (Req #29)")
    void testSetActiveMiningRunRejectsNonSuccess() {
        MiningRun failedRun = MiningRun.builder().id(3L).algorithm("APRIORI").status("FAILED").selectedForRecommendation(false).build();
        Mockito.when(miningRunRepository.findById(3L)).thenReturn(Optional.of(failedRun));

        boolean result = miningRunService.setActiveMiningRun(3L);

        assertFalse(result, "Không được phép kích hoạt MiningRun có status khác SUCCESS");
        Mockito.verify(miningRunRepository, Mockito.never()).resetAllSelectedForRecommendation();
    }
}
