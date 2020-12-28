package cn.gxust.ui;

import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * <p>description:  </p>
 * <p>create: 2020/10/15 7:55</p>
 *
 * @author zhaoyijie
 * @version v1.0
 */
public abstract class MusicStage extends Stage {

    private double xOffSet = 0;

    private double yOffSet = 0;

    /**
     * 包括任务栏的电脑屏幕对象
     */
    public Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

    public MusicStage() {

    }

    /**
     * 自定义窗体
     *
     * @param isAlwaysOnTop 是否总是置顶
     */
    public void diyStage(boolean isAlwaysOnTop, double woffset, double hoffset, Pane pane) {
        Scene scene = new Scene(pane);
        scene.setFill(null);
        this.setScene(scene);
        double width = pane.getPrefWidth();
        double height = pane.getPrefHeight();
        this.initStyle(StageStyle.UNDECORATED);
        this.initStyle(StageStyle.TRANSPARENT);
        this.setAlwaysOnTop(isAlwaysOnTop);
        this.setX((screenBounds.getWidth() - width) / 2 + woffset);
        this.setY((screenBounds.getHeight() - height) / 2 + hoffset);
    }

    /**
     * 添加拖拽
     */
    public void addDragEvent() {
        Parent root = this.getScene().getRoot();
        root.setOnMousePressed(event -> {
            xOffSet = event.getSceneX();
            yOffSet = event.getSceneY();
            event.consume();
        });
        root.setOnMouseDragged(event -> {
            this.setX(event.getScreenX() - xOffSet);
            this.setY(event.getScreenY() - yOffSet);
            event.consume();
        });
    }

    /**
     *
     * @return 返回包括任务栏的整个屏幕宽度
     */
    public double getScreenWidth() {
        return screenBounds.getWidth();
    }

    /**
     *
     * @return 返回包括任务栏的整个屏幕高度
     */
    public double getScreenHeight() {
        return screenBounds.getHeight();
    }
}
