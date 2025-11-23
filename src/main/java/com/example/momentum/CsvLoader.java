
package com.example.momentum;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Simple CSV loader for Yahoo Finance style files using Files.lines and String.split(",").
 * Expects header row and columns: Date,Open,High,Low,Close,Adj Close,Volume
 */
public class CsvLoader {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static EtfHistory loadYahooCsv(String symbol, Path csvPath) throws IOException {
        List<PriceBar> bars = new ArrayList<>();

        try (var lines = Files.lines(csvPath)) {
            lines.skip(1).forEach(line -> {
                String[] parts = line.split(",");
                if (parts.length < 6) return;
                String dateStr = parts[0].trim();
                String adjCloseStr = parts[5].trim();
                if (dateStr.isEmpty() || adjCloseStr.isEmpty() || "null".equalsIgnoreCase(adjCloseStr)) return;

                LocalDate date = LocalDate.parse(dateStr, DATE_FORMAT);
                double close = Double.parseDouble(adjCloseStr);
                bars.add(new PriceBar(date, close));
            });
        }

        return new EtfHistory(symbol, bars);
    }
}
