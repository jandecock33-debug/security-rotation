
package com.example.momentum;

import java.time.LocalDate;
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
}
