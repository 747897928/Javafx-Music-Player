package com.aquarius.wizard.player.fx.ui;

import com.aquarius.wizard.player.domain.model.LyricLine;
import com.aquarius.wizard.player.domain.model.SongSummary;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.Duration;
import java.util.List;

/**
 * Floating desktop lyric panel that keeps the old always-on-top translucent
 * workflow separate from the drawer details page.
 */
public final class DesktopLyricStage extends Stage {

    private static final double STAGE_WIDTH = 740.0;
    private static final double STAGE_HEIGHT = 92.0;
    private final BorderPane root = new BorderPane();
    private final Label lyricLabel = new Label("暂无歌词");
    private final Button playPauseButton = new Button();

    private SongSummary currentSong;
    private Duration lastPosition = Duration.ZERO;
    private Runnable onPreviousAction = () -> { };
    private Runnable onPlayPauseAction = () -> { };
    private Runnable onNextAction = () -> { };

    private double dragOffsetX;
    private double dragOffsetY;

    public DesktopLyricStage(final Stage owner) {
        initStyle(StageStyle.TRANSPARENT);
        setAlwaysOnTop(true);
        setTitle("Desktop Lyric");

        this.root.getStyleClass().add("desktop-lyric-root");
        this.root.setTop(buildToolbar());
        this.root.setCenter(buildLyricBody());

        final Scene scene = new Scene(this.root, STAGE_WIDTH, STAGE_HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(
            DesktopLyricStage.class.getResource("/css/player-shell.css").toExternalForm()
        );
        setScene(scene);
        setResizable(false);
        setWidth(STAGE_WIDTH);
        setHeight(STAGE_HEIGHT);

        setOnCloseRequest(event -> {
            event.consume();
            hide();
        });
        enableDrag(this.root);
    }

    public void setSong(final SongSummary songSummary) {
        this.currentSong = songSummary;
        updateLyricForPosition(this.lastPosition);
    }

    public void updatePlaybackPosition(final Duration playbackPosition) {
        this.lastPosition = playbackPosition == null ? Duration.ZERO : playbackPosition;
        updateLyricForPosition(this.lastPosition);
    }

    public void setPlaybackActive(final boolean active) {
        applyPlayPauseGraphic(active);
    }

    public void setOnPreviousAction(final Runnable action) {
        this.onPreviousAction = action == null ? () -> { } : action;
    }

    public void setOnPlayPauseAction(final Runnable action) {
        this.onPlayPauseAction = action == null ? () -> { } : action;
    }

    public void setOnNextAction(final Runnable action) {
        this.onNextAction = action == null ? () -> { } : action;
    }

    public void toggleVisible() {
        if (isShowing()) {
            hide();
            return;
        }
        showAtDefaultPosition();
    }

    public void toggleVisibleBelow(final Stage anchorStage) {
        if (isShowing()) {
            hide();
            return;
        }
        showBelow(anchorStage);
    }

    public void showAtDefaultPosition() {
        sizeToScene();
        final Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        setX(visualBounds.getMinX() + Math.max(32.0, (visualBounds.getWidth() - getWidth()) / 2.0));
        setY(visualBounds.getMaxY() - getHeight() - 96.0);
        show();
        toFront();
    }

    public void showBelow(final Stage anchorStage) {
        if (anchorStage == null || !anchorStage.isShowing()) {
            showAtDefaultPosition();
            return;
        }
        sizeToScene();
        final Rectangle2D visualBounds = resolveVisualBounds(anchorStage);
        final double stageWidth = Math.max(getWidth(), STAGE_WIDTH);
        final double stageHeight = Math.max(getHeight(), STAGE_HEIGHT);
        final double preferredX = anchorStage.getX() + ((anchorStage.getWidth() - stageWidth) / 2.0);
        final double minX = visualBounds.getMinX() + 12.0;
        final double maxX = visualBounds.getMaxX() - stageWidth - 12.0;
        final double minY = visualBounds.getMinY() + 12.0;
        final double maxY = visualBounds.getMaxY() - stageHeight - 12.0;
        final double preferredBelowY = anchorStage.getY() + anchorStage.getHeight() + 8.0;
        final double fallbackAbovePlayerY = anchorStage.getY() + anchorStage.getHeight() - stageHeight - 18.0;
        final double resolvedY = preferredBelowY <= maxY
            ? preferredBelowY
            : Math.max(minY, Math.min(fallbackAbovePlayerY, maxY));
        setX(Math.max(minX, Math.min(preferredX, maxX)));
        setY(resolvedY);
        show();
        toFront();
    }

    private Node buildToolbar() {
        final HBox toolbar = new HBox(14.0);
        toolbar.getStyleClass().add("desktop-lyric-toolbar");
        toolbar.setAlignment(Pos.CENTER);
        toolbar.setPadding(new Insets(8.0, 12.0, 0.0, 12.0));

        final Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);

        final HBox controls = new HBox(10.0);
        controls.setAlignment(Pos.CENTER);
        controls.getChildren().addAll(
            lyricButton(AppGlyphs.PREVIOUS, 15.0, () -> this.onPreviousAction.run()),
            lyricButton(AppGlyphs.PLAY, 15.0, () -> this.onPlayPauseAction.run(), this.playPauseButton),
            lyricButton(AppGlyphs.NEXT, 15.0, () -> this.onNextAction.run())
        );

        final Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        final Button closeButton = lyricButton(AppGlyphs.CLOSE, 13.0, this::hide);
        closeButton.getStyleClass().add("desktop-lyric-close-button");

        toolbar.getChildren().addAll(leftSpacer, controls, rightSpacer, closeButton);
        return toolbar;
    }

