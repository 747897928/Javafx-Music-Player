package com.aquarius.wizard.player.server.support.storage;

import com.aquarius.wizard.player.server.support.storage.StorageLayout;
import com.aquarius.wizard.player.server.model.CatalogSong;
import com.aquarius.wizard.player.server.support.metadata.AudioMetadataReader;
import com.aquarius.wizard.player.server.support.metadata.AudioTrackMetadata;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Scans backend-managed online music files and projects them into catalog models.
 */
@Component
public class OnlineLibraryScanner {

    private static final List<String> AUDIO_SUFFIXES = List.of(".mp3", ".wav", ".m4a", ".flac", ".aac", ".pcm");

    private final StorageLayout storageLayout;
    private final AudioMetadataReader audioMetadataReader;

    public OnlineLibraryScanner(final StorageLayout storageLayout, final AudioMetadataReader audioMetadataReader) {
        this.storageLayout = Objects.requireNonNull(storageLayout, "storageLayout must not be null");
        this.audioMetadataReader = Objects.requireNonNull(audioMetadataReader, "audioMetadataReader must not be null");
    }

    public List<CatalogSong> scanTracks() {
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
                .map(this::scanTrack)
                .filter(Objects::nonNull)
                .forEach(songs::add);
        } catch (IOException ignored) {
            return List.of();
        }
        return songs;
    }

    private CatalogSong scanTrack(final Path audioFilePath) {
        final String fileName = audioFilePath.getFileName().toString();
        final String fileStem = stripExtension(fileName);
        final AudioTrackMetadata metadata = this.audioMetadataReader.readMetadata(audioFilePath, fileStem, "", "在线曲库");
        final Path lyricPath = this.storageLayout.lyricsDirectory().resolve(fileStem + ".lrc").normalize();
        return new CatalogSong(
            encodeSongId(fileName),
            fileName,
            fileStem,
            toRelativeStoragePath(audioFilePath),
            Files.isRegularFile(lyricPath) ? toRelativeStoragePath(lyricPath) : null,
            audioFilePath.normalize(),
            metadata.title(),
            metadata.artist(),
            metadata.album(),
            metadata.duration(),
            metadata.artworkAvailable(),
            resolveLastModifiedAt(audioFilePath)
        );
    }

    private Instant resolveLastModifiedAt(final Path audioFilePath) {
        try {
            return Files.getLastModifiedTime(audioFilePath).toInstant();
        } catch (IOException ignored) {
            return Instant.EPOCH;
        }
    }

    private boolean isSupportedAudioFile(final Path audioFilePath) {
        final String lowerCaseName = audioFilePath.getFileName().toString().toLowerCase(Locale.ROOT);
        return AUDIO_SUFFIXES.stream().anyMatch(lowerCaseName::endsWith);
    }

    private String toRelativeStoragePath(final Path absolutePath) {
        return this.storageLayout.rootDirectory()
            .normalize()
            .relativize(absolutePath.normalize())
            .toString()
            .replace('\\', '/');
    }

    private String stripExtension(final String fileName) {
        final int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }

    private String encodeSongId(final String fileName) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(fileName.getBytes(StandardCharsets.UTF_8));
    }
}

