package com.aquarius.wizard.player.fx.ui;

import javafx.util.Duration;
import org.pomo.toasterfx.ToastBarToasterService;
import org.pomo.toasterfx.model.ToastParameter;
import org.pomo.toasterfx.model.impl.SingleAudio;

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
}

