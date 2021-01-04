package cn.gxust.ui;

import cn.gxust.cloudutils.CloudMusicSpider;
import cn.gxust.cloudutils.CloudRequest;
import cn.gxust.cloudutils.FileDownService;
import cn.gxust.localioutils.LocalMusicUtils;
import cn.gxust.pojo.PlayBean;
import cn.gxust.pojo.PlayListBean;
import cn.gxust.utils.AnimationUtil;
import cn.gxust.utils.Log4jUtils;
import com.jfoenix.controls.*;
import com.jfoenix.effects.JFXDepthManager;
import com.jfoenix.svg.SVGGlyph;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.pomo.toasterfx.ToastBarToasterService;
import org.pomo.toasterfx.model.ToastParameter;
import org.pomo.toasterfx.model.impl.SingleAudio;

import java.awt.PopupMenu;
import java.awt.SplashScreen;
import java.awt.TrayIcon;
import java.awt.SystemTray;
import javax.imageio.ImageIO;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.RED;

/**
 * <p>description: 主界面</p>
 * <p>create: 2019/10/14 12:28</p>
 *
 * @author zhaoyijie
 * @version v1.0
 */
public class MainApp extends Application {

    private Stage mainStage;/*全局的"舞台"对象*/

    private Label labGroupName;/* 歌单名称标签*/

    //8.播放列表的TableView
    private TableView<PlayBean> tableView;

    //精简模式的tabview
    private TableView<PlayBean> simplifyTableView;

    //9.当前播放歌曲的索引
    private int currentIndex;
    //10.当前播放的时间的前一秒--设置滚动条
    private int prevSecond;
    //11.当前播放的PlayBean
    private PlayBean currentPlayBean;
    //12.下侧面板的：总时间
    private Label labTotalTime;

    private ImageView songListCoverImageView; /*歌单封面imageView*/

    private ImageView rodImageView;/*碟片磁头图片*/

    private ImageView panImageView;/*碟片的ImageView对象*/

    private ImageView songCoverImageView;/*底部歌曲封面的ImageView对象*/

    //18.当前播放模式：
    private int playMode = 1;//1 : 列表循环；2. 顺序播放  3.单曲循环

    //19.播放时间滚动条对象
    private JFXSlider sliderSong;

    //20.已播放时间的Lable
    private Label labPlayTime;

    //21.音量滚动条
    private JFXSlider sldVolume;

    //24.显示歌词的VBox容器
    private VBox lrcVBox;

    //25.存储歌词时间的ArrayList
    private ArrayList<BigDecimal> lrcList;
    //26.当前歌词的索引
    private int currentLrcIndex;

    private Date date;

    private MediaPlayer mediaPlayer;

    private int currentSecond;

    private CloudRequest cloudRequest;//网易云请求工具类

    private SimpleDateFormat simpleDateFormat;

    private double millis;
    //判断此次是否在正常的播放区间
    private double min = 0;

    private double max = 0;

    private Label searchTiplabel;/*搜索歌曲时用来提示用户正在搜索的标签*/

    private CloudMusicSpider cloudMusicSpider;

    private ChangeListener<Duration> changeListener;/*播放进度监听器,复用对象*/

    private Runnable valueRunnable;/*歌曲播放完后的操作,复用对象*/

    private Label singerLabel;//用来显示歌手名的label

    private Label songNameLabel;//歌曲名label

    private Label siLab;//歌曲详情页用来显示歌手名的label

    private Label sNLab;//歌曲详情页歌曲名label

    private Label albumLabel; //歌曲详情页专辑名label

    private FlowPane flowPane;

    private ArrayList<PlayListBean> playListBeanList;

    private JFXTabPane tabPane;

    private final int PANIMAGVIEWSIZE = 200;//音乐封面Image最大大小

    private final int SONGLISTCOVERIMAGEVIEWSIZE = 120;//歌单封面Image最大大小

    private final int MUSICICOIMAGEVIEWSIZE = 80;//发现音乐图片大小

    private Label lrcStageLabel;

    private LrcStage lrcStage;

    private SimplifyModelStage simplifyModelStage;

    /*黑色半透明提示的总控制器，由它来产生提示消息，消息位于右下角弹出*/
    private ToastBarToasterService service;

    private ToastParameter customAudioParameter;

    private FileDownService fileDownService;

    private Image panDefaultImage;

    private PlaySvg playSvg;

    private VoiceSvg voiceSvg;

    private PlayModeSvg playModeSvg;

    private MaskerPane maskerPane;

    private JFXDrawer jfxDrawer;/*抽屉，抽屉弹出的节点是歌单详情页*/

    private RotateTransition rotateTransition;

    private AlertStage alertStage;/*删除歌曲的时候弹出的自定义弹框*/

    private final Font boldFont = Font.font("Timer New Roman", FontWeight.BOLD, FontPosture.ITALIC, 18);

    private final Font font = Font.font("黑体", 14);

    private Timeline t1;

    private boolean stopTimeline;/*是否停止t1动画，用来取消动画演示减少内存*/

    private Label tagsLabel;/*歌单标签*/

    private Label descLabel;/*歌单简介标签*/

    private Clipboard clipboard;/*剪切板*/

    private ClipboardContent clipboardContent;/*剪切板的内容*/

    private JFXListView leftListView;/*左侧列表*/

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.mainStage = primaryStage;
        Image logoImage = new Image(this.getClass().getResourceAsStream("/images/topandbottom/logo.png"));
        primaryStage.getIcons().add(logoImage);//设置logo;
        /*每隔20毫秒执行一次*/
        t1 = new Timeline(new KeyFrame(Duration.millis(20), event -> {
            lrcVBox.setLayoutY(lrcVBox.getLayoutY() - 15);
        }));
        t1.setCycleCount(3);//执行1次
        stopTimeline = false;
        date = new Date();
        alertStage = new AlertStage(primaryStage);
        cloudMusicSpider = new CloudMusicSpider();
        cloudRequest = new CloudRequest();
        simpleDateFormat = new SimpleDateFormat("mm:ss");
        lrcList = new ArrayList<>();
        service = new ToastBarToasterService();
        service.initialize();

        clipboard = Clipboard.getSystemClipboard();
        clipboardContent = new ClipboardContent();

        try {
            SingleAudio customAudio = new SingleAudio(this.getClass().getResource("/audio/custom.mp3"));
            customAudioParameter = ToastParameter.builder().audio(customAudio).timeout(Duration.seconds(5)).build();
        } catch (Exception e) {
            customAudioParameter = ToastParameter.builder().timeout(Duration.seconds(5)).build();
        }
        service.applyDarkTheme();/*使用黑色主题*/
        String appName = "WizardMusicBox";
        BorderPane mainborderPane = new BorderPane();

        Paint paint = Paint.valueOf("#1a3399");
        Background background = new Background(new BackgroundFill(paint, null, null));
        BorderPane bo2 = new BorderPane();
        bo2.setLeft(getLeftPane());
        bo2.setCenter(getCenterPane(background));

        lrcStage = new LrcStage(this, logoImage);

        lrcStageLabel = lrcStage.getLrcStageLabel();

