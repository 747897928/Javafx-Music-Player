package com.aquarius.wizard.player.server.dto.online;

/**
 * Read model exposed by online-library management endpoints.
 */
public record OnlineTrackResponse(
    String id,
    String fileName,
    String title,
    String artist,
    String album,
    long durationMillis,
    String relativeAudioPath,
    String relativeLyricPath,
    boolean artworkAvailable,
    String lastModifiedAtUtc
) {
}

