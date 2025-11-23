
package com.example.momentum;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class RelativeStrengthRanker {

    private final RelativeStrengthCalculator calculator;

    public RelativeStrengthRanker(RelativeStrengthCalculator calculator) {
        this.calculator = calculator;
    }

    public List<RankedEtf> rank(Map<String, EtfHistory> universe, LocalDate asOfDate) {
        List<RankedEtf> list = new ArrayList<>();

        for (EtfHistory history : universe.values()) {
            calculator.computeRelativeStrength(history, asOfDate)
                      .ifPresent(rs -> list.add(new RankedEtf(history.getSymbol(), rs)));
        }

        return list.stream()
                   .sorted(Comparator.comparingDouble(RankedEtf::relativeStrength).reversed())
                   .collect(Collectors.toList());
    }
}
