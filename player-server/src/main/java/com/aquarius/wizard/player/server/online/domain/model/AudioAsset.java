package com.aquarius.wizard.player.server.online.domain.model;

import org.springframework.core.io.UrlResource;

/**
 * Streamable audio resource paired with its catalog metadata.
 */
public record AudioAsset(CatalogSong song, UrlResource resource) {
}
