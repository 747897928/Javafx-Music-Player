package com.aquarius.wizard.player.server.service;

import com.aquarius.wizard.player.server.service.result.OnlineCatalogRefreshResult;
import com.aquarius.wizard.player.server.service.result.OnlineLibraryImportResult;
import com.aquarius.wizard.player.server.support.storage.OnlineLibraryFileStorage;
import com.aquarius.wizard.player.server.support.storage.StoredOnlineFiles;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

/**
 * Application service for importing files into the backend-managed online library.
 */
@Service
public class OnlineLibraryImportService {

    private final OnlineLibraryFileStorage onlineLibraryFileStorage;
    private final OnlineCatalogSynchronizationService onlineCatalogSynchronizationService;

    public OnlineLibraryImportService(
        final OnlineLibraryFileStorage onlineLibraryFileStorage,
        final OnlineCatalogSynchronizationService onlineCatalogSynchronizationService
    ) {
        this.onlineLibraryFileStorage = Objects.requireNonNull(
            onlineLibraryFileStorage,
            "onlineLibraryFileStorage must not be null"
        );
        this.onlineCatalogSynchronizationService = Objects.requireNonNull(
            onlineCatalogSynchronizationService,
            "onlineCatalogSynchronizationService must not be null"
        );
    }

    public OnlineLibraryImportResult importFiles(final List<MultipartFile> files) {
        final StoredOnlineFiles storedOnlineFiles = this.onlineLibraryFileStorage.storeFiles(files);
        final OnlineCatalogRefreshResult refreshResult = this.onlineCatalogSynchronizationService.refreshCatalog();
        return new OnlineLibraryImportResult(
            storedOnlineFiles.storedAudioCount(),
            storedOnlineFiles.storedLyricCount(),
            storedOnlineFiles.storedAudioFiles(),
            storedOnlineFiles.storedLyricFiles(),
            storedOnlineFiles.ignoredFiles(),
            refreshResult.trackCount(),
            refreshResult.synchronizedAtUtc()
        );
    }
}

