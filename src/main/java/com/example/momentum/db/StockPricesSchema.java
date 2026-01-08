package com.example.momentum.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Detects the column naming used by the stock_prices table so the loader can work
 * with slightly different schemas (trade_date vs date, open vs last_open, ...).
 */
public record StockPricesSchema(
        String tableName,
        String symbolColumn,
        String dateColumn,
        String openColumn,
        String highColumn,
        String lowColumn,
        String closeColumn,
        String volumeColumn
) {

    public static StockPricesSchema detect(Connection conn) throws SQLException {
        Set<String> cols = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "select column_name from information_schema.columns where table_schema = 'public' and table_name = 'stock_prices'")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cols.add(rs.getString(1).toLowerCase(Locale.ROOT));
                }
            }
        }
        if (cols.isEmpty()) {
            // try without schema restriction
            try (PreparedStatement ps = conn.prepareStatement(
                    "select column_name from information_schema.columns where table_name = 'stock_prices'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        cols.add(rs.getString(1).toLowerCase(Locale.ROOT));
                    }
                }
            }
        }
        if (cols.isEmpty()) {
            throw new SQLException("Could not detect columns for table stock_prices (table not found?)");
        }

        String symbol = pick(cols, "symbol", "ticker", "code");
        String date = pick(cols, "trade_date", "date", "pricedate", "price_date");
        String open = pick(cols, "open", "last_open", "open_price");
        String high = pick(cols, "high", "last_high", "high_price");
        String low = pick(cols, "low", "last_low", "low_price");
        String close = pick(cols, "close", "last_close", "close_price");
        String volume = pick(cols, "volume", "last_volume", "vol");

        if (symbol == null || date == null || close == null) {
            throw new SQLException("stock_prices table is missing required columns. Found=" + cols);
        }

        // open/high/low/volume are optional in our calculations but we try to load them.
        if (open == null) open = close;
        if (high == null) high = close;
        if (low == null) low = close;
        // volume may not exist in older schemas; loader will use 0.0

        return new StockPricesSchema("stock_prices", symbol, date, open, high, low, close, volume);
    }

    private static String pick(Set<String> cols, String... preferred) {
        for (String p : preferred) {
            if (cols.contains(p.toLowerCase(Locale.ROOT))) return p;
        }
        return null;
    }
}
