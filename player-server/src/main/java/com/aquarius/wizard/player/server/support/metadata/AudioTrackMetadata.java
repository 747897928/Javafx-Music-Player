package com.aquarius.wizard.player.server.support.metadata;

import java.time.Duration;

/**
 * Extracted metadata from an audio file.
 */
public record AudioTrackMetadata(
    String title,
    String artist,
    String album,
    Duration duration,
    boolean artworkAvailable
) {
}

