
package com.example.momentum;

import java.time.LocalDate;
import java.util.OptionalDouble;

/**
 * Computes a combined relative-strength / momentum score using one or more lookback windows.
 * If you pass a single lookback (e.g. 126 days) this reduces to classic 1-period momentum.
 * If you pass multiple lookbacks (e.g. 63/126/252) their simple average is used.
 */
public class RelativeStrengthCalculator {

    private final int[] lookbackDays; // e.g. {63, 126, 252} for 3/6/12 months

    public RelativeStrengthCalculator(int... lookbackDays) {
        if (lookbackDays == null || lookbackDays.length == 0) {
            throw new IllegalArgumentException("At least one lookback is required");
        }
        this.lookbackDays = lookbackDays;
    }

    public OptionalDouble computeRelativeStrength(EtfHistory history, LocalDate asOfDate) {
        OptionalDouble closeTodayOpt = history.getCloseOnOrBefore(asOfDate);
        if (closeTodayOpt.isEmpty()) return OptionalDouble.empty();
        double cToday = closeTodayOpt.getAsDouble();

        double sum = 0.0;
        int count = 0;

        for (int lb : lookbackDays) {
            LocalDate lookbackDate = asOfDate.minusDays(lb);
            OptionalDouble closeLookbackOpt = history.getCloseOnOrBefore(lookbackDate);
            if (closeLookbackOpt.isEmpty()) {
                continue;
            }
            double cPast = closeLookbackOpt.getAsDouble();
            if (cPast <= 0.0) continue;

            double ret = (cToday / cPast) - 1.0;
            sum += ret;
            count++;
        }

        if (count == 0) return OptionalDouble.empty();

        double avg = sum / count;
        return OptionalDouble.of(avg);
    }
}
