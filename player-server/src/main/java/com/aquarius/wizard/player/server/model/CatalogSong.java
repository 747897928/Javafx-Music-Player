package com.aquarius.wizard.player.server.model;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Read model for a backend-managed online track.
 */
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

