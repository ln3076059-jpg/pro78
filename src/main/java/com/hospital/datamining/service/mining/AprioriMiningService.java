package com.hospital.datamining.service.mining;

import com.hospital.datamining.service.mining.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("aprioriMiningService")
public class AprioriMiningService implements AssociationMiningService {

    private static final Logger log = LoggerFactory.getLogger(AprioriMiningService.class);
    private static final double EPSILON = 1e-9;

    @Override
    public String getAlgorithmName() {
        return "APRIORI";
    }

    @Override
    public MiningResult mine(List<Set<String>> transactions, MiningParameters parameters) {
        long startTime = System.currentTimeMillis();
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memBefore = runtime.totalMemory() - runtime.freeMemory();

        if (transactions == null || transactions.isEmpty()) {
            return MiningResult.builder()
                    .algorithm(getAlgorithmName())
                    .runtimeMs(0)
                    .memoryUsageMb(0.0)
                    .transactionCount(0)
                    .uniqueDrugCount(0)
                    .build();
        }

        java.util.Objects.requireNonNull(parameters, "MiningParameters không được null");
        parameters.validate();

        int totalTransactions = transactions.size();
        double minSupport = parameters.getMinSupport();
        double minConfidence = parameters.getMinConfidence();
        double minLift = parameters.getMinLift();
        int maxItemsetSize = parameters.getMaxItemsetSize() > 0 ? parameters.getMaxItemsetSize() : 10;

        // Tập hợp tất cả các thuốc duy nhất
        Set<String> allUniqueDrugs = new HashSet<>();
        for (Set<String> t : transactions) {
            allUniqueDrugs.addAll(t);
        }

        // Lưu trữ tất cả frequent itemsets: Map<Set<String>, Integer (supportCount)>
        Map<Set<String>, Integer> frequentItemsetCounts = new HashMap<>();

        // 1. Sinh L1: Tập phổ biến 1 phần tử
        Map<String, Integer> item1Counts = new HashMap<>();
        for (Set<String> transaction : transactions) {
            for (String item : transaction) {
                item1Counts.put(item, item1Counts.getOrDefault(item, 0) + 1);
            }
        }

        List<Set<String>> currentFrequentItemsets = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : item1Counts.entrySet()) {
            double support = (double) entry.getValue() / totalTransactions;
            if (support >= minSupport - EPSILON) {
                Set<String> itemset = new HashSet<>();
                itemset.add(entry.getKey());
                frequentItemsetCounts.put(itemset, entry.getValue());
                currentFrequentItemsets.add(itemset);
            }
        }

        // 2. Vòng lặp sinh L_k từ L_{k-1}
        int k = 2;
        while (!currentFrequentItemsets.isEmpty() && k <= maxItemsetSize) {
            // Sinh ứng viên C_k
            List<Set<String>> candidates = generateCandidates(currentFrequentItemsets, k);
            if (candidates.isEmpty()) {
                break;
            }

            // Đếm tần suất xuất hiện của ứng viên trong các giao dịch
            Map<Set<String>, Integer> candidateCounts = new HashMap<>();
            for (Set<String> candidate : candidates) {
                for (Set<String> transaction : transactions) {
                    if (transaction.containsAll(candidate)) {
                        candidateCounts.put(candidate, candidateCounts.getOrDefault(candidate, 0) + 1);
                    }
                }
            }

            // Lọc ứng viên đạt minSupport để tạo L_k
            List<Set<String>> nextFrequentItemsets = new ArrayList<>();
            for (Map.Entry<Set<String>, Integer> entry : candidateCounts.entrySet()) {
                double support = (double) entry.getValue() / totalTransactions;
                if (support >= minSupport - EPSILON) {
                    frequentItemsetCounts.put(entry.getKey(), entry.getValue());
                    nextFrequentItemsets.add(entry.getKey());
                }
            }

            currentFrequentItemsets = nextFrequentItemsets;
            k++;
        }

        // Chuyển đổi frequentItemsets sang dạng kết quả
        List<FrequentItemsetResult> frequentItemsetResults = new ArrayList<>();
        for (Map.Entry<Set<String>, Integer> entry : frequentItemsetCounts.entrySet()) {
            double support = (double) entry.getValue() / totalTransactions;
            frequentItemsetResults.add(FrequentItemsetResult.builder()
                    .items(new TreeSet<>(entry.getKey()))
                    .support(round4(support))
                    .supportCount(entry.getValue())
                    .build());
        }
        Collections.sort(frequentItemsetResults);

        // 3. Sinh Luật Kết Hợp (Association Rules)
        List<AssociationRuleResult> associationRules = generateRules(
                frequentItemsetCounts, totalTransactions, minConfidence, minLift
        );
        Collections.sort(associationRules);

        long endTime = System.currentTimeMillis();
        long memAfter = runtime.totalMemory() - runtime.freeMemory();
        double memUsedMb = Math.max(0.01, (memAfter - memBefore) / (1024.0 * 1024.0));

        log.info("Apriori hoàn tất: {} itemsets, {} rules, thời gian {} ms",
                frequentItemsetResults.size(), associationRules.size(), (endTime - startTime));

