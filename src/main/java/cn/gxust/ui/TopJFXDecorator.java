package cn.gxust.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.svg.SVGGlyph;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.BoundingBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.pomo.toasterfx.ToastBarToasterService;
import org.pomo.toasterfx.model.ToastParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>description:上边框  </p>
 * <p>create: 2020/10/8 18:57</p>
 *
 * @author zhaoyijie
 * @version v1.0
 */
public class TopJFXDecorator extends VBox {

    private JFXButton simplifyModelButton;

    private JFXButton aboutButton;

    private Label searchTiplabel;

    private Stage primaryStage;

    private double xOffset = 0;
    private double yOffset = 0;
    private double newX, newY, initX, initY, initWidth = -1, initHeight = -1,
            initStageX = -1, initStageY = -1;

    private boolean allowMove = false;
    private boolean isDragging = false;
    private Timeline windowDecoratorAnimation;
    private StackPane contentPlaceHolder = new StackPane();
    private HBox buttonsContainer;

    private ObjectProperty<Runnable> onCloseButtonAction = new SimpleObjectProperty<>(() ->
            primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST)));

    private BooleanProperty customMaximize = new SimpleBooleanProperty(false);
    private boolean maximized = false;
    private BoundingBox originalBox;
    private BoundingBox maximizedBox;

    protected JFXButton btnMax;
    protected JFXButton btnClose;
    protected JFXButton btnMin;

    protected StringProperty title = new SimpleStringProperty();
    protected Text text;
    protected Node graphic;
    protected HBox graphicContainer;


    public TopJFXDecorator(Stage primaryStage, Node contentNode, MainApp mainApp, Image logoImage, String title, Color titleColor) {
        this(primaryStage, contentNode, true, true);
        //左侧的Logo
        ImageView icoImageView = new ImageView(logoImage);
        icoImageView.setFitHeight(24);//设置图片的高度：24像素
        icoImageView.setPreserveRatio(true);//根据图片设置的高度，保持宽高比；
        //标题
        Label labTitle = new Label(title);
        labTitle.setTextFill(titleColor);
        labTitle.setFont(new Font("FangSong", 14));

        //关于
        SVGGlyph icoSvg = new SVGGlyph("M1217.32934 1021.586a33.846 33.846 0 0 1-22-8.462l-56.41-50.205a329.435 329.435 0 0 0-431.537 0l-56.41 50.205a33.846 33.846 0 0 1-44.564 0l-56.41-50.205a329.435 329.435 0 0 0-431.536 0l-62.051 52.462A33.846 33.846 0 0 1 0.00034 989.996V307.435a33.846 33.846 0 0 1 67.692 0v605.843a397.69 397.69 0 0 1 520.664 0l35.539 31.026 35.538-31.026a397.69 397.69 0 0 1 520.664 0V307.435a33.846 33.846 0 1 1 67.692 0V987.74a33.846 33.846 0 0 1-33.846 33.846zM625.58834 811.74a39.487 39.487 0 0 1-25.949-9.59l-43.436-37.794a232.973 232.973 0 0 0-305.178 0L207.59034 802.15a39.487 39.487 0 0 1-66-29.898V247.076A247.64 247.64 0 0 1 389.23034 0H418.00034a247.64 247.64 0 0 1 247.64 247.076v524.613a39.487 39.487 0 0 1-23.128 36.103 38.923 38.923 0 0 1-16.923 3.948zM403.33234 627.844a311.383 311.383 0 0 1 182.205 56.41V247.076a169.23 169.23 0 0 0-169.23-169.23H389.23034A169.23 169.23 0 0 0 220.00034 247.076v438.87a311.383 311.383 0 0 1 183.332-58.102zM625.58834 811.74a38.923 38.923 0 0 1-16.36-3.384 39.487 39.487 0 0 1-23.127-36.103V247.076A247.64 247.64 0 0 1 833.17734 0h28.769a247.64 247.64 0 0 1 247.076 247.076v524.613a39.487 39.487 0 0 1-66 29.898l-43.436-37.795a232.973 232.973 0 0 0-305.178 0l-43.436 37.795a39.487 39.487 0 0 1-25.384 10.153zM833.17734 78.41a169.23 169.23 0 0 0-169.23 169.23v438.87a313.076 313.076 0 0 1 364.973 0V247.076a169.23 169.23 0 0 0-169.23-169.23z", Paint.valueOf("#ffffff"));
        icoSvg.setSize(16.0);

        //设置标签
        aboutButton = new JFXButton("", icoSvg);

        SVGGlyph v0 = new SVGGlyph("M352 64a32 32 0 0 1 32 32v256a32 32 0 0 1-32 32H96a32 32 0 0 1-32-32V96a32 32 0 0 1 32-32h256m0-64H96a96 96 0 0 0-96 96v256a96 96 0 0 0 96 96h256a96 96 0 0 0 96-96V96a96 96 0 0 0-96-96z m576 64a32 32 0 0 1 32 32v256a32 32 0 0 1-32 32h-256a32 32 0 0 1-32-32V96a32 32 0 0 1 32-32h256m0-64h-256a96 96 0 0 0-96 96v256a96 96 0 0 0 96 96h256a96 96 0 0 0 96-96V96a96 96 0 0 0-96-96zM352 640a32 32 0 0 1 32 32v256a32 32 0 0 1-32 32H96a32 32 0 0 1-32-32v-256a32 32 0 0 1 32-32h256m0-64H96a96 96 0 0 0-96 96v256a96 96 0 0 0 96 96h256a96 96 0 0 0 96-96v-256a96 96 0 0 0-96-96z m576 64a32 32 0 0 1 32 32v256a32 32 0 0 1-32 32h-256a32 32 0 0 1-32-32v-256a32 32 0 0 1 32-32h256m0-64h-256a96 96 0 0 0-96 96v256a96 96 0 0 0 96 96h256a96 96 0 0 0 96-96v-256a96 96 0 0 0-96-96z", Paint.valueOf("#ffffff"));

        v0.setSize(16.0);
        v0.setFill(Color.WHITE);
        simplifyModelButton = new JFXButton("", v0);

        ToastBarToasterService service = mainApp.getService();
        ToastParameter customAudioParameter = mainApp.getCustomAudioParameter();
        TextField searchTextField = new TextField();
        searchTextField.setPromptText("搜索单曲音乐");
        searchTextField.setStyle("-fx-background-radius: 15 15 15 15;");
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

        setTitle("");
        buttonsContainer.getChildren().add(1, aboutButton);
        buttonsContainer.getChildren().add(2, simplifyModelButton);
        this.getStylesheets().add(this.getClass().getResource("/css/jfoenix-components.css").toExternalForm());
        graphicContainer.getChildren().addAll(icoImageView, labTitle,searchTextField, searchButton, searchTiplabel);
        HBox.setMargin(searchTextField, new Insets(0, 10, 0, 10));
        HBox.setMargin(searchTiplabel, new Insets(0, 20, 0, 10));
        HBox.setMargin(labTitle, new Insets(0, 10, 0, 10));
        this.setCustomMaximize(true);
    }

    /**
     * Create a window decorator for the specified node with the options:
     * - full screen
     * - maximize
     * - minimize
     *
     * @param stage      the primary stage used by the application
     * @param node       the node to be decorated
     * @param max        indicates whether to show maximize option or not
     * @param min        indicates whether to show minimize option or not
     */
    public TopJFXDecorator(Stage stage, Node node, boolean max, boolean min) {
        primaryStage = stage;
        // Note that setting the style to TRANSPARENT is causing performance
        // degradation, as an alternative we set it to UNDECORATED instead.
        primaryStage.initStyle(StageStyle.UNDECORATED);

        setPickOnBounds(false);
        getStyleClass().add("jfx-decorator");

        initializeButtons();
        initializeContainers(node, max, min);

        primaryStage.fullScreenProperty().addListener((o, oldVal, newVal) -> {
            if (newVal) {
                // remove border
                contentPlaceHolder.getStyleClass().remove("resize-border");
                /*
                 *  note the border property MUST NOT be bound to another property
                 *  when going full screen mode, thus the binding will be lost if exisited
                 */
                contentPlaceHolder.borderProperty().unbind();
                contentPlaceHolder.setBorder(Border.EMPTY);
                if (windowDecoratorAnimation != null) {
                    windowDecoratorAnimation.stop();
                }
                windowDecoratorAnimation = new Timeline(new KeyFrame(Duration.millis(320),
                        new KeyValue(this.translateYProperty(),
                                -buttonsContainer.getHeight(),
                                Interpolator.EASE_BOTH)));
                windowDecoratorAnimation.setOnFinished((finish) -> {
                    this.getChildren().remove(buttonsContainer);
                    this.setTranslateY(0);
                });
                windowDecoratorAnimation.play();
            } else {
                // add border
                if (windowDecoratorAnimation != null) {
                    if (windowDecoratorAnimation.getStatus() == Animation.Status.RUNNING) {
                        windowDecoratorAnimation.stop();
                    } else {
                        this.getChildren().add(0, buttonsContainer);
                    }
                }
                this.setTranslateY(-buttonsContainer.getHeight());
                windowDecoratorAnimation = new Timeline(new KeyFrame(Duration.millis(320),
                        new KeyValue(this.translateYProperty(),
                                0,
                                Interpolator.EASE_BOTH)));
                windowDecoratorAnimation.setOnFinished((finish) -> {
                    contentPlaceHolder.setBorder(new Border(new BorderStroke(Color.BLACK,
                            BorderStrokeStyle.SOLID,
                            CornerRadii.EMPTY,
                            new BorderWidths(0, 4, 4, 4))));
                    contentPlaceHolder.getStyleClass().add("resize-border");
                });
                windowDecoratorAnimation.play();
            }
        });

        contentPlaceHolder.addEventHandler(MouseEvent.MOUSE_PRESSED, (mouseEvent) ->
                updateInitMouseValues(mouseEvent));
        buttonsContainer.addEventHandler(MouseEvent.MOUSE_PRESSED, (mouseEvent) ->
                updateInitMouseValues(mouseEvent));

        // show the drag cursor on the borders
        addEventFilter(MouseEvent.MOUSE_MOVED, (mouseEvent) -> showDragCursorOnBorders(mouseEvent));


        // handle drag events on the decorator pane
        addEventFilter(MouseEvent.MOUSE_RELEASED, (mouseEvent) -> isDragging = false);
        this.setOnMouseDragged((mouseEvent) -> handleDragEventOnDecoratorPane(mouseEvent));
    }

    private void initializeButtons() {
        SVGGlyph minus = new SVGGlyph(0,
                "MINUS",
                "M804.571 420.571v109.714q0 22.857-16 38.857t-38.857 16h-694.857q-22.857 0-38.857-16t-16-38.857v-109.714q0-22.857 16-38.857t38.857-16h694.857q22.857 0 38.857 16t16 38.857z",
                Color.WHITE);
        minus.setSize(12, 2);
        minus.setTranslateY(4);
        SVGGlyph resizeMax = new SVGGlyph(0,
                "RESIZE_MAX",
                "M726 810v-596h-428v596h428zM726 44q34 0 59 25t25 59v768q0 34-25 60t-59 26h-428q-34 0-59-26t-25-60v-768q0-34 25-60t59-26z",
                Color.WHITE);
        resizeMax.setSize(12, 12);
        SVGGlyph resizeMin = new SVGGlyph(0,
                "RESIZE_MIN",
                "M80.842 943.158v-377.264h565.894v377.264h-565.894zM0 404.21v619.79h727.578v-619.79h-727.578zM377.264 161.684h565.894v377.264h-134.736v80.842h215.578v-619.79h-727.578v323.37h80.842v-161.686z",
                Color.WHITE);
        resizeMin.setSize(12, 12);
        SVGGlyph close = new SVGGlyph(0,
                "CLOSE",
                "M810 274l-238 238 238 238-60 60-238-238-238 238-60-60 238-238-238-238 60-60 238 238 238-238z",
                Color.WHITE);
        close.setSize(12, 12);

        btnClose = new JFXButton();
        btnClose.getStyleClass().add("jfx-decorator-button");
        btnClose.setCursor(Cursor.HAND);
        btnClose.setOnAction((action) -> onCloseButtonAction.get().run());
        btnClose.setGraphic(close);
        btnClose.setRipplerFill(Color.WHITE);

        btnMin = new JFXButton();
        btnMin.getStyleClass().add("jfx-decorator-button");
        btnMin.setCursor(Cursor.HAND);
        btnMin.setOnAction((action) -> primaryStage.setIconified(true));
        btnMin.setGraphic(minus);
        btnMin.setRipplerFill(Color.WHITE);

        btnMax = new JFXButton();
        btnMax.getStyleClass().add("jfx-decorator-button");
        btnMax.setCursor(Cursor.HAND);
        btnMax.setRipplerFill(Color.WHITE);
        btnMax.setOnAction((action) -> maximize(resizeMin, resizeMax));
        btnMax.setGraphic(resizeMax);
    }

    private void maximize(SVGGlyph resizeMin, SVGGlyph resizeMax) {
        if (!isCustomMaximize()) {
            primaryStage.setMaximized(!primaryStage.isMaximized());
            maximized = primaryStage.isMaximized();
            if (primaryStage.isMaximized()) {
                btnMax.setGraphic(resizeMin);
                btnMax.setTooltip(new Tooltip("Restore Down"));
            } else {
                btnMax.setGraphic(resizeMax);
                btnMax.setTooltip(new Tooltip("Maximize"));
            }
        } else {
            if (!maximized) {
                // store original bounds
                originalBox = new BoundingBox(primaryStage.getX(), primaryStage.getY(), primaryStage.getWidth(), primaryStage.getHeight());
                // get the max stage bounds
                Screen screen = Screen.getScreensForRectangle(primaryStage.getX(),
                        primaryStage.getY(),
                        primaryStage.getWidth(),
                        primaryStage.getHeight()).get(0);
                Rectangle2D bounds = screen.getVisualBounds();
                maximizedBox = new BoundingBox(bounds.getMinX(),
                        bounds.getMinY(),
                        bounds.getWidth(),
                        bounds.getHeight());
                // maximized the stage
                primaryStage.setX(maximizedBox.getMinX());
                primaryStage.setY(maximizedBox.getMinY());
                primaryStage.setWidth(maximizedBox.getWidth());
                primaryStage.setHeight(maximizedBox.getHeight());
                btnMax.setGraphic(resizeMin);
                btnMax.setTooltip(new Tooltip("Restore Down"));
            } else {
                // restore stage to its original size
                primaryStage.setX(originalBox.getMinX());
                primaryStage.setY(originalBox.getMinY());
                primaryStage.setWidth(originalBox.getWidth());
                primaryStage.setHeight(originalBox.getHeight());
                originalBox = null;
                btnMax.setGraphic(resizeMax);
                btnMax.setTooltip(new Tooltip("Maximize"));
            }
            maximized = !maximized;
        }
    }

    private void initializeContainers(Node node, boolean max, boolean min) {
        buttonsContainer = new HBox();
        buttonsContainer.getStyleClass().add("jfx-decorator-buttons-container");
        buttonsContainer.setBackground(new Background(new BackgroundFill(Color.BLACK,
                CornerRadii.EMPTY,
                Insets.EMPTY)));
        // BINDING
        buttonsContainer.setPadding(new Insets(4));
        buttonsContainer.setAlignment(Pos.CENTER_RIGHT);

        // customize decorator buttons
        List<JFXButton> btns = new ArrayList<>();
        if (min) {
            btns.add(btnMin);
        }
        if (max) {
            btns.add(btnMax);
            // maximize/restore the window on header double click
            buttonsContainer.addEventHandler(MouseEvent.MOUSE_CLICKED, (mouseEvent) -> {
                if (mouseEvent.getClickCount() == 2) {
                    btnMax.fire();
                }
            });
        }
        btns.add(btnClose);

        text = new Text();
        text.getStyleClass().addAll("jfx-decorator-text", "title", "jfx-decorator-title");
        text.setFill(Color.WHITE);
        text.textProperty().bind(title); //binds the Text's text to title
        title.bind(primaryStage.titleProperty()); //binds title to the primaryStage's title

        graphicContainer = new HBox();
        graphicContainer.setPickOnBounds(false);
        graphicContainer.setAlignment(Pos.CENTER_LEFT);
        graphicContainer.getChildren().setAll(text);

        HBox graphicTextContainer = new HBox(graphicContainer, text);
        graphicTextContainer.getStyleClass().add("jfx-decorator-title-container");
        graphicTextContainer.setAlignment(Pos.CENTER_LEFT);
        graphicTextContainer.setPickOnBounds(false);
        HBox.setHgrow(graphicTextContainer, Priority.ALWAYS);
        HBox.setMargin(graphicContainer, new Insets(0, 8, 0, 8));

        buttonsContainer.getChildren().setAll(graphicTextContainer);
        buttonsContainer.getChildren().addAll(btns);
        buttonsContainer.addEventHandler(MouseEvent.MOUSE_ENTERED, (enter) -> allowMove = true);
        buttonsContainer.addEventHandler(MouseEvent.MOUSE_EXITED, (enter) -> {
            if (!isDragging) {
                allowMove = false;
            }
        });
        buttonsContainer.setMinWidth(180);
        contentPlaceHolder.getStyleClass().add("jfx-decorator-content-container");
        contentPlaceHolder.setMinSize(0, 0);
        StackPane clippedContainer = new StackPane(node);
        contentPlaceHolder.getChildren().add(clippedContainer);
        ((Region) node).setMinSize(0, 0);
        VBox.setVgrow(contentPlaceHolder, Priority.ALWAYS);
        contentPlaceHolder.getStyleClass().add("resize-border");
        contentPlaceHolder.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(0, 4, 4, 4))));
        // BINDING

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(clippedContainer.widthProperty());
        clip.heightProperty().bind(clippedContainer.heightProperty());
        clippedContainer.setClip(clip);
        this.getChildren().addAll(buttonsContainer, contentPlaceHolder);
    }

    private void showDragCursorOnBorders(MouseEvent mouseEvent) {
        if (primaryStage.isMaximized() || primaryStage.isFullScreen() || maximized) {
            this.setCursor(Cursor.DEFAULT);
            return; // maximized mode does not support resize
        }
        if (!primaryStage.isResizable()) {
            return;
        }
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        if (contentPlaceHolder.getBorder() != null && contentPlaceHolder.getBorder().getStrokes().size() > 0) {
            double borderWidth = contentPlaceHolder.snappedLeftInset();
            if (isRightEdge(x)) {
                if (y < borderWidth) {
                    this.setCursor(Cursor.NE_RESIZE);
                } else if (y > this.getHeight() - borderWidth) {
                    this.setCursor(Cursor.SE_RESIZE);
                } else {
                    this.setCursor(Cursor.E_RESIZE);
                }
            } else if (isLeftEdge(x)) {
                if (y < borderWidth) {
                    this.setCursor(Cursor.NW_RESIZE);
                } else if (y > this.getHeight() - borderWidth) {
                    this.setCursor(Cursor.SW_RESIZE);
                } else {
                    this.setCursor(Cursor.W_RESIZE);
                }
            } else if (isTopEdge(y)) {
                this.setCursor(Cursor.N_RESIZE);
            } else if (isBottomEdge(y)) {
                this.setCursor(Cursor.S_RESIZE);
            } else {
                this.setCursor(Cursor.DEFAULT);
            }
        }
    }

    private void handleDragEventOnDecoratorPane(MouseEvent mouseEvent) {
        isDragging = true;
        if (!mouseEvent.isPrimaryButtonDown() || (xOffset == -1 && yOffset == -1)) {
            return;
        }
        /*
         * Long press generates drag event!
         */
        if (primaryStage.isFullScreen() || mouseEvent.isStillSincePress() || primaryStage.isMaximized() || maximized) {
            return;
        }

        newX = mouseEvent.getScreenX();
        newY = mouseEvent.getScreenY();


        double deltax = newX - initX;
        double deltay = newY - initY;
        Cursor cursor = this.getCursor();

        if (Cursor.E_RESIZE.equals(cursor)) {
            setStageWidth(initWidth + deltax);
            mouseEvent.consume();
        } else if (Cursor.NE_RESIZE.equals(cursor)) {
            if (setStageHeight(initHeight - deltay)) {
                primaryStage.setY(initStageY + deltay);
            }
            setStageWidth(initWidth + deltax);
            mouseEvent.consume();
        } else if (Cursor.SE_RESIZE.equals(cursor)) {
            setStageWidth(initWidth + deltax);
            setStageHeight(initHeight + deltay);
            mouseEvent.consume();
        } else if (Cursor.S_RESIZE.equals(cursor)) {
            setStageHeight(initHeight + deltay);
            mouseEvent.consume();
        } else if (Cursor.W_RESIZE.equals(cursor)) {
            if (setStageWidth(initWidth - deltax)) {
                primaryStage.setX(initStageX + deltax);
            }
            mouseEvent.consume();
        } else if (Cursor.SW_RESIZE.equals(cursor)) {
            if (setStageWidth(initWidth - deltax)) {
                primaryStage.setX(initStageX + deltax);
            }
            setStageHeight(initHeight + deltay);
            mouseEvent.consume();
        } else if (Cursor.NW_RESIZE.equals(cursor)) {
            if (setStageWidth(initWidth - deltax)) {
                primaryStage.setX(initStageX + deltax);
            }
            if (setStageHeight(initHeight - deltay)) {
                primaryStage.setY(initStageY + deltay);
            }
            mouseEvent.consume();
        } else if (Cursor.N_RESIZE.equals(cursor)) {
            if (setStageHeight(initHeight - deltay)) {
                primaryStage.setY(initStageY + deltay);
            }
            mouseEvent.consume();
        } else if (allowMove) {
            primaryStage.setX(mouseEvent.getScreenX() - xOffset);
            primaryStage.setY(mouseEvent.getScreenY() - yOffset);
            mouseEvent.consume();
        }
    }

    private void updateInitMouseValues(MouseEvent mouseEvent) {
        initStageX = primaryStage.getX();
        initStageY = primaryStage.getY();
        initWidth = primaryStage.getWidth();
        initHeight = primaryStage.getHeight();
        initX = mouseEvent.getScreenX();
        initY = mouseEvent.getScreenY();
        xOffset = mouseEvent.getSceneX();
        yOffset = mouseEvent.getSceneY();
    }


    private boolean isRightEdge(double x) {
        final double width = this.getWidth();
        return x < width && x > width - contentPlaceHolder.snappedLeftInset();
    }

    private boolean isTopEdge(double y) {
        return y >= 0 && y < contentPlaceHolder.snappedLeftInset();
    }

    private boolean isBottomEdge(double y) {
        final double height = this.getHeight();
        return y < height && y > height - contentPlaceHolder.snappedLeftInset();
    }

    private boolean isLeftEdge(double x) {
        return x >= 0 && x < contentPlaceHolder.snappedLeftInset();
    }

    boolean setStageWidth(double width) {
        if (width >= primaryStage.getMinWidth() && width >= buttonsContainer.getMinWidth()) {
            primaryStage.setWidth(width);
//            initX = newX;
            return true;
        } else if (width >= primaryStage.getMinWidth() && width <= buttonsContainer.getMinWidth()) {
            width = buttonsContainer.getMinWidth();
            primaryStage.setWidth(width);
        }
        return false;
    }

    boolean setStageHeight(double height) {
        if (height >= primaryStage.getMinHeight() && height >= buttonsContainer.getHeight()) {
            primaryStage.setHeight(height);
//            initY = newY;
            return true;
        } else if (height >= primaryStage.getMinHeight() && height <= buttonsContainer.getHeight()) {
            height = buttonsContainer.getHeight();
            primaryStage.setHeight(height);
        }
        return false;
    }

    /**
     * set a speficed runnable when clicking on the close button
     *
     * @param onCloseButtonAction runnable to be executed
     */
    public void setOnCloseButtonAction(Runnable onCloseButtonAction) {
        this.onCloseButtonAction.set(onCloseButtonAction);
    }

    /**
     * this property is used to replace JavaFX maximization
     * with a custom one that prevents hiding windows taskbar when
     * the JFXDecorator is maximized.
     *
     * @return customMaximizeProperty whether to use custom maximization or not.
     */
    public final BooleanProperty customMaximizeProperty() {
        return this.customMaximize;
    }

    /**
     * @return whether customMaximizeProperty is active or not
     */
    public final boolean isCustomMaximize() {
        return this.customMaximizeProperty().get();
    }

    /**
     * set customMaximize property
     *
     * @param customMaximize
     */
    public final void setCustomMaximize(final boolean customMaximize) {
        this.customMaximizeProperty().set(customMaximize);
    }

    /**
     * @param maximized
     */
    public void setMaximized(boolean maximized) {
        if (this.maximized != maximized) {
            Platform.runLater(() -> {
                btnMax.fire();
            });
        }
    }

    /**
     * will change the decorator content
     *
     * @param content
     */
    public void setContent(Node content) {
        this.contentPlaceHolder.getChildren().setAll(content);
    }

    /**
     * will set the title
     *
     * @param text
     * @deprecated Use {@link com.jfoenix.controls.JFXDecorator#setTitle(java.lang.String)} instead.
     */
    @Deprecated
    public void setText(String text) {
        setTitle(text);
    }

    /**
     * will get the title
     *
     * @deprecated Use {@link com.jfoenix.controls.JFXDecorator#setTitle(java.lang.String)} instead.
     */
    @Deprecated
    public String getText() {
        return getTitle();
    }

    public String getTitle() {
        return title.get();
    }

    /**
     * By default this title property is bound to the primaryStage's title property.
     * <p>
     * To change it to something else, use <pre>
     *     {@code jfxDecorator.titleProperty().unbind();}</pre> first.
     */
    public StringProperty titleProperty() {
        return title;
    }

    /**
     * If you want the {@code primaryStage}'s title and the {@code JFXDecorator}'s title to be different, then
     * go ahead and use this method.
     * <p>
     * By default, this title property is bound to the {@code primaryStage}'s title property-so merely setting the
     * {@code primaryStage}'s title, will set the {@code JFXDecorator}'s title.
     */
    public void setTitle(String title) {
        this.title.unbind();
        this.title.set(title);
    }

    public void setGraphic(Node node) {
        if (graphic != null) {
            graphicContainer.getChildren().remove(graphic);
        }
        if (node != null) {
            graphicContainer.getChildren().add(0, node);
        }
        graphic = node;
    }

    public HBox getButtonsContainer() {
        return buttonsContainer;
    }

    public Node getGraphic() {
        return graphic;
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
