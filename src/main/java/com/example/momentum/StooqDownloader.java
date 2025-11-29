
package com.example.momentum;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class StooqDownloader {

    private final HttpClient client = HttpClient.newHttpClient();

    public void downloadDailyCsv(String stooqSymbol, Path targetFile)
            throws IOException, InterruptedException {

        String url = "https://stooq.com/q/d/l/?s=" + stooqSymbol + "&i=d";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        String body = response.body();

        if (response.statusCode() != 200 || !body.startsWith("Date,")) {
            throw new IOException("Stooq did not return CSV (status " + response.statusCode() + "):\n" + body);
        }

        Files.createDirectories(targetFile.getParent());
        Files.writeString(targetFile, body);
    }
}
