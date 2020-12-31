package cn.gxust.cloudutils;

import cn.gxust.localioutils.LocalMusicUtils;
import cn.gxust.localioutils.LrcWriteUtils;
import cn.gxust.utils.Log4jUtils;
import cn.gxust.pojo.PlayBean;
import cn.gxust.ui.MainApp;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.pomo.toasterfx.ToastBarToasterService;
import org.pomo.toasterfx.model.ToastParameter;
import org.pomo.toasterfx.model.ToastState;
import org.pomo.toasterfx.model.impl.SingleToast;
import org.pomo.toasterfx.model.impl.ToastTypes;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * <p>description:  </p>
 * <p>create:  2020/11/25 19:17</p>
 *
 * @author zhaoyijie(AquariusGenius)
 */
public class FileDownService extends Service<Number> {

    private String urlPath;

    private String savepath;

    private String validateMusicName;

    private final ProgressBar progressBar;

    private final MainApp mainApp;

    private SingleToast singleToast;

    private final ToastBarToasterService service;

    private final ToastParameter toastParameter;

    private final VBox progressBarVBox;

    public FileDownService(MainApp mainApp) {
        super();
        progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setProgress(0.0);
        Label label = new Label("下载中...");
        label.setTextFill(Color.WHITE);
        label.setFont(new Font("微软雅黑", 14));
        progressBarVBox = new VBox(label, progressBar);
        progressBarVBox.setSpacing(2);
        Insets in2 = new Insets(2, 2, 2, 2);
        progressBarVBox.setPadding(in2);
        VBox.setMargin(progressBar, in2);
        this.mainApp = mainApp;
        this.service = mainApp.getService();
        toastParameter = ToastParameter.builder().build();
        this.progressProperty().addListener((observable, oldValue, newValue) -> progressBar.setProgress(newValue.doubleValue()));
    }

    @Override
    protected void succeeded() {
        PlayBean currentPlayBean = mainApp.getCurrentPlayBean();
        CloudRequest cloudRequest = mainApp.getCloudRequest();
        ImageView panImageView = mainApp.getSongCoverImageView();
        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(panImageView.getImage(), null);
            LocalMusicUtils.setMusicInf(currentPlayBean, savepath, bufferedImage);//设置MP3的头文件信息
            String musicId = currentPlayBean.getMusicId();
            String lrcString = currentPlayBean.getLrc();
            if (lrcString == null) {
                lrcString = cloudRequest.spiderLrc(musicId);
            }
            if (!lrcString.contains("未找到歌词") && !lrcString.contains("无歌词")) {
                String LrcSavePath = LocalMusicUtils.LOCAL_LRC_DIR + validateMusicName + ".lrc";
                LrcWriteUtils.writeFile(lrcString, LrcSavePath);
            }
            java.awt.Desktop.getDesktop().open(new File(LocalMusicUtils.LOCAL_MUSIC_DIR));
            Platform.runLater(() -> {
                progressBar.setProgress(0.0D);
                Stage mainStage = mainApp.getMainStage();
                /*Windows任务栏图标闪烁效果}*/
                if (!mainStage.isFocused()) {
                    mainStage.requestFocus();
                }
                ToastState state = singleToast.getState();
                if (state == ToastState.SHOWING || state == ToastState.SHOWN) {
                    try {
                        singleToast.close();
                    } catch (Exception e) {
                        Log4jUtils.logger.error("", e);
                    }
                }
                service.success(currentPlayBean.getMusicName(),
                        "下载成功！", mainApp.getCustomAudioParameter());
            });
        } catch (Exception e) {
            Log4jUtils.logger.error("", e);
            ToastState state = singleToast.getState();
            if (state == ToastState.SHOWING || state == ToastState.SHOWN) {
                try {
                    singleToast.close();
                } catch (Exception exception) {
                    Log4jUtils.logger.error("", e);
                }
            }
            this.reset();
        }
        this.reset();
    }

    @Override
    protected void failed() {
        Platform.runLater(() -> {
            progressBar.setProgress(0.0D);
            service.fail(mainApp.getCurrentPlayBean().getMusicName(),
                    "下载失败!", mainApp.getCustomAudioParameter());
            ToastState state = singleToast.getState();
            if (state == ToastState.SHOWING || state == ToastState.SHOWN) {
                singleToast.close();
            }
        });
        this.reset();
    }

    @Override
    public void start() {
        this.singleToast = new SingleToast(toastParameter, ToastTypes.INFO, toast -> progressBarVBox);
        service.push(singleToast);
        service.info("开始下载音乐", "这过程需要一些时间，请耐心等待", mainApp.getCustomAudioParameter());
        /*创建LocalMusic文件夹*/
        LocalMusicUtils.createLocalMusicDir();
        super.start();
    }

    @Override
    protected Task<Number> createTask() {
        FileDownService fileDownService = this;
        return new Task<Number>() {
            @Override
            protected Number call() {
                try {
                    // 统一资源
                    URL url = new URL(urlPath);
                    // 连接类的父类，抽象类
                    URLConnection urlConnection = url.openConnection();
                    // http的连接类
                    HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                    //设置超时
                    httpURLConnection.setConnectTimeout(1000 * 20);
                    //设置请求方式，默认是GET
                    //httpURLConnection.setRequestMethod("POST");
                    // 设置字符编码
                    httpURLConnection.setRequestProperty("Charset", "UTF-8");
                    httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 4.1.1; Nexus 7 Build/JRO03D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Safari/535.19");
                    // 打开到此 URL引用的资源的通信链接（如果尚未建立这样的连接）。
                    httpURLConnection.connect();
                    int statusCode = httpURLConnection.getResponseCode();
                    System.out.println("statusCode:" + statusCode);
                    if (statusCode == 404) {
                        Log4jUtils.logger.error("404 not found");
                        return null;
                    } else if (statusCode == 403) {
                        Log4jUtils.logger.error("403 服务器拒绝请求");
                        return null;
                    } else if (statusCode == 302) {
                        Log4jUtils.logger.error("发生重定向，可能是付费音乐");
                        return null;
                    }
                    // 文件大小
                    int fileLength = httpURLConnection.getContentLength();

                    // 控制台打印文件大小
                    Log4jUtils.logger.debug(" size:" + fileLength / (1024 * 1024) + "MB");
                    BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());
                    File file = new File(savepath);
                    // 校验文件夹目录是否存在，不存在就创建一个目录
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    OutputStream out = new FileOutputStream(file);
                    int size;
                    double len = 0;
                    double progress = 0;
                    byte[] buf = new byte[2048];
                    while ((size = bin.read(buf)) != -1) {
                        len += size;
                        out.write(buf, 0, size);
                        progress = len / fileLength;
                        this.updateProgress(len, fileLength);
                    }
                    // 关闭资源
                    bin.close();
                    out.close();
                    httpURLConnection.disconnect();
                    return progress;
                } catch (Exception e) {
                    Log4jUtils.logger.error("", e);
                    return -1;
                }
            }
        };
    }

    public void setUrlAndSavepath(String urlPath, String savepath, String validateMusicName) {
        this.urlPath = urlPath;
        this.savepath = savepath;
        this.validateMusicName = validateMusicName;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }
}
