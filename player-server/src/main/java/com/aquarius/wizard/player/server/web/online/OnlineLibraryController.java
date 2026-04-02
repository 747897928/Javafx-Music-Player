package com.aquarius.wizard.player.server.web.online;

import com.aquarius.wizard.player.server.online.application.OnlineCatalogQueryService;
import com.aquarius.wizard.player.server.online.application.OnlineCatalogSynchronizationService;
import com.aquarius.wizard.player.server.online.application.OnlineLibraryImportService;
import com.aquarius.wizard.player.server.online.application.result.OnlineCatalogRefreshResult;
import com.aquarius.wizard.player.server.online.application.result.OnlineLibraryImportResult;
import com.aquarius.wizard.player.server.online.domain.model.CatalogSong;
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
    public TrackListResponse tracks() {
        final List<TrackPayload> tracks = this.onlineCatalogQueryService.listTracks().stream()
            .map(this::mapTrack)
            .toList();
        return new TrackListResponse(tracks.size(), tracks);
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh() {
        final OnlineCatalogRefreshResult refreshResult = this.onlineCatalogSynchronizationService.refreshCatalog();
        return new RefreshResponse("ok", refreshResult.trackCount(), refreshResult.synchronizedAtUtc().toString());
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResponse importFiles(@RequestParam("files") final List<MultipartFile> files) {
        final OnlineLibraryImportResult importResult = this.onlineLibraryImportService.importFiles(files);
        return new ImportResponse(
            "ok",
            importResult.importedAudioCount(),
            importResult.importedLyricCount(),
            importResult.storedAudioFiles(),
            importResult.storedLyricFiles(),
            importResult.ignoredFiles(),
            importResult.trackCount(),
            importResult.synchronizedAtUtc().toString()
        );
    }

    private TrackPayload mapTrack(final CatalogSong song) {
        return new TrackPayload(
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

    public record TrackListResponse(int count, List<TrackPayload> tracks) {
    }

    public record TrackPayload(
        String id,
        String fileName,
        String title,
        String artist,
        String album,
        long durationMillis,
        String relativeAudioPath,
        String relativeLyricPath,
        boolean artworkAvailable,
        String lastModifiedAtUtc
    ) {
    }

    public record RefreshResponse(String status, int trackCount, String synchronizedAtUtc) {
    }

    public record ImportResponse(
        String status,
        int importedAudioCount,
        int importedLyricCount,
        List<String> storedAudioFiles,
        List<String> storedLyricFiles,
        List<String> ignoredFiles,
        int trackCount,
        String synchronizedAtUtc
    ) {
    }
}
