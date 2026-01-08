package com.example.momentum;

import java.time.LocalDate;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Computes a combined momentum / relative-strength score as the average of
 * returns over multiple lookback windows.
 *
 * Example: 63/126/252 days (~3/6/12 months).
 */
public class CombinedMomentumCalculator implements ScoreCalculator {

    private final int[] lookbackDays;

    public CombinedMomentumCalculator(int... lookbackDays) {
        if (lookbackDays == null || lookbackDays.length == 0) {
            throw new IllegalArgumentException("At least one lookback is required");
        }
        this.lookbackDays = lookbackDays;
    }

    @Override
    public Optional<ScoreSnapshot> computeScore(EtfHistory history, LocalDate asOfDate) {
        OptionalDouble closeTodayOpt = history.getCloseOnOrBefore(asOfDate);
        if (closeTodayOpt.isEmpty()) return Optional.empty();
        double cToday = closeTodayOpt.getAsDouble();

        double sum = 0.0;
        int count = 0;

        for (int lb : lookbackDays) {
            LocalDate lookbackDate = asOfDate.minusDays(lb);
            OptionalDouble closeLookbackOpt = history.getCloseOnOrBefore(lookbackDate);
            if (closeLookbackOpt.isEmpty()) continue;
            double cPast = closeLookbackOpt.getAsDouble();
            if (cPast <= 0.0) continue;

            double ret = (cToday / cPast) - 1.0; // fractional return
            sum += ret;
            count++;
        }

        if (count == 0) return Optional.empty();

        double avg = sum / count;
        return Optional.of(ScoreSnapshot.single(avg));
    }
}
