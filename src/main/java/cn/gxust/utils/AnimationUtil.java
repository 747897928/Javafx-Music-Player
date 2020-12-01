package cn.gxust.utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;


public class AnimationUtil {
    
    /**
     * 淡入淡出动画
     *
     * @param node  节点
     * @param time  持续时间
     * @param delay 延时
     * @param from  开始透明度
     * @param to    结束透明度
     * @return 淡入淡出动画对象
     */
    public static FadeTransition fade(Node node, double time, double delay, double from, double to) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(time), node);
        fadeTransition.setInterpolator(Interpolator.LINEAR);
        fadeTransition.setDelay(Duration.seconds(delay));
        fadeTransition.setFromValue(from);
        fadeTransition.setToValue(to);
        fadeTransition.play();
        return fadeTransition;
    }
    
    /**
     * 缩放动画
     *
     * @param node  节点
     * @param time  持续时间
     * @param delay 延时
     * @param fromX X 轴起始大小
     * @param toX   X 轴结束大小
     * @param fromY Y 轴起始大小
     * @param toY   Y 轴结束大小
     */
    public static void scale(Node node, double time, double delay, double fromX, double toX, double fromY, double toY) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(time), node);
        scaleTransition.setInterpolator(Interpolator.LINEAR);
        scaleTransition.setDelay(Duration.seconds(delay));
        scaleTransition.setFromX(fromX);
        scaleTransition.setToX(toX);
        scaleTransition.setFromY(fromY);
        scaleTransition.setToY(toY);
        scaleTransition.play();
    }
    
    /**
     * 旋转动画
     *
     * @param node  节点
     * @param time  持续时间
     * @param delay 延时
     * @param from  开始角度
     * @param to    结束角度
     */
    public static void rotate(Node node, double time, double delay, double from, double to) {
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(time), node);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.setDelay(Duration.seconds(delay));
        rotateTransition.setAutoReverse(true);
        rotateTransition.setFromAngle(from);
        rotateTransition.setToAngle(to);
        rotateTransition.setCycleCount(2);
        rotateTransition.play();
    }
    
    /**
     * 绕点旋转
     *
     * @param node  节点
     * @param from  开始角度
     * @param to    结束角度
     * @param x     旋转中心 x
     * @param y     旋转中心 y
     * @param time  持续时间
     * @param delay 延时
     */
    public static void rotateByPosition(Node node, double from, double to, double x, double y, double time, double delay) {
        Rotate rotate = new Rotate(from, x, y);
        node.getTransforms().add(rotate);
        KeyValue keyValue = new KeyValue(rotate.angleProperty(), to);
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(time), keyValue);
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(keyFrame);
        timeline.setDelay(Duration.seconds(delay));
        timeline.play();
    }
    
    /**
     * 位移动画
     *
     * @param node  节点
     * @param time  持续时间
     * @param delay 延时
     * @param fromX X 轴起始位置
     * @param toX   X 轴结束位置
     * @param fromY Y 轴起始位置
     * @param toY   Y 轴结束位置
     */
    public static void translate(Node node, double time, double delay, double fromX, double toX, double fromY, double toY) {
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(time), node);
        translateTransition.setInterpolator(Interpolator.LINEAR);
        translateTransition.setDelay(Duration.seconds(delay));
        translateTransition.setFromX(fromX);
        translateTransition.setToX(toX);
        translateTransition.setFromY(fromY);
        translateTransition.setToY(toY);
        translateTransition.play();
    }


    /*private void closeLyricPane() {
        AnimationUtil.fade(musicDetailPane, 0.3, 0, 1, 0);
        AnimationUtil.fade(name, 0.5, 0.2, 0, 1);
        AnimationUtil.fade(author, 0.5, 0.2, 0, 1);
        AnimationUtil.fade(musicPic, 0.5, 0.2, 0, 1);
        AnimationUtil.fade(musicInfo, 0.5, 0.2, 0, 1);
        AnimationUtil.scale(musicDetailPane, 0.3, 0, 1, 0, 1, 0);
        AnimationUtil.translate(musicDetailPane, 0.3, 0, 0, -390, 0, 180);
    }*/

    /**
     * 打开主界面歌曲详情页
     */
    /*public void mouseClicked() {
        AnimationUtil.fade(musicDetailPane, 0.3, 0, 0, 0.9);
        AnimationUtil.fade(name, 0.3, 0, 1, 0);
        AnimationUtil.fade(author, 0.3, 0, 1, 0);
        AnimationUtil.fade(musicPic, 0.3, 0, 1, 0);
        AnimationUtil.fade(musicInfo, 0.3, 0, 1, 0);
        AnimationUtil.scale(musicDetailPane, 0.3, 0, 0, 1, 0, 1);
        AnimationUtil.translate(musicDetailPane, 0.3, 0, -390, 0, 180, 0);
    }*/

    /**
     * 歌曲详情页封面旋转
     *//*
    private void initMusicBigPane() {
        musicBigPicTT = new RotateTransition(Duration.seconds(30), musicBigPic);
        musicBigPicTT.setOnFinished(actionEvent -> musicBigPicTT.play());
        musicBigPicTT.setInterpolator(Interpolator.LINEAR);
        musicBigPicTT.setFromAngle(0);
        musicBigPicTT.setToAngle(360);
        musicBigPicTT.play();
    }*/

}
