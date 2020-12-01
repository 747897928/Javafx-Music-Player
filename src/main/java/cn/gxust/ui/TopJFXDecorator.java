package cn.gxust.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.svg.SVGGlyph;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.pomo.toasterfx.ToastBarToasterService;
import org.pomo.toasterfx.model.ToastParameter;

/**
 * <p>description:上边框  </p>
 * <p>create: 2020/10/8 18:57</p>
 *
 * @author zhaoyijie
 * @version v1.0
 */
public class TopJFXDecorator extends JFXDecorator {

    private final JFXButton simplifyModelButton;

    private final JFXButton aboutButton;

    private final Label searchTiplabel;

    public TopJFXDecorator(Stage primaryStage, Node contentNode, MainApp mainApp, Image logoImage, String title, Color titleColor) {
        super(primaryStage, contentNode, false, true, true);
        //左侧的Logo
        ImageView icoImageView = new ImageView(logoImage);
        icoImageView.setFitHeight(24);//设置图片的高度：24像素
        icoImageView.setPreserveRatio(true);//根据图片设置的高度，保持宽高比；
        //标题
        Label labTitle = new Label(title);
        //labTitle.setPrefWidth(120);
        labTitle.setTextFill(titleColor);
        labTitle.setFont(new Font("FangSong", 14));
        HBox hBox = new HBox(5);
        hBox.setAlignment(Pos.CENTER_LEFT);//左对齐
        hBox.setPrefHeight(50);
        hBox.setMaxHeight(50);
        HBox.setMargin(labTitle, new Insets(0, 10, 0, 10));
        hBox.getChildren().addAll(icoImageView, labTitle);

        //关于
        SVGGlyph icoSvg = new SVGGlyph("M1217.32934 1021.586a33.846 33.846 0 0 1-22-8.462l-56.41-50.205a329.435 329.435 0 0 0-431.537 0l-56.41 50.205a33.846 33.846 0 0 1-44.564 0l-56.41-50.205a329.435 329.435 0 0 0-431.536 0l-62.051 52.462A33.846 33.846 0 0 1 0.00034 989.996V307.435a33.846 33.846 0 0 1 67.692 0v605.843a397.69 397.69 0 0 1 520.664 0l35.539 31.026 35.538-31.026a397.69 397.69 0 0 1 520.664 0V307.435a33.846 33.846 0 1 1 67.692 0V987.74a33.846 33.846 0 0 1-33.846 33.846zM625.58834 811.74a39.487 39.487 0 0 1-25.949-9.59l-43.436-37.794a232.973 232.973 0 0 0-305.178 0L207.59034 802.15a39.487 39.487 0 0 1-66-29.898V247.076A247.64 247.64 0 0 1 389.23034 0H418.00034a247.64 247.64 0 0 1 247.64 247.076v524.613a39.487 39.487 0 0 1-23.128 36.103 38.923 38.923 0 0 1-16.923 3.948zM403.33234 627.844a311.383 311.383 0 0 1 182.205 56.41V247.076a169.23 169.23 0 0 0-169.23-169.23H389.23034A169.23 169.23 0 0 0 220.00034 247.076v438.87a311.383 311.383 0 0 1 183.332-58.102zM625.58834 811.74a38.923 38.923 0 0 1-16.36-3.384 39.487 39.487 0 0 1-23.127-36.103V247.076A247.64 247.64 0 0 1 833.17734 0h28.769a247.64 247.64 0 0 1 247.076 247.076v524.613a39.487 39.487 0 0 1-66 29.898l-43.436-37.795a232.973 232.973 0 0 0-305.178 0l-43.436 37.795a39.487 39.487 0 0 1-25.384 10.153zM833.17734 78.41a169.23 169.23 0 0 0-169.23 169.23v438.87a313.076 313.076 0 0 1 364.973 0V247.076a169.23 169.23 0 0 0-169.23-169.23z", Paint.valueOf("#ffffff"));
        icoSvg.setSize(16.0);

        //设置标签
        aboutButton = new JFXButton("", icoSvg);

        SVGGlyph v0 = new SVGGlyph("M352 64a32 32 0 0 1 32 32v256a32 32 0 0 1-32 32H96a32 32 0 0 1-32-32V96a32 32 0 0 1 32-32h256m0-64H96a96 96 0 0 0-96 96v256a96 96 0 0 0 96 96h256a96 96 0 0 0 96-96V96a96 96 0 0 0-96-96z m576 64a32 32 0 0 1 32 32v256a32 32 0 0 1-32 32h-256a32 32 0 0 1-32-32V96a32 32 0 0 1 32-32h256m0-64h-256a96 96 0 0 0-96 96v256a96 96 0 0 0 96 96h256a96 96 0 0 0 96-96V96a96 96 0 0 0-96-96zM352 640a32 32 0 0 1 32 32v256a32 32 0 0 1-32 32H96a32 32 0 0 1-32-32v-256a32 32 0 0 1 32-32h256m0-64H96a96 96 0 0 0-96 96v256a96 96 0 0 0 96 96h256a96 96 0 0 0 96-96v-256a96 96 0 0 0-96-96z m576 64a32 32 0 0 1 32 32v256a32 32 0 0 1-32 32h-256a32 32 0 0 1-32-32v-256a32 32 0 0 1 32-32h256m0-64h-256a96 96 0 0 0-96 96v256a96 96 0 0 0 96 96h256a96 96 0 0 0 96-96v-256a96 96 0 0 0-96-96z", Paint.valueOf("#ffffff"));

        v0.setSize(16.0);
        v0.setFill(Color.WHITE);
        simplifyModelButton = new JFXButton("", v0);

        HBox rigthHbox = new HBox(5);
        rigthHbox.setAlignment(Pos.CENTER_RIGHT);//右对齐
        rigthHbox.setPrefHeight(50);
        rigthHbox.setMaxHeight(50);
        HBox.setMargin(simplifyModelButton, new Insets(0, 5, 0, 5));
        rigthHbox.getChildren().addAll(aboutButton, simplifyModelButton);

        ToastBarToasterService service = mainApp.getService();
        ToastParameter customAudioParameter = mainApp.getCustomAudioParameter();
        final TextField searchTextField = new TextField();
        searchTextField.setPromptText("搜索单曲音乐");
        SVGGlyph searchSVGGlyph = new SVGGlyph("M160.021999 409.536004C160.021999 254.345703 286.286107 128.081595 441.476408 128.081595 596.730704 128.081595 722.994813 254.345703 722.994813 409.536004 722.994813 564.726305 596.730704 690.990413 441.476408 690.990413 286.286107 690.990413 160.021999 564.726305 160.021999 409.536004M973.219174 864.867546 766.320105 657.904481C819.180801 588.916793 850.986813 502.970164 850.986813 409.536004 850.986813 183.758115 667.318293 0.089594 441.476408 0.089594 215.698519 0.089594 32.029998 183.758115 32.029998 409.536004 32.029998 635.313893 215.698519 818.982414 441.476408 818.982414 527.743016 818.982414 607.738016 792.104093 673.781889 746.410949L882.728829 955.35789C895.208049 967.83711 911.591026 974.108718 927.974002 974.108718 944.356978 974.108718 960.739954 967.83711 973.219174 955.35789 998.24161 930.335454 998.24161 889.825986 973.219174 864.867546");
        searchSVGGlyph.setSize(16.0);
        searchSVGGlyph.setFill(Color.WHITE);
        JFXButton searchButton = new JFXButton("", searchSVGGlyph);
        searchTiplabel = new Label("");
        searchButton.setOnAction(event -> {
            String text = searchTextField.getText();
            if (text.trim().length() == 0) {
                //Log4jUtils.logger.error("搜索信息不能为空");
                service.fail("操作错误", "搜索信息不能为空！", customAudioParameter);
                return;
            }
            if (searchTiplabel.getText().equals("searching....")) {
                service.fail("操作失败！", "正在搜索，期间不允许其他操作！", customAudioParameter);
                return;
            }
            mainApp.searchSingleMusic(text);
        });

        searchTiplabel.setFont(Font.font("Timer New Roman",
                FontWeight.BOLD, FontPosture.ITALIC, 12));
        searchTiplabel.setAlignment(Pos.CENTER);//字体居中
        searchTiplabel.setTextFill(Color.WHITE);
        HBox hBox1 = new HBox(5);//HBox将孩子放在一排水平排列中。 如果hbox有一个边框和/或填充集合，那么这些内容将被放置在这些插入内。
        hBox1.setAlignment(Pos.CENTER_LEFT);//左对齐
        hBox1.setMinWidth(300.0);
        hBox1.setPrefHeight(50);
        hBox1.setMaxHeight(50);
        hBox1.getChildren().addAll(searchTextField, searchButton, searchTiplabel);
        HBox.setMargin(searchTextField, new Insets(0, 10, 0, 10));
        HBox.setMargin(searchTiplabel, new Insets(0, 20, 0, 10));

        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(hBox);
        borderPane.setCenter(hBox1);
        borderPane.setRight(rigthHbox);
        borderPane.setPadding(new Insets(2, 5, 2, 5));
        borderPane.prefWidthProperty().bind(this.widthProperty());
        this.getStylesheets().add(this.getClass().getResource("/css/jfoenix-components.css").toExternalForm());
        this.setGraphic(borderPane);
        this.setCustomMaximize(true);
    }

    public JFXButton getSimplifyModelButton() {
        return simplifyModelButton;
    }

    public JFXButton getAboutButton() {
        return aboutButton;
    }

    public Label getSearchTiplabel() {
        return searchTiplabel;
    }
}
