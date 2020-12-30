package cn.gxust.ui;

import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.svg.SVGGlyph;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * <p>description: 关于界面 </p>
 * <p>create: 2020/10/17 13:51</p>
 *
 * @author zhaoyijie
 * @version v1.0
 */
public class AboutStage extends Stage {

    public AboutStage(String appName, Image logoImage) {

        this.getIcons().add(logoImage);

        this.setTitle("关于");

        VBox vBox = new VBox(10);

        ImageView imageView = new ImageView("images/topandbottom/pandefault.png");

        imageView.setFitWidth(30.0);

        imageView.setPreserveRatio(true);

        Label label = new Label(appName, imageView);

        Label label1 = new Label("version 2.1.5");

        Label label2 = new Label("开发者：水瓶座鬼才");

        Label label3 = new Label("个人博客：http://39.106.104.78:8080/blog/");

        Label label4 = new Label("Copyright@水瓶座鬼才2020");

        vBox.getChildren().addAll(label, label1, label2, label3, label4);

        vBox.setPadding(new Insets(10, 5, 5, 5));

        vBox.setAlignment(Pos.TOP_CENTER);

        JFXDecorator jfxDecorator = new JFXDecorator(this, vBox, false, false, false);

        SVGGlyph icoSvg = new SVGGlyph("M1217.32934 1021.586a33.846 33.846 0 0 1-22-8.462l-56.41-50.205a329.435 329.435 0 0 0-431.537 0l-56.41 50.205a33.846 33.846 0 0 1-44.564 0l-56.41-50.205a329.435 329.435 0 0 0-431.536 0l-62.051 52.462A33.846 33.846 0 0 1 0.00034 989.996V307.435a33.846 33.846 0 0 1 67.692 0v605.843a397.69 397.69 0 0 1 520.664 0l35.539 31.026 35.538-31.026a397.69 397.69 0 0 1 520.664 0V307.435a33.846 33.846 0 1 1 67.692 0V987.74a33.846 33.846 0 0 1-33.846 33.846zM625.58834 811.74a39.487 39.487 0 0 1-25.949-9.59l-43.436-37.794a232.973 232.973 0 0 0-305.178 0L207.59034 802.15a39.487 39.487 0 0 1-66-29.898V247.076A247.64 247.64 0 0 1 389.23034 0H418.00034a247.64 247.64 0 0 1 247.64 247.076v524.613a39.487 39.487 0 0 1-23.128 36.103 38.923 38.923 0 0 1-16.923 3.948zM403.33234 627.844a311.383 311.383 0 0 1 182.205 56.41V247.076a169.23 169.23 0 0 0-169.23-169.23H389.23034A169.23 169.23 0 0 0 220.00034 247.076v438.87a311.383 311.383 0 0 1 183.332-58.102zM625.58834 811.74a38.923 38.923 0 0 1-16.36-3.384 39.487 39.487 0 0 1-23.127-36.103V247.076A247.64 247.64 0 0 1 833.17734 0h28.769a247.64 247.64 0 0 1 247.076 247.076v524.613a39.487 39.487 0 0 1-66 29.898l-43.436-37.795a232.973 232.973 0 0 0-305.178 0l-43.436 37.795a39.487 39.487 0 0 1-25.384 10.153zM833.17734 78.41a169.23 169.23 0 0 0-169.23 169.23v438.87a313.076 313.076 0 0 1 364.973 0V247.076a169.23 169.23 0 0 0-169.23-169.23z");

        icoSvg.setFill(Color.WHITE);

        icoSvg.setSize(16.0);

        jfxDecorator.setGraphic(icoSvg);

        Scene scene = new Scene(jfxDecorator, 340, 200);

        scene.getStylesheets().addAll((getClass().getResource("/css/main.css").toExternalForm()), (getClass().getResource("/css/jfoenix-components.css").toExternalForm()));

        this.setScene(scene);

        this.setTitle(appName);

        this.setOnCloseRequest(event -> this.hide());
    }
}
