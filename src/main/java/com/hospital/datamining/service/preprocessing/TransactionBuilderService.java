package com.hospital.datamining.service.preprocessing;

import com.hospital.datamining.entity.Medicine;
import com.hospital.datamining.entity.Transaction;
import com.hospital.datamining.entity.TransactionItem;
import com.hospital.datamining.repository.MedicineRepository;
import com.hospital.datamining.repository.TransactionItemRepository;
import com.hospital.datamining.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service xây dựng tập các giao dịch (Transactions) từ dữ liệu nhập viện / encounter.
 * Mỗi giao dịch biểu diễn: encounter_id -> Tập các thuốc được sử dụng đồng thời trong đợt điều trị.
 */
@Service
public class TransactionBuilderService {

    private static final Logger log = LoggerFactory.getLogger(TransactionBuilderService.class);

    private final DrugNormalizationService normalizationService;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final TransactionItemRepository transactionItemRepository;
    private final MedicineRepository medicineRepository;

    public TransactionBuilderService(DrugNormalizationService normalizationService,
                                     TransactionService transactionService) {
        this(normalizationService, transactionService, null, null, null);
    }

    public TransactionBuilderService(DrugNormalizationService normalizationService,
                                     TransactionService transactionService,
                                     TransactionRepository transactionRepository,
                                     TransactionItemRepository transactionItemRepository,
                                     MedicineRepository medicineRepository) {
        this.normalizationService = normalizationService;
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
        this.transactionItemRepository = transactionItemRepository;
        this.medicineRepository = medicineRepository;
    }

    /**
     * Xây dựng danh sách giao dịch từ Map thô (encounter_id -> danh sách thuốc)
     */
    public List<Set<String>> buildTransactions(Map<String, ? extends Collection<String>> rawEncounterToDrugs,
                                               int minDrugsPerTransaction,
                                               String datasetName) {
        return buildTransactionsAndPersist(rawEncounterToDrugs, Collections.emptyMap(), minDrugsPerTransaction, datasetName, null);
    }

    /**
     * Xây dựng danh sách giao dịch từ Map encounter_id -> danh sách thuốc (tiện ích cho UCI dataset)
     */
    public List<Set<String>> buildTransactionsFromEncounters(Map<String, ? extends Collection<String>> rawEncounterToDrugs,
                                                            int minDrugsPerTransaction) {
        return buildTransactions(rawEncounterToDrugs, minDrugsPerTransaction, "UCI-Diabetes");
    }

    /**
     * Xây dựng danh sách giao dịch, lọc transaction có >= minDrugsPerTransaction và lưu vào DB nếu có repository
     */
    @Transactional
    public List<Set<String>> buildTransactionsAndPersist(Map<String, ? extends Collection<String>> rawEncounterToDrugs,
                                                         Map<String, String> encounterToPatient,
                                                         int minDrugsPerTransaction,
                                                         String datasetName,
                                                         Long importId) {
        if (rawEncounterToDrugs == null || rawEncounterToDrugs.isEmpty()) {
            List<Set<String>> empty = Collections.emptyList();
            transactionService.setTransactions(empty, datasetName);
            return empty;
        }

        List<Set<String>> result = new ArrayList<>();
        Set<String> allUniqueDrugs = new HashSet<>();

        Map<String, Medicine> medicineMap = new HashMap<>();
        if (medicineRepository != null) {
            try {
                List<Medicine> allMeds = medicineRepository.findAll();
                for (Medicine m : allMeds) {
                    medicineMap.put(m.getGenericName().toLowerCase(), m);
                    if (m.getDrugCode() != null) {
                        medicineMap.put(m.getDrugCode().toLowerCase(), m);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        List<Transaction> transactionsToSave = new ArrayList<>();

        for (Map.Entry<String, ? extends Collection<String>> entry : rawEncounterToDrugs.entrySet()) {
            String encounterId = entry.getKey();
            Collection<String> rawDrugs = entry.getValue();
            if (rawDrugs == null || rawDrugs.isEmpty()) {
                continue;
            }

            Set<String> normalizedSet = new LinkedHashSet<>();
            for (String raw : rawDrugs) {
                String normalized = normalizationService != null ? normalizationService.normalize(raw) : raw;
                if (!normalized.isEmpty()) {
                    normalizedSet.add(normalized);
                }
            }

            if (normalizedSet.size() >= minDrugsPerTransaction) {
                result.add(normalizedSet);
                allUniqueDrugs.addAll(normalizedSet);

                if (transactionRepository != null && importId != null) {
                    Transaction tx = Transaction.builder()
                            .encounterId(encounterId)
                            .patientReference(encounterToPatient != null ? encounterToPatient.get(encounterId) : null)
                            .sourceImportId(importId)
                            .itemCount(normalizedSet.size())
                            .build();

                    for (String drugName : normalizedSet) {
                        Medicine matchedMed = medicineMap.get(drugName.toLowerCase());
                        TransactionItem item = TransactionItem.builder()
                                .drugName(drugName)
                                .medicine(matchedMed)
                                .build();
                        tx.addItem(item);
                    }
                    transactionsToSave.add(tx);
                }
            }
        }

        if (transactionRepository != null && importId != null && !transactionsToSave.isEmpty()) {
            try {
                int batchSize = 1000;
                for (int i = 0; i < transactionsToSave.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, transactionsToSave.size());
                    transactionRepository.saveAll(transactionsToSave.subList(i, end));
                }
                log.info("TransactionBuilderService: Đã lưu thành công {} transactions vào MySQL cho import ID: {}",
                        transactionsToSave.size(), importId);
            } catch (Exception e) {
                log.warn("Lỗi khi lưu transaction vào DB: {}", e.getMessage());
            }
        }

        transactionService.setTransactions(result, datasetName);
        log.info("TransactionBuilderService: Đã xây dựng {} transactions từ {} lượt encounter (Tổng thuốc duy nhất: {})",
                result.size(), rawEncounterToDrugs.size(), allUniqueDrugs.size());

        return result;
    }

    public List<Set<String>> buildFromCleanSets(List<Set<String>> cleanTransactions, String datasetName) {
        List<Set<String>> valid = new ArrayList<>();
        for (Set<String> t : cleanTransactions) {
            if (t != null && t.size() >= 2) {
                valid.add(new HashSet<>(t));
            }
        }
        transactionService.setTransactions(valid, datasetName);
        return valid;
    }

    public List<Set<String>> loadTransactionsFromDb(Long sourceImportId) {
        if (transactionRepository == null) return Collections.emptyList();
        List<Transaction> dbTransactions = transactionRepository.findAllWithItemsByImportId(sourceImportId);
        List<Set<String>> result = new ArrayList<>();
        for (Transaction tx : dbTransactions) {
            Set<String> drugs = new HashSet<>();
            for (TransactionItem item : tx.getItems()) {
                drugs.add(item.getDrugName());
            }
            if (drugs.size() >= 2) {
                result.add(drugs);
            }
        }
        transactionService.setTransactions(result, "Import #" + sourceImportId);
        return result;
    }

    public DrugNormalizationService getNormalizationService() {
        return normalizationService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }
}
