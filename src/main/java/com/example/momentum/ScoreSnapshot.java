package com.example.momentum;

/**
 * Score values for a symbol as-of a given date.
 *
 * @param score   the value used for sorting/selection (higher is better)
 * @param daily   daily overall score (or NaN when not applicable)
 * @param weekly  weekly overall score (or NaN when not applicable)
 * @param monthly monthly overall score (or NaN when not applicable)
 */
public record ScoreSnapshot(double score, double daily, double weekly, double monthly) {

    public static ScoreSnapshot single(double score) {
        return new ScoreSnapshot(score, score, Double.NaN, Double.NaN);
    }
}
