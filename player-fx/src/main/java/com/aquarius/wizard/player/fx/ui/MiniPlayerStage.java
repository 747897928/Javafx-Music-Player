package com.aquarius.wizard.player.fx.ui;

import com.aquarius.wizard.player.model.SongSummary;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Floating mini-mode player aligned with the old simplify-mode workflow.
 */
public final class MiniPlayerStage extends Stage {

    private static final double STAGE_WIDTH = 240.0;
    private static final double STAGE_HEIGHT = 100.0;
    private final Stage ownerStage;
    private final StackPane root = new StackPane();
    private final ImageView coverImageView = new ImageView();
    private final ImageView restoreCoverImageView = new ImageView();
    private final Button restoreButton = new Button();
    private final Label titleLabel = new Label();
    private final Label subtitleLabel = new Label();
    private final Button playPauseButton = new Button();
    private final Button lyricButton = new Button();
    private final Button unfoldButton = new Button();
    private final ContextMenu playlistMenu = new ContextMenu();
    private final ArtworkImageLoader artworkImageLoader = new ArtworkImageLoader();

    private Runnable onPreviousAction = () -> { };
    private Runnable onPlayPauseAction = () -> { };
    private Runnable onNextAction = () -> { };
    private Runnable onRestoreAction = () -> { };
    private Runnable onLyricAction = () -> { };
    private Consumer<SongSummary> onSongSelected = songSummary -> { };
    private List<SongSummary> playlistSongs = List.of();
    private double dragOffsetX;
    private double dragOffsetY;

