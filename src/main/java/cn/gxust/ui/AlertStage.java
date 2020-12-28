package cn.gxust.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.svg.SVGGlyph;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>description:  自定义弹框</p>
 * <p>create: 2020/12/13 11:29</p>
 *
 * @author :zhaoyijie
 */

public class AlertStage extends Stage {

    private final Label headerTextLabel;

    private final Label contentTextLabel;

    private ButtonType buttonType;

    /*使用 Platform.enterNestedEventLoop 暂停执行事件处理程序和 < code>
    Platform.exitNestedEventLoop （自JavaFX 9起可用）恢复执行：
    private final Object PAUSE_KEY = new Object（）;
    private void pause（）{Platform.enterNestedEventLoop（PAUSE_KEY）; }
    private void resume（）{Platform.exitNestedEventLoop（PAUSE_KEY，null）; }
    */

    public AlertStage(Stage primaryStage) {

        buttonType = ButtonType.CANCEL;/*初始为取消*/

        this.initOwner(primaryStage);

        this.initModality(Modality.WINDOW_MODAL);

        this.setTitle("删除音乐文件");

        VBox vBox = new VBox(10);

        SVGGlyph svgGlyph = new SVGGlyph("M512 853.333333a341.333333 341.333333 0 1 0 0-682.666666 341.333333 341.333333 0 0 0 0 682.666666z m0-768c235.648 0 426.666667 191.018667 426.666667 426.666667s-191.018667 426.666667-426.666667 426.666667S85.333333 747.648 85.333333 512 276.352 85.333333 512 85.333333z m1.706667 546.133334a55.466667 55.466667 0 1 0 0 110.933333 55.466667 55.466667 0 0 0 0-110.933333z m53.76-341.333334h-106.666667l21.333333 298.666667h64l21.333334-298.666667z", Color.BLACK);

        svgGlyph.setSize(25.0);

        headerTextLabel = new Label();

        contentTextLabel = new Label();

        JFXButton okButton = new JFXButton("确定");

        Color color1 = Color.BLACK;

        Border border = new Border(new BorderStroke(
                color1, color1, color1, color1,/*四个边的颜色*/
                BorderStrokeStyle.SOLID,/*四个边的线型--实线*/
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
                new BorderWidths(1),
                new Insets(1, 1, 1, 1)
        ));

        okButton.setBorder(border);

        okButton.setOnAction(event -> {
            buttonType = ButtonType.OK;
            this.hide();
        });

        headerTextLabel.setGraphic(svgGlyph);

        HBox hbox2 = new HBox(5);

        hbox2.setAlignment(Pos.CENTER_RIGHT);

        vBox.getChildren().addAll(headerTextLabel, contentTextLabel, okButton);

        vBox.setPadding(new Insets(10, 5, 5, 5));

        vBox.setAlignment(Pos.TOP_CENTER);

        JFXDecorator jfxDecorator = new JFXDecorator(this, vBox, false, false, false);

        ObservableList<Image> icons = primaryStage.getIcons();

        if (icons.size() != 0) {
            Image image = icons.get(0);
            this.getIcons().add(image);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(16.0);
            imageView.setPreserveRatio(true);
            jfxDecorator.setGraphic(imageView);
        }
        Scene scene = new Scene(jfxDecorator, 340, 148);

        scene.getStylesheets().addAll((getClass().getResource("/css/main.css").toExternalForm()), (getClass().getResource("/css/jfoenix-components.css").toExternalForm()));

        this.setScene(scene);

        this.setOnCloseRequest(event -> {
            buttonType = ButtonType.CANCEL;
            this.hide();
        });

        this.setResizable(false);
    }

    public void setHeaderText(String text) {
        headerTextLabel.setText(text);
    }

    public void setContentText(String text) {
        contentTextLabel.setText(text);
    }


    public ButtonType showWaitResult() {
        super.showAndWait();
        return buttonType;
    }
}
