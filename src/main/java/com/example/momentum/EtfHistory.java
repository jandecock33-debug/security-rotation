
package com.example.momentum;

import java.time.LocalDate;
import java.util.*;

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

    public OptionalDouble getCloseOn(LocalDate date) {
        return bars.stream()
                   .filter(b -> b.date().equals(date))
                   .mapToDouble(PriceBar::close)
                   .findFirst();
    }

    public OptionalDouble getCloseOnOrBefore(LocalDate date) {
      return bars.stream()
          .filter(b -> !b.date().isAfter(date))
          .max(Comparator.comparing(PriceBar::date))
          .stream()
          .mapToDouble(PriceBar::close)
          .findFirst();
    }

    public LocalDate getFirstDate() {
        return bars.get(0).date();
    }

    public LocalDate getLastDate() {
        return bars.get(bars.size() - 1).date();
    }
}
