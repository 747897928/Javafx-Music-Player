package cn.gxust.ui;

import cn.gxust.cloudutils.CloudMusicSpider;
import cn.gxust.cloudutils.CloudRequest;
import cn.gxust.cloudutils.FileDownService;
import cn.gxust.utils.AnimationUtil;
import com.jfoenix.controls.*;
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
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import cn.gxust.localioutils.LocalMusicUtils;
import cn.gxust.utils.Log4jUtils;
import cn.gxust.pojo.PlayBean;
import cn.gxust.pojo.PlayListBean;
import org.pomo.toasterfx.ToastBarToasterService;
import org.pomo.toasterfx.model.ToastParameter;
import org.pomo.toasterfx.model.impl.SingleAudio;

import java.awt.MenuItem;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static javafx.scene.paint.Color.BLACK;

/**
 * <p>description: 主界面</p>
 * <p>create: 2019/10/14 12:28</p>
 *
 * @author zhaoyijie
 * @version v1.0
 */
public class MainApp extends Application {

    //1.全局的"舞台"对象
    private Stage mainStage;

    //7.歌单名称标签
    private Label labGroupName;

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

    //13.底部歌曲封面的ImageView对象
    private ImageView songCoverImageView;

    //14.碟片的ImageView对象
    private ImageView panImageView;

    //18.当前播放模式：
    private int playMode = 1;//1 : 列表循环；2. 顺序播放  3.单曲循环

    //19.播放时间滚动条对象
    private JFXSlider sliderSong;

    //20.已播放时间的Lable
    private Label labPlayTime;

    //21.音量滚动条
    private JFXSlider sldVolume;

    //24.显示歌词的Listview容器
    private JFXListView<String> lrcListView;

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

    private Label searchTiplabel;

    private CloudMusicSpider cloudMusicSpider;

    private ChangeListener<Duration> changeListener;//播放进度监听器

    private Runnable valueRunnable;

    private Label singerLabel;//用来显示歌手名的label

    private Label songNameLabel;//歌曲名label

    private Label siLab;//歌曲详情页用来显示歌手名的label

    private Label sNLab;//歌曲详情页歌曲名label

    private Label albumLabel; //歌曲详情页专辑名label

    private FlowPane flowPane;

    private ArrayList<PlayListBean> playListBeanList;

    private JFXTabPane tabPane;

    private ImageView songListCoverImageView;//歌单封面

    private final int PANIMAGVIEWSIZE = 200;//音乐封面Image最大大小

    private final int SONGLISTCOVERIMAGEVIEWSIZE = 100;//歌单封面Image最大大小

    private final int MUSICICOIMAGEVIEWSIZE = 80;//发现音乐图片大小

    private Label lrcStageLabel;

    private LrcStage lrcStage;

    private SimplifyModelStage simplifyModelStage;

    private ToastBarToasterService service;

    private ToastParameter customAudioParameter;

    private FileDownService fileDownService;

    private Image panDefaultImage;

    private PlaySvg playSvg;

    private VoiceSvg voiceSvg;

    private PlayModeSvg playModeSvg;

    private MaskerPane maskerPane;

    private JFXDrawer jfxDrawer;

    private RotateTransition rotateTransition;

    private ImageView rodImageView;

    private java.awt.TrayIcon trayIcon;

    @Override
    public void start(Stage primaryStage) throws Exception {
        mainStage = primaryStage;
        Image logoImage = new Image(this.getClass().getResourceAsStream("/images/topandbottom/logo.png"));
        primaryStage.getIcons().add(logoImage);//设置logo
        panDefaultImage = new Image("/images/topandbottom/logoDark.png");

        date = new Date();
        cloudMusicSpider = new CloudMusicSpider();
        cloudRequest = new CloudRequest();//网易云请求工具类
        simpleDateFormat = new SimpleDateFormat("mm:ss");
        lrcList = new ArrayList<>();

        service = new ToastBarToasterService();
        service.initialize();

        try {
            SingleAudio customAudio = new SingleAudio(this.getClass().getResource("/audio/custom.mp3"));
            customAudioParameter = ToastParameter.builder().audio(customAudio).timeout(Duration.seconds(5)).build();
        } catch (Exception e) {
            customAudioParameter = ToastParameter.builder().timeout(Duration.seconds(5)).build();
        }
        service.applyDarkTheme();
        String appName = "WizardMusicBox";
        //primaryStage.setTitle(appName);
        BorderPane mainborderPane = new BorderPane();

        Background background = new Background(new BackgroundFill(Paint.valueOf("#1a3399"), null, null));
        Paint paint = background.getFills().get(0).getFill();
        BorderPane bo2 = new BorderPane();
        bo2.setLeft(getLeftPane());
        bo2.setCenter(getCenterPane(background));
        jfxDrawer = new JFXDrawer();
        jfxDrawer.setDefaultDrawerSize(1080);
        jfxDrawer.setDirection(JFXDrawer.DrawerDirection.BOTTOM);
        jfxDrawer.setContent(bo2);
        HBox h4 = new HBox();
        h4.getChildren().add(getSidePane());
        h4.setAlignment(Pos.CENTER);
        /*h4.getStyleClass().add("bagNode");
        mainborderPane.getStyleClass().add("bagNode");*/
        jfxDrawer.setSidePane(h4);
        mainborderPane.setCenter(jfxDrawer);

        BorderPane bottomPane = getBottomPane();
        bottomPane.setBackground(background);

        mainborderPane.setBottom(bottomPane);

        lrcStage = new LrcStage(this, logoImage, appName);
        lrcStageLabel = lrcStage.getLrcStageLabel();
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
        aboutLabel.setOnMouseClicked(mouseEvent -> aboutStage.showAndWait());

        changeListener = initChangeListener();
        valueRunnable = initRunnable();

        initTray(appName, logoImage);

        double w = 740;
        double h = 600;
        //2.创建一个场景
        Scene scene = new Scene(topJFXDecorator, w, h);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        //3.将场景设置到舞台
        primaryStage.setScene(scene);

        primaryStage.setTitle(appName);

        primaryStage.setMinWidth(w);
        primaryStage.setMinHeight(h);

        primaryStage.setOnCloseRequest(event -> {
            try {
                if (java.awt.SystemTray.isSupported())
                    if (trayIcon != null)
                        java.awt.SystemTray.getSystemTray().remove(trayIcon);
            } catch (Exception e) {
            }
            System.exit(0);
        });
        //lrcStage.show();
        //显示舞台
        primaryStage.show();
        java.awt.SplashScreen splashScreen = java.awt.SplashScreen.getSplashScreen();
        if (splashScreen != null) splashScreen.close();
        changePlaylist();
    }

