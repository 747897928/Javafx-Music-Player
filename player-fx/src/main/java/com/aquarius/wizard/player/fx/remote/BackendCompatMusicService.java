package com.aquarius.wizard.player.fx.remote;

import com.aquarius.wizard.player.common.json.JacksonUtils;
import com.aquarius.wizard.player.model.LyricLine;
import com.aquarius.wizard.player.model.SongSummary;
import com.aquarius.wizard.player.fx.local.LocalAudioMetadataUtils;
import com.aquarius.wizard.player.fx.ui.FxSampleData;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads Spring Boot compat endpoints that temporarily expose a NetEase-like
 * JSON subset while the JavaFX client is switching off the old direct calls.
 *
 * <p>This class deliberately hides most HTTP details from the UI layer. The
 * JavaFX shell asks for "playlists", "songs", or "download this track", and
 * this client is responsible for translating those requests into compat API
 * calls, parsing JSON, and turning the result back into the shared UI model.</p>
 */
public final class BackendCompatMusicService {

    private static final Pattern LRC_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})(?:\\.(\\d{1,3}))?](.*)");

    private final HttpClient httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(8))
        .build();

    private final String serverBaseUrl;

    public BackendCompatMusicService() {
        this(BackendServerEndpointResolver.resolveBaseUrl());
    }

    BackendCompatMusicService(final String serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl;
    }

    public List<FxSampleData.PlaylistDetail> loadFeaturedPlaylists() {
        final JsonNode rootNode = getJsonResponse(this.serverBaseUrl + "/api/compat/netease/personalized?limit=18");
        if (rootNode == null || !rootNode.path("result").isArray()) {
            return List.of();
        }
        final List<FxSampleData.PlaylistDetail> result = new ArrayList<>();
        for (final JsonNode itemNode : rootNode.path("result")) {
            final String playlistId = itemNode.path("id").asText("");
            final String title = textOr(itemNode, "name", "在线曲库");
            final String copywriter = textOr(itemNode, "copywriter", "在线推荐内容");
            final String imageUrl = textOr(itemNode, "picUrl", "");
            if (playlistId.isBlank() || title.isBlank()) {
                continue;
            }
            result.add(new FxSampleData.PlaylistDetail(
                title,
                "在线 / 推荐",
                copywriter.isBlank() ? "在线推荐内容" : copywriter,
                colorFromSeed(title),
                coverMark(title),
                List.of(),
                "backend-compat",
                playlistId,
                imageUrl
            ));
        }
        return result;
    }

    public FxSampleData.PlaylistDetail loadPlaylist(final String playlistId) {
        final JsonNode rootNode = getJsonResponse(
            this.serverBaseUrl + "/api/compat/netease/playlist/detail?id=" + encode(playlistId)
        );
        if (rootNode == null || rootNode.path("result").isMissingNode()) {
            return emptyCompatPlaylist("歌单加载失败", playlistId);
        }
        final JsonNode resultNode = rootNode.path("result");
        final String title = textOr(resultNode, "name", "在线曲库");
        final String tags = joinTextNodes(resultNode.path("tags"));
        final String description = textOr(resultNode, "description", "该歌单暂无详细介绍。").replace("\n", " ").trim();
        final String coverImageUrl = textOr(resultNode, "coverImgUrl", "");
        final List<SongSummary> songs = mapSongs(resultNode.path("tracks"));
        return new FxSampleData.PlaylistDetail(
            title,
            tags.isBlank() ? "在线 / 推荐" : tags,
            description.isBlank() ? "该歌单暂无详细介绍。" : description,
            colorFromSeed(title),
            coverMark(title),
            songs,
            "backend-compat",
            playlistId,
            coverImageUrl
        );
    }

    public FxSampleData.PlaylistDetail searchSongs(final String keyword) {
        final JsonNode rootNode = getJsonResponse(
            this.serverBaseUrl + "/api/compat/netease/search/get/web?s=" + encode(keyword)
                + "&type=1&offset=0&total=true&limit=20"
        );
        if (rootNode == null) {
            return emptyCompatPlaylist(keyword, "search");
        }
        final List<SongSummary> songs = mapSongs(rootNode.path("result").path("songs"));
        return new FxSampleData.PlaylistDetail(
            keyword,
            "在线搜索结果",
            songs.isEmpty() ? "未搜索到匹配歌曲。" : "为你整理的在线搜索结果。",
            colorFromSeed(keyword),
            coverMark(keyword),
            songs,
            "backend-compat-search",
            keyword,
            ""
        );
    }

    public SongSummary loadLyrics(final SongSummary songSummary) {
        if (songSummary == null || songSummary.sourceId() == null || songSummary.sourceId().isBlank()) {
            return songSummary;
        }
        final String lyricText = loadLyricText(songSummary);
        if (lyricText.isBlank()) {
            return songSummary;
        }
        final List<LyricLine> lyricLines = parseLyrics(lyricText, songSummary);
        return songSummary.withLyricLines(lyricLines);
    }

    public String loadLyricText(final SongSummary songSummary) {
        if (songSummary == null || songSummary.sourceId() == null || songSummary.sourceId().isBlank()) {
            return "";
        }
        final JsonNode rootNode = getJsonResponse(
            this.serverBaseUrl + "/api/compat/netease/song/lyric?id=" + encode(songSummary.sourceId()) + "&lv=1&kv=1&tv=-1"
        );
        return rootNode == null ? "" : rootNode.path("lrc").path("lyric").asText("");
    }

    public boolean isBackendManagedSong(final SongSummary songSummary) {
        return songSummary != null
            && songSummary.mediaSource() != null
            && songSummary.mediaSource().startsWith(this.serverBaseUrl + "/api/files/audio/");
    }

    public DownloadResult downloadSongToLocalLibrary(
        final SongSummary songSummary,
        final Path musicDirectory,
        final Path lyricDirectory
    ) {
        if (songSummary == null) {
            return DownloadResult.failed("当前没有可下载的歌曲。");
        }
        try {
            Files.createDirectories(musicDirectory);
            Files.createDirectories(lyricDirectory);
            final String resolvedMediaSource = resolveDownloadMediaSource(songSummary);
            if (resolvedMediaSource.isBlank()) {
                return DownloadResult.failed("当前歌曲没有可用下载地址。");
            }

            final HttpRequest downloadRequest = HttpRequest.newBuilder(URI.create(resolvedMediaSource))
                .GET()
                .timeout(Duration.ofMinutes(2))
                .build();
            final HttpResponse<InputStream> downloadResponse = this.httpClient.send(
                downloadRequest,
                HttpResponse.BodyHandlers.ofInputStream()
            );
            if (downloadResponse.statusCode() >= 400) {
                try (InputStream ignored = downloadResponse.body()) {
                    return DownloadResult.failed("下载音频失败，状态码：" + downloadResponse.statusCode());
                }
            }

            final String fileStem = sanitizeFileName(buildDownloadStem(songSummary));
            final String extension = resolveAudioExtension(downloadResponse.headers());
            final Path targetMusicFile = resolveAvailablePath(musicDirectory, fileStem, extension);
            final Path temporaryFile = Files.createTempFile(musicDirectory, "wizard-download-", ".part");
            // Always stream into a temporary file first so the local library
            // never sees a half-written track if the network drops mid-copy.
            try (InputStream inputStream = downloadResponse.body()) {
                Files.copy(inputStream, temporaryFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception exception) {
                Files.deleteIfExists(temporaryFile);
                throw exception;
            }
            try {
                Files.move(temporaryFile, targetMusicFile, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temporaryFile);
            }

            String resultMessage = "下载成功";
            // Persist the song title / artist / album / cover into the audio
            // file itself. This keeps the local-library view and any external
            // music player aligned with the same metadata after download.
            final LocalAudioMetadataUtils.AudioTagWriteResult tagWriteResult = LocalAudioMetadataUtils.writeMetadata(
                targetMusicFile,
                firstNonBlank(songSummary.title(), stripExtension(targetMusicFile.getFileName().toString())),
                songSummary.artist(),
                songSummary.album(),
                songSummary.artworkUrl()
            );
            if (!tagWriteResult.message().isBlank()) {
                resultMessage = tagWriteResult.success()
                    ? "下载成功，" + tagWriteResult.message()
                    : "下载成功，但" + tagWriteResult.message();
            }

            Path targetLyricFile = null;
            final String lyricText = loadLyricText(songSummary);
            if (!lyricText.isBlank()) {
                try {
                    // Lyrics are stored next to local music by stem so the
                    // existing local-library scanner can discover them without
                    // any special online-module logic.
                    targetLyricFile = lyricDirectory.resolve(stripExtension(targetMusicFile.getFileName().toString()) + ".lrc");
                    Files.writeString(targetLyricFile, lyricText, StandardCharsets.UTF_8);
                } catch (Exception exception) {
                    resultMessage += "，歌词未能保存：" + safeMessage(exception.getMessage());
                    targetLyricFile = null;
                }
            }
            return new DownloadResult(true, resultMessage, targetMusicFile, targetLyricFile);
        } catch (Exception exception) {
            return DownloadResult.failed("下载失败：" + safeMessage(exception.getMessage()));
        }
    }

    private List<SongSummary> mapSongs(final JsonNode songsNode) {
        if (songsNode == null || !songsNode.isArray() || songsNode.isEmpty()) {
            return List.of();
        }
        final List<JsonNode> trackNodes = new ArrayList<>();
        final List<String> songIds = new ArrayList<>();
        for (final JsonNode songNode : songsNode) {
            trackNodes.add(songNode);
            songIds.add(songNode.path("id").asText(""));
        }
        final Map<String, String> playerUrls = resolvePlayerUrls(songIds);

        final List<SongSummary> songs = new ArrayList<>();
        for (final JsonNode songNode : trackNodes) {
            final String songId = songNode.path("id").asText("");
            final String title = textOr(songNode, "name", "未知歌曲");
            final String artist = firstArtist(songNode);
            final String album = firstAlbum(songNode);
            final String artworkUrl = firstArtwork(songNode);
            final long durationMillis = songNode.path("duration").asLong(songNode.path("dt").asLong(0L));
            songs.add(new SongSummary(
                title,
                artist,
                album,
                Duration.ofMillis(Math.max(0L, durationMillis)),
                colorFromSeed(title + artist),
                fallbackLyrics(title, artist, album),
                "Online Module",
                playerUrls.getOrDefault(songId, ""),
                "backend-compat",
                songId,
                artworkUrl
            ));
        }
        return songs;
    }

    private Map<String, String> resolvePlayerUrls(final List<String> songIds) {
        final List<String> validSongIds = songIds.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(id -> !id.isBlank())
            .distinct()
            .toList();
        if (validSongIds.isEmpty()) {
            return Map.of();
        }
        final String ids = "[" + validSongIds.stream()
            .map(id -> "\"" + id + "\"")
            .reduce((left, right) -> left + "," + right)
            .orElse("") + "]";
        final JsonNode rootNode = getJsonResponse(
            this.serverBaseUrl + "/api/compat/netease/song/enhance/player/url?ids=" + encode(ids) + "&br=128000"
        );
        if (rootNode == null || !rootNode.path("data").isArray()) {
            return Map.of();
        }
        final Map<String, String> urlMap = new HashMap<>();
        for (final JsonNode dataNode : rootNode.path("data")) {
            final String id = dataNode.path("id").asText("");
            final String url = dataNode.path("url").asText("");
            if (!id.isBlank() && !url.isBlank()) {
                urlMap.put(id, url);
            }
        }
        return urlMap;
    }

    private List<LyricLine> parseLyrics(final String lyricText, final SongSummary songSummary) {
        if (lyricText == null || lyricText.isBlank()) {
            return fallbackLyrics(songSummary.title(), songSummary.artist(), songSummary.album());
        }
        final List<LyricLine> lyricLines = new ArrayList<>();
        for (final String row : lyricText.split("\\R")) {
            final Matcher matcher = LRC_PATTERN.matcher(row.trim());
            if (!matcher.matches()) {
                continue;
            }
            final int minutes = Integer.parseInt(matcher.group(1));
            final int seconds = Integer.parseInt(matcher.group(2));
            final String millisText = matcher.group(3);
            int millis = 0;
            if (millisText != null && !millisText.isBlank()) {
                millis = Integer.parseInt((millisText + "00").substring(0, 3));
            }
            final String content = matcher.group(4).trim();
            lyricLines.add(new LyricLine(
                Duration.ofMinutes(minutes).plusSeconds(seconds).plusMillis(millis),
                content.isBlank() ? "..." : content
            ));
        }
        return lyricLines.isEmpty() ? fallbackLyrics(songSummary.title(), songSummary.artist(), songSummary.album()) : lyricLines;
    }

    private List<LyricLine> fallbackLyrics(final String title, final String artist, final String album) {
        return List.of(
            new LyricLine(Duration.ZERO, title),
            new LyricLine(Duration.ofSeconds(8), artist.isBlank() ? "未知歌手" : artist),
            new LyricLine(Duration.ofSeconds(16), album.isBlank() ? "在线曲库" : album),
            new LyricLine(Duration.ofSeconds(24), "当前歌词由在线音乐服务提供。")
        );
    }

    private FxSampleData.PlaylistDetail emptyCompatPlaylist(final String title, final String playlistId) {
        return new FxSampleData.PlaylistDetail(
            title,
            "在线服务",
            "当前未能加载在线数据。",
            colorFromSeed(title),
            coverMark(title),
            List.of(),
            "backend-compat",
            playlistId == null ? "" : playlistId,
            ""
        );
    }

    private JsonNode getJsonResponse(final String uri) {
        final HttpRequest request = HttpRequest.newBuilder(URI.create(uri))
            .GET()
            .timeout(Duration.ofSeconds(15))
            .header("Accept", "application/json")
            .build();
        try {
            final HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400 || response.body() == null || response.body().isBlank()) {
                return null;
            }
            return JacksonUtils.readTree(response.body());
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }

    private String firstArtist(final JsonNode songNode) {
        final JsonNode artistsNode = songNode.path("artists").isArray() ? songNode.path("artists") : songNode.path("ar");
        if (!artistsNode.isArray() || artistsNode.isEmpty()) {
            return "未知歌手";
        }
        return textOr(artistsNode.get(0), "name", "未知歌手");
    }

    private String firstAlbum(final JsonNode songNode) {
        final JsonNode albumNode = songNode.path("album").isObject() ? songNode.path("album") : songNode.path("al");
        if (!albumNode.isObject()) {
            return "在线曲库";
        }
        return textOr(albumNode, "name", "在线曲库");
    }

    private String firstArtwork(final JsonNode songNode) {
        final JsonNode albumNode = songNode.path("album").isObject() ? songNode.path("album") : songNode.path("al");
        if (!albumNode.isObject()) {
            return "";
        }
        return textOr(albumNode, "picUrl", "");
    }

    private String joinTextNodes(final JsonNode textNodes) {
        if (textNodes == null || !textNodes.isArray() || textNodes.isEmpty()) {
            return "";
        }
        final List<String> values = new ArrayList<>();
        for (final JsonNode textNode : textNodes) {
            final String text = textNode.asText("");
            if (!text.isBlank()) {
                values.add(text);
            }
        }
        return String.join(" / ", values);
    }

    private String textOr(final JsonNode node, final String fieldName, final String fallback) {
        if (node == null || fieldName == null || fieldName.isBlank()) {
            return fallback;
        }
        final String value = node.path(fieldName).asText("");
        return value == null || value.isBlank() ? fallback : value;
    }

    private String colorFromSeed(final String seed) {
        final String[] palette = {"#4d8fff", "#ff8d7a", "#8d78ff", "#2dc8aa", "#ffc95c", "#ff6d98", "#57b5ff"};
        final int hash = Math.abs(Objects.requireNonNullElse(seed, "").hashCode());
        return palette[hash % palette.length];
    }

    private String coverMark(final String title) {
        if (title == null || title.isBlank()) {
            return "乐";
        }
        return title.substring(0, 1).toUpperCase(Locale.ROOT);
    }

    private String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String resolveDownloadMediaSource(final SongSummary songSummary) {
        final String mediaSource = songSummary.mediaSource();
        if (mediaSource != null && !mediaSource.isBlank()) {
            return mediaSource;
        }
        if (songSummary.sourceId() == null || songSummary.sourceId().isBlank()) {
            return "";
        }
        return resolvePlayerUrls(List.of(songSummary.sourceId())).getOrDefault(songSummary.sourceId(), "");
    }

    private String buildDownloadStem(final SongSummary songSummary) {
        final String title = firstNonBlank(songSummary.title(), firstNonBlank(songSummary.sourceId(), "backend-track"));
        if (songSummary.artist() == null || songSummary.artist().isBlank()) {
            return title;
        }
        return title + "-" + songSummary.artist();
    }

    private Path resolveAvailablePath(final Path directory, final String fileStem, final String extension) {
        Path candidate = directory.resolve(fileStem + extension);
        if (!Files.exists(candidate)) {
            return candidate;
        }
        int index = 1;
        while (true) {
            candidate = directory.resolve(fileStem + " (" + index + ")" + extension);
            if (!Files.exists(candidate)) {
                return candidate;
            }
            index++;
        }
    }

    private String resolveAudioExtension(final HttpHeaders headers) {
        // The backend streaming endpoint intentionally avoids sending a
        // filename-based Content-Disposition header because Tomcat 11 rejects
        // non-ASCII values such as Chinese song names. Content-Type is the
        // primary signal now; filename parsing remains as a fallback for
        // external or older backends that may still include an ASCII header.
        final String contentType = headers.firstValue("Content-Type").orElse("").toLowerCase(Locale.ROOT);
        final String extensionFromContentType = resolveAudioExtensionFromContentType(contentType);
        if (!extensionFromContentType.isBlank()) {
            return extensionFromContentType;
        }

        final String contentDisposition = headers.firstValue("Content-Disposition").orElse("");
        final String headerFileName = parseHeaderFileName(contentDisposition);
        final int lastDot = headerFileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < headerFileName.length() - 1) {
            return headerFileName.substring(lastDot);
        }
        return ".mp3";
    }

    private String resolveAudioExtensionFromContentType(final String contentType) {
        if (contentType.contains("flac")) {
            return ".flac";
        }
        if (contentType.contains("x-wav") || contentType.contains("wav")) {
            return ".wav";
        }
        if (contentType.contains("aac")) {
            return ".aac";
        }
        if (contentType.contains("mp4") || contentType.contains("m4a")) {
            return ".m4a";
        }
        if (contentType.contains("mpeg")) {
            return ".mp3";
        }
        return "";
    }

    private String parseHeaderFileName(final String contentDisposition) {
        if (contentDisposition == null || contentDisposition.isBlank()) {
            return "";
        }
        for (final String segment : contentDisposition.split(";")) {
            final String trimmedSegment = segment.trim();
            if (trimmedSegment.startsWith("filename=")) {
                return trimmedSegment.substring("filename=".length()).replace("\"", "").trim();
            }
        }
        return "";
    }

    private String sanitizeFileName(final String value) {
        final String sanitized = value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return sanitized.isBlank() ? "backend-track" : sanitized;
    }

    private String stripExtension(final String fileName) {
        final int lastDot = fileName == null ? -1 : fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : firstNonBlank(fileName, "backend-track");
    }

    private String firstNonBlank(final String candidate, final String fallback) {
        return candidate == null || candidate.isBlank() ? fallback : candidate;
    }

    private String safeMessage(final String message) {
        return firstNonBlank(message, "未知原因");
    }

    public record DownloadResult(
        boolean success,
        String message,
        Path musicFile,
        Path lyricFile
    ) {

        public static DownloadResult failed(final String message) {
            return new DownloadResult(false, message, null, null);
        }
    }

}