        return MiningResult.builder()
                .algorithm(getAlgorithmName())
                .frequentItemsets(frequentItemsetResults)
                .associationRules(associationRules)
                .runtimeMs(endTime - startTime)
                .memoryUsageMb(round2(memUsedMb))
                .transactionCount(totalTransactions)
                .uniqueDrugCount(allUniqueDrugs.size())
                .build();
    }

    /**
     * Sinh tập ứng viên C_k từ L_{k-1} và tỉa theo tính chất Apriori
     */
    private List<Set<String>> generateCandidates(List<Set<String>> prevFrequent, int k) {
        List<Set<String>> candidates = new ArrayList<>();
        int size = prevFrequent.size();

        // Chuyển đổi thành danh sách đã sắp xếp để kết hợp
        List<List<String>> sortedPrev = new ArrayList<>(size);
        for (Set<String> itemset : prevFrequent) {
            List<String> list = new ArrayList<>(itemset);
            Collections.sort(list);
            sortedPrev.add(list);
        }

        Set<Set<String>> prevSetLookup = new HashSet<>(prevFrequent);

        for (int i = 0; i < size; i++) {
            List<String> l1 = sortedPrev.get(i);
            for (int j = i + 1; j < size; j++) {
                List<String> l2 = sortedPrev.get(j);

                // Kiểm tra xem k-2 phần tử đầu có giống nhau không
                boolean canJoin = true;
                for (int m = 0; m < k - 2; m++) {
                    if (!l1.get(m).equals(l2.get(m))) {
                        canJoin = false;
                        break;
                    }
                }

                if (canJoin) {
                    Set<String> candidate = new HashSet<>(l1);
                    candidate.add(l2.get(k - 2));

                    if (candidate.size() == k) {
                        // Tỉa theo tính chất Apriori: Mọi tập con kích thước k-1 phải thuộc L_{k-1}
                        if (allSubsetsAreFrequent(candidate, prevSetLookup)) {
                            candidates.add(candidate);
                        }
                    }
                }
            }
        }
        return candidates;
    }

    /**
     * Kiểm tra tính chất Apriori: Tất cả tập con kích thước (size - 1) phải thuộc tập phổ biến
     */
    private boolean allSubsetsAreFrequent(Set<String> candidate, Set<Set<String>> prevFrequent) {
        List<String> list = new ArrayList<>(candidate);
        for (int i = 0; i < list.size(); i++) {
            Set<String> subset = new HashSet<>(list);
            subset.remove(list.get(i));
            if (!prevFrequent.contains(subset)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sinh luật kết hợp từ các tập phổ biến có kích thước >= 2
     */
    private List<AssociationRuleResult> generateRules(
            Map<Set<String>, Integer> frequentItemsetCounts,
            int totalTransactions,
            double minConfidence,
            double minLift) {

        List<AssociationRuleResult> rules = new ArrayList<>();

        for (Map.Entry<Set<String>, Integer> entry : frequentItemsetCounts.entrySet()) {
            Set<String> itemset = entry.getKey();
            if (itemset.size() < 2) continue;

            int itemsetCount = entry.getValue();
            double itemsetSupport = (double) itemsetCount / totalTransactions;

            // Sinh tất cả tập con thực sự khác rỗng làm Tiền đề (Antecedent)
            List<Set<String>> properSubsets = generateProperSubsets(itemset);

            for (Set<String> antecedent : properSubsets) {
                Set<String> consequent = new HashSet<>(itemset);
                consequent.removeAll(antecedent);

                Integer antecedentCount = frequentItemsetCounts.get(antecedent);
                Integer consequentCount = frequentItemsetCounts.get(consequent);

                if (antecedentCount != null && antecedentCount > 0 && consequentCount != null && consequentCount > 0) {
                    double confidence = (double) itemsetCount / antecedentCount;
                    double consequentSupport = (double) consequentCount / totalTransactions;

                    if (confidence >= minConfidence - EPSILON) {
                        double lift = consequentSupport > 0 ? (confidence / consequentSupport) : 0.0;

                        if (lift >= minLift - EPSILON) {
                            rules.add(AssociationRuleResult.builder()
                                    .antecedent(new TreeSet<>(antecedent))
                                    .consequent(new TreeSet<>(consequent))
                                    .support(round4(itemsetSupport))
                                    .confidence(round4(confidence))
                                    .lift(round4(lift))
                                    .build());
                        }
                    }
                }
            }
        }
        return rules;
    }

    /**
     * Sinh tất cả các tập con thực sự khác rỗng của một itemset
     */
    private List<Set<String>> generateProperSubsets(Set<String> set) {
        List<String> list = new ArrayList<>(set);
        List<Set<String>> subsets = new ArrayList<>();
        int n = list.size();
        int totalSubsets = 1 << n; // 2^n

        // Bỏ qua tập rỗng (i = 0) và chính nó (i = 2^n - 1)
        for (int i = 1; i < totalSubsets - 1; i++) {
            Set<String> sub = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) != 0) {
                    sub.add(list.get(j));
                }
            }
            subsets.add(sub);
        }
        return subsets;
    }

    @Override
    public MiningResult mineWithIds(List<Set<Long>> transactions, MiningParameters parameters) {
        if (transactions == null || transactions.isEmpty()) {
            return MiningResult.builder()
                    .algorithm(getAlgorithmName())
                    .runtimeMs(0)
                    .memoryUsageMb(0.0)
                    .transactionCount(0)
                    .uniqueDrugCount(0)
                    .build();
        }
        List<Set<String>> stringTransactions = new ArrayList<>(transactions.size());
        for (Set<Long> t : transactions) {
            Set<String> s = new HashSet<>();
            if (t != null) {
                for (Long id : t) {
                    if (id != null) s.add(String.valueOf(id));
                }
            }
            stringTransactions.add(s);
        }
        return mine(stringTransactions, parameters);
    }

    private double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
