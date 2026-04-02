package com.aquarius.wizard.player.server.online.application;

import com.aquarius.wizard.player.server.online.application.result.OnlineCatalogRefreshResult;
import com.aquarius.wizard.player.server.online.domain.model.CatalogSong;
import com.aquarius.wizard.player.server.online.infrastructure.persistence.OnlineTrackRepository;
import com.aquarius.wizard.player.server.online.infrastructure.storage.OnlineLibraryScanner;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Rebuilds the online track index from the backend-managed file storage.
 */
@Service
public class OnlineCatalogSynchronizationService {

    private static final long SYNC_INTERVAL_MILLIS = 5_000L;

    private final OnlineLibraryScanner onlineLibraryScanner;
    private final OnlineTrackRepository onlineTrackRepository;
    private final Object syncMonitor = new Object();

    private volatile long lastSynchronizedAtMillis;

    public OnlineCatalogSynchronizationService(
        final OnlineLibraryScanner onlineLibraryScanner,
        final OnlineTrackRepository onlineTrackRepository
    ) {
        this.onlineLibraryScanner = Objects.requireNonNull(onlineLibraryScanner, "onlineLibraryScanner must not be null");
        this.onlineTrackRepository = Objects.requireNonNull(onlineTrackRepository, "onlineTrackRepository must not be null");
    }

    @PostConstruct
    void initializeCatalog() {
        refreshCatalog();
    }

    public void refreshCatalogIfStale() {
        final long now = System.currentTimeMillis();
        if (now - this.lastSynchronizedAtMillis < SYNC_INTERVAL_MILLIS) {
            return;
        }
        synchronized (this.syncMonitor) {
            final long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastSynchronizedAtMillis < SYNC_INTERVAL_MILLIS) {
                return;
            }
            refreshCatalogInternal();
        }
    }

    public OnlineCatalogRefreshResult refreshCatalog() {
        synchronized (this.syncMonitor) {
            return refreshCatalogInternal();
        }
    }

    private OnlineCatalogRefreshResult refreshCatalogInternal() {
        final List<CatalogSong> scannedSongs = this.onlineLibraryScanner.scanTracks();
        this.onlineTrackRepository.replaceAll(scannedSongs);
        this.lastSynchronizedAtMillis = System.currentTimeMillis();
        return new OnlineCatalogRefreshResult(scannedSongs.size(), Instant.ofEpochMilli(this.lastSynchronizedAtMillis));
    }
}
