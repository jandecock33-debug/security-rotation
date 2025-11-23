package com.example.momentum;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class StooqCsvLoader {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");


  public static EtfHistory load(String symbol, Path csvPath) throws IOException {
    List<PriceBar> bars = new ArrayList<>();

    try (var lines = Files.lines(csvPath)) {
      lines.skip(1).forEach(line -> {
        String[] parts = line.split(",");
        if (parts.length < 5) return;
        String dateStr = parts[0].trim();
        String closeStr = parts[4].trim();  // Close column

        if (dateStr.isEmpty() || closeStr.isEmpty() || "null".equalsIgnoreCase(closeStr)) return;

        LocalDate date = LocalDate.parse(dateStr, DATE_FORMAT);
        double close = Double.parseDouble(closeStr);
        bars.add(new PriceBar(date, close));
      });
    }

    return new EtfHistory(symbol, bars);
  }
}