    private StackPane buildLyricBody() {
        final StackPane body = new StackPane();
        body.getStyleClass().add("desktop-lyric-body");
        body.setAlignment(Pos.CENTER_LEFT);
        body.setPadding(new Insets(6.0, 18.0, 16.0, 18.0));
        this.lyricLabel.getStyleClass().add("desktop-lyric-label");
        this.lyricLabel.setAlignment(Pos.CENTER_LEFT);
        this.lyricLabel.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(this.lyricLabel, Pos.CENTER_LEFT);
        body.getChildren().add(this.lyricLabel);
        return body;
    }

    private Button lyricButton(final String glyph, final double size, final Runnable action) {
        final Button button = new Button();
        return lyricButton(glyph, size, action, button);
    }

    private Button lyricButton(final String glyph, final double size, final Runnable action, final Button button) {
        button.setText("");
        button.setGraphic(SvgIconFactory.createIcon(glyph, size, Color.web("#958686")));
        button.getStyleClass().add("desktop-lyric-button");
        button.setOnAction(event -> action.run());
        MaterialButtonFeedback.install(button);
        return button;
    }

    private void updateLyricForPosition(final Duration playbackPosition) {
        if (this.currentSong == null || this.currentSong.lyricLines() == null || this.currentSong.lyricLines().isEmpty()) {
            this.lyricLabel.setText("暂无歌词");
            return;
        }
        final List<LyricLine> lyricLines = this.currentSong.lyricLines();
        String activeContent = lyricLines.get(0).content();
        for (final LyricLine lyricLine : lyricLines) {
            if (playbackPosition.compareTo(lyricLine.position()) < 0) {
                break;
            }
            activeContent = lyricLine.content();
        }
        this.lyricLabel.setText(activeContent == null || activeContent.isBlank() ? "..." : activeContent);
    }

    private void applyPlayPauseGraphic(final boolean active) {
        this.playPauseButton.setGraphic(
            SvgIconFactory.createIcon(active ? AppGlyphs.PAUSE : AppGlyphs.PLAY, 15.0, Color.web("#958686"))
        );
    }

    private void enableDrag(final BorderPane dragTarget) {
        dragTarget.setOnMousePressed(event -> {
            this.dragOffsetX = event.getSceneX();
            this.dragOffsetY = event.getSceneY();
        });
        dragTarget.setOnMouseDragged(event -> {
            setX(event.getScreenX() - this.dragOffsetX);
            setY(event.getScreenY() - this.dragOffsetY);
        });
    }

    private Rectangle2D resolveVisualBounds(final Stage anchorStage) {
        return Screen.getScreensForRectangle(
                anchorStage.getX(),
                anchorStage.getY(),
                Math.max(anchorStage.getWidth(), 1.0),
                Math.max(anchorStage.getHeight(), 1.0)
            )
            .stream()
            .findFirst()
            .orElseGet(Screen::getPrimary)
            .getVisualBounds();
    }
}

