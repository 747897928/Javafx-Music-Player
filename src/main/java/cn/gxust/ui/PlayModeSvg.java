package cn.gxust.ui;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;

/**
 * <p>description:  </p>
 * <p>create: 2020/10/14 21:29</p>
 *
 * @author :zhaoyijie
 */


public class PlayModeSvg {

    private final SVGPath repeatSvgPath; /*列表循环*/

    private final SVGPath orderPlaySvgPath; /*顺序播放*/

    private final SVGPath repeatInOneSvgPath; /*单曲循环*/

    private final Region playModeRegion;

    public PlayModeSvg() {
        this.orderPlaySvgPath = new SVGPath();
        this.orderPlaySvgPath.setContent("M469.423554 213.304419h767.819824a42.695436 42.695436 0 1 0 0-85.332703h-767.819824a42.695436 42.695436 0 0 0 0 85.332703z m767.819824 255.939941h-767.819824a42.695436 42.695436 0 0 0 0 85.332703h767.819824a42.695436 42.695436 0 1 0 0-85.332703z m0 341.272645h-767.819824a42.695436 42.695436 0 0 0 0 85.332703h767.819824a42.695436 42.695436 0 1 0 0-85.332703zM213.483613-0.056423a42.637268 42.637268 0 0 0-42.637268 42.637267v837.970636l-100.572763-100.572764a41.124895 41.124895 0 0 0-58.168168 58.168169l163.743394 163.743394a42.637268 42.637268 0 0 0 37.402132 22.162072 43.277117 43.277117 0 0 0 5.002463-0.29084 40.717718 40.717718 0 0 0 37.343964-37.227628 43.218949 43.218949 0 0 0 0.290841-5.118799V42.639013A42.695436 42.695436 0 0 0 213.25094 0.001745z");
        this.repeatInOneSvgPath = new SVGPath();
        this.repeatInOneSvgPath.setContent("M1001.890909 511.348364c0 269.917091-218.810182 488.727273-488.727273 488.727272a46.545455 46.545455 0 1 1 0-93.090909A395.636364 395.636364 0 0 0 689.198545 156.858182l-76.474181-38.074182L801.978182 6.516364a46.545455 46.545455 0 0 1 47.476363 80.058181l-49.152 29.184a488.401455 488.401455 0 0 1 201.63491 395.589819zM0 508.090182C0 238.173091 218.810182 19.362909 488.727273 19.362909a46.545455 46.545455 0 0 1 0 93.090909A395.636364 395.636364 0 0 0 312.645818 862.487273l76.474182 38.074182-189.160727 112.314181a46.545455 46.545455 0 0 1-47.522909-80.058181l49.152-29.137455A488.401455 488.401455 0 0 1 0 508.090182zM495.709091 316.322909h49.105454v398.801455h-65.349818V395.077818c-24.017455 21.783273-54.178909 37.981091-91.042909 48.593455V378.88a236.916364 236.916364 0 0 0 56.971637-23.458909 249.018182 249.018182 0 0 0 50.26909-39.098182z");
        this.repeatSvgPath = new SVGPath();
        this.repeatSvgPath.setContent("M733.652618 639.977604a51.191042 51.191042 0 0 0-51.191042 88.662884l121.834679 70.336491A409.681906 409.681906 0 0 1 115.879128 407.723848L16.824463 381.667608c-57.48754 217.817882 34.758717 455.60027 239.215737 573.646812 244.846752 141.389657 557.931162 57.48754 699.269628-187.359212l-221.65721-127.977604zM767.950616 68.68558C523.103864-72.704077 210.019453 11.19804 68.680988 256.044792l221.65721 127.977604a51.191042 51.191042 0 0 0 51.191041-88.662884L219.69456 225.023021a409.681906 409.681906 0 0 1 688.417127 391.253131l99.054666 26.05624c57.48754-217.817882-34.758717-455.60027-239.215737-573.646812z");
        this.playModeRegion = new Region();
    }

    public void changeSvgPath(int playMode) {
        switch (playMode) {
            case 1:
                playModeRegion.setShape(repeatSvgPath);
                break;
            case 2:
                playModeRegion.setShape(orderPlaySvgPath);
                break;
            case 3:
                playModeRegion.setShape(repeatInOneSvgPath);
                break;
        }
    }

    public Region getPlayModeRegion(double wh, Paint paint) {
        playModeRegion.setShape(repeatSvgPath);
        playModeRegion.setMinSize(wh, wh);
        playModeRegion.setPrefSize(wh, wh);
        playModeRegion.setMaxSize(wh, wh);
        playModeRegion.setBackground(new Background(new BackgroundFill(paint, null, null)));
        return playModeRegion;
    }
}