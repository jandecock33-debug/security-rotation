package com.example.momentum;

import java.time.LocalDate;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Computes the simple 6-month return in percentage terms.
 * 6 months is approximated as 120 trading days here.
 */
public class SixMonthReturnCalculator implements ScoreCalculator {

    private final int lookbackDays;

    public SixMonthReturnCalculator() {
        this(120); // ~6 months
    }

    public SixMonthReturnCalculator(int lookbackDays) {
        this.lookbackDays = lookbackDays;
    }

    @Override
    public Optional<ScoreSnapshot> computeScore(EtfHistory history, LocalDate asOfDate) {
        OptionalDouble closeTodayOpt = history.getCloseOnOrBefore(asOfDate);
        if (closeTodayOpt.isEmpty()) return Optional.empty();
        double cToday = closeTodayOpt.getAsDouble();

        LocalDate lookbackDate = asOfDate.minusDays(lookbackDays);
        OptionalDouble closeLookbackOpt = history.getCloseOnOrBefore(lookbackDate);
        if (closeLookbackOpt.isEmpty()) return Optional.empty();
        double cPast = closeLookbackOpt.getAsDouble();
        if (cPast <= 0.0) return Optional.empty();

        double retFraction = (cToday / cPast) - 1.0;
        double retPercent = retFraction * 100.0;
        return Optional.of(ScoreSnapshot.single(retPercent));
    }
}
