package com.aquarius.wizard.player.fx.remote;

import com.aquarius.wizard.player.common.json.JacksonUtils;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Small client used by the JavaFX shell to manage backend online-library files.
 */
public final class BackendOnlineLibraryService {

    private static final List<String> IMPORTABLE_SUFFIXES = List.of(".mp3", ".wav", ".m4a", ".flac", ".aac", ".pcm", ".lrc");

    private final HttpClient httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(8))
        .build();

    private final String serverBaseUrl;

    public BackendOnlineLibraryService() {
        this(BackendServerEndpointResolver.resolveBaseUrl());
    }

    BackendOnlineLibraryService(final String serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl;
    }

    public RefreshResult refreshCatalog() {
        final HttpRequest request = HttpRequest.newBuilder(URI.create(this.serverBaseUrl + "/api/online/library/refresh"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .timeout(Duration.ofSeconds(20))
            .header("Accept", "application/json")
            .build();
        final JsonNode dataNode = sendJsonRequest(request).path("data");
        return new RefreshResult(
            dataNode.path("trackCount").asInt(0),
            textOr(dataNode, "synchronizedAtUtc", "")
        );
    }

    public ImportResult importFiles(final List<Path> files) {
        final List<Path> importableFiles = files == null
            ? List.of()
            : files.stream()
                .filter(path -> path != null && Files.isRegularFile(path))
                .filter(this::isImportablePath)
                .toList();
        if (importableFiles.isEmpty()) {
            return ImportResult.empty();
        }

        final String boundary = "----WizardMusicBoxBoundary" + System.nanoTime();
        final HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) URI.create(this.serverBaseUrl + "/api/online/library/import").toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(8_000);
            connection.setReadTimeout(60_000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream outputStream = connection.getOutputStream()) {
                for (final Path file : importableFiles) {
                    writeMultipartPart(outputStream, boundary, file);
                }
                writeUtf8(outputStream, "--" + boundary + "--\r\n");
            }

            final int statusCode = connection.getResponseCode();
            final String responseBody = readResponseBody(connection, statusCode);
            if (statusCode >= 400) {
                throw new IllegalStateException("Backend online import failed with status " + statusCode + ".");
            }
            if (responseBody == null || responseBody.isBlank()) {
                throw new IllegalStateException("Backend online import returned an empty response.");
            }
            final JsonNode dataNode = JacksonUtils.readTree(responseBody).path("data");
            return new ImportResult(
                dataNode.path("importedAudioCount").asInt(0),
                dataNode.path("importedLyricCount").asInt(0),
                toStringList(dataNode.path("storedAudioFiles")),
                toStringList(dataNode.path("storedLyricFiles")),
                toStringList(dataNode.path("ignoredFiles")),
                dataNode.path("trackCount").asInt(0),
                textOr(dataNode, "synchronizedAtUtc", "")
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to import files into the backend online library.", exception);
        }
    }

    private JsonNode sendJsonRequest(final HttpRequest request) {
        try {
            final HttpResponse<String> response = this.httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            if (response.statusCode() >= 400 || response.body() == null || response.body().isBlank()) {
                throw new IllegalStateException("Backend request failed with status " + response.statusCode() + ".");
            }
            return JacksonUtils.readTree(response.body());
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to call backend online library API.", exception);
        }
    }

    private void writeMultipartPart(final OutputStream outputStream, final String boundary, final Path file) throws IOException {
        final String fileName = escapeMultipartFileName(file.getFileName().toString());
        writeUtf8(outputStream, "--" + boundary + "\r\n");
        writeUtf8(
            outputStream,
            "Content-Disposition: form-data; name=\"files\"; filename=\"" + fileName + "\"\r\n"
        );
        writeUtf8(outputStream, "Content-Type: " + resolveContentType(file) + "\r\n\r\n");
        Files.copy(file, outputStream);
        writeUtf8(outputStream, "\r\n");
    }

    private void writeUtf8(final OutputStream outputStream, final String text) throws IOException {
        outputStream.write(text.getBytes(StandardCharsets.UTF_8));
    }

    private String readResponseBody(final HttpURLConnection connection, final int statusCode) throws IOException {
        final InputStream stream = statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (stream == null) {
            return "";
        }
        try (InputStream inputStream = stream) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String resolveContentType(final Path file) throws IOException {
        final String probedContentType = Files.probeContentType(file);
        if (probedContentType != null && !probedContentType.isBlank()) {
            return probedContentType;
        }
        final String lowerCaseName = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (lowerCaseName.endsWith(".lrc")) {
            return "text/plain";
        }
        return "application/octet-stream";
    }

    private boolean isImportablePath(final Path file) {
        final String lowerCaseName = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return IMPORTABLE_SUFFIXES.stream().anyMatch(lowerCaseName::endsWith);
    }

    private List<String> toStringList(final JsonNode arrayNode) {
        if (arrayNode == null || !arrayNode.isArray() || arrayNode.isEmpty()) {
            return List.of();
        }
        final List<String> values = new ArrayList<>();
        for (final JsonNode valueNode : arrayNode) {
            final String value = valueNode.asText("");
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    private String textOr(final JsonNode node, final String fieldName, final String fallback) {
        if (node == null || fieldName == null || fieldName.isBlank()) {
            return fallback;
        }
        final String value = node.path(fieldName).asText("");
        return value == null || value.isBlank() ? fallback : value;
    }

    private String escapeMultipartFileName(final String fileName) {
        return fileName.replace("\\", "_").replace("\"", "'");
    }

    public record RefreshResult(int trackCount, String synchronizedAtUtc) {
    }

    public record ImportResult(
        int importedAudioCount,
        int importedLyricCount,
        List<String> storedAudioFiles,
        List<String> storedLyricFiles,
        List<String> ignoredFiles,
        int trackCount,
        String synchronizedAtUtc
    ) {

        public static ImportResult empty() {
            return new ImportResult(0, 0, List.of(), List.of(), List.of(), 0, "");
        }

        public int importedCount() {
            return this.importedAudioCount + this.importedLyricCount;
        }
    }
}
