package com.aquarius.wizard.player.server.controller;

import com.aquarius.wizard.player.common.api.ApiResponse;
import com.aquarius.wizard.player.server.dto.online.OnlineLibraryImportResponse;
import com.aquarius.wizard.player.server.dto.online.OnlineLibraryRefreshResponse;
import com.aquarius.wizard.player.server.dto.online.OnlineTrackListResponse;
import com.aquarius.wizard.player.server.dto.online.OnlineTrackResponse;
import com.aquarius.wizard.player.server.model.CatalogSong;
import com.aquarius.wizard.player.server.service.OnlineCatalogQueryService;
import com.aquarius.wizard.player.server.service.OnlineCatalogSynchronizationService;
import com.aquarius.wizard.player.server.service.OnlineLibraryImportService;
import com.aquarius.wizard.player.server.service.result.OnlineCatalogRefreshResult;
import com.aquarius.wizard.player.server.service.result.OnlineLibraryImportResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

/**
 * Management endpoints for the backend-managed online library.
 */
@RestController
@RequestMapping("/api/online/library")
public class OnlineLibraryController {

    private final OnlineCatalogQueryService onlineCatalogQueryService;
    private final OnlineCatalogSynchronizationService onlineCatalogSynchronizationService;
    private final OnlineLibraryImportService onlineLibraryImportService;

    public OnlineLibraryController(
        final OnlineCatalogQueryService onlineCatalogQueryService,
        final OnlineCatalogSynchronizationService onlineCatalogSynchronizationService,
        final OnlineLibraryImportService onlineLibraryImportService
    ) {
        this.onlineCatalogQueryService = Objects.requireNonNull(
            onlineCatalogQueryService,
            "onlineCatalogQueryService must not be null"
        );
        this.onlineCatalogSynchronizationService = Objects.requireNonNull(
            onlineCatalogSynchronizationService,
            "onlineCatalogSynchronizationService must not be null"
        );
        this.onlineLibraryImportService = Objects.requireNonNull(
            onlineLibraryImportService,
            "onlineLibraryImportService must not be null"
        );
    }

    @GetMapping("/tracks")
    public ApiResponse<OnlineTrackListResponse> tracks() {
        final List<OnlineTrackResponse> tracks = this.onlineCatalogQueryService.listTracks().stream()
            .map(this::mapTrack)
            .toList();
        return ApiResponse.ok("Loaded backend online tracks.", new OnlineTrackListResponse(tracks.size(), tracks));
    }

    @PostMapping("/refresh")
    public ApiResponse<OnlineLibraryRefreshResponse> refresh() {
        final OnlineCatalogRefreshResult refreshResult = this.onlineCatalogSynchronizationService.refreshCatalog();
        return ApiResponse.ok(
            "Refreshed backend online catalog.",
            new OnlineLibraryRefreshResponse(refreshResult.trackCount(), refreshResult.synchronizedAtUtc().toString())
        );
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<OnlineLibraryImportResponse> importFiles(@RequestParam("files") final List<MultipartFile> files) {
        final OnlineLibraryImportResult importResult = this.onlineLibraryImportService.importFiles(files);
        return ApiResponse.ok(
            "Imported files into backend online library.",
            new OnlineLibraryImportResponse(
                importResult.importedAudioCount(),
                importResult.importedLyricCount(),
                importResult.storedAudioFiles(),
                importResult.storedLyricFiles(),
                importResult.ignoredFiles(),
                importResult.trackCount(),
                importResult.synchronizedAtUtc().toString()
            )
        );
    }

    private OnlineTrackResponse mapTrack(final CatalogSong song) {
        return new OnlineTrackResponse(
            song.id(),
            song.fileName(),
            song.title(),
            song.artist(),
            song.album(),
            song.duration().toMillis(),
            song.relativeAudioPath(),
            song.relativeLyricPath(),
            song.artworkAvailable(),
            song.lastModifiedAt().toString()
        );
    }
}

