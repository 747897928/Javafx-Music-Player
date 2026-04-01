package cn.gxust.player.server.web;

import java.nio.file.Path;
import java.time.Instant;

/**
 * Basic system summary exposed during the first migration phase.
 *
 * @param applicationName application identifier
 * @param workingDirectory current process working directory
 * @param storageRoot      resolved storage root
 * @param databaseFile     resolved SQLite file
 * @param startedAt        server startup time
 */
public record SystemSummaryResponse(
    String applicationName,
    Path workingDirectory,
    Path storageRoot,
    Path databaseFile,
    Instant startedAt
) {
}
