package com.aquarius.wizard.player.server.controller;

import com.aquarius.wizard.player.server.service.OnlineCatalogQueryService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Streams backend-managed audio files and embedded artwork to the desktop app.
 *
 * <p>This endpoint is optimized for in-app playback instead of browser-style
 * downloads. We intentionally do not emit a {@code Content-Disposition}
 * filename header here, because online-library files may use Chinese names and
 * Tomcat 11 will reject non ISO-8859-1 header values. The JavaFX client only
 * needs a stable stream plus a correct {@code Content-Type}, so omitting the
 * filename header avoids noisy warnings without affecting playback.</p>
 */
@RestController
@RequestMapping("/api/files")
public class MediaFileController {

    private final OnlineCatalogQueryService onlineCatalogQueryService;

    public MediaFileController(final OnlineCatalogQueryService onlineCatalogQueryService) {
        this.onlineCatalogQueryService = Objects.requireNonNull(
            onlineCatalogQueryService,
            "onlineCatalogQueryService must not be null"
        );
    }

    @GetMapping("/audio/{songId}")
    public ResponseEntity<Resource> audio(@PathVariable("songId") final String songId) {
        return this.onlineCatalogQueryService.loadAudioAsset(songId)
            .map(asset -> {
                final Resource resource = asset.resource();
                return ResponseEntity.ok()
                    .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                    .cacheControl(CacheControl.noCache())
                    .body(resource);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/covers/song/{songId}")
    public ResponseEntity<byte[]> songCover(@PathVariable("songId") final String songId) {
        return this.onlineCatalogQueryService.loadArtwork(songId)
            .map(payload -> ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(payload.mediaType()))
                .cacheControl(CacheControl.noCache())
                .body(payload.bytes()))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

