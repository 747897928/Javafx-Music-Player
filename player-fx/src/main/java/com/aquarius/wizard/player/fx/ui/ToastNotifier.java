package com.aquarius.wizard.player.fx.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.pomo.toasterfx.ToastBarToasterService;
import org.pomo.toasterfx.model.ToastParameter;
import org.pomo.toasterfx.model.ToastState;
import org.pomo.toasterfx.model.impl.SingleToast;
import org.pomo.toasterfx.model.impl.SingleAudio;
import org.pomo.toasterfx.model.impl.ToastTypes;

/**
 * Thin wrapper around ToasterFX so the rebuilt client can keep the old
 * non-blocking feedback style without spreading third-party details around the
 * UI layer.
 */
public final class ToastNotifier {

    private final ToastBarToasterService toasterService = new ToastBarToasterService();
    private final ToastParameter defaultParameter;

    public ToastNotifier() {
        this.toasterService.initialize();
        this.toasterService.applyDarkTheme();
        this.defaultParameter = createDefaultParameter();
    }

    public void info(final String title, final String message) {
        this.toasterService.info(title, message, this.defaultParameter);
    }

    public void success(final String title, final String message) {
        this.toasterService.success(title, message, this.defaultParameter);
    }

    public void warn(final String title, final String message) {
        this.toasterService.warn(title, message, this.defaultParameter);
    }

    public void fail(final String title, final String message) {
        this.toasterService.fail(title, message, this.defaultParameter);
    }

    public LoadingHandle loading(final String title, final String message) {
        final Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("Microsoft YaHei", 16.0));

        final Label messageLabel = new Label(message);
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("Microsoft YaHei", 13.0));

        final ProgressBar progressBar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        final VBox content = new VBox(titleLabel, messageLabel, progressBar);
        content.setSpacing(4.0);
        content.setPadding(new Insets(4.0, 6.0, 4.0, 6.0));

        final SingleToast toast = new SingleToast(this.defaultParameter, ToastTypes.INFO, ignored -> content);
        this.toasterService.push(toast);
        return new LoadingHandle(toast, messageLabel, progressBar);
    }

    public void destroy() {
        this.toasterService.destroy();
    }

    private ToastParameter createDefaultParameter() {
        try {
            final SingleAudio customAudio = new SingleAudio(
                ToastNotifier.class.getResource("/audio/custom.mp3")
            );
            return ToastParameter.builder()
                .audio(customAudio)
                .timeout(Duration.seconds(5.0))
                .build();
        } catch (Exception exception) {
            return ToastParameter.builder()
                .timeout(Duration.seconds(5.0))
                .build();
        }
    }

    public static final class LoadingHandle {

        private final SingleToast toast;
        private final Label messageLabel;
        private final ProgressBar progressBar;

        private LoadingHandle(final SingleToast toast, final Label messageLabel, final ProgressBar progressBar) {
            this.toast = toast;
            this.messageLabel = messageLabel;
            this.progressBar = progressBar;
        }

        public void updateMessage(final String message) {
            if (message == null) {
                return;
            }
            Platform.runLater(() -> this.messageLabel.setText(message));
        }

        public void updateProgress(final double progressValue) {
            Platform.runLater(() -> this.progressBar.setProgress(progressValue));
        }

        public void close() {
            Platform.runLater(() -> {
                final ToastState state = this.toast.getState();
                if (state == ToastState.SHOWING || state == ToastState.SHOWN) {
                    this.toast.close();
                }
            });
        }
    }
}

