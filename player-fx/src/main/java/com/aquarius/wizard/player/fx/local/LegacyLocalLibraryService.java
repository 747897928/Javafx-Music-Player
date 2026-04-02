package com.aquarius.wizard.player.fx.local;

import com.aquarius.wizard.player.common.path.WorkspacePathResolver;
import com.aquarius.wizard.player.model.LyricLine;
import com.aquarius.wizard.player.model.SongSummary;
import com.aquarius.wizard.player.fx.ui.FxSampleData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Temporary local library adapter used during phase 1.
 *
 * <p>The final architecture will move scanning behind Spring Boot, but the
 * migration stage still needs a real local playback path to replace the fake
 * shell interactions.</p>
 */
public final class LegacyLocalLibraryService {

    private static final Pattern LRC_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})(?:\\.(\\d{1,3}))?](.*)");
    private static final List<String> AUDIO_SUFFIXES = List.of(".mp3", ".wav", ".m4a", ".flac", ".aac", ".pcm");
    private static final String DEFAULT_ACCENT = "#5a8dff";

    private final Path workspaceRoot;
    private final Path localMusicDirectory;
    private final Path localLyricDirectory;

    public LegacyLocalLibraryService() {
        this.workspaceRoot = resolveWorkspaceRoot();
        final WorkspacePathResolver pathResolver = new WorkspacePathResolver(this.workspaceRoot);
        this.localMusicDirectory = pathResolver.resolve(Path.of("LocalMusic", "Music"));
        this.localLyricDirectory = pathResolver.resolve(Path.of("LocalMusic", "Lrc"));
    }

    public FxSampleData.PlaylistDetail loadLocalLibrary() {
        ensureDirectories();
        final List<SongSummary> songs = scanLocalSongs();
        final String description = songs.isEmpty()
            ? "请把音乐文件放到 ./LocalMusic/Music，把歌词放到 ./LocalMusic/Lrc。"
            : "已从 ./LocalMusic/Music 扫描本地音乐，并优先读取同名 .lrc 歌词。";
        final String tags = "LocalMusic / 相对路径 / " + songs.size() + " 首";
        return new FxSampleData.PlaylistDetail("本地音乐", tags, description, DEFAULT_ACCENT, "本", songs, "local", "", "");
    }

    public Path localMusicDirectory() {
        ensureDirectories();
        return this.localMusicDirectory;
    }

    public Path localLyricDirectory() {
        ensureDirectories();
        return this.localLyricDirectory;
    }

    public Path localLibraryRoot() {
        ensureDirectories();
        return this.localMusicDirectory.getParent();
    }

    public ImportResult importFiles(final List<Path> sourceFiles) {
        ensureDirectories();
        if (sourceFiles == null || sourceFiles.isEmpty()) {
            return ImportResult.empty();
        }

        int musicCount = 0;
        int lyricCount = 0;
        int skippedCount = 0;
        for (final Path sourceFile : sourceFiles) {
            if (sourceFile == null || !Files.isRegularFile(sourceFile)) {
                skippedCount++;
                continue;
            }

            final String lowerCaseName = sourceFile.getFileName().toString().toLowerCase(Locale.ROOT);
            final Path targetFile;
            final boolean musicFile;
            if (isSupportedAudioFile(sourceFile)) {
                targetFile = this.localMusicDirectory.resolve(sourceFile.getFileName());
                musicFile = true;
            } else if (lowerCaseName.endsWith(".lrc")) {
                targetFile = this.localLyricDirectory.resolve(sourceFile.getFileName());
                musicFile = false;
            } else {
                skippedCount++;
                continue;
            }

            try {
                if (Files.exists(targetFile) || sourceFile.toRealPath().equals(targetFile.toAbsolutePath())) {
                    skippedCount++;
                    continue;
                }
                Files.copy(sourceFile, targetFile, StandardCopyOption.COPY_ATTRIBUTES);
                if (musicFile) {
                    musicCount++;
                } else {
                    lyricCount++;
                }
            } catch (Exception ignored) {
                skippedCount++;
            }
        }
        return new ImportResult(musicCount, lyricCount, skippedCount);
    }

    public boolean deleteSong(final SongSummary songSummary) {
        final Path songPath = resolveSongPath(songSummary);
        if (songPath == null || !Files.exists(songPath) || !songPath.normalize().startsWith(this.localMusicDirectory.normalize())) {
            return false;
        }
        try {
            Files.delete(songPath);
            final Path lyricPath = resolveLyricPath(songPath);
            if (lyricPath != null && Files.exists(lyricPath)) {
                Files.delete(lyricPath);
            }
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    public boolean isLocalSong(final SongSummary songSummary) {
        final Path songPath = resolveSongPath(songSummary);
        return songPath != null && songPath.normalize().startsWith(this.localMusicDirectory.normalize());
    }

    public Path resolveSongPath(final SongSummary songSummary) {
        if (songSummary == null || songSummary.mediaSource() == null || !songSummary.mediaSource().startsWith("file:")) {
            return null;
        }
        try {
            return Path.of(java.net.URI.create(songSummary.mediaSource())).normalize();
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<SongSummary> scanLocalSongs() {
        if (!Files.isDirectory(this.localMusicDirectory)) {
            return List.of();
        }
        final List<SongSummary> songs = new ArrayList<>();
        try (var pathStream = Files.list(this.localMusicDirectory)) {
            pathStream
                .filter(Files::isRegularFile)
                .filter(this::isSupportedAudioFile)
                .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                .map(this::mapToSongSummary)
                .filter(Objects::nonNull)
                .forEach(songs::add);
        } catch (IOException ignored) {
            return List.of();
        }
        return songs;
    }

    private SongSummary mapToSongSummary(final Path audioFilePath) {
        final String fileName = audioFilePath.getFileName().toString();
        final String stem = stripExtension(fileName);
        final LocalAudioMetadataUtils.AudioMetadata metadata =
            LocalAudioMetadataUtils.readMetadata(audioFilePath, stem, "", "本地音乐");
        final List<LyricLine> lyricLines = loadLyricLines(stem, metadata.title(), metadata.artist(), metadata.album());
        return new SongSummary(
            metadata.title(),
            metadata.artist(),
            metadata.album(),
            metadata.duration(),
            colorFromName(stem),
            lyricLines,
            "LocalMusic",
            audioFilePath.toUri().toString(),
            "local",
            stem,
            ""
        );
    }

    private List<LyricLine> loadLyricLines(
        final String fileStem,
        final String title,
        final String artist,
        final String album
    ) {
        final Path lyricPath = this.localLyricDirectory.resolve(fileStem + ".lrc");
        if (!Files.exists(lyricPath)) {
            return fallbackLyricLines(title, artist, album);
        }
        final List<LyricLine> lyricLines = new ArrayList<>();
        try {
            for (final String row : Files.readAllLines(lyricPath, StandardCharsets.UTF_8)) {
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
                    Duration.ofMinutes(minutes)
                        .plusSeconds(seconds)
                        .plusMillis(millis),
                    content.isBlank() ? "..." : content
                ));
            }
        } catch (Exception ignored) {
            return fallbackLyricLines(title, artist, album);
        }
        return lyricLines.isEmpty() ? fallbackLyricLines(title, artist, album) : lyricLines;
    }

    private List<LyricLine> fallbackLyricLines(final String title, final String artist, final String album) {
        return List.of(
            new LyricLine(Duration.ZERO, title),
            new LyricLine(Duration.ofSeconds(8), artist.isBlank() ? "未知歌手" : artist),
            new LyricLine(Duration.ofSeconds(16), album.isBlank() ? "本地音乐" : album),
            new LyricLine(Duration.ofSeconds(24), "将 .lrc 放到 ./LocalMusic/Lrc 可显示同步歌词")
        );
    }

    private void ensureDirectories() {
        try {
            Files.createDirectories(this.localMusicDirectory);
            Files.createDirectories(this.localLyricDirectory);
        } catch (IOException ignored) {
        }
    }

    private boolean isSupportedAudioFile(final Path filePath) {
        final String lowerCaseName = filePath.getFileName().toString().toLowerCase(Locale.ROOT);
        return AUDIO_SUFFIXES.stream().anyMatch(lowerCaseName::endsWith);
    }

    private String stripExtension(final String fileName) {
        final int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }

    private String colorFromName(final String value) {
        final int hash = Math.abs(value.hashCode());
        final String[] palette = {"#5a8dff", "#ff8d7a", "#8d78ff", "#2dc8aa", "#ffc95c", "#ff6d98"};
        return palette[hash % palette.length];
    }

    private Path resolveLyricPath(final Path songPath) {
        if (songPath == null || songPath.getFileName() == null) {
            return null;
        }
        return this.localLyricDirectory.resolve(stripExtension(songPath.getFileName().toString()) + ".lrc");
    }

    private Path resolveWorkspaceRoot() {
        Path currentPath = Path.of("").toAbsolutePath().normalize();
        if (Files.exists(currentPath.resolve("pom.xml"))
            && currentPath.getParent() != null
            && Files.exists(currentPath.getParent().resolve("pom.xml"))
            && currentPath.getFileName() != null
            && "player-fx".equals(currentPath.getFileName().toString())) {
            return currentPath.getParent();
        }
        return currentPath;
    }

    /**
     * Copy result for local import operations.
     *
     * @param musicCount   imported audio files
     * @param lyricCount   imported lyric files
     * @param skippedCount skipped or unsupported files
     */
    public record ImportResult(
        int musicCount,
        int lyricCount,
        int skippedCount
    ) {

        public static ImportResult empty() {
            return new ImportResult(0, 0, 0);
        }

        public int importedCount() {
            return this.musicCount + this.lyricCount;
        }
    }
}


