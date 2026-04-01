package cn.gxust.player.domain.model;

import java.time.Duration;
import java.util.List;

/**
 * Lightweight song view model used during the first migration stage.
 *
 * @param title           song title
 * @param artist          artist name
 * @param album           album name
 * @param duration        total duration
 * @param accentColor     UI accent color
 * @param lyricLines      timed lyric lines
 * @param backgroundLabel background hint displayed in the drawer
 * @param mediaSource     playable media source URI used by the desktop client during migration
 * @param sourceType      source type such as local/sample/legacy-online
 * @param sourceId        source-specific identifier
 * @param artworkUrl      optional artwork url used by legacy compatibility mode
 */
public record SongSummary(
    String title,
    String artist,
    String album,
    Duration duration,
    String accentColor,
    List<LyricLine> lyricLines,
    String backgroundLabel,
    String mediaSource,
    String sourceType,
    String sourceId,
    String artworkUrl
) {

    public SongSummary withLyricLines(final List<LyricLine> lines) {
        return new SongSummary(
            this.title,
            this.artist,
            this.album,
            this.duration,
            this.accentColor,
            lines,
            this.backgroundLabel,
            this.mediaSource,
            this.sourceType,
            this.sourceId,
            this.artworkUrl
        );
    }

    public SongSummary withMediaSource(final String resolvedMediaSource) {
        return new SongSummary(
            this.title,
            this.artist,
            this.album,
            this.duration,
            this.accentColor,
            this.lyricLines,
            this.backgroundLabel,
            resolvedMediaSource,
            this.sourceType,
            this.sourceId,
            this.artworkUrl
        );
    }

    public boolean isLegacyOnlineSource() {
        return "legacy-online".equals(this.sourceType);
    }

    public boolean isLocalSource() {
        return "local".equals(this.sourceType);
    }
}
