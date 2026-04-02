package com.aquarius.wizard.player.server.support.storage;

import com.aquarius.wizard.player.common.path.WorkspacePathResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Resolves and prepares the local storage directory structure.
 */
public final class StorageLayoutResolver {

    private final WorkspacePathResolver workspacePathResolver;

    public StorageLayoutResolver(final WorkspacePathResolver workspacePathResolver) {
        this.workspacePathResolver = Objects.requireNonNull(
            workspacePathResolver,
            "workspacePathResolver must not be null"
        );
    }

    public StorageLayout resolve(
        final String rootPath,
        final String databaseFileName,
        final String musicDirectory,
        final String lyricsDirectory,
        final String coversDirectory,
        final String cacheDirectory
    ) {
        final Path rootDirectory = this.workspacePathResolver.resolve(rootPath);
        final Path databaseFile = rootDirectory.resolve(databaseFileName).normalize();
        return new StorageLayout(
            rootDirectory,
            databaseFile,
            rootDirectory.resolve(musicDirectory).normalize(),
            rootDirectory.resolve(lyricsDirectory).normalize(),
            rootDirectory.resolve(coversDirectory).normalize(),
            rootDirectory.resolve(cacheDirectory).normalize()
        );
    }

    public void prepareDirectories(final StorageLayout storageLayout) throws IOException {
        final StorageLayout layout = Objects.requireNonNull(storageLayout, "storageLayout must not be null");
        Files.createDirectories(layout.rootDirectory());
        Files.createDirectories(layout.musicDirectory());
        Files.createDirectories(layout.lyricsDirectory());
        Files.createDirectories(layout.coversDirectory());
        Files.createDirectories(layout.cacheDirectory());
    }
}


