package com.aquarius.wizard.player.server.web.media;

import com.aquarius.wizard.player.server.library.BackendOnlineCatalogService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
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
 */
@RestController
@RequestMapping("/api/files")
public class MediaFileController {

    private final BackendOnlineCatalogService backendOnlineCatalogService;

    public MediaFileController(final BackendOnlineCatalogService backendOnlineCatalogService) {
        this.backendOnlineCatalogService = Objects.requireNonNull(
            backendOnlineCatalogService,
            "backendOnlineCatalogService must not be null"
        );
    }

    @GetMapping("/audio/{songId}")
    public ResponseEntity<Resource> audio(@PathVariable("songId") final String songId) {
        return this.backendOnlineCatalogService.loadAudioAsset(songId)
            .map(asset -> {
                final Resource resource = asset.resource();
                return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .cacheControl(CacheControl.noCache())
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.inline().filename(asset.song().audioFile().getFileName().toString()).build().toString()
                )
                .body(resource);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/covers/song/{songId}")
    public ResponseEntity<byte[]> songCover(@PathVariable("songId") final String songId) {
        return this.backendOnlineCatalogService.loadArtwork(songId)
            .map(payload -> ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(payload.mediaType()))
                .cacheControl(CacheControl.noCache())
                .body(payload.bytes()))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
