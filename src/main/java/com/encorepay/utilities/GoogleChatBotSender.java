package com.encorepay.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

public final class GoogleChatBotSender {

    private static final String CHAT_SCOPE = "https://www.googleapis.com/auth/chat.messages";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    private GoogleChatBotSender() {
    }

    public static void sendMessageWithAttachment(
            String spaceName,
            String messageText,
            String filePath,
            String serviceAccountCredentialsPath) throws IOException {

        if (spaceName == null || spaceName.isBlank()) {
            throw new IllegalArgumentException("Google Chat space name must not be blank.");
        }
        if (messageText == null || messageText.isBlank()) {
            throw new IllegalArgumentException("Google Chat message text must not be blank.");
        }
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Attachment file path must not be blank.");
        }
        if (serviceAccountCredentialsPath == null || serviceAccountCredentialsPath.isBlank()) {
            throw new IllegalArgumentException("Service account credentials path must not be blank.");
        }

        File attachmentFile = new File(filePath);
        if (!attachmentFile.exists() || !attachmentFile.isFile()) {
            throw new IllegalArgumentException("Attachment file not found: " + filePath);
        }

        String accessToken = getAccessToken(serviceAccountCredentialsPath);
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

        String uploadResponse = uploadAttachment(client, accessToken, spaceName, attachmentFile);
        postMessageWithAttachment(client, accessToken, spaceName, messageText, uploadResponse);
    }

    private static String getAccessToken(String serviceAccountCredentialsPath) throws IOException {
        try (FileInputStream stream = new FileInputStream(serviceAccountCredentialsPath)) {
            GoogleCredential credential = GoogleCredential.fromStream(stream)
                .createScoped(List.of(CHAT_SCOPE));
            if (!credential.refreshToken()) {
                throw new IOException("Unable to refresh Google Chat access token.");
            }
            String token = credential.getAccessToken();
            if (token == null || token.isBlank()) {
                throw new IOException("Google Chat access token is empty.");
            }
            return token;
        }
    }

    private static String uploadAttachment(
            HttpClient client,
            String accessToken,
            String spaceName,
            File file) throws IOException {

        String boundary = "Boundary-" + UUID.randomUUID();
        String encodedSpace = URLEncoder.encode(spaceName, StandardCharsets.UTF_8);
        String uploadUrl = "https://chat.googleapis.com/upload/v1/media/"
            + encodedSpace + "?uploadType=multipart";

        String mimeType = probeMimeType(file);
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        byte[] requestBody = buildMultipartBody(boundary, file.getName(), mimeType, fileBytes);

        HttpRequest uploadRequest = HttpRequest.newBuilder()
            .uri(URI.create(uploadUrl))
            .timeout(REQUEST_TIMEOUT)
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "multipart/related; boundary=" + boundary)
            .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
            .build();

        HttpResponse<String> uploadResponse;
        try {
            uploadResponse = client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Google Chat attachment upload interrupted.", e);
        }

        if (uploadResponse.statusCode() < 200 || uploadResponse.statusCode() >= 300) {
            throw new IOException("Google Chat attachment upload failed. HTTP "
                + uploadResponse.statusCode() + " - " + uploadResponse.body());
        }

        String body = uploadResponse.body();
        if (body == null || body.isBlank()) {
            throw new IOException("Google Chat upload response is empty.");
        }
        return body;
    }

    private static void postMessageWithAttachment(
            HttpClient client,
            String accessToken,
            String spaceName,
            String messageText,
            String uploadResponseJson) throws IOException {

        String encodedSpace = URLEncoder.encode(spaceName, StandardCharsets.UTF_8);
        String createMessageUrl = "https://chat.googleapis.com/v1/" + encodedSpace + "/messages";

        String messageBody = "{"
            + "\"text\":\"" + escapeJson(messageText) + "\","
            + "\"attachment\":[" + uploadResponseJson + "]"
            + "}";

        HttpRequest messageRequest = HttpRequest.newBuilder()
            .uri(URI.create(createMessageUrl))
            .timeout(REQUEST_TIMEOUT)
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(messageBody))
            .build();

        HttpResponse<String> messageResponse;
        try {
            messageResponse = client.send(messageRequest, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Google Chat message send interrupted.", e);
        }

        if (messageResponse.statusCode() < 200 || messageResponse.statusCode() >= 300) {
            throw new IOException("Google Chat message send failed. HTTP "
                + messageResponse.statusCode() + " - " + messageResponse.body());
        }
    }

    private static byte[] buildMultipartBody(
            String boundary,
            String fileName,
            String mimeType,
            byte[] fileBytes) {

        String metadataPart = "--" + boundary + "\r\n"
            + "Content-Type: application/json; charset=UTF-8\r\n\r\n"
            + "{\"filename\":\"" + escapeJson(fileName) + "\"}\r\n";

        String fileHeader = "--" + boundary + "\r\n"
            + "Content-Type: " + mimeType + "\r\n"
            + "Content-Transfer-Encoding: binary\r\n\r\n";

        String closing = "\r\n--" + boundary + "--\r\n";

        byte[] metadataBytes = metadataPart.getBytes(StandardCharsets.UTF_8);
        byte[] fileHeaderBytes = fileHeader.getBytes(StandardCharsets.UTF_8);
        byte[] closingBytes = closing.getBytes(StandardCharsets.UTF_8);

        byte[] payload = new byte[metadataBytes.length + fileHeaderBytes.length
            + fileBytes.length + closingBytes.length];

        int offset = 0;
        System.arraycopy(metadataBytes, 0, payload, offset, metadataBytes.length);
        offset += metadataBytes.length;
        System.arraycopy(fileHeaderBytes, 0, payload, offset, fileHeaderBytes.length);
        offset += fileHeaderBytes.length;
        System.arraycopy(fileBytes, 0, payload, offset, fileBytes.length);
        offset += fileBytes.length;
        System.arraycopy(closingBytes, 0, payload, offset, closingBytes.length);

        return payload;
    }

    private static String probeMimeType(File file) throws IOException {
        String mime = Files.probeContentType(file.toPath());
        if (mime != null && !mime.isBlank()) {
            return mime;
        }
        return "application/octet-stream";
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
