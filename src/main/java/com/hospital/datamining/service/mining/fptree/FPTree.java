package com.hospital.datamining.service.mining.fptree;

import java.util.*;

public class FPTree {

    private final FPTreeNode root = new FPTreeNode(null, 0);
    private final Map<String, Integer> headerTableCounts = new LinkedHashMap<>();
    private final Map<String, FPTreeNode> headerTableLinks = new LinkedHashMap<>();

    public FPTree() {
    }

    /**
     * Thêm một transaction (đã sắp xếp giảm dần theo tần suất) vào cây
     */
    public void addTransaction(List<String> transaction, int count) {
        FPTreeNode current = root;
        for (String item : transaction) {
            FPTreeNode child = current.getChild(item);
            if (child == null) {
                child = new FPTreeNode(item, count);
                current.addChild(child);
                linkHeaderTable(child);
            } else {
                child.incrementCount(count);
            }
            // Cập nhật tổng count trong headerTable
            headerTableCounts.put(item, headerTableCounts.getOrDefault(item, 0) + count);
            current = child;
        }
    }

    private void linkHeaderTable(FPTreeNode newNode) {
        String item = newNode.getItem();
        if (!headerTableLinks.containsKey(item)) {
            headerTableLinks.put(item, newNode);
        } else {
            FPTreeNode head = headerTableLinks.get(item);
            while (head.getNodeLink() != null) {
                head = head.getNodeLink();
            }
            head.setNodeLink(newNode);
        }
    }

    public FPTreeNode getRoot() {
        return root;
    }

    public Map<String, Integer> getHeaderTableCounts() {
        return headerTableCounts;
    }

    public Map<String, FPTreeNode> getHeaderTableLinks() {
        return headerTableLinks;
    }

    public boolean isEmpty() {
        return root.getChildren().isEmpty();
    }

    /**
     * Kiểm tra cây có phải chỉ có một đường dẫn đơn (single path) hay không
     */
    public boolean hasSinglePath() {
        if (isEmpty()) return false;
        FPTreeNode current = root;
        while (!current.getChildren().isEmpty()) {
            if (current.getChildren().size() > 1) {
                return false;
            }
            current = current.getChildren().values().iterator().next();
        }
        return true;
    }
}
