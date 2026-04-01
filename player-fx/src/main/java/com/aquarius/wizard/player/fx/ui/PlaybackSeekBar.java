package com.aquarius.wizard.player.fx.ui;

import javafx.scene.layout.Region;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Lightweight custom seek bar used by the phase-1 desktop player shell.
 *
 * <p>JavaFX's default slider skin clashes with the original UI and caused
 * hit-area issues in the bottom player bar, so the migration stage uses a
 * dedicated control with explicit layout and drag behavior.</p>
 */
public final class PlaybackSeekBar extends Region {

    private static final double TRACK_HEIGHT = 6.0;
    private static final double THUMB_SIZE = 16.0;
    private static final double PREF_HEIGHT = 24.0;
    private static final double MIN_WIDTH = 120.0;

    private final Region track = new Region();
    private final Region fill = new Region();
    private final Region thumb = new Region();

    private double maxSeconds = 1.0;
    private double currentSeconds;
    private boolean dragInFlight;
    private Consumer<Duration> seekHandler = duration -> { };

    public PlaybackSeekBar() {
        getStyleClass().add("playback-seek-bar");
        this.track.getStyleClass().add("playback-seek-bar-track");
        this.fill.getStyleClass().add("playback-seek-bar-fill");
        this.thumb.getStyleClass().add("playback-seek-bar-thumb");

        this.track.setManaged(false);
        this.fill.setManaged(false);
        this.thumb.setManaged(false);
        getChildren().addAll(this.track, this.fill, this.thumb);

        setMinHeight(PREF_HEIGHT);
        setPrefHeight(PREF_HEIGHT);
        setMinWidth(MIN_WIDTH);
        setMaxWidth(Double.MAX_VALUE);

        setOnMousePressed(event -> updateFromMouse(event.getX(), false));
        setOnMouseDragged(event -> updateFromMouse(event.getX(), false));
        setOnMouseReleased(event -> {
            updateFromMouse(event.getX(), true);
            this.dragInFlight = false;
        });
    }

    public void setSeekHandler(final Consumer<Duration> handler) {
        this.seekHandler = handler == null ? duration -> { } : handler;
    }

    public void setTotalDuration(final Duration totalDuration) {
        if (totalDuration == null || totalDuration.isNegative() || totalDuration.isZero()) {
            this.maxSeconds = 1.0;
            requestLayout();
            return;
        }
        this.maxSeconds = Math.max(1.0, totalDuration.toMillis() / 1000.0);
        if (this.currentSeconds > this.maxSeconds) {
            this.currentSeconds = this.maxSeconds;
        }
        requestLayout();
    }

    public void setCurrentTime(final Duration currentDuration) {
        final double seconds = currentDuration == null || currentDuration.isNegative()
            ? 0.0
            : currentDuration.toMillis() / 1000.0;
        this.currentSeconds = clamp(seconds, 0.0, this.maxSeconds);
        requestLayout();
    }

    public void reset() {
        this.currentSeconds = 0.0;
        this.dragInFlight = false;
        requestLayout();
    }

    public boolean isDragInFlight() {
        return this.dragInFlight;
    }

    @Override
    protected void layoutChildren() {
        final double width = Math.max(0.0, getWidth());
        final double height = Math.max(PREF_HEIGHT, getHeight());
        final double trackY = (height - TRACK_HEIGHT) / 2.0;
        final double ratio = this.maxSeconds <= 0.0 ? 0.0 : clamp(this.currentSeconds / this.maxSeconds, 0.0, 1.0);
        final double fillWidth = width * ratio;
        final double thumbRadius = THUMB_SIZE / 2.0;
        final double thumbCenterX = clamp(fillWidth, thumbRadius, Math.max(thumbRadius, width - thumbRadius));

        this.track.resizeRelocate(0.0, trackY, width, TRACK_HEIGHT);
        this.fill.resizeRelocate(0.0, trackY, fillWidth, TRACK_HEIGHT);
        this.thumb.resizeRelocate(thumbCenterX - thumbRadius, (height - THUMB_SIZE) / 2.0, THUMB_SIZE, THUMB_SIZE);
    }

    @Override
    protected double computePrefHeight(final double width) {
        return PREF_HEIGHT;
    }

    @Override
    protected double computeMinWidth(final double height) {
        return MIN_WIDTH;
    }

    private void updateFromMouse(final double mouseX, final boolean commit) {
        if (isDisabled()) {
            return;
        }
        final double width = Math.max(1.0, getWidth());
        final double ratio = clamp(mouseX / width, 0.0, 1.0);
        this.currentSeconds = this.maxSeconds * ratio;
        this.dragInFlight = !commit;
        requestLayout();
        if (commit) {
            this.seekHandler.accept(Duration.ofMillis((long) (this.currentSeconds * 1000.0)));
        }
    }

    private double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }
}

