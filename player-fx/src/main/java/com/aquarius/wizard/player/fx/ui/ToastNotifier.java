package com.aquarius.wizard.player.fx.ui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Project-local toast layer used by the rebuilt desktop shell.
 */
public final class ToastNotifier {

    private static final Duration DEFAULT_TIMEOUT = Duration.seconds(4.6);
    private static final Duration FADE_DURATION = Duration.millis(170.0);
    private static final double EDGE_GAP = 18.0;
    private static final double STACK_GAP = 12.0;

    private final Stage ownerStage;
    private final String stylesheetUrl;
    private final List<ToastEntry> activeToasts = new ArrayList<>();

    public ToastNotifier(final Stage ownerStage) {
        this.ownerStage = Objects.requireNonNull(ownerStage, "ownerStage");
        this.stylesheetUrl = Objects.requireNonNull(
            ToastNotifier.class.getResource("/css/player-shell.css"),
            "player-shell.css"
        ).toExternalForm();
    }

    public void info(final String title, final String message) {
        showToast(ToastKind.INFO, title, message, DEFAULT_TIMEOUT);
    }

    public void success(final String title, final String message) {
        showToast(ToastKind.SUCCESS, title, message, DEFAULT_TIMEOUT);
    }

    public void warn(final String title, final String message) {
        showToast(ToastKind.WARN, title, message, DEFAULT_TIMEOUT);
    }

    public void fail(final String title, final String message) {
        showToast(ToastKind.FAIL, title, message, DEFAULT_TIMEOUT);
    }

    public LoadingHandle loading(final String title, final String message) {
        return callOnFxThread(() -> {
            final ToastEntry entry = createToastEntry(ToastKind.LOADING, title, message, null, true);
            showEntry(entry);
            return new LoadingHandle(entry);
        });
    }

    public void destroy() {
        runOnFxThread(() -> {
            final List<ToastEntry> entries = new ArrayList<>(this.activeToasts);
            for (final ToastEntry entry : entries) {
                hideEntry(entry, false);
            }
            this.activeToasts.clear();
        });
    }

    private void showToast(final ToastKind kind, final String title, final String message, final Duration timeout) {
        runOnFxThread(() -> {
            final ToastEntry duplicateEntry = findDuplicateEntry(kind, title, message);
            if (duplicateEntry != null) {
                refreshEntry(duplicateEntry);
                return;
            }
            showEntry(createToastEntry(kind, title, message, timeout, false));
        });
    }

    private ToastEntry createToastEntry(
        final ToastKind kind,
        final String title,
        final String message,
        final Duration timeout,
        final boolean loading
    ) {
        final Label titleLabel = new Label(title == null ? "" : title);
        titleLabel.getStyleClass().add("toast-title");

        final Button closeButton = new Button();
        closeButton.getStyleClass().add("toast-close-button");
        closeButton.setGraphic(SvgIconFactory.createIcon(AppGlyphs.CLOSE, 11.0, Color.rgb(255, 255, 255, 0.88)));
        closeButton.setFocusTraversable(false);

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        final HBox header = new HBox(10.0, titleLabel, spacer, closeButton);
        header.setAlignment(Pos.TOP_LEFT);
        header.getStyleClass().add("toast-header");
        HBox.setMargin(closeButton, new Insets(-2.0, -2.0, 0.0, 6.0));

        final Label messageLabel = new Label(message == null ? "" : message);
        messageLabel.getStyleClass().add("toast-message");
        messageLabel.setWrapText(true);

        final VBox content = new VBox(6.0, header, messageLabel);
        content.setAlignment(Pos.CENTER_LEFT);
        content.getStyleClass().add("toast-copy");

        ProgressBar progressBar = null;
        if (loading) {
            progressBar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
            progressBar.getStyleClass().add("toast-progress");
            progressBar.getStyleClass().add("toast-progress-loading");
            progressBar.setMaxWidth(Double.MAX_VALUE);
            VBox.setMargin(progressBar, new Insets(4.0, 0.0, 0.0, 0.0));
            content.getChildren().add(progressBar);
        } else if (timeout != null) {
            progressBar = new ProgressBar(1.0);
            progressBar.getStyleClass().add("toast-progress");
            progressBar.getStyleClass().add("toast-progress-timed");
            progressBar.setMaxWidth(Double.MAX_VALUE);
            VBox.setMargin(progressBar, new Insets(4.0, 0.0, 0.0, 0.0));
            content.getChildren().add(progressBar);
        }

        final StackPane iconBadge = buildIconBadge(kind);
        final HBox row = new HBox(12.0, iconBadge, content);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("toast-card");
        row.getStyleClass().add("toast-card-" + kind.cssSuffix);
        row.setOpacity(0.0);

        final StackPane surface = new StackPane(row);
        surface.getStyleClass().add("toast-popup-shell");
        surface.getStylesheets().add(this.stylesheetUrl);

        final Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(false);
        popup.setHideOnEscape(false);
        popup.getContent().setAll(surface);

        final ToastEntry entry = new ToastEntry(
            popup,
            surface,
            row,
            messageLabel,
            progressBar,
            timeout,
            loading,
            normalizeText(title),
            normalizeText(message),
            kind
        );
        closeButton.setOnAction(event -> hideEntry(entry, true));
        installHoverHandling(entry);
        return entry;
    }

