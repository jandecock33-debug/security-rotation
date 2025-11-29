
package com.example.momentum;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StooqCsvLoader {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Load a Stooq daily CSV (Date,Open,High,Low,Close,Volume).
     */
    public static EtfHistory load(String symbol, Path csvPath) throws IOException {
        List<PriceBar> bars = new ArrayList<>();

        try (var lines = Files.lines(csvPath)) {
            lines.skip(1).forEach(line -> {
                String[] parts = line.split(",");
                if (parts.length < 6) return;
                String dateStr = parts[0].trim();
                String openStr = parts[1].trim();
                String highStr = parts[2].trim();
                String lowStr = parts[3].trim();
                String closeStr = parts[4].trim();
                String volStr = parts[5].trim();

                if (dateStr.isEmpty() || closeStr.isEmpty() || "null".equalsIgnoreCase(closeStr)) return;

                LocalDate date = LocalDate.parse(dateStr, DATE_FORMAT);
                double open = Double.parseDouble(openStr);
                double high = Double.parseDouble(highStr);
                double low = Double.parseDouble(lowStr);
                double close = Double.parseDouble(closeStr);
                double volume = volStr.isEmpty() ? 0.0 : Double.parseDouble(volStr);

                bars.add(new PriceBar(date, open, high, low, close, volume));
            });
        }

        return new EtfHistory(symbol, bars);
    }
}
