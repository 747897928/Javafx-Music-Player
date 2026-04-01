package cn.gxust.player.fx.legacy;

import cn.gxust.player.domain.model.LyricLine;
import cn.gxust.player.domain.model.SongSummary;
import cn.gxust.player.fx.ui.FxSampleData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Temporary legacy online adapter used to migrate the old NetEase-based flows
 * into the rebuilt JavaFX shell before the Spring Boot backend replaces them.
 */
public final class LegacyOnlineMusicService {

    private static final int FEATURED_PLAYLIST_LIMIT = 18;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern LRC_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})(?:\\.(\\d{1,3}))?](.*)");
    private static final String USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0 Safari/537.36";
    private static final String REFERER = "https://music.163.com/";

    private final HttpClient httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(12))
        .build();

    public List<FxSampleData.PlaylistDetail> loadFeaturedPlaylists() {
        final int offset = ThreadLocalRandom.current().nextInt(0, 38) * 35;
        final String html = getTextResponse(
            "https://music.163.com/discover/playlist/?order=hot&cat=全部&limit=35&offset=" + offset
        );
        if (html.isBlank()) {
            return List.of();
        }

        final Document document = Jsoup.parse(html);
        final List<FxSampleData.PlaylistDetail> result = new ArrayList<>();
        for (final Element coverElement : document.select(".u-cover.u-cover-1")) {
            if (result.size() >= FEATURED_PLAYLIST_LIMIT) {
                break;
            }
            final Element anchor = coverElement.selectFirst("a[href*=/playlist?id=]");
            if (anchor == null) {
                continue;
            }
            final String playlistId = anchor.attr("href").replace("/playlist?id=", "").trim();
            final String title = anchor.attr("title").trim();
            if (playlistId.isBlank() || title.isBlank()) {
                continue;
            }
            final String imageUrl = coverElement.selectFirst("img") == null
                ? ""
                : coverElement.selectFirst("img").attr("src").replace("?param=140y140", "?param=300y300");
            result.add(new FxSampleData.PlaylistDetail(
                title,
                "Legacy Online / 推荐歌单",
                "旧版在线推荐歌单，点击后加载歌单详情。",
                colorFromSeed(title),
                coverMark(title),
                List.of(),
                "legacy-online",
                playlistId,
                imageUrl
            ));
        }
        return result;
    }

    public FxSampleData.PlaylistDetail loadPlaylist(final String playlistId) {
        final JsonNode rootNode = getJsonResponse(
            "https://music.163.com/api/playlist/detail?id=" + encode(playlistId)
        );
        if (rootNode == null || rootNode.path("result").isMissingNode()) {
            return emptyLegacyPlaylist("歌单加载失败", playlistId);
        }

        final JsonNode resultNode = rootNode.path("result");
        final String title = textOr(resultNode, "name", "Legacy Playlist");
        final String tags = joinTextNodes(resultNode.path("tags"));
        final String description = textOr(resultNode, "description", "该歌单暂无详细介绍。").replace("\n", " ").trim();
        final String coverImageUrl = textOr(resultNode, "coverImgUrl", "");
        final List<SongSummary> songs = mapSongs(resultNode.path("tracks"));
        return new FxSampleData.PlaylistDetail(
            title,
            tags.isBlank() ? "音乐" : tags,
            description.isBlank() ? "该歌单暂无详细介绍。" : description,
            colorFromSeed(title),
            coverMark(title),
            songs,
            "legacy-online",
            playlistId,
            coverImageUrl
        );
    }

    public FxSampleData.PlaylistDetail searchSongs(final String keyword) {
        final JsonNode rootNode = getJsonResponse(
            "https://music.163.com/api/search/get/web?s=" + encode(keyword)
                + "&type=1&offset=0&total=true&limit=20"
        );
        if (rootNode == null) {
            return emptyLegacyPlaylist(keyword, "search");
        }
        final List<SongSummary> songs = mapSongs(rootNode.path("result").path("songs"));
        return new FxSampleData.PlaylistDetail(
            keyword,
            "Legacy Online / 搜索结果",
            songs.isEmpty() ? "未搜索到可用歌曲。" : "旧版在线搜索结果，当前按歌曲列表方式展示。",
            colorFromSeed(keyword),
            coverMark(keyword),
            songs,
            "legacy-online-search",
            keyword,
            ""
        );
    }

    public SongSummary loadLyrics(final SongSummary songSummary) {
        if (songSummary == null || songSummary.sourceId() == null || songSummary.sourceId().isBlank()) {
            return songSummary;
        }
        final JsonNode rootNode = getJsonResponse(
            "https://music.163.com/api/song/lyric?id=" + encode(songSummary.sourceId()) + "&lv=1&kv=1&tv=-1"
        );
        if (rootNode == null) {
            return songSummary;
        }
        final String lyricText = rootNode.path("lrc").path("lyric").asText("");
        final List<LyricLine> lyricLines = parseLyrics(lyricText, songSummary);
        return songSummary.withLyricLines(lyricLines);
    }

    public DownloadResult downloadSong(
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
            String mediaSource = songSummary.mediaSource();
            if (mediaSource == null || mediaSource.isBlank()) {
                mediaSource = resolvePlayerUrls(List.of(songSummary.sourceId())).getOrDefault(songSummary.sourceId(), "");
            }
            if (mediaSource == null || mediaSource.isBlank()) {
                return DownloadResult.failed("当前歌曲没有可用下载地址。");
            }

            final String fileStem = sanitizeFileName(songSummary.title() + "-" + songSummary.artist());
            final Path targetMusicFile = musicDirectory.resolve(fileStem + ".mp3");
            final HttpRequest musicRequest = baseRequest(mediaSource).GET().build();
            final HttpResponse<Path> downloadResponse = this.httpClient.send(
                musicRequest,
                HttpResponse.BodyHandlers.ofFile(targetMusicFile)
            );
            if (downloadResponse.statusCode() >= 400) {
                return DownloadResult.failed("下载音频失败，状态码：" + downloadResponse.statusCode());
            }

            final SongSummary songWithLyrics = loadLyrics(songSummary);
            if (songWithLyrics.lyricLines() != null && !songWithLyrics.lyricLines().isEmpty()) {
                final Path lyricFile = lyricDirectory.resolve(fileStem + ".lrc");
                final String lyricContent = toLrc(songWithLyrics.lyricLines());
                Files.writeString(lyricFile, lyricContent, StandardCharsets.UTF_8);
            }
            return new DownloadResult(true, "下载成功", targetMusicFile);
        } catch (Exception exception) {
            return DownloadResult.failed("下载失败：" + exception.getMessage());
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
                "Legacy Online",
                playerUrls.getOrDefault(songId, ""),
                "legacy-online",
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
        final String ids = "[" + validSongIds.stream().map(id -> "\"" + id + "\"").reduce((a, b) -> a + "," + b).orElse("") + "]";
        final JsonNode rootNode = getJsonResponse(
            "https://music.163.com/api/song/enhance/player/url?ids=" + encode(ids) + "&br=128000"
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
            new LyricLine(Duration.ofSeconds(16), album.isBlank() ? "在线音乐" : album),
            new LyricLine(Duration.ofSeconds(24), "旧版在线歌词暂未返回，已回退到基础信息。")
        );
    }

    private String toLrc(final List<LyricLine> lyricLines) {
        final StringBuilder builder = new StringBuilder();
        for (final LyricLine lyricLine : lyricLines) {
            final long totalMillis = lyricLine.position().toMillis();
            final long minutes = totalMillis / 60000;
            final long seconds = (totalMillis % 60000) / 1000;
            final long millis = totalMillis % 1000;
            builder.append(String.format(Locale.ROOT, "[%02d:%02d.%03d]%s%n", minutes, seconds, millis, lyricLine.content()));
        }
        return builder.toString();
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
            return "在线音乐";
        }
        return textOr(albumNode, "name", "在线音乐");
    }

    private String firstArtwork(final JsonNode songNode) {
        final JsonNode albumNode = songNode.path("album").isObject() ? songNode.path("album") : songNode.path("al");
        if (!albumNode.isObject()) {
            return "";
        }
        return textOr(albumNode, "picUrl", textOr(albumNode, "blurPicUrl", ""));
    }

    private FxSampleData.PlaylistDetail emptyLegacyPlaylist(final String title, final String sourceId) {
        return new FxSampleData.PlaylistDetail(
            title,
            "Legacy Online",
            "当前未获取到可用在线数据。",
            colorFromSeed(title),
            coverMark(title),
            List.of(),
            "legacy-online",
            sourceId,
            ""
        );
    }

    private JsonNode getJsonResponse(final String url) {
        try {
            final String body = getTextResponse(url);
            return body.isBlank() ? null : OBJECT_MAPPER.readTree(body);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String getTextResponse(final String url) {
        try {
            final HttpRequest request = baseRequest(url).GET().build();
            final HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                return "";
            }
            return response.body();
        } catch (Exception ignored) {
            return "";
        }
    }

    private HttpRequest.Builder baseRequest(final String url) {
        return HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(12))
            .header("User-Agent", USER_AGENT)
            .header("Referer", REFERER)
            .header("Accept", "*/*");
    }

    private String joinTextNodes(final JsonNode jsonNode) {
        if (jsonNode == null || !jsonNode.isArray()) {
            return "";
        }
        final List<String> values = new ArrayList<>();
        for (final JsonNode item : jsonNode) {
            final String value = item.asText("");
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        return String.join(" / ", values);
    }

    private String textOr(final JsonNode jsonNode, final String fieldName, final String fallback) {
        if (jsonNode == null || jsonNode.isMissingNode()) {
            return fallback;
        }
        final String value = jsonNode.path(fieldName).asText("");
        return value == null || value.isBlank() ? fallback : value;
    }

    private String colorFromSeed(final String seed) {
        final String[] palette = {"#5a8dff", "#ff8d7a", "#8d78ff", "#2dc8aa", "#ffc95c", "#ff6d98"};
        final int index = Math.abs(seed.hashCode()) % palette.length;
        return palette[index];
    }

    private String coverMark(final String title) {
        if (title == null || title.isBlank()) {
            return "云";
        }
        return title.substring(0, 1);
    }

    private String sanitizeFileName(final String value) {
        final String sanitized = value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return sanitized.isBlank() ? "legacy-song" : sanitized;
    }

    private String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Download result returned to the JavaFX shell.
     *
     * @param success    whether the download succeeded
     * @param message    user-facing message
     * @param targetPath saved file path
     */
    public record DownloadResult(boolean success, String message, Path targetPath) {

        public static DownloadResult failed(final String message) {
            return new DownloadResult(false, message, null);
        }
    }
}
