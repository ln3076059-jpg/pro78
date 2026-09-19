package com.hospital.datamining.interaction;

import com.hospital.datamining.dto.DrugInteractionDTO;
import com.hospital.datamining.entity.DrugInteraction;
import com.hospital.datamining.repository.DrugInteractionRepository;
import com.hospital.datamining.service.interaction.DrugInteractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class DrugInteractionServiceTest {

    private DrugInteractionRepository repository;
    private DrugInteractionService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(DrugInteractionRepository.class);
        service = new DrugInteractionService(repository);
    }

    @Test
    @DisplayName("Kiểm tra tra cứu tương tác thuốc đã biết (KNOWN) từ FDA DDI")
    void testCheckPairKnown() {
        DrugInteraction interaction = DrugInteraction.builder()
                .id(1L)
                .drugNameA("Metformin")
                .drugNameB("Furosemide")
                .interactionStatus("KNOWN")
                .severity("Moderate")
                .description("Furosemide có thể làm tăng nồng độ Metformin trong huyết tương")
                .source("FDA DDI")
                .build();

        when(repository.findInteractionsBetween("Metformin", "Furosemide"))
                .thenReturn(Collections.singletonList(interaction));

        DrugInteractionDTO result = service.checkPair("Metformin", "Furosemide");

        assertNotNull(result);
        assertEquals("KNOWN", result.getStatus());
        assertEquals("Moderate", result.getSeverity());
        assertTrue(result.getDescription().contains("Furosemide"));
    }

    @Test
    @DisplayName("Kiểm tra tra cứu cặp thuốc không có tương tác (NOT FOUND)")
    void testCheckPairNotFound() {
        when(repository.findInteractionsBetween(anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        DrugInteractionDTO result = service.checkPair("Metformin", "Paracetamol");

        assertNotNull(result);
        assertEquals("NOT FOUND", result.getStatus());
        assertEquals("None", result.getSeverity());
    }

    @Test
    @DisplayName("Kiểm tra đối chiếu thuốc ứng viên với đơn hiện có")
    void testCheckCandidateWithExisting() {
        DrugInteraction interaction = DrugInteraction.builder()
                .id(2L)
                .drugNameA("Aspirin")
                .drugNameB("Heparin")
                .interactionStatus("KNOWN")
                .severity("Major")
                .description("Tăng nguy cơ xuất huyết tiêu hóa")
                .source("FDA DDI")
                .build();

        when(repository.findInteractionsBetween("Aspirin", "Heparin"))
                .thenReturn(Collections.singletonList(interaction));

        List<DrugInteractionDTO> warnings = service.checkCandidateWithExistingDrugs("Aspirin", Arrays.asList("Heparin", "Metformin"));

        assertNotNull(warnings);
        assertEquals(1, warnings.size());
        assertEquals("Major", warnings.get(0).getSeverity());
    }
}
