package com.hospital.datamining.service.preprocessing;

import com.hospital.datamining.entity.DatasetImport;
import com.hospital.datamining.entity.Transaction;
import com.hospital.datamining.entity.TransactionItem;
import com.hospital.datamining.repository.DatasetImportRepository;
import com.hospital.datamining.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final DatasetImportRepository datasetImportRepository;

    // Lưu trữ transactions đang hoạt động trong bộ nhớ đệm
    private List<Set<String>> currentTransactions = new ArrayList<>();
    private final Map<String, Integer> drugFrequencyCache = new ConcurrentHashMap<>();
    private final Set<String> uniqueDrugsCache = Collections.synchronizedSet(new HashSet<>());
    private String currentDatasetName = "Chưa nạp dữ liệu";

    public TransactionService() {
        this.transactionRepository = null;
        this.datasetImportRepository = null;
    }

    @Autowired
    public TransactionService(@Autowired(required = false) TransactionRepository transactionRepository,
                              @Autowired(required = false) DatasetImportRepository datasetImportRepository) {
        this.transactionRepository = transactionRepository;
        this.datasetImportRepository = datasetImportRepository;
    }

    public synchronized void setTransactions(List<Set<String>> transactions, String datasetName) {
        this.currentTransactions = new ArrayList<>(transactions);
        this.currentDatasetName = datasetName;
        recalculateCaches();
        log.info("Đã cập nhật TransactionService: {} transactions từ dataset '{}'",
                transactions.size(), datasetName);
    }

    /**
     * Nạp tập giao dịch từ CSDL theo importId (hoặc từ lần import thành công gần nhất)
     */
    public synchronized boolean loadFromDatabase(Long importId) {
        if (transactionRepository == null) {
            return false;
        }

        List<Transaction> txList;
        String dsName = this.currentDatasetName;

        if (importId != null) {
            txList = transactionRepository.findAllWithItemsByImportId(importId);
            if (datasetImportRepository != null) {
                datasetImportRepository.findById(importId).ifPresent(imp -> this.currentDatasetName = imp.getFileName());
            }
        } else {
            if (datasetImportRepository != null) {
                Optional<DatasetImport> latest = datasetImportRepository.findFirstByStatusOrderByCreatedAtDesc("COMPLETED");
                if (latest.isPresent()) {
                    txList = transactionRepository.findAllWithItemsByImportId(latest.get().getId());
                    dsName = latest.get().getFileName();
                } else {
                    txList = Collections.emptyList();
                }
            } else {
                txList = Collections.emptyList();
            }
        }

        if (txList == null || txList.isEmpty()) {
            return false;
        }

        List<Set<String>> loaded = new ArrayList<>();
        for (Transaction tx : txList) {
            Set<String> itemSet = new HashSet<>();
            if (tx.getItems() != null) {
                for (TransactionItem it : tx.getItems()) {
                    if (it.getDrugName() != null && !it.getDrugName().trim().isEmpty()) {
                        itemSet.add(it.getDrugName().trim());
                    }
                }
            }
            if (itemSet.size() >= 2) {
                loaded.add(itemSet);
            }
        }

        if (!loaded.isEmpty()) {
            setTransactions(loaded, dsName);
            log.info("TransactionService: Nạp thành công {} transactions từ CSDL (Dataset: {})", loaded.size(), dsName);
            return true;
        }
        return false;
    }

    public synchronized List<Set<String>> getTransactions() {
        if (currentTransactions.isEmpty()) {
            loadFromDatabase(null);
        }
        return Collections.unmodifiableList(currentTransactions);
    }

    public synchronized int getTransactionCount() {
        if (currentTransactions.isEmpty()) {
            loadFromDatabase(null);
        }
        return currentTransactions.size();
    }

    public synchronized Set<String> getUniqueDrugs() {
        if (uniqueDrugsCache.isEmpty() && currentTransactions.isEmpty()) {
            loadFromDatabase(null);
        }
        return Collections.unmodifiableSet(uniqueDrugsCache);
    }

    public synchronized int getUniqueDrugCount() {
        if (uniqueDrugsCache.isEmpty() && currentTransactions.isEmpty()) {
            loadFromDatabase(null);
        }
        return uniqueDrugsCache.size();
    }

    public synchronized Map<String, Integer> getDrugFrequencies() {
        if (drugFrequencyCache.isEmpty() && currentTransactions.isEmpty()) {
            loadFromDatabase(null);
        }
        return Collections.unmodifiableMap(drugFrequencyCache);
    }

    public synchronized String getCurrentDatasetName() {
        return currentDatasetName;
    }

    public synchronized boolean hasTransactions() {
        if (currentTransactions.isEmpty()) {
            loadFromDatabase(null);
        }
        return !currentTransactions.isEmpty();
    }

    private void recalculateCaches() {
        drugFrequencyCache.clear();
        uniqueDrugsCache.clear();

        for (Set<String> transaction : currentTransactions) {
            for (String drug : transaction) {
                uniqueDrugsCache.add(drug);
                drugFrequencyCache.put(drug, drugFrequencyCache.getOrDefault(drug, 0) + 1);
            }
        }
    }

    /**
     * Xuất các transaction ra tệp văn bản trung gian
     */
    public synchronized void exportToFile(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            int idx = 1;
            for (Set<String> t : currentTransactions) {
                writer.write("T" + idx + ": " + String.join(", ", t));
                writer.newLine();
                idx++;
            }
        }
    }

    /**
     * Nạp transactions từ tệp văn bản trung gian (HADM_ID: Drug1, Drug2...)
     */
    public synchronized int loadFromFile(File file) throws IOException {
        List<Set<String>> loaded = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                int colonIdx = line.indexOf(':');
                String drugsPart = (colonIdx != -1) ? line.substring(colonIdx + 1) : line;

                String[] items = drugsPart.split(",");
                Set<String> transaction = new HashSet<>();
                for (String it : items) {
                    String drug = it.trim();
                    if (!drug.isEmpty()) {
                        transaction.add(drug);
                    }
                }
                if (transaction.size() >= 2) {
                    loaded.add(transaction);
                }
            }
        }
        setTransactions(loaded, file.getName());
        return loaded.size();
    }
}
