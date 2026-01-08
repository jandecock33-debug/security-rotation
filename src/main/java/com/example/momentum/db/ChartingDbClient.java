package com.example.momentum.db;

import com.example.momentum.EtfHistory;
import com.example.momentum.PriceBar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Minimal JDBC client to read symbols (from quotes) and daily price bars (from stock_prices)
 * from the "charting" database.
 */
public class ChartingDbClient implements AutoCloseable {

    private final Connection connection;
    private final StockPricesSchema stockPricesSchema;

    public ChartingDbClient(DbConfig config) throws SQLException {
        this.connection = DriverManager.getConnection(config.url(), config.username(), config.password());
        this.stockPricesSchema = StockPricesSchema.detect(connection);
    }

    public List<String> findSymbolsByOrigin(String originKey) throws SQLException {
        // origin is stored on quotes.origin, possibly as CSV-like text: "SP500" or "SP500,NASDAQ100".
        String sql = "select symbol from quotes where origin ilike ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + originKey + "%");
            try (ResultSet rs = ps.executeQuery()) {
                Set<String> result = new HashSet<>();
                while (rs.next()) {
                    String sym = rs.getString(1);
                    if (sym != null && !sym.isBlank()) {
                        result.add(sym.trim().toUpperCase(Locale.ROOT));
                    }
                }
                return result.stream().sorted().toList();
            }
        }
    }

    public EtfHistory loadHistory(String symbol, LocalDate startDateInclusive) throws SQLException {
        String symCol = stockPricesSchema.symbolColumn();
        String dateCol = stockPricesSchema.dateColumn();
        String openCol = stockPricesSchema.openColumn();
        String highCol = stockPricesSchema.highColumn();
        String lowCol = stockPricesSchema.lowColumn();
        String closeCol = stockPricesSchema.closeColumn();
        String volumeCol = stockPricesSchema.volumeColumn();

        String volumeSelect = (volumeCol == null) ? "0 as volume" : volumeCol + " as volume";

        String sql = "select " + dateCol + " as d, " +
                openCol + " as o, " +
                highCol + " as h, " +
                lowCol + " as l, " +
                closeCol + " as c, " +
                volumeSelect +
                " from " + stockPricesSchema.tableName() +
                " where " + symCol + " = ?" +
                (startDateInclusive != null ? (" and " + dateCol + " >= ?") : "") +
                " order by " + dateCol;

        List<PriceBar> bars = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, symbol);
            if (startDateInclusive != null) {
                ps.setObject(2, startDateInclusive);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate d = rs.getObject("d", LocalDate.class);
                    if (d == null) continue;
                    double o = rs.getDouble("o");
                    double h = rs.getDouble("h");
                    double l = rs.getDouble("l");
                    double c = rs.getDouble("c");
                    double v = rs.getDouble("volume");
                    bars.add(new PriceBar(d, o, h, l, c, v));
                }
            }
        }
        return new EtfHistory(symbol, bars);
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (Exception ignored) {
        }
    }
}
