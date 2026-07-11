package com.encorepay.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import com.encorepay.config.ConfigLoader;

public class DownloadFileUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String DEFAULT_DOWNLOAD_DIRECTORY = "downloads";

    private final ConfigLoader config;
    private final Path downloadRootPath;

    public DownloadFileUtil() {
        this.config = ConfigLoader.getInstance();
        this.downloadRootPath = resolveDownloadRootPath();
    }

    public String createTodayFolder() throws IOException {
        LocalDate today = LocalDate.now();
        Path todayFolder = downloadRootPath.resolve(today.format(DATE_FORMATTER));
        Files.createDirectories(todayFolder);
        System.out.println("[INFO] Creating folder: " + todayFolder);
        return todayFolder.toString();
    }

    public Path downloadFileDirectly(String sourceUrl, Path targetFile) throws IOException {
        Files.createDirectories(targetFile.getParent());

        System.out.println("[INFO] Direct HTTP download started for: " + sourceUrl);

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(sourceUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();

        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Direct download failed with HTTP status " + response.statusCode());
            }

            try (InputStream stream = response.body()) {
                long bytesCopied = Files.copy(stream, targetFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[INFO] Direct HTTP download completed: " + targetFile.toAbsolutePath() + " (" + bytesCopied + " bytes)");
            }

            if (Files.size(targetFile) == 0) {
                throw new IOException("Downloaded file is empty; the server may have returned an error page.");
            }

            return targetFile;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Direct HTTP download interrupted", e);
        }
    }

    public boolean waitForBrowserDownloadStart(long timeoutMillis) throws IOException {
        Path downloadFolder = resolveDownloadRootPath();
        Files.createDirectories(downloadFolder);

        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < deadline) {
            if (hasBrowserDownloadArtifact(downloadFolder)) {
                return true;
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Browser download polling interrupted", e);
            }
        }

        return false;
    }

    public Path resolveDownloadRootPath() {
        String configuredPath = config.getProperty("download.path", DEFAULT_DOWNLOAD_DIRECTORY);
        Path path = Paths.get(configuredPath);
        return path.isAbsolute() ? path.normalize() : Paths.get(System.getProperty("user.dir"), configuredPath).normalize();
    }

    public void cleanupDownloadArtifacts() throws IOException {
        Path downloadFolder = resolveDownloadRootPath();
        if (!Files.exists(downloadFolder)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(downloadFolder)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".crdownload")
                            || path.getFileName().toString().endsWith(".part")
                            || path.getFileName().toString().endsWith(".tmp"))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                            // Best-effort cleanup only
                        }
                    });
        }
    }

    private boolean hasBrowserDownloadArtifact(Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(Files::isRegularFile)
                    .anyMatch(path -> path.getFileName().toString().endsWith(".crdownload")
                            || path.getFileName().toString().endsWith(".xml"));
        }
    }
}
