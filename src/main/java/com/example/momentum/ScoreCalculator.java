package com.example.momentum;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Computes a ranking score for a symbol as of a given date.
 *
 * Some modes are single-score (RS / 6m return), while the TradingView-technical
 * mode produces multiple timeframe scores (D/W/M). To keep the rest of the
 * code simple, we always return a {@link ScoreSnapshot}.
 */
public interface ScoreCalculator {
    Optional<ScoreSnapshot> computeScore(EtfHistory history, LocalDate asOfDate);
}
