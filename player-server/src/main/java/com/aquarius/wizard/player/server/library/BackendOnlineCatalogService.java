package com.aquarius.wizard.player.server.library;

import com.aquarius.wizard.player.domain.model.LyricLine;
import com.aquarius.wizard.player.infra.storage.StorageLayout;
import jakarta.annotation.PostConstruct;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Online catalog service for the Spring Boot 4 backend.
 *
 * <p>Audio, lyric and cover files remain on disk under the backend-managed
 * relative runtime directories, while searchable track metadata is synchronized
 * into the default SQLite database so the project stays lightweight and
 * self-contained.</p>
 */
@Service
public class BackendOnlineCatalogService {

    private static final List<String> AUDIO_SUFFIXES = List.of(".mp3", ".wav", ".m4a", ".flac", ".aac", ".pcm");
    private static final long SYNC_INTERVAL_MILLIS = 5_000L;
    private static final String CREATE_TRACK_TABLE = """
        CREATE TABLE IF NOT EXISTS online_track (
            song_id TEXT PRIMARY KEY,
            file_name TEXT NOT NULL,
            file_stem TEXT NOT NULL,
            relative_audio_path TEXT NOT NULL,
            relative_lyric_path TEXT,
            title TEXT NOT NULL,
            artist TEXT NOT NULL,
            album TEXT NOT NULL,
            duration_millis INTEGER NOT NULL,
            artwork_available INTEGER NOT NULL,
            last_modified_epoch_millis INTEGER NOT NULL,
            updated_at_utc TEXT NOT NULL
        )
        """;
    private static final String CREATE_TITLE_INDEX = """
        CREATE INDEX IF NOT EXISTS idx_online_track_title
        ON online_track(title)
        """;
    private static final String CREATE_ARTIST_INDEX = """
        CREATE INDEX IF NOT EXISTS idx_online_track_artist
        ON online_track(artist)
        """;
    private static final String CREATE_LAST_MODIFIED_INDEX = """
        CREATE INDEX IF NOT EXISTS idx_online_track_last_modified
        ON online_track(last_modified_epoch_millis DESC)
        """;
    private static final String INSERT_TRACK_SQL = """
        INSERT INTO online_track (
            song_id,
            file_name,
            file_stem,
            relative_audio_path,
            relative_lyric_path,
            title,
            artist,
            album,
            duration_millis,
            artwork_available,
            last_modified_epoch_millis,
            updated_at_utc
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    private static final String SELECT_TRACK_COLUMNS = """
        SELECT
            song_id,
            file_name,
            file_stem,
            relative_audio_path,
            relative_lyric_path,
            title,
            artist,
            album,
            duration_millis,
            artwork_available,
            last_modified_epoch_millis
        FROM online_track
        """;

    private final StorageLayout storageLayout;
    private final DataSource dataSource;
    private final Object syncMonitor = new Object();

    private volatile long lastSynchronizedAtMillis;

    public BackendOnlineCatalogService(final StorageLayout storageLayout, final DataSource dataSource) {
        this.storageLayout = Objects.requireNonNull(storageLayout, "storageLayout must not be null");
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
    }

    @PostConstruct
    void initializeCatalog() {
        ensureSchema();
        synchronizeCatalogNow();
    }

    public List<FeaturedPlaylist> loadFeaturedPlaylists(final int limit) {
        ensureCatalogReady();
        final List<CatalogSong> songs = loadAllTracks();
        final List<FeaturedPlaylist> playlists = new ArrayList<>();
        playlists.add(buildAllSongsPlaylist(songs));
        if (songs.size() > 1) {
            playlists.add(buildRecentPlaylist(loadRecentTracks(30)));
        }
        final List<CatalogSong> lyricReadySongs = loadLyricReadyTracks(30);
        if (!lyricReadySongs.isEmpty()) {
            playlists.add(buildLyricReadyPlaylist(lyricReadySongs));
        }
        return playlists.stream().limit(Math.max(1, limit)).toList();
    }

    public Optional<FeaturedPlaylist> loadPlaylist(final String playlistId) {
        ensureCatalogReady();
        if (playlistId == null || playlistId.isBlank()) {
            return Optional.empty();
        }
        return switch (playlistId) {
            case "online-library", "local-library" -> Optional.of(buildAllSongsPlaylist(loadAllTracks()));
            case "recent-added" -> Optional.of(buildRecentPlaylist(loadRecentTracks(30)));
            case "lyric-ready" -> Optional.of(buildLyricReadyPlaylist(loadLyricReadyTracks(30)));
            default -> Optional.empty();
        };
    }

    public List<CatalogSong> listTracks() {
        ensureCatalogReady();
        return loadAllTracks();
    }

    public List<CatalogSong> searchSongs(final String keyword, final int limit) {
        ensureCatalogReady();
        final String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (normalizedKeyword.isBlank()) {
            return List.of();
        }
        final String likeValue = "%" + normalizedKeyword + "%";
        return queryTracks(
            SELECT_TRACK_COLUMNS
                + " WHERE lower(title) LIKE ? OR lower(artist) LIKE ? OR lower(album) LIKE ?"
                + " ORDER BY lower(title), file_name LIMIT ?",
            statement -> {
                statement.setString(1, likeValue);
                statement.setString(2, likeValue);
                statement.setString(3, likeValue);
                statement.setInt(4, Math.max(1, limit));
            }
        );
    }

    public Optional<CatalogSong> findSong(final String songId) {
        ensureCatalogReady();
        if (songId == null || songId.isBlank()) {
            return Optional.empty();
        }
        final List<CatalogSong> tracks = queryTracks(
            SELECT_TRACK_COLUMNS + " WHERE song_id = ? LIMIT 1",
            statement -> statement.setString(1, songId)
        );
        return tracks.stream().findFirst();
    }

    public String loadLyricText(final String songId) {
        final Optional<CatalogSong> songOptional = findSong(songId);
        if (songOptional.isEmpty()) {
            return "";
        }
        final CatalogSong song = songOptional.get();
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
        final Optional<CatalogSong> songOptional = findSong(songId);
        if (songOptional.isEmpty()) {
            return Optional.empty();
        }
        try {
            final AudioFile audioFile = AudioFileIO.read(songOptional.get().audioFile().toFile());
            final Tag tag = audioFile.getTag();
            if (tag == null || tag.getFirstArtwork() == null) {
                return Optional.empty();
            }
            final var artwork = tag.getFirstArtwork();
            final byte[] binaryData = artwork.getBinaryData();
            if (binaryData == null || binaryData.length == 0) {
                return Optional.empty();
            }
            final String mimeType = artwork.getMimeType() == null || artwork.getMimeType().isBlank()
                ? "image/jpeg"
                : artwork.getMimeType();
            return Optional.of(new BinaryPayload(binaryData, mimeType));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public RefreshResult refreshCatalog() {
        synchronized (this.syncMonitor) {
            ensureSchema();
            final int trackCount = synchronizeCatalogNow();
            return new RefreshResult(trackCount, Instant.ofEpochMilli(this.lastSynchronizedAtMillis));
        }
    }

    public ImportResult importFiles(final List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            final RefreshResult refreshResult = refreshCatalog();
            return new ImportResult(
                0,
                0,
                List.of(),
                List.of(),
                List.of(),
                refreshResult.trackCount(),
                refreshResult.synchronizedAtUtc()
            );
        }

        final List<PendingImport> pendingImports = new ArrayList<>();
        final List<String> ignoredFiles = new ArrayList<>();
        final Set<String> reservedStems = loadReservedImportStems();
        final Map<String, String> targetStemsBySourceStem = new LinkedHashMap<>();

        for (final MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                ignoredFiles.add("empty-upload");
                continue;
            }

            final String sanitizedFileName = sanitizeFileName(file.getOriginalFilename());
            if (sanitizedFileName.isBlank()) {
                ignoredFiles.add("unnamed-upload");
                continue;
            }

            final String normalizedFileName = sanitizedFileName.toLowerCase(Locale.ROOT);
            final boolean lyricFile = normalizedFileName.endsWith(".lrc");
            if (!lyricFile && !isSupportedAudioName(normalizedFileName)) {
                ignoredFiles.add(sanitizedFileName);
                continue;
            }

            final String sourceStem = stripExtension(sanitizedFileName);
            final String normalizedStemKey = sourceStem.isBlank()
                ? "upload"
                : sourceStem.toLowerCase(Locale.ROOT);
            final String targetStem = targetStemsBySourceStem.computeIfAbsent(
                normalizedStemKey,
                ignored -> resolveUniqueTargetStem(sourceStem, reservedStems)
            );

            pendingImports.add(new PendingImport(
                file,
                sanitizedFileName,
                targetStem,
                lyricFile ? ".lrc" : fileExtension(normalizedFileName),
                lyricFile
            ));
        }

        final List<String> storedAudioFiles = new ArrayList<>();
        final List<String> storedLyricFiles = new ArrayList<>();
        try {
            Files.createDirectories(this.storageLayout.musicDirectory());
            Files.createDirectories(this.storageLayout.lyricsDirectory());
            for (final PendingImport pendingImport : pendingImports) {
                final Path targetDirectory = pendingImport.lyricFile()
                    ? this.storageLayout.lyricsDirectory()
                    : this.storageLayout.musicDirectory();
                final String targetFileName = pendingImport.targetStem() + pendingImport.normalizedSuffix();
                final Path targetPath = targetDirectory.resolve(targetFileName).normalize();
                try (InputStream inputStream = pendingImport.file().getInputStream()) {
                    Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                if (pendingImport.lyricFile()) {
                    storedLyricFiles.add(targetFileName);
                } else {
                    storedAudioFiles.add(targetFileName);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to import files into the backend online library.", exception);
        }

        final RefreshResult refreshResult = refreshCatalog();
        return new ImportResult(
            storedAudioFiles.size(),
            storedLyricFiles.size(),
            storedAudioFiles,
            storedLyricFiles,
            ignoredFiles,
            refreshResult.trackCount(),
            refreshResult.synchronizedAtUtc()
        );
    }

    private void ensureCatalogReady() {
        final long now = System.currentTimeMillis();
        if (now - this.lastSynchronizedAtMillis < SYNC_INTERVAL_MILLIS) {
            return;
        }
        synchronized (this.syncMonitor) {
            final long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastSynchronizedAtMillis < SYNC_INTERVAL_MILLIS) {
                return;
            }
            ensureSchema();
            synchronizeCatalogNow();
        }
    }

    private void ensureSchema() {
        try (Connection connection = this.dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TRACK_TABLE);
            statement.execute(CREATE_TITLE_INDEX);
            statement.execute(CREATE_ARTIST_INDEX);
            statement.execute(CREATE_LAST_MODIFIED_INDEX);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to initialize online catalog schema.", exception);
        }
    }

    private int synchronizeCatalogNow() {
        final List<CatalogSong> scannedSongs = scanSongsFromStorage();
        try (Connection connection = this.dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (Statement deleteStatement = connection.createStatement()) {
                deleteStatement.execute("DELETE FROM online_track");
            }
            try (PreparedStatement insertStatement = connection.prepareStatement(INSERT_TRACK_SQL)) {
                for (final CatalogSong song : scannedSongs) {
                    insertStatement.setString(1, song.id());
                    insertStatement.setString(2, song.fileName());
                    insertStatement.setString(3, song.fileStem());
                    insertStatement.setString(4, song.relativeAudioPath());
                    insertStatement.setString(5, song.relativeLyricPath());
                    insertStatement.setString(6, song.title());
                    insertStatement.setString(7, song.artist());
                    insertStatement.setString(8, song.album());
                    insertStatement.setLong(9, song.duration().toMillis());
                    insertStatement.setInt(10, song.artworkAvailable() ? 1 : 0);
                    insertStatement.setLong(11, song.lastModifiedAt().toEpochMilli());
                    insertStatement.setTimestamp(12, Timestamp.from(Instant.now()));
                    insertStatement.addBatch();
                }
                insertStatement.executeBatch();
            }
            connection.commit();
            this.lastSynchronizedAtMillis = System.currentTimeMillis();
            return scannedSongs.size();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to synchronize backend online catalog.", exception);
        }
    }

    private List<CatalogSong> loadAllTracks() {
        return queryTracks(
            SELECT_TRACK_COLUMNS + " ORDER BY lower(title), file_name",
            statement -> {
            }
        );
    }

    private List<CatalogSong> loadRecentTracks(final int limit) {
        return queryTracks(
            SELECT_TRACK_COLUMNS + " ORDER BY last_modified_epoch_millis DESC, file_name LIMIT ?",
            statement -> statement.setInt(1, Math.max(1, limit))
        );
    }

    private List<CatalogSong> loadLyricReadyTracks(final int limit) {
        return queryTracks(
            SELECT_TRACK_COLUMNS + " WHERE relative_lyric_path IS NOT NULL AND relative_lyric_path <> ''"
                + " ORDER BY lower(title), file_name LIMIT ?",
            statement -> statement.setInt(1, Math.max(1, limit))
        );
    }

    private List<CatalogSong> queryTracks(final String sql, final StatementBinder statementBinder) {
        final List<CatalogSong> tracks = new ArrayList<>();
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statementBinder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tracks.add(mapCatalogSong(resultSet));
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to query backend online catalog.", exception);
        }
        return tracks;
    }

    private CatalogSong mapCatalogSong(final ResultSet resultSet) throws Exception {
        final String relativeAudioPath = resultSet.getString("relative_audio_path");
        final String relativeLyricPath = resultSet.getString("relative_lyric_path");
        return new CatalogSong(
            resultSet.getString("song_id"),
            resultSet.getString("file_name"),
            resultSet.getString("file_stem"),
            relativeAudioPath,
            relativeLyricPath == null || relativeLyricPath.isBlank() ? null : relativeLyricPath,
            this.storageLayout.rootDirectory().resolve(relativeAudioPath).normalize(),
            resultSet.getString("title"),
            resultSet.getString("artist"),
            resultSet.getString("album"),
            Duration.ofMillis(Math.max(0L, resultSet.getLong("duration_millis"))),
            resultSet.getInt("artwork_available") > 0,
            Instant.ofEpochMilli(Math.max(0L, resultSet.getLong("last_modified_epoch_millis")))
        );
    }

    private List<CatalogSong> scanSongsFromStorage() {
        final Path musicDirectory = this.storageLayout.musicDirectory();
        if (!Files.isDirectory(musicDirectory)) {
            return List.of();
        }
        final List<CatalogSong> songs = new ArrayList<>();
        try (var stream = Files.list(musicDirectory)) {
            stream
                .filter(Files::isRegularFile)
                .filter(this::isSupportedAudioFile)
                .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                .map(this::scanSong)
                .filter(Objects::nonNull)
                .forEach(songs::add);
        } catch (IOException ignored) {
            return List.of();
        }
        return songs;
    }

    private CatalogSong scanSong(final Path audioFilePath) {
        final String fileName = audioFilePath.getFileName().toString();
        final String fileStem = stripExtension(fileName);
        String title = fileStem;
        String artist = "";
        String album = "在线曲库";
        Duration duration = Duration.ZERO;
        boolean artworkAvailable = false;
        Instant lastModifiedAt = Instant.EPOCH;

        try {
            lastModifiedAt = Files.getLastModifiedTime(audioFilePath).toInstant();
        } catch (IOException ignored) {
            lastModifiedAt = Instant.EPOCH;
        }

        try {
            final AudioFile audioFile = AudioFileIO.read(audioFilePath.toFile());
            final Tag tag = audioFile.getTag();
            final AudioHeader audioHeader = audioFile.getAudioHeader();
            if (audioHeader != null && audioHeader.getTrackLength() > 0) {
                duration = Duration.ofSeconds(audioHeader.getTrackLength());
            }
            if (tag != null) {
                title = firstNonBlank(tag.getFirst(FieldKey.TITLE), title);
                artist = firstNonBlank(tag.getFirst(FieldKey.ARTIST), artist);
                album = firstNonBlank(tag.getFirst(FieldKey.ALBUM), album);
                artworkAvailable = tag.getFirstArtwork() != null;
            }
        } catch (Exception ignored) {
            duration = Duration.ZERO;
        }

        final Path lyricPath = this.storageLayout.lyricsDirectory().resolve(fileStem + ".lrc").normalize();
        return new CatalogSong(
            encodeSongId(fileName),
            fileName,
            fileStem,
            toRelativeStoragePath(audioFilePath),
            Files.isRegularFile(lyricPath) ? toRelativeStoragePath(lyricPath) : null,
            audioFilePath.normalize(),
            title,
            artist,
            album,
            duration,
            artworkAvailable,
            lastModifiedAt
        );
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

    private boolean isSupportedAudioFile(final Path audioFilePath) {
        final String lowerCaseName = audioFilePath.getFileName().toString().toLowerCase(Locale.ROOT);
        return AUDIO_SUFFIXES.stream().anyMatch(lowerCaseName::endsWith);
    }

    private boolean isSupportedAudioName(final String fileName) {
        final String normalizedFileName = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        return AUDIO_SUFFIXES.stream().anyMatch(normalizedFileName::endsWith);
    }

    private Path resolveLyricPath(final CatalogSong song) {
        if (song.relativeLyricPath() == null || song.relativeLyricPath().isBlank()) {
            return null;
        }
        return this.storageLayout.rootDirectory().resolve(song.relativeLyricPath()).normalize();
    }

    private String toRelativeStoragePath(final Path absolutePath) {
        return this.storageLayout.rootDirectory()
            .normalize()
            .relativize(absolutePath.normalize())
            .toString()
            .replace('\\', '/');
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

    private String firstNonBlank(final String candidate, final String fallback) {
        return candidate == null || candidate.isBlank() ? fallback : candidate;
    }

    private Set<String> loadReservedImportStems() {
        final Set<String> reservedStems = new HashSet<>();
        collectExistingStems(this.storageLayout.musicDirectory(), reservedStems);
        collectExistingStems(this.storageLayout.lyricsDirectory(), reservedStems);
        return reservedStems;
    }

    private void collectExistingStems(final Path directory, final Set<String> reservedStems) {
        if (!Files.isDirectory(directory)) {
            return;
        }
        try (var stream = Files.list(directory)) {
            stream
                .filter(Files::isRegularFile)
                .map(path -> path.getFileName().toString())
                .map(this::stripExtension)
                .map(stem -> stem.toLowerCase(Locale.ROOT))
                .forEach(reservedStems::add);
        } catch (IOException ignored) {
        }
    }

    private String resolveUniqueTargetStem(final String requestedStem, final Set<String> reservedStems) {
        final String baseStem = requestedStem == null || requestedStem.isBlank() ? "upload" : requestedStem;
        String candidate = baseStem;
        int suffix = 1;
        while (reservedStems.contains(candidate.toLowerCase(Locale.ROOT))) {
            candidate = baseStem + " (" + suffix + ")";
            suffix++;
        }
        reservedStems.add(candidate.toLowerCase(Locale.ROOT));
        return candidate;
    }

    private String sanitizeFileName(final String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "";
        }
        final String normalized = originalFileName.replace('\\', '/');
        final int lastSlashIndex = normalized.lastIndexOf('/');
        final String fileName = lastSlashIndex >= 0 ? normalized.substring(lastSlashIndex + 1) : normalized;
        final String sanitized = fileName.replaceAll("[\\p{Cntrl}<>:\"/\\\\|?*]", "_").trim();
        return sanitized.equals(".") || sanitized.equals("..") ? "" : sanitized;
    }

    private String stripExtension(final String fileName) {
        final int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }

    private String fileExtension(final String fileName) {
        final int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex >= 0 ? fileName.substring(lastDotIndex).toLowerCase(Locale.ROOT) : "";
    }

    private String encodeSongId(final String fileName) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(fileName.getBytes(StandardCharsets.UTF_8));
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws Exception;
    }

    public record CatalogSong(
        String id,
        String fileName,
        String fileStem,
        String relativeAudioPath,
        String relativeLyricPath,
        Path audioFile,
        String title,
        String artist,
        String album,
        Duration duration,
        boolean artworkAvailable,
        Instant lastModifiedAt
    ) {
    }

    public record FeaturedPlaylist(
        String id,
        String name,
        String copywriter,
        String description,
        List<String> tags,
        List<CatalogSong> songs
    ) {
    }

    public record AudioAsset(CatalogSong song, UrlResource resource) {
    }

    public record BinaryPayload(byte[] bytes, String mediaType) {
    }

    public record RefreshResult(int trackCount, Instant synchronizedAtUtc) {
    }

    public record ImportResult(
        int importedAudioCount,
        int importedLyricCount,
        List<String> storedAudioFiles,
        List<String> storedLyricFiles,
        List<String> ignoredFiles,
        int trackCount,
        Instant synchronizedAtUtc
    ) {
    }

    private record PendingImport(
        MultipartFile file,
        String originalFileName,
        String targetStem,
        String normalizedSuffix,
        boolean lyricFile
    ) {
    }
}
