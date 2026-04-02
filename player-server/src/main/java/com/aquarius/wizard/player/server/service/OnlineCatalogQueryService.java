package com.aquarius.wizard.player.server.service;

import com.aquarius.wizard.player.model.LyricLine;
import com.aquarius.wizard.player.server.support.storage.StorageLayout;
import com.aquarius.wizard.player.server.model.AudioAsset;
import com.aquarius.wizard.player.server.model.BinaryPayload;
import com.aquarius.wizard.player.server.model.CatalogSong;
import com.aquarius.wizard.player.server.model.FeaturedPlaylist;
import com.aquarius.wizard.player.server.repository.OnlineTrackRepository;
import com.aquarius.wizard.player.server.support.metadata.AudioMetadataReader;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Read-side application service for the backend-managed online catalog.
 */
@Service
public class OnlineCatalogQueryService {

    private final OnlineTrackRepository onlineTrackRepository;
    private final OnlineCatalogSynchronizationService onlineCatalogSynchronizationService;
    private final StorageLayout storageLayout;
    private final AudioMetadataReader audioMetadataReader;

    public OnlineCatalogQueryService(
        final OnlineTrackRepository onlineTrackRepository,
        final OnlineCatalogSynchronizationService onlineCatalogSynchronizationService,
        final StorageLayout storageLayout,
        final AudioMetadataReader audioMetadataReader
    ) {
        this.onlineTrackRepository = Objects.requireNonNull(onlineTrackRepository, "onlineTrackRepository must not be null");
        this.onlineCatalogSynchronizationService = Objects.requireNonNull(
            onlineCatalogSynchronizationService,
            "onlineCatalogSynchronizationService must not be null"
        );
        this.storageLayout = Objects.requireNonNull(storageLayout, "storageLayout must not be null");
        this.audioMetadataReader = Objects.requireNonNull(audioMetadataReader, "audioMetadataReader must not be null");
    }

    public List<FeaturedPlaylist> loadFeaturedPlaylists(final int limit) {
        this.onlineCatalogSynchronizationService.refreshCatalogIfStale();
        final List<CatalogSong> songs = this.onlineTrackRepository.findAll();
        final List<FeaturedPlaylist> playlists = new ArrayList<>();
        playlists.add(buildAllSongsPlaylist(songs));
        if (songs.size() > 1) {
            playlists.add(buildRecentPlaylist(this.onlineTrackRepository.findRecent(30)));
        }
        final List<CatalogSong> lyricReadySongs = this.onlineTrackRepository.findLyricReady(30);
        if (!lyricReadySongs.isEmpty()) {
            playlists.add(buildLyricReadyPlaylist(lyricReadySongs));
        }
        return playlists.stream().limit(Math.max(1, limit)).toList();
    }

    public Optional<FeaturedPlaylist> loadPlaylist(final String playlistId) {
        this.onlineCatalogSynchronizationService.refreshCatalogIfStale();
        if (playlistId == null || playlistId.isBlank()) {
            return Optional.empty();
        }
        return switch (playlistId) {
            case "online-library" -> Optional.of(buildAllSongsPlaylist(this.onlineTrackRepository.findAll()));
            case "recent-added" -> Optional.of(buildRecentPlaylist(this.onlineTrackRepository.findRecent(30)));
            case "lyric-ready" -> Optional.of(buildLyricReadyPlaylist(this.onlineTrackRepository.findLyricReady(30)));
            default -> Optional.empty();
        };
    }

    public List<CatalogSong> listTracks() {
        this.onlineCatalogSynchronizationService.refreshCatalogIfStale();
        return this.onlineTrackRepository.findAll();
    }

