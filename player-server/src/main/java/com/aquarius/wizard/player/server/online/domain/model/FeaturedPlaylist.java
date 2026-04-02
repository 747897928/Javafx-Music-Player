package com.aquarius.wizard.player.server.online.domain.model;

import java.util.List;

/**
 * Lightweight playlist projection exposed to the JavaFX client.
 */
public record FeaturedPlaylist(
    String id,
    String name,
    String copywriter,
    String description,
    List<String> tags,
    List<CatalogSong> songs
) {
}
