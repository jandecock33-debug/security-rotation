
package com.example.momentum;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class EtfRanker {

    private final ScoreCalculator calculator;
    private final ScoreMode mode;

    public EtfRanker(ScoreCalculator calculator, ScoreMode mode) {
        this.calculator = calculator;
        this.mode = mode;
    }

    public ScoreMode getMode() {
        return mode;
    }

    public List<RankedEtf> rank(Map<String, EtfHistory> universe, LocalDate asOfDate) {
        List<RankedEtf> list = new ArrayList<>();

        for (EtfHistory history : universe.values()) {
            calculator.computeScore(history, asOfDate)
                      .ifPresent(score -> list.add(new RankedEtf(history.getSymbol(), score)));
        }

        return list.stream()
                   .sorted(Comparator.comparingDouble(RankedEtf::score).reversed())
                   .collect(Collectors.toList());
    }
}
