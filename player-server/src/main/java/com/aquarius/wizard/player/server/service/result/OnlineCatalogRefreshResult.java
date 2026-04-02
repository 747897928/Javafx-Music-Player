package com.aquarius.wizard.player.server.service.result;

import java.time.Instant;

/**
 * Result returned after rebuilding the online catalog index.
 */
public record OnlineCatalogRefreshResult(int trackCount, Instant synchronizedAtUtc) {
}

