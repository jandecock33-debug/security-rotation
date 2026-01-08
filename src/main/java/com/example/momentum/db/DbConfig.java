package com.example.momentum.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Small helper to build JDBC connection settings for the "charting" database.
 *
 * Priority order:
 *  1) System properties: CHARTING_DB_URL / CHARTING_DB_USERNAME / CHARTING_DB_PASSWORD
 *  2) Environment variables: CHARTING_DB_URL / CHARTING_DB_USERNAME / CHARTING_DB_PASSWORD
 *  3) Properties file (default: config/charting-db.properties)
 */
public record DbConfig(String url, String username, String password) {

    public static final Path DEFAULT_PROPERTIES_PATH = Path.of("config/charting-db.properties");

    public static DbConfig loadOrThrow(Path propertiesPath) {
        Properties props = new Properties();
        if (propertiesPath != null && Files.exists(propertiesPath)) {
            try (InputStream in = Files.newInputStream(propertiesPath)) {
                props.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read DB properties from " + propertiesPath.toAbsolutePath(), e);
            }
        }

        String url = firstNonBlank(
                System.getProperty("CHARTING_DB_URL"),
                System.getenv("CHARTING_DB_URL"),
                props.getProperty("url"),
                props.getProperty("spring.datasource.url")
        );

        String username = firstNonBlank(
                System.getProperty("CHARTING_DB_USERNAME"),
                System.getenv("CHARTING_DB_USERNAME"),
                props.getProperty("username"),
                props.getProperty("spring.datasource.username")
        );

        String password = firstNonBlank(
                System.getProperty("CHARTING_DB_PASSWORD"),
                System.getenv("CHARTING_DB_PASSWORD"),
                props.getProperty("password"),
                props.getProperty("spring.datasource.password")
        );

        if (isBlank(url) || isBlank(username)) {
            String hint = "Provide CHARTING_DB_URL / CHARTING_DB_USERNAME / CHARTING_DB_PASSWORD " +
                    "as env vars/system properties, or create " +
                    (propertiesPath == null ? DEFAULT_PROPERTIES_PATH : propertiesPath).toAbsolutePath() +
                    " with keys url, username, password.";
            throw new IllegalStateException("Missing DB configuration (url/username). " + hint);
        }
        return new DbConfig(url, username, Objects.toString(password, ""));
    }

    private static String firstNonBlank(String... candidates) {
        for (String c : candidates) {
            if (!isBlank(c)) return c.trim();
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty() || "null".equalsIgnoreCase(s.trim());
    }
}
