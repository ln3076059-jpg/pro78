package com.hospital.datamining.service.mining.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.TreeSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FrequentItemsetResult implements Comparable<FrequentItemsetResult> {
    private Set<String> items;
    private double support;
    private int supportCount;

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public String getItemsAsString() {
        if (items == null || items.isEmpty()) return "";
        return String.join(", ", new TreeSet<>(items));
    }

    @Override
    public int compareTo(FrequentItemsetResult o) {
        int cmp = Double.compare(o.support, this.support);
        if (cmp != 0) return cmp;
        return Integer.compare(this.getItemCount(), o.getItemCount());
    }
}