    private StackPane buildIconBadge(final ToastKind kind) {
        final Label symbolLabel = new Label(kind.symbol);
        symbolLabel.getStyleClass().add("toast-icon-label");

        final StackPane badge = new StackPane(symbolLabel);
        badge.getStyleClass().add("toast-icon-badge");
        badge.getStyleClass().add("toast-icon-badge-" + kind.cssSuffix);
        return badge;
    }

    private void showEntry(final ToastEntry entry) {
        if (entry == null || entry.closed.get()) {
            return;
        }
        if (!this.ownerStage.isShowing()) {
            return;
        }
        this.activeToasts.add(entry);
        entry.popup.show(this.ownerStage, 0.0, 0.0);
        entry.surface.applyCss();
        entry.surface.layout();
        relayoutToasts();

        final FadeTransition fadeIn = new FadeTransition(FADE_DURATION, entry.card);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        restartAutoClose(entry);
    }

    private void hideEntry(final ToastEntry entry, final boolean animate) {
        if (entry == null || !entry.closed.compareAndSet(false, true)) {
            return;
        }
        stopAutoClose(entry);
        if (!entry.popup.isShowing()) {
            this.activeToasts.remove(entry);
            return;
        }
        if (!animate) {
            entry.popup.hide();
            this.activeToasts.remove(entry);
            relayoutToasts();
            return;
        }
        final FadeTransition fadeOut = new FadeTransition(FADE_DURATION, entry.card);
        fadeOut.setFromValue(entry.card.getOpacity());
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            entry.popup.hide();
            this.activeToasts.remove(entry);
            relayoutToasts();
        });
        fadeOut.play();
    }

    private ToastEntry findDuplicateEntry(final ToastKind kind, final String title, final String message) {
        final String normalizedTitle = normalizeText(title);
        final String normalizedMessage = normalizeText(message);
        for (int index = this.activeToasts.size() - 1; index >= 0; index--) {
            final ToastEntry entry = this.activeToasts.get(index);
            if (entry.closed.get() || !entry.popup.isShowing()) {
                continue;
            }
            if (entry.kind != kind) {
                continue;
            }
            if (!Objects.equals(entry.title, normalizedTitle) || !Objects.equals(entry.message, normalizedMessage)) {
                continue;
            }
            return entry;
        }
        return null;
    }

    private void refreshEntry(final ToastEntry entry) {
        if (entry == null || entry.closed.get()) {
            return;
        }
        entry.shownAtNanos = System.nanoTime();
        this.activeToasts.remove(entry);
        this.activeToasts.add(entry);
        relayoutToasts();
        restartAutoClose(entry);
    }

    private void installHoverHandling(final ToastEntry entry) {
        entry.card.setOnMouseEntered(event -> pauseAutoClose(entry));
        entry.card.setOnMouseExited(event -> resumeAutoClose(entry));
    }

    private void restartAutoClose(final ToastEntry entry) {
        if (entry == null || entry.loading || entry.timeout == null || entry.progressBar == null) {
            return;
        }
        stopAutoClose(entry);
        entry.progressBar.setProgress(1.0);
        entry.autoCloseTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(entry.progressBar.progressProperty(), 1.0, Interpolator.LINEAR)),
            new KeyFrame(entry.timeout, event -> hideEntry(entry, true),
                new KeyValue(entry.progressBar.progressProperty(), 0.0, Interpolator.LINEAR))
        );
        entry.autoCloseTimeline.playFromStart();
    }

    private void stopAutoClose(final ToastEntry entry) {
        if (entry == null || entry.autoCloseTimeline == null) {
            return;
        }
        entry.autoCloseTimeline.stop();
        entry.autoCloseTimeline = null;
    }

    private void pauseAutoClose(final ToastEntry entry) {
        if (entry == null || entry.loading || entry.autoCloseTimeline == null) {
            return;
        }
        entry.autoCloseTimeline.pause();
    }

    private void resumeAutoClose(final ToastEntry entry) {
        if (entry == null || entry.loading || entry.autoCloseTimeline == null) {
            return;
        }
        entry.autoCloseTimeline.play();
    }

    private void relayoutToasts() {
        if (this.activeToasts.isEmpty()) {
            return;
        }
        final Rectangle2D visualBounds = resolveToastBounds();
        double currentY = visualBounds.getMaxY() - EDGE_GAP;
        final List<ToastEntry> visibleEntries = this.activeToasts.stream()
            .filter(entry -> entry.popup.isShowing())
            .sorted(Comparator.comparingInt(this.activeToasts::indexOf).reversed())
            .toList();

        for (final ToastEntry entry : visibleEntries) {
            entry.surface.applyCss();
            entry.surface.layout();
            final double toastWidth = resolvePrefWidth(entry.surface);
            final double toastHeight = resolvePrefHeight(entry.surface);
            final double popupX = Math.max(visualBounds.getMinX() + EDGE_GAP, visualBounds.getMaxX() - toastWidth - EDGE_GAP);
            currentY -= toastHeight;
            final double popupY = Math.max(visualBounds.getMinY() + EDGE_GAP, currentY);
            entry.popup.setX(popupX);
            entry.popup.setY(popupY);
            currentY = popupY - STACK_GAP;
        }
    }

    private Rectangle2D resolveToastBounds() {
        final List<Screen> screens = Screen.getScreensForRectangle(
            this.ownerStage.getX(),
            this.ownerStage.getY(),
            Math.max(this.ownerStage.getWidth(), 1.0),
            Math.max(this.ownerStage.getHeight(), 1.0)
        );
        return screens.isEmpty() ? Screen.getPrimary().getVisualBounds() : screens.get(0).getVisualBounds();
    }

    private double resolvePrefWidth(final Region region) {
        final double prefWidth = region.prefWidth(-1.0);
        return prefWidth > 0.0 ? prefWidth : region.getLayoutBounds().getWidth();
    }

    private double resolvePrefHeight(final Region region) {
        final double prefHeight = region.prefHeight(-1.0);
        return prefHeight > 0.0 ? prefHeight : region.getLayoutBounds().getHeight();
    }

    private void runOnFxThread(final Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        Platform.runLater(action);
    }

    private <T> T callOnFxThread(final java.util.function.Supplier<T> supplier) {
        if (Platform.isFxApplicationThread()) {
            return supplier.get();
        }
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<T> result = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                result.set(supplier.get());
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await(5L, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        return result.get();
    }

    private String normalizeText(final String value) {
        return value == null ? "" : value.trim();
    }

    public final class LoadingHandle {

        private final ToastEntry entry;
        private final AtomicBoolean closeRequested = new AtomicBoolean(false);

        private LoadingHandle(final ToastEntry entry) {
            this.entry = entry;
        }

        public void updateMessage(final String message) {
            if (message == null || this.entry == null) {
                return;
            }
            runOnFxThread(() -> this.entry.messageLabel.setText(message));
        }

        public void updateProgress(final double progressValue) {
            if (this.entry == null || this.entry.progressBar == null) {
                return;
            }
            runOnFxThread(() -> this.entry.progressBar.setProgress(progressValue));
        }

        public void close() {
            if (this.entry == null || !this.closeRequested.compareAndSet(false, true)) {
                return;
            }
            runOnFxThread(() -> hideEntry(this.entry, true));
        }
    }

    private enum ToastKind {
        INFO("i", "info"),
        SUCCESS("✓", "success"),
        WARN("!", "warn"),
        FAIL("×", "fail"),
        LOADING("…", "loading");

        private final String symbol;
        private final String cssSuffix;

        ToastKind(final String symbol, final String cssSuffix) {
            this.symbol = symbol;
            this.cssSuffix = cssSuffix;
        }
    }

    private static final class ToastEntry {

        private final Popup popup;
        private final StackPane surface;
        private final HBox card;
        private final Label messageLabel;
        private final ProgressBar progressBar;
        private final Duration timeout;
        private final boolean loading;
        private final String title;
        private final String message;
        private final ToastKind kind;
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private Timeline autoCloseTimeline;
        private long shownAtNanos = System.nanoTime();

        private ToastEntry(
            final Popup popup,
            final StackPane surface,
            final HBox card,
            final Label messageLabel,
            final ProgressBar progressBar,
            final Duration timeout,
            final boolean loading,
            final String title,
            final String message,
            final ToastKind kind
        ) {
            this.popup = popup;
            this.surface = surface;
            this.card = card;
            this.messageLabel = messageLabel;
            this.progressBar = progressBar;
            this.timeout = timeout;
            this.loading = loading;
            this.title = title;
            this.message = message;
            this.kind = kind;
        }
    }
}
