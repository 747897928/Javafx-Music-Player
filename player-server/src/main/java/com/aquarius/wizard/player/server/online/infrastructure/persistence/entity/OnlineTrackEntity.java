package com.aquarius.wizard.player.server.online.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Persistent representation of the backend-managed online track index.
 */
@TableName("online_track")
public class OnlineTrackEntity {

    @TableId(value = "song_id", type = IdType.INPUT)
    private String songId;

    @TableField("file_name")
    private String fileName;

    @TableField("file_stem")
    private String fileStem;

    @TableField("relative_audio_path")
    private String relativeAudioPath;

    @TableField("relative_lyric_path")
    private String relativeLyricPath;

    private String title;

    private String artist;

    private String album;

    @TableField("duration_millis")
    private long durationMillis;

    @TableField("artwork_available")
    private int artworkAvailable;

    @TableField("last_modified_epoch_millis")
    private long lastModifiedEpochMillis;

    @TableField("updated_at_utc")
    private String updatedAtUtc;

    public String getSongId() {
        return this.songId;
    }

    public void setSongId(final String songId) {
        this.songId = songId;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getFileStem() {
        return this.fileStem;
    }

    public void setFileStem(final String fileStem) {
        this.fileStem = fileStem;
    }

    public String getRelativeAudioPath() {
        return this.relativeAudioPath;
    }

    public void setRelativeAudioPath(final String relativeAudioPath) {
        this.relativeAudioPath = relativeAudioPath;
    }

    public String getRelativeLyricPath() {
        return this.relativeLyricPath;
    }

    public void setRelativeLyricPath(final String relativeLyricPath) {
        this.relativeLyricPath = relativeLyricPath;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getArtist() {
        return this.artist;
    }

    public void setArtist(final String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return this.album;
    }

    public void setAlbum(final String album) {
        this.album = album;
    }

    public long getDurationMillis() {
        return this.durationMillis;
    }

    public void setDurationMillis(final long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public int getArtworkAvailable() {
        return this.artworkAvailable;
    }

    public void setArtworkAvailable(final int artworkAvailable) {
        this.artworkAvailable = artworkAvailable;
    }

    public long getLastModifiedEpochMillis() {
        return this.lastModifiedEpochMillis;
    }

    public void setLastModifiedEpochMillis(final long lastModifiedEpochMillis) {
        this.lastModifiedEpochMillis = lastModifiedEpochMillis;
    }

    public String getUpdatedAtUtc() {
        return this.updatedAtUtc;
    }

    public void setUpdatedAtUtc(final String updatedAtUtc) {
        this.updatedAtUtc = updatedAtUtc;
    }
}