    private void initTray(String appName, Image logoImage) throws Exception {
        if (java.awt.SystemTray.isSupported()) {
            java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(logoImage, null);
            java.awt.PopupMenu popupMenu = new java.awt.PopupMenu();
            ActionListener actionListener = (event) -> {
                MenuItem menuItem = (MenuItem) event.getSource();
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
            java.awt.Font font = new java.awt.Font("FangSong", java.awt.Font.PLAIN, 18);
            MenuItem preItem = new MenuItem("prevMusic");
            preItem.setFont(font);

            preItem.addActionListener(actionListener);
            MenuItem playOrPauseItem = new MenuItem("pause/play");
            playOrPauseItem.addActionListener(actionListener);
            MenuItem nextItem = new MenuItem("nextMusic");
            nextItem.addActionListener(actionListener);
            MenuItem exitItem = new MenuItem("exitApp");
            exitItem.addActionListener(actionListener);
            popupMenu.add(preItem);
            popupMenu.add(playOrPauseItem);
            popupMenu.add(nextItem);
            popupMenu.add(exitItem);
            for (int i = 0; i < popupMenu.getItemCount(); i++) {
                popupMenu.getItem(i).setFont(font);
            }
            if (bufferedImage != null) {
                trayIcon = new java.awt.TrayIcon(bufferedImage, appName);
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip(appName);
                trayIcon.setPopupMenu(popupMenu);
                tray.add(trayIcon);
            }
        }
    }


    //创建一个左侧面板
    private Node getLeftPane() {
        Font boldFont = Font.font("Timer New Roman", FontWeight.BOLD, FontPosture.ITALIC, 14);
        VBox localVBox = new VBox(10);
        Label recommendLabGd = new Label("推荐");
        recommendLabGd.setPrefHeight(20);
        recommendLabGd.setTextFill(BLACK);
        recommendLabGd.setFont(boldFont);

        Paint paint = Paint.valueOf("#8a8a8a");
        SVGGlyph paperPlaneSvg = new SVGGlyph("M512.00404775 8C233.6469927 8 8 233.6550882 8 512.00404775s225.6469927 503.99595225 504.00404775 503.99595225 503.99595225-225.6469927 503.99595225-503.99595225S790.3530073 8 512.00404775 8z m-52.71006234 731.10014184V631.8666163l64.80479939 18.66831553z m200.99607314-64.48907292L463.45509308 613.80546622l175.71370064-207.37535864-226.73989111 192.52004231-184.10877549-55.77017709 533.35038094-258.24773264z", paint);
        paperPlaneSvg.setSize(20.0);
        Insets in1 = new Insets(5, 5, 5, 5);
        paperPlaneSvg.setPadding(in1);
        JFXButton findMusicGd = new JFXButton("发现音乐", paperPlaneSvg);

        findMusicGd.setOnAction(mouseEvent -> tabPane.getSelectionModel().select(0));

        SVGGlyph songListSvg = new SVGGlyph("M1061.726316 59.230316l2.910316 359.262316a79.225263 79.225263 0 0 1-23.174737 56.589473l-521.485474 521.485474a79.225263 79.225263 0 0 1-111.993263 0l-335.97979-335.97979a79.225263 79.225263 0 0 1 0-111.993263L593.381053 27.109053a79.225263 79.225263 0 0 1 56.643368-23.174737l359.262316 2.910316a52.816842 52.816842 0 0 1 52.331789 52.385684z m-187.122527 134.736842a52.816842 52.816842 0 1 0-74.64421 74.64421 52.816842 52.816842 0 0 0 74.64421-74.64421z", paint);
        songListSvg.setSize(20.0);
        songListSvg.setPadding(in1);

        JFXButton songListGd = new JFXButton("歌单", songListSvg);

        songListGd.setOnAction(mouseEvent -> tabPane.getSelectionModel().select(1));

        SVGGlyph lrcSvg = new SVGGlyph("M872.727273 1024H151.272727C67.490909 1024 0 956.509091 0 872.727273V151.272727C0 67.490909 67.490909 0 151.272727 0h721.454546c83.781818 0 151.272727 67.490909 151.272727 151.272727v721.454546c0 83.781818-67.490909 151.272727-151.272727 151.272727zM151.272727 69.818182c-45.381818 0-81.454545 36.072727-81.454545 81.454545v721.454546c0 45.381818 36.072727 81.454545 81.454545 81.454545h721.454546c45.381818 0 81.454545-36.072727 81.454545-81.454545V151.272727c0-45.381818-36.072727-81.454545-81.454545-81.454545H151.272727zM576 314.181818h267.636364c22.109091 0 40.727273 18.618182 40.727272 40.727273S865.745455 395.636364 843.636364 395.636364h-267.636364c-22.109091 0-40.727273-18.618182-40.727273-40.727273s18.618182-40.727273 40.727273-40.727273zM576 488.727273h267.636364c22.109091 0 40.727273 18.618182 40.727272 40.727272S865.745455 570.181818 843.636364 570.181818h-267.636364c-22.109091 0-40.727273-18.618182-40.727273-40.727273s18.618182-40.727273 40.727273-40.727272zM576 663.272727h267.636364c22.109091 0 40.727273 18.618182 40.727272 40.727273S865.745455 744.727273 843.636364 744.727273h-267.636364c-22.109091 0-40.727273-18.618182-40.727273-40.727273s18.618182-40.727273 40.727273-40.727273zM610.909091 128H546.909091c-83.781818 0-151.272727 67.490909-151.272727 151.272727v308.363637c-26.763636-18.618182-58.181818-29.090909-93.090909-29.090909-89.6 0-162.909091 73.309091-162.909091 162.90909s73.309091 162.909091 162.909091 162.909091 162.909091-73.309091 162.90909-162.909091V279.272727c0-45.381818 36.072727-81.454545 81.454546-81.454545h64c19.781818 0 34.909091-15.127273 34.909091-34.909091s-15.127273-34.909091-34.909091-34.909091zM302.545455 814.545455c-51.2 0-93.090909-41.890909-93.09091-93.09091s41.890909-93.090909 93.09091-93.090909 93.090909 41.890909 93.090909 93.090909-41.890909 93.090909-93.090909 93.09091z", paint);
        lrcSvg.setSize(20.0);
        lrcSvg.setPadding(in1);

        JFXButton lrcGd = new JFXButton("歌词", lrcSvg);

        lrcGd.setOnAction(mouseEvent -> {
            if (jfxDrawer.getDefaultDrawerSize() < mainStage.getHeight()) {
                jfxDrawer.setDefaultDrawerSize(mainStage.getHeight());
            }
            if (jfxDrawer.isClosed()) {
                jfxDrawer.open();
            } else {
                jfxDrawer.close();
            }
        });

       /* SVGGlyph settingSvg = new SVGGlyph("M512 64c249.6 0 448 198.4 448 448s-198.4 448-448 448-448-198.4-448-448 198.4-448 448-448z m0-64C230.4 0 0 230.4 0 512s230.4 512 512 512 512-230.4 512-512-230.4-512-512-512zM758.4 591.36c-4.48 13.696-9.856 26.88-16.384 39.424 26.432 31.424 25.856 77.888-3.776 107.52a79.36 79.36 0 0 1-107.456 3.712 258.112 258.112 0 0 1-39.424 16.32A79.808 79.808 0 0 1 512 832a79.744 79.744 0 0 1-79.36-73.6 256.256 256.256 0 0 1-39.424-16.384 79.36 79.36 0 0 1-107.52-3.776 79.296 79.296 0 0 1-3.712-107.456 255.36 255.36 0 0 1-16.32-39.424A79.744 79.744 0 0 1 192 512c0-41.984 32.512-76.096 73.6-79.36 4.48-13.696 9.856-26.88 16.384-39.424a79.36 79.36 0 0 1 3.712-107.52 79.36 79.36 0 0 1 107.52-3.712c12.608-6.528 25.728-11.968 39.424-16.384A79.744 79.744 0 0 1 512 192c41.984 0 76.096 32.448 79.36 73.6 13.696 4.48 26.88 9.856 39.424 16.384a79.36 79.36 0 0 1 107.52 3.712 79.36 79.36 0 0 1 3.712 107.52c6.528 12.608 11.904 25.728 16.32 39.424C799.488 435.904 832 470.016 832 512s-32.512 76.096-73.6 79.36z m-65.536-203.648l17.152-17.152a40 40 0 0 0-56.576-56.512l-17.28 17.28a218.752 218.752 0 0 0-84.16-35.328V272a40 40 0 0 0-80 0v24c-30.848 5.76-59.264 18.176-84.16 35.264l-17.28-17.28a40 40 0 0 0-56.576 56.576l17.152 17.152a217.472 217.472 0 0 0-35.456 84.288h-23.68a40 40 0 0 0 0 80h23.68c5.76 30.272 17.792 59.008 35.456 84.288l-17.152 17.152a40 40 0 0 0 56.576 56.512l17.28-17.28a218.88 218.88 0 0 0 84.16 35.328v24a40 40 0 0 0 80 0v-24a218.56 218.56 0 0 0 84.16-35.264l17.28 17.28a40 40 0 0 0 56.576-56.576l-17.152-17.152c17.664-25.28 29.76-54.016 35.456-84.288h23.68a40 40 0 0 0 0-80h-23.68a217.472 217.472 0 0 0-35.456-84.288zM392 512a120 120 0 1 1 240 0 120 120 0 0 1-240 0z m40 0a80 80 0 1 0 160 0 80 80 0 0 0-160 0z", paint);
        settingSvg.setSize(20.0);
        settingSvg.setPadding(in1);
        JFXButton settingGd = new JFXButton("设置", settingSvg);
        settingGd.setOnAction(mouseEvent -> tabPane.getSelectionModel().select(3));*/
        Label locallabGd = new Label("我的音乐");
        locallabGd.setPrefHeight(20);
        locallabGd.setTextFill(BLACK);
        locallabGd.setFont(boldFont);

        SVGGlyph musicIco = new SVGGlyph("M592.1792 616.7552a62.4128 53.4528 0 1 0 124.8256 0 62.4128 53.4528 0 1 0-124.8256 0ZM306.9952 652.4416a62.4128 53.4528 0 1 0 124.8256 0 62.4128 53.4528 0 1 0-124.8256 0ZM512 0a512 512 0 1 0 512 512A512 512 0 0 0 512 0z m240.64 616.7552c0 49.0496-43.6736 89.1392-98.048 89.1392S556.544 665.6 556.544 616.7552s43.6736-89.088 98.048-89.088A103.5264 103.5264 0 0 1 716.8 547.84V336.0256a16.0768 16.0768 0 0 0-6.0416-13.4656 17.3056 17.3056 0 0 0-14.2848-4.4544l-213.8624 26.8288a18.2272 18.2272 0 0 0-16.0768 17.8176V421.376l176.4864-23.8592a17.5104 17.5104 0 1 1 4.4544 34.7648l-178.2784 24.064h-2.6624v184.6784a76.3392 76.3392 0 0 1 0.9216 11.4176c0 48.9984-43.6736 89.088-98.048 89.088S271.36 701.44 271.36 652.4416 315.0336 563.2 369.408 563.2a103.6288 103.6288 0 0 1 62.3616 20.48V362.752a54.6304 54.6304 0 0 1 47.2576-53.4528l213.9136-26.7264a58.9824 58.9824 0 0 1 41.8816 13.3632 52.6848 52.6848 0 0 1 17.8176 40.0896z", paint);
        musicIco.setSize(20.0);

        Tooltip tooltip = new Tooltip("(请将本地音乐放到LocalMusic文件夹下，让程序检测出来)");
        /*setTipTime(tooltip,10000);//10毫秒显示时间*/

        musicIco.setPadding(in1);
        JFXButton localMuisButton = new JFXButton("本地音乐", musicIco);

        localMuisButton.setTooltip(tooltip);

        localMuisButton.setOnAction(event -> searchLocalMusic());

        SVGGlyph dirIcoSVGGlyph = new SVGGlyph("M1239.3472 425.7792a62.0544 62.0544 0 0 0-39.5776-22.5792v-126.976c0-50.1248-40.6528-90.7264-90.6752-90.7264h-500.5312a10.0864 10.0864 0 0 1-3.1232-2.5088L585.216 99.84a99.4816 99.4816 0 0 0-36.352-55.3984A102.912 102.912 0 0 0 486.4 22.1696H116.2752C66.2016 22.1696 25.6 62.7712 25.6 112.7936v817.3568c0 20.2752 6.8096 39.936 19.3536 55.808l-0.3072 0.6144 4.096 4.4544c17.4592 19.2 41.2672 29.7984 67.072 29.7984 4.7616 0 327.7312 0.512 607.0272 0.512 140.8 0 270.4384-0.2048 342.784-0.512 39.936-0.1024 75.2128-29.3888 83.9168-69.5296l49.7664-230.0928 52.5824-242.5344a63.0784 63.0784 0 0 0-12.544-52.8896z m-48.7424-14.336h0.512-0.512zM515.072 206.08l0.256 1.1776c5.0176 19.6608 16.6912 37.2224 33.8432 50.5344 17.5616 13.824 37.7856 21.0944 58.624 21.0944h498.7904V402.944H223.0784c-29.696 0-54.9376 20.224-61.2864 49.3568l-42.8544 197.632V115.456h367.616c0.4608 0 2.5088 0.5632 5.0176 2.5088a10.5984 10.5984 0 0 1 2.9696 3.3792l20.48 84.6336z m637.1328 289.9456l-45.9264 213.0944-47.2576 218.112H154.112l93.3376-431.2064h904.704z", Paint.valueOf("#8a8a8a"));
        dirIcoSVGGlyph.setSize(20.0);
        dirIcoSVGGlyph.setPadding(in1);
        JFXButton dirIcoButton = new JFXButton("本地音乐文件夹", dirIcoSVGGlyph);
        dirIcoButton.setOnAction(event -> {
            //打开LocalMusic文件夹
            try {
                LocalMusicUtils.createLocalMusicDir();
                java.awt.Desktop.getDesktop().open(new File(LocalMusicUtils.LOCAL_MUSIC_DIR));
            } catch (IOException e) {
                Log4jUtils.logger.error("", e);
            }
        });

        fileDownService = new FileDownService(this);

        SVGGlyph downSVGGlyph = new SVGGlyph("M54.272 470.528c-4.608-2.048-9.728-3.584-14.848-3.584-23.04 1.536-40.448 22.016-38.912 45.056-0.512 17.408 8.704 33.28 24.064 41.472l375.808 179.712c9.216 4.608 20.48 4.608 29.696 0l171.008-81.92c19.968-64.512 62.464-119.296 118.784-154.112L414.72 643.072 54.272 470.528zM590.336 754.176l-175.104 83.968L54.272 665.6c-4.608-2.048-9.728-3.584-14.848-3.584-23.04 2.048-40.448 22.016-38.912 45.056-0.512 17.408 8.704 33.28 24.064 41.472l375.808 179.712c9.216 4.608 20.48 4.608 29.696 0l181.248-87.04c-11.264-26.624-18.432-56.32-20.992-87.04zM24.064 358.4l375.808 179.712c4.608 2.048 9.728 3.584 14.848 3.584s10.24-1.024 14.848-3.584L805.888 358.4c23.04-13.312 30.72-42.496 17.92-65.536-4.096-7.168-10.24-13.312-17.92-17.92L430.08 95.232c-9.216-4.608-20.48-4.608-29.696 0L24.064 275.456c-23.04 13.312-30.72 42.496-17.408 65.536 4.096 7.168 10.24 13.312 17.408 17.408zM931.84 610.304v158.72h92.16l-159.232 159.232-159.232-159.232h92.16v-158.72H931.84z", Paint.valueOf("#8a8a8a"));
        downSVGGlyph.setSize(20.0);
        downSVGGlyph.setPadding(in1);

        JFXButton downMusicButton = new JFXButton("下载当前音乐", downSVGGlyph);
        downMusicButton.setOnAction(event -> {
            //创建LocalMusic文件夹
            LocalMusicUtils.createLocalMusicDir();
            if (currentPlayBean != null && !currentPlayBean.isPlayable()) {
                service.fail("操作失败！", "无法播放的音乐不能下载", customAudioParameter);
                return;
            }
            if (fileDownService.isRunning()) {
                //Log4jUtils.logger.warn("正在下载！");
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
        });


        localVBox.getChildren().addAll(recommendLabGd, findMusicGd, songListGd, lrcGd, locallabGd, localMuisButton, downMusicButton, dirIcoButton);
        Color rgb144 = Color.rgb(114, 114, 114);
        Border border = new Border(new BorderStroke(
                rgb144, rgb144, rgb144, rgb144,//四个边的颜色
                BorderStrokeStyle.SOLID,//四个边的线型--实线
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                new CornerRadii(1),
                new BorderWidths(1),
                null
        ));
        localVBox.setBorder(border);
        localVBox.setPrefWidth(160.0);
        return localVBox;
    }

    private Node getSidePane() {

        StackPane s1 = new StackPane();
        ImageView iv1 = new ImageView("/images/topandbottom/pan.png");
        iv1.setFitHeight(250);
        iv1.setFitWidth(250);
        //4.碟片的图片
        panImageView = new ImageView("/images/topandbottom/logoDark.png");
        panImageView.setFitHeight(170);
        panImageView.setFitWidth(170);
        Label panLabel = new Label("", panImageView);
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

        siLab = new Label("歌手");
        siLab.setLayoutX(480);
        siLab.setLayoutY(70);
        siLab.setPrefWidth(140.0);

        albumLabel = new Label("专辑：");
        albumLabel.setLayoutX(300);
        albumLabel.setLayoutY(70);
        albumLabel.setPrefWidth(140.0);

        //5.歌词的listview容器
        lrcListView = new JFXListView();
        lrcListView.getItems().addAll(FXCollections.observableArrayList("暂无歌词"));
        lrcListView.setExpanded(true);
        lrcListView.setDepth(4);

        lrcListView.setPrefSize(400.0, 400.0);
        lrcListView.setLayoutX(300.0);
        lrcListView.setLayoutY(100.0);

        SVGGlyph svgGlyph = new SVGGlyph("M-45.3,472l5.2-5.2c0.1-0.1,0.2-0.1,0.3,0l1,1c0.1,0.1,0.1,0.2,0,0.3l-5.2,5.2h3.8c0.2,0,0.4,0.2,0.4,0.4v1.2c0,0.1-0.1,0.2-0.2,0.2h-6.3c-0.4,0-0.8-0.4-0.8-0.8V468c0-0.1,0.1-0.2,0.2-0.2h1.2c0.2,0,0.4,0.2,0.4,0.4V472z M-28.7,458l-5.2,5.2c-0.1,0.1-0.2,0.1-0.3,0c0,0,0,0,0,0l-1-1c-0.1-0.1-0.1-0.2,0-0.3c0,0,0,0,0,0l5.2-5.2h-3.8c-0.2,0-0.4-0.2-0.4-0.4v-1.2c0-0.1,0.1-0.2,0.2-0.2h6.3c0.4,0,0.8,0.4,0.8,0.8v6.3c0,0.1-0.1,0.2-0.2,0.2h-1.2c-0.2,0-0.4-0.2-0.4-0.4C-28.7,461.8-28.7,458-28.7,458z", BLACK);
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
        Paint paint = Paint.valueOf("#eae3e3");
        Border border = new Border(new BorderStroke(
                paint, paint, paint, paint,/*四个边的颜色*/
                BorderStrokeStyle.SOLID,/*四个边的线型--实线*/
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                new CornerRadii(1),
                new BorderWidths(1),
                new Insets(1, 1, 1, 1)
        ));
        button.setBorder(border);
        //AnchorPane
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().addAll(button, s1, rodImageView, lrcListView, sNLab, siLab, albumLabel);
        return anchorPane;
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


        songListCoverImageView = new ImageView(panDefaultImage);
        songListCoverImageView.setFitWidth(SONGLISTCOVERIMAGEVIEWSIZE);
        songListCoverImageView.setFitHeight(SONGLISTCOVERIMAGEVIEWSIZE);

        songListCoverImageView.setLayoutX(30);
        songListCoverImageView.setLayoutY(14);

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
        anchorPane.getChildren().addAll(lab1, labGroupName, songListCoverImageView,
                lab3, filterTextField, searchButton);
        anchorPane.setPrefHeight(150);

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
        simplifyTableView.setPrefHeight(260.0);
        TableColumn siCol = new TableColumn("");
        siCol.setCellValueFactory(new PropertyValueFactory<>("musicName"));
        simplifyTableView.getColumns().addAll(siCol);
        siCol.prefWidthProperty().bind(simplifyTableView.widthProperty());
        simplifyTableView.getStyleClass().add("simplifyTableView");
        simplifyTableView.setItems(tableObList);
        simplifyTableView.setRowFactory(tv -> {
            TableRow<PlayBean> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                //验证双击
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    //1.获取选中行的索引
                    this.currentIndex = row.getIndex();
                    //2.将前一秒置为：0
                    this.prevSecond = 0;
                    //3.判断当前是否正在播放，如果是：将其停止
                    if (this.currentPlayBean != null) {
                        if (this.mediaPlayer != null) {
                            this.mediaPlayer.stop();
                        }
                    }
                    //4.获取当前的PlayBean
                    this.currentPlayBean = row.getItem();
                    this.tableView.getSelectionModel().select(this.currentIndex);
                    this.tableView.scrollTo(currentIndex);
                    //5.播放
                    play();
                }
            });
            return row;
        });


