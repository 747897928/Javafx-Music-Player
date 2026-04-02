package com.aquarius.wizard.player.server.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configures relative storage locations for the standalone backend.
 */
@Validated
@ConfigurationProperties(prefix = "player.storage")
public class PlayerStorageProperties {

    @NotBlank
    private String root = "./runtime";

    @NotBlank
    private String databaseFileName = "musicbox.db";

    @NotBlank
    private String musicDirectory = "online/music";

    @NotBlank
    private String lyricsDirectory = "online/lyrics";

    @NotBlank
    private String coversDirectory = "online/covers";

    @NotBlank
    private String cacheDirectory = "online/cache";

    public String getRoot() {
        return this.root;
    }

    public void setRoot(final String root) {
        this.root = root;
    }

    public String getDatabaseFileName() {
        return this.databaseFileName;
    }

    public void setDatabaseFileName(final String databaseFileName) {
        this.databaseFileName = databaseFileName;
    }

    public String getMusicDirectory() {
        return this.musicDirectory;
    }

    public void setMusicDirectory(final String musicDirectory) {
        this.musicDirectory = musicDirectory;
    }

    public String getLyricsDirectory() {
        return this.lyricsDirectory;
    }

    public void setLyricsDirectory(final String lyricsDirectory) {
        this.lyricsDirectory = lyricsDirectory;
    }

    public String getCoversDirectory() {
        return this.coversDirectory;
    }

    public void setCoversDirectory(final String coversDirectory) {
        this.coversDirectory = coversDirectory;
    }

    public String getCacheDirectory() {
        return this.cacheDirectory;
    }

    public void setCacheDirectory(final String cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }
}

