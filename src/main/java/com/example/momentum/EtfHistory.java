
package com.example.momentum;

import java.time.LocalDate;
import java.util.*;
import java.util.OptionalDouble;

public class EtfHistory {
    private final String symbol;
    private final List<PriceBar> bars; // sorted ascending by date

    public EtfHistory(String symbol, List<PriceBar> bars) {
        this.symbol = symbol;
        this.bars = new ArrayList<>(bars);
        this.bars.sort(Comparator.comparing(PriceBar::date));
    }

    public String getSymbol() {
        return symbol;
    }

    public List<PriceBar> getBars() {
        return Collections.unmodifiableList(bars);
    }

    public OptionalDouble getCloseOnOrBefore(LocalDate date) {
        Optional<PriceBar> barOpt = bars.stream()
                .filter(b -> !b.date().isAfter(date))
                .max(Comparator.comparing(PriceBar::date));

        if (barOpt.isEmpty()) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(barOpt.get().close());
    }

    public LocalDate getFirstDate() {
        return bars.get(0).date();
    }

    public LocalDate getLastDate() {
        return bars.get(bars.size() - 1).date();
    }
}
