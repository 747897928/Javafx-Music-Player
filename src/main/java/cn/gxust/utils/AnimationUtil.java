package cn.gxust.utils;

import javafx.animation.*;
import javafx.scene.Node;
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
     */
    public static void fade(Node node, double time, double delay, double from, double to) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(time), node);
        fadeTransition.setInterpolator(Interpolator.LINEAR);
        fadeTransition.setDelay(Duration.seconds(delay));
        fadeTransition.setFromValue(from);
        fadeTransition.setToValue(to);
        fadeTransition.play();
    }

    /**
     * 旋转动画
     *
     * @param node 节点
     * @param time 持续时间
     * @param fromAngle 起始角度
     * @param toAngle 终点角度
     * @param count 动画次数
     */
    public static void rotate(Node node, double time, double fromAngle, double toAngle, int count) {
        RotateTransition rt = new RotateTransition(Duration.millis(time), node);
        rt.setFromAngle(fromAngle);
        rt.setToAngle(toAngle);
        rt.setCycleCount(count);
        rt.play();
    }
}
