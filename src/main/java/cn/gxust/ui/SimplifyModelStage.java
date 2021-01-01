package cn.gxust.ui;

import cn.gxust.utils.AnimationUtil;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.svg.SVGGlyph;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.WHITE;

/**
 * <p>description:  </p>
 * <p>create: 2020/10/14 21:29</p>
 *
 * @author :zhaoyijie
 * @version v1.0
 */
public class SimplifyModelStage extends MusicStage {

    private final MainApp mainApp;

    private ImageView panImageView;

    private Label singerLabel;//用来显示歌手名的label

    private Label songNameLabel;//歌曲名label

    private final PlaySvg playSvg;

    public SimplifyModelStage(MainApp mainApp, Image logoImage, String appName, Paint paint) {
        this.playSvg = new PlaySvg();
        this.mainApp = mainApp;
        this.getIcons().add(logoImage);
        HBox mainHbox = new HBox(2);

        Insets insets = new Insets(5, 5, 5, 5);
        mainHbox.setPadding(insets);
        mainHbox.setPrefSize(240, 100);
        //hBox.setAlignment(Pos.CENTER);
        mainHbox.getChildren().addAll(initLeftPane(), initCenterPane(), initRightPane());
        mainHbox.setBorder(new Border(new BorderStroke(
                paint, paint, paint, paint,
                BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                new CornerRadii(1), new BorderWidths(2), null)));
        mainHbox.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(20), null)));
        this.diyStage(true, 0, 80, mainHbox);
        this.setX(screenBounds.getWidth() - mainHbox.getPrefWidth() - 10);
        this.addDragEvent();
        this.getScene().getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        this.setTitle(appName);
        mainApp.getSimplifyTableView().setPrefWidth(mainHbox.getPrefWidth());
        this.getScene().getRoot().getStyleClass().add("bag1Node");
    }

    public void setImage(Image image) {
        panImageView.setImage(image);
    }

    private Node initLeftPane() {
        this.panImageView = new ImageView("/images/topandbottom/pandefault.png");
        final int PANIMAGVIEWSIZE = 70;
        this.panImageView.setFitWidth(PANIMAGVIEWSIZE);
        this.panImageView.setFitHeight(PANIMAGVIEWSIZE);
        Circle circle1 = new Circle();
        int halfCircleSize = PANIMAGVIEWSIZE / 2;
        circle1.setCenterX(halfCircleSize);
        circle1.setCenterY(halfCircleSize);
        circle1.setRadius(halfCircleSize);
        panImageView.setClip(circle1);

        StackPane stackPane = new StackPane();

        SVGPath svgPath = new SVGPath();
        svgPath.setContent("M-45.3,472l5.2-5.2c0.1-0.1,0.2-0.1,0.3,0l1,1c0.1,0.1,0.1,0.2,0,0.3l-5.2,5.2h3.8c0.2,0,0.4,0.2,0.4,0.4v1.2c0,0.1-0.1,0.2-0.2,0.2h-6.3c-0.4,0-0.8-0.4-0.8-0.8V468c0-0.1,0.1-0.2,0.2-0.2h1.2c0.2,0,0.4,0.2,0.4,0.4V472z M-28.7,458l-5.2,5.2c-0.1,0.1-0.2,0.1-0.3,0c0,0,0,0,0,0l-1-1c-0.1-0.1-0.1-0.2,0-0.3c0,0,0,0,0,0l5.2-5.2h-3.8c-0.2,0-0.4-0.2-0.4-0.4v-1.2c0-0.1,0.1-0.2,0.2-0.2h6.3c0.4,0,0.8,0.4,0.8,0.8v6.3c0,0.1-0.1,0.2-0.2,0.2h-1.2c-0.2,0-0.4-0.2-0.4-0.4C-28.7,461.8-28.7,458-28.7,458z");
        svgPath.setFill(Paint.valueOf("#eaeaea"));
        svgPath.setScaleX(2.0);
        svgPath.setScaleY(2.0);

        Button button = new Button("", svgPath);
        button.setOnAction(event -> {
            this.hide();
            mainApp.getMainStage().show();
            mainApp.getTabPane().getSelectionModel().select(1);
        });
        button.setOpacity(0.0);
        stackPane.setOnMouseEntered(event -> AnimationUtil.fade(button, 0.1, 0, 0, 1));
        stackPane.setOnMouseExited(event -> AnimationUtil.fade(button, 0.1, 0, 1, 0));
        button.setPrefSize(PANIMAGVIEWSIZE, PANIMAGVIEWSIZE);
        button.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3)");

        Circle circle2 = new Circle();
        circle2.setCenterX(halfCircleSize);
        circle2.setCenterY(halfCircleSize);
        circle2.setRadius(halfCircleSize);
        button.setClip(circle2);
        stackPane.getChildren().addAll(panImageView, button);
        return stackPane;
    }

    private Node initRightPane() {

        VBox vBox = new VBox();
        //4.右侧的关闭按钮
        //Region closeRegion = playSvg.getCloseRegion(15.0, WHITE);
        Paint paint = WHITE;
        SVGGlyph closeSvgGlyph = new SVGGlyph("M621.469971 490.921719l399.945118-399.945117a52.380261 52.380261 0 0 0 0-73.659742l-2.182511-2.182511a52.380261 52.380261 0 0 0-73.659742 0L545.627718 415.625094 145.682601 15.134349a52.380261 52.380261 0 0 0-73.659742 0L69.840348 17.31686a51.357209 51.357209 0 0 0 0 73.659742l399.945117 399.945117-399.945117 399.945118a52.380261 52.380261 0 0 0 0 73.659742l2.182511 2.182511a52.380261 52.380261 0 0 0 73.659742 0L545.627718 566.763972l399.945118 399.945118a52.380261 52.380261 0 0 0 73.659742 0l2.182511-2.182511a52.380261 52.380261 0 0 0 0-73.659742L621.469971 490.921719z", paint);
        closeSvgGlyph.setSize(18.0);

        JFXButton closeButton = new JFXButton("", closeSvgGlyph);
        closeButton.setOnAction(e -> {
            this.hide();
            mainApp.getMainStage().show();
        });
        this.setOnCloseRequest(event -> {
            this.hide();
            mainApp.getMainStage().show();
        });

        closeButton.getStyleClass().add("shadowLabel");
        SVGGlyph lrcSvgGlyph = new SVGGlyph("M457.635835 437.766298c7.735172-0.354064 16.101723-0.717338 25.093515-1.079588 8.997931-0.355087 18.986423-0.534166 29.687136-0.534166l39.20593 0c14.483876 0 27.448142 0.091074 38.525432 0.26913 10.873652 0.184195 20.815071 0.455371 29.540803 0.805342 8.801457 0.36225 16.995069 0.812505 24.356734 1.338485 7.375991 0.525979 15.118326 1.14201 23.671119 1.882883l8.978489 0.776689 0-53.18327-8.986675 0.786922c-7.883551 0.689708-15.844873 1.323135-23.662933 1.88186-7.617491 0.544399-15.810081 0.994654-24.354688 1.338485-8.728802 0.358157-18.756179 0.629333-29.810956 0.805342-10.887978 0.181125-23.75503 0.26913-39.337937 0.26913-15.216563 0-27.818579-0.088004-38.531572-0.26913-10.877745-0.176009-20.815071-0.447185-29.543873-0.805342-8.526187-0.342808-16.813944-0.793062-24.633027-1.338485-7.92346-0.552585-15.777335-1.183965-23.343661-1.877767l-9.020444-0.826831 0 52.175313 9.012258-0.816598C442.250426 438.659645 450.043926 438.122409 457.635835 437.766298zM322.402851 314.39241c5.222955 6.445806 10.1645 13.152555 14.684444 19.925818 4.603855 6.913457 9.270131 14.272052 13.862729 21.861914 4.637624 7.684006 9.662057 16.567327 14.934131 26.402323l3.928472 7.329942 45.704948-24.857131-4.437056-7.394411c-6.4192-10.69969-12.279674-20.22053-17.422811-28.310789-5.16565-8.102538-10.483772-15.752776-15.814174-22.747073-5.281283-6.910387-10.93505-13.861706-16.811898-20.669762-6.216585-7.181563-12.958126-14.925944-20.037359-23.016203l-5.066389-5.789866L298.574143 285.352003l5.489014 6.65456C311.537386 301.06794 317.366138 308.181965 322.402851 314.39241zM646.220934 675.172313l-1.537006-9.575076c-1.393743-8.691963-2.27174-16.575514-2.609431-23.441898-0.352017-6.968715-0.529049-15.198144-0.529049-24.460088L641.545448 514.950192c0-7.987928 0.084934-13.603833 0.260943-17.187451 0.160659-3.414773 0.573051-6.956436 1.225921-10.525728l1.783623-9.753131-9.914814 0c-7.18361 0-13.729699 0.092098-19.47147 0.275269-5.585205 0.185218-11.338233 0.36225-17.288758 0.540306-5.742794 0.179079-12.114922 0.26606-19.479657 0.26606l-74.086345 0c-6.553253 0-12.837376-0.089028-18.688641-0.267083-6.039553-0.174985-12.899798-0.446162-20.391423-0.805342l-10.378371-0.49835 1.848091 10.224876c0.698918 3.872191 1.313925 7.907087 1.824555 11.992125 0.496304 3.967358 0.747014 9.261944 0.747014 15.738449L459.536115 626.348328c0 5.248537-0.095167 10.213619-0.283456 14.848173-5.609765-6.426363-9.686616-14.317077-12.124132-23.464411l-3.955078-14.846126-10.209526 11.481496c-11.416004 12.837376-21.856797 23.998577-31.032784 33.174564-4.377704 4.378727-8.696056 8.53335-12.927427 12.444427L389.003711 501.971599c0-11.842723 0.795109-23.945365 2.363837-35.973306 1.505283-11.536754 3.53757-20.082384 6.040576-25.39539l6.222725-13.20986-14.528901 1.463328c-6.943133 0.698918-16.167215 1.325182-27.415396 1.860371-11.165294 0.532119-22.585391 0.801249-33.945113 0.801249-15.994276 0-28.563546-0.267083-37.358863-0.796132-8.905834-0.534166-16.386202-1.156336-22.233374-1.850138l-9.239432-1.095961 0 58.114582 10.535961-3.00852c9.612938-2.745531 20.454868-4.841262 32.220842-6.227842 10.149151-1.189082 23.025413-1.879813 38.359656-2.058892l0 186.362478c0 12.403494-1.685385 23.922852-5.008061 34.231639-3.164063 9.794064-7.891737 17.729803-14.053064 23.589254l-8.083096 7.68503 9.705036 5.497201c9.866718 5.588275 18.73162 13.121855 26.348088 22.38994l6.684236 8.133238 6.315846-8.422833c3.302209-4.40431 6.102998-8.079002 8.569166-11.247158 2.396583-3.087315 5.004991-6.217608 7.761778-9.31311 2.868327-3.228531 5.922896-6.644327 9.159614-10.235109 3.053546-3.39533 6.873548-7.214309 11.355629-11.349489l59.614749-57.482179c-0.51063 5.955642-1.197268 12.527314-2.045589 19.599384l-1.110287 9.250688 53.81465 0 0-42.720987 83.509972 0 0 34.609239L646.220934 675.172313zM509.056971 600.777953l0-81.346703 83.509972 0 0 81.346703L509.056971 600.777953zM705.169511 296.332079c-9.907651 0.36225-20.348444 0.725524-31.336706 1.079588-10.761088 0.356111-22.86066 0.536212-35.957957 0.536212L542.970362 297.947879c-14.009062 0-26.654057-0.180102-37.592177-0.537236-10.82044-0.342808-21.306258-0.795109-31.16786-1.342578-9.94449-0.552585-19.716041-1.186012-29.043477-1.882883l-8.882298-0.664126 0 52.766784 14.038738-0.735757c11.514241-0.603751 21.807678-1.143033 30.886451-1.611707 10.140964-0.524956 19.699668-0.88516 28.402887-1.072425 8.668427-0.178055 16.60826-0.356111 23.827685-0.541329 7.214309-0.178055 14.604626-0.268106 21.967314-0.268106l135.578862 0L690.986487 696.107111c0 5.158487-0.574075 9.43386-1.701758 12.687974-0.234337 0.673335-0.948605 2.720971-5.358031 4.548596-2.37407 0.982374-7.05672 2.152013-15.876596 2.153036-1.263783 0-2.573615-0.024559-3.926426-0.071631-9.886161-0.348947-23.636327-1.952468-40.869826-4.76349l-18.391882-3.001357 10.120498 15.648398c3.345188 5.17179 5.398964 9.79304 6.102998 13.721513 0.851391 4.796236 1.638313 11.113105 2.341325 18.774599l0.687662 7.512091 7.543813 0c22.586414 0 41.056068-0.934279 54.901401-2.775206 14.877849-1.990331 26.16287-5.586229 34.506908-10.999518 8.862855-5.767354 14.83487-13.675464 17.751293-23.5166 2.556219-8.668427 3.852748-19.643386 3.852748-32.620955L742.670613 409.500535c0-15.595186 0.088004-28.55229 0.26913-39.617299 0.185218-11.836583 0.36225-21.458731 0.539282-29.424146 0.171915-8.023744 0.525979-15.117302 1.051959-21.081131 0.527003-5.960759 1.13587-11.021008 1.810229-15.040555l1.721201-10.252505-10.377348 0.633427C725.917044 295.437709 714.978924 295.980062 705.169511 296.332079zM511.99693 63.875796c-247.215428 0-447.629947 200.413496-447.629947 447.629947 0 247.221568 200.413496 447.635063 447.629947 447.635063 247.221568 0 447.635063-200.413496 447.635063-447.635063C959.631994 264.289292 759.218498 63.875796 511.99693 63.875796zM785.110191 784.617981c-35.491329 35.489282-76.801177 63.348794-122.788557 82.800821-47.578622 20.12434-98.155531 30.329772-150.323681 30.329772-52.16815 0-102.739942-10.205433-150.320611-30.329772-45.987381-19.452028-87.298252-47.311539-122.792651-82.800821-35.491329-35.490306-63.349817-76.800153-82.795705-122.787534-20.125363-47.579645-30.329772-98.156554-30.329772-150.324704 0-52.16815 10.204409-102.745059 30.329772-150.320611 19.445888-45.987381 47.305399-87.296205 82.795705-122.792651 35.494399-35.489282 76.80527-63.348794 122.792651-82.800821 47.579645-20.12127 98.152461-30.324656 150.320611-30.324656 52.16815 0 102.745059 10.204409 150.323681 30.324656 45.987381 19.452028 87.298252 47.311539 122.788557 82.800821 35.494399 35.490306 63.348794 76.80527 82.800821 122.792651 20.12434 47.574528 30.328749 98.152461 30.328749 150.320611 0 52.16815-10.204409 102.745059-30.328749 150.324704C848.457962 707.812711 820.599474 749.127675 785.110191 784.617981zM712.23237 332.817038", paint);
        lrcSvgGlyph.setSize(22.0);
        JFXButton lrcButton = new JFXButton("", lrcSvgGlyph);
        lrcButton.getStyleClass().add("shadowLabel");

        LrcStage lrcStage = mainApp.getLrcStage();
        lrcButton.setOnAction(event -> {
            if (!lrcStage.isShowing()) {
                lrcStage.show();
            } else {
                lrcStage.hide();
            }
        });

        //5.歌单列表展开
        SVGGlyph unFoldSVGGlyph = new SVGGlyph("M512 1018.587A506.587 506.587 0 1 1 512 5.413a506.587 506.587 0 1 1 0 1013.174z m0-59.611a446.976 446.976 0 1 0 0-893.879 446.976 446.976 0 1 0 0 893.806z m5.925-352.183c87.99-87.26 116.37-114.907 204.36-202.24 8.997-8.923 19.164-13.75 32.184-10.971a32.183 32.183 0 0 1 18.212 52.077c-1.755 2.048-3.657 3.877-5.632 5.852-96.036 95.305-132.608 130.926-228.644 226.304-17.482 17.262-35.328 17.262-52.81 0-96.548-95.817-133.485-132.023-230.18-227.694-9.509-9.508-13.824-20.334-10.46-33.353a32.475 32.475 0 0 1 51.786-16.75c2.194 1.756 4.315 3.95 6.363 5.925 87.479 86.82 115.42 114.103 202.898 200.85 1.829 1.902 3.292 4.242 5.925 7.9 2.78-3.584 4.169-5.998 5.998-7.9z", paint);
        unFoldSVGGlyph.setSize(22.0);
        JFXButton unFoldButton = new JFXButton("", unFoldSVGGlyph);

        unFoldButton.getStyleClass().add("shadowLabel");
        JFXPopup jfxPopup = new JFXPopup();

        jfxPopup.setPopupContent(mainApp.getSimplifyTableView());

        unFoldButton.setOnAction(event -> {
            jfxPopup.show(unFoldButton, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.RIGHT, 10, 40);
        });
        vBox.getChildren().addAll(closeButton, lrcButton, unFoldButton);
        VBox.setMargin(lrcButton, new Insets(10, 0, 0, 0));
        return vBox;
    }

    public Node initCenterPane() {
        VBox vBox = new VBox(5);
        HBox hBox = new HBox(2);
        double width = 20.0;

        Paint paint = WHITE;

        Region preMusicRegion = playSvg.getPreRegion(width, paint);
        JFXButton b1 = new JFXButton("", preMusicRegion);
        b1.setOnAction(event -> mainApp.preMusic());
        b1.getStyleClass().add("shadowLabel");

        Region playOrPauseRegion = playSvg.getPlayOrPauseRegion(width, paint);

        JFXButton b2 = new JFXButton("", playOrPauseRegion);
        b2.setOnAction(event -> mainApp.playOrPauseMusic());
        b2.getStyleClass().add("shadowLabel");

        Region nextMusicSvgRegion = playSvg.getNextRegion(width, paint);
        JFXButton b3 = new JFXButton("", nextMusicSvgRegion);
        b3.setOnAction(event -> mainApp.nextMusic());
        hBox.getChildren().addAll(b1, b2, b3);
        b3.getStyleClass().add("shadowLabel");

        singerLabel = new Label("");
        singerLabel.setTextFill(WHITE);
        singerLabel.setFont(Font.font("Timer New Roman",
                FontWeight.BOLD, FontPosture.ITALIC, 12));
        singerLabel.getStyleClass().add("shadowLabel");
        songNameLabel = new Label("");
        songNameLabel.setTextFill(WHITE);
        songNameLabel.setFont(Font.font("Timer New Roman",
                FontWeight.BOLD, FontPosture.ITALIC, 12));
        songNameLabel.getStyleClass().add("shadowLabel");
        vBox.getChildren().addAll(songNameLabel, singerLabel, hBox);
        vBox.setAlignment(Pos.BOTTOM_LEFT);
        return vBox;
    }

    public void changeSvgPath(PlaySvg.PlayStatus playStatus) {
        playSvg.changeSvgPath(playStatus);
    }

    public void setSongNameAndSingerName(String songName, String singerName) {
        songNameLabel.setText(songName);
        singerLabel.setText(singerName);
    }
}

