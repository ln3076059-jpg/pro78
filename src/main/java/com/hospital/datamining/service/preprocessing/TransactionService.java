package com.hospital.datamining.service.preprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    // Lưu trữ transactions đang hoạt động trong bộ nhớ đệm
    private List<Set<String>> currentTransactions = new ArrayList<>();
    private final Map<String, Integer> drugFrequencyCache = new ConcurrentHashMap<>();
    private final Set<String> uniqueDrugsCache = Collections.synchronizedSet(new HashSet<>());
    private String currentDatasetName = "Chưa nạp dữ liệu";

    public synchronized void setTransactions(List<Set<String>> transactions, String datasetName) {
        this.currentTransactions = new ArrayList<>(transactions);
        this.currentDatasetName = datasetName;
        recalculateCaches();
        log.info("Đã cập nhật TransactionService: {} transactions từ dataset '{}'",
                transactions.size(), datasetName);
    }

    public synchronized List<Set<String>> getTransactions() {
        return Collections.unmodifiableList(currentTransactions);
    }

    public synchronized int getTransactionCount() {
        return currentTransactions.size();
    }

    public synchronized Set<String> getUniqueDrugs() {
        return Collections.unmodifiableSet(uniqueDrugsCache);
    }

    public synchronized int getUniqueDrugCount() {
        return uniqueDrugsCache.size();
    }

    public synchronized Map<String, Integer> getDrugFrequencies() {
        return Collections.unmodifiableMap(drugFrequencyCache);
    }

    public synchronized String getCurrentDatasetName() {
        return currentDatasetName;
    }

    public synchronized boolean hasTransactions() {
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
