package com.aquarius.wizard.player.fx.remote;

import com.aquarius.wizard.player.domain.model.LyricLine;
import com.aquarius.wizard.player.domain.model.SongSummary;
import com.aquarius.wizard.player.fx.ui.FxSampleData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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
 */
public final class BackendCompatMusicService {

    private static final String DEFAULT_SERVER_BASE_URL = "http://127.0.0.1:18080";
    private static final String SERVER_BASE_URL_PROPERTY = "wizard.player.server.base-url";
    private static final String SERVER_BASE_URL_ENV = "WIZARD_PLAYER_SERVER_BASE_URL";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern LRC_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})(?:\\.(\\d{1,3}))?](.*)");

    private final HttpClient httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(8))
        .build();

    private final String serverBaseUrl;

    public BackendCompatMusicService() {
        this(resolveServerBaseUrl());
    }

    BackendCompatMusicService(final String serverBaseUrl) {
        this.serverBaseUrl = normalizeBaseUrl(serverBaseUrl);
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
            final String copywriter = textOr(itemNode, "copywriter", "由 Spring Boot 4 在线模块提供。");
            final String imageUrl = textOr(itemNode, "picUrl", "");
            if (playlistId.isBlank() || title.isBlank()) {
                continue;
            }
            result.add(new FxSampleData.PlaylistDetail(
                title,
                "在线 / Spring Boot 4",
                copywriter.isBlank() ? "由 Spring Boot 4 在线模块提供。" : copywriter,
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
            tags.isBlank() ? "在线 / 后端" : tags,
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
            songs.isEmpty() ? "未搜索到匹配歌曲。" : "由 Spring Boot 4 在线模块返回的搜索结果。",
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
        final JsonNode rootNode = getJsonResponse(
            this.serverBaseUrl + "/api/compat/netease/song/lyric?id=" + encode(songSummary.sourceId()) + "&lv=1&kv=1&tv=-1"
        );
        if (rootNode == null) {
            return songSummary;
        }
        final String lyricText = rootNode.path("lrc").path("lyric").asText("");
        final List<LyricLine> lyricLines = parseLyrics(lyricText, songSummary);
        return songSummary.withLyricLines(lyricLines);
    }

    public boolean isBackendManagedSong(final SongSummary songSummary) {
        return songSummary != null
            && songSummary.mediaSource() != null
            && songSummary.mediaSource().startsWith(this.serverBaseUrl + "/api/files/audio/");
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
            new LyricLine(Duration.ofSeconds(24), "当前歌词由 Spring Boot 4 在线模块提供。")
        );
    }

    private FxSampleData.PlaylistDetail emptyCompatPlaylist(final String title, final String playlistId) {
        return new FxSampleData.PlaylistDetail(
            title,
            "后端接管中",
            "当前未能从 Spring Boot 4 在线模块加载数据。",
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
            return OBJECT_MAPPER.readTree(response.body());
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

    private static String resolveServerBaseUrl() {
        final String propertyValue = System.getProperty(SERVER_BASE_URL_PROPERTY);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }
        final String environmentValue = System.getenv(SERVER_BASE_URL_ENV);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }
        return DEFAULT_SERVER_BASE_URL;
    }

    private String normalizeBaseUrl(final String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return DEFAULT_SERVER_BASE_URL;
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
