package com.aquarius.wizard.player.server.web;

import com.aquarius.wizard.player.server.library.BackendOnlineCatalogService;
import com.aquarius.wizard.player.server.library.BackendOnlineCatalogService.CatalogSong;
import com.aquarius.wizard.player.server.library.BackendOnlineCatalogService.ImportResult;
import com.aquarius.wizard.player.server.library.BackendOnlineCatalogService.RefreshResult;
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
 * Small management surface for the backend-managed online music library.
 */
@RestController
@RequestMapping("/api/online/library")
public class OnlineLibraryController {

    private final BackendOnlineCatalogService backendOnlineCatalogService;

    public OnlineLibraryController(final BackendOnlineCatalogService backendOnlineCatalogService) {
        this.backendOnlineCatalogService = Objects.requireNonNull(
            backendOnlineCatalogService,
            "backendOnlineCatalogService must not be null"
        );
    }

    @GetMapping("/tracks")
    public TrackListResponse tracks() {
        final List<TrackPayload> tracks = this.backendOnlineCatalogService.listTracks().stream()
            .map(this::mapTrack)
            .toList();
        return new TrackListResponse(tracks.size(), tracks);
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh() {
        final RefreshResult refreshResult = this.backendOnlineCatalogService.refreshCatalog();
        return new RefreshResponse("ok", refreshResult.trackCount(), refreshResult.synchronizedAtUtc().toString());
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResponse importFiles(@RequestParam("files") final List<MultipartFile> files) {
        final ImportResult importResult = this.backendOnlineCatalogService.importFiles(files);
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