    public List<CatalogSong> searchSongs(final String keyword, final int limit) {
        this.onlineCatalogSynchronizationService.refreshCatalogIfStale();
        final String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isBlank()) {
            return List.of();
        }
        return this.onlineTrackRepository.search(normalizedKeyword, limit);
    }

    public Optional<CatalogSong> findSong(final String songId) {
        this.onlineCatalogSynchronizationService.refreshCatalogIfStale();
        if (songId == null || songId.isBlank()) {
            return Optional.empty();
        }
        return this.onlineTrackRepository.findBySongId(songId);
    }

    public String loadLyricText(final String songId) {
        return findSong(songId)
            .map(this::resolveLyricText)
            .orElse("");
    }

    public Optional<AudioAsset> loadAudioAsset(final String songId) {
        return findSong(songId)
            .flatMap(song -> {
                try {
                    final UrlResource resource = new UrlResource(song.audioFile().toUri());
                    if (!resource.exists()) {
                        return Optional.empty();
                    }
                    return Optional.of(new AudioAsset(song, resource));
                } catch (Exception ignored) {
                    return Optional.empty();
                }
            });
    }

    public Optional<BinaryPayload> loadArtwork(final String songId) {
        return findSong(songId).flatMap(song -> this.audioMetadataReader.loadArtwork(song.audioFile()));
    }

    private String resolveLyricText(final CatalogSong song) {
        final Path lyricPath = resolveLyricPath(song);
        if (lyricPath != null && Files.isRegularFile(lyricPath)) {
            try {
                final String lyricText = Files.readString(lyricPath, StandardCharsets.UTF_8);
                if (!lyricText.isBlank()) {
                    return lyricText;
                }
            } catch (IOException ignored) {
            }
        }
        return toLrc(fallbackLyricLines(song.title(), song.artist(), song.album()));
    }

    private FeaturedPlaylist buildAllSongsPlaylist(final List<CatalogSong> songs) {
        final List<CatalogSong> orderedSongs = songs.stream()
            .sorted(Comparator.comparing(CatalogSong::title, String.CASE_INSENSITIVE_ORDER))
            .toList();
        final String description = orderedSongs.isEmpty()
            ? "当前在线曲库为空，请先把后端在线音乐文件放到 ./runtime/online/music。"
            : "当前由 Spring Boot 4 后端接管的在线曲库。";
        return new FeaturedPlaylist(
            "online-library",
            "在线曲库",
            orderedSongs.isEmpty() ? "等待在线资源入库" : "后端统一维护的在线音乐列表",
            description,
            List.of("在线", "后端接管"),
            orderedSongs
        );
    }

    private FeaturedPlaylist buildRecentPlaylist(final List<CatalogSong> songs) {
        return new FeaturedPlaylist(
            "recent-added",
            "最近入库",
            "按后端文件修改时间整理",
            "方便第二阶段联调“在线推荐 -> 在线歌单 -> 播放”的完整链路。",
            List.of("在线", "最近"),
            songs
        );
    }

    private FeaturedPlaylist buildLyricReadyPlaylist(final List<CatalogSong> songs) {
        return new FeaturedPlaylist(
            "lyric-ready",
            "歌词就绪",
            "优先挑出后端已有 .lrc 的歌曲",
            "用于验证在线歌曲的桌面歌词、详情抽屉歌词与播放进度联动。",
            List.of("在线", "歌词"),
            songs
        );
    }

    private Path resolveLyricPath(final CatalogSong song) {
        if (song.relativeLyricPath() == null || song.relativeLyricPath().isBlank()) {
            return null;
        }
        return this.storageLayout.rootDirectory().resolve(song.relativeLyricPath()).normalize();
    }

    private List<LyricLine> fallbackLyricLines(final String title, final String artist, final String album) {
        return List.of(
            new LyricLine(Duration.ZERO, title),
            new LyricLine(Duration.ofSeconds(8), artist == null || artist.isBlank() ? "未知歌手" : artist),
            new LyricLine(Duration.ofSeconds(16), album == null || album.isBlank() ? "在线曲库" : album),
            new LyricLine(Duration.ofSeconds(24), "将同名 .lrc 放到 ./runtime/online/lyrics 可显示同步歌词")
        );
    }

    private String toLrc(final List<LyricLine> lyricLines) {
        final StringBuilder builder = new StringBuilder();
        for (final LyricLine lyricLine : lyricLines) {
            final long totalMillis = lyricLine.position().toMillis();
            final long minutes = totalMillis / 60_000L;
            final long seconds = (totalMillis % 60_000L) / 1_000L;
            final long millis = totalMillis % 1_000L;
            builder.append(String.format(Locale.ROOT, "[%02d:%02d.%03d]%s%n", minutes, seconds, millis, lyricLine.content()));
        }
        return builder.toString();
    }
}

