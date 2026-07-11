package com.encorepay.utilities;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class ServerAvailabilityChecker {

    private ServerAvailabilityChecker() {
    }

    /**
     * Checks server availability with retry mechanism.
     * Industry-standard approach: retries with exponential backoff before giving up.
     * 
     * @param url the application URL to check
     * @param timeoutSeconds timeout for each check attempt
     * @param maxRetries maximum number of retry attempts
     * @param retryDelayMillis delay between retries in milliseconds
     * @return ServerStatus indicating if server is reachable
     */
    public static ServerStatus checkWithRetry(String url, int timeoutSeconds, int maxRetries, long retryDelayMillis) {
        if (url == null || url.isBlank()) {
            return ServerStatus.down("Application URL is blank.");
        }

        ServerStatus lastStatus = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            lastStatus = check(url, timeoutSeconds);
            
            if (lastStatus.isReachable()) {
                return lastStatus;
            }
            
            // Don't wait after the last attempt
            if (attempt < maxRetries) {
                try {
                    System.out.println("[SERVER CHECK] Attempt " + attempt + "/" + maxRetries + " failed. Retrying in " + (retryDelayMillis / 1000) + "s...");
                    Thread.sleep(retryDelayMillis);
                    // Exponential backoff: double the delay for next attempt
                    retryDelayMillis *= 2;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return ServerStatus.down("Server check interrupted during retry.");
                }
            }
        }
        
        return lastStatus != null ? lastStatus : ServerStatus.down("Server check failed after " + maxRetries + " attempts.");
    }

    public static ServerStatus check(String url, int timeoutSeconds) {
        if (url == null || url.isBlank()) {
            return ServerStatus.down("Application URL is blank.");
        }

        String normalizedUrl = normalizeUrl(url);
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeoutSeconds))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(normalizedUrl))
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .GET()
            .build();

        try {
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 500) {
                return ServerStatus.up(statusCode, "Server reachable.");
            }
            return ServerStatus.down("Server returned HTTP " + statusCode + ".");
        } catch (IOException e) {
            return ServerStatus.down("Server not reachable: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ServerStatus.down("Server check interrupted.");
        } catch (IllegalArgumentException e) {
            return ServerStatus.down("Invalid application URL: " + url);
        }
    }

    private static String normalizeUrl(String url) {
        String trimmed = url.trim();
        int hashIndex = trimmed.indexOf('#');
        return hashIndex >= 0 ? trimmed.substring(0, hashIndex) : trimmed;
    }

    public static final class ServerStatus {

        private final boolean reachable;
        private final int statusCode;
        private final String message;

        private ServerStatus(boolean reachable, int statusCode, String message) {
            this.reachable = reachable;
            this.statusCode = statusCode;
            this.message = message;
        }

        public static ServerStatus up(int statusCode, String message) {
            return new ServerStatus(true, statusCode, message);
        }

        public static ServerStatus down(String message) {
            return new ServerStatus(false, -1, message);
        }

        public boolean isReachable() {
            return reachable;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getMessage() {
            return message;
        }
    }
}