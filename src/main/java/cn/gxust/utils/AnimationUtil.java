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
}
