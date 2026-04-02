package com.aquarius.wizard.player.server.online.application.result;

import java.time.Instant;
import java.util.List;

/**
 * Result returned after importing files into the backend-managed online library.
 */
public record OnlineLibraryImportResult(
    int importedAudioCount,
    int importedLyricCount,
    List<String> storedAudioFiles,
    List<String> storedLyricFiles,
    List<String> ignoredFiles,
    int trackCount,
    Instant synchronizedAtUtc
) {
}
