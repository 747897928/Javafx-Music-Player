package com.aquarius.wizard.player.server.dto.online;

/**
 * Refresh result payload for online-library management endpoints.
 */
public record OnlineLibraryRefreshResponse(int trackCount, String synchronizedAtUtc) {
}

