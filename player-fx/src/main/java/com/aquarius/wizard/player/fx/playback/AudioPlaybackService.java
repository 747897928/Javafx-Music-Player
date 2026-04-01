package com.aquarius.wizard.player.fx.playback;

import com.aquarius.wizard.player.domain.model.SongSummary;
import javafx.beans.value.ChangeListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;

import java.time.Duration;

/**
 * Minimal desktop playback service used during the first migration stage.
 *
 * <p>The service owns the JavaFX {@link MediaPlayer} lifecycle and exposes a
 * listener-driven API so the JavaFX shell can stay focused on UI state.</p>
 */
public final class AudioPlaybackService {

    private PlaybackListener playbackListener = PlaybackListener.noop();
    private MediaPlayer mediaPlayer;
    private SongSummary currentSong;
    private ChangeListener<javafx.util.Duration> currentTimeListener;
    private double currentVolume = 0.5;

    public void setPlaybackListener(final PlaybackListener listener) {
        this.playbackListener = listener == null ? PlaybackListener.noop() : listener;
    }

    public void play(final SongSummary songSummary) {
        if (songSummary == null || isBlank(songSummary.mediaSource())) {
            this.playbackListener.onPlaybackFailed("当前歌曲没有可用音源。");
            return;
        }
        disposePlayer();
        this.currentSong = songSummary;
        try {
            this.mediaPlayer = new MediaPlayer(new Media(songSummary.mediaSource()));
        } catch (MediaException exception) {
            this.playbackListener.onPlaybackFailed("音频资源加载失败：" + exception.getMessage());
            return;
        }
        this.currentTimeListener = (observable, oldValue, newValue) -> notifyTimeChanged();
        this.mediaPlayer.currentTimeProperty().addListener(this.currentTimeListener);
        this.mediaPlayer.setVolume(this.currentVolume);
        this.mediaPlayer.setOnReady(() -> {
            this.playbackListener.onPlaybackReady(toJavaDuration(this.mediaPlayer.getTotalDuration()));
            notifyTimeChanged();
        });
        this.mediaPlayer.setOnPlaying(() -> this.playbackListener.onPlaybackStateChanged(true));
        this.mediaPlayer.setOnPaused(() -> this.playbackListener.onPlaybackStateChanged(false));
        this.mediaPlayer.setOnStopped(() -> this.playbackListener.onPlaybackStateChanged(false));
        this.mediaPlayer.setOnEndOfMedia(this.playbackListener::onPlaybackFinished);
        this.mediaPlayer.setOnError(() ->
            this.playbackListener.onPlaybackFailed("音频播放失败：" + this.mediaPlayer.getError())
        );
        this.mediaPlayer.play();
    }

    public void pause() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.pause();
        }
    }

    public void resume() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.play();
        }
    }

    public void seek(final Duration position) {
        if (this.mediaPlayer == null || position == null || position.isNegative()) {
            return;
        }
        this.mediaPlayer.seek(javafx.util.Duration.millis(position.toMillis()));
        notifyTimeChanged();
    }

    public void stop() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.stop();
        }
    }

    public void setVolume(final double volume) {
        this.currentVolume = Math.max(0.0, Math.min(1.0, volume));
        if (this.mediaPlayer != null) {
            this.mediaPlayer.setVolume(this.currentVolume);
        }
    }

    public double getVolume() {
        return this.currentVolume;
    }

    public boolean isPlaying() {
        return this.mediaPlayer != null && this.mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public boolean hasLoadedSong() {
        return this.mediaPlayer != null && this.currentSong != null;
    }

    public boolean isCurrentSong(final SongSummary songSummary) {
        return this.currentSong != null && this.currentSong.equals(songSummary);
    }

    public void dispose() {
        disposePlayer();
        this.currentSong = null;
    }

    private void notifyTimeChanged() {
        if (this.mediaPlayer == null) {
            return;
        }
        this.playbackListener.onPlaybackTimeChanged(
            toJavaDuration(this.mediaPlayer.getCurrentTime()),
            toJavaDuration(this.mediaPlayer.getTotalDuration())
        );
    }

    private void disposePlayer() {
        if (this.mediaPlayer == null) {
            return;
        }
        if (this.currentTimeListener != null) {
            this.mediaPlayer.currentTimeProperty().removeListener(this.currentTimeListener);
            this.currentTimeListener = null;
        }
        this.mediaPlayer.setOnReady(null);
        this.mediaPlayer.setOnPlaying(null);
        this.mediaPlayer.setOnPaused(null);
        this.mediaPlayer.setOnStopped(null);
        this.mediaPlayer.setOnEndOfMedia(null);
        this.mediaPlayer.setOnError(null);
        this.mediaPlayer.stop();
        this.mediaPlayer.dispose();
        this.mediaPlayer = null;
    }

    private Duration toJavaDuration(final javafx.util.Duration javafxDuration) {
        if (javafxDuration == null || javafxDuration.isUnknown() || javafxDuration.lessThanOrEqualTo(javafx.util.Duration.ZERO)) {
            return Duration.ZERO;
        }
        return Duration.ofMillis((long) javafxDuration.toMillis());
    }

    private boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }

    /**
     * Playback callbacks consumed by the JavaFX shell.
     */
    public interface PlaybackListener {

        static PlaybackListener noop() {
            return new PlaybackListener() {
            };
        }

        default void onPlaybackReady(final Duration totalDuration) {
        }

        default void onPlaybackTimeChanged(final Duration currentTime, final Duration totalDuration) {
        }

        default void onPlaybackStateChanged(final boolean playing) {
        }

        default void onPlaybackFinished() {
        }

        default void onPlaybackFailed(final String message) {
        }
    }
}

