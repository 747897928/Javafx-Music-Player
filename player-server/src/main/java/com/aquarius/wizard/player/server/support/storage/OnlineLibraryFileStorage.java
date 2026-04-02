package com.aquarius.wizard.player.server.support.storage;

import com.aquarius.wizard.player.server.support.storage.StorageLayout;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Stores uploaded online-library files under the backend-managed runtime directories.
 */
@Component
public class OnlineLibraryFileStorage {

    private static final List<String> AUDIO_SUFFIXES = List.of(".mp3", ".wav", ".m4a", ".flac", ".aac", ".pcm");

    private final StorageLayout storageLayout;

    public OnlineLibraryFileStorage(final StorageLayout storageLayout) {
        this.storageLayout = Objects.requireNonNull(storageLayout, "storageLayout must not be null");
    }

    public StoredOnlineFiles storeFiles(final List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return new StoredOnlineFiles(0, 0, List.of(), List.of(), List.of());
        }

        final List<String> storedAudioFiles = new ArrayList<>();
        final List<String> storedLyricFiles = new ArrayList<>();
        final List<String> ignoredFiles = new ArrayList<>();
        final Set<String> reservedStems = loadReservedImportStems();
        final Map<String, String> targetStemsBySourceStem = new LinkedHashMap<>();

        try {
            Files.createDirectories(this.storageLayout.musicDirectory());
            Files.createDirectories(this.storageLayout.lyricsDirectory());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to prepare online library directories.", exception);
        }

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

            final String targetFileName = targetStem + (lyricFile ? ".lrc" : fileExtension(normalizedFileName));
            final Path targetDirectory = lyricFile ? this.storageLayout.lyricsDirectory() : this.storageLayout.musicDirectory();
            final Path targetPath = targetDirectory.resolve(targetFileName).normalize();
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to store uploaded online library files.", exception);
            }

            if (lyricFile) {
                storedLyricFiles.add(targetFileName);
            } else {
                storedAudioFiles.add(targetFileName);
            }
        }

        return new StoredOnlineFiles(
            storedAudioFiles.size(),
            storedLyricFiles.size(),
            storedAudioFiles,
            storedLyricFiles,
            ignoredFiles
        );
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

    private boolean isSupportedAudioName(final String fileName) {
        final String normalizedFileName = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        return AUDIO_SUFFIXES.stream().anyMatch(normalizedFileName::endsWith);
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
}

