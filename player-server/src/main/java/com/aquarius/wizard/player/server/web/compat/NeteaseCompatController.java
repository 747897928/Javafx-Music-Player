package com.aquarius.wizard.player.server.web.compat;

import com.aquarius.wizard.player.server.library.BackendOnlineCatalogService;
import com.aquarius.wizard.player.server.library.BackendOnlineCatalogService.CatalogSong;
import com.aquarius.wizard.player.server.library.BackendOnlineCatalogService.FeaturedPlaylist;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Keeps the desktop client on a small NetEase-shaped JSON subset while the
 * Spring Boot backend becomes the real data source.
 */
@RestController
@RequestMapping("/api/compat/netease")
public class NeteaseCompatController {

    private final BackendOnlineCatalogService backendOnlineCatalogService;

    public NeteaseCompatController(final BackendOnlineCatalogService backendOnlineCatalogService) {
        this.backendOnlineCatalogService = Objects.requireNonNull(
            backendOnlineCatalogService,
            "backendOnlineCatalogService must not be null"
        );
    }

    @GetMapping("/personalized")
    public PersonalizedResponse personalized(
        @RequestParam(name = "limit", defaultValue = "18") final int limit,
        final HttpServletRequest request
    ) {
        final List<PersonalizedPlaylistItem> result = this.backendOnlineCatalogService.loadFeaturedPlaylists(limit).stream()
            .map(playlist -> new PersonalizedPlaylistItem(
                playlist.id(),
                playlist.name(),
                resolvePlaylistCoverUrl(playlist, request),
                playlist.copywriter()
            ))
            .toList();
        return new PersonalizedResponse(200, result);
    }

    @GetMapping("/playlist/detail")
    public PlaylistDetailResponse playlistDetail(
        @RequestParam("id") final String playlistId,
        final HttpServletRequest request
    ) {
        return this.backendOnlineCatalogService.loadPlaylist(playlistId)
            .map(playlist -> new PlaylistDetailResponse(200, mapPlaylist(playlist, request)))
            .orElseGet(() -> new PlaylistDetailResponse(
                404,
                new PlaylistResult(playlistId, "未找到歌单", List.of(), "未找到对应歌单。", "", List.of())
            ));
    }

    @GetMapping("/search/get/web")
    public SearchResponse searchSongs(
        @RequestParam(name = "s", defaultValue = "") final String keyword,
        @RequestParam(name = "limit", defaultValue = "20") final int limit,
        final HttpServletRequest request
    ) {
        final List<CompatSong> songs = this.backendOnlineCatalogService.searchSongs(keyword, limit).stream()
            .map(song -> mapSong(song, request))
            .toList();
        return new SearchResponse(200, new SearchResult(songs.size(), songs));
    }

    @GetMapping("/song/lyric")
    public LyricResponse lyric(@RequestParam("id") final String songId) {
        final String lyricText = this.backendOnlineCatalogService.loadLyricText(songId);
        return new LyricResponse(lyricText.isBlank() ? 404 : 200, new LyricPayload(lyricText));
    }

    @GetMapping("/song/enhance/player/url")
    public PlayerUrlResponse playerUrl(
        @RequestParam("ids") final String ids,
        final HttpServletRequest request
    ) {
        final List<PlayerUrlPayload> data = parseSongIds(ids).stream()
            .map(this.backendOnlineCatalogService::findSong)
            .flatMap(Optional::stream)
            .map(song -> new PlayerUrlPayload(song.id(), buildAudioUrl(song, request)))
            .toList();
        return new PlayerUrlResponse(200, data);
    }

    private PlaylistResult mapPlaylist(final FeaturedPlaylist playlist, final HttpServletRequest request) {
        final List<CompatSong> tracks = playlist.songs().stream()
            .map(song -> mapSong(song, request))
            .toList();
        return new PlaylistResult(
            playlist.id(),
            playlist.name(),
            playlist.tags(),
            playlist.description(),
            resolvePlaylistCoverUrl(playlist, request),
            tracks
        );
    }

    private CompatSong mapSong(final CatalogSong song, final HttpServletRequest request) {
        final long durationMillis = Math.max(0L, song.duration().toMillis());
        final ArtistPayload artist = new ArtistPayload(song.artist() == null || song.artist().isBlank() ? "未知歌手" : song.artist());
        final String coverUrl = song.artworkAvailable() ? buildCoverUrl(song, request) : "";
        final AlbumPayload album = new AlbumPayload(song.album() == null || song.album().isBlank() ? "在线曲库" : song.album(), coverUrl);
        return new CompatSong(
            song.id(),
            song.title(),
            durationMillis,
            durationMillis,
            List.of(artist),
            List.of(artist),
            album,
            album
        );
    }

    private String resolvePlaylistCoverUrl(final FeaturedPlaylist playlist, final HttpServletRequest request) {
        return playlist.songs().stream()
            .filter(CatalogSong::artworkAvailable)
            .findFirst()
            .map(song -> buildCoverUrl(song, request))
            .orElse("");
    }

    private String buildAudioUrl(final CatalogSong song, final HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath("/api/files/audio/{songId}")
            .replaceQuery(null)
            .buildAndExpand(song.id())
            .toUriString();
    }

    private String buildCoverUrl(final CatalogSong song, final HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath("/api/files/covers/song/{songId}")
            .replaceQuery(null)
            .buildAndExpand(song.id())
            .toUriString();
    }

    private List<String> parseSongIds(final String ids) {
        if (ids == null || ids.isBlank()) {
            return List.of();
        }
        final String normalized = ids.trim();
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            final String content = normalized.substring(1, normalized.length() - 1).trim();
            if (content.isBlank()) {
                return List.of();
            }
            final List<String> result = new ArrayList<>();
            for (final String item : content.split(",")) {
                final String value = item.replace("\"", "").trim();
                if (!value.isBlank()) {
                    result.add(value);
                }
            }
            return result;
        }
        return List.of(normalized);
    }

    public record PersonalizedResponse(int code, List<PersonalizedPlaylistItem> result) {
    }

    public record PersonalizedPlaylistItem(String id, String name, String picUrl, String copywriter) {
    }

    public record PlaylistDetailResponse(int code, PlaylistResult result) {
    }

    public record PlaylistResult(
        String id,
        String name,
        List<String> tags,
        String description,
        String coverImgUrl,
        List<CompatSong> tracks
    ) {
    }

    public record SearchResponse(int code, SearchResult result) {
    }

    public record SearchResult(int songCount, List<CompatSong> songs) {
    }

    public record LyricResponse(int code, LyricPayload lrc) {
    }

    public record LyricPayload(String lyric) {
    }

    public record PlayerUrlResponse(int code, List<PlayerUrlPayload> data) {
    }

    public record PlayerUrlPayload(String id, String url) {
    }

    public record CompatSong(
        String id,
        String name,
        long dt,
        long duration,
        List<ArtistPayload> ar,
        List<ArtistPayload> artists,
        AlbumPayload al,
        AlbumPayload album
    ) {
    }

    public record ArtistPayload(String name) {
    }

    public record AlbumPayload(String name, String picUrl) {
    }
}
