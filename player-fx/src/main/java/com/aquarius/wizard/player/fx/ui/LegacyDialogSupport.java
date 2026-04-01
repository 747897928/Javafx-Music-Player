package com.aquarius.wizard.player.fx.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Restores the old custom "about" and confirmation dialogs without bringing
 * JFoenix back into the rebuilt module.
 */
public final class LegacyDialogSupport {

    private static final double ABOUT_WIDTH = 430.0;
    private static final double ABOUT_HEIGHT = 280.0;
    private static final double CONFIRM_WIDTH = 388.0;
    private static final double CONFIRM_HEIGHT = 220.0;

    private LegacyDialogSupport() {
    }

    public static void showAbout(final Stage owner) {
        final Stage dialog = createDialog(owner, "关于 WizardMusicBox", ABOUT_WIDTH, ABOUT_HEIGHT);
        final VBox body = new VBox(18.0);
        body.getStyleClass().add("legacy-dialog-body");

        final ImageView logoView = loadImageView("/images/topandbottom/pandefault.png", 54.0);
        final HBox headerRow = new HBox(16.0, logoView, buildAboutTextBlock());
        headerRow.setAlignment(Pos.CENTER_LEFT);

        final HBox actions = new HBox(createDialogButton("确定", true, dialog::close));
        actions.getStyleClass().add("legacy-dialog-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);

        body.getChildren().addAll(headerRow, actions);
        attachContent(dialog, body);
        dialog.showAndWait();
    }

    public static boolean showConfirm(final Stage owner, final String title, final String content) {
        final AtomicBoolean confirmed = new AtomicBoolean(false);
        final Stage dialog = createDialog(owner, title, CONFIRM_WIDTH, CONFIRM_HEIGHT);
        final VBox body = new VBox(18.0);
        body.getStyleClass().add("legacy-dialog-body");

        final HBox headerRow = new HBox(buildConfirmTextBlock(title, content));
        headerRow.setAlignment(Pos.TOP_LEFT);

        final Button cancelButton = createDialogButton("取消", false, dialog::close);
        final Button confirmButton = createDialogButton("确定", true, () -> {
            confirmed.set(true);
            dialog.close();
        });

        final HBox actions = new HBox(10.0, cancelButton, confirmButton);
        actions.getStyleClass().add("legacy-dialog-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);

        body.getChildren().addAll(headerRow, actions);
        attachContent(dialog, body);
        dialog.showAndWait();
        return confirmed.get();
    }

    private static VBox buildAboutTextBlock() {
        final Label appLabel = new Label("WizardMusicBox");
        appLabel.getStyleClass().add("legacy-dialog-heading");

        final Label versionLabel = new Label("version 2.1.5");
        versionLabel.getStyleClass().add("legacy-dialog-copy");

        final Label developerLabel = new Label("开发者：水瓶座鬼才");
        developerLabel.getStyleClass().add("legacy-dialog-copy");

        final Label blogLabel = new Label("个人博客：http://39.106.104.78:8080/blog/");
        blogLabel.getStyleClass().add("legacy-dialog-copy");
        blogLabel.setWrapText(true);

        final Label copyrightLabel = new Label("Copyright@水瓶座鬼才2020");
        copyrightLabel.getStyleClass().add("legacy-dialog-copy");

        final VBox textBlock = new VBox(8.0, appLabel, versionLabel, developerLabel, blogLabel, copyrightLabel);
        textBlock.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textBlock, Priority.ALWAYS);
        return textBlock;
    }

    private static VBox buildConfirmTextBlock(final String title, final String content) {
        final Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("legacy-dialog-heading");

        final Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("legacy-dialog-copy");
        contentLabel.setWrapText(true);

        final VBox textBlock = new VBox(10.0, titleLabel, contentLabel);
        textBlock.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(textBlock, Priority.ALWAYS);
        return textBlock;
    }

    private static Button createDialogButton(final String text, final boolean primary, final Runnable action) {
        final Button button = new Button(text);
        button.getStyleClass().add("legacy-dialog-button");
        button.getStyleClass().add(primary ? "legacy-dialog-button-primary" : "legacy-dialog-button-secondary");
        button.setOnAction(event -> action.run());
        MaterialButtonFeedback.install(button);
        return button;
    }

    private static Stage createDialog(final Stage owner, final String title, final double width, final double height) {
        final Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(false);
        dialog.setTitle(title);
        copyStageIcons(owner, dialog);

        final BorderPane shell = new BorderPane();
        shell.getStyleClass().add("legacy-dialog-shell");
        final HBox titleBar = buildTitleBar(dialog, title);
        shell.setTop(titleBar);

        final StackPane root = new StackPane(shell);
        root.getStyleClass().add("legacy-dialog-root");

        final Scene scene = new Scene(root, width, height);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(
            Objects.requireNonNull(LegacyDialogSupport.class.getResource("/css/player-shell.css")).toExternalForm()
        );
        dialog.setScene(scene);
        centerDialog(owner, dialog, width, height);
        enableDrag(dialog, titleBar);
        return dialog;
    }

    private static void attachContent(final Stage dialog, final VBox body) {
        final BorderPane shell = (BorderPane) ((StackPane) dialog.getScene().getRoot()).getChildren().get(0);
        shell.setCenter(body);
    }

    private static HBox buildTitleBar(final Stage dialog, final String title) {
        final ImageView logoView = loadImageView("/images/topandbottom/logo.png", 18.0);
        final Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("legacy-dialog-title");

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        final Button closeButton = new Button("×");
        closeButton.getStyleClass().add("legacy-dialog-close-button");
        closeButton.setOnAction(event -> dialog.close());
        MaterialButtonFeedback.install(closeButton);

        final HBox titleBar = new HBox(10.0, logoView, titleLabel, spacer, closeButton);
        titleBar.getStyleClass().add("legacy-dialog-titlebar");
        titleBar.setAlignment(Pos.CENTER_LEFT);
        return titleBar;
    }

    private static ImageView loadImageView(final String resourcePath, final double size) {
        final ImageView imageView = new ImageView();
        try (InputStream inputStream = LegacyDialogSupport.class.getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
                imageView.setImage(new Image(inputStream));
            }
        } catch (Exception ignored) {
        }
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("legacy-dialog-logo");
        return imageView;
    }

    private static void copyStageIcons(final Stage owner, final Stage dialog) {
        if (!owner.getIcons().isEmpty()) {
            dialog.getIcons().addAll(owner.getIcons());
        }
    }

    private static void centerDialog(final Stage owner, final Stage dialog, final double width, final double height) {
        final double ownerWidth = Math.max(owner.getWidth(), width);
        final double ownerHeight = Math.max(owner.getHeight(), height);
        dialog.setX(owner.getX() + ((ownerWidth - width) / 2.0));
        dialog.setY(owner.getY() + ((ownerHeight - height) / 2.0));
    }

    private static void enableDrag(final Stage dialog, final HBox titleBar) {
        final double[] dragOffset = new double[2];
        titleBar.setOnMousePressed(event -> {
            dragOffset[0] = event.getSceneX();
            dragOffset[1] = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            dialog.setX(event.getScreenX() - dragOffset[0]);
            dialog.setY(event.getScreenY() - dragOffset[1]);
        });
    }
}
