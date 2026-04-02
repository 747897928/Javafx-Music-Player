package com.aquarius.wizard.player.fx;

import com.aquarius.wizard.player.fx.ui.PlayerShellView;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Rectangle2D;

import java.io.InputStream;

/**
 * JavaFX entry point for the rebuilt desktop client.
 */
public final class PlayerFxApplication {

    private static final double MIN_WIDTH = 960.0;
    private static final double MIN_HEIGHT = 680.0;
    private static final double PREFERRED_MAX_WIDTH = 1460.0;
    private static final double PREFERRED_MAX_HEIGHT = 920.0;
    private static final double SCREEN_MARGIN = 72.0;

    private PlayerShellView shellView;

    public void start(final Stage stage) {
        Platform.setImplicitExit(false);
        stage.initStyle(StageStyle.TRANSPARENT);
        this.shellView = new PlayerShellView(stage);
        final Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        final double initialWidth = calculateInitialWidth(visualBounds);
        final double initialHeight = calculateInitialHeight(visualBounds);

        final Scene scene = new Scene(this.shellView.getView(), initialWidth, initialHeight);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(
            PlayerFxApplication.class.getResource("/css/player-shell.css").toExternalForm()
        );
        this.shellView.configureResponsiveLayout(scene.widthProperty(), scene.heightProperty());

        stage.setTitle("Wizard Music Box");
        loadStageIcon(stage);
        stage.setMinWidth(Math.min(MIN_WIDTH, visualBounds.getWidth()));
        stage.setMinHeight(Math.min(MIN_HEIGHT, visualBounds.getHeight()));
        stage.setScene(scene);
        stage.setOnCloseRequest(this.shellView::handlePrimaryStageCloseRequest);
        stage.centerOnScreen();
        stage.show();
    }

    public void stop() {
        if (this.shellView != null) {
            this.shellView.shutdown();
        }
    }

    public static void main(final String[] args) {
        launchStandalone();
    }

    public static void launchStandalone() {
        Platform.startup(() -> {
            final PlayerFxApplication application = new PlayerFxApplication();
            final Stage stage = new Stage();
            application.start(stage);
        });
    }

    private double calculateInitialWidth(final Rectangle2D visualBounds) {
        final double maxAllowedWidth = Math.max(820.0, visualBounds.getWidth() - SCREEN_MARGIN);
        final double preferredWidth = Math.min(PREFERRED_MAX_WIDTH, visualBounds.getWidth() * 0.88);
        return clamp(preferredWidth, Math.min(MIN_WIDTH, maxAllowedWidth), maxAllowedWidth);
    }

    private double calculateInitialHeight(final Rectangle2D visualBounds) {
        final double maxAllowedHeight = Math.max(620.0, visualBounds.getHeight() - SCREEN_MARGIN);
        final double preferredHeight = Math.min(PREFERRED_MAX_HEIGHT, visualBounds.getHeight() * 0.9);
        return clamp(preferredHeight, Math.min(MIN_HEIGHT, maxAllowedHeight), maxAllowedHeight);
    }

    private double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void loadStageIcon(final Stage stage) {
        try (InputStream inputStream = PlayerFxApplication.class.getResourceAsStream("/images/topandbottom/logo.png")) {
            if (inputStream != null) {
                stage.getIcons().add(new Image(inputStream));
            }
        } catch (Exception ignored) {
        }
    }
}

