package com.hospital.datamining.service.mining;

import com.hospital.datamining.service.mining.fptree.FPTree;
import com.hospital.datamining.service.mining.fptree.FPTreeNode;
import com.hospital.datamining.service.mining.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("fpGrowthMiningService")
public class FPGrowthMiningService implements AssociationMiningService {

    private static final Logger log = LoggerFactory.getLogger(FPGrowthMiningService.class);
    private static final double EPSILON = 1e-9;

    @Override
    public String getAlgorithmName() {
        return "FP_GROWTH";
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

        if (parameters != null) {
            parameters.validate();
        }

        int totalTransactions = transactions.size();
        double minSupport = parameters.getMinSupport();
        double minConfidence = parameters.getMinConfidence();
        double minLift = parameters.getMinLift();
        int maxItemsetSize = parameters.getMaxItemsetSize() > 0 ? parameters.getMaxItemsetSize() : 10;
        int minSupportCount = (int) Math.ceil(minSupport * totalTransactions - EPSILON);

        // Đếm thuốc duy nhất
        Set<String> allUniqueDrugs = new HashSet<>();
        for (Set<String> t : transactions) {
            allUniqueDrugs.addAll(t);
        }

        // Bước 1: Đếm tần suất các 1-itemset
        Map<String, Integer> itemFrequencies = new HashMap<>();
        for (Set<String> transaction : transactions) {
            for (String item : transaction) {
                itemFrequencies.put(item, itemFrequencies.getOrDefault(item, 0) + 1);
            }
        }

        // Lọc các item phổ biến >= minSupportCount
        Map<String, Integer> frequent1Items = new HashMap<>();
        for (Map.Entry<String, Integer> entry : itemFrequencies.entrySet()) {
            if (entry.getValue() >= minSupportCount) {
                frequent1Items.put(entry.getKey(), entry.getValue());
            }
        }

        // Comparator sắp xếp item theo tần suất giảm dần
        Comparator<String> itemComparator = (a, b) -> {
            int cmp = Integer.compare(frequent1Items.get(b), frequent1Items.get(a));
            if (cmp != 0) return cmp;
            return a.compareTo(b); // Giữ tính tất định
        };

        // Bước 2: Xây dựng FP-Tree ban đầu
        FPTree tree = new FPTree();
        for (Set<String> transaction : transactions) {
            List<String> filteredSorted = new ArrayList<>();
            for (String item : transaction) {
                if (frequent1Items.containsKey(item)) {
                    filteredSorted.add(item);
                }
            }
            if (!filteredSorted.isEmpty()) {
                filteredSorted.sort(itemComparator);
                tree.addTransaction(filteredSorted, 1);
            }
        }

        // Bước 3: Khai phá đệ quy FP-Tree để tìm tất cả frequent itemsets
        Map<Set<String>, Integer> frequentItemsetCounts = new HashMap<>();
        mineFPTree(tree, new HashSet<>(), frequentItemsetCounts, minSupportCount, maxItemsetSize);

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

        // Bước 4: Sinh Association Rules
        List<AssociationRuleResult> associationRules = generateRules(
                frequentItemsetCounts, totalTransactions, minConfidence, minLift
        );
        Collections.sort(associationRules);

        long endTime = System.currentTimeMillis();
        long memAfter = runtime.totalMemory() - runtime.freeMemory();
        double memUsedMb = Math.max(0.01, (memAfter - memBefore) / (1024.0 * 1024.0));

        log.info("FP-Growth hoàn tất: {} itemsets, {} rules, thời gian {} ms",
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
     * Khai phá đệ quy FP-Tree
     */
    private void mineFPTree(FPTree tree,
                            Set<String> prefix,
                            Map<Set<String>, Integer> frequentItemsets,
                            int minSupportCount,
                            int maxItemsetSize) {

        if (tree.isEmpty()) return;

        // Header table items sắp xếp theo thứ tự tần suất tăng dần (duyệt từ dưới lên)
        List<Map.Entry<String, Integer>> headerList = new ArrayList<>(tree.getHeaderTableCounts().entrySet());
        headerList.sort(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> headerEntry : headerList) {
            String item = headerEntry.getKey();
            int count = headerEntry.getValue();

            Set<String> newPattern = new HashSet<>(prefix);
            newPattern.add(item);

            if (count >= minSupportCount) {
                // Lưu mẫu phổ biến
                frequentItemsets.put(newPattern, count);

                if (newPattern.size() >= maxItemsetSize) {
                    continue;
                }

                // Thu thập Conditional Pattern Base (các nhánh tiền tố dẫn tới node item)
                List<List<String>> prefixPaths = new ArrayList<>();
                List<Integer> pathCounts = new ArrayList<>();

                FPTreeNode node = tree.getHeaderTableLinks().get(item);
                while (node != null) {
                    int nodeCount = node.getCount();
                    List<String> path = new ArrayList<>();
                    FPTreeNode parent = node.getParent();
                    while (parent != null && parent.getItem() != null) {
                        path.add(parent.getItem());
                        parent = parent.getParent();
                    }
                    if (!path.isEmpty()) {
                        Collections.reverse(path);
                        prefixPaths.add(path);
                        pathCounts.add(nodeCount);
                    }
                    node = node.getNodeLink();
                }

                // Đếm tần suất các item trong Conditional Pattern Base
                Map<String, Integer> condItemCounts = new HashMap<>();
                for (int i = 0; i < prefixPaths.size(); i++) {
                    List<String> path = prefixPaths.get(i);
                    int pCount = pathCounts.get(i);
                    for (String pItem : path) {
                        condItemCounts.put(pItem, condItemCounts.getOrDefault(pItem, 0) + pCount);
                    }
                }

                // Lọc các item thỏa mãn minSupportCount trong cây điều kiện
                Map<String, Integer> frequentCondItems = new HashMap<>();
                for (Map.Entry<String, Integer> cEntry : condItemCounts.entrySet()) {
                    if (cEntry.getValue() >= minSupportCount) {
                        frequentCondItems.put(cEntry.getKey(), cEntry.getValue());
                    }
                }

                if (!frequentCondItems.isEmpty()) {
                    Comparator<String> condComparator = (a, b) -> {
                        int cmp = Integer.compare(frequentCondItems.get(b), frequentCondItems.get(a));
                        if (cmp != 0) return cmp;
                        return a.compareTo(b);
                    };

                    // Xây dựng cây FP-Tree điều kiện
                    FPTree conditionalTree = new FPTree();
                    for (int i = 0; i < prefixPaths.size(); i++) {
                        List<String> path = prefixPaths.get(i);
                        int pCount = pathCounts.get(i);
                        List<String> filteredPath = new ArrayList<>();
                        for (String pItem : path) {
                            if (frequentCondItems.containsKey(pItem)) {
                                filteredPath.add(pItem);
                            }
                        }
                        if (!filteredPath.isEmpty()) {
                            filteredPath.sort(condComparator);
                            conditionalTree.addTransaction(filteredPath, pCount);
                        }
                    }

                    // Đệ quy khai phá trên cây điều kiện
                    mineFPTree(conditionalTree, newPattern, frequentItemsets, minSupportCount, maxItemsetSize);
                }
            }
        }
    }

    /**
     * Sinh luật kết hợp từ các tập phổ biến
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

    private List<Set<String>> generateProperSubsets(Set<String> set) {
        List<String> list = new ArrayList<>(set);
        List<Set<String>> subsets = new ArrayList<>();
        int n = list.size();
        int totalSubsets = 1 << n;

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
