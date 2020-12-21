package cn.gxust.ui;

import com.jfoenix.controls.JFXButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


/**
 * @author zhaoyijie
 */
public class LrcStage extends MusicStage {

    private final BorderPane borderPane;

    private final Label lrcStageLabel;

    private final PlaySvg playSvg;

    public LrcStage(MainApp mainApp, Image logoImage) {
        playSvg = new PlaySvg();
        borderPane = new BorderPane();
        borderPane.setStyle("-fx-border-color:  rgba(0, 0, 0, 0.3);-fx-background-color:  rgba(0, 0, 0, 0.1)");
        borderPane.setPrefSize(740, 80);
        lrcStageLabel = new Label("暂无歌词");
        borderPane.setCenter(lrcStageLabel);
        BorderPane.setMargin(lrcStageLabel, new Insets(5, 5, 5, 5));
        lrcStageLabel.setFont(Font.font("Timer New Roman",
                FontWeight.BOLD, FontPosture.ITALIC, 18));
        lrcStageLabel.setStyle("-fx-background-radius: 2.0em;-fx-effect: dropshadow(three-pass-box, rgb(0,0,0), 5.0,0.6, 0, 0);");
        lrcStageLabel.setAlignment(Pos.CENTER);//字体居中
        lrcStageLabel.setTextFill(Color.WHITE);
        /*隐藏图标+个性化定制*/
        Stage stage = new Stage();
        stage.setWidth(1.0);
        stage.setHeight(1.0);
        stage.initStyle(StageStyle.UTILITY);
        stage.setOpacity(0.0);
        this.diyStage(true, 0, 320, borderPane);
        this.initOwner(stage);
        this.addDragEvent();
        this.initTopPane(mainApp);
        this.getIcons().add(logoImage);
        stage.show();
    }

    public void initTopPane(MainApp mainApp) {
        HBox hBox = new HBox(5);

        Paint paint = Paint.valueOf("#958686");

        double w = 18.0;
        Region preMusicRegion = playSvg.getPreRegion(w, paint);
        JFXButton b1 = new JFXButton("", preMusicRegion);
        b1.setOnAction(event -> {
            mainApp.preMusic();
        });

        Region playOrPauseRegion = playSvg.getPlayOrPauseRegion(w, paint);

        JFXButton b2 = new JFXButton("", playOrPauseRegion);
        b2.setOnAction(event -> {
            mainApp.playOrPauseMusic();
        });

        Region nextMusicSvgRegion = playSvg.getNextRegion(w, paint);
        JFXButton b3 = new JFXButton("", nextMusicSvgRegion);
        b3.setOnAction(event -> {
            mainApp.nextMusic();
        });
        hBox.getChildren().addAll(b1, b2, b3);
        //4.右侧的关闭按钮

        Region closeRegion = playSvg.getCloseRegion(15.0, paint);
        //充当关闭按钮
        JFXButton closeButton = new JFXButton("", closeRegion);
        closeButton.setOnAction(e -> {
            this.hide();//隐藏lrcpane
        });
        Region region = new Region();
        region.setPrefSize(borderPane.getPrefWidth() / 2 - 50, 2);
        BorderPane bo = new BorderPane();
        bo.setLeft(region);
        bo.setCenter(hBox);
        bo.setRight(closeButton);
        hBox.setLayoutY(-10);
        Insets in1 = new Insets(5, 5, 5, 5);
        bo.setMargin(closeButton, in1);
        bo.setMargin(hBox, in1);
        bo.setPadding(in1);
        this.borderPane.setTop(bo);
    }

    public void changeSvgPath(PlayStatus playStatus) {
        playSvg.changeSvgPath(playStatus);
    }

    public Label getLrcStageLabel() {
        return lrcStageLabel;
    }
}