        tableView.setRowFactory(tv -> {
            TableRow<PlayBean> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                //验证双击
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    //1.获取选中行的索引
                    this.currentIndex = row.getIndex();
                    //2.将前一秒置为：0
                    this.prevSecond = 0;
                    //3.判断当前是否正在播放，如果是：将其停止
                    if (this.currentPlayBean != null) {
                        if (this.mediaPlayer != null) {
                            this.mediaPlayer.stop();
                        }
                    }
                    //4.获取当前的PlayBean
                    this.currentPlayBean = row.getItem();
                    this.simplifyTableView.getSelectionModel().select(this.currentIndex);
                    this.simplifyTableView.scrollTo(currentIndex);
                    //5.播放
                    play();
                }
            });
            return row;
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
        if ((playListBean.getImageUrl() == null) || (playListBean.getPlayListUrl() == null)) {
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
                songListCoverImageView.setImage(new Image(
                        playListBean.getImageUrl(),
                        SONGLISTCOVERIMAGEVIEWSIZE, SONGLISTCOVERIMAGEVIEWSIZE,
                        false, false));
                tabPane.getSelectionModel().select(1);
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
                searchTiplabel.setText("");
                labGroupName.setText(text.trim());
                /*Windows任务栏图标闪烁效果}*/
                if (!mainStage.isFocused()) {
                    mainStage.requestFocus();
                }
                tabPane.getSelectionModel().select(1);
                if (list.size() != 0) {
                    songListCoverImageView.setImage(new Image(list.get(0).
                            getImageUrl(), SONGLISTCOVERIMAGEVIEWSIZE, SONGLISTCOVERIMAGEVIEWSIZE, false, false));
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
        list.clear();//清空表格
        searchTiplabel.setText("searching....");
        new Thread(() -> {
            LocalMusicUtils.getLocalMusicInf(list);
            //用GUI线程更新UI组件
            Platform.runLater(() -> {
                songListCoverImageView.setImage(panDefaultImage);
                /*Windows任务栏图标闪烁效果}*/
                if (!mainStage.isFocused()) {
                    mainStage.requestFocus();
                }
                tableView.refresh();
                tabPane.getSelectionModel().select(1);
                searchTiplabel.setText("");
            });
            if (list.size() != 0) {
                currentIndex = 0;
            }
            //System.gc();
        }).start();
    }

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
                        img.setImage(new Image(playListBean.getImageUrl(),
                                PANIMAGVIEWSIZE, PANIMAGVIEWSIZE,
                                false, false, true));
                    }
                }
                tabPane.getSelectionModel().select(0);
                searchTiplabel.setText("");
            });
        }).start();
    }

    //播放
    private void play() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            try {
                mediaPlayer.currentTimeProperty().removeListener(changeListener);
                mediaPlayer.setOnEndOfMedia(null);
            } catch (Exception e) {
                Log4jUtils.logger.error("", e);
            }
            mediaPlayer.dispose();//释放资源
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
        if (currentPlayBean == null) {
            Log4jUtils.logger.error("currentPlayBean 为空");
            return;
        }
        String mp3Url = currentPlayBean.getMp3Url();
        boolean playable = currentPlayBean.isPlayable();
        if (!currentPlayBean.isLocalMusic() && playable) {
            mp3Url = cloudRequest.getReal(mp3Url);
            currentPlayBean.setMp3Url(mp3Url);
        }
        rotateTransition.stop();
        mediaPlayer = new MediaPlayer(new Media(mp3Url));

        //System.out.println(mp3Url);
        if (!playable || mp3Url.equals("https://music.163.com/404")) {
            currentPlayBean.setPlayable(false);
            labTotalTime.setText("00:00");
            sliderSong.setValue(0.0);
            service.fail("错误", "无法播放此音乐，可能是付费音乐!", customAudioParameter);
            return;
        }
        new Thread(() -> mediaPlayer.play()).start();
        loadLrc();
        mediaPlayer.currentTimeProperty().addListener(changeListener);
        boolean isSongListMusic = false; // 判断是否是歌单音乐，歌单音乐的信息一开始是不全的
        if (currentPlayBean.getArtistName() != null &&
                currentPlayBean.getArtistName().equals("")
                && !currentPlayBean.isLocalMusic()) {
            isSongListMusic = true;
        }
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
            /*歌单音乐将不设置图片，待最后获取到完整的音乐信息才设置*/
            if (!isSongListMusic) {
                try {
                    songCoverImageView.setImage(new Image(currentPlayBean.getImageUrl(), PANIMAGVIEWSIZE, PANIMAGVIEWSIZE, false, false, true));
                } catch (Exception e) {
                    songCoverImageView.setImage(panDefaultImage);
                }
                panImageView.setImage(songCoverImageView.getImage());
            }
        }
        if (!isSongListMusic) {
            if (songCoverImageView.getImage() != null) {
                simplifyModelStage.setImage(songCoverImageView.getImage());
            }
        }
        //资源全部载入播放器后，这时候可以获取到总时间
        mediaPlayer.setOnReady(() -> {
            double total_second = Math.floor(mediaPlayer.getTotalDuration().toSeconds());
            date.setTime((long) (total_second * 1000));
            labTotalTime.setText(simpleDateFormat.format(date));
            if (total_second != 0.0) {
                sliderSong.setMax(total_second);
            }
        });

        labTotalTime.setText("00:00");
        //在音频文件还没完整地读取之前，这时候无法获取总时间，这时候就先给个100，稍后在监听器那边修改
        sliderSong.setMax(100);
        sliderSong.setMajorTickUnit(1);//每次前进1格
        sliderSong.setValue(0);
        prevSecond = 0;
        playSvg.changeSvgPath(PlayStatus.PAUSE);
        lrcStage.changeSvgPath(PlayStatus.PAUSE);
        simplifyModelStage.changeSvgPath(PlayStatus.PAUSE);
        mediaPlayer.setVolume(sldVolume.getValue() / 100.0);
        mediaPlayer.setOnEndOfMedia(valueRunnable);
        if (isSongListMusic) {
            new Thread(() -> {
                cloudRequest.getMusicDetail(currentPlayBean);
                Platform.runLater(() -> {
                    songNameLabel.setText(currentPlayBean.getMusicName());
                    singerLabel.setText(currentPlayBean.getArtistName());
                    simplifyModelStage.setSongNameAndSingerName(
                            currentPlayBean.getMusicName(),
                            currentPlayBean.getArtistName()
                    );
                    sNLab.setText(currentPlayBean.getMusicName());
                    siLab.setText("歌手：" + currentPlayBean.getArtistName());
                    albumLabel.setText("专辑：" + currentPlayBean.getArtistName());
                    try {
                        songCoverImageView.setImage(new Image(currentPlayBean.getImageUrl(), PANIMAGVIEWSIZE, PANIMAGVIEWSIZE, false, false, true));
                    } catch (Exception e) {
                        songCoverImageView.setImage(panDefaultImage);
                    }
                    if (songCoverImageView.getImage() != null) {
                        simplifyModelStage.setImage(songCoverImageView.getImage());
                    }
                    panImageView.setImage(songCoverImageView.getImage());
                    tableView.refresh();
                });
            }).start();
        }
        RotateTransition rt = new RotateTransition(Duration.millis(200), rodImageView);
        rt.setFromAngle(0);
        rt.setToAngle(35);
        rt.setCycleCount(1);
        rt.play();
        if (rotateTransition.getStatus() != Animation.Status.RUNNING) {
            rotateTransition.play();
        }
        //System.gc();
    }

    //加载正在播放的歌曲的lrc文件(歌词文件)
    private void loadLrc() {
        if (currentPlayBean.getMusicName() == null || currentPlayBean.getMusicName().equals("")) {
            return;
        }
        //初始化listview
        ObservableList observableList = this.lrcListView.getItems();
        observableList.clear();
        this.lrcList.clear();
        this.currentLrcIndex = 0;
        String musicId = currentPlayBean.getMusicId();
        String[] musicLrcList;
        String lrcString;
        if (!currentPlayBean.isLocalMusic()) {
            lrcString = cloudRequest.spiderLrc(musicId);
            //封装歌词Label
        } else {
            String localLrlPath = currentPlayBean.getLocalLrlPath();
            lrcString = LocalMusicUtils.getLrc(localLrlPath);
        }
        musicLrcList = lrcString.split("\n");
        for (String row : musicLrcList) {
            if (!row.contains("[") || !row.contains("]")) {
                continue;
            }
            if (row.charAt(1) < '0' || row.charAt(1) > '9') {
                continue;
            }
            String strTime = row.substring(1, row.indexOf("]"));//00:03.29
            String strMinute = strTime.substring(0, strTime.indexOf(":"));//取出：分钟
            String strSecond = strTime.substring(strTime.indexOf(":") + 1);//取出：秒和毫秒
            //转换为int分钟
            BigDecimal totalMilli = null;
            try {
                int intMinute = Integer.parseInt(strMinute);
                //换算为总的毫秒
                totalMilli = new BigDecimal(intMinute * 60).add(new BigDecimal(strSecond)).multiply(new BigDecimal("1000"));
            } catch (NumberFormatException e) {
                Log4jUtils.logger.error("", e);
                totalMilli = new BigDecimal(0);
            }
            this.lrcList.add(totalMilli);
            observableList.add(row.trim().substring(row.indexOf("]") + 1));
        }
        if (observableList.size() != 0) {
            this.lrcListView.getSelectionModel().select(0);
            this.lrcListView.scrollTo(currentLrcIndex);
        }
    }

    /**
     * 切歌，上一首歌曲
     */
    public void preMusic() {
        if (this.currentPlayBean != null) {
            this.mediaPlayer.stop();
        }
        if (this.tableView.getItems().size() != 0) {
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

    public void playOrPauseMusic() {
        if (this.mediaPlayer != null) {
            //判断如果当前正在播放，暂停
            if (this.mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                //设置播放器暂停
                this.mediaPlayer.pause();
                //设置播放按钮图标为：播放
                playSvg.changeSvgPath(PlayStatus.PLAY);
                lrcStage.changeSvgPath(PlayStatus.PLAY);
                RotateTransition rt = new RotateTransition(Duration.millis(200), rodImageView);
                rt.setFromAngle(35);
                rt.setToAngle(0);
                rt.setCycleCount(1);
                rt.play();
                if (rotateTransition.getStatus() == Animation.Status.RUNNING) {
                    rotateTransition.pause();
                }
                simplifyModelStage.changeSvgPath(PlayStatus.PLAY);
            } else if (this.mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                this.mediaPlayer.play();
                playSvg.changeSvgPath(PlayStatus.PAUSE);
                lrcStage.changeSvgPath(PlayStatus.PAUSE);
                RotateTransition rt = new RotateTransition(Duration.millis(200), rodImageView);
                rt.setFromAngle(0);
                rt.setToAngle(35);
                rt.setCycleCount(1);
                rt.play();
                if (rotateTransition.getStatus() != Animation.Status.RUNNING) {
                    rotateTransition.play();
                }
                simplifyModelStage.changeSvgPath(PlayStatus.PAUSE);
            }
        }
    }

    public void nextMusic() {
        if (this.currentPlayBean != null) {
            this.mediaPlayer.stop();
        }
        if (this.tableView.getItems().size() != 0) {
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
        singerLabel.setFont(Font.font("Timer New Roman",
                FontWeight.BOLD, FontPosture.ITALIC, 12));
        singerLabel.setPrefWidth(80);
        songNameLabel = new Label("");
        songNameLabel.setTextFill(Color.WHITE);
        songNameLabel.setFont(Font.font("Timer New Roman",
                FontWeight.BOLD, FontPosture.ITALIC, 12));
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
                rgb144, rgb144, rgb144, rgb144,//四个边的颜色
                BorderStrokeStyle.SOLID,//四个边的线型--实线
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                new CornerRadii(1),
                new BorderWidths(1),
                null

        ));
        bottomhbox.setBorder(border);


        //*************************中间滚动条部分**********************************//
        //1.上一首
        playSvg = new PlaySvg();
        Paint playPaint = Paint.valueOf("#ffffff");
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
        //labPlayTime.setPrefWidth(40);
        labPlayTime.setTextFill(Color.WHITE);
        //2.滚动条
        sliderSong = new JFXSlider();
        sliderSong.setValue(0.0D);

        //Slider的鼠标抬起事件中
        sliderSong.setOnMouseReleased(e -> {
            if (currentPlayBean != null) {
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
            double value = sldVolume.getValue();
            if (currentPlayBean != null) {
                mediaPlayer.setVolume(value / 100.0);
            }
            if (value == 0) {
                voiceSvg.changeSvgPath(VoiceStatus.VOICE_ZERO);
            } else {
                voiceSvg.changeSvgPath(VoiceStatus.VOICE_N);
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
            //2.设置滚动条，一秒一次
            if (currentSecond == prevSecond + 1) {
                //设置滚动条
                sliderSong.setValue(sliderSong.getValue() + 1);
                //设置前一秒
                prevSecond++;
                //设置新的播放时间
                date.setTime((int) sliderSong.getValue() * 1000);
                labPlayTime.setText(simpleDateFormat.format(date));
            }
            //1.获取当前的播放时间
            millis = newValue.toMillis();
            //2.判断此次是否在正常的播放区间
            min = 0;
            max = 0;
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
            //判断是否在正常的区间
            if (millis >= min && millis < max) {
                return;
            }
            if (currentLrcIndex < lrcList.size() - 1 &&
                    millis >= lrcList.get(currentLrcIndex + 1).doubleValue()) {
                currentLrcIndex++;//当前歌词索引的指示器
            } else if (currentLrcIndex > 0 && millis < lrcList.get(currentLrcIndex).doubleValue()) {
                //拖动播放条，回退了
                currentLrcIndex--;
                lrcListView.scrollTo(currentLrcIndex);
            }
            lrcListView.getSelectionModel().select(currentLrcIndex);
            lrcListView.scrollTo(currentLrcIndex);
            lrcStageLabel.setText(lrcListView.getItems().get(currentLrcIndex));
        };
    }

    private Runnable initRunnable() {
        return () -> {
            lrcStageLabel.setText("WizardMusicBox");
            //如果表格为空,比如用户在搜索歌曲过程中，搜不到歌曲，表格数据被清空了
            if (tableView.getItems().size() == 0) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();//释放资源
                    mediaPlayer = null;
                }
                playSvg.changeSvgPath(PlayStatus.PLAY);
                lrcStage.changeSvgPath(PlayStatus.PLAY);
                simplifyModelStage.changeSvgPath(PlayStatus.PLAY);
            } else {
                //根据当前的播放模式选择下一首歌
                switch (playMode) {
                    case 1://循环播放
                        currentIndex++;
                        if (currentIndex >= tableView.getItems().size()) {
                            currentIndex = 0;
                        }
                        currentPlayBean = tableView.getItems().get(currentIndex);
                        break;
                    case 2://列表顺序播放
                        currentIndex++;
                        if (currentIndex >= tableView.getItems().size()) {
                            return;
                        }
                        currentPlayBean = tableView.getItems().get(currentIndex);
                        break;
                    case 3://单曲循环
                        break;
                }
                //tableView.getSelectionModel().select(currentIndex);
                tableView.getSelectionModel().clearAndSelect(currentIndex);
                tableView.scrollTo(currentIndex);
                simplifyTableView.getSelectionModel().clearAndSelect(currentIndex);
                simplifyTableView.scrollTo(currentIndex);
                play();
            }
        };
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
