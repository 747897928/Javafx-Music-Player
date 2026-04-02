package com.aquarius.wizard.player.server.dto.online;

import java.util.List;

/**
 * Import result payload for online-library management endpoints.
 */
public record OnlineLibraryImportResponse(
    int importedAudioCount,
    int importedLyricCount,
    List<String> storedAudioFiles,
    List<String> storedLyricFiles,
    List<String> ignoredFiles,
    int trackCount,
    String synchronizedAtUtc
) {
}

