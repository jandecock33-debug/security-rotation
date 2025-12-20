package com.example.momentum;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.*;

/**
 * Resamples daily bars to weekly/monthly bars (O/H/L/C/V aggregation).
 */
public final class TvResampler {

    public enum PeriodType { WEEKLY, MONTHLY }

    public static List<PriceBar> resample(List<PriceBar> dailyAscending, PeriodType type, LocalDate asOfDate) {
        // Filter to <= asOfDate
        List<PriceBar> filtered = dailyAscending.stream()
                .filter(b -> !b.date().isAfter(asOfDate))
                .sorted(Comparator.comparing(PriceBar::date))
                .toList();
        if (filtered.isEmpty()) return List.of();

        Map<String, List<PriceBar>> buckets = new TreeMap<>();
        for (PriceBar b : filtered) {
            String key = bucketKey(b.date(), type);
            buckets.computeIfAbsent(key, k -> new ArrayList<>()).add(b);
        }

        List<PriceBar> out = new ArrayList<>(buckets.size());
        for (List<PriceBar> bucket : buckets.values()) {
            bucket.sort(Comparator.comparing(PriceBar::date));
            PriceBar first = bucket.get(0);
            PriceBar last = bucket.get(bucket.size() - 1);

            double open = first.open();
            double close = last.close();
            double high = bucket.stream().mapToDouble(PriceBar::high).max().orElse(Double.NaN);
            double low = bucket.stream().mapToDouble(PriceBar::low).min().orElse(Double.NaN);
            double vol = bucket.stream().mapToDouble(PriceBar::volume).sum();

            out.add(new PriceBar(last.date(), open, high, low, close, vol));
        }
        return out;
    }

    private static String bucketKey(LocalDate d, PeriodType type) {
        return switch (type) {
            case WEEKLY -> {
                int week = d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                int year = d.get(IsoFields.WEEK_BASED_YEAR);
                yield year + "-W" + String.format("%02d", week);
            }
            case MONTHLY -> d.getYear() + "-" + String.format("%02d", d.getMonthValue());
        };
    }

    private TvResampler() {}
}
