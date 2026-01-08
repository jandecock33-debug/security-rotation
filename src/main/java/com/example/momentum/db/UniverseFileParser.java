package com.example.momentum.db;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Parses an input file describing which symbols/universes should be processed.
 *
 * - Supports one token per line, or comma-separated tokens.
 * - Lines starting with #, // or ; are treated as comments.
 * - Inline comments are supported: everything after # or // is ignored.
 * - Tokens are uppercased.
 */
public final class UniverseFileParser {

    private UniverseFileParser() {}

    public static Set<String> parse(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("Universe file not found: " + path.toAbsolutePath());
        }

        Set<String> tokens = new LinkedHashSet<>();
        for (String rawLine : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String line = stripInlineComment(rawLine).trim();
            if (line.isEmpty()) continue;
            if (isFullLineComment(line)) continue;

            // allow comma-separated or whitespace-separated
            String normalized = line.replace(',', ' ');
            for (String part : normalized.split("\\s+")) {
                String t = part.trim();
                if (t.isEmpty()) continue;
                if (t.startsWith("#") || t.startsWith("//") || t.startsWith(";")) continue;
                tokens.add(t.toUpperCase(Locale.ROOT));
            }
        }
        return tokens;
    }

    private static boolean isFullLineComment(String line) {
        return line.startsWith("#") || line.startsWith("//") || line.startsWith(";");
    }

    private static String stripInlineComment(String line) {
        if (line == null) return "";
        int hash = line.indexOf('#');
        int slashes = line.indexOf("//");
        int cut = -1;
        if (hash >= 0) cut = hash;
        if (slashes >= 0) cut = (cut < 0) ? slashes : Math.min(cut, slashes);
        return (cut < 0) ? line : line.substring(0, cut);
    }
}
