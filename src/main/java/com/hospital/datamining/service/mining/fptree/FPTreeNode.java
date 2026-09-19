package com.hospital.datamining.service.mining.fptree;

import java.util.LinkedHashMap;
import java.util.Map;

public class FPTreeNode {
    private String item;
    private int count;
    private FPTreeNode parent;
    private final Map<String, FPTreeNode> children = new LinkedHashMap<>();
    private FPTreeNode nodeLink; // Con trỏ danh sách liên kết cho Header Table

    public FPTreeNode(String item, int count) {
        this.item = item;
        this.count = count;
    }

    public void incrementCount(int value) {
        this.count += value;
    }

    public FPTreeNode getChild(String item) {
        return children.get(item);
    }

    public void addChild(FPTreeNode child) {
        child.setParent(this);
        children.put(child.getItem(), child);
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public FPTreeNode getParent() {
        return parent;
    }

    public void setParent(FPTreeNode parent) {
        this.parent = parent;
    }

    public Map<String, FPTreeNode> getChildren() {
        return children;
    }

    public FPTreeNode getNodeLink() {
        return nodeLink;
    }

    public void setNodeLink(FPTreeNode nodeLink) {
        this.nodeLink = nodeLink;
    }
}
