package com.encorepay.utilities;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class GoogleChatWebhookSender {

    private GoogleChatWebhookSender() {
    }

    public static void sendSummary(String webhookUrl, String summary) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            throw new IllegalArgumentException("https://chat.googleapis.com/v1/spaces/AAQAA_atP20/messages?key=AIzaSyDdI0hCZtE6vySjMm-WEfRq3CPzqKqqsHI&token=ct_Bc5j88WN1LpwLZGy5LC4udTY-aFFWZEpjZXP0zQw.");
        }
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException("Monitoring summary must not be blank.");
        }

        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(webhookUrl.trim()))
            .timeout(Duration.ofSeconds(20))
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString("{\"text\":\"" + escapeJson(summary) + "\"}"))
            .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Google Chat webhook failed with HTTP status: " + response.statusCode());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to send monitoring summary to Google Chat.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Google Chat webhook send was interrupted.", e);
        }
    }

    private static String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder();
        for (char ch : value.toCharArray()) {
            switch (ch) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
                    break;
            }
        }
        return escaped.toString();
    }
}
