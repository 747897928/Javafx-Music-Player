package cn.gxust.player.infra.storage;

import java.nio.file.Path;

/**
 * Resolved storage layout for local database and media assets.
 *
 * @param rootDirectory    application storage root
 * @param databaseFile     SQLite database file
 * @param musicDirectory   managed music directory
 * @param lyricsDirectory  lyric directory
 * @param coversDirectory  cover directory
 * @param cacheDirectory   cache directory
 */
public record StorageLayout(
    Path rootDirectory,
    Path databaseFile,
    Path musicDirectory,
    Path lyricsDirectory,
    Path coversDirectory,
    Path cacheDirectory
) {
}
