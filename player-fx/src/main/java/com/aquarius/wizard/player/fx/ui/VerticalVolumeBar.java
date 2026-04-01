package com.aquarius.wizard.player.fx.ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Region;

/**
 * Lightweight vertical volume bar used in the popup near the volume button.
 * This avoids the default JavaFX Slider skin, which produced visual artifacts
 * in the transparent popup scene.
 */
public final class VerticalVolumeBar extends Region {

    private static final double PREF_WIDTH = 16.0;
    private static final double PREF_HEIGHT = 88.0;
    private static final double TRACK_WIDTH = 3.0;
    private static final double THUMB_SIZE = 10.0;

    private final Region track = new Region();
    private final Region fill = new Region();
    private final Region thumb = new Region();
    private final DoubleProperty value = new SimpleDoubleProperty(this, "value", 50.0);

    public VerticalVolumeBar() {
        getStyleClass().add("volume-bar");
        this.track.getStyleClass().add("volume-bar-track");
        this.fill.getStyleClass().add("volume-bar-fill");
        this.thumb.getStyleClass().add("volume-bar-thumb");

        this.track.setManaged(false);
        this.fill.setManaged(false);
        this.thumb.setManaged(false);
        getChildren().addAll(this.track, this.fill, this.thumb);

        setPrefSize(PREF_WIDTH, PREF_HEIGHT);
        setMinSize(PREF_WIDTH, PREF_HEIGHT);
        setMaxSize(PREF_WIDTH, PREF_HEIGHT);

        this.value.addListener((observable, oldValue, newValue) -> requestLayout());

        setOnMousePressed(event -> updateFromMouse(event.getY()));
        setOnMouseDragged(event -> updateFromMouse(event.getY()));
    }

    public double getValue() {
        return this.value.get();
    }

    public void setValue(final double newValue) {
        this.value.set(clamp(newValue, 0.0, 100.0));
    }

    public DoubleProperty valueProperty() {
        return this.value;
    }

    @Override
    protected void layoutChildren() {
        final double width = Math.max(PREF_WIDTH, getWidth());
        final double height = Math.max(PREF_HEIGHT, getHeight());
        final double centerX = (width - TRACK_WIDTH) / 2.0;
        final double ratio = clamp(getValue() / 100.0, 0.0, 1.0);
        final double fillHeight = height * ratio;
        final double thumbX = (width - THUMB_SIZE) / 2.0;
        final double thumbY = clamp((height - fillHeight) - (THUMB_SIZE / 2.0), 0.0, height - THUMB_SIZE);

        this.track.resizeRelocate(centerX, 0.0, TRACK_WIDTH, height);
        this.fill.resizeRelocate(centerX, height - fillHeight, TRACK_WIDTH, fillHeight);
        this.thumb.resizeRelocate(thumbX, thumbY, THUMB_SIZE, THUMB_SIZE);
    }

    @Override
    protected double computePrefWidth(final double height) {
        return PREF_WIDTH;
    }

    @Override
    protected double computePrefHeight(final double width) {
        return PREF_HEIGHT;
    }

    private void updateFromMouse(final double mouseY) {
        final double height = Math.max(1.0, getHeight());
        final double ratio = 1.0 - clamp(mouseY / height, 0.0, 1.0);
        setValue(ratio * 100.0);
    }

    private double clamp(final double rawValue, final double min, final double max) {
        return Math.max(min, Math.min(max, rawValue));
    }
}
