
package com.example.momentum;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class DateUtils {

    public static List<LocalDate> monthEndTradingDays(EtfHistory reference) {
        Map<YearMonth, LocalDate> monthToLastTradingDay = new HashMap<>();

        for (PriceBar bar : reference.getBars()) {
            YearMonth ym = YearMonth.from(bar.date());
            monthToLastTradingDay.merge(ym, bar.date(),
                    (oldDate, newDate) -> newDate.isAfter(oldDate) ? newDate : oldDate);
        }

        return monthToLastTradingDay.values().stream()
                                    .sorted()
                                    .collect(Collectors.toList());
    }
}