        jfxDrawer = new JFXDrawer();
        /*弹出的位置，从下往上弹出*/
        jfxDrawer.setDirection(JFXDrawer.DrawerDirection.BOTTOM);
        /*抽屉是以bo2节点为起始点弹出*/
        jfxDrawer.setContent(bo2);
        /*mainborderPane.getStyleClass().add("bagNode");*/
        /*抽屉弹出的节点为getSidePane()方法返回的节点*/
        jfxDrawer.setSidePane(getSidePane());
        /*抽屉的默认展开大小为屏幕的最大高度*/
        jfxDrawer.setDefaultDrawerSize(lrcStage.getScreenHeight());
        /*隐藏歌曲详情页时暂停动画来减少资源消耗*/
        jfxDrawer.setOnDrawerClosed(event -> {
            if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                if (rotateTransition.getStatus() == Animation.Status.RUNNING) {
                    rotateTransition.pause();
                }
                stopTimeline = true;
                System.out.println(rotateTransition.getStatus());
            }
        });
        jfxDrawer.setOnDrawerOpened(event -> {
            if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                if (rotateTransition.getStatus() != Animation.Status.RUNNING) {
                    rotateTransition.play();
                }
                stopTimeline = false;
                System.out.println(rotateTransition.getStatus());

            }
        });

        primaryStage.setOnHidden(event -> {
            if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                if (rotateTransition.getStatus() == Animation.Status.RUNNING) {
                    rotateTransition.pause();
                }
                stopTimeline = true;
                System.out.println(rotateTransition.getStatus());
            }
        });

        primaryStage.setOnShown(event -> {
            if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                if (jfxDrawer.isOpened() && rotateTransition.getStatus() != Animation.Status.RUNNING) {
                    rotateTransition.play();
                }
                stopTimeline = false;
                System.out.println(rotateTransition.getStatus());
            }
        });
        mainborderPane.setCenter(jfxDrawer);

        BorderPane bottomPane = getBottomPane();
        bottomPane.setBackground(background);

        mainborderPane.setBottom(bottomPane);

        simplifyModelStage = new SimplifyModelStage(this, logoImage, appName, paint);

        TopJFXDecorator topJFXDecorator =
                new TopJFXDecorator(primaryStage, mainborderPane, this,
                        logoImage, appName, Color.WHITE);
        searchTiplabel = topJFXDecorator.getSearchTiplabel();
        JFXButton simplifyModelLabel = topJFXDecorator.getSimplifyModelButton();

        simplifyModelLabel.setOnMouseClicked(event -> {
            primaryStage.hide();
            simplifyModelStage.show();
        });
        simplifyModelLabel.setTooltip(new Tooltip("开启精简模式"));

        AboutStage aboutStage = new AboutStage(appName, logoImage);

        aboutStage.initOwner(primaryStage);

        aboutStage.initModality(Modality.WINDOW_MODAL);

        JFXButton aboutLabel = topJFXDecorator.getAboutButton();
        aboutLabel.setTooltip(new Tooltip("关于"));//设置提示文本
        aboutLabel.setOnMouseClicked(mouseEvent -> aboutStage.show());

        changeListener = initChangeListener();
        valueRunnable = initRunnable();

        TrayIcon trayIcon = initTray(appName, logoImage);

        double w = 780;
        double h = 600;

        //2.创建一个场景
        Scene scene = new Scene(topJFXDecorator, w, h);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        //3.将场景设置到舞台
        primaryStage.setScene(scene);

        primaryStage.setTitle(appName);

        primaryStage.setMinWidth(w);
        primaryStage.setMinHeight(h);

        topJFXDecorator.requestFocus();

        primaryStage.setOnCloseRequest(event -> {
            try {
                if (SystemTray.isSupported())
                    if (trayIcon != null)
                        SystemTray.getSystemTray().remove(trayIcon);
            } catch (Exception ignored) {
            }
            System.exit(0);
        });
        SplashScreen splashScreen = SplashScreen.getSplashScreen();
        if (splashScreen != null) splashScreen.close();
        primaryStage.show();/*显示舞台*/
        changePlaylist();
    }

    private TrayIcon initTray(String appName, Image logoImage) throws Exception {
        TrayIcon trayIcon = null;
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(logoImage, null);
            PopupMenu popupMenu = new PopupMenu();
            ActionListener actionListener = (event) -> {
                java.awt.MenuItem menuItem = (java.awt.MenuItem) event.getSource();
                String s = menuItem.getLabel();
                switch (s) {
                    case "prevMusic":
                        Platform.runLater(this::preMusic);
                        break;
                    case "pause/play":
                        Platform.runLater(this::playOrPauseMusic);
                        break;
                    case "nextMusic":
                        Platform.runLater(this::nextMusic);
                        break;
                    case "exitApp":
                        System.exit(0);
                        break;
                    default:
                        break;
                }
            };
            java.awt.MenuItem preItem = new java.awt.MenuItem("prevMusic");
            preItem.addActionListener(actionListener);
            java.awt.MenuItem playOrPauseItem = new java.awt.MenuItem("pause/play");
            playOrPauseItem.addActionListener(actionListener);
            java.awt.MenuItem nextItem = new java.awt.MenuItem("nextMusic");
            nextItem.addActionListener(actionListener);
            java.awt.MenuItem exitItem = new java.awt.MenuItem("exitApp");
            exitItem.addActionListener(actionListener);
            popupMenu.add(preItem);
            popupMenu.add(playOrPauseItem);
            popupMenu.add(nextItem);
            popupMenu.add(exitItem);
            if (bufferedImage != null) {
                trayIcon = new TrayIcon(bufferedImage, appName);
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip(appName);
                trayIcon.setPopupMenu(popupMenu);
                tray.add(trayIcon);
            }
        }
        return trayIcon;
    }


    //创建一个左侧面板
    private Node getLeftPane() {
        VBox vBox = new VBox();
        AnchorPane anchorPane = new AnchorPane();
        ImageView imageView = new ImageView("/images/topandbottom/materials.jpg");
        imageView.setFitWidth(180.0);
        imageView.setPreserveRatio(true);
        imageView.setLayoutX(0);
        imageView.setLayoutY(0);
        anchorPane.setPrefHeight(imageView.getFitHeight());
        ImageView userView = new ImageView("/images/topandbottom/user.jpg");
        userView.setFitWidth(60.0);
        userView.setPreserveRatio(true);
        Label userImgLabel = new Label("", userView);
        userImgLabel.setOnMouseEntered(event -> {
            RotateTransition r1 = new RotateTransition(Duration.seconds(0.5), userView);
            r1.setFromAngle(0);
            r1.setToAngle(360);
            r1.setCycleCount(1);
            r1.setAutoReverse(false);
            r1.play();
        });
        userImgLabel.setLayoutX(10);
        userImgLabel.setLayoutY(10);
        JFXDepthManager.setDepth(userImgLabel, 4);
        Circle c1 = new Circle();
        c1.centerXProperty().bind(userImgLabel.widthProperty().divide(2));
        c1.centerYProperty().bind(userImgLabel.widthProperty().divide(2));
        c1.radiusProperty().bind(userImgLabel.widthProperty().divide(2));
        userView.setClip(c1);

        Label userNameLabel = new Label("水瓶座鬼才");
        userNameLabel.setLayoutX(10);
        userNameLabel.setLayoutY(80);
        userNameLabel.setTextFill(Color.WHITE);

        Label mailLabel = new Label("747897928@qq.com");
        mailLabel.setLayoutX(10);
        mailLabel.setLayoutY(100);
        mailLabel.setTextFill(Color.WHITE);

        anchorPane.getChildren().addAll(imageView, userImgLabel, userNameLabel, mailLabel);

        leftListView = new JFXListView();
        leftListView.setPrefWidth(180.0);
        Paint paint = Paint.valueOf("#8a8a8a");
        SVGGlyph paperPlaneSvg = new SVGGlyph("M512.00404775 8C233.6469927 8 8 233.6550882 8 512.00404775s225.6469927 503.99595225 504.00404775 503.99595225 503.99595225-225.6469927 503.99595225-503.99595225S790.3530073 8 512.00404775 8z m-52.71006234 731.10014184V631.8666163l64.80479939 18.66831553z m200.99607314-64.48907292L463.45509308 613.80546622l175.71370064-207.37535864-226.73989111 192.52004231-184.10877549-55.77017709 533.35038094-258.24773264z", paint);
        paperPlaneSvg.setSize(20.0);
        Label findMusicGd = new Label("发现音乐", paperPlaneSvg);

        SVGGlyph songListSvg = new SVGGlyph("M1061.726316 59.230316l2.910316 359.262316a79.225263 79.225263 0 0 1-23.174737 56.589473l-521.485474 521.485474a79.225263 79.225263 0 0 1-111.993263 0l-335.97979-335.97979a79.225263 79.225263 0 0 1 0-111.993263L593.381053 27.109053a79.225263 79.225263 0 0 1 56.643368-23.174737l359.262316 2.910316a52.816842 52.816842 0 0 1 52.331789 52.385684z m-187.122527 134.736842a52.816842 52.816842 0 1 0-74.64421 74.64421 52.816842 52.816842 0 0 0 74.64421-74.64421z", paint);
        songListSvg.setSize(20.0);

        Label songListGd = new Label("歌单", songListSvg);

        SVGGlyph lrcSvg = new SVGGlyph("M872.727273 1024H151.272727C67.490909 1024 0 956.509091 0 872.727273V151.272727C0 67.490909 67.490909 0 151.272727 0h721.454546c83.781818 0 151.272727 67.490909 151.272727 151.272727v721.454546c0 83.781818-67.490909 151.272727-151.272727 151.272727zM151.272727 69.818182c-45.381818 0-81.454545 36.072727-81.454545 81.454545v721.454546c0 45.381818 36.072727 81.454545 81.454545 81.454545h721.454546c45.381818 0 81.454545-36.072727 81.454545-81.454545V151.272727c0-45.381818-36.072727-81.454545-81.454545-81.454545H151.272727zM576 314.181818h267.636364c22.109091 0 40.727273 18.618182 40.727272 40.727273S865.745455 395.636364 843.636364 395.636364h-267.636364c-22.109091 0-40.727273-18.618182-40.727273-40.727273s18.618182-40.727273 40.727273-40.727273zM576 488.727273h267.636364c22.109091 0 40.727273 18.618182 40.727272 40.727272S865.745455 570.181818 843.636364 570.181818h-267.636364c-22.109091 0-40.727273-18.618182-40.727273-40.727273s18.618182-40.727273 40.727273-40.727272zM576 663.272727h267.636364c22.109091 0 40.727273 18.618182 40.727272 40.727273S865.745455 744.727273 843.636364 744.727273h-267.636364c-22.109091 0-40.727273-18.618182-40.727273-40.727273s18.618182-40.727273 40.727273-40.727273zM610.909091 128H546.909091c-83.781818 0-151.272727 67.490909-151.272727 151.272727v308.363637c-26.763636-18.618182-58.181818-29.090909-93.090909-29.090909-89.6 0-162.909091 73.309091-162.909091 162.90909s73.309091 162.909091 162.909091 162.909091 162.909091-73.309091 162.90909-162.909091V279.272727c0-45.381818 36.072727-81.454545 81.454546-81.454545h64c19.781818 0 34.909091-15.127273 34.909091-34.909091s-15.127273-34.909091-34.909091-34.909091zM302.545455 814.545455c-51.2 0-93.090909-41.890909-93.09091-93.09091s41.890909-93.090909 93.09091-93.090909 93.090909 41.890909 93.090909 93.090909-41.890909 93.090909-93.090909 93.09091z", paint);
        lrcSvg.setSize(20.0);

        Label lrcGd = new Label("歌词", lrcSvg);


       /* SVGGlyph settingSvg = new SVGGlyph("M512 64c249.6 0 448 198.4 448 448s-198.4 448-448 448-448-198.4-448-448 198.4-448 448-448z m0-64C230.4 0 0 230.4 0 512s230.4 512 512 512 512-230.4 512-512-230.4-512-512-512zM758.4 591.36c-4.48 13.696-9.856 26.88-16.384 39.424 26.432 31.424 25.856 77.888-3.776 107.52a79.36 79.36 0 0 1-107.456 3.712 258.112 258.112 0 0 1-39.424 16.32A79.808 79.808 0 0 1 512 832a79.744 79.744 0 0 1-79.36-73.6 256.256 256.256 0 0 1-39.424-16.384 79.36 79.36 0 0 1-107.52-3.776 79.296 79.296 0 0 1-3.712-107.456 255.36 255.36 0 0 1-16.32-39.424A79.744 79.744 0 0 1 192 512c0-41.984 32.512-76.096 73.6-79.36 4.48-13.696 9.856-26.88 16.384-39.424a79.36 79.36 0 0 1 3.712-107.52 79.36 79.36 0 0 1 107.52-3.712c12.608-6.528 25.728-11.968 39.424-16.384A79.744 79.744 0 0 1 512 192c41.984 0 76.096 32.448 79.36 73.6 13.696 4.48 26.88 9.856 39.424 16.384a79.36 79.36 0 0 1 107.52 3.712 79.36 79.36 0 0 1 3.712 107.52c6.528 12.608 11.904 25.728 16.32 39.424C799.488 435.904 832 470.016 832 512s-32.512 76.096-73.6 79.36z m-65.536-203.648l17.152-17.152a40 40 0 0 0-56.576-56.512l-17.28 17.28a218.752 218.752 0 0 0-84.16-35.328V272a40 40 0 0 0-80 0v24c-30.848 5.76-59.264 18.176-84.16 35.264l-17.28-17.28a40 40 0 0 0-56.576 56.576l17.152 17.152a217.472 217.472 0 0 0-35.456 84.288h-23.68a40 40 0 0 0 0 80h23.68c5.76 30.272 17.792 59.008 35.456 84.288l-17.152 17.152a40 40 0 0 0 56.576 56.512l17.28-17.28a218.88 218.88 0 0 0 84.16 35.328v24a40 40 0 0 0 80 0v-24a218.56 218.56 0 0 0 84.16-35.264l17.28 17.28a40 40 0 0 0 56.576-56.576l-17.152-17.152c17.664-25.28 29.76-54.016 35.456-84.288h23.68a40 40 0 0 0 0-80h-23.68a217.472 217.472 0 0 0-35.456-84.288zM392 512a120 120 0 1 1 240 0 120 120 0 0 1-240 0z m40 0a80 80 0 1 0 160 0 80 80 0 0 0-160 0z", paint);
        settingSvg.setSize(20.0);
        settingSvg.setPadding(in1);
        JFXButton settingGd = new JFXButton("设置", settingSvg);
        settingGd.setOnAction(mouseEvent -> tabPane.getSelectionModel().select(3));*/

        SVGGlyph musicIco = new SVGGlyph("M592.1792 616.7552a62.4128 53.4528 0 1 0 124.8256 0 62.4128 53.4528 0 1 0-124.8256 0ZM306.9952 652.4416a62.4128 53.4528 0 1 0 124.8256 0 62.4128 53.4528 0 1 0-124.8256 0ZM512 0a512 512 0 1 0 512 512A512 512 0 0 0 512 0z m240.64 616.7552c0 49.0496-43.6736 89.1392-98.048 89.1392S556.544 665.6 556.544 616.7552s43.6736-89.088 98.048-89.088A103.5264 103.5264 0 0 1 716.8 547.84V336.0256a16.0768 16.0768 0 0 0-6.0416-13.4656 17.3056 17.3056 0 0 0-14.2848-4.4544l-213.8624 26.8288a18.2272 18.2272 0 0 0-16.0768 17.8176V421.376l176.4864-23.8592a17.5104 17.5104 0 1 1 4.4544 34.7648l-178.2784 24.064h-2.6624v184.6784a76.3392 76.3392 0 0 1 0.9216 11.4176c0 48.9984-43.6736 89.088-98.048 89.088S271.36 701.44 271.36 652.4416 315.0336 563.2 369.408 563.2a103.6288 103.6288 0 0 1 62.3616 20.48V362.752a54.6304 54.6304 0 0 1 47.2576-53.4528l213.9136-26.7264a58.9824 58.9824 0 0 1 41.8816 13.3632 52.6848 52.6848 0 0 1 17.8176 40.0896z", paint);
        musicIco.setSize(20.0);

        Tooltip tooltip = new Tooltip("(请将本地音乐放到LocalMusic文件夹下，让程序检测出来)");
        /*setTipTime(tooltip,10000);//10毫秒显示时间*/

        Label localMusiclab = new Label("本地音乐", musicIco);

        localMusiclab.setTooltip(tooltip);

        SVGGlyph dirIcoSVGGlyph = new SVGGlyph("M1239.3472 425.7792a62.0544 62.0544 0 0 0-39.5776-22.5792v-126.976c0-50.1248-40.6528-90.7264-90.6752-90.7264h-500.5312a10.0864 10.0864 0 0 1-3.1232-2.5088L585.216 99.84a99.4816 99.4816 0 0 0-36.352-55.3984A102.912 102.912 0 0 0 486.4 22.1696H116.2752C66.2016 22.1696 25.6 62.7712 25.6 112.7936v817.3568c0 20.2752 6.8096 39.936 19.3536 55.808l-0.3072 0.6144 4.096 4.4544c17.4592 19.2 41.2672 29.7984 67.072 29.7984 4.7616 0 327.7312 0.512 607.0272 0.512 140.8 0 270.4384-0.2048 342.784-0.512 39.936-0.1024 75.2128-29.3888 83.9168-69.5296l49.7664-230.0928 52.5824-242.5344a63.0784 63.0784 0 0 0-12.544-52.8896z m-48.7424-14.336h0.512-0.512zM515.072 206.08l0.256 1.1776c5.0176 19.6608 16.6912 37.2224 33.8432 50.5344 17.5616 13.824 37.7856 21.0944 58.624 21.0944h498.7904V402.944H223.0784c-29.696 0-54.9376 20.224-61.2864 49.3568l-42.8544 197.632V115.456h367.616c0.4608 0 2.5088 0.5632 5.0176 2.5088a10.5984 10.5984 0 0 1 2.9696 3.3792l20.48 84.6336z m637.1328 289.9456l-45.9264 213.0944-47.2576 218.112H154.112l93.3376-431.2064h904.704z", Paint.valueOf("#8a8a8a"));
        dirIcoSVGGlyph.setSize(20.0);
        Label dirIcoLab = new Label("本地音乐文件夹", dirIcoSVGGlyph);

        fileDownService = new FileDownService(this);

        SVGGlyph downSVGGlyph = new SVGGlyph("M54.272 470.528c-4.608-2.048-9.728-3.584-14.848-3.584-23.04 1.536-40.448 22.016-38.912 45.056-0.512 17.408 8.704 33.28 24.064 41.472l375.808 179.712c9.216 4.608 20.48 4.608 29.696 0l171.008-81.92c19.968-64.512 62.464-119.296 118.784-154.112L414.72 643.072 54.272 470.528zM590.336 754.176l-175.104 83.968L54.272 665.6c-4.608-2.048-9.728-3.584-14.848-3.584-23.04 2.048-40.448 22.016-38.912 45.056-0.512 17.408 8.704 33.28 24.064 41.472l375.808 179.712c9.216 4.608 20.48 4.608 29.696 0l181.248-87.04c-11.264-26.624-18.432-56.32-20.992-87.04zM24.064 358.4l375.808 179.712c4.608 2.048 9.728 3.584 14.848 3.584s10.24-1.024 14.848-3.584L805.888 358.4c23.04-13.312 30.72-42.496 17.92-65.536-4.096-7.168-10.24-13.312-17.92-17.92L430.08 95.232c-9.216-4.608-20.48-4.608-29.696 0L24.064 275.456c-23.04 13.312-30.72 42.496-17.408 65.536 4.096 7.168 10.24 13.312 17.408 17.408zM931.84 610.304v158.72h92.16l-159.232 159.232-159.232-159.232h92.16v-158.72H931.84z", Paint.valueOf("#8a8a8a"));
        downSVGGlyph.setSize(20.0);

        Label downMusicLab = new Label("下载当前音乐", downSVGGlyph);

        leftListView.getItems().addAll(findMusicGd, songListGd, lrcGd, localMusiclab, downMusicLab, dirIcoLab);

        leftListView.setCellFactory(param -> {
            JFXListCell jfxListCell = new JFXListCell();
            jfxListCell.setOnMouseClicked(event -> {
                int index = leftListView.getSelectionModel().getSelectedIndex();
                switch (index) {
                    case 0:
                        tabPane.getSelectionModel().select(0);
                        break;
                    case 1:
                        tabPane.getSelectionModel().select(1);
                        break;
                    case 2: {
                        if (jfxDrawer.getDefaultDrawerSize() < mainStage.getHeight()) {
                            jfxDrawer.setDefaultDrawerSize(mainStage.getHeight());
                        }
                        if (jfxDrawer.isClosed()) {
                            jfxDrawer.open();
                        } else {
                            jfxDrawer.close();
                        }
                    }
                    break;
                    case 3:
                        searchLocalMusic();
                        break;
                    case 4: {
                        if (currentPlayBean != null && !currentPlayBean.isPlayable()) {
                            service.fail("操作失败！", "无法播放的音乐不能下载", customAudioParameter);
                            return;
                        }
                        if (fileDownService.isRunning()) {
                            service.warn("警告！", "正在下载，期间不允许其他下载操作！", customAudioParameter);
                            return;
                        }
                        if (currentPlayBean != null) {
                            if (!currentPlayBean.isLocalMusic()) {
                                /*去除windows系统中文件名的非法路径*/
                                String validateMusicName = validateFileName(currentPlayBean.getSaveFileName());
                                String musicSavepath = LocalMusicUtils.LOCAL_MUSIC_DIR + validateMusicName + ".mp3";
                                fileDownService.setUrlAndSavepath(currentPlayBean.getMp3Url(), musicSavepath, validateMusicName);
                                fileDownService.start();
                            } else {
                                service.fail("操作错误", "本地音乐无法下载！", customAudioParameter);
                            }
                        } else {
                            service.fail("操作错误", "未选择歌曲，无法下载！", customAudioParameter);
                        }
                    }
                    break;
                    case 5:
                        LocalMusicUtils.openLocalDir();
                        break;
                    default:
                        break;
                }
            });
            return jfxListCell;
        });

        vBox.getChildren().addAll(anchorPane, leftListView);
        /*leftListView.setDepth(4);*/
        leftListView.setVerticalGap(10.0);
        leftListView.setExpanded(true);
        Color rgb144 = Color.rgb(114, 114, 114);
        Border border = new Border(new BorderStroke(
                rgb144, rgb144, rgb144, rgb144,/*四个边的颜色*/
                BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,/*四个边的线型--实线*/
                new CornerRadii(1),
                new BorderWidths(2), null));
        Border redBorder = new Border(new BorderStroke(
                RED, RED, RED, RED,
                BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                new CornerRadii(1),
                new BorderWidths(2), null));
        vBox.setBorder(border);
        vBox.setVgrow(leftListView, Priority.ALWAYS);
        vBox.setOnDragOver(event -> {
            event.acceptTransferModes(event.getTransferMode());
            event.consume();
        });
        vBox.setOnDragEntered(event -> {
            vBox.setBorder(redBorder);
            event.consume();
        });
        vBox.setOnDragExited(event -> {
            vBox.setBorder(border);
            event.consume();
        });
        vBox.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasFiles()) {
                List<File> files = dragboard.getFiles();
                new Thread(() -> {
                    service.info("长时间任务运行中，请耐心等待！", "开始复制音乐和lrc文件到本地音乐文件夹", customAudioParameter);
                    int[] countArr = LocalMusicUtils.copyMusicToLocalDir(files);
                    service.success("请手动点击本地音乐栏刷新表格！", countArr[0] + "个音乐文件，" + countArr[1] + "个lrc文件！", customAudioParameter);
                    Platform.runLater(() -> {
                        /**
                         * 如果复制成功的音乐文件数是一个或者多个，并且当前歌单是本地音乐歌单表格就需要
                         * 刷新，lrc文件更新不需要，因为lrc文件的读取是加载音乐的时候实时读取的,不过
                         * 经过检测，新拷贝过来的文件立马用jaudiotagger读取会出现Copying Primitives
                         * Read NumberFixed的警告，似乎复制过去的文件不能立马读取，一旦出现这些警告
                         * 内存就上涨就有点问题，似乎不回收，也不知道存不存在内存泄漏，不知道是不是
                         * jaudiotagger的一个bug
                         */
                        if (countArr[0] > 0 && labGroupName.getText().equals("本地音乐")) {
                            searchLocalMusic();
                        }
                    });
                }).start();
                event.consume();
            }
        });
        return vBox;
    }

    /**
     * @return 歌单详情页
     */
    private Node getSidePane() {

        StackPane s1 = new StackPane();
        ImageView iv1 = new ImageView("/images/topandbottom/pan.png");
        iv1.setFitHeight(250);
        iv1.setFitWidth(250);
        //4.碟片的图片
        panImageView = new ImageView("/images/topandbottom/pandefault.png");
        panImageView.setFitHeight(170);
        panImageView.setFitWidth(170);
        Label panLabel = new Label("", panImageView);
        panLabel.getStyleClass().add("hoverNode");
        Circle circle2 = new Circle();
        circle2.centerXProperty().bind(panLabel.widthProperty().divide(2));
        circle2.centerYProperty().bind(panLabel.heightProperty().divide(2));
        circle2.radiusProperty().bind(panLabel.widthProperty().divide(2));
        panImageView.setClip(circle2);
        s1.getChildren().addAll(iv1, panLabel);
        s1.setLayoutX(20);
        s1.setLayoutY(50);

        rotateTransition = new RotateTransition(Duration.seconds(60), panImageView);
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(360);
        // 无限循环
        rotateTransition.setCycleCount(Timeline.INDEFINITE);
        // 每次旋转后是否改变旋转方向
        rotateTransition.setAutoReverse(false);
        // RotateTransition旋转方式两轮旋转间有间隔

        rodImageView = new ImageView("/images/topandbottom/rodImageView.png");
        Circle circle1 = new Circle();
        circle1.setCenterX(125);
        circle1.setCenterY(125);
        circle1.setRadius(125);//圆的半径

        rodImageView.setClip(circle1);

        sNLab = new Label();
        sNLab.setLayoutX(300.0);
        sNLab.setLayoutY(35.0);
        sNLab.setPrefWidth(160.0);
        sNLab.setFont(new Font("黑体", 18));
        sNLab.setTextFill(Color.WHITE);
        sNLab.getStyleClass().add("shadowLabel");

        siLab = new Label("歌手");
        siLab.setLayoutX(480);
        siLab.setLayoutY(70);
        siLab.setPrefWidth(140.0);
        siLab.setTextFill(Color.WHITE);
        siLab.getStyleClass().add("shadowLabel");

        albumLabel = new Label("专辑：");
        albumLabel.setLayoutX(300);
        albumLabel.setLayoutY(70);
        albumLabel.setPrefWidth(140.0);
        albumLabel.setTextFill(Color.WHITE);
        albumLabel.getStyleClass().add("shadowLabel");

        //5.歌词的listview容器
        lrcVBox = new VBox(15);
        lrcVBox.setPadding(new Insets(20, 20, 20, 20));
        lrcVBox.setLayoutX(10);
        lrcVBox.setLayoutY(60);
        lrcVBox.setBackground(new Background(new BackgroundFill(Color.color(0.5, 0.5, 0.5, 0.3), new CornerRadii(15), null)));

        AnchorPane lrcPane = new AnchorPane();

        ScrollPane scrollPane = new ScrollPane();
        /*隐藏水平和垂直滚动条*/
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPadding(new Insets(0, 0, 0, 0));
        scrollPane.setContent(lrcPane);
        scrollPane.setMouseTransparent(true);/*使ScrollPane不接收鼠标事件*/

        lrcPane.prefWidthProperty().bind(scrollPane.widthProperty());
        lrcPane.prefHeightProperty().bind(scrollPane.heightProperty());
        //lrcPane.setStyle("-fx-background-color: rgba(0,0,0,0);");
        lrcPane.setStyle("-fx-background-color: transparent;");
        lrcPane.getChildren().addAll(lrcVBox);
        scrollPane.setPrefSize(400.0, 400.0);
        scrollPane.setLayoutX(300.0);
        scrollPane.setLayoutY(100.0);

        Paint paint = Paint.valueOf("#2B2B2B");

        SVGGlyph svgGlyph = new SVGGlyph("M-45.3,472l5.2-5.2c0.1-0.1,0.2-0.1,0.3,0l1,1c0.1,0.1,0.1,0.2,0,0.3l-5.2,5.2h3.8c0.2,0,0.4,0.2,0.4,0.4v1.2c0,0.1-0.1,0.2-0.2,0.2h-6.3c-0.4,0-0.8-0.4-0.8-0.8V468c0-0.1,0.1-0.2,0.2-0.2h1.2c0.2,0,0.4,0.2,0.4,0.4V472z M-28.7,458l-5.2,5.2c-0.1,0.1-0.2,0.1-0.3,0c0,0,0,0,0,0l-1-1c-0.1-0.1-0.1-0.2,0-0.3c0,0,0,0,0,0l5.2-5.2h-3.8c-0.2,0-0.4-0.2-0.4-0.4v-1.2c0-0.1,0.1-0.2,0.2-0.2h6.3c0.4,0,0.8,0.4,0.8,0.8v6.3c0,0.1-0.1,0.2-0.2,0.2h-1.2c-0.2,0-0.4-0.2-0.4-0.4C-28.7,461.8-28.7,458-28.7,458z", paint);
        svgGlyph.setSize(20.0);

        JFXButton button = new JFXButton("", svgGlyph);
        button.setOnAction(event -> {
            if (jfxDrawer.getDefaultDrawerSize() < mainStage.getHeight()) {
                jfxDrawer.setDefaultDrawerSize(mainStage.getHeight());
            }
            if (jfxDrawer.isClosed()) {
                jfxDrawer.open();
            } else {
                jfxDrawer.close();
            }
        });

        button.setLayoutX(640);
        button.setLayoutY(20);
        Border border = new Border(new BorderStroke(
                paint, paint, paint, paint,
                BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                new CornerRadii(1), new BorderWidths(2),
                new Insets(1, 1, 1, 1)
        ));
        button.setBorder(border);
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().addAll(button, s1, rodImageView, scrollPane, sNLab, siLab, albumLabel);
        HBox h4 = new HBox();
        h4.getChildren().add(anchorPane);
        /*把自定义布局放到hbox里面，设置居中对齐，让自定义布局在整体上始终展示在正中央*/
        h4.setAlignment(Pos.CENTER);
        h4.getStyleClass().add("bag2Node");
        return h4;
    }

    //创建一个中间的面板
    private Node getCenterPane(Background background) {
        Font boldFont = Font.font("Timer New Roman", FontWeight.BOLD, FontPosture.ITALIC, 14);
        //2.歌单：标签
        Label lab1 = new Label("歌单：");
        lab1.setTextFill(BLACK);
        lab1.setFont(boldFont);
        Paint paint = background.getFills().get(0).getFill();
        lab1.setBorder(new Border(new BorderStroke(
                paint, paint, paint, paint,/*四个边的颜色*/
                BorderStrokeStyle.SOLID,/*四个边的线型--实线*/
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
                new BorderWidths(5),
                new Insets(2, 2, 2, 2)
        )));
        lab1.setLayoutX(180);
        lab1.setLayoutY(10);
        lab1.setPrefWidth(80);
        lab1.setPrefHeight(25);
        lab1.setAlignment(Pos.CENTER);

        //3.歌单名称：标签
        labGroupName = new Label("默认歌单");
        labGroupName.setLayoutX(270);
        labGroupName.setLayoutY(14);
        labGroupName.setTextFill(BLACK);
        labGroupName.setFont(boldFont);
        labGroupName.setPrefWidth(250);
        labGroupName.setPrefHeight(25);
        labGroupName.setAlignment(Pos.CENTER_LEFT);

        Paint svgPaint = Paint.valueOf("#8a8a8a");

        SVGGlyph tagSvg = new SVGGlyph("M1061.726316 59.230316l2.910316 359.262316a79.225263 79.225263 0 0 1-23.174737 56.589473l-521.485474 521.485474a79.225263 79.225263 0 0 1-111.993263 0l-335.97979-335.97979a79.225263 79.225263 0 0 1 0-111.993263L593.381053 27.109053a79.225263 79.225263 0 0 1 56.643368-23.174737l359.262316 2.910316a52.816842 52.816842 0 0 1 52.331789 52.385684z m-187.122527 134.736842a52.816842 52.816842 0 1 0-74.64421 74.64421 52.816842 52.816842 0 0 0 74.64421-74.64421z", svgPaint);
        tagSvg.setSize(15.0);
        tagsLabel = new Label("标签：", tagSvg);
        tagsLabel.setPrefSize(200.0, 10.0);
        tagsLabel.setLayoutX(200);
        tagsLabel.setLayoutY(50);

        SVGGlyph descSvg = new SVGGlyph("M513.787 61.037c-248.303 0-449.593 201.29-449.593 449.593s201.29 449.593 449.593 449.593S963.38 758.933 963.38 510.63 762.091 61.037 513.787 61.037zM319.569 737.201H210.537V410.098h109.032v327.103z m490.639-297.52v52.199c0 6.954-1.502 13.628-3.951 19.9l-82.185 192.173c-8.174 19.49-27.531 33.256-50.153 33.256H428.601c-30.123 0-54.521-24.398-54.521-54.521V410.105c0-14.996 6.134-28.62 15.948-38.57l179.508-179.501 29.03 28.757c7.357 7.357 11.993 17.58 11.993 28.897 0 2.996-0.41 5.861-0.957 8.583l-26.034 124.571h172.127c30.123 0 54.521 24.398 54.521 54.521l-0.28 2.046 0.272 0.272z", svgPaint);
        descSvg.setSize(15.0);
        descSvg.setLayoutX(200);
        descSvg.setLayoutY(72);
        descLabel = new Label("介绍：");
        descLabel.setPrefWidth(330.0);

        descLabel.setWrapText(true);
        descLabel.setLayoutX(220);
        descLabel.setLayoutY(72);
        descLabel.setMaxHeight(40.0);

        panDefaultImage = new Image("/images/topandbottom/pandefault.png");
        songListCoverImageView = new ImageView(panDefaultImage);
        songListCoverImageView.setFitWidth(SONGLISTCOVERIMAGEVIEWSIZE);
        songListCoverImageView.setFitHeight(SONGLISTCOVERIMAGEVIEWSIZE);

        Label songListCoverLabel = new Label("", songListCoverImageView);
        songListCoverLabel.setLayoutX(30);
        songListCoverLabel.setLayoutY(14);
        songListCoverLabel.getStyleClass().add("hoverNode");

        songListCoverLabel.setOnDragDetected(event -> {
            dragToPC(songListCoverImageView);
        });

        SVGGlyph copyUrlSvg = new SVGGlyph("M339.57 697c1 0.86 1.79 1 1.16 0.46a21.38 21.38 0 0 0-2-1.18c0.27 0.27 0.56 0.52 0.84 0.72zM428.33 696.49c-0.5 0.34-1 0.75-1.45 1.13a5.06 5.06 0 0 0 0.81-0.58zM393.21 711.57c3-0.3 1.54-0.23-0.07 0h-0.28zM392.86 711.6h-0.07c-0.43 0.06-0.86 0.14-1.2 0.22 0.41-0.09 0.84-0.16 1.27-0.22zM338.73 696.32l-0.06-0.06-0.56-0.28zM317.65 668.51c0.42 1.14 1.06 1.81 0.82 1.11-0.39-0.8-0.82-1.58-1.27-2.34zM542.41 582.32a3.85 3.85 0 0 0 0.76-1.21 18.8 18.8 0 0 0-1.45 2zM552.76 564.32l-0.35 0.57a2 2 0 0 0-0.08 0.22c0.14-0.27 0.28-0.54 0.43-0.79zM429.15 696l-0.58 0.29-0.24 0.22a5.54 5.54 0 0 1 0.82-0.51zM556.72 526.64v0.94c0 0.29 0.12 0.6 0.17 0.91a15 15 0 0 1-0.17-1.85zM557 528.94l-0.09-0.45c0.05 0.3 0.09 0.59 0.11 0.88s0 0.64 0.08 1a6.14 6.14 0 0 0-0.1-1.43zM541.72 583.12l-0.08 0.09c-0.09 0.18-0.19 0.37-0.28 0.57a7.16 7.16 0 0 1 0.36-0.66zM317.2 667.28v-0.07l-0.36-0.57zM552.33 565.11c-0.42 0.77-0.8 1.59-1.16 2.43a3.83 3.83 0 0 0 0.76-1.35c0.13-0.36 0.26-0.72 0.4-1.08zM312.69 630.75c0.05-0.3 0.11-0.6 0.16-0.89s0-0.58 0-0.9a14 14 0 0 1-0.16 1.79zM375.67 711.83l-0.38-0.08zM597.65 326.45l-1 0.71c-2.73 1.92-0.1 0.16 1-0.71zM374.05 711.57h0.42c-1.73-0.22-3.61-0.32-0.42 0zM706.72 411.9l0.55-1.14a3.69 3.69 0 0 0 0.39-1.19c0 0.16-0.11 0.32-0.17 0.48-0.18 0.5-0.49 1.2-0.77 1.85zM706.72 411.9l-0.13 0.26c0 0.16-0.06 0.31-0.09 0.48a4.3 4.3 0 0 1 0.22-0.74zM512 65C265.13 65 65 265.13 65 512s200.13 447 447 447 447-200.13 447-447S758.87 65 512 65z m91.65 492.62c-3.82 26.66-17.91 49.76-36.69 68.54l-83.15 83.15C472.15 721 461 733 446.92 741.88c-32.36 20.59-74.27 23.52-109.41 8.68S275 704.24 267.1 666.7c-8.17-38.92 2.64-79.4 30-108.46 21.52-22.83 44.42-44.48 66.6-66.66 9.48-9.48 24.35-8.79 33.92 0s8.88 25 0 33.91l-2.53 2.51L356 567.12l-18.34 18.33c-1.41 1.41-2.83 2.82-4.23 4.24-1.16 1.16-2.3 2.33-3.41 3.54s-2.22 2.87-3.47 4.09l-0.45 0.63a92.07 92.07 0 0 0-6.27 10.27c-0.72 1.36-1.37 2.74-2 4.13-0.21 0.54-0.45 1.08-0.65 1.6a96.77 96.77 0 0 0-3.51 11.91c-0.3 1.33-0.55 2.66-0.8 4-0.06 3.35-0.31 3.57-0.36 2.8a97.31 97.31 0 0 0-0.29 10.66c0 1.6 0.13 3.19 0.25 4.79l0.12 1.25c0.88 3.74 1.38 7.56 2.44 11.28 0.51 1.77 1.08 3.53 1.69 5.27 0.15 0.43 0.31 0.87 0.48 1.3a14 14 0 0 1 1.29 2.41c1.27 2.53 2.35 5.22 3.79 7.62 1 1.63 2 3.23 3.05 4.79 0.46 0.68 4.1 6.28 1.93 2.78-2-3.29 0.24 0.21 0.87 0.94s1.28 1.46 1.93 2.18q1.48 1.64 3 3.19c1.39 1.38 2.81 2.72 4.28 4l1.3 1.12a11.4 11.4 0 0 1 2.06 1.24c2.51 1.65 4.92 3.71 7.46 5.17 1.62 0.93 3.27 1.82 4.95 2.65 0.27 0.14 1.27 0.61 2.22 1.06 0.6 0.25 1.32 0.55 1.64 0.67 0.87 0.31 1.74 0.62 2.61 0.91 1.75 0.59 3.52 1.12 5.29 1.61 3.26 0.88 6.6 1.31 9.87 2.06 0.67 0.07 1.34 0.13 2 0.18 1.82 0.13 3.64 0.2 5.47 0.23a97.37 97.37 0 0 0 10.53-0.43l0.35-0.05c0.4-0.06 0.79-0.11 1.16-0.18q2.71-0.48 5.39-1.13a95.25 95.25 0 0 0 10.57-3.2l0.61-0.25c3.89-2 1.62-0.71 0 0l-1.18 0.59c0.62-0.31 1.28-0.56 1.91-0.86q2.55-1.17 5-2.5a94.64 94.64 0 0 0 9.65-5.94c0.2-0.14 0.41-0.31 0.62-0.47-0.91 0.51-1 0 1.69-1.35 1.18-1 2.35-2 3.49-3.1 0.79-0.75 1.56-1.51 2.34-2.28 8.67-8.58 17.25-17.26 25.88-25.88q37.55-37.54 75.08-75.08c1.42-1.42 2.83-2.84 4.18-4.32q1.07-1.18 2.1-2.4c1.13-2.23 1.66-2.56 1.53-2.1 0.53-0.67 1.07-1.33 1.51-2a93.61 93.61 0 0 0 5.38-9.12c0.4-0.78 0.75-1.62 1.11-2.46-0.27 0.24-0.15-0.41 1.24-2.65l0.48-1.31a95 95 0 0 0 3-10.6c0.43-1.92 0.67-3.9 1.07-5.82 0-0.45 0.09-0.91 0.13-1.37a95.38 95.38 0 0 0 0.23-11c-0.05-1.5-0.15-3-0.27-4.49 0 0.77-0.3 0.57-0.36-2.76-0.6-3.16-1.27-6.29-2.16-9.38-0.64-2.21-1.36-4.41-2.15-6.57-0.18-0.49-0.41-1-0.61-1.51C550.3 507 548.75 504 547 501c-1.1-1.83-2.27-3.61-3.49-5.36-0.26-0.37-0.53-0.73-0.8-1.09-2-2.31-4-4.64-6.18-6.82-2-2-4.14-3.88-6.28-5.75a6.14 6.14 0 0 1-0.51-0.51c-0.48-0.31-1-0.6-1.29-0.83a99 99 0 0 0-11.28-6.77c-11.89-6.18-14.43-21.79-8.61-32.81 6.26-11.84 21.69-14.39 32.81-8.61a122 122 0 0 1 26.61 19c29.2 27.22 41.26 67.16 35.67 106.17z m143.22-118.87c-9.37 18.46-24.65 32.38-39.06 46.8l-32.92 32.91c-9.47 9.48-24.34 8.79-33.91 0s-8.88-25 0-33.91l2.66-2.66 37.82-37.82c3.36-3.37 6.75-6.72 10.1-10.1 1.38-1.4 2.73-2.82 4-4.29 0.47-0.53 2-2.44 2.46-3 0.58-0.81 1.15-1.6 1.3-1.83q1.39-2.06 2.69-4.18c1.68-2.77 3.13-5.63 4.55-8.53 0.53-2.82 1.14-3.28 1.07-2.59 0.49-1.36 1-2.73 1.39-4.11a93.74 93.74 0 0 0 2.65-10.69c0.07-0.35 0.13-0.69 0.19-1 0.13-1.38 0.32-2.77 0.41-4.15a95.23 95.23 0 0 0 0.08-10.95c-0.08-1.59-0.2-3.18-0.36-4.76-0.65-3.54-1.38-7-2.38-10.51q-0.76-2.66-1.68-5.27c-0.16-0.45-0.62-1.63-1-2.53-0.56-1.21-1.31-2.81-1.53-3.26-0.74-1.46-1.53-2.9-2.35-4.33-1.66-2.89-3.53-5.64-5.44-8.37l-1.12-1.35q-1.9-2.22-3.95-4.32a95 95 0 0 0-7-6.5l-0.24-0.2c-3.32-2.26-1.21-1 0 0l1.15 0.79c-0.59-0.4-1.15-0.86-1.73-1.28q-2.31-1.68-4.72-3.21a92.24 92.24 0 0 0-9.21-5.16l-1-0.5A49.63 49.63 0 0 1 665 316a90.23 90.23 0 0 0-9.3-2.6c-1.73-0.39-3.48-0.68-5.23-1l-0.59-0.07a95 95 0 0 0-10.26-0.4q-3.09 0-6.16 0.28l-1.48 0.15c-1 0.15-2.53 0.39-3 0.48-1.8 0.35-3.59 0.76-5.37 1.21a110 110 0 0 0-10.48 3.34l-0.09 0.05q-2.22 1-4.4 2.16a94.3 94.3 0 0 0-9.64 5.9c-0.34 0.23-0.66 0.46-1 0.7-0.58 1.08-2.86 2.32-3.78 3.15-1.59 1.42-3.11 2.92-4.63 4.43q-13.1 13.05-26.15 26.15l-72.61 72.61-2.43 2.46c-0.77 0.78-1.54 1.55-2.28 2.35-1.34 1.42-2.6 2.89-3.87 4.36l-0.2 0.23a95.52 95.52 0 0 0-6.09 9.57 90.25 90.25 0 0 0-2.28 4.36c-0.17 0.34-0.33 0.69-0.49 1-0.54 3.28-2.57 6.89-3.44 10.1s-1.41 6.25-2 9.4c0 0.37-0.07 0.75-0.1 1.12a90.66 90.66 0 0 0-0.31 5.47 95.11 95.11 0 0 0 0.25 10.27c0 0.38 0.07 0.76 0.11 1.14 0.37 1.51 0.48 3.2 0.79 4.7a93.43 93.43 0 0 0 2.46 9.33c0.56 1.76 1.23 3.47 1.85 5.2 0.1 0.24 0.2 0.48 0.31 0.7a94.37 94.37 0 0 0 5 9.28c1.12 1.81 2.29 3.59 3.53 5.33l0.33 0.45c2.05 2.33 4 4.68 6.24 6.88 9.48 9.48 8.79 24.34 0 33.92s-25 8.88-33.92 0a126.55 126.55 0 0 1-16.47-20.58C419 520.48 414 481.9 425.07 449c7.09-21.1 19.61-38.11 35.15-53.65l79.83-79.84c9.93-9.92 19.46-19.92 30.68-28.44 28-21.28 65.87-27.8 99.77-19.38 33.68 8.37 64.31 33 78.47 64.85 15.28 34.35 15.03 72.46-2.1 106.21zM328.07 595.22c0.16-0.23 0.2-0.27 0 0zM312.69 630.75c0 0.17-0.07 0.34-0.1 0.51a6.17 6.17 0 0 0-0.1 1.4c0-0.32 0.05-0.65 0.08-1s0.07-0.59 0.12-0.91z", BLACK);
        copyUrlSvg.setSize(20.0);
        ContextMenu songListCoverMenu = new ContextMenu();
        MenuItem item1 = new MenuItem("复制歌单封面链接", copyUrlSvg);
        item1.setOnAction(event -> {
            clipboardContent.clear();
            /*String url = songListCoverImageView.getImage().impl_getUrl();*/
            String url = songListCoverImageView.getImage().getUrl();
            /*jdk8 not have getUrl() in javafx.scene.image.Image
            since jdk9 impl_getUrl() has been replaced getUrl()*/
            url = url.replace("?param=300y300", "");
            clipboardContent.putString(url);
            clipboard.setContent(clipboardContent);
        });
        songListCoverMenu.getItems().add(item1);
        songListCoverLabel.setContextMenu(songListCoverMenu);


        Color color1 = Color.rgb(114, 114, 114);

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

        //6.歌单列表标签
        Label lab3 = new Label("歌单列表");
        lab3.setPrefWidth(80);
        lab3.setPrefHeight(25);
        lab3.setTextFill(Color.WHITE);
        lab3.setAlignment(Pos.CENTER);
        lab3.setBackground(background);
        lab3.setLayoutX(30);
        lab3.setLayoutY(140);

        JFXTextField filterTextField = new JFXTextField();
        filterTextField.setPromptText("搜索表格中的音乐");
        filterTextField.setLabelFloat(true);

        filterTextField.setLayoutX(340);
        filterTextField.setLayoutY(140);

        SVGGlyph searchRegion = new SVGGlyph("M160.021999 409.536004C160.021999 254.345703 286.286107 128.081595 441.476408 128.081595 596.730704 128.081595 722.994813 254.345703 722.994813 409.536004 722.994813 564.726305 596.730704 690.990413 441.476408 690.990413 286.286107 690.990413 160.021999 564.726305 160.021999 409.536004M973.219174 864.867546 766.320105 657.904481C819.180801 588.916793 850.986813 502.970164 850.986813 409.536004 850.986813 183.758115 667.318293 0.089594 441.476408 0.089594 215.698519 0.089594 32.029998 183.758115 32.029998 409.536004 32.029998 635.313893 215.698519 818.982414 441.476408 818.982414 527.743016 818.982414 607.738016 792.104093 673.781889 746.410949L882.728829 955.35789C895.208049 967.83711 911.591026 974.108718 927.974002 974.108718 944.356978 974.108718 960.739954 967.83711 973.219174 955.35789 998.24161 930.335454 998.24161 889.825986 973.219174 864.867546", Paint.valueOf("#8a8a8a"));
        searchRegion.setSize(20.0);

        JFXButton searchButton = new JFXButton("", searchRegion);
        searchButton.setOnAction(actionEvent -> {
            String text = filterTextField.getText().trim();
            if (!text.equals("")) {
                //System.out.println(text);
                text = text.toLowerCase();
                ObservableList<PlayBean> list = tableView.getItems();
                TableView.TableViewSelectionModel<PlayBean> selectionModel = tableView.getSelectionModel();
                selectionModel.clearSelection();
                for (int i = 0; i < list.size(); i++) {
                    PlayBean playBean = list.get(i);
                    if (requireNonNullElse(playBean.getMusicName(), "").toLowerCase().contains(text) ||
                            requireNonNullElse(playBean.getArtistName(), "").toLowerCase().contains(text) ||
                            requireNonNullElse(playBean.getAlbum(), "").toLowerCase().contains(text)) {
                        //System.out.println(playBean);
                        selectionModel.select(i);
                        tableView.scrollTo(i);
                    }
                }
            }
        });
        searchButton.setLayoutX(500);
        searchButton.setLayoutY(140);
        searchButton.setId("searchButton");

        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().addAll(lab1, labGroupName, songListCoverLabel,
                tagsLabel, descSvg, descLabel, lab3, filterTextField, searchButton);
        anchorPane.setPrefHeight(170);

        anchorPane.setBorder(border);

        tableView = new TableView<>();
        tableView.setBorder(border);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.setPrefWidth(540);
        TableColumn c1 = new TableColumn("音乐标题");
        c1.setPrefWidth(180);
        c1.setCellValueFactory(new PropertyValueFactory<>("musicName"));

        TableColumn c2 = new TableColumn("歌手");
        c2.setPrefWidth(180);
        c2.setCellValueFactory(new PropertyValueFactory<>("artistName"));

        TableColumn c3 = new TableColumn("专辑");
        c3.setPrefWidth(190);
        c3.setCellValueFactory(new PropertyValueFactory<>("album"));

        tableView.getColumns().addAll(c1, c2, c3);
        ObservableList<PlayBean> tableObList = FXCollections.observableArrayList();
        tableView.setItems(tableObList);
        simplifyTableView = new TableView<>();

        SVGGlyph svg1 = new SVGGlyph("M512 1024C229.248 1024 0 794.752 0 512S229.248 0 512 0s512 229.248 512 512-229.248 512-512 512z m0-938.666667C276.352 85.333333 85.333333 276.352 85.333333 512s191.018667 426.666667 426.666667 426.666667 426.666667-191.018667 426.666667-426.666667S747.648 85.333333 512 85.333333z m253.269333 441.856c-0.725333 2.048-0.896 4.181333-1.962666 6.144-0.469333 0.938667-1.365333 1.450667-1.92 2.346667-1.450667 2.346667-3.370667 4.309333-5.290667 6.357333-1.749333 1.834667-3.370667 3.669333-5.418667 5.077334-0.853333 0.64-1.408 1.621333-2.346666 2.218666l-333.482667 205.184a42.410667 42.410667 0 0 1-24.661333 12.245334c-1.92 0.426667-3.797333 0.981333-5.76 1.152L384 768c-0.341333 0-0.597333-0.170667-0.938667-0.213333a37.930667 37.930667 0 0 1-29.098666-12.202667c-0.170667-0.128-0.256-0.341333-0.426667-0.512-2.346667-2.517333-4.906667-4.778667-6.656-8.021333-0.597333-1.109333-0.597333-2.304-1.066667-3.456-1.066667-2.176-1.536-4.565333-2.176-6.912-0.725333-2.730667-1.493333-5.333333-1.664-8.106667C341.888 727.424 341.333333 726.485333 341.333333 725.333333V299.648c-0.170667-7.594667 0.853333-15.274667 4.650667-22.314667a39.253333 39.253333 0 0 1 34.517333-20.650666C381.738667 256.597333 382.762667 256 384 256c0.554667 0 1.024 0.298667 1.621333 0.341333l0.341334 0.042667a41.941333 41.941333 0 0 1 29.781333 14.464l332.885333 204.202667c1.237333 0.768 1.962667 2.005333 3.114667 2.901333 0.682667 0.512 1.237333 1.194667 1.877333 1.792a40.661333 40.661333 0 0 1 8.917334 10.752l0.469333 0.554667c0.213333 0.469333 0.213333 0.981333 0.426667 1.450666 2.090667 4.224 3.541333 8.576 4.224 13.312 0.170667 1.28 0.170667 2.474667 0.256 3.712a45.653333 45.653333 0 0 1-0.768 11.776c-0.426667 2.048-1.194667 3.925333-1.877334 5.888zM426.666667 376.064v272.64l221.866666-136.533333-221.866666-136.106667z", BLACK);
        svg1.setSize(20.0);
        MenuItem playMenuItem = new MenuItem("播放", svg1);
        playMenuItem.setOnAction(event -> {
            if (tableView.getItems().size() != 0) {
                TableView.TableViewSelectionModel<PlayBean> selectionModel = tableView.getSelectionModel();
                int selectedIndex = selectionModel.getSelectedIndex();
                if (selectedIndex == -1) {
                    return;
                }
                this.currentPlayBean = selectionModel.getSelectedItem();
                this.currentIndex = selectedIndex;
                selectionModel.clearAndSelect(this.currentIndex);
                simplifyTableView.getSelectionModel().clearAndSelect(this.currentIndex);
                this.play();
            }
        });

        SVGGlyph svg2 = new SVGGlyph("M512 62.389956c-248.312412 0-449.610044 201.297632-449.610044 449.610044s201.297632 449.610044 449.610044 449.610044 449.610044-201.297632 449.610044-449.610044S760.312412 62.389956 512 62.389956zM786.507004 786.507004c-35.672454 35.672454-77.196173 63.672158-123.416867 83.222423-47.821145 20.22667-98.655927 30.482245-151.09116 30.482245-52.435233 0-103.270015-10.255575-151.09116-30.482245-46.220694-19.549242-87.744413-47.549969-123.416867-83.222423-35.672454-35.672454-63.672158-77.196173-83.222423-123.416867-20.22667-47.821145-30.482245-98.655927-30.482245-151.090137 0-52.435233 10.255575-103.270015 30.482245-151.09116 19.549242-46.220694 47.549969-87.744413 83.222423-123.416867 35.672454-35.672454 77.196173-63.672158 123.416867-83.222423 47.821145-20.22667 98.654904-30.482245 151.09116-30.482245 52.435233 0 103.268992 10.255575 151.09116 30.482245 46.220694 19.549242 87.744413 47.549969 123.416867 83.222423 35.672454 35.672454 63.672158 77.196173 83.222423 123.416867 20.22667 47.821145 30.482245 98.655927 30.482245 151.09116 0 52.435233-10.255575 103.268992-30.482245 151.090137C850.179163 709.310831 822.179458 750.83455 786.507004 786.507004zM575.653739 507.980453 308.169685 305.667701c-3.094478-1.786693-6.961552 0.446162-6.961552 4.019547l0 404.625504c0 3.572362 3.868097 5.806239 6.961552 4.019547l267.484054-202.312752C578.747193 514.232854 578.747193 509.767146 575.653739 507.980453zM718.151174 306.049395l-92.229564 0c-2.563382 0-4.640694 2.365884-4.640694 5.28333l0 401.334551c0 2.917446 2.078335 5.28333 4.640694 5.28333l92.229564 0c2.563382 0 4.640694-2.365884 4.640694-5.28333L722.791867 311.332724C722.791867 308.415278 720.714556 306.049395 718.151174 306.049395z", BLACK);
        svg2.setSize(20.0);
        MenuItem nextMenuItem = new MenuItem("下一首播放", svg2);

        nextMenuItem.setOnAction(event -> {
            ObservableList<PlayBean> observableList = tableView.getItems();
            if (observableList.size() != 0) {
                TableView.TableViewSelectionModel<PlayBean> selectionModel = tableView.getSelectionModel();
                int selectedIndex = selectionModel.getSelectedIndex();
                if (selectedIndex == -1) {
                    return;
                }
                int nextIndex = (selectedIndex + 1) % (observableList.size());
                this.currentPlayBean = observableList.get(nextIndex);
                this.currentIndex = selectedIndex;
                selectionModel.clearAndSelect(this.currentIndex);
                simplifyTableView.getSelectionModel().clearAndSelect(this.currentIndex);
                this.play();
            }
        });

        SVGGlyph svg3 = new SVGGlyph("M1239.3472 425.7792a62.0544 62.0544 0 0 0-39.5776-22.5792v-126.976c0-50.1248-40.6528-90.7264-90.6752-90.7264h-500.5312a10.0864 10.0864 0 0 1-3.1232-2.5088L585.216 99.84a99.4816 99.4816 0 0 0-36.352-55.3984A102.912 102.912 0 0 0 486.4 22.1696H116.2752C66.2016 22.1696 25.6 62.7712 25.6 112.7936v817.3568c0 20.2752 6.8096 39.936 19.3536 55.808l-0.3072 0.6144 4.096 4.4544c17.4592 19.2 41.2672 29.7984 67.072 29.7984 4.7616 0 327.7312 0.512 607.0272 0.512 140.8 0 270.4384-0.2048 342.784-0.512 39.936-0.1024 75.2128-29.3888 83.9168-69.5296l49.7664-230.0928 52.5824-242.5344a63.0784 63.0784 0 0 0-12.544-52.8896z m-48.7424-14.336h0.512-0.512zM515.072 206.08l0.256 1.1776c5.0176 19.6608 16.6912 37.2224 33.8432 50.5344 17.5616 13.824 37.7856 21.0944 58.624 21.0944h498.7904V402.944H223.0784c-29.696 0-54.9376 20.224-61.2864 49.3568l-42.8544 197.632V115.456h367.616c0.4608 0 2.5088 0.5632 5.0176 2.5088a10.5984 10.5984 0 0 1 2.9696 3.3792l20.48 84.6336z m637.1328 289.9456l-45.9264 213.0944-47.2576 218.112H154.112l93.3376-431.2064h904.704z", BLACK);
        svg3.setSize(20.0);
        MenuItem openFileMenuItem = new MenuItem("打开文件所在目录", svg3);

        openFileMenuItem.setOnAction(event -> {
            LocalMusicUtils.createLocalMusicDir();
            try {
                if (tableView.getItems().size() != 0) {
                    TableView.TableViewSelectionModel<PlayBean> selectionModel = tableView.getSelectionModel();
                    PlayBean playBean = selectionModel.getSelectedItem();
                    if (playBean != null && playBean.isLocalMusic()) {
                        LocalMusicUtils.openFileAndSelect(playBean);
                    } else {
                        service.warn("警告", "本地音乐才有目录", customAudioParameter);
                    }
                }
            } catch (Exception e) {
                Log4jUtils.logger.warn("", e);
            }
        });

        SVGGlyph svg4 = new SVGGlyph("M742.4 0h-358.4C199.68 0 51.2 148.48 51.2 332.8v358.4C51.2 875.52 199.68 1024 384 1024h358.4C926.72 1024 1075.2 875.52 1075.2 691.2v-358.4C1075.2 148.48 926.72 0 742.4 0z m-290.133333 163.84h223.573333c13.653333 0 25.6 11.946667 25.6 25.6s-11.946667 25.6-25.6 25.6H452.266667c-13.653333 0-25.6-11.946667-25.6-25.6 0-15.36 10.24-25.6 25.6-25.6z m428.373333 153.6h-42.666667V781.653333c-3.413333 37.546667-34.133333 68.266667-68.266666 68.266667H356.693333c-34.133333 0-64.853333-30.72-68.266666-68.266667V317.44h-42.666667c-13.653333 0-25.6-11.946667-25.6-25.6s11.946667-25.6 25.6-25.6h633.173333c13.653333 0 25.6 11.946667 25.6 25.6s-10.24 25.6-23.893333 25.6zM339.626667 778.24c1.706667 10.24 10.24 18.773333 17.066666 18.773333H768c6.826667 0 15.36-8.533333 17.066667-18.773333v-460.8H339.626667v460.8zM597.333333 496.64c0-13.653333 11.946667-25.6 25.6-25.6s25.6 11.946667 25.6 25.6v119.466667c0 13.653333-11.946667 25.6-25.6 25.6S597.333333 631.466667 597.333333 617.813333v-121.173333z m-119.466666 0c0-13.653333 11.946667-25.6 25.6-25.6s25.6 11.946667 25.6 25.6v119.466667c0 13.653333-11.946667 25.6-25.6 25.6s-25.6-11.946667-25.6-25.6v-119.466667z", BLACK);
        svg4.setSize(20.0);
        MenuItem deleteMenuItem = new MenuItem("从磁盘中删除", svg4);
        alertStage.setHeaderText("是否彻底删除以下音乐文件?");

        deleteMenuItem.setOnAction(event -> {
            if (tableView.getItems().size() != 0) {
                TableView.TableViewSelectionModel<PlayBean> selectionModel = tableView.getSelectionModel();
                PlayBean playBean = selectionModel.getSelectedItem();
                if (playBean == null) {
                    return;
                }
                if (playBean == currentPlayBean) {
                    service.warn("警告", "这首歌正在播放，不允许删除", customAudioParameter);
                    return;
                }
                if (playBean.isLocalMusic()) {
                    alertStage.setContentText(playBean.getMusicName());
                    ButtonType buttonType = alertStage.showWaitResult();
                    if (buttonType == ButtonType.OK) {
                        boolean b = LocalMusicUtils.deleteMusic(playBean);
                        if (b) {
                            tableView.getItems().remove(playBean);
                            service.success("成功删除", "音乐文件名：" + playBean.getMusicName(), customAudioParameter);
                        } else {
                            service.fail("错误", "删除音乐失败", customAudioParameter);
                        }
                    }
                } else {
                    service.fail("错误操作", "非本地音乐不能删除", customAudioParameter);
                }
            }
        });

        SVGGlyph svg5 = new SVGGlyph("M339.57 697c1 0.86 1.79 1 1.16 0.46a21.38 21.38 0 0 0-2-1.18c0.27 0.27 0.56 0.52 0.84 0.72zM428.33 696.49c-0.5 0.34-1 0.75-1.45 1.13a5.06 5.06 0 0 0 0.81-0.58zM393.21 711.57c3-0.3 1.54-0.23-0.07 0h-0.28zM392.86 711.6h-0.07c-0.43 0.06-0.86 0.14-1.2 0.22 0.41-0.09 0.84-0.16 1.27-0.22zM338.73 696.32l-0.06-0.06-0.56-0.28zM317.65 668.51c0.42 1.14 1.06 1.81 0.82 1.11-0.39-0.8-0.82-1.58-1.27-2.34zM542.41 582.32a3.85 3.85 0 0 0 0.76-1.21 18.8 18.8 0 0 0-1.45 2zM552.76 564.32l-0.35 0.57a2 2 0 0 0-0.08 0.22c0.14-0.27 0.28-0.54 0.43-0.79zM429.15 696l-0.58 0.29-0.24 0.22a5.54 5.54 0 0 1 0.82-0.51zM556.72 526.64v0.94c0 0.29 0.12 0.6 0.17 0.91a15 15 0 0 1-0.17-1.85zM557 528.94l-0.09-0.45c0.05 0.3 0.09 0.59 0.11 0.88s0 0.64 0.08 1a6.14 6.14 0 0 0-0.1-1.43zM541.72 583.12l-0.08 0.09c-0.09 0.18-0.19 0.37-0.28 0.57a7.16 7.16 0 0 1 0.36-0.66zM317.2 667.28v-0.07l-0.36-0.57zM552.33 565.11c-0.42 0.77-0.8 1.59-1.16 2.43a3.83 3.83 0 0 0 0.76-1.35c0.13-0.36 0.26-0.72 0.4-1.08zM312.69 630.75c0.05-0.3 0.11-0.6 0.16-0.89s0-0.58 0-0.9a14 14 0 0 1-0.16 1.79zM375.67 711.83l-0.38-0.08zM597.65 326.45l-1 0.71c-2.73 1.92-0.1 0.16 1-0.71zM374.05 711.57h0.42c-1.73-0.22-3.61-0.32-0.42 0zM706.72 411.9l0.55-1.14a3.69 3.69 0 0 0 0.39-1.19c0 0.16-0.11 0.32-0.17 0.48-0.18 0.5-0.49 1.2-0.77 1.85zM706.72 411.9l-0.13 0.26c0 0.16-0.06 0.31-0.09 0.48a4.3 4.3 0 0 1 0.22-0.74zM512 65C265.13 65 65 265.13 65 512s200.13 447 447 447 447-200.13 447-447S758.87 65 512 65z m91.65 492.62c-3.82 26.66-17.91 49.76-36.69 68.54l-83.15 83.15C472.15 721 461 733 446.92 741.88c-32.36 20.59-74.27 23.52-109.41 8.68S275 704.24 267.1 666.7c-8.17-38.92 2.64-79.4 30-108.46 21.52-22.83 44.42-44.48 66.6-66.66 9.48-9.48 24.35-8.79 33.92 0s8.88 25 0 33.91l-2.53 2.51L356 567.12l-18.34 18.33c-1.41 1.41-2.83 2.82-4.23 4.24-1.16 1.16-2.3 2.33-3.41 3.54s-2.22 2.87-3.47 4.09l-0.45 0.63a92.07 92.07 0 0 0-6.27 10.27c-0.72 1.36-1.37 2.74-2 4.13-0.21 0.54-0.45 1.08-0.65 1.6a96.77 96.77 0 0 0-3.51 11.91c-0.3 1.33-0.55 2.66-0.8 4-0.06 3.35-0.31 3.57-0.36 2.8a97.31 97.31 0 0 0-0.29 10.66c0 1.6 0.13 3.19 0.25 4.79l0.12 1.25c0.88 3.74 1.38 7.56 2.44 11.28 0.51 1.77 1.08 3.53 1.69 5.27 0.15 0.43 0.31 0.87 0.48 1.3a14 14 0 0 1 1.29 2.41c1.27 2.53 2.35 5.22 3.79 7.62 1 1.63 2 3.23 3.05 4.79 0.46 0.68 4.1 6.28 1.93 2.78-2-3.29 0.24 0.21 0.87 0.94s1.28 1.46 1.93 2.18q1.48 1.64 3 3.19c1.39 1.38 2.81 2.72 4.28 4l1.3 1.12a11.4 11.4 0 0 1 2.06 1.24c2.51 1.65 4.92 3.71 7.46 5.17 1.62 0.93 3.27 1.82 4.95 2.65 0.27 0.14 1.27 0.61 2.22 1.06 0.6 0.25 1.32 0.55 1.64 0.67 0.87 0.31 1.74 0.62 2.61 0.91 1.75 0.59 3.52 1.12 5.29 1.61 3.26 0.88 6.6 1.31 9.87 2.06 0.67 0.07 1.34 0.13 2 0.18 1.82 0.13 3.64 0.2 5.47 0.23a97.37 97.37 0 0 0 10.53-0.43l0.35-0.05c0.4-0.06 0.79-0.11 1.16-0.18q2.71-0.48 5.39-1.13a95.25 95.25 0 0 0 10.57-3.2l0.61-0.25c3.89-2 1.62-0.71 0 0l-1.18 0.59c0.62-0.31 1.28-0.56 1.91-0.86q2.55-1.17 5-2.5a94.64 94.64 0 0 0 9.65-5.94c0.2-0.14 0.41-0.31 0.62-0.47-0.91 0.51-1 0 1.69-1.35 1.18-1 2.35-2 3.49-3.1 0.79-0.75 1.56-1.51 2.34-2.28 8.67-8.58 17.25-17.26 25.88-25.88q37.55-37.54 75.08-75.08c1.42-1.42 2.83-2.84 4.18-4.32q1.07-1.18 2.1-2.4c1.13-2.23 1.66-2.56 1.53-2.1 0.53-0.67 1.07-1.33 1.51-2a93.61 93.61 0 0 0 5.38-9.12c0.4-0.78 0.75-1.62 1.11-2.46-0.27 0.24-0.15-0.41 1.24-2.65l0.48-1.31a95 95 0 0 0 3-10.6c0.43-1.92 0.67-3.9 1.07-5.82 0-0.45 0.09-0.91 0.13-1.37a95.38 95.38 0 0 0 0.23-11c-0.05-1.5-0.15-3-0.27-4.49 0 0.77-0.3 0.57-0.36-2.76-0.6-3.16-1.27-6.29-2.16-9.38-0.64-2.21-1.36-4.41-2.15-6.57-0.18-0.49-0.41-1-0.61-1.51C550.3 507 548.75 504 547 501c-1.1-1.83-2.27-3.61-3.49-5.36-0.26-0.37-0.53-0.73-0.8-1.09-2-2.31-4-4.64-6.18-6.82-2-2-4.14-3.88-6.28-5.75a6.14 6.14 0 0 1-0.51-0.51c-0.48-0.31-1-0.6-1.29-0.83a99 99 0 0 0-11.28-6.77c-11.89-6.18-14.43-21.79-8.61-32.81 6.26-11.84 21.69-14.39 32.81-8.61a122 122 0 0 1 26.61 19c29.2 27.22 41.26 67.16 35.67 106.17z m143.22-118.87c-9.37 18.46-24.65 32.38-39.06 46.8l-32.92 32.91c-9.47 9.48-24.34 8.79-33.91 0s-8.88-25 0-33.91l2.66-2.66 37.82-37.82c3.36-3.37 6.75-6.72 10.1-10.1 1.38-1.4 2.73-2.82 4-4.29 0.47-0.53 2-2.44 2.46-3 0.58-0.81 1.15-1.6 1.3-1.83q1.39-2.06 2.69-4.18c1.68-2.77 3.13-5.63 4.55-8.53 0.53-2.82 1.14-3.28 1.07-2.59 0.49-1.36 1-2.73 1.39-4.11a93.74 93.74 0 0 0 2.65-10.69c0.07-0.35 0.13-0.69 0.19-1 0.13-1.38 0.32-2.77 0.41-4.15a95.23 95.23 0 0 0 0.08-10.95c-0.08-1.59-0.2-3.18-0.36-4.76-0.65-3.54-1.38-7-2.38-10.51q-0.76-2.66-1.68-5.27c-0.16-0.45-0.62-1.63-1-2.53-0.56-1.21-1.31-2.81-1.53-3.26-0.74-1.46-1.53-2.9-2.35-4.33-1.66-2.89-3.53-5.64-5.44-8.37l-1.12-1.35q-1.9-2.22-3.95-4.32a95 95 0 0 0-7-6.5l-0.24-0.2c-3.32-2.26-1.21-1 0 0l1.15 0.79c-0.59-0.4-1.15-0.86-1.73-1.28q-2.31-1.68-4.72-3.21a92.24 92.24 0 0 0-9.21-5.16l-1-0.5A49.63 49.63 0 0 1 665 316a90.23 90.23 0 0 0-9.3-2.6c-1.73-0.39-3.48-0.68-5.23-1l-0.59-0.07a95 95 0 0 0-10.26-0.4q-3.09 0-6.16 0.28l-1.48 0.15c-1 0.15-2.53 0.39-3 0.48-1.8 0.35-3.59 0.76-5.37 1.21a110 110 0 0 0-10.48 3.34l-0.09 0.05q-2.22 1-4.4 2.16a94.3 94.3 0 0 0-9.64 5.9c-0.34 0.23-0.66 0.46-1 0.7-0.58 1.08-2.86 2.32-3.78 3.15-1.59 1.42-3.11 2.92-4.63 4.43q-13.1 13.05-26.15 26.15l-72.61 72.61-2.43 2.46c-0.77 0.78-1.54 1.55-2.28 2.35-1.34 1.42-2.6 2.89-3.87 4.36l-0.2 0.23a95.52 95.52 0 0 0-6.09 9.57 90.25 90.25 0 0 0-2.28 4.36c-0.17 0.34-0.33 0.69-0.49 1-0.54 3.28-2.57 6.89-3.44 10.1s-1.41 6.25-2 9.4c0 0.37-0.07 0.75-0.1 1.12a90.66 90.66 0 0 0-0.31 5.47 95.11 95.11 0 0 0 0.25 10.27c0 0.38 0.07 0.76 0.11 1.14 0.37 1.51 0.48 3.2 0.79 4.7a93.43 93.43 0 0 0 2.46 9.33c0.56 1.76 1.23 3.47 1.85 5.2 0.1 0.24 0.2 0.48 0.31 0.7a94.37 94.37 0 0 0 5 9.28c1.12 1.81 2.29 3.59 3.53 5.33l0.33 0.45c2.05 2.33 4 4.68 6.24 6.88 9.48 9.48 8.79 24.34 0 33.92s-25 8.88-33.92 0a126.55 126.55 0 0 1-16.47-20.58C419 520.48 414 481.9 425.07 449c7.09-21.1 19.61-38.11 35.15-53.65l79.83-79.84c9.93-9.92 19.46-19.92 30.68-28.44 28-21.28 65.87-27.8 99.77-19.38 33.68 8.37 64.31 33 78.47 64.85 15.28 34.35 15.03 72.46-2.1 106.21zM328.07 595.22c0.16-0.23 0.2-0.27 0 0zM312.69 630.75c0 0.17-0.07 0.34-0.1 0.51a6.17 6.17 0 0 0-0.1 1.4c0-0.32 0.05-0.65 0.08-1s0.07-0.59 0.12-0.91z", BLACK);
        svg5.setSize(20.0);
        MenuItem copyLinkMenuItem = new MenuItem("复制链接", svg5);

        copyLinkMenuItem.setOnAction(event -> {
            ObservableList<PlayBean> observableList = tableView.getItems();
            if (observableList.size() != 0) {
                PlayBean playBean = tableView.getSelectionModel().getSelectedItem();
                if (playBean != null) {
                    clipboardContent.clear();
                    clipboardContent.putString(playBean.getMp3Url());
                    clipboard.setContent(clipboardContent);
                }
            }

        });

        SVGGlyph svg6 = new SVGGlyph("M511.68 0C271.68 0 90.56 87.04 90.56 202.24v618.88C90.56 936.32 271.68 1024 511.68 1024s421.12-87.04 421.12-202.24V202.24C933.44 87.04 752.32 0 511.68 0z m0 64c210.56 0 357.12 72.96 357.12 138.24S722.24 340.48 511.68 340.48 154.56 267.52 154.56 202.24 301.12 64 511.68 64z m0 896C301.12 960 154.56 886.4 154.56 821.12v-96a609.92 609.92 0 0 0 357.12 92.16 739.84 739.84 0 0 0 288-52.48 32 32 0 1 0-25.6-58.88 675.2 675.2 0 0 1-262.4 47.36C301.12 753.28 154.56 680.32 154.56 615.04V518.4a609.92 609.92 0 0 0 357.12 92.8 741.76 741.76 0 0 0 286.08-51.84 32 32 0 0 0-25.6-58.88 677.76 677.76 0 0 1-261.12 46.72C301.12 547.2 154.56 474.24 154.56 408.96V312.32a609.92 609.92 0 0 0 357.12 92.16 609.92 609.92 0 0 0 357.12-92.16v512c0.64 62.08-146.56 135.68-357.12 135.68z", BLACK);

        svg6.setSize(20.0);
        MenuItem copyObjectMenuItem = new MenuItem("复制歌曲信息", svg6);

        copyObjectMenuItem.setOnAction(event -> {
            ObservableList<PlayBean> observableList = tableView.getItems();
            if (observableList.size() != 0) {
                PlayBean playBean = tableView.getSelectionModel().getSelectedItem();
                if (playBean != null) {
                    clipboardContent.clear();
                    clipboardContent.putString(playBean.getMusicInf());
                    clipboard.setContent(clipboardContent);
                }
            }

        });

        SVGGlyph lrcSvg = new SVGGlyph("M872.727273 1024H151.272727C67.490909 1024 0 956.509091 0 872.727273V151.272727C0 67.490909 67.490909 0 151.272727 0h721.454546c83.781818 0 151.272727 67.490909 151.272727 151.272727v721.454546c0 83.781818-67.490909 151.272727-151.272727 151.272727zM151.272727 69.818182c-45.381818 0-81.454545 36.072727-81.454545 81.454545v721.454546c0 45.381818 36.072727 81.454545 81.454545 81.454545h721.454546c45.381818 0 81.454545-36.072727 81.454545-81.454545V151.272727c0-45.381818-36.072727-81.454545-81.454545-81.454545H151.272727zM576 314.181818h267.636364c22.109091 0 40.727273 18.618182 40.727272 40.727273S865.745455 395.636364 843.636364 395.636364h-267.636364c-22.109091 0-40.727273-18.618182-40.727273-40.727273s18.618182-40.727273 40.727273-40.727273zM576 488.727273h267.636364c22.109091 0 40.727273 18.618182 40.727272 40.727272S865.745455 570.181818 843.636364 570.181818h-267.636364c-22.109091 0-40.727273-18.618182-40.727273-40.727273s18.618182-40.727273 40.727273-40.727272zM576 663.272727h267.636364c22.109091 0 40.727273 18.618182 40.727272 40.727273S865.745455 744.727273 843.636364 744.727273h-267.636364c-22.109091 0-40.727273-18.618182-40.727273-40.727273s18.618182-40.727273 40.727273-40.727273zM610.909091 128H546.909091c-83.781818 0-151.272727 67.490909-151.272727 151.272727v308.363637c-26.763636-18.618182-58.181818-29.090909-93.090909-29.090909-89.6 0-162.909091 73.309091-162.909091 162.90909s73.309091 162.909091 162.909091 162.909091 162.909091-73.309091 162.90909-162.909091V279.272727c0-45.381818 36.072727-81.454545 81.454546-81.454545h64c19.781818 0 34.909091-15.127273 34.909091-34.909091s-15.127273-34.909091-34.909091-34.909091zM302.545455 814.545455c-51.2 0-93.090909-41.890909-93.09091-93.09091s41.890909-93.090909 93.09091-93.090909 93.090909 41.890909 93.090909 93.090909-41.890909 93.090909-93.090909 93.09091z", BLACK);
        lrcSvg.setSize(20.0);

        MenuItem copyLrcMenuItem = new MenuItem("复制歌词", lrcSvg);
        copyLrcMenuItem.setOnAction(event -> {
            if (currentPlayBean != null) {
                String lrcString = currentPlayBean.getLrc();
                if (currentPlayBean.isLocalMusic()) {
                    lrcString = LocalMusicUtils.getLrc(currentPlayBean.getLocalLrcPath());
                }
                if (lrcString != null) {
                    clipboardContent.clear();
                    clipboardContent.putString(lrcString);
                    clipboard.setContent(clipboardContent);
                    service.success("成功", "已成功复制歌词", customAudioParameter);
                }
            }
        });

        SVGGlyph svg7 = new SVGGlyph("M511.396712 0C228.960418 0 0 228.960418 0 511.396712s228.960418 511.400325 511.396712 511.400325c282.436294 0 511.3931-228.96403 511.3931-511.400325S793.833006 0 511.396712 0zM312.344204 299.378932c46.019671 0 83.322374 37.393015 83.322374 83.521061 0 46.128046-37.302702 83.524674-83.322374 83.524674-46.016059 0-83.322374-37.396627-83.322374-83.524674C229.018218 336.768334 266.328145 299.378932 312.344204 299.378932zM850.49511 723.41088 172.309151 723.41088l-0.003613-84.727637 113.428971-83.437974 58.479193 53.587864 171.030325-195.999944 54.93894 58.916306 112.543907-146.259395 167.764623 258.171114L850.491498 723.41088z", BLACK);
        svg7.setSize(20.0);
        MenuItem copyImageMenuItem = new MenuItem("复制当前歌曲封面", svg7);

        copyImageMenuItem.setOnAction(event -> {
            if (currentPlayBean != null) {
                clipboard.clear();
                clipboardContent.clear();
                clipboardContent.putImage(panImageView.getImage());
                clipboard.setContent(clipboardContent);
            }
        });

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(playMenuItem, nextMenuItem, copyLinkMenuItem, openFileMenuItem, deleteMenuItem, copyObjectMenuItem, copyLrcMenuItem, copyImageMenuItem);

        tableView.setContextMenu(contextMenu);
        simplifyTableView.setPrefHeight(260.0);
        TableColumn siCol = new TableColumn("");
        siCol.setCellValueFactory(new PropertyValueFactory<>("musicName"));
        simplifyTableView.getColumns().addAll(siCol);
        siCol.prefWidthProperty().bind(simplifyTableView.widthProperty());
        simplifyTableView.getStyleClass().add("simplifyTableView");
        simplifyTableView.setItems(tableObList);

        simplifyTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                PlayBean playBean = simplifyTableView.getSelectionModel().getSelectedItem();
                if (playBean != null) {
                    //1.获取选中行的索引
                    this.currentIndex = simplifyTableView.getSelectionModel().getSelectedIndex();
                    //2.将前一秒置为：0
                    this.prevSecond = 0;
                    //3.判断当前是否正在播放，如果是：将其停止
                    if (this.currentPlayBean != null) {
                        if (this.mediaPlayer != null) {
                            this.mediaPlayer.stop();
                        }
                    }
                    //4.获取当前的PlayBean
                    this.currentPlayBean = playBean;
                    this.tableView.getSelectionModel().clearAndSelect(this.currentIndex);
                    this.tableView.scrollTo(currentIndex);
                    //5.播放
                    play();
                }
            }
            event.consume();
        });

        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                PlayBean playBean = tableView.getSelectionModel().getSelectedItem();
                if (playBean != null) {
                    //1.获取选中行的索引
                    this.currentIndex = tableView.getSelectionModel().getSelectedIndex();
                    //2.将前一秒置为：0
                    this.prevSecond = 0;
                    //3.判断当前是否正在播放，如果是：将其停止
                    if (this.currentPlayBean != null) {
                        if (this.mediaPlayer != null) {
                            this.mediaPlayer.stop();
                        }
                    }
                    //4.获取当前的PlayBean
                    this.currentPlayBean = playBean;
                    this.simplifyTableView.getSelectionModel().clearAndSelect(this.currentIndex);
                    this.simplifyTableView.scrollTo(currentIndex);
                    //5.播放
                    play();
                }
            }
            event.consume();
        });

        BorderPane songListBorderPane = new BorderPane();
        songListBorderPane.setTop(anchorPane);
        songListBorderPane.setCenter(tableView);

        maskerPane = new MaskerPane();
        flowPane = new FlowPane();
        flowPane.setHgap(8.0);
        flowPane.setVgap(8.0);
        flowPane.setBorder(border);

        VBox vBox = new VBox(5);
        int playListBeanListSize = 18;
        playListBeanList = new ArrayList<>(playListBeanListSize);

        for (int k = 0; k < playListBeanListSize; k++) {
            PlayListBean playListBean = new PlayListBean();
            playListBeanList.add(playListBean);
        }
        Insets insets1 = new Insets(3, 3, 3, 3);
        for (PlayListBean listBean : playListBeanList) {
            VBox vbox = new VBox(8);
            PlayListBean playListBean = listBean;
            ImageView musicIcoImageView = new ImageView();
            if (playListBean.getImageUrl() != null) {
                musicIcoImageView.setImage(new Image(playListBean.getImageUrl()));
            }
            musicIcoImageView.setFitWidth(MUSICICOIMAGEVIEWSIZE);
            musicIcoImageView.setPreserveRatio(true);
            //歌单名称：Label
            Label labGroupName1 = new Label(playListBean.getAlbum());
            labGroupName1.setPrefHeight(30);
            labGroupName1.setPrefWidth(MUSICICOIMAGEVIEWSIZE);
            labGroupName1.setWrapText(true);
            labGroupName1.setTextFill(BLACK);
            labGroupName1.setFont(new Font(10));
            vbox.getChildren().addAll(musicIcoImageView, labGroupName1);
            vbox.setMargin(musicIcoImageView, insets1);
            vbox.setOnMouseClicked(event -> searchSongList(playListBean));
            /*vBox.setOnMouseEntered(event -> {
                JFXDepthManager.setDepth(vBox,4);
            });
            vBox.setOnMouseExited(event -> {
                JFXDepthManager.setDepth(vBox,0);
            });*/
            flowPane.getChildren().addAll(vbox);
        }

        Label label = new Label("推荐歌单 >");
        label.setFont(Font.font("Timer New Roman", FontWeight.BOLD, FontPosture.ITALIC, 16));
        label.setTextFill(BLACK);
        label.setPadding(new Insets(5, 5, 5, 5));

        SVGGlyph refreshSVGGlyph = new SVGGlyph("M91.738353 320.632471a449.716706 449.716706 0 0 0 186.368 579.824941l-47.887059 103.424 272.022588-71.318588-121.675294-253.590589-49.995294 108.182589A325.270588 325.270588 0 0 1 185.886118 593.317647a324.547765 324.547765 0 0 1 18.853647-220.581647 325.270588 325.270588 0 0 1 251.783529-186.608941l-56.199529-116.073412a449.295059 449.295059 0 0 0-308.645647 250.578824z m577.355294-90.714353a325.330824 325.330824 0 0 1 146.733177 195.041882 324.668235 324.668235 0 0 1-18.853648 220.581647 325.270588 325.270588 0 0 1-257.686588 187.331765l56.018824 116.675764a449.897412 449.897412 0 0 0 314.729412-251.964235 449.054118 449.054118 0 0 0 25.961411-304.790588l-0.060235-0.361412a449.656471 449.656471 0 0 0-214.799059-276.058353l45.236706-98.665412-271.721412 72.282353 122.458353 253.108706 51.983059-113.242353z", Paint.valueOf("#8a8a8a"));
        refreshSVGGlyph.setSize(18.0);
        JFXButton refreshButton = new JFXButton("", refreshSVGGlyph);
        refreshButton.setPadding(new Insets(8, 5, 5, 5));
        refreshButton.setOnAction(event -> changePlaylist());

        refreshButton.setTooltip(new Tooltip("更换发现音乐栏的歌单"));

        HBox h1 = new HBox(5);
        h1.getChildren().addAll(label, refreshButton);

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(flowPane, maskerPane);
        vBox.getChildren().addAll(h1, stackPane);
        flowPane.prefHeightProperty().bind(vBox.heightProperty());

        Tab findMusicTab = new Tab("发现音乐", vBox);
        Tab songListTab = new Tab("歌单", songListBorderPane);

        tabPane = new JFXTabPane();
        tabPane.getTabs().addAll(findMusicTab, songListTab);
        /*tabPane.setTabMinHeight(-10);//隐藏TabBar
        tabPane.setTabMaxHeight(-10);*/
        //tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tableView.prefHeightProperty().bind(songListBorderPane.prefHeightProperty());
        return tabPane;
    }


    public <T> T requireNonNullElse(T obj, T defaultObj) {
        return (obj != null) ? obj : defaultObj;
    }

    private void searchSongList(PlayListBean playListBean) {
        if ((playListBean.getImageUrl() == null) || (playListBean.getPlayListId() == null)) {
            return;
        }
        this.labGroupName.setText(playListBean.getAlbum());
        if (searchTiplabel.getText().equals("searching....")) {
            service.fail("操作失败！", "正在搜索，期间不允许其他操作！", customAudioParameter);
            return;
        }
        ObservableList<PlayBean> list = tableView.getItems();
        list.clear();//清空表格
        searchTiplabel.setText("searching....");
        new Thread(() -> {
            //搜索歌曲
            cloudMusicSpider.getSongList(playListBean, list);
            //用GUI线程更新UI组件
            Platform.runLater(() -> {
                //songListCoverImageView.setImage(new Image(playListBean.getImageUrl(), SONGLISTCOVERIMAGEVIEWSIZE, SONGLISTCOVERIMAGEVIEWSIZE, false, false));
                songListCoverImageView.setImage(new Image(playListBean.getImageUrl(), true));
                tagsLabel.setText("标签：" + playListBean.getTags());
                descLabel.setText("介绍：" + playListBean.getDescription());
                tabPane.getSelectionModel().select(1);
                leftListView.getSelectionModel().clearAndSelect(1);
                searchTiplabel.setText("");
                //Windows任务栏图标闪烁效果}
                if (!mainStage.isFocused()) {
                    mainStage.requestFocus();
                }
            });
            if (list.size() != 0) {
                currentIndex = 0;
            }
            //System.gc();
        }).start();
    }


    public void searchSingleMusic(String text) {
        ObservableList<PlayBean> list = tableView.getItems();
        list.clear();//清空表格
        searchTiplabel.setText("searching....");
        new Thread(() -> {
            //搜索歌曲
            cloudRequest.searchMusic(text, list);
            Platform.runLater(() -> {
                tagsLabel.setText("标签：音乐");
                String trim = text.trim();
                descLabel.setText("介绍：" + trim);
                searchTiplabel.setText("");
                labGroupName.setText(trim);
                /*Windows任务栏图标闪烁效果}*/
                if (!mainStage.isFocused()) {
                    mainStage.requestFocus();
                }
                tabPane.getSelectionModel().select(1);
                leftListView.getSelectionModel().clearAndSelect(1);
                if (list.size() != 0) {
                    songListCoverImageView.setImage(panDefaultImage);
                    //songListCoverImageView.setImage(new Image(list.get(0).getImageUrl(), SONGLISTCOVERIMAGEVIEWSIZE, SONGLISTCOVERIMAGEVIEWSIZE, false, false));
                    currentIndex = 0;
                }
            });
        }).start();
    }

    private void searchLocalMusic() {
        this.labGroupName.setText("本地音乐");
        if (searchTiplabel.getText().equals("searching....")) {
            service.fail("操作失败！", "正在搜索，期间不允许其他操作！", customAudioParameter);
            return;
        }
        ObservableList<PlayBean> list = tableView.getItems();
        list.clear();
        searchTiplabel.setText("searching....");
        new Thread(() -> {
            LocalMusicUtils.getLocalMusicInf(list);
            //用GUI线程更新UI组件
            Platform.runLater(() -> {
                songListCoverImageView.setImage(panDefaultImage);
                tagsLabel.setText("标签：本地音乐");
                descLabel.setText("介绍：音乐，是人生最大的快乐；音乐，是生活中的一股清泉；音乐，是陶冶性情的熔炉。");
                /*Windows任务栏图标闪烁效果}*/
                if (!mainStage.isFocused()) {
                    mainStage.requestFocus();
                }
                tableView.refresh();
                tabPane.getSelectionModel().select(1);
                leftListView.getSelectionModel().clearAndSelect(3);
                searchTiplabel.setText("");
            });
            if (list.size() != 0) {
                currentIndex = 0;
            }
        }).start();
    }

    /**
     * 加载发现音乐界面
     */
    private void changePlaylist() {
        searchTiplabel.setText("searching....");
        maskerPane.setVisible(true);
        new Thread(() -> {
            //随机获取歌单
            try {
                cloudMusicSpider.getPlayList(playListBeanList);
            } catch (Exception e) {
                service.fail("随机获取时歌单发生错误", e.toString(), customAudioParameter);
                Log4jUtils.logger.error("", e);
            }
            //用GUI线程更新UI组件
            Platform.runLater(() -> {
                maskerPane.setVisible(false);
                for (int i = 0; i < playListBeanList.size(); i++) {
                    PlayListBean playListBean = playListBeanList.get(i);
                    VBox vBox = (VBox) flowPane.getChildren().get(i);
                    ImageView img = (ImageView) vBox.getChildren().get(0);
                    Label label = (Label) vBox.getChildren().get(1);
                    label.setText(playListBean.getAlbum());
                    if (playListBean.getImageUrl() != null) {
                        img.setImage(new Image(playListBean.getImageUrl(), PANIMAGVIEWSIZE, PANIMAGVIEWSIZE,
                                false, false, true));
                    }
                }
                tabPane.getSelectionModel().select(0);
                leftListView.getSelectionModel().clearAndSelect(0);
                searchTiplabel.setText("");
            });
        }).start();
    }

    /**
     * 播放音乐的方法
     */
    private void play() {
        if (currentPlayBean == null) {
            Log4jUtils.logger.error("currentPlayBean 为空");
            return;
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            try {
                /*移除所有注册的监听器，避免内存泄漏*/
                mediaPlayer.currentTimeProperty().removeListener(changeListener);
                mediaPlayer.setOnEndOfMedia(null);
            } catch (Exception e) {
                Log4jUtils.logger.error("", e);
            }
            mediaPlayer.dispose();/*释放资源*/
            mediaPlayer = null;
        }
        labPlayTime.setText("00:00");
        labTotalTime.setText("00:00");
        songNameLabel.setText(currentPlayBean.getMusicName());
        singerLabel.setText(currentPlayBean.getArtistName());

        sNLab.setText(currentPlayBean.getMusicName());
        siLab.setText("歌手：" + currentPlayBean.getArtistName());
        albumLabel.setText("专辑：" + currentPlayBean.getArtistName());

        simplifyModelStage.setSongNameAndSingerName(
                currentPlayBean.getMusicName(),
                currentPlayBean.getArtistName()
        );
        String mp3Url = currentPlayBean.getMp3Url();
        boolean playable = currentPlayBean.isPlayable();
        if (!currentPlayBean.isLocalMusic() && playable) {
            mp3Url = cloudRequest.getReal(mp3Url);
            currentPlayBean.setMp3Url(mp3Url);
        }
        /*碟片旋转停止*/
        rotateTransition.stop();

        if (!playable || mp3Url.equals("https://music.163.com/404")) {
            currentPlayBean.setPlayable(false);
            labTotalTime.setText("00:00");
            sliderSong.setValue(0.0);
            service.fail("错误", "无法播放此音乐，可能是付费音乐!", customAudioParameter);
            return;
        }
        mediaPlayer = new MediaPlayer(new Media(mp3Url));
        new Thread(() -> mediaPlayer.play()).start();
        loadLrc();
        mediaPlayer.currentTimeProperty().addListener(changeListener);
        if (currentPlayBean.isLocalMusic()) {
            try {
                File file = new File(new URI(mp3Url));
                WritableImage writableImage = LocalMusicUtils.getLocalMusicArtwork(file);
                songCoverImageView.setImage(requireNonNullElse(writableImage, panDefaultImage));
                panImageView.setImage(songCoverImageView.getImage());
            } catch (Exception e) {
                Log4jUtils.logger.error("", e);
            }
        } else {
            /*backgroundLoading开启异步加载Image*/
            try {
                songCoverImageView.setImage(new Image(currentPlayBean.getImageUrl(), PANIMAGVIEWSIZE, PANIMAGVIEWSIZE, false, false, true));
            } catch (Exception e) {
                songCoverImageView.setImage(panDefaultImage);
            }
            panImageView.setImage(songCoverImageView.getImage());
        }
        if (songCoverImageView.getImage() != null) {
            simplifyModelStage.setImage(songCoverImageView.getImage());
        }
        /*资源全部载入播放器后，这时候可以获取到总时间*/
        mediaPlayer.setOnReady(() -> {
            double total_second = Math.floor(mediaPlayer.getTotalDuration().toSeconds());
            date.setTime((long) (total_second * 1000));
            labTotalTime.setText(simpleDateFormat.format(date));
            if (total_second != 0.0) {
                sliderSong.setMax(total_second);
            }
        });

        /*在音频文件还没完整地读取之前，这时候无法获取总时间，这时候就先给个100，稍后在资源全部加载完后修改*/
        sliderSong.setMax(100);
        sliderSong.setMajorTickUnit(1);/*每次前进1格*/
        sliderSong.setValue(0);
        prevSecond = 0;
        playSvg.changeSvgPath(PlaySvg.PlayStatus.PAUSE);
        lrcStage.changeSvgPath(PlaySvg.PlayStatus.PAUSE);
        simplifyModelStage.changeSvgPath(PlaySvg.PlayStatus.PAUSE);
        mediaPlayer.setVolume(sldVolume.getValue() / 100.0);
        mediaPlayer.setOnEndOfMedia(valueRunnable);
        /*碟片上的磁头图片旋转35度，过程添加动画，伪造磁头滑下读取碟片效果，200毫秒的过渡动画*/
        AnimationUtil.rotate(rodImageView, 200, 0.0, 35.0, 1);
        /*如果主界面和抽屉是打开的状态，也就是歌单详情页是展示状态就让碟片旋转动画启动，
        否则不启动动画以减少内存消耗*/
        if (mainStage.isShowing() && jfxDrawer.isOpened()) {
            if (rotateTransition.getStatus() != Animation.Status.RUNNING) {
                rotateTransition.play();
            }
        }
    }

    /**
     * 加载正在播放的歌曲的lrc文件(歌词文件)
     */
    private void loadLrc() {
        if (currentPlayBean.getMusicName() == null || currentPlayBean.getMusicName().equals("")) {
            return;
        }
        /*初始化lrcvbox*/
        this.lrcVBox.getChildren().clear();
        this.lrcVBox.setLayoutY(60);
        this.lrcList.clear();
        this.currentLrcIndex = 0;
        String musicId = currentPlayBean.getMusicId();
        String[] musicLrcList;
        String lrcString;
        /*如果当前的音乐对象没有存储有歌词就去获取，如果有直接取出来*/
        if (currentPlayBean.getLrc() == null) {
            /*如果是不是本地音乐就去网络抓取歌词，如果是本地音乐，
            则读取本地音乐对应的lrc文件获取歌词*/
            if (!currentPlayBean.isLocalMusic()) {
                lrcString = cloudRequest.spiderLrc(musicId);
                /*非本地音乐的歌曲保存lrc，本地音乐的歌曲实时读取lrc文件，将不保存lrcString*/
                currentPlayBean.setLrc(lrcString);
            } else {
                String localLrlPath = currentPlayBean.getLocalLrcPath();
                lrcString = LocalMusicUtils.getLrc(localLrlPath);
            }
        } else {
            lrcString = currentPlayBean.getLrc();
        }
        musicLrcList = lrcString.split("\n");
        for (String row : musicLrcList) {
            row = row.trim();
            if (!row.contains("[") || !row.contains("]")) {
                continue;
            }
            if (row.charAt(1) < '0' || row.charAt(1) > '9') {
                continue;
            }
            String strTime = row.substring(1, row.indexOf("]"));//00:03.29
            String strMinute = strTime.substring(0, strTime.indexOf(":"));//取出：分钟
            String strSecond = strTime.substring(strTime.indexOf(":") + 1);//取出：秒和毫秒
            /*转换为int分钟*/
            BigDecimal totalMilli = null;
            try {
                int intMinute = Integer.parseInt(strMinute);
                /*换算为总的毫秒*/
                totalMilli = new BigDecimal(intMinute * 60).add(new BigDecimal(strSecond)).multiply(new BigDecimal("1000"));
            } catch (NumberFormatException e) {
                Log4jUtils.logger.error("", e);
                totalMilli = new BigDecimal(0);
            }
            this.lrcList.add(totalMilli);
            Label lab = new Label(row.substring(row.indexOf("]") + 1).trim());

            lab.setPrefWidth(380);
            lab.setPrefHeight(30);
            lab.setTextFill(Color.WHITE);
            lab.setAlignment(Pos.CENTER);

            /*判断是否是第一个歌词，如果是加粗(大号字体),如果不是就用默认字体(不加粗，小号字体)*/
            if (this.lrcVBox.getChildren().size() == 0) {
                lab.setFont(boldFont);
                lab.getStyleClass().add("shadowLabel");
                lrcStageLabel.setText(lab.getText());
            } else {
                lab.setFont(font);
            }
            /*将歌词Label添加到lrcVBox中*/
            this.lrcVBox.getChildren().add(lab);
        }
    }

    /**
     * 切歌，上一首歌曲
     */
    public void preMusic() {
        if (this.tableView.getItems().size() != 0) {
            if (this.currentPlayBean != null && mediaPlayer != null) {
                this.mediaPlayer.stop();
            }
            //让当前的索引-1
            this.currentIndex--;
            if (currentIndex < 0) {
                if (this.playMode == 1) {//列表循环
                    this.currentIndex = this.tableView.getItems().size() - 1;//定位到最后一首歌
                } else {
                    this.currentIndex = 0;
                }
            }
            //设置Table的选中
            this.tableView.getSelectionModel().clearAndSelect(currentIndex);
            this.tableView.scrollTo(currentIndex);
            this.simplifyTableView.getSelectionModel().clearAndSelect(currentIndex);
            this.simplifyTableView.scrollTo(currentIndex);
            //设置播放PlayBean对象
            this.currentPlayBean = this.tableView.getItems().get(currentIndex);
            //开始播放
            play();
        }
    }

    /**
     * 播放暂停音乐
     */
    public void playOrPauseMusic() {
        if (this.mediaPlayer != null) {
            //判断如果当前正在播放，暂停
            if (this.mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                //设置播放器暂停
                this.mediaPlayer.pause();
                //设置播放按钮图标为：播放
                playSvg.changeSvgPath(PlaySvg.PlayStatus.PLAY);
                lrcStage.changeSvgPath(PlaySvg.PlayStatus.PLAY);
                AnimationUtil.rotate(rodImageView, 200, 35.0, 0.0, 1);
                if (rotateTransition.getStatus() == Animation.Status.RUNNING) {
                    rotateTransition.pause();
                }
                simplifyModelStage.changeSvgPath(PlaySvg.PlayStatus.PLAY);
            } else if (this.mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                this.mediaPlayer.play();
                playSvg.changeSvgPath(PlaySvg.PlayStatus.PAUSE);
                lrcStage.changeSvgPath(PlaySvg.PlayStatus.PAUSE);
                AnimationUtil.rotate(rodImageView, 200, 0.0, 35.0, 1);
                if (rotateTransition.getStatus() != Animation.Status.RUNNING) {
                    rotateTransition.play();
                }
                simplifyModelStage.changeSvgPath(PlaySvg.PlayStatus.PAUSE);
            }
        }
    }

    /**
     * 播放下一首音乐
     */
    public void nextMusic() {
        if (this.tableView.getItems().size() != 0) {
            if (this.currentPlayBean != null && mediaPlayer != null) {
                this.mediaPlayer.stop();
            }
            //让当前的索引+1
            this.currentIndex++;
            if (currentIndex >= this.tableView.getItems().size()) {
                if (this.playMode == 1) {//列表循环
                    this.currentIndex = 0;//定位到第一首歌
                } else {
                    this.currentIndex = this.tableView.getItems().size() - 1;
                }
            }
            //设置Table的选中
            //this.tableView.getSelectionModel().select(currentIndex);
            this.tableView.getSelectionModel().clearAndSelect(currentIndex);
            this.tableView.scrollTo(currentIndex);
            this.simplifyTableView.getSelectionModel().clearAndSelect(currentIndex);
            this.simplifyTableView.scrollTo(currentIndex);
            //设置播放PlayBean对象
            this.currentPlayBean = this.tableView.getItems().get(currentIndex);
            //开始播放
            play();
        }
    }


    //获取下侧面板
    private BorderPane getBottomPane() {

        StackPane stackPane = new StackPane();
        songCoverImageView = new ImageView(panDefaultImage);
        songCoverImageView.setFitHeight(50);
        songCoverImageView.setFitWidth(50);

        SVGPath svgPath = new SVGPath();
        svgPath.setContent("M-45.3,472l5.2-5.2c0.1-0.1,0.2-0.1,0.3,0l1,1c0.1,0.1,0.1,0.2,0,0.3l-5.2,5.2h3.8c0.2,0,0.4,0.2,0.4,0.4v1.2c0,0.1-0.1,0.2-0.2,0.2h-6.3c-0.4,0-0.8-0.4-0.8-0.8V468c0-0.1,0.1-0.2,0.2-0.2h1.2c0.2,0,0.4,0.2,0.4,0.4V472z M-28.7,458l-5.2,5.2c-0.1,0.1-0.2,0.1-0.3,0c0,0,0,0,0,0l-1-1c-0.1-0.1-0.1-0.2,0-0.3c0,0,0,0,0,0l5.2-5.2h-3.8c-0.2,0-0.4-0.2-0.4-0.4v-1.2c0-0.1,0.1-0.2,0.2-0.2h6.3c0.4,0,0.8,0.4,0.8,0.8v6.3c0,0.1-0.1,0.2-0.2,0.2h-1.2c-0.2,0-0.4-0.2-0.4-0.4C-28.7,461.8-28.7,458-28.7,458z");
        svgPath.setFill(Paint.valueOf("#eaeaea"));
        svgPath.setScaleX(2.0);
        svgPath.setScaleY(2.0);

        JFXButton button = new JFXButton("", svgPath);
        button.setOnAction(event -> {
            if (jfxDrawer.getDefaultDrawerSize() < mainStage.getHeight()) {
                jfxDrawer.setDefaultDrawerSize(mainStage.getHeight());
            }
            if (jfxDrawer.isClosed()) {
                jfxDrawer.open();
            } else {
                jfxDrawer.close();
            }
        });
        button.setRipplerFill(Color.WHEAT);
        button.setOpacity(0.0);
        stackPane.setOnMouseEntered(event -> AnimationUtil.fade(button, 0.1, 0, 0, 1));
        stackPane.setOnMouseExited(event -> AnimationUtil.fade(button, 0.1, 0, 1, 0));
        button.setPrefSize(50, 50);
        button.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3)");
        stackPane.getChildren().addAll(songCoverImageView, button);

        VBox leftbottomvBox = new VBox(5);
        singerLabel = new Label("");
        singerLabel.setTextFill(Color.WHITE);
        Font font1 = Font.font("Timer New Roman", FontWeight.BOLD, FontPosture.ITALIC, 12);
        singerLabel.setFont(font1);
        singerLabel.setPrefWidth(80);
        songNameLabel = new Label("");
        songNameLabel.setTextFill(Color.WHITE);
        songNameLabel.setFont(font1);
        songNameLabel.setPrefWidth(80);
        Insets in3 = new Insets(5, 10, 5, 10);
        VBox.setMargin(songNameLabel, in3);
        VBox.setMargin(singerLabel, in3);
        leftbottomvBox.getChildren().addAll(songNameLabel, singerLabel);
        HBox bottomhbox = new HBox(5);
        bottomhbox.getChildren().addAll(stackPane, leftbottomvBox);

        bottomhbox.setOnMouseMoved(e -> {
            //改变鼠标的形状
            bottomhbox.setCursor(Cursor.OPEN_HAND);
            //bottomhbox.setCursor(Cursor.CLOSED_HAND);
        });
        Color rgb144 = Color.rgb(114, 114, 114);
        Border border = new Border(new BorderStroke(
                rgb144, rgb144, rgb144, rgb144,/*四个边的颜色*/
                BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,/*四个边的线型--实线*/
                BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                new CornerRadii(1), new BorderWidths(1), null));
        bottomhbox.setBorder(border);

        //1.上一首
        playSvg = new PlaySvg();
        Paint playPaint = Color.WHITE;
        Region preRegion = playSvg.getPreRegion(25.0, playPaint);
        JFXButton button1 = new JFXButton("", preRegion);
        button1.setOnAction(event -> {
            this.preMusic();
        });

        //2.暂停、播放
        Region playOrPauseRegion = playSvg.getPlayOrPauseRegion(25.0, playPaint);
        JFXButton button2 = new JFXButton("", playOrPauseRegion);
        button2.setOnAction(event -> {
            this.playOrPauseMusic();
        });

        //3.下一首
        Region nextRegion = playSvg.getNextRegion(25.0, playPaint);
        JFXButton button3 = new JFXButton("", nextRegion);
        button3.setOnAction(event -> {
            this.nextMusic();
        });

        //4.播放模式图片
        playModeSvg = new PlayModeSvg();
        Region playModeRegion = playModeSvg.getPlayModeRegion(25.0, playPaint);
        JFXButton button6 = new JFXButton("", playModeRegion);
        button6.setOnAction(e -> {
            //此处只处理playMode，实现，放在播放的事件中
            this.playMode++;
            if (this.playMode > 3) {
                this.playMode = 1;
            }
            playModeSvg.changeSvgPath(this.playMode);
        });
        //5.歌词svg
        SVGGlyph r7 = new SVGGlyph("M457.635835 437.766298c7.735172-0.354064 16.101723-0.717338 25.093515-1.079588 8.997931-0.355087 18.986423-0.534166 29.687136-0.534166l39.20593 0c14.483876 0 27.448142 0.091074 38.525432 0.26913 10.873652 0.184195 20.815071 0.455371 29.540803 0.805342 8.801457 0.36225 16.995069 0.812505 24.356734 1.338485 7.375991 0.525979 15.118326 1.14201 23.671119 1.882883l8.978489 0.776689 0-53.18327-8.986675 0.786922c-7.883551 0.689708-15.844873 1.323135-23.662933 1.88186-7.617491 0.544399-15.810081 0.994654-24.354688 1.338485-8.728802 0.358157-18.756179 0.629333-29.810956 0.805342-10.887978 0.181125-23.75503 0.26913-39.337937 0.26913-15.216563 0-27.818579-0.088004-38.531572-0.26913-10.877745-0.176009-20.815071-0.447185-29.543873-0.805342-8.526187-0.342808-16.813944-0.793062-24.633027-1.338485-7.92346-0.552585-15.777335-1.183965-23.343661-1.877767l-9.020444-0.826831 0 52.175313 9.012258-0.816598C442.250426 438.659645 450.043926 438.122409 457.635835 437.766298zM322.402851 314.39241c5.222955 6.445806 10.1645 13.152555 14.684444 19.925818 4.603855 6.913457 9.270131 14.272052 13.862729 21.861914 4.637624 7.684006 9.662057 16.567327 14.934131 26.402323l3.928472 7.329942 45.704948-24.857131-4.437056-7.394411c-6.4192-10.69969-12.279674-20.22053-17.422811-28.310789-5.16565-8.102538-10.483772-15.752776-15.814174-22.747073-5.281283-6.910387-10.93505-13.861706-16.811898-20.669762-6.216585-7.181563-12.958126-14.925944-20.037359-23.016203l-5.066389-5.789866L298.574143 285.352003l5.489014 6.65456C311.537386 301.06794 317.366138 308.181965 322.402851 314.39241zM646.220934 675.172313l-1.537006-9.575076c-1.393743-8.691963-2.27174-16.575514-2.609431-23.441898-0.352017-6.968715-0.529049-15.198144-0.529049-24.460088L641.545448 514.950192c0-7.987928 0.084934-13.603833 0.260943-17.187451 0.160659-3.414773 0.573051-6.956436 1.225921-10.525728l1.783623-9.753131-9.914814 0c-7.18361 0-13.729699 0.092098-19.47147 0.275269-5.585205 0.185218-11.338233 0.36225-17.288758 0.540306-5.742794 0.179079-12.114922 0.26606-19.479657 0.26606l-74.086345 0c-6.553253 0-12.837376-0.089028-18.688641-0.267083-6.039553-0.174985-12.899798-0.446162-20.391423-0.805342l-10.378371-0.49835 1.848091 10.224876c0.698918 3.872191 1.313925 7.907087 1.824555 11.992125 0.496304 3.967358 0.747014 9.261944 0.747014 15.738449L459.536115 626.348328c0 5.248537-0.095167 10.213619-0.283456 14.848173-5.609765-6.426363-9.686616-14.317077-12.124132-23.464411l-3.955078-14.846126-10.209526 11.481496c-11.416004 12.837376-21.856797 23.998577-31.032784 33.174564-4.377704 4.378727-8.696056 8.53335-12.927427 12.444427L389.003711 501.971599c0-11.842723 0.795109-23.945365 2.363837-35.973306 1.505283-11.536754 3.53757-20.082384 6.040576-25.39539l6.222725-13.20986-14.528901 1.463328c-6.943133 0.698918-16.167215 1.325182-27.415396 1.860371-11.165294 0.532119-22.585391 0.801249-33.945113 0.801249-15.994276 0-28.563546-0.267083-37.358863-0.796132-8.905834-0.534166-16.386202-1.156336-22.233374-1.850138l-9.239432-1.095961 0 58.114582 10.535961-3.00852c9.612938-2.745531 20.454868-4.841262 32.220842-6.227842 10.149151-1.189082 23.025413-1.879813 38.359656-2.058892l0 186.362478c0 12.403494-1.685385 23.922852-5.008061 34.231639-3.164063 9.794064-7.891737 17.729803-14.053064 23.589254l-8.083096 7.68503 9.705036 5.497201c9.866718 5.588275 18.73162 13.121855 26.348088 22.38994l6.684236 8.133238 6.315846-8.422833c3.302209-4.40431 6.102998-8.079002 8.569166-11.247158 2.396583-3.087315 5.004991-6.217608 7.761778-9.31311 2.868327-3.228531 5.922896-6.644327 9.159614-10.235109 3.053546-3.39533 6.873548-7.214309 11.355629-11.349489l59.614749-57.482179c-0.51063 5.955642-1.197268 12.527314-2.045589 19.599384l-1.110287 9.250688 53.81465 0 0-42.720987 83.509972 0 0 34.609239L646.220934 675.172313zM509.056971 600.777953l0-81.346703 83.509972 0 0 81.346703L509.056971 600.777953zM705.169511 296.332079c-9.907651 0.36225-20.348444 0.725524-31.336706 1.079588-10.761088 0.356111-22.86066 0.536212-35.957957 0.536212L542.970362 297.947879c-14.009062 0-26.654057-0.180102-37.592177-0.537236-10.82044-0.342808-21.306258-0.795109-31.16786-1.342578-9.94449-0.552585-19.716041-1.186012-29.043477-1.882883l-8.882298-0.664126 0 52.766784 14.038738-0.735757c11.514241-0.603751 21.807678-1.143033 30.886451-1.611707 10.140964-0.524956 19.699668-0.88516 28.402887-1.072425 8.668427-0.178055 16.60826-0.356111 23.827685-0.541329 7.214309-0.178055 14.604626-0.268106 21.967314-0.268106l135.578862 0L690.986487 696.107111c0 5.158487-0.574075 9.43386-1.701758 12.687974-0.234337 0.673335-0.948605 2.720971-5.358031 4.548596-2.37407 0.982374-7.05672 2.152013-15.876596 2.153036-1.263783 0-2.573615-0.024559-3.926426-0.071631-9.886161-0.348947-23.636327-1.952468-40.869826-4.76349l-18.391882-3.001357 10.120498 15.648398c3.345188 5.17179 5.398964 9.79304 6.102998 13.721513 0.851391 4.796236 1.638313 11.113105 2.341325 18.774599l0.687662 7.512091 7.543813 0c22.586414 0 41.056068-0.934279 54.901401-2.775206 14.877849-1.990331 26.16287-5.586229 34.506908-10.999518 8.862855-5.767354 14.83487-13.675464 17.751293-23.5166 2.556219-8.668427 3.852748-19.643386 3.852748-32.620955L742.670613 409.500535c0-15.595186 0.088004-28.55229 0.26913-39.617299 0.185218-11.836583 0.36225-21.458731 0.539282-29.424146 0.171915-8.023744 0.525979-15.117302 1.051959-21.081131 0.527003-5.960759 1.13587-11.021008 1.810229-15.040555l1.721201-10.252505-10.377348 0.633427C725.917044 295.437709 714.978924 295.980062 705.169511 296.332079zM511.99693 63.875796c-247.215428 0-447.629947 200.413496-447.629947 447.629947 0 247.221568 200.413496 447.635063 447.629947 447.635063 247.221568 0 447.635063-200.413496 447.635063-447.635063C959.631994 264.289292 759.218498 63.875796 511.99693 63.875796zM785.110191 784.617981c-35.491329 35.489282-76.801177 63.348794-122.788557 82.800821-47.578622 20.12434-98.155531 30.329772-150.323681 30.329772-52.16815 0-102.739942-10.205433-150.320611-30.329772-45.987381-19.452028-87.298252-47.311539-122.792651-82.800821-35.491329-35.490306-63.349817-76.800153-82.795705-122.787534-20.125363-47.579645-30.329772-98.156554-30.329772-150.324704 0-52.16815 10.204409-102.745059 30.329772-150.320611 19.445888-45.987381 47.305399-87.296205 82.795705-122.792651 35.494399-35.489282 76.80527-63.348794 122.792651-82.800821 47.579645-20.12127 98.152461-30.324656 150.320611-30.324656 52.16815 0 102.745059 10.204409 150.323681 30.324656 45.987381 19.452028 87.298252 47.311539 122.788557 82.800821 35.494399 35.490306 63.348794 76.80527 82.800821 122.792651 20.12434 47.574528 30.328749 98.152461 30.328749 150.320611 0 52.16815-10.204409 102.745059-30.328749 150.324704C848.457962 707.812711 820.599474 749.127675 785.110191 784.617981zM712.23237 332.817038", Paint.valueOf("#ffffff"));
        r7.setSize(25.0);
        JFXButton button7 = new JFXButton("", r7);
        button7.setOnAction(event -> {
            if (!lrcStage.isShowing()) {
                lrcStage.show();
            } else {
                lrcStage.hide();
            }
        });

        HBox hBox1 = new HBox(5);
        hBox1.setAlignment(Pos.CENTER);

        //1.已播放的时间：
        labPlayTime = new Label("00:00");
        labPlayTime.getStyleClass().add("hoverNode");
        //labPlayTime.setPrefWidth(40);
        labPlayTime.setTextFill(Color.WHITE);
        //2.滚动条
        sliderSong = new JFXSlider();
        sliderSong.setValue(0.0D);

        //Slider的鼠标抬起事件中
        sliderSong.setOnMouseReleased(e -> {
            if (currentPlayBean != null && mediaPlayer != null) {
                Duration duration = new Duration(sliderSong.getValue() * 1000);
                mediaPlayer.seek(duration);//设置新的播放时间

                //同时设置Label
                date.setTime((long) mediaPlayer.getCurrentTime().toMillis());

                labPlayTime.setText(simpleDateFormat.format(date));
                //设置前一秒
                prevSecond = (int) duration.toSeconds() - 1;
            }
        });

        //3.总时间标签
        labTotalTime = new Label("00:00");
        //labTotalTime.setPrefWidth(40);
        labTotalTime.setTextFill(Color.WHITE);
        labTotalTime.getStyleClass().add("hoverNode");

        HBox hBox2 = new HBox(5);
        hBox2.getChildren().addAll(labPlayTime, sliderSong, labTotalTime);
        hBox2.setMaxWidth(Double.MAX_VALUE);
        sliderSong.setMaxWidth(Double.MAX_VALUE);
        /*如果HBox里面所有的控件都设置成ALWAYS，那么这些控件需要设置maxWidth="Infinity"，否则会不起作用。*/
        hBox2.setHgrow(sliderSong, Priority.ALWAYS);
        Insets insets = new Insets(0, 5, 0, 5);
        labPlayTime.setPadding(insets);
        labTotalTime.setPadding(insets);
        VBox sliderSongVBox = new VBox(5);
        sliderSongVBox.getChildren().addAll(hBox1, hBox2);

        //2.音量滚动条
        sldVolume = new JFXSlider();
        sldVolume.setMax(100);
        sldVolume.setValue(50.0);
        sldVolume.setMajorTickUnit(1);//每前进一格，增加多少的值
        sldVolume.setOrientation(Orientation.VERTICAL);
        sldVolume.setMinHeight(0.0);
        sldVolume.setPrefWidth(30.0);

        //监听进度条的值发生变化时
        sldVolume.valueProperty().addListener((observable, oldValue, newValue) -> {
            double volumeValue = sldVolume.getValue();
            if (currentPlayBean != null && mediaPlayer != null) {
                mediaPlayer.setVolume(volumeValue / 100.0);
            }
            if (volumeValue == 0) {
                voiceSvg.changeSvgPath(VoiceSvg.VoiceStatus.VOICE_ZERO);
            } else {
                voiceSvg.changeSvgPath(VoiceSvg.VoiceStatus.VOICE_N);
            }
        });
        JFXPopup jfxPopup = new JFXPopup(sldVolume);
        voiceSvg = new VoiceSvg();
        Region voiceRegion = voiceSvg.getVoiceRegion(25.0, playPaint);
        JFXButton button5 = new JFXButton("", voiceRegion);
        button5.setOnAction(e -> {
            jfxPopup.show(button5, JFXPopup.PopupVPosition.BOTTOM, JFXPopup.PopupHPosition.LEFT, 5, -35);
        });
        hBox1.getChildren().addAll(button6, button1, button2, button3, button7, button5);
        //**********************总的BorderPane***********************************//
        BorderPane bottomPane = new BorderPane();
        bottomPane.setLeft(bottomhbox);
        bottomPane.setCenter(sliderSongVBox);
        bottomPane.setMargin(sliderSongVBox, new Insets(2, 5, 2, 5));
        return bottomPane;
    }

    private ChangeListener<Duration> initChangeListener() {
        return (observable, oldValue, newValue) -> {
           /* 此方法用于在媒体播放器播放时自动调用，每隔100毫秒调用一次
            1.由于是每秒使滚动条前进一次，获newValue中的"秒"*/
            currentSecond = (int) newValue.toSeconds();
            /*2.设置滚动条，一秒一次*/
            if (currentSecond == prevSecond + 1) {
                /*设置滚动条*/
                sliderSong.setValue(sliderSong.getValue() + 1);
                /*设置前一秒*/
                prevSecond++;
                /*设置新的播放时间*/
                date.setTime((int) sliderSong.getValue() * 1000);
                labPlayTime.setText(simpleDateFormat.format(date));
            }
            /*1.获取当前的播放时间*/
            millis = newValue.toMillis();
            /*2.判断此次是否在正常的播放区间*/
            min = 0;
            max = 0;
            /*如果歌词列表没有元素则直接返回避免后面抛异常*/
            if (lrcList.size() == 0) {
                return;
            }
            if (currentLrcIndex == 0) {
                min = 0;
            } else {
                min = lrcList.get(currentLrcIndex).doubleValue();
            }
            if (currentLrcIndex != lrcList.size() - 1) {
                max = lrcList.get(currentLrcIndex + 1).doubleValue();
            } else {
                max = lrcList.get(currentLrcIndex).doubleValue();
            }
            /*判断是否在正常的区间*/
            if (millis >= min && millis < max) {
                return;
            }
            if (currentLrcIndex < lrcList.size() - 1 && millis >= lrcList.get(currentLrcIndex + 1).doubleValue()) {
                currentLrcIndex++;//当前歌词索引的指示器
                /*上移,如果歌词详情页是隐藏的，则不展示动画减少内存消耗*/
                if (stopTimeline) {
                    lrcVBox.setLayoutY(lrcVBox.getLayoutY() - 45);
                } else {
                    /*lrcvbox上移过渡动画演示*/
                    t1.play();
                }
                /*当前歌词字体加粗*/
                Label lab_current = (Label) lrcVBox.getChildren().get(currentLrcIndex);
                /*桌面歌词设置歌词*/
                lrcStageLabel.setText(lab_current.getText());
                lab_current.setFont(boldFont);
                lab_current.getStyleClass().add("shadowLabel");
                /*前一行字体不加粗*/
                Label lab_Pre_1 = (Label) lrcVBox.getChildren().get(currentLrcIndex - 1);
                if (lab_Pre_1 != null) {
                    lab_Pre_1.setFont(font);
                    lab_Pre_1.getStyleClass().removeAll("shadowLabel");
                }
                /*后一行字体不加粗*/
                if (currentLrcIndex + 1 < lrcList.size()) {
                    Label lab_next_1 = (Label) lrcVBox.getChildren().get(currentLrcIndex + 1);
                    lab_next_1.setFont(font);
                    lab_next_1.getStyleClass().removeAll("shadowLabel");
                }
            } else if (currentLrcIndex > 0 && millis < lrcList.get(currentLrcIndex).doubleValue()) {
                /*拖动播放条，回退了*/
                currentLrcIndex--;
                /*歌词VBox的下移*/
                lrcVBox.setLayoutY(lrcVBox.getLayoutY() + 45);
                /*当前歌词字体加粗*/
                Label lab_current = (Label) lrcVBox.getChildren().get(currentLrcIndex);
                lab_current.setFont(boldFont);
                lab_current.getStyleClass().add("shadowLabel");
                /*桌面歌词设置歌词*/
                lrcStageLabel.setText(lab_current.getText());
                /*前一行字体不加粗*/
                if (currentLrcIndex - 1 >= 0) {
                    Label lab_Pre_1 = (Label) lrcVBox.getChildren().get(currentLrcIndex - 1);
                    lab_Pre_1.setFont(font);
                    lab_Pre_1.getStyleClass().removeAll("shadowLabel");
                }
                /*后一行字体不加粗*/
                if (currentLrcIndex + 1 < lrcVBox.getChildren().size()) {
                    Label lab_next_1 = (Label) lrcVBox.getChildren().get(currentLrcIndex + 1);
                    lab_next_1.setFont(font);
                    lab_next_1.getStyleClass().removeAll("shadowLabel");
                }
            }
        };
    }

    private Runnable initRunnable() {
        return () -> {
            /*桌面歌词设置*/
            lrcStageLabel.setText("WizardMusicBox");
            /*如果表格为空,比如用户在搜索歌曲过程中，搜不到歌曲，表格数据被清空了*/
            if (tableView.getItems().size() == 0) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();/*释放资源*/
                    mediaPlayer = null;/*引用制空,等待gc回收*/
                }
                playSvg.changeSvgPath(PlaySvg.PlayStatus.PLAY);
                lrcStage.changeSvgPath(PlaySvg.PlayStatus.PLAY);
                simplifyModelStage.changeSvgPath(PlaySvg.PlayStatus.PLAY);
            } else {
                /*根据当前的播放模式选择下一首歌*/
                switch (playMode) {
                    case 1:/*循环播放*/
                        currentIndex++;
                        if (currentIndex >= tableView.getItems().size()) {
                            currentIndex = 0;
                        }
                        currentPlayBean = tableView.getItems().get(currentIndex);
                        break;
                    case 2:/*列表顺序播放*/
                        currentIndex++;
                        if (currentIndex >= tableView.getItems().size()) {
                            currentIndex = 0;
                            return;
                        }
                        currentPlayBean = tableView.getItems().get(currentIndex);
                        break;
                    case 3:/*单曲循环*/
                        break;
                }
                /*设置歌单表格选中，滚动条滚动到当前选中的行位置*/
                tableView.getSelectionModel().clearAndSelect(currentIndex);
                tableView.scrollTo(currentIndex);
                /*精简模式的表格也设置选中，滚动条滚动到当前选中的行位置*/
                simplifyTableView.getSelectionModel().clearAndSelect(currentIndex);
                simplifyTableView.scrollTo(currentIndex);
                /*播放音乐*/
                play();
            }
        };
    }

    /**
     * 拖拽imageView并将imageView里的image保存到剪切板，鼠标一松将保存到本地
     */
    private void dragToPC(ImageView imageView) {
        Dragboard dragboard = imageView.startDragAndDrop(TransferMode.MOVE);
        clipboardContent.clear();
        Image image = imageView.getImage();
        dragboard.setDragView(image);
        //创建临时文件
        File file = null;
        try {
            file = File.createTempFile("img_", ".jpg");
            System.out.println(file.getAbsolutePath());
            BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
            if (bImage != null) {
                ImageIO.write(bImage, "jpg", file);
                List<File> files = new ArrayList<File>();
                files.add(file);
                // 将文件保存到面板里面
                clipboardContent.putFiles(files);
                // 放入拖出内容
                dragboard.setContent(clipboardContent);
            } else {
                service.fail("拖拽保存图片出现错误", "javafx.Image to BufferedImage fail!", customAudioParameter);
            }
            //退出时,删除临时文件
            file.deleteOnExit();
        } catch (Exception e) {
            Log4jUtils.logger.error("", e);
        }
    }

    /* 将匹配到的非法字符以空替换*//* '/ \ : * ? " < > |'*/
    public String validateFileName(String fileName) {
        return fileName.replaceAll("[\\/\\\\\\:\\*\\?\\\"\\<\\>\\|]", "");
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public Stage getMainStage() {
        return mainStage;
    }

    public LrcStage getLrcStage() {
        return lrcStage;
    }

    public TableView<PlayBean> getSimplifyTableView() {
        return simplifyTableView;
    }

    public ToastBarToasterService getService() {
        return service;
    }

    public PlayBean getCurrentPlayBean() {
        return currentPlayBean;
    }

    public ToastParameter getCustomAudioParameter() {
        return customAudioParameter;
    }

    public ImageView getSongCoverImageView() {
        return songCoverImageView;
    }

    public CloudRequest getCloudRequest() {
        return cloudRequest;
    }
}
