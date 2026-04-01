package com.aquarius.wizard.player.fx.ui;

import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 * Adds lightweight press feedback so standard JavaFX buttons feel closer to a
 * Material action surface during the rebuild stage.
 */
public final class MaterialButtonFeedback {

    private static final Duration PRESS_DURATION = Duration.millis(110.0);
    private static final Duration RELEASE_DURATION = Duration.millis(160.0);
    private static final double PRESSED_SCALE = 0.94;
    private static final String TRANSITION_KEY = "material-feedback-transition";

    private MaterialButtonFeedback() {
    }

    public static void install(final ButtonBase button) {
        install((Node) button);
    }

    public static void install(final Node node) {
        node.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> animate(node, PRESSED_SCALE, PRESS_DURATION));
        node.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> animate(node, 1.0, RELEASE_DURATION));
        node.addEventHandler(MouseEvent.MOUSE_EXITED, event -> animate(node, 1.0, RELEASE_DURATION));
    }

    private static void animate(final Node node, final double scale, final Duration duration) {
        final Object cachedTransition = node.getProperties().get(TRANSITION_KEY);
        if (cachedTransition instanceof ScaleTransition scaleTransition) {
            scaleTransition.stop();
        }
        final ScaleTransition transition = new ScaleTransition(duration, node);
        transition.setToX(scale);
        transition.setToY(scale);
        node.getProperties().put(TRANSITION_KEY, transition);
        transition.playFromStart();
    }
}