    public MiniPlayerStage(final Stage owner) {
        this.ownerStage = owner;
        initStyle(StageStyle.TRANSPARENT);
        setAlwaysOnTop(true);

        this.root.getStyleClass().add("mini-player-stage");
        this.root.getChildren().add(buildLayout());

        final Scene scene = new Scene(this.root, STAGE_WIDTH, STAGE_HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(
            MiniPlayerStage.class.getResource("/css/player-shell.css").toExternalForm()
        );
        setScene(scene);
        setResizable(false);
        setWidth(STAGE_WIDTH);
        setHeight(STAGE_HEIGHT);
        this.playlistMenu.getStyleClass().add("mini-player-playlist-menu");
        setOnCloseRequest(event -> {
            event.consume();
            this.onRestoreAction.run();
        });
    }

    public void setSong(final SongSummary songSummary) {
        if (songSummary == null) {
            return;
        }
        this.titleLabel.setText(songSummary.title());
        this.subtitleLabel.setText(songSummary.artist());
        final Image artwork = this.artworkImageLoader.loadSongArtwork(songSummary);
        this.coverImageView.setImage(artwork);
        this.restoreCoverImageView.setImage(artwork);
    }

    public void setPlaylistSongs(final List<SongSummary> songs) {
        this.playlistSongs = songs == null ? List.of() : new ArrayList<>(songs);
        rebuildPlaylistMenu();
    }

    public void setPlaybackActive(final boolean active) {
        this.playPauseButton.setGraphic(
            SvgIconFactory.createIcon(active ? AppGlyphs.PAUSE : AppGlyphs.PLAY, 16.0, Color.WHITE)
        );
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

    public void setOnRestoreAction(final Runnable action) {
        this.onRestoreAction = action == null ? () -> { } : action;
    }

    public void setOnLyricAction(final Runnable action) {
        this.onLyricAction = action == null ? () -> { } : action;
    }

    public void setOnSongSelected(final Consumer<SongSummary> action) {
        this.onSongSelected = action == null ? songSummary -> { } : action;
    }

    public void showNearScreenEdge() {
        showNearWindowEdge(this.ownerStage);
    }

    public void showNearWindowEdge(final Stage anchorStage) {
        sizeToScene();
        final Rectangle2D visualBounds = resolveVisualBounds(anchorStage);
        final double stageWidth = Math.max(getWidth(), getScene() == null ? STAGE_WIDTH : getScene().getWidth());
        final double stageHeight = Math.max(getHeight(), getScene() == null ? STAGE_HEIGHT : getScene().getHeight());
        final double preferredX = visualBounds.getMaxX() - stageWidth - 10.0;
        final double preferredY = visualBounds.getMinY() + ((visualBounds.getHeight() - stageHeight) / 2.0) + 80.0;
        final double minX = visualBounds.getMinX() + 12.0;
        final double maxX = visualBounds.getMaxX() - stageWidth - 12.0;
        final double minY = visualBounds.getMinY() + 12.0;
        final double maxY = visualBounds.getMaxY() - stageHeight - 12.0;
        setX(Math.max(minX, Math.min(preferredX, maxX)));
        setY(Math.max(minY, Math.min(preferredY, maxY)));
        show();
        toFront();
        requestFocus();
    }

    private StackPane buildLayout() {
        final StackPane shell = new StackPane();
        shell.getStyleClass().add("mini-player-shell");
        shell.setPrefSize(STAGE_WIDTH, STAGE_HEIGHT);
        shell.setMinSize(STAGE_WIDTH, STAGE_HEIGHT);
        shell.setMaxSize(STAGE_WIDTH, STAGE_HEIGHT);

        final HBox layout = new HBox(10.0);
        layout.getStyleClass().add("mini-player-content");
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setPadding(new Insets(8.0, 10.0, 8.0, 10.0));

        layout.getChildren().addAll(buildLeftPane(), buildCenterPane(), buildRightPane());
        shell.getChildren().add(layout);
        enableDrag(shell);
        return shell;
    }

    private StackPane buildLeftPane() {
        final StackPane coverPane = new StackPane();
        coverPane.getStyleClass().add("mini-player-cover");
        coverPane.setPrefSize(70.0, 70.0);

        this.coverImageView.setFitWidth(70.0);
        this.coverImageView.setFitHeight(70.0);
        this.coverImageView.setPreserveRatio(false);
        this.coverImageView.setClip(new Circle(35.0, 35.0, 35.0));

        this.restoreCoverImageView.setFitWidth(70.0);
        this.restoreCoverImageView.setFitHeight(70.0);
        this.restoreCoverImageView.setPreserveRatio(false);
        this.restoreCoverImageView.setClip(new Circle(35.0, 35.0, 35.0));

        final StackPane restoreOverlay = new StackPane();
        restoreOverlay.getStyleClass().add("mini-player-restore-overlay");
        restoreOverlay.setVisible(false);
        restoreOverlay.managedProperty().bind(restoreOverlay.visibleProperty());
        restoreOverlay.getChildren().add(this.restoreCoverImageView);

        this.restoreButton.setText("");
        this.restoreButton.setGraphic(SvgIconFactory.createIcon(AppGlyphs.RESTORE, 14.0, Color.web("#eaeaea")));
        this.restoreButton.getStyleClass().add("mini-player-restore-button");
        this.restoreButton.setOnAction(event -> this.onRestoreAction.run());
        MaterialButtonFeedback.install(this.restoreButton);
        restoreOverlay.getChildren().add(this.restoreButton);

        coverPane.setOnMouseEntered(event -> restoreOverlay.setVisible(true));
        coverPane.setOnMouseExited(event -> restoreOverlay.setVisible(false));
        coverPane.getChildren().addAll(this.coverImageView, restoreOverlay);
        return coverPane;
    }

    private VBox buildCenterPane() {
        final VBox centerPane = new VBox(8.0);
        centerPane.setAlignment(Pos.BOTTOM_LEFT);
        HBox.setHgrow(centerPane, Priority.ALWAYS);
        centerPane.setMaxWidth(112.0);
        centerPane.setPrefWidth(112.0);

        this.titleLabel.getStyleClass().add("mini-player-title");
        this.titleLabel.setMaxWidth(112.0);
        this.titleLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        this.subtitleLabel.getStyleClass().add("mini-player-subtitle");
        this.subtitleLabel.setMaxWidth(112.0);
        this.subtitleLabel.setTextOverrun(OverrunStyle.ELLIPSIS);

        final HBox controls = new HBox(4.0);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.getChildren().addAll(
            controlButton(AppGlyphs.PREVIOUS, 16.0, () -> this.onPreviousAction.run()),
            controlButton(AppGlyphs.PLAY, 16.0, () -> this.onPlayPauseAction.run(), this.playPauseButton),
            controlButton(AppGlyphs.NEXT, 16.0, () -> this.onNextAction.run())
        );

        centerPane.getChildren().addAll(this.titleLabel, this.subtitleLabel, controls);
        return centerPane;
    }

    private VBox buildRightPane() {
        final VBox utilities = new VBox(8.0);
        utilities.getStyleClass().add("mini-player-utility-column");
        utilities.setAlignment(Pos.TOP_CENTER);

        final Button closeButton = utilityButton(AppGlyphs.CLOSE, 14.0, () -> this.onRestoreAction.run());
        utilityButton(AppGlyphs.LYRICS, 14.0, () -> this.onLyricAction.run(), this.lyricButton);
        utilityButton(AppGlyphs.UNFOLD, 14.0, this::showPlaylistPopup, this.unfoldButton);

        utilities.getChildren().addAll(closeButton, this.lyricButton, this.unfoldButton);
        return utilities;
    }

    private Button controlButton(final String glyph, final double size, final Runnable action) {
        final Button button = new Button();
        return controlButton(glyph, size, action, button);
    }

    private Button controlButton(final String glyph, final double size, final Runnable action, final Button button) {
        button.setText("");
        button.setGraphic(SvgIconFactory.createIcon(glyph, size, Color.WHITE));
        button.getStyleClass().add("mini-player-control-button");
        button.setOnAction(event -> action.run());
        MaterialButtonFeedback.install(button);
        return button;
    }

    private Button utilityButton(final String glyph, final double size, final Runnable action) {
        final Button button = new Button();
        return utilityButton(glyph, size, action, button);
    }

    private Button utilityButton(final String glyph, final double size, final Runnable action, final Button button) {
        button.setText("");
        button.setGraphic(SvgIconFactory.createIcon(glyph, size, Color.WHITE));
        button.getStyleClass().add("mini-player-utility-button");
        button.setOnAction(event -> action.run());
        MaterialButtonFeedback.install(button);
        return button;
    }

    private void showPlaylistPopup() {
        rebuildPlaylistMenu();
        if (this.playlistMenu.getItems().isEmpty()) {
            return;
        }
        if (this.playlistMenu.isShowing()) {
            this.playlistMenu.hide();
        }
        this.playlistMenu.show(this.unfoldButton, Side.BOTTOM, -18.0, 8.0);
    }

    private void rebuildPlaylistMenu() {
        this.playlistMenu.getItems().clear();
        for (final SongSummary songSummary : this.playlistSongs) {
            final Label itemLabel = new Label(songSummary.title());
            itemLabel.getStyleClass().add("mini-player-playlist-item-label");
            itemLabel.setMaxWidth(168.0);
            itemLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
            final CustomMenuItem menuItem = new CustomMenuItem(itemLabel, true);
            menuItem.getStyleClass().add("mini-player-playlist-item");
            menuItem.setOnAction(event -> this.onSongSelected.accept(songSummary));
            this.playlistMenu.getItems().add(menuItem);
        }
    }

    private void enableDrag(final StackPane dragSurface) {
        dragSurface.setOnMousePressed(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            if (event.getTarget() instanceof Button) {
                return;
            }
            this.dragOffsetX = event.getSceneX();
            this.dragOffsetY = event.getSceneY();
        });
        dragSurface.setOnMouseDragged(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            if (event.getTarget() instanceof Button) {
                return;
            }
            setX(event.getScreenX() - this.dragOffsetX);
            setY(event.getScreenY() - this.dragOffsetY);
        });
    }

    private Rectangle2D resolveVisualBounds(final Stage anchorStage) {
        if (anchorStage == null) {
            return Screen.getPrimary().getVisualBounds();
        }
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


