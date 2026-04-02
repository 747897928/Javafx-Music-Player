package com.aquarius.wizard.player.fx.ui;

import com.aquarius.wizard.player.model.LyricLine;
import com.aquarius.wizard.player.model.SongSummary;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Immersive now-playing drawer content that keeps the old project feel while
 * replacing the original JFXDrawer implementation.
 *
 * <p>The drawer owns several animations and lyric labels that are recreated
 * when songs change. Because this view can stay alive for the entire app
 * session, it also exposes a small {@link #dispose()} hook so the shell can
 * explicitly stop timers, release image references, and unbind lyric labels on
 * shutdown.</p>
 */
public final class NowPlayingDrawerView extends StackPane {

    private static final double DEMO_LYRIC_TIME_SCALE = 0.16;
    private static final Duration FALLBACK_LYRIC_DELAY = Duration.seconds(2.4);

    private SongSummary currentSong;
    private java.time.Duration currentTotalDuration = java.time.Duration.ZERO;

    private final VBox lyricTrack = new VBox(18.0);
    private final List<Label> lyricLabels = new ArrayList<>();
    private final BorderPane content = new BorderPane();
    private final VBox artworkPanel = new VBox(22.0);
    private final VBox lyricsPanel = new VBox(22.0);
    private final StackPane lyricViewport = new StackPane();
    private final Region lyricFocusBand = new Region();
    private final Region lyricFadeTop = new Region();
    private final Region lyricFadeBottom = new Region();
    private final Rectangle lyricViewportClip = new Rectangle();
    private final PauseTransition lyricTicker = new PauseTransition();
    private final Circle labelDisc = new Circle(84.0);
    private final Label albumTitleLabel = new Label();
    private final Label drawerSongTitleLabel = new Label();
    private final Label drawerSongSubtitleLabel = new Label();
    private final Label badgeLabel = new Label();
    private final Label timelineLabel = new Label();
    private final Button closeDrawerButton = new Button();
    private final ImageView albumArtworkView = new ImageView();
    private final ImageView vinylBaseImageView = new ImageView();
    private final ImageView rodImageView = new ImageView();
    private final ArtworkImageLoader artworkImageLoader;

    private Timeline lyricScrollTimeline;
    private Timeline lyricStateTimeline;
    private Timeline rodAnimation;
    private RotateTransition vinylRotation;
    private boolean lyricTickerEnabled = true;
    private boolean playbackActive;
    private int activeLyricIndex;
    private Runnable onPreviousAction = () -> { };
    private Runnable onPlayPauseAction = () -> { };
    private Runnable onNextAction = () -> { };
    private Runnable onLyricAction = () -> { };
    private Runnable onCloseAction = () -> { };

    public NowPlayingDrawerView(final ArtworkImageLoader artworkImageLoader, final SongSummary songSummary) {
        this.artworkImageLoader = Objects.requireNonNull(artworkImageLoader, "artworkImageLoader");
        this.currentSong = songSummary;
        getStyleClass().add("drawer-view");
        getChildren().add(buildLayout());
        this.lyricViewport.setClip(this.lyricViewportClip);
        this.lyricViewportClip.widthProperty().bind(this.lyricViewport.widthProperty());
        this.lyricViewportClip.heightProperty().bind(this.lyricViewport.heightProperty());
        updatePlaybackVisualState();
        setSong(songSummary);
    }

    public void setSong(final SongSummary songSummary) {
        setSong(songSummary, null);
    }

    public void setSong(final SongSummary songSummary, final Image artworkImage) {
        if (songSummary == null) {
            return;
        }
        this.currentSong = songSummary;
        this.currentTotalDuration = songSummary.duration();
        this.activeLyricIndex = 0;
        updateSongMeta(artworkImage);
        rebuildLyrics();
        restartLyricTicker();
        Platform.runLater(() -> applyActiveLyric(false));
    }

    public void setOnPreviousAction(final Runnable action) {
        this.onPreviousAction = defaultAction(action);
    }

    public void setOnPlayPauseAction(final Runnable action) {
        this.onPlayPauseAction = defaultAction(action);
    }

    public void setOnNextAction(final Runnable action) {
        this.onNextAction = defaultAction(action);
    }

    public void setOnLyricAction(final Runnable action) {
        this.onLyricAction = defaultAction(action);
    }

    public void setOnCloseAction(final Runnable action) {
        this.onCloseAction = defaultAction(action);
    }

    public void setTickerEnabled(final boolean enabled) {
        this.lyricTickerEnabled = enabled;
        restartLyricTicker();
    }

    public void setPlaybackActive(final boolean active) {
        this.playbackActive = active;
        updatePlaybackVisualState();
        restartLyricTicker();
    }

    public void updateTotalDuration(final java.time.Duration totalDuration) {
        if (totalDuration == null || totalDuration.isZero() || totalDuration.isNegative()) {
            return;
        }
        this.currentTotalDuration = totalDuration;
        this.timelineLabel.setText("00:00  /  " + formatDuration(this.currentTotalDuration));
    }

    public void updatePlaybackPosition(final java.time.Duration currentPosition) {
        if (currentPosition == null || currentPosition.isNegative()) {
            return;
        }
        this.timelineLabel.setText(formatDuration(currentPosition) + "  /  " + formatDuration(this.currentTotalDuration));
        syncActiveLyricWithPlayback(currentPosition);
    }

    public void dispose() {
        this.lyricTicker.stop();
        this.lyricTicker.setOnFinished(null);
        if (this.lyricScrollTimeline != null) {
            this.lyricScrollTimeline.stop();
            this.lyricScrollTimeline = null;
        }
        if (this.lyricStateTimeline != null) {
            this.lyricStateTimeline.stop();
            this.lyricStateTimeline = null;
        }
        if (this.rodAnimation != null) {
            this.rodAnimation.stop();
            this.rodAnimation = null;
        }
        if (this.vinylRotation != null) {
            this.vinylRotation.stop();
        }
        releaseLyricLabels();
        this.albumArtworkView.setImage(null);
        this.vinylBaseImageView.setImage(null);
        this.rodImageView.setImage(null);
    }

    private StackPane buildLayout() {
        final StackPane root = new StackPane();
        root.getStyleClass().add("drawer-scene");

        this.content.getStyleClass().add("drawer-content");
        this.content.setPadding(new Insets(44.0, 54.0, 26.0, 54.0));
        this.content.setLeft(buildArtworkPanel());
        this.content.setCenter(buildLyricsPanel());

        final Button closeButton = buildCloseButton();
        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
        StackPane.setMargin(closeButton, new Insets(20.0, 28.0, 0.0, 0.0));

        root.getChildren().addAll(this.content, closeButton);
        return root;
    }

    private VBox buildArtworkPanel() {
        this.artworkPanel.getStyleClass().add("drawer-artwork-panel");
        this.artworkPanel.setAlignment(Pos.CENTER);
        this.artworkPanel.setPrefWidth(480.0);

        final StackPane vinylStage = new StackPane();
        vinylStage.getStyleClass().add("vinyl-stage");
        this.vinylBaseImageView.setImage(loadResourceImage("/images/topandbottom/pan.png"));
        this.vinylBaseImageView.setFitWidth(360.0);
        this.vinylBaseImageView.setFitHeight(360.0);
        this.vinylBaseImageView.setPreserveRatio(true);

        final StackPane albumLabel = new StackPane();
        albumLabel.getStyleClass().add("album-label");
        albumLabel.setPrefSize(170.0, 170.0);
        this.albumArtworkView.setFitWidth(170.0);
        this.albumArtworkView.setFitHeight(170.0);
        this.albumArtworkView.setPreserveRatio(false);
        this.albumArtworkView.setClip(new Circle(85.0, 85.0, 85.0));
        this.albumArtworkView.getStyleClass().add("album-artwork-image");
        this.albumTitleLabel.getStyleClass().add("album-label-title");
        this.albumTitleLabel.setWrapText(true);
        this.albumTitleLabel.setTextAlignment(TextAlignment.CENTER);
        albumLabel.getChildren().addAll(this.albumArtworkView, this.albumTitleLabel);

        final Circle centerHole = new Circle(12.0, Color.web("#e6edff"));

        final StackPane vinyl = new StackPane(this.vinylBaseImageView, albumLabel, centerHole);
        vinyl.getStyleClass().add("vinyl-record");

        final RotateTransition rotation = new RotateTransition(Duration.seconds(60.0), albumLabel);
        rotation.setByAngle(360.0);
        rotation.setCycleCount(Animation.INDEFINITE);
        rotation.setInterpolator(Interpolator.LINEAR);
        this.vinylRotation = rotation;
        this.rodImageView.setImage(loadResourceImage("/images/topandbottom/rodImageView.png"));
        this.rodImageView.setFitWidth(290.0);
        this.rodImageView.setPreserveRatio(true);
        this.rodImageView.getStyleClass().add("vinyl-needle-image");
        this.rodImageView.setTranslateX(-42.0);
        this.rodImageView.setTranslateY(-118.0);
        this.rodImageView.setRotate(-18.0);

        updatePlaybackVisualState();

        vinylStage.getChildren().addAll(vinyl, this.rodImageView);

        this.drawerSongTitleLabel.getStyleClass().add("drawer-song-title");
        this.drawerSongSubtitleLabel.getStyleClass().add("drawer-song-subtitle");
        this.badgeLabel.getStyleClass().add("drawer-badge");

        this.artworkPanel.getChildren().setAll(
            vinylStage,
            this.drawerSongTitleLabel,
            this.drawerSongSubtitleLabel,
            this.badgeLabel
        );
        return this.artworkPanel;
    }

    private VBox buildLyricsPanel() {
        this.lyricsPanel.getStyleClass().add("drawer-lyrics-panel");
        this.lyricsPanel.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(this.lyricsPanel, Priority.ALWAYS);

        final Label title = new Label("Live Lyrics");
        title.getStyleClass().add("drawer-section-title");

        this.lyricViewport.getStyleClass().add("lyric-viewport");
        this.lyricViewport.setAlignment(Pos.TOP_CENTER);
        StackPane.setAlignment(this.lyricTrack, Pos.TOP_CENTER);
        StackPane.setAlignment(this.lyricFocusBand, Pos.CENTER);
        StackPane.setAlignment(this.lyricFadeTop, Pos.TOP_CENTER);
        StackPane.setAlignment(this.lyricFadeBottom, Pos.BOTTOM_CENTER);
        this.lyricTrack.getStyleClass().add("lyric-track");
        this.lyricTrack.setAlignment(Pos.TOP_CENTER);
        this.lyricFocusBand.getStyleClass().add("lyric-focus-band");
        this.lyricFadeTop.getStyleClass().addAll("lyric-fade", "lyric-fade-top");
        this.lyricFadeBottom.getStyleClass().addAll("lyric-fade", "lyric-fade-bottom");
        this.lyricViewport.getChildren().setAll(
            this.lyricFocusBand,
            this.lyricTrack,
            this.lyricFadeTop,
            this.lyricFadeBottom
        );

        this.lyricsPanel.getChildren().setAll(title, this.lyricViewport);
        VBox.setVgrow(this.lyricViewport, Priority.ALWAYS);
        return this.lyricsPanel;
    }

    private Button buildCloseButton() {
        this.closeDrawerButton.setText("");
        this.closeDrawerButton.setGraphic(SvgIconFactory.createIcon(AppGlyphs.RESTORE, 16.0, Color.web("#f4f7ff")));
        this.closeDrawerButton.getStyleClass().add("drawer-close-button");
        this.closeDrawerButton.setOnAction(event -> this.onCloseAction.run());
        MaterialButtonFeedback.install(this.closeDrawerButton);
        return this.closeDrawerButton;
    }

    private void updateSongMeta(final Image artworkImage) {
        final Image resolvedArtwork = artworkImage != null
            ? artworkImage
            : this.artworkImageLoader.loadSongArtwork(this.currentSong, 220.0, 220.0);
        this.albumArtworkView.setImage(resolvedArtwork);
        this.albumTitleLabel.setText(this.currentSong.title());
        this.drawerSongTitleLabel.setText(this.currentSong.title());
        this.drawerSongSubtitleLabel.setText(this.currentSong.artist() + "  •  " + this.currentSong.album());
        this.badgeLabel.setText(this.currentSong.backgroundLabel());
        this.timelineLabel.setText("00:00  /  " + formatDuration(this.currentTotalDuration));
    }

    private void rebuildLyrics() {
        releaseLyricLabels();

        final List<LyricLine> lyricLines = this.currentSong.lyricLines();
        if (lyricLines == null || lyricLines.isEmpty()) {
            final Label emptyLabel = createLyricLabel("暂无歌词");
            this.lyricLabels.add(emptyLabel);
            this.lyricTrack.getChildren().add(emptyLabel);
            refreshActiveLyric(false);
            return;
        }

        for (final LyricLine line : lyricLines) {
            final Label label = createLyricLabel(line.content());
            this.lyricLabels.add(label);
        }
        this.lyricTrack.getChildren().setAll(this.lyricLabels);
        refreshActiveLyric(false);
    }

    private Label createLyricLabel(final String text) {
        final Label label = new Label(text);
        label.getStyleClass().add("lyric-line");
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.CENTER);
        label.maxWidthProperty().bind(this.lyricViewport.widthProperty().subtract(92.0));
        return label;
    }

    private void releaseLyricLabels() {
        for (final Label lyricLabel : this.lyricLabels) {
            if (lyricLabel != null && lyricLabel.maxWidthProperty().isBound()) {
                lyricLabel.maxWidthProperty().unbind();
            }
        }
        this.lyricLabels.clear();
        this.lyricTrack.getChildren().clear();
    }

    private void restartLyricTicker() {
        this.lyricTicker.stop();
        if (!this.lyricTickerEnabled || !this.playbackActive || this.lyricLabels.size() <= 1) {
            return;
        }
        this.lyricTicker.setOnFinished(event -> {
            advanceLyric();
            scheduleNextLyricTick();
        });
        scheduleNextLyricTick();
    }

    private void scheduleNextLyricTick() {
        this.lyricTicker.stop();
        this.lyricTicker.setDuration(resolveNextLyricDelay());
        this.lyricTicker.playFromStart();
    }

    private Duration resolveNextLyricDelay() {
        final List<LyricLine> lyricLines = this.currentSong.lyricLines();
        if (lyricLines == null || lyricLines.size() <= 1) {
            return FALLBACK_LYRIC_DELAY;
        }

        final int nextIndex = (this.activeLyricIndex + 1) % lyricLines.size();
        if (nextIndex == 0) {
            return FALLBACK_LYRIC_DELAY;
        }

        final java.time.Duration currentPosition = lyricLines.get(this.activeLyricIndex).position();
        final java.time.Duration nextPosition = lyricLines.get(nextIndex).position();
        final java.time.Duration gap = nextPosition.minus(currentPosition);
        if (gap.isZero() || gap.isNegative()) {
            return FALLBACK_LYRIC_DELAY;
        }

        final double scaledMillis = clamp(gap.toMillis() * DEMO_LYRIC_TIME_SCALE, 1_350.0, 3_400.0);
        return Duration.millis(scaledMillis);
    }

    private void updatePlaybackVisualState() {
        animateNeedle(this.playbackActive);
        if (this.vinylRotation != null) {
            if (this.playbackActive) {
                this.vinylRotation.play();
            } else {
                this.vinylRotation.pause();
            }
        }
    }

    private void animateNeedle(final boolean engaged) {
        if (this.rodImageView.getImage() == null) {
            return;
        }
        if (this.rodAnimation != null) {
            this.rodAnimation.stop();
        }
        final double targetAngle = engaged ? 8.0 : -18.0;
        this.rodAnimation = new Timeline(
            new KeyFrame(
                Duration.millis(220.0),
                new KeyValue(this.rodImageView.rotateProperty(), targetAngle, Interpolator.EASE_BOTH)
            )
        );
        this.rodAnimation.play();
    }

    private void advanceLyric() {
        if (this.lyricLabels.isEmpty()) {
            return;
        }
        this.activeLyricIndex = (this.activeLyricIndex + 1) % this.lyricLabels.size();
        applyActiveLyric(true);
    }

    private void syncActiveLyricWithPlayback(final java.time.Duration currentPosition) {
        final List<LyricLine> lyricLines = this.currentSong.lyricLines();
        if (lyricLines == null || lyricLines.isEmpty()) {
            return;
        }
        int resolvedIndex = 0;
        for (int index = 0; index < lyricLines.size(); index++) {
            if (currentPosition.compareTo(lyricLines.get(index).position()) < 0) {
                break;
            }
            resolvedIndex = index;
        }
        if (resolvedIndex == this.activeLyricIndex) {
            return;
        }
        this.activeLyricIndex = resolvedIndex;
        applyActiveLyric(true);
    }

    private void applyActiveLyric(final boolean animate) {
        refreshActiveLyric(animate);
        Platform.runLater(() -> refreshLyricPosition(animate));
    }

    private void refreshActiveLyric(final boolean animate) {
        for (int index = 0; index < this.lyricLabels.size(); index++) {
            final Label lyricLabel = this.lyricLabels.get(index);
            lyricLabel.getStyleClass().removeAll(
                "lyric-line-active",
                "lyric-line-near",
                "lyric-line-far",
                "lyric-line-dim"
            );

            final int distance = Math.abs(index - this.activeLyricIndex);
            if (distance == 0) {
                lyricLabel.getStyleClass().add("lyric-line-active");
                continue;
            }
            if (distance == 1) {
                lyricLabel.getStyleClass().add("lyric-line-near");
                continue;
            }
            if (distance <= 3) {
                lyricLabel.getStyleClass().add("lyric-line-far");
                continue;
            }
            lyricLabel.getStyleClass().add("lyric-line-dim");
        }
        animateLyricStates(animate);
    }

    private String formatDuration(final java.time.Duration duration) {
        final long minutes = duration.toMinutes();
        final long seconds = duration.minus(minutes, ChronoUnit.MINUTES).toSeconds();
        return "%02d:%02d".formatted(minutes, seconds);
    }

    public void configureResponsiveLayout(
        final ReadOnlyDoubleProperty sceneWidthProperty,
        final ReadOnlyDoubleProperty drawerHeightProperty
    ) {
        this.artworkPanel.prefWidthProperty().bind(
            Bindings.createDoubleBinding(
                () -> clamp(sceneWidthProperty.get() * 0.34, 300.0, 480.0),
                sceneWidthProperty
            )
        );

        this.lyricViewport.prefHeightProperty().bind(
            Bindings.createDoubleBinding(
                () -> clamp(drawerHeightProperty.get() - 230.0, 260.0, 440.0),
                drawerHeightProperty
            )
        );
        this.lyricViewport.maxHeightProperty().bind(this.lyricViewport.prefHeightProperty());
        this.lyricFocusBand.prefWidthProperty().bind(this.lyricViewport.widthProperty().subtract(48.0));
        this.lyricFocusBand.prefHeightProperty().bind(
            Bindings.createDoubleBinding(
                () -> clamp(this.lyricViewport.getHeight() * 0.24, 96.0, 128.0),
                this.lyricViewport.heightProperty()
            )
        );
        this.lyricFadeTop.prefHeightProperty().bind(this.lyricViewport.heightProperty().multiply(0.2));
        this.lyricFadeBottom.prefHeightProperty().bind(this.lyricViewport.heightProperty().multiply(0.2));
        this.lyricFadeTop.prefWidthProperty().bind(this.lyricViewport.widthProperty());
        this.lyricFadeBottom.prefWidthProperty().bind(this.lyricViewport.widthProperty());

        sceneWidthProperty.addListener((observable, oldValue, newValue) ->
            this.content.setPadding(resolveContentPadding(newValue.doubleValue()))
        );
        this.content.setPadding(resolveContentPadding(sceneWidthProperty.get()));
        drawerHeightProperty.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> refreshLyricPosition(false)));
    }

    private Insets resolveContentPadding(final double sceneWidth) {
        if (sceneWidth < 1220.0) {
            return new Insets(32.0, 30.0, 22.0, 30.0);
        }
        return new Insets(44.0, 54.0, 26.0, 54.0);
    }

    private double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void refreshLyricPosition(final boolean animate) {
        if (this.lyricLabels.isEmpty() || this.lyricViewport.getHeight() <= 0.0) {
            return;
        }

        final Label activeLabel = this.lyricLabels.get(this.activeLyricIndex);
        final double activeCenter = activeLabel.getBoundsInParent().getMinY()
            + (activeLabel.getBoundsInParent().getHeight() / 2.0);
        final double viewportCenter = this.lyricViewport.getHeight() / 2.0;
        final double maxOffset = Math.max(0.0, this.lyricTrack.getBoundsInLocal().getHeight() - this.lyricViewport.getHeight());
        final double targetOffset = clamp(activeCenter - viewportCenter, 0.0, maxOffset);
        final double targetTranslate = -targetOffset;
        if (!animate) {
            this.lyricTrack.setTranslateY(targetTranslate);
            return;
        }

        final double currentTranslate = this.lyricTrack.getTranslateY();
        if (Math.abs(currentTranslate - targetTranslate) < 0.5) {
            this.lyricTrack.setTranslateY(targetTranslate);
            return;
        }

        if (this.lyricScrollTimeline != null) {
            this.lyricScrollTimeline.stop();
        }

        final double direction = Math.signum(targetTranslate - currentTranslate);
        final double overshoot = clamp(targetTranslate + (direction * 10.0), -maxOffset, 0.0);
        final double durationMillis = clamp(Math.abs(targetTranslate - currentTranslate) * 4.3, 240.0, 420.0);

        this.lyricScrollTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(this.lyricTrack.translateYProperty(), currentTranslate)),
            new KeyFrame(
                Duration.millis(durationMillis * 0.82),
                new KeyValue(this.lyricTrack.translateYProperty(), overshoot, Interpolator.EASE_OUT)
            ),
            new KeyFrame(
                Duration.millis(durationMillis),
                new KeyValue(this.lyricTrack.translateYProperty(), targetTranslate, Interpolator.EASE_BOTH)
            )
        );
        this.lyricScrollTimeline.playFromStart();
    }

    private void animateLyricStates(final boolean animate) {
        if (this.lyricStateTimeline != null) {
            this.lyricStateTimeline.stop();
        }

        if (!animate) {
            for (int index = 0; index < this.lyricLabels.size(); index++) {
                applyLyricVisualState(this.lyricLabels.get(index), Math.abs(index - this.activeLyricIndex));
            }
            return;
        }

        this.lyricStateTimeline = new Timeline();
        for (int index = 0; index < this.lyricLabels.size(); index++) {
            final Label lyricLabel = this.lyricLabels.get(index);
            final int distance = Math.abs(index - this.activeLyricIndex);
            this.lyricStateTimeline.getKeyFrames().add(
                new KeyFrame(
                    Duration.millis(320.0),
                    new KeyValue(lyricLabel.scaleXProperty(), resolveLyricScale(distance), Interpolator.EASE_BOTH),
                    new KeyValue(lyricLabel.scaleYProperty(), resolveLyricScale(distance), Interpolator.EASE_BOTH),
                    new KeyValue(lyricLabel.opacityProperty(), resolveLyricOpacity(distance), Interpolator.EASE_BOTH)
                )
            );
        }
        this.lyricStateTimeline.playFromStart();
    }

    private void applyLyricVisualState(final Label lyricLabel, final int distance) {
        final double scale = resolveLyricScale(distance);
        lyricLabel.setScaleX(scale);
        lyricLabel.setScaleY(scale);
        lyricLabel.setOpacity(resolveLyricOpacity(distance));
    }

    private double resolveLyricScale(final int distance) {
        if (distance == 0) {
            return 1.08;
        }
        if (distance == 1) {
            return 1.0;
        }
        if (distance <= 3) {
            return 0.94;
        }
        return 0.88;
    }

    private double resolveLyricOpacity(final int distance) {
        if (distance == 0) {
            return 1.0;
        }
        if (distance == 1) {
            return 0.76;
        }
        if (distance <= 3) {
            return 0.42;
        }
        return 0.16;
    }

    private Runnable defaultAction(final Runnable action) {
        return action == null ? () -> { } : action;
    }

    private Image loadResourceImage(final String resourcePath) {
        try {
            return new Image(NowPlayingDrawerView.class.getResource(resourcePath).toExternalForm(), true);
        } catch (Exception ignored) {
            return null;
        }
    }
}


