
package com.example.momentum;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

public class TechnicalIndicators {

    public static OptionalDouble simpleMovingAverage(EtfHistory history, LocalDate asOfDate, int periodDays) {
        List<PriceBar> bars = history.getBars();
        double sum = 0.0;
        int count = 0;

        for (int i = bars.size() - 1; i >= 0 && count < periodDays; i--) {
            PriceBar bar = bars.get(i);
            if (bar.date().isAfter(asOfDate)) {
                continue;
            }
            sum += bar.close();
            count++;
        }

        if (count < periodDays) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(sum / count);
    }

    /**
     * Wilder-style ATR: average of last N True Range values as of asOfDate.
     */
    public static OptionalDouble atr(EtfHistory history, LocalDate asOfDate, int periodDays) {
        List<PriceBar> bars = history.getBars();
        if (bars.size() < periodDays + 1) {
            return OptionalDouble.empty();
        }

        List<Double> trs = new ArrayList<>();
        PriceBar prev = null;
        for (PriceBar bar : bars) {
            if (bar.date().isAfter(asOfDate)) {
                break;
            }
            if (prev != null) {
                double highLow = bar.high() - bar.low();
                double highPrevClose = Math.abs(bar.high() - prev.close());
                double lowPrevClose = Math.abs(bar.low() - prev.close());
                double tr = Math.max(highLow, Math.max(highPrevClose, lowPrevClose));
                trs.add(tr);
            }
            prev = bar;
        }

        if (trs.size() < periodDays) {
            return OptionalDouble.empty();
        }

        double sum = 0.0;
        int start = trs.size() - periodDays;
        for (int i = start; i < trs.size(); i++) {
            sum += trs.get(i);
        }
        return OptionalDouble.of(sum / periodDays);
    }

    /**
     * ATR% = ATR / close * 100.
     */
    public static OptionalDouble atrPercent(EtfHistory history, LocalDate asOfDate, int periodDays) {
        OptionalDouble atrOpt = atr(history, asOfDate, periodDays);
        OptionalDouble closeOpt = history.getCloseOnOrBefore(asOfDate);
        if (atrOpt.isEmpty() || closeOpt.isEmpty()) {
            return OptionalDouble.empty();
        }
        double close = closeOpt.getAsDouble();
        if (close <= 0.0) return OptionalDouble.empty();
        double atrPct = (atrOpt.getAsDouble() / close) * 100.0;
        return OptionalDouble.of(atrPct);
    }
}
