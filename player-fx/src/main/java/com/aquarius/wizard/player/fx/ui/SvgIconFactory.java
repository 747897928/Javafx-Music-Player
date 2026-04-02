package com.aquarius.wizard.player.fx.ui;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;

/**
 * Small SVG icon factory that scales raw path data without bringing back a
 * heavy third-party icon dependency.
 */
public final class SvgIconFactory {

    private SvgIconFactory() {
    }

    public static Node createIcon(final String svgPathContent, final double targetSize, final Paint fill) {
        final SVGPath icon = new SVGPath();
        icon.setContent(svgPathContent);
        icon.setFill(fill);
        final Bounds bounds = icon.getLayoutBounds();
        final double maxDimension = Math.max(bounds.getWidth(), bounds.getHeight());
        final double scale = maxDimension <= 0.0 ? 1.0 : targetSize / maxDimension;
        icon.setScaleX(scale);
        icon.setScaleY(scale);

        final StackPane holder = new StackPane(icon);
        holder.setMinSize(targetSize, targetSize);
        holder.setPrefSize(targetSize, targetSize);
        holder.setMaxSize(targetSize, targetSize);
        holder.setPickOnBounds(false);
        return holder;
    }
}

