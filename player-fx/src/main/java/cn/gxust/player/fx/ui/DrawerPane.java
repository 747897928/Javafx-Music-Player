package cn.gxust.player.fx.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Objects;

/**
 * Generic drawer pane inspired by the interaction model used by JFXDrawer.
 * It supports four directions while keeping only the subset of behavior the
 * rebuilt player currently needs.
 */
public class DrawerPane extends StackPane {

    /**
     * Supported drawer slide directions.
     */
    public enum DrawerDirection {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM
    }

    private static final Duration ANIMATION_DURATION = Duration.millis(360.0);

    private final StackPane contentLayer = new StackPane();
    private final Region overlayLayer = new Region();
    private final StackPane sidePaneLayer = new StackPane();
    private final DoubleProperty defaultDrawerSize = new SimpleDoubleProperty(this, "defaultDrawerSize", 620.0);
    private final BooleanProperty open = new SimpleBooleanProperty(this, "open", false);
    private final ObjectProperty<DrawerDirection> direction = new SimpleObjectProperty<>(
        this,
        "direction",
        DrawerDirection.BOTTOM
    );
    private final ObjectProperty<Runnable> onOpened = new SimpleObjectProperty<>(this, "onOpened");
    private final ObjectProperty<Runnable> onClosed = new SimpleObjectProperty<>(this, "onClosed");
    private final Rectangle rootClip = new Rectangle();
    private final Rectangle overlayClip = new Rectangle();
    private final Rectangle sidePaneClip = new Rectangle();

    private Timeline currentTimeline;

