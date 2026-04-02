package com.aquarius.wizard.player.server.online.infrastructure.storage;

import java.util.List;

/**
 * Files stored in the backend-managed online library during one import operation.
 */
public record StoredOnlineFiles(
    int storedAudioCount,
    int storedLyricCount,
    List<String> storedAudioFiles,
    List<String> storedLyricFiles,
    List<String> ignoredFiles
) {
}
