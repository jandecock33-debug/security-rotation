package com.example.momentum;

/**
 * Ranked ETF entry.
 *
 * @param score the value used for sorting/selection (higher is better)
 */
public record RankedEtf(String symbol, double score, double daily, double weekly, double monthly) {

    public boolean hasMultiTimeframeScores() {
        return !(Double.isNaN(daily) && Double.isNaN(weekly) && Double.isNaN(monthly));
    }
}
