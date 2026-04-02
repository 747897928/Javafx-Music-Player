package com.aquarius.wizard.player.server.dto.online;

import java.util.List;

/**
 * Track list payload for online-library management endpoints.
 */
public record OnlineTrackListResponse(int count, List<OnlineTrackResponse> tracks) {
}

