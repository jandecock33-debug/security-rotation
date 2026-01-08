package com.example.momentum.db;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Resolves the tokens from the universe file into an actual set of symbols.
 *
 * Supported special tokens:
 *  - SP500 -> all symbols from quotes where origin contains "SP500"
 *  - NASDAQ100 -> all symbols from quotes where origin contains "NASDAQ100"
 *
 * Everything else is treated as an explicit ticker.
 */
public final class UniverseResolver {

    private UniverseResolver() {}

    public static Set<String> resolve(Set<String> tokens, ChartingDbClient db) throws SQLException {
        Set<String> symbols = new LinkedHashSet<>();
        for (String t0 : tokens) {
            if (t0 == null || t0.isBlank()) continue;
            String t = normalizeToken(t0);

            if ("SP500".equalsIgnoreCase(t) || "S&P500".equalsIgnoreCase(t) || "S&P-500".equalsIgnoreCase(t)) {
                symbols.addAll(db.findSymbolsByOrigin("SP500"));
            } else if ("NASDAQ100".equalsIgnoreCase(t) || "NASDAQ-100".equalsIgnoreCase(t) || "QQQ".equalsIgnoreCase(t)) {
                symbols.addAll(db.findSymbolsByOrigin("NASDAQ100"));
            } else {
                symbols.add(t.toUpperCase(Locale.ROOT));
            }
        }
        return symbols;
    }

    /**
     * Normalizes common formats:
     *  - trims
     *  - removes .US suffix
     */
    private static String normalizeToken(String t) {
        String s = t.trim();
        if (s.endsWith(".US") || s.endsWith(".us")) {
            s = s.substring(0, s.length() - 3);
        }
        return s;
    }
}