    public DrawerPane() {
        getStyleClass().add("drawer-pane-root");
        setClip(this.rootClip);

        this.contentLayer.getStyleClass().add("drawer-pane-content-layer");

        this.overlayLayer.getStyleClass().add("drawer-pane-overlay");
        this.overlayLayer.setManaged(false);
        this.overlayLayer.setOpacity(0.0);
        this.overlayLayer.setVisible(false);
        this.overlayLayer.setMouseTransparent(true);
        this.overlayLayer.setClip(this.overlayClip);
        this.overlayLayer.setOnMouseClicked(event -> close());

        this.sidePaneLayer.getStyleClass().add("drawer-pane-surface");
        this.sidePaneLayer.setManaged(false);
        this.sidePaneLayer.setVisible(false);
        this.sidePaneLayer.setMouseTransparent(true);
        this.sidePaneLayer.setClip(this.sidePaneClip);

        this.direction.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                this.direction.set(oldValue == null ? DrawerDirection.BOTTOM : oldValue);
                return;
            }
            applyDirectionState();
            requestLayout();
        });

        getChildren().addAll(this.contentLayer, this.overlayLayer, this.sidePaneLayer);
        applyDirectionState();
    }

    public void setContent(final Node node) {
        this.contentLayer.getChildren().setAll(node);
    }

    public void setMainContent(final Node node) {
        setContent(node);
    }

    public void setSidePane(final Node node) {
        this.sidePaneLayer.getChildren().setAll(node);
    }

    public void setDrawerContent(final Node node) {
        setSidePane(node);
    }

    public double getDefaultDrawerSize() {
        return this.defaultDrawerSize.get();
    }

    public void setDefaultDrawerSize(final double drawerSize) {
        this.defaultDrawerSize.set(drawerSize);
        requestLayout();
    }

    public DoubleProperty defaultDrawerSizeProperty() {
        return this.defaultDrawerSize;
    }

    public double getDrawerSize() {
        return getDefaultDrawerSize();
    }

    public void setDrawerSize(final double drawerSize) {
        setDefaultDrawerSize(drawerSize);
    }

    public DoubleProperty drawerSizeProperty() {
        return defaultDrawerSizeProperty();
    }

    public double getDrawerHeight() {
        return getDefaultDrawerSize();
    }

    public void setDrawerHeight(final double drawerHeight) {
        setDefaultDrawerSize(drawerHeight);
    }

    public DoubleProperty drawerHeightProperty() {
        return defaultDrawerSizeProperty();
    }

    public DrawerDirection getDirection() {
        return this.direction.get();
    }

    public void setDirection(final DrawerDirection direction) {
        this.direction.set(Objects.requireNonNull(direction, "direction must not be null"));
    }

    public ObjectProperty<DrawerDirection> directionProperty() {
        return this.direction;
    }

    public boolean isOpen() {
        return this.open.get();
    }

    public BooleanProperty openProperty() {
        return this.open;
    }

    public boolean isOpened() {
        return isOpen();
    }

    public boolean isClosed() {
        return !isOpen();
    }

    public void toggle() {
        if (isOpen()) {
            close();
            return;
        }
        open();
    }

    public void open() {
        if (isOpen()) {
            return;
        }
        this.open.set(true);
        this.overlayLayer.setVisible(true);
        this.overlayLayer.setMouseTransparent(false);
        this.sidePaneLayer.setVisible(true);
        this.sidePaneLayer.setMouseTransparent(false);
        playAnimation(true);
    }

    public void close() {
        if (!isOpen()) {
            return;
        }
        this.open.set(false);
        playAnimation(false);
    }

    public ObjectProperty<Runnable> onOpenedProperty() {
        return this.onOpened;
    }

    public void setOnOpened(final Runnable onOpened) {
        this.onOpened.set(onOpened);
    }

    public ObjectProperty<Runnable> onClosedProperty() {
        return this.onClosed;
    }

    public void setOnClosed(final Runnable onClosed) {
        this.onClosed.set(onClosed);
    }

    @Override
    protected void layoutChildren() {
        final double width = snapSizeX(getWidth());
        final double height = snapSizeY(getHeight());

        this.rootClip.setWidth(width);
        this.rootClip.setHeight(height);

        layoutInArea(this.contentLayer, 0.0, 0.0, width, height, 0.0, HPos.CENTER, VPos.CENTER);

        this.overlayLayer.resizeRelocate(0.0, 0.0, width, height);
        this.overlayClip.setWidth(width);
        this.overlayClip.setHeight(height);

        final double sidePaneSize = resolveSidePaneSize(width, height);
        switch (getDirection()) {
            case LEFT -> this.sidePaneLayer.resizeRelocate(0.0, 0.0, sidePaneSize, height);
            case RIGHT -> this.sidePaneLayer.resizeRelocate(width - sidePaneSize, 0.0, sidePaneSize, height);
            case TOP -> this.sidePaneLayer.resizeRelocate(0.0, 0.0, width, sidePaneSize);
            case BOTTOM -> this.sidePaneLayer.resizeRelocate(0.0, height - sidePaneSize, width, sidePaneSize);
        }

        this.sidePaneClip.setWidth(this.sidePaneLayer.getWidth());
        this.sidePaneClip.setHeight(this.sidePaneLayer.getHeight());

        if (!isOpen() && !isAnimationRunning()) {
            positionClosed(sidePaneSize);
        }
    }

    private void playAnimation(final boolean opening) {
        if (this.currentTimeline != null) {
            this.currentTimeline.stop();
        }

        final double sidePaneSize = resolveSidePaneSize(getWidth(), getHeight());
        final double fromTranslate = activeTranslateProperty().get();
        final double toTranslate = opening ? 0.0 : hiddenTranslate(sidePaneSize);
        final double fromOpacity = this.overlayLayer.getOpacity();
        final double toOpacity = opening ? 1.0 : 0.0;

        this.currentTimeline = new Timeline(
            new KeyFrame(
                Duration.ZERO,
                new KeyValue(activeTranslateProperty(), fromTranslate),
                new KeyValue(this.overlayLayer.opacityProperty(), fromOpacity)
            ),
            new KeyFrame(
                ANIMATION_DURATION,
                event -> {
                    if (!opening) {
                        this.overlayLayer.setVisible(false);
                        this.overlayLayer.setMouseTransparent(true);
                        this.sidePaneLayer.setVisible(false);
                        this.sidePaneLayer.setMouseTransparent(true);
                    }
                    final Runnable callback = opening ? this.onOpened.get() : this.onClosed.get();
                    if (callback != null) {
                        callback.run();
                    }
                },
                new KeyValue(activeTranslateProperty(), toTranslate),
                new KeyValue(this.overlayLayer.opacityProperty(), toOpacity)
            )
        );
        this.currentTimeline.play();
    }

    private void applyDirectionState() {
        switch (getDirection()) {
            case LEFT -> StackPane.setAlignment(this.sidePaneLayer, javafx.geometry.Pos.CENTER_LEFT);
            case RIGHT -> StackPane.setAlignment(this.sidePaneLayer, javafx.geometry.Pos.CENTER_RIGHT);
            case TOP -> StackPane.setAlignment(this.sidePaneLayer, javafx.geometry.Pos.TOP_CENTER);
            case BOTTOM -> StackPane.setAlignment(this.sidePaneLayer, javafx.geometry.Pos.BOTTOM_CENTER);
        }

        this.sidePaneLayer.setTranslateX(0.0);
        this.sidePaneLayer.setTranslateY(0.0);
    }

    private void positionClosed(final double sidePaneSize) {
        if (isHorizontal()) {
            this.sidePaneLayer.setTranslateY(0.0);
            this.sidePaneLayer.setTranslateX(hiddenTranslate(sidePaneSize));
            return;
        }
        this.sidePaneLayer.setTranslateX(0.0);
        this.sidePaneLayer.setTranslateY(hiddenTranslate(sidePaneSize));
    }

    private javafx.beans.property.DoubleProperty activeTranslateProperty() {
        return isHorizontal() ? this.sidePaneLayer.translateXProperty() : this.sidePaneLayer.translateYProperty();
    }

    private double hiddenTranslate(final double sidePaneSize) {
        return switch (getDirection()) {
            case LEFT, TOP -> -sidePaneSize;
            case RIGHT, BOTTOM -> sidePaneSize;
        };
    }

    private double resolveSidePaneSize(final double width, final double height) {
        return isHorizontal()
            ? Math.min(getDefaultDrawerSize(), Math.max(0.0, width))
            : Math.min(getDefaultDrawerSize(), Math.max(0.0, height));
    }

    private boolean isHorizontal() {
        return getDirection() == DrawerDirection.LEFT || getDirection() == DrawerDirection.RIGHT;
    }

    private boolean isAnimationRunning() {
        return this.currentTimeline != null && this.currentTimeline.getStatus() == Animation.Status.RUNNING;
    }
}
