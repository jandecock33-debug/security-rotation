
package com.example.momentum;

import java.time.LocalDate;
import java.util.OptionalDouble;

public interface ScoreCalculator {
    OptionalDouble computeScore(EtfHistory history, LocalDate asOfDate);
}
