package com.aquarius.wizard.player.server.support.storage;

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

