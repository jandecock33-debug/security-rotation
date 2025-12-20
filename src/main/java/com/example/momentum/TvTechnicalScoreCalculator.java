package com.example.momentum;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TradingView-style technical score.
 *
 * For a given asOfDate we compute 3 overall scores:
 * - Daily: computed on daily bars
 * - Weekly: daily bars resampled to weekly
 * - Monthly: daily bars resampled to monthly
 *
 * Ranking is lexicographic on (monthly, weekly, daily). We encode this into
 * a single sort score so the existing backtest selection logic keeps working.
 */
public class TvTechnicalScoreCalculator implements ScoreCalculator {

    // Big weights to preserve lexicographic ordering while keeping selection "score > 0" meaningful.
    private static final double MONTHLY_W = 1_000_000.0;
    private static final double WEEKLY_W = 1_000.0;

    @Override
    public Optional<ScoreSnapshot> computeScore(EtfHistory history, LocalDate asOfDate) {
        List<PriceBar> dailyBars = history.getBars().stream()
                .filter(b -> !b.date().isAfter(asOfDate))
                .toList();

        if (dailyBars.isEmpty()) return Optional.empty();

        TvRater.TvTimeframeScore daily = TvRater.rate(dailyBars);
        TvRater.TvTimeframeScore weekly = TvRater.rate(TvResampler.resample(history.getBars(), TvResampler.PeriodType.WEEKLY, asOfDate));
        TvRater.TvTimeframeScore monthly = TvRater.rate(TvResampler.resample(history.getBars(), TvResampler.PeriodType.MONTHLY, asOfDate));

        double d = daily.overallScore();
        double w = weekly.overallScore();
        double m = monthly.overallScore();

        if (Double.isNaN(d) && Double.isNaN(w) && Double.isNaN(m)) return Optional.empty();

        double sortScore = safe(m) * MONTHLY_W + safe(w) * WEEKLY_W + safe(d);
        return Optional.of(new ScoreSnapshot(sortScore, d, w, m));
    }

    private static double safe(double v) {
        return Double.isNaN(v) ? 0.0 : v;
    }
}
