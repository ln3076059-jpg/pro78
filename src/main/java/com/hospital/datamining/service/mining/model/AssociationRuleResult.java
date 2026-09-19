package com.hospital.datamining.service.mining.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssociationRuleResult implements Comparable<AssociationRuleResult> {
    private Set<String> antecedent;
    private Set<String> consequent;
    private double support;
    private double confidence;
    private double lift;

    public int getAntecedentSize() {
        return antecedent != null ? antecedent.size() : 0;
    }

    public int getConsequentSize() {
        return consequent != null ? consequent.size() : 0;
    }

    public String getAntecedentAsString() {
        if (antecedent == null || antecedent.isEmpty()) return "";
        return String.join(", ", new TreeSet<>(antecedent));
    }

    public String getConsequentAsString() {
        if (consequent == null || consequent.isEmpty()) return "";
        return String.join(", ", new TreeSet<>(consequent));
    }

    @Override
    public int compareTo(AssociationRuleResult o) {
        int cmp = Double.compare(o.confidence, this.confidence);
        if (cmp != 0) return cmp;
        int cmpLift = Double.compare(o.lift, this.lift);
        if (cmpLift != 0) return cmpLift;
        return Double.compare(o.support, this.support);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssociationRuleResult that = (AssociationRuleResult) o;
        return Objects.equals(antecedent, that.antecedent) &&
               Objects.equals(consequent, that.consequent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(antecedent, consequent);
    }
}
