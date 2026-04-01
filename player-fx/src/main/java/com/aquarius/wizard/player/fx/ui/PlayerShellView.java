package com.aquarius.wizard.player.fx.ui;

import com.aquarius.wizard.player.domain.model.SongSummary;
import com.aquarius.wizard.player.fx.legacy.LegacyOnlineMusicService;
import com.aquarius.wizard.player.fx.local.LegacyLocalLibraryService;
import com.aquarius.wizard.player.fx.playback.AudioPlaybackService;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

/**
 * First-stage desktop shell that migrates the old main window structure into
 * the rebuilt JavaFX module before the backend integration phase starts.
 */
public final class PlayerShellView {

    private final Stage stage;
    private final StackPane root = new StackPane();
    private final DrawerPane drawer = new DrawerPane();
    private final SongSummary initialSong = FxSampleData.nowPlaying();
    private final List<FxSampleData.PlaylistDetail> discoverPlaylists = new ArrayList<>(FxSampleData.playlistDetails());
    private final ObservableList<SongSummary> songRows = FXCollections.observableArrayList();
    private final FilteredList<SongSummary> filteredSongs = new FilteredList<>(this.songRows);
    private final NowPlayingDrawerView nowPlayingDrawerView = new NowPlayingDrawerView(this.initialSong);
    private final AudioPlaybackService playbackService = new AudioPlaybackService();
    private final LegacyLocalLibraryService localLibraryService = new LegacyLocalLibraryService();
    private final LegacyOnlineMusicService legacyOnlineMusicService = new LegacyOnlineMusicService();
    private final SystemTrayBridge systemTrayBridge = new SystemTrayBridge("WizardMusicBox");
    private final ToastNotifier toastNotifier = new ToastNotifier();
    private final MiniPlayerStage miniPlayerStage;
    private final DesktopLyricStage desktopLyricStage;
    private final ArtworkImageLoader artworkImageLoader = new ArtworkImageLoader();

    private final VBox sidebar = new VBox(18.0);
    private final TextField searchField = new TextField();
    private final TextField playlistFilterField = new TextField();
    private final FlowPane discoverGrid = new FlowPane(18.0, 18.0);
    private final List<StackPane> discoverCards = new ArrayList<>();
    private final List<Button> navigationButtons = new ArrayList<>();
    private final StackPane workspaceStack = new StackPane();
    private final ScrollPane discoverScrollPane = new ScrollPane();
    private final BorderPane playlistPage = new BorderPane();
    private final StackPane playlistCover = new StackPane();
    private final Label playlistCoverMark = new Label();
    private final Label playlistTitleLabel = new Label();
    private final Label playlistTagsLabel = new Label();
    private final Label playlistDescriptionLabel = new Label();
    private final Label playlistCountLabel = new Label();
    private final Label discoverSubtitleLabel = new Label();
    private final Label currentTrackTitleLabel = new Label();
    private final Label currentTrackArtistLabel = new Label();
    private final ImageView currentTrackArtworkView = new ImageView();
    private final Label currentTrackThumbnailLabel = new Label("▶");
    private final ImageView playlistCoverImageView = new ImageView();
    private final Label leftTimeLabel = new Label("00:00");
    private final Label rightTimeLabel = new Label();
    private final PlaybackSeekBar progressSeekBar = new PlaybackSeekBar();
    private final TableView<SongSummary> songTable = new TableView<>();
    private final Clipboard clipboard = Clipboard.getSystemClipboard();
    private final ClipboardContent clipboardContent = new ClipboardContent();
    private final Button playbackModeButton = roundControl("");
    private final Button playPauseButton = roundControl("");
    private final Button volumeButton = roundControl("");
    private final Button miniModeButton = roundControl("");
    private final Button moreActionsButton = roundControl("");
    private final VerticalVolumeBar volumeSlider = new VerticalVolumeBar();
    private final Popup volumePopup = new Popup();

    private Button discoverNavButton;
    private Button playlistNavButton;
    private Button localMusicNavButton;
    private Button downloadNavButton;
    private Button workspaceNavButton;
    private Button discoverRefreshButton;
    private FxSampleData.PlaylistDetail activePlaylist;
    private SongSummary currentSong;
    private int currentSongIndex;
    private int discoverRotationOffset;
    private boolean playbackActive;
    private boolean legacyOnlineLoading;
    private boolean disposed;
    private boolean exiting;
    private boolean windowMaximized;
    private FxSampleData.PlaylistDetail cachedLocalLibrary;
    private SongSummary queuedNextSong;
    private ReadOnlyDoubleProperty responsiveSceneWidthProperty;
    private PlaybackMode playbackMode = PlaybackMode.LIST_LOOP;
    private int discoverVisibleBatchSize = -1;

    private double dragOffsetX;
    private double dragOffsetY;
    private double restoredStageX;
    private double restoredStageY;
    private double restoredStageWidth;
    private double restoredStageHeight;

    public PlayerShellView(final Stage stage) {
        this.stage = stage;
        this.miniPlayerStage = new MiniPlayerStage(stage);
        this.desktopLyricStage = new DesktopLyricStage(stage);
        this.activePlaylist = this.discoverPlaylists.get(0);
        this.currentSong = this.initialSong;
        this.drawer.setDirection(DrawerPane.DrawerDirection.BOTTOM);
        this.drawer.setOnOpened(() -> this.nowPlayingDrawerView.setTickerEnabled(false));
        this.drawer.setOnClosed(() -> this.nowPlayingDrawerView.setTickerEnabled(false));
        this.nowPlayingDrawerView.setOnPreviousAction(() -> playRelativeSong(-1));
        this.nowPlayingDrawerView.setOnPlayPauseAction(this::togglePlayback);
        this.nowPlayingDrawerView.setOnNextAction(() -> playRelativeSong(1));
        this.nowPlayingDrawerView.setOnLyricAction(this.drawer::toggle);
        this.nowPlayingDrawerView.setOnCloseAction(this.drawer::close);
        this.miniPlayerStage.setOnPreviousAction(() -> playRelativeSong(-1));
        this.miniPlayerStage.setOnPlayPauseAction(this::togglePlayback);
        this.miniPlayerStage.setOnNextAction(() -> playRelativeSong(1));
        this.miniPlayerStage.setOnRestoreAction(this::restoreFromMiniMode);
        this.miniPlayerStage.setOnLyricAction(this::toggleDesktopLyricFromMiniMode);
        this.miniPlayerStage.setOnSongSelected(songSummary -> playSong(songSummary, false));
        this.desktopLyricStage.setOnPreviousAction(() -> playRelativeSong(-1));
        this.desktopLyricStage.setOnPlayPauseAction(this::togglePlayback);
        this.desktopLyricStage.setOnNextAction(() -> playRelativeSong(1));
        configurePlaybackCallbacks();
        configureVolumeControls();
        this.root.getStyleClass().add("app-frame");
        this.root.getChildren().add(buildMainChrome());
        installLocalImportDragAndDrop(this.sidebar);
        applyCurrentSong(this.currentSong);
        syncPlaybackState();
        this.nowPlayingDrawerView.setTickerEnabled(false);
        showPlaylist(this.activePlaylist, false);
        showDiscoverPage();
        refreshLegacyDiscoverPlaylists(false);
        this.systemTrayBridge.install(
            this::showMainWindow,
            () -> playRelativeSong(-1),
            this::togglePlayback,
            () -> playRelativeSong(1),
            this::enterMiniMode,
            this::requestExit
        );
    }

    public StackPane getView() {
        return this.root;
    }

    public void configureResponsiveLayout(
        final ReadOnlyDoubleProperty sceneWidthProperty,
        final ReadOnlyDoubleProperty sceneHeightProperty
    ) {
        this.responsiveSceneWidthProperty = sceneWidthProperty;
        this.searchField.prefWidthProperty().bind(
            Bindings.createDoubleBinding(
                () -> clamp(sceneWidthProperty.get() * 0.24, 220.0, 360.0),
                sceneWidthProperty
            )
        );

        this.sidebar.prefWidthProperty().bind(
            Bindings.createDoubleBinding(
                () -> clamp(sceneWidthProperty.get() * 0.18, 208.0, 248.0),
                sceneWidthProperty
            )
        );

        this.drawer.drawerHeightProperty().bind(
            Bindings.createDoubleBinding(
                () -> {
                    final double availableHeight = Math.max(460.0, sceneHeightProperty.get() - 54.0);
                    return clamp(sceneHeightProperty.get() * 0.76, 460.0, availableHeight);
                },
                sceneHeightProperty
            )
        );

        this.discoverGrid.prefWrapLengthProperty().bind(
            Bindings.createDoubleBinding(
                () -> Math.max(560.0, sceneWidthProperty.get() - this.sidebar.getPrefWidth() - 112.0),
                sceneWidthProperty,
                this.sidebar.prefWidthProperty()
            )
        );
        sceneWidthProperty.addListener((observable, oldValue, newValue) -> refreshDiscoverBatchSizeIfNeeded());

        rebindDiscoverCardWidths();
        refreshDiscoverBatchSizeIfNeeded();

        this.songTable.prefHeightProperty().bind(
            Bindings.createDoubleBinding(
                () -> Math.max(320.0, sceneHeightProperty.get() - 360.0),
                sceneHeightProperty
            )
        );

        this.nowPlayingDrawerView.configureResponsiveLayout(sceneWidthProperty, this.drawer.drawerHeightProperty());
    }

    private Node buildMainChrome() {
        final BorderPane shell = new BorderPane();
        shell.getStyleClass().add("app-shell");
        shell.setTop(buildTopBar());
        shell.setCenter(buildDrawerHost());
        shell.setBottom(buildBottomPlayerBar());
        return shell;
    }

    private Node buildDrawerHost() {
        final BorderPane contentShell = new BorderPane();
        contentShell.setLeft(buildSidebar());
        contentShell.setCenter(buildContentArea());

        this.drawer.setContent(contentShell);
        this.drawer.setSidePane(this.nowPlayingDrawerView);
        return this.drawer;
    }

    private Node buildTopBar() {
        final HBox topBar = new HBox(18.0);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(18.0, 22.0, 16.0, 26.0));

        final HBox brandBox = new HBox(10.0);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        final Label logo = new Label("WizardMusicBox");
        logo.getStyleClass().add("brand-logo");
        brandBox.getChildren().addAll(createBrandMark(), logo);

        this.searchField.setPromptText("搜索本地曲库、歌单或歌词");
        this.searchField.getStyleClass().add("search-field");
        this.searchField.setOnAction(event -> submitGlobalSearch());

        final Button searchButton = topIconButton(
            SvgIconFactory.createIcon(AppGlyphs.SEARCH, 13.0, Color.WHITE),
            this::submitGlobalSearch
        );
        applyTooltip(searchButton, "搜索");

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        final HBox topActions = new HBox(10.0);
        topActions.setAlignment(Pos.CENTER_RIGHT);
        topActions.getChildren().addAll(
            topIconButton(SvgIconFactory.createIcon(AppGlyphs.ABOUT, 13.0, Color.WHITE), this::openAboutDialog),
            topIconButton(SvgIconFactory.createIcon(AppGlyphs.MINI_MODE, 13.0, Color.WHITE), this::enterMiniMode)
        );

        final HBox windowControls = new HBox(10.0);
        windowControls.setAlignment(Pos.CENTER_RIGHT);
        windowControls.getChildren().addAll(
            windowButton("—", () -> this.stage.setIconified(true)),
            windowButton("□", this::toggleMaximized),
            windowButton("✕", this::requestExit)
        );

        topBar.getChildren().addAll(brandBox, this.searchField, searchButton, spacer, topActions, windowControls);
        enableWindowDrag(topBar);
        return topBar;
    }

    private Node createBrandMark() {
        final StackPane brandMark = new StackPane();
        brandMark.getStyleClass().add("brand-mark");
        final Image logoImage = this.artworkImageLoader.loadLogo();
        if (logoImage != null) {
            final ImageView imageView = new ImageView(logoImage);
            imageView.setFitWidth(22.0);
            imageView.setFitHeight(22.0);
            imageView.setPreserveRatio(true);
            imageView.setClip(new Circle(11.0, 11.0, 11.0));
            brandMark.getChildren().add(imageView);
        } else {
            final Label fallback = new Label("♫");
            fallback.setTextFill(Color.WHITE);
            brandMark.getChildren().add(fallback);
        }
        return brandMark;
    }

    private Node buildSidebar() {
        this.sidebar.getStyleClass().add("sidebar");
        this.sidebar.setPadding(new Insets(16.0, 16.0, 18.0, 16.0));

        final StackPane hero = new StackPane();
        hero.getStyleClass().add("sidebar-hero");
        hero.setPrefHeight(166.0);

        final VBox heroContent = new VBox(10.0);
        heroContent.setAlignment(Pos.TOP_LEFT);
        heroContent.setPadding(new Insets(14.0));

        final Node avatar = createSidebarAvatar();

        final Label userName = new Label("水瓶座鬼才");
        userName.getStyleClass().add("sidebar-user-name");
        final Label mailLabel = new Label("747897928@qq.com");
        mailLabel.getStyleClass().add("sidebar-user-mail");

        heroContent.getChildren().addAll(avatar, userName, mailLabel);
        hero.getChildren().add(heroContent);

        final VBox navigation = new VBox(8.0);
        navigation.getStyleClass().add("nav-group");

        this.discoverNavButton = createNavigationButton("发现音乐");
        this.discoverNavButton.setOnAction(event -> showDiscoverPage());

        this.playlistNavButton = createNavigationButton("歌单");
        this.playlistNavButton.setOnAction(event -> openActivePlaylist());

        final Button lyricNavButton = createNavigationButton("歌词");
        lyricNavButton.setOnAction(event -> this.drawer.toggle());

        this.localMusicNavButton = createNavigationButton("本地音乐");
        this.localMusicNavButton.setOnAction(event -> showPlaylist(loadLocalLibrary(), this.localMusicNavButton));

        this.downloadNavButton = createNavigationButton("下载当前音乐");
        this.downloadNavButton.setOnAction(event -> downloadCurrentSong());

        this.workspaceNavButton = createNavigationButton("本地音乐文件夹");
        this.workspaceNavButton.setOnAction(event -> openLocalMusicDirectory());

        navigation.getChildren().addAll(
            this.discoverNavButton,
            this.playlistNavButton,
            lyricNavButton,
            this.localMusicNavButton,
            this.downloadNavButton,
            this.workspaceNavButton
        );

        final Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        this.sidebar.getChildren().setAll(hero, navigation, spacer);
        return this.sidebar;
    }

    private Node buildContentArea() {
        final VBox discoverPageContent = new VBox(18.0);
        discoverPageContent.getStyleClass().add("discover-page");
        discoverPageContent.setPadding(new Insets(22.0, 24.0, 26.0, 24.0));
        discoverPageContent.getChildren().addAll(buildDiscoverHeader(), buildDiscoverGrid());

        this.discoverScrollPane.getStyleClass().add("content-scroll-pane");
        this.discoverScrollPane.setFitToWidth(true);
        this.discoverScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.discoverScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.discoverScrollPane.setContent(discoverPageContent);

        this.playlistPage.getStyleClass().add("playlist-page");
        this.playlistPage.setPadding(new Insets(22.0, 24.0, 26.0, 24.0));
        this.playlistPage.setTop(buildPlaylistHeader());
        this.playlistPage.setCenter(buildPlaylistTable());

        this.workspaceStack.getStyleClass().add("workspace-stack");
        this.workspaceStack.getChildren().addAll(this.discoverScrollPane, this.playlistPage);
        return this.workspaceStack;
    }

    private Node buildDiscoverHeader() {
        final HBox header = new HBox(14.0);
        header.getStyleClass().add("discover-header");
        header.setAlignment(Pos.CENTER_LEFT);

        final VBox titleBox = new VBox(4.0);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        final Label title = new Label("推荐歌单 >");
        title.getStyleClass().add("discover-title");
        restoreDiscoverSubtitle();
        this.discoverSubtitleLabel.getStyleClass().add("discover-subtitle");
        this.discoverSubtitleLabel.setWrapText(true);
        titleBox.getChildren().addAll(title, this.discoverSubtitleLabel);

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.discoverRefreshButton = new Button("换一组");
        this.discoverRefreshButton.getStyleClass().add("discover-action-button");
        MaterialButtonFeedback.install(this.discoverRefreshButton);
        this.discoverRefreshButton.setOnAction(event -> refreshDiscoverContent());
        this.discoverRefreshButton.setDisable(false);

        header.getChildren().addAll(titleBox, spacer, this.discoverRefreshButton);
        return header;
    }

    private Node buildDiscoverGrid() {
        this.discoverGrid.getStyleClass().add("discover-grid");
        rebuildDiscoverCards();
        return this.discoverGrid;
    }

    private Node buildDiscoverCard(final FxSampleData.PlaylistDetail detail) {
        final StackPane card = new StackPane();
        card.getStyleClass().add("discover-card");
        card.setCursor(Cursor.HAND);
        card.setMinHeight(214.0);
        MaterialButtonFeedback.install(card);
        card.setOnMouseClicked(event -> openDiscoverPlaylist(detail));
        this.discoverCards.add(card);
        bindDiscoverCardWidth(card);

        final VBox body = new VBox(12.0);
        body.setPadding(new Insets(14.0));

        final StackPane cover = new StackPane();
        cover.getStyleClass().add("discover-card-cover");
        cover.setStyle("-fx-background-color: linear-gradient(to bottom right, "
            + detail.accentColor()
            + ", rgba(31, 53, 104, 0.96));");
        cover.setMinHeight(130.0);
        cover.setPrefHeight(130.0);
        cover.setMaxHeight(130.0);
        final Image coverImage = this.artworkImageLoader.loadPlaylistArtwork(detail);
        final ImageView coverImageView = new ImageView(coverImage);
        coverImageView.setManaged(false);
        coverImageView.setMouseTransparent(true);
        coverImageView.setPreserveRatio(false);
        coverImageView.fitWidthProperty().bind(cover.widthProperty());
        coverImageView.fitHeightProperty().bind(cover.heightProperty());
        final Rectangle coverClip = createRoundedClip(320.0, 130.0, 16.0);
        coverClip.widthProperty().bind(cover.widthProperty());
        coverClip.heightProperty().bind(cover.heightProperty());
        coverImageView.setClip(coverClip);
        coverImageView.getStyleClass().add("discover-card-cover-image");

        final Label coverMark = new Label(detail.coverMark());
        coverMark.getStyleClass().add("discover-card-cover-mark");
        coverMark.setMouseTransparent(true);
        coverMark.setVisible(coverImage == null || coverImage == this.artworkImageLoader.loadFallbackArtwork());
        cover.getChildren().addAll(coverImageView, coverMark);

        final Label title = new Label(detail.title());
        title.getStyleClass().add("discover-card-title");
        title.setWrapText(true);
        title.setTextOverrun(OverrunStyle.ELLIPSIS);
        title.setMinHeight(58.0);
        title.setPrefHeight(58.0);
        title.setMaxHeight(58.0);

        final Label tags = new Label(detail.tags());
        tags.getStyleClass().add("discover-card-tags");

        final Label description = new Label(detail.description());
        description.getStyleClass().add("discover-card-description");
        description.setWrapText(true);
        description.setTextOverrun(OverrunStyle.ELLIPSIS);
        description.setMinHeight(76.0);
        description.setPrefHeight(76.0);
        description.setMaxHeight(76.0);

        body.getChildren().addAll(cover, title, tags, description);
        card.getChildren().add(body);
        return card;
    }

    private Node buildPlaylistHeader() {
        final VBox header = new VBox(16.0);
        header.getChildren().addAll(buildPlaylistHero(), buildPlaylistToolbar());
        return header;
    }

    private Node buildPlaylistHero() {
        final HBox hero = new HBox(18.0);
        hero.getStyleClass().add("playlist-hero");
        hero.setAlignment(Pos.CENTER_LEFT);

        this.playlistCover.getStyleClass().add("playlist-cover");
        this.playlistCover.setMinSize(136.0, 136.0);
        this.playlistCover.setPrefSize(136.0, 136.0);
        this.playlistCoverImageView.getStyleClass().add("playlist-cover-image");
        this.playlistCoverImageView.setFitWidth(136.0);
        this.playlistCoverImageView.setFitHeight(136.0);
        this.playlistCoverImageView.setPreserveRatio(false);
        this.playlistCoverImageView.setClip(createRoundedClip(136.0, 136.0, 18.0));
        this.playlistCoverMark.getStyleClass().add("playlist-cover-mark");
        this.playlistCover.getStyleClass().add("playlist-cover-draggable");
        this.playlistCover.setCursor(Cursor.OPEN_HAND);
        this.playlistCover.setOnDragDetected(event -> {
            beginPlaylistCoverExportDrag();
            event.consume();
        });
        this.playlistCover.setOnDragDone(event -> {
            this.playlistCover.getStyleClass().remove("playlist-cover-dragging");
            this.playlistCover.setCursor(Cursor.OPEN_HAND);
            event.consume();
        });
        final ContextMenu playlistCoverMenu = new ContextMenu();
        final MenuItem copyCoverLinkItem = createMenuItem("复制歌单封面链接", AppGlyphs.LINK);
        copyCoverLinkItem.setOnAction(event -> copyPlaylistCoverLink());
        playlistCoverMenu.getItems().setAll(copyCoverLinkItem);
        playlistCoverMenu.setOnShowing(event -> copyCoverLinkItem.setDisable(resolvePlaylistCoverUrl().isBlank()));
        this.playlistCover.setOnContextMenuRequested(event -> {
            playlistCoverMenu.show(this.playlistCover, event.getScreenX(), event.getScreenY());
            event.consume();
        });
        Tooltip.install(this.playlistCover, new Tooltip("拖拽封面到系统保存图片，右键复制封面链接"));
        this.playlistCover.getChildren().setAll(this.playlistCoverImageView, this.playlistCoverMark);

        final VBox meta = new VBox(10.0);
        meta.setAlignment(Pos.CENTER_LEFT);

        final Label pill = new Label("歌单");
        pill.getStyleClass().add("playlist-label-pill");

        this.playlistTitleLabel.getStyleClass().add("playlist-name");
        this.playlistTagsLabel.getStyleClass().add("playlist-meta");
        this.playlistDescriptionLabel.getStyleClass().add("playlist-description");
        this.playlistDescriptionLabel.setWrapText(true);

        meta.getChildren().addAll(
            pill,
            this.playlistTitleLabel,
            metadataRow(AppGlyphs.TAG, this.playlistTagsLabel),
            metadataRow(AppGlyphs.DESCRIPTION, this.playlistDescriptionLabel)
        );
        HBox.setHgrow(meta, Priority.ALWAYS);

        hero.getChildren().addAll(this.playlistCover, meta);
        return hero;
    }

    private Node buildPlaylistToolbar() {
        final HBox toolbar = new HBox(12.0);
        toolbar.getStyleClass().add("playlist-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        final Label sectionBadge = new Label("歌单列表");
        sectionBadge.getStyleClass().add("playlist-section-badge");

        this.playlistFilterField.setPromptText("搜索表格中的音乐");
        this.playlistFilterField.getStyleClass().add("playlist-filter-field");
        HBox.setHgrow(this.playlistFilterField, Priority.ALWAYS);
        this.playlistFilterField.textProperty().addListener((observable, oldValue, newValue) -> applySongFilter());
        this.playlistFilterField.setOnAction(event -> applySongFilter());

        final Button searchButton = new Button("搜索");
        searchButton.getStyleClass().add("playlist-filter-button");
        searchButton.setOnAction(event -> applySongFilter());
        MaterialButtonFeedback.install(searchButton);

        final Button importButton = toolbarActionButton("导入音乐", this::importLocalFiles);
        final Button refreshButton = toolbarActionButton("刷新本地", this::refreshLocalLibrary);
        final Button openFolderButton = toolbarActionButton("打开目录", this::openLocalMusicDirectory);

        this.playlistCountLabel.getStyleClass().add("playlist-count");

        toolbar.getChildren().addAll(
            sectionBadge,
            this.playlistFilterField,
            searchButton,
            importButton,
            refreshButton,
            openFolderButton,
            this.playlistCountLabel
        );
        return toolbar;
    }

    private Node buildPlaylistTable() {
        this.songTable.getStyleClass().add("playlist-table");
        this.songTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.songTable.setPlaceholder(new Label("该歌单暂时没有数据"));
        this.songTable.setItems(this.filteredSongs);

        final TableColumn<SongSummary, String> titleColumn = new TableColumn<>("音乐标题");
        titleColumn.setCellValueFactory(cell -> Bindings.createStringBinding(cell.getValue()::title));

        final TableColumn<SongSummary, String> artistColumn = new TableColumn<>("歌手");
        artistColumn.setCellValueFactory(cell -> Bindings.createStringBinding(cell.getValue()::artist));

        final TableColumn<SongSummary, String> albumColumn = new TableColumn<>("专辑");
        albumColumn.setCellValueFactory(cell -> Bindings.createStringBinding(cell.getValue()::album));

        this.songTable.getColumns().setAll(titleColumn, artistColumn, albumColumn);
        this.songTable.setRowFactory(tableView -> buildSongRow());
        this.songTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                final SongSummary selectedSong = this.songTable.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    playSong(selectedSong, false);
                }
            }
        });
        this.songTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                final SongSummary selectedSong = this.songTable.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    playSong(selectedSong, false);
                    event.consume();
                }
            }
        });
        return this.songTable;
    }

    private TableRow<SongSummary> buildSongRow() {
        final TableRow<SongSummary> row = new TableRow<>();
        row.setContextMenu(buildSongContextMenu(row));
        row.setOnMouseClicked(event -> {
            if (!row.isEmpty() && event.getClickCount() == 2) {
                this.songTable.getSelectionModel().select(row.getIndex());
                playSong(row.getItem(), false);
            }
        });
        return row;
    }

    private Node buildBottomPlayerBar() {
        final HBox bottomBar = new HBox(22.0);
        bottomBar.getStyleClass().add("bottom-player-bar");
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setPadding(new Insets(16.0, 22.0, 18.0, 22.0));

        final HBox currentTrackCard = new HBox(14.0);
        currentTrackCard.getStyleClass().add("current-track-card");
        currentTrackCard.setAlignment(Pos.CENTER_LEFT);
        currentTrackCard.setCursor(Cursor.HAND);
        currentTrackCard.setPickOnBounds(true);
        MaterialButtonFeedback.install(currentTrackCard);
        currentTrackCard.setOnMouseClicked(event -> this.drawer.toggle());

        final StackPane trackArtworkPane = new StackPane();
        trackArtworkPane.getStyleClass().add("track-thumbnail-pane");
        this.currentTrackArtworkView.getStyleClass().add("track-thumbnail-image");
        this.currentTrackArtworkView.setFitWidth(52.0);
        this.currentTrackArtworkView.setFitHeight(52.0);
        this.currentTrackArtworkView.setPreserveRatio(false);
        this.currentTrackArtworkView.setClip(createRoundedClip(52.0, 52.0, 16.0));
        this.currentTrackThumbnailLabel.getStyleClass().add("track-thumbnail");
        trackArtworkPane.getChildren().addAll(this.currentTrackArtworkView, this.currentTrackThumbnailLabel);

        final VBox trackInfo = new VBox(4.0);
        this.currentTrackTitleLabel.getStyleClass().add("track-title");
        this.currentTrackArtistLabel.getStyleClass().add("track-artist");
        trackInfo.getChildren().addAll(this.currentTrackTitleLabel, this.currentTrackArtistLabel);

        currentTrackCard.getChildren().addAll(trackArtworkPane, trackInfo);

        final VBox progressBlock = new VBox(10.0);
        progressBlock.setAlignment(Pos.CENTER);
        progressBlock.setMinWidth(0.0);
        HBox.setHgrow(progressBlock, Priority.ALWAYS);

        final HBox controls = new HBox(12.0);
        controls.setAlignment(Pos.CENTER);
        applyTooltip(this.playbackModeButton, "切换播放模式");
        this.playbackModeButton.setOnAction(event -> cyclePlaybackMode());
        final Button previousButton = roundControl("");
        previousButton.setOnAction(event -> playRelativeSong(-1));
        final Button nextButton = roundControl("");
        nextButton.setOnAction(event -> playRelativeSong(1));
        final Button lyricButton = roundControl("");
        applyTooltip(previousButton, "上一首");
        applyTooltip(this.playPauseButton, "播放 / 暂停");
        applyTooltip(nextButton, "下一首");
        applyTooltip(lyricButton, "桌面歌词");
        setRoundButtonGraphic(previousButton, AppGlyphs.PREVIOUS, 16.0);
        setRoundButtonGraphic(nextButton, AppGlyphs.NEXT, 16.0);
        setRoundButtonGraphic(lyricButton, AppGlyphs.LYRICS, 16.0);
        this.volumeButton.setGraphic(SvgIconFactory.createIcon(AppGlyphs.VOLUME, 15.0, Color.WHITE));
        applyTooltip(this.volumeButton, "音量");
        this.volumeButton.setOnAction(event -> toggleVolumePopup());
        lyricButton.setOnAction(event -> toggleDesktopLyric(this.stage));
        this.playPauseButton.setOnAction(event -> togglePlayback());
        controls.getChildren().addAll(
            this.playbackModeButton,
            previousButton,
            this.playPauseButton,
            nextButton,
            lyricButton,
            this.volumeButton
        );

        final HBox progressRow = new HBox(12.0);
        progressRow.setAlignment(Pos.CENTER);
        progressRow.setMinWidth(0.0);
        this.leftTimeLabel.getStyleClass().add("progress-time");
        configureProgressSeekBar();
        HBox.setHgrow(this.progressSeekBar, Priority.ALWAYS);
        this.rightTimeLabel.getStyleClass().add("progress-time");
        progressRow.getChildren().addAll(this.leftTimeLabel, this.progressSeekBar, this.rightTimeLabel);

        progressBlock.getChildren().addAll(controls, progressRow);

        final HBox rightActions = new HBox(12.0);
        rightActions.setAlignment(Pos.CENTER_RIGHT);
        rightActions.setMinWidth(0.0);
        this.miniModeButton.setText("");
        this.miniModeButton.setGraphic(SvgIconFactory.createIcon(AppGlyphs.MINI_MODE, 14.0, Color.WHITE));
        applyTooltip(this.miniModeButton, "进入迷你模式");
        this.miniModeButton.setOnAction(event -> enterMiniMode());
        final Button localFolderButton = roundControl("⌂");
        localFolderButton.setText("");
        localFolderButton.setGraphic(SvgIconFactory.createIcon(AppGlyphs.FOLDER, 14.0, Color.WHITE));
        applyTooltip(localFolderButton, "打开本地音乐目录");
        localFolderButton.setOnAction(event -> openLocalMusicDirectory());
        this.moreActionsButton.setText("");
        this.moreActionsButton.setGraphic(SvgIconFactory.createIcon(AppGlyphs.UNFOLD, 14.0, Color.WHITE));
        applyTooltip(this.moreActionsButton, "更多操作");
        this.moreActionsButton.setOnAction(event -> showQuickActionsMenu());
        rightActions.getChildren().addAll(this.miniModeButton, localFolderButton, this.moreActionsButton);

        bottomBar.getChildren().addAll(currentTrackCard, progressBlock, rightActions);
        return bottomBar;
    }

    private void showDiscoverPage() {
        this.discoverScrollPane.setVisible(true);
        this.discoverScrollPane.setManaged(true);
        this.playlistPage.setVisible(false);
        this.playlistPage.setManaged(false);
        setActiveNavigation(this.discoverNavButton);
        restoreDiscoverSubtitle();
        rebuildDiscoverCards();
    }

    private void openActivePlaylist() {
        if (this.activePlaylist == null) {
            return;
        }
        if (this.activePlaylist.isLegacyOnlinePlaylist() && (this.activePlaylist.songs() == null || this.activePlaylist.songs().isEmpty())) {
            openDiscoverPlaylist(this.activePlaylist);
            return;
        }
        showPlaylist(this.activePlaylist, true);
    }

    private void showPlaylist(final FxSampleData.PlaylistDetail detail, final boolean highlightPlaylistNav) {
        showPlaylist(detail, highlightPlaylistNav ? this.playlistNavButton : null);
    }

    private void showPlaylist(final FxSampleData.PlaylistDetail detail, final Button activeButton) {
        this.activePlaylist = detail;
        this.songRows.setAll(detail.songs());
        this.playlistFilterField.clear();
        this.playlistTitleLabel.setText(detail.title());
        this.playlistTagsLabel.setText("标签：" + detail.tags());
        this.playlistDescriptionLabel.setText("介绍：" + detail.description());
        this.playlistCoverMark.setText(detail.coverMark());
        final Image playlistArtwork = this.artworkImageLoader.loadPlaylistArtwork(detail);
        this.playlistCoverImageView.setImage(playlistArtwork);
        this.playlistCoverMark.setVisible(
            playlistArtwork == null || playlistArtwork == this.artworkImageLoader.loadFallbackArtwork()
        );
        this.playlistCover.setStyle("-fx-background-color: linear-gradient(to bottom right, "
            + detail.accentColor()
            + ", rgba(27, 58, 117, 0.96));");
        applySongFilter();
        syncSongSelection();
        this.miniPlayerStage.setPlaylistSongs(new ArrayList<>(this.songRows));

        this.discoverScrollPane.setVisible(false);
        this.discoverScrollPane.setManaged(false);
        this.playlistPage.setVisible(true);
        this.playlistPage.setManaged(true);
        if (activeButton != null) {
            setActiveNavigation(activeButton);
        }
    }

    private FxSampleData.PlaylistDetail loadLocalLibrary() {
        this.cachedLocalLibrary = this.localLibraryService.loadLocalLibrary();
        return this.cachedLocalLibrary;
    }

    private void applySongFilter() {
        final String playlistFilter = normalizeFilter(this.playlistFilterField.getText());
        this.filteredSongs.setPredicate(song -> matchesSongFilter(song, playlistFilter));
        this.playlistCountLabel.setText(this.filteredSongs.size() + " 首");
        this.miniPlayerStage.setPlaylistSongs(new ArrayList<>(this.filteredSongs));
    }

    private void submitGlobalSearch() {
        final String query = this.searchField.getText() == null ? "" : this.searchField.getText().trim();
        if (query.isEmpty()) {
            this.discoverRotationOffset = 0;
            showDiscoverPage();
            return;
        }
        searchLegacyOnlineSongs(query);
    }

    private void refreshDiscoverContent() {
        refreshLegacyDiscoverPlaylists(true);
    }

    private void refreshLegacyDiscoverPlaylists(final boolean forceRefresh) {
        if (this.legacyOnlineLoading) {
            return;
        }
        this.legacyOnlineLoading = true;
        if (this.discoverRefreshButton != null) {
            this.discoverRefreshButton.setDisable(true);
        }
        this.discoverSubtitleLabel.setText("正在从 legacy 在线源加载推荐歌单...");
        CompletableFuture
            .supplyAsync(this.legacyOnlineMusicService::loadFeaturedPlaylists)
            .thenAccept(playlistDetails -> Platform.runLater(() -> {
                this.legacyOnlineLoading = false;
                if (this.discoverRefreshButton != null) {
                    this.discoverRefreshButton.setDisable(false);
                }
                if (!playlistDetails.isEmpty()) {
                    final List<FxSampleData.PlaylistDetail> resolvedPlaylists = ensureMinimumDiscoverPlaylists(playlistDetails);
                    this.discoverPlaylists.clear();
                    this.discoverPlaylists.addAll(resolvedPlaylists);
                    this.discoverRotationOffset = 0;
                    this.activePlaylist = resolvedPlaylists.get(0);
                    restoreDiscoverSubtitle();
                    rebuildDiscoverCards();
                    return;
                }
                if (forceRefresh) {
                    this.discoverSubtitleLabel.setText("legacy 在线歌单暂未返回，已回退到本地样例。");
                }
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> {
                    this.legacyOnlineLoading = false;
                    if (this.discoverRefreshButton != null) {
                        this.discoverRefreshButton.setDisable(false);
                    }
                    if (forceRefresh) {
                        this.discoverSubtitleLabel.setText("legacy 在线歌单加载失败，当前继续使用本地样例。");
                    }
                });
                return null;
            });
    }

    private void searchLegacyOnlineSongs(final String query) {
        this.discoverSubtitleLabel.setText("正在搜索在线歌曲...");
        if (this.discoverRefreshButton != null) {
            this.discoverRefreshButton.setDisable(true);
        }
        CompletableFuture
            .supplyAsync(() -> this.legacyOnlineMusicService.searchSongs(query))
            .thenAccept(playlistDetail -> Platform.runLater(() -> {
                if (this.discoverRefreshButton != null) {
                    this.discoverRefreshButton.setDisable(false);
                }
                showPlaylist(playlistDetail, this.playlistNavButton);
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> {
                    if (this.discoverRefreshButton != null) {
                        this.discoverRefreshButton.setDisable(false);
                    }
                    showWarning("在线搜索失败", "当前未能完成 legacy 在线搜索。");
                });
                return null;
            });
    }

    private void openDiscoverPlaylist(final FxSampleData.PlaylistDetail detail) {
        if (detail == null) {
            return;
        }
        if (detail.isLegacyOnlinePlaylist() && (detail.songs() == null || detail.songs().isEmpty())) {
            this.discoverSubtitleLabel.setText("正在加载歌单详情...");
            if (this.discoverRefreshButton != null) {
                this.discoverRefreshButton.setDisable(true);
            }
            CompletableFuture
                .supplyAsync(() -> this.legacyOnlineMusicService.loadPlaylist(detail.sourceId()))
                .thenAccept(playlistDetail -> Platform.runLater(() -> {
                    if (this.discoverRefreshButton != null) {
                        this.discoverRefreshButton.setDisable(false);
                    }
                    replaceDiscoverPlaylist(detail, playlistDetail);
                    showPlaylist(playlistDetail, true);
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        if (this.discoverRefreshButton != null) {
                            this.discoverRefreshButton.setDisable(false);
                        }
                        showWarning("歌单加载失败", "legacy 在线歌单详情加载失败。");
                    });
                    return null;
                });
            return;
        }
        showPlaylist(detail, true);
    }

    private void replaceDiscoverPlaylist(
        final FxSampleData.PlaylistDetail originalDetail,
        final FxSampleData.PlaylistDetail resolvedDetail
    ) {
        final int discoverIndex = this.discoverPlaylists.indexOf(originalDetail);
        if (discoverIndex >= 0) {
            this.discoverPlaylists.set(discoverIndex, resolvedDetail);
            rebuildDiscoverCards();
        }
    }

    private long countMatchingDiscoverPlaylists() {
        final String filterText = normalizeFilter(this.searchField.getText());
        return this.discoverPlaylists.stream()
            .filter(detail ->
                filterText.isEmpty()
                    || containsIgnoreCase(detail.title(), filterText)
                    || containsIgnoreCase(detail.tags(), filterText)
                    || containsIgnoreCase(detail.description(), filterText)
            )
            .count();
    }

    private boolean containsIgnoreCase(final String source, final String filterText) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(filterText);
    }

    private boolean isLegacySearchPlaylistActive() {
        return this.activePlaylist != null && "legacy-online-search".equals(this.activePlaylist.sourceType());
    }

    private void restoreDiscoverSubtitle() {
        if (this.discoverPlaylists.stream().anyMatch(FxSampleData.PlaylistDetail::isLegacyOnlinePlaylist)) {
            this.discoverSubtitleLabel.setText("旧版在线推荐歌单已接回，点击卡片可继续加载歌单详情。");
            return;
        }
        this.discoverSubtitleLabel.setText("旧版首页先迁成真正可点击的工作区，不再停留在展示型 Hero 卡片。");
    }

    private String normalizeFilter(final String filterText) {
        return filterText == null ? "" : filterText.trim().toLowerCase(Locale.ROOT);
    }

    private boolean matchesSongFilter(final SongSummary song, final String filterText) {
        if (filterText.isEmpty()) {
            return true;
        }
        return containsIgnoreCase(song.title(), filterText)
            || containsIgnoreCase(song.artist(), filterText)
            || containsIgnoreCase(song.album(), filterText);
    }

    private void rebuildDiscoverCards() {
        this.discoverGrid.getChildren().clear();
        this.discoverCards.clear();
        for (final FxSampleData.PlaylistDetail detail : resolveVisibleDiscoverPlaylists()) {
            this.discoverGrid.getChildren().add(buildDiscoverCard(detail));
        }
    }

    private List<FxSampleData.PlaylistDetail> resolveVisibleDiscoverPlaylists() {
        final String filterText = normalizeFilter(this.searchField.getText());
        final List<FxSampleData.PlaylistDetail> matchingPlaylists = this.discoverPlaylists.stream()
            .filter(detail ->
                filterText.isEmpty()
                    || containsIgnoreCase(detail.title(), filterText)
                    || containsIgnoreCase(detail.tags(), filterText)
                    || containsIgnoreCase(detail.description(), filterText)
            )
            .toList();

        final int batchSize = resolveDiscoverBatchSize();
        if (matchingPlaylists.size() <= batchSize) {
            return matchingPlaylists;
        }

        final List<FxSampleData.PlaylistDetail> visiblePlaylists = new ArrayList<>(batchSize);
        for (int index = 0; index < batchSize; index++) {
            final int resolvedIndex = (this.discoverRotationOffset + index) % matchingPlaylists.size();
            visiblePlaylists.add(matchingPlaylists.get(resolvedIndex));
        }
        return visiblePlaylists;
    }

    private int resolveDiscoverBatchSize() {
        final double sceneWidth = this.responsiveSceneWidthProperty != null
            ? this.responsiveSceneWidthProperty.get()
            : Math.max(this.root.getWidth(), 1280.0);
        if (sceneWidth >= 1500.0) {
            return 10;
        }
        if (sceneWidth >= 1280.0) {
            return 8;
        }
        return 6;
    }

    private void playSong(final SongSummary song, final boolean openDrawer) {
        if (song == null) {
            return;
        }
        this.currentSong = song;
        this.currentSongIndex = Math.max(0, this.songRows.indexOf(song));
        applyCurrentSong(song);
        this.playbackService.play(song);
        if (song.isLegacyOnlineSource() && song.sourceId() != null && !song.sourceId().isBlank()) {
            loadLegacyLyricsAsync(song);
        }
        syncSongSelection();
        if (openDrawer) {
            this.drawer.open();
        }
    }

    private void loadLegacyLyricsAsync(final SongSummary song) {
        CompletableFuture
            .supplyAsync(() -> this.legacyOnlineMusicService.loadLyrics(song))
            .thenAccept(enhancedSong -> Platform.runLater(() -> applyEnhancedSong(enhancedSong)))
            .exceptionally(throwable -> null);
    }

    private void applyEnhancedSong(final SongSummary enhancedSong) {
        if (enhancedSong == null || this.currentSong == null || !Objects.equals(this.currentSong.sourceId(), enhancedSong.sourceId())) {
            return;
        }
        this.currentSong = enhancedSong;
        this.currentTrackTitleLabel.setText(enhancedSong.title());
        this.currentTrackArtistLabel.setText(enhancedSong.artist());
        this.nowPlayingDrawerView.setSong(enhancedSong);
        this.miniPlayerStage.setSong(enhancedSong);
        this.desktopLyricStage.setSong(enhancedSong);
        this.systemTrayBridge.updateToolTip(enhancedSong.title());
        final int currentIndexInRows = findSongIndexBySourceId(this.songRows, enhancedSong.sourceId());
        if (currentIndexInRows >= 0) {
            this.songRows.set(currentIndexInRows, enhancedSong);
        }
        if (this.activePlaylist != null && this.activePlaylist.songs() != null) {
            final List<SongSummary> updatedSongs = new ArrayList<>(this.activePlaylist.songs());
            final int activeIndex = findSongIndexBySourceId(updatedSongs, enhancedSong.sourceId());
            if (activeIndex >= 0) {
                updatedSongs.set(activeIndex, enhancedSong);
                this.activePlaylist = this.activePlaylist.withSongs(updatedSongs);
            }
        }
    }

    private int findSongIndexBySourceId(final List<SongSummary> songs, final String sourceId) {
        if (songs == null || sourceId == null || sourceId.isBlank()) {
            return -1;
        }
        for (int index = 0; index < songs.size(); index++) {
            final SongSummary item = songs.get(index);
            if (item != null && Objects.equals(item.sourceId(), sourceId)) {
                return index;
            }
        }
        return -1;
    }

    private void playRelativeSong(final int offset) {
        if (this.songRows.isEmpty()) {
            return;
        }
        final int safeIndex = resolveRelativeSongIndex(offset);
        playSong(this.songRows.get(safeIndex), false);
    }

    private int resolveCurrentPlaylistIndex() {
        final int songIndex = this.songRows.indexOf(this.currentSong);
        if (songIndex >= 0) {
            return songIndex;
        }
        return Math.min(this.currentSongIndex, Math.max(0, this.songRows.size() - 1));
    }

    private int clampIndex(final int index, final int size) {
        if (size <= 0) {
            return 0;
        }
        if (index < 0) {
            return size - 1;
        }
        if (index >= size) {
            return 0;
        }
        return index;
    }

    private int resolveRelativeSongIndex(final int offset) {
        final int currentIndex = resolveCurrentPlaylistIndex();
        final int size = this.songRows.size();
        if (this.playbackMode == PlaybackMode.LIST_LOOP) {
            return clampIndex(currentIndex + offset, size);
        }
        return Math.max(0, Math.min(size - 1, currentIndex + offset));
    }

    private void applyCurrentSong(final SongSummary song) {
        this.currentTrackTitleLabel.setText(song.title());
        this.currentTrackArtistLabel.setText(song.artist());
        this.currentTrackArtworkView.setImage(this.artworkImageLoader.loadSongArtwork(song));
        this.currentTrackThumbnailLabel.setVisible(this.currentTrackArtworkView.getImage() == null);
        this.currentTrackThumbnailLabel.setText(song.title().isBlank() ? "♫" : song.title().substring(0, 1));
        this.currentTrackThumbnailLabel.setStyle("-fx-background-color: linear-gradient(to bottom right, "
            + song.accentColor()
            + ", #4d62ff);");
        this.leftTimeLabel.setText("00:00");
        this.rightTimeLabel.setText(song.duration().isZero() ? "--:--" : formatDuration(song.duration()));
        this.progressSeekBar.setDisable(true);
        this.progressSeekBar.setTotalDuration(song.duration());
        this.progressSeekBar.reset();
        this.nowPlayingDrawerView.setSong(song);
        this.miniPlayerStage.setSong(song);
        this.desktopLyricStage.setSong(song);
        this.desktopLyricStage.updatePlaybackPosition(Duration.ZERO);
        this.systemTrayBridge.updateToolTip(song.title());
        syncPlaybackState();
    }

    private void syncSongSelection() {
        final int songIndex = this.songRows.indexOf(this.currentSong);
        if (songIndex < 0) {
            this.songTable.getSelectionModel().clearSelection();
            return;
        }
        this.songTable.getSelectionModel().clearAndSelect(songIndex);
        this.songTable.scrollTo(songIndex);
    }

    private ContextMenu buildSongContextMenu(final TableRow<SongSummary> row) {
        final MenuItem playItem = createMenuItem("播放", AppGlyphs.PLAY);
        playItem.setOnAction(event -> playSong(row.getItem(), false));

        final MenuItem nextPlayItem = createMenuItem("下一首播放", AppGlyphs.NEXT_QUEUE);
        nextPlayItem.setOnAction(event -> queueNextSong(row.getItem()));

        final MenuItem detailItem = createMenuItem("查看歌词详情", AppGlyphs.LYRICS);
        detailItem.setOnAction(event -> playSong(row.getItem(), true));

        final MenuItem copyLinkItem = createMenuItem("复制链接", AppGlyphs.LINK);
        copyLinkItem.setOnAction(event -> copySongLink(row.getItem()));

        final MenuItem openFolderItem = createMenuItem("打开文件所在目录", AppGlyphs.FOLDER);
        openFolderItem.setOnAction(event -> openSongInFileManager(row.getItem()));

        final MenuItem deleteItem = createMenuItem("从磁盘中删除", AppGlyphs.DELETE);
        deleteItem.setOnAction(event -> deleteSongFromDisk(row.getItem()));

        final MenuItem copyItem = createMenuItem("复制歌曲信息", AppGlyphs.SONG_INFO);
        copyItem.setOnAction(event -> copySongInfo(row.getItem()));

        final MenuItem copyLyricsItem = createMenuItem("复制歌词", AppGlyphs.LYRICS);
        copyLyricsItem.setOnAction(event -> copyLyrics(row.getItem()));

        final MenuItem copyCoverItem = createMenuItem("复制当前歌曲封面", AppGlyphs.COVER);
        copyCoverItem.setOnAction(event -> copySongArtwork(row.getItem()));

        final ContextMenu contextMenu = new ContextMenu(
            playItem,
            nextPlayItem,
            detailItem,
            copyLinkItem,
            openFolderItem,
            deleteItem,
            copyItem,
            copyLyricsItem,
            copyCoverItem
        );
        contextMenu.setOnShowing(event -> {
            final SongSummary song = row.getItem();
            final boolean localSong = this.localLibraryService.isLocalSong(song);
            openFolderItem.setDisable(!localSong);
            deleteItem.setDisable(!localSong || song == null || song.equals(this.currentSong));
            copyLinkItem.setDisable(song == null || resolveSongShareLink(song).isBlank());
        });
        row.emptyProperty().addListener((observable, oldValue, empty) -> row.setContextMenu(empty ? null : contextMenu));
        return contextMenu;
    }

    private void copySongInfo(final SongSummary song) {
        if (song == null) {
            return;
        }
        this.clipboardContent.clear();
        this.clipboardContent.putString(song.title() + " - " + song.artist() + " - " + song.album());
        this.clipboard.setContent(this.clipboardContent);
        showInformation("复制成功", "已复制歌曲信息。");
    }

    private void copyLyrics(final SongSummary song) {
        if (song == null || song.lyricLines() == null || song.lyricLines().isEmpty()) {
            return;
        }
        this.clipboardContent.clear();
        this.clipboardContent.putString(
            song.lyricLines().stream()
                .map(lyricLine -> lyricLine.content())
                .collect(Collectors.joining(System.lineSeparator()))
        );
        this.clipboard.setContent(this.clipboardContent);
        showInformation("复制成功", "已复制歌词。");
    }

    private void queueNextSong(final SongSummary song) {
        if (song == null) {
            return;
        }
        this.queuedNextSong = song;
        showInformation("已设置下一首", "下一首将播放：" + song.title());
    }

    private void copySongLink(final SongSummary song) {
        final String link = resolveSongShareLink(song);
        if (link.isBlank()) {
            return;
        }
        this.clipboardContent.clear();
        this.clipboardContent.putString(link);
        this.clipboard.setContent(this.clipboardContent);
        showInformation("复制成功", "已复制歌曲链接。");
    }

    private String resolveSongShareLink(final SongSummary song) {
        if (song == null) {
            return "";
        }
        if (song.mediaSource() != null && !song.mediaSource().isBlank()) {
            return song.mediaSource();
        }
        if (song.isLegacyOnlineSource() && song.sourceId() != null && !song.sourceId().isBlank()) {
            return "https://music.163.com/#/song?id=" + song.sourceId();
        }
        return "";
    }

    private void copySongArtwork(final SongSummary song) {
        final Image artworkImage = song != null && song.equals(this.currentSong)
            ? this.currentTrackArtworkView.getImage()
            : this.artworkImageLoader.loadSongArtwork(song);
        if (artworkImage == null) {
            showWarning("无法复制封面", "当前歌曲没有可用封面。");
            return;
        }
        this.clipboard.clear();
        this.clipboardContent.clear();
        this.clipboardContent.putImage(artworkImage);
        this.clipboard.setContent(this.clipboardContent);
        showInformation("复制成功", "已复制当前歌曲封面。");
    }

    private void copyPlaylistCoverLink() {
        final String coverLink = resolvePlaylistCoverUrl();
        if (coverLink.isBlank()) {
            showWarning("无法复制链接", "当前歌单没有可用的封面链接。");
            return;
        }
        this.clipboardContent.clear();
        this.clipboardContent.putString(coverLink);
        this.clipboard.setContent(this.clipboardContent);
        showInformation("复制成功", "已复制歌单封面链接。");
    }

    private void beginPlaylistCoverExportDrag() {
        final Image coverImage = this.playlistCoverImageView.getImage();
        if (coverImage == null) {
            showWarning("无法拖拽保存", "当前歌单没有可用封面。");
            return;
        }
        try {
            final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(coverImage, null);
            if (bufferedImage == null) {
                showWarning("无法拖拽保存", "当前封面尚未完成加载。");
                return;
            }
            final Path temporaryImagePath = Files.createTempFile(
                sanitizeFileSegment(this.activePlaylist == null ? "wizard-playlist-cover" : this.activePlaylist.title()),
                ".png"
            );
            ImageIO.write(bufferedImage, "png", temporaryImagePath.toFile());
            temporaryImagePath.toFile().deleteOnExit();

            final Dragboard dragboard = this.playlistCover.startDragAndDrop(TransferMode.COPY);
            dragboard.setDragView(coverImage);
            final ClipboardContent dragContent = new ClipboardContent();
            dragContent.putFiles(List.of(temporaryImagePath.toFile()));
            dragboard.setContent(dragContent);
            this.playlistCover.getStyleClass().add("playlist-cover-dragging");
            this.playlistCover.setCursor(Cursor.CLOSED_HAND);
        } catch (IOException exception) {
            showWarning("无法拖拽保存", "导出歌单封面到临时文件时发生异常。");
        }
    }

    private String resolvePlaylistCoverUrl() {
        if (this.activePlaylist != null) {
            if (this.activePlaylist.coverImageUrl() != null && !this.activePlaylist.coverImageUrl().isBlank()) {
                return normalizePlaylistCoverUrl(this.activePlaylist.coverImageUrl());
            }
            if (this.activePlaylist.songs() != null && !this.activePlaylist.songs().isEmpty()) {
                final SongSummary firstSong = this.activePlaylist.songs().get(0);
                if (firstSong.artworkUrl() != null && !firstSong.artworkUrl().isBlank()) {
                    return normalizePlaylistCoverUrl(firstSong.artworkUrl());
                }
            }
        }
        final Image playlistCoverImage = this.playlistCoverImageView.getImage();
        if (playlistCoverImage == null || playlistCoverImage.getUrl() == null || playlistCoverImage.getUrl().isBlank()) {
            return "";
        }
        return normalizePlaylistCoverUrl(playlistCoverImage.getUrl());
    }

    private String normalizePlaylistCoverUrl(final String url) {
        return url == null ? "" : url.replace("?param=300y300", "").trim();
    }

    private String sanitizeFileSegment(final String sourceValue) {
        if (sourceValue == null || sourceValue.isBlank()) {
            return "wizard-playlist-cover";
        }
        final String sanitizedValue = sourceValue.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return sanitizedValue.isBlank() ? "wizard-playlist-cover" : sanitizedValue;
    }

    private void importLocalFiles() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导入本地音乐与歌词");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("音频与歌词", "*.mp3", "*.wav", "*.m4a", "*.flac", "*.aac", "*.pcm", "*.lrc"),
            new FileChooser.ExtensionFilter("音频文件", "*.mp3", "*.wav", "*.m4a", "*.flac", "*.aac", "*.pcm"),
            new FileChooser.ExtensionFilter("歌词文件", "*.lrc")
        );
        final List<java.io.File> files = fileChooser.showOpenMultipleDialog(this.stage);
        if (files == null || files.isEmpty()) {
            return;
        }
        importLocalFiles(files.stream().map(java.io.File::toPath).toList(), true);
    }

    private void importLocalFiles(final List<Path> files, final boolean showFeedback) {
        if (files == null || files.isEmpty()) {
            return;
        }
        final ToastNotifier.LoadingHandle loadingHandle = this.toastNotifier.loading(
            "长时间任务运行中，请耐心等待！",
            "开始复制音乐和 lrc 文件到本地音乐文件夹。"
        );
        CompletableFuture
            .supplyAsync(() -> this.localLibraryService.importFiles(files))
            .thenAccept(importResult -> Platform.runLater(() -> {
                loadingHandle.close();
                if (importResult.musicCount() > 0) {
                    showPlaylist(loadLocalLibrary(), this.localMusicNavButton);
                } else {
                    refreshLocalLibraryView();
                }
                if (importResult.importedCount() > 0) {
                    this.toastNotifier.success(
                        showFeedback ? "文件复制成功！" : "拖拽导入成功！",
                        importResult.musicCount() + " 个音乐文件，"
                            + importResult.lyricCount() + " 个歌词文件。"
                    );
                } else {
                    this.toastNotifier.warn(
                        "未导入任何文件",
                        "跳过 " + importResult.skippedCount() + " 个文件，请检查文件格式或目标目录。"
                    );
                }
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> {
                    loadingHandle.close();
                    this.toastNotifier.fail("导入失败", "复制音乐或歌词文件时发生异常。");
                });
                return null;
            });
    }

    private void downloadCurrentSong() {
        if (this.currentSong == null) {
            showWarning("无法下载", "当前没有选中的歌曲。");
            return;
        }
        if (this.currentSong.isLocalSource()) {
            showWarning("无法下载", "本地音乐已经在磁盘中，不需要重复下载。");
            return;
        }
        if (!this.currentSong.isLegacyOnlineSource()) {
            showWarning("无法下载", "当前歌曲来源不支持 legacy 下载。");
            return;
        }
        final SongSummary downloadSong = this.currentSong;
        final ToastNotifier.LoadingHandle loadingHandle = this.toastNotifier.loading(
            "开始下载音乐",
            "这过程需要一些时间，请耐心等待。"
        );
        CompletableFuture
            .supplyAsync(() -> this.legacyOnlineMusicService.downloadSong(
                downloadSong,
                this.localLibraryService.localMusicDirectory(),
                this.localLibraryService.localLyricDirectory()
            ))
            .thenAccept(downloadResult -> Platform.runLater(() -> {
                loadingHandle.close();
                if (downloadResult.success()) {
                    refreshLocalLibraryView();
                    this.toastNotifier.success(downloadSong.title(), downloadResult.message());
                } else {
                    this.toastNotifier.fail("下载失败", downloadResult.message());
                }
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> {
                    loadingHandle.close();
                    this.toastNotifier.fail("下载失败", "legacy 下载流程执行失败。");
                });
                return null;
            });
    }

    private void refreshLocalLibrary() {
        refreshLocalLibraryView();
        if (isLocalPlaylistActive()) {
            showInformation("本地音乐已刷新", "已重新扫描 ./LocalMusic/Music 与 ./LocalMusic/Lrc。");
        }
    }

    private void refreshLocalLibraryView() {
        final FxSampleData.PlaylistDetail localLibrary = loadLocalLibrary();
        if (isLocalPlaylistActive()) {
            showPlaylist(localLibrary, this.localMusicNavButton);
        }
    }

    private boolean isLocalPlaylistActive() {
        return this.activePlaylist != null && "本地音乐".equals(this.activePlaylist.title());
    }

    private void deleteSongFromDisk(final SongSummary song) {
        if (song == null || !this.localLibraryService.isLocalSong(song)) {
            return;
        }
        if (song.equals(this.currentSong)) {
            showWarning("无法删除", "当前正在使用的歌曲不能直接删除，请先切到其他歌曲。");
            return;
        }
        if (!confirmAction("删除音乐", "是否从磁盘彻底删除以下文件？\n" + song.title())) {
            return;
        }
        if (this.localLibraryService.deleteSong(song)) {
            refreshLocalLibraryView();
            showInformation("成功删除", "音乐文件名：" + song.title());
        } else {
            showWarning("删除失败", "未能删除该音乐文件或对应歌词文件。");
        }
    }

    private void openLocalMusicDirectory() {
        openPathInFileManager(this.localLibraryService.localLibraryRoot(), false);
    }

    private void openSongInFileManager(final SongSummary song) {
        final Path songPath = resolveSongPath(song);
        if (songPath == null || !Files.exists(songPath)) {
            return;
        }
        openPathInFileManager(songPath, true);
    }

    private void showQuickActionsMenu() {
        final ContextMenu quickMenu = new ContextMenu();
        final MenuItem importItem = createMenuItem("导入本地音乐", AppGlyphs.FOLDER);
        importItem.setOnAction(event -> importLocalFiles());

        final MenuItem downloadItem = createMenuItem("下载当前音乐", AppGlyphs.LINK);
        downloadItem.setOnAction(event -> downloadCurrentSong());

        final MenuItem refreshItem = createMenuItem("刷新本地音乐", AppGlyphs.SEARCH);
        refreshItem.setOnAction(event -> refreshLocalLibrary());

        final MenuItem openFolderItem = createMenuItem("打开本地音乐目录", AppGlyphs.FOLDER);
        openFolderItem.setOnAction(event -> openLocalMusicDirectory());

        final MenuItem miniItem = createMenuItem("进入迷你模式", AppGlyphs.MINI_MODE);
        miniItem.setOnAction(event -> enterMiniMode());

        final MenuItem drawerItem = createMenuItem("打开歌词抽屉", AppGlyphs.LYRICS);
        drawerItem.setOnAction(event -> this.drawer.open());

        final MenuItem exitItem = createMenuItem("退出程序", AppGlyphs.CLOSE);
        exitItem.setOnAction(event -> requestExit());

        quickMenu.getItems().addAll(importItem, downloadItem, refreshItem, openFolderItem, miniItem, drawerItem, exitItem);
        quickMenu.show(this.moreActionsButton, Side.TOP, 0.0, 8.0);
    }

    private void installLocalImportDragAndDrop(final Node dragTarget) {
        dragTarget.setOnDragOver(event -> {
            if (hasImportableFiles(event.getDragboard())) {
                event.acceptTransferModes(TransferMode.COPY);
                this.sidebar.getStyleClass().add("sidebar-import-drag-over");
            }
            event.consume();
        });
        dragTarget.setOnDragExited(event -> {
            this.sidebar.getStyleClass().remove("sidebar-import-drag-over");
            event.consume();
        });
        dragTarget.setOnDragDropped(event -> {
            this.sidebar.getStyleClass().remove("sidebar-import-drag-over");
            final Dragboard dragboard = event.getDragboard();
            if (hasImportableFiles(dragboard)) {
                importLocalFiles(dragboard.getFiles().stream().map(java.io.File::toPath).toList(), false);
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });
    }

    private boolean hasImportableFiles(final Dragboard dragboard) {
        return dragboard != null
            && dragboard.hasFiles()
            && dragboard.getFiles().stream().anyMatch(file -> isImportablePath(file.toPath()));
    }

    private boolean isImportablePath(final Path filePath) {
        final String lowerCaseName = filePath.getFileName().toString().toLowerCase(Locale.ROOT);
        return lowerCaseName.endsWith(".lrc")
            || lowerCaseName.endsWith(".mp3")
            || lowerCaseName.endsWith(".wav")
            || lowerCaseName.endsWith(".m4a")
            || lowerCaseName.endsWith(".flac")
            || lowerCaseName.endsWith(".aac")
            || lowerCaseName.endsWith(".pcm");
    }

    private Button createNavigationButton(final String text) {
        final Button button = new Button(text);
        button.getStyleClass().add("nav-item");
        button.setMaxWidth(Double.MAX_VALUE);
        MaterialButtonFeedback.install(button);
        this.navigationButtons.add(button);
        return button;
    }

    private Node createSidebarAvatar() {
        final Image image = loadImageOrNull("/images/topandbottom/user.jpg");
        if (image != null) {
            final ImageView avatar = new ImageView(image);
            avatar.setFitWidth(64.0);
            avatar.setFitHeight(64.0);
            final Circle avatarClip = new Circle(32.0, 32.0, 32.0);
            avatar.setClip(avatarClip);
            return avatar;
        }

        final Label fallbackAvatar = new Label("W");
        fallbackAvatar.getStyleClass().add("profile-avatar");
        return fallbackAvatar;
    }

    private Image loadImageOrNull(final String resourcePath) {
        try (InputStream inputStream = PlayerShellView.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                return null;
            }
            return new Image(inputStream);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void setActiveNavigation(final Button activeButton) {
        for (final Button button : this.navigationButtons) {
            button.getStyleClass().remove("nav-item-active");
        }
        activeButton.getStyleClass().add("nav-item-active");
    }

    private Button roundControl(final String text) {
        final Button button = new Button(text);
        button.getStyleClass().add("round-control");
        MaterialButtonFeedback.install(button);
        return button;
    }

    private Button toolbarActionButton(final String text, final Runnable action) {
        final Button button = new Button(text);
        button.getStyleClass().add("playlist-toolbar-button");
        button.setOnAction(event -> action.run());
        MaterialButtonFeedback.install(button);
        return button;
    }

    private HBox metadataRow(final String glyphPath, final Label contentLabel) {
        final HBox row = new HBox(8.0);
        row.setAlignment(Pos.TOP_LEFT);
        final StackPane icon = new StackPane(SvgIconFactory.createIcon(glyphPath, 14.0, Color.web("#8c8c8c")));
        icon.getStyleClass().add("playlist-meta-icon");
        row.getChildren().addAll(icon, contentLabel);
        return row;
    }

    private Button topIconButton(final Node graphic, final Runnable action) {
        final Button button = new Button();
        button.getStyleClass().add("top-icon-button");
        button.setGraphic(graphic);
        button.setOnAction(event -> action.run());
        MaterialButtonFeedback.install(button);
        return button;
    }

    private Button windowButton(final String text, final Runnable action) {
        final Button button = new Button(text);
        button.getStyleClass().add("window-button");
        button.setOnAction(event -> action.run());
        MaterialButtonFeedback.install(button);
        return button;
    }

    private void toggleMaximized() {
        if (!this.windowMaximized) {
            this.restoredStageX = this.stage.getX();
            this.restoredStageY = this.stage.getY();
            this.restoredStageWidth = this.stage.getWidth();
            this.restoredStageHeight = this.stage.getHeight();
            final Rectangle2D visualBounds = resolveStageVisualBounds();
            this.stage.setX(visualBounds.getMinX());
            this.stage.setY(visualBounds.getMinY());
            this.stage.setWidth(visualBounds.getWidth());
            this.stage.setHeight(visualBounds.getHeight());
        } else {
            this.stage.setX(this.restoredStageX);
            this.stage.setY(this.restoredStageY);
            this.stage.setWidth(this.restoredStageWidth);
            this.stage.setHeight(this.restoredStageHeight);
        }
        this.windowMaximized = !this.windowMaximized;
    }

    private void applyTooltip(final Button button, final String text) {
        button.setTooltip(new Tooltip(text));
    }

    private MenuItem createMenuItem(final String text, final String glyphPath) {
        return new MenuItem(text, SvgIconFactory.createIcon(glyphPath, 15.0, Color.web("#23314f")));
    }

    private void configureVolumeControls() {
        this.volumeSlider.setValue(50.0);
        this.volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            this.playbackService.setVolume(newValue.doubleValue() / 100.0);
            updateVolumeButtonIcon();
        });

        final StackPane sliderWrapper = new StackPane(this.volumeSlider);
        sliderWrapper.getStyleClass().add("volume-slider-wrapper");
        final StackPane popupSurface = new StackPane(sliderWrapper);
        popupSurface.getStyleClass().add("volume-popup-surface");
        popupSurface.getStylesheets().add(
            Objects.requireNonNull(PlayerShellView.class.getResource("/css/player-shell.css")).toExternalForm()
        );
        this.volumePopup.getContent().setAll(popupSurface);
        this.volumePopup.setAutoHide(true);
        this.volumePopup.setAutoFix(true);
        this.volumePopup.setHideOnEscape(true);
        this.playbackService.setVolume(0.5);
        updateVolumeButtonIcon();
    }

    private void toggleVolumePopup() {
        if (this.volumePopup.isShowing()) {
            this.volumePopup.hide();
            return;
        }
        if (this.volumePopup.getContent().isEmpty()) {
            return;
        }
        final Node popupContent = this.volumePopup.getContent().get(0);
        popupContent.applyCss();
        popupContent.autosize();
        final double popupWidth = popupContent instanceof Region region
            ? region.prefWidth(-1.0)
            : popupContent.getLayoutBounds().getWidth();
        final double popupHeight = popupContent instanceof Region region
            ? region.prefHeight(-1.0)
            : popupContent.getLayoutBounds().getHeight();
        final Bounds buttonBounds = this.volumeButton.localToScreen(this.volumeButton.getBoundsInLocal());
        if (buttonBounds == null) {
            return;
        }
        final Rectangle2D visualBounds = Screen.getScreensForRectangle(
            buttonBounds.getMinX(),
            buttonBounds.getMinY(),
            buttonBounds.getWidth(),
            buttonBounds.getHeight()
        ).stream().findFirst().orElseGet(Screen::getPrimary).getVisualBounds();
        final double popupX = clamp(
            buttonBounds.getMinX() + ((buttonBounds.getWidth() - popupWidth) / 2.0),
            visualBounds.getMinX() + 8.0,
            visualBounds.getMaxX() - popupWidth - 8.0
        );
        final double popupY = clamp(
            buttonBounds.getMinY() - popupHeight - 10.0,
            visualBounds.getMinY() + 8.0,
            visualBounds.getMaxY() - popupHeight - 8.0
        );
        this.volumePopup.show(this.stage, popupX, popupY);
    }

    private void updateVolumeButtonIcon() {
        final String glyphPath = this.volumeSlider.getValue() <= 0.0 ? AppGlyphs.VOLUME_MUTE : AppGlyphs.VOLUME;
        this.volumeButton.setGraphic(SvgIconFactory.createIcon(glyphPath, 14.0, Color.WHITE));
    }

    private Rectangle createRoundedClip(final double width, final double height, final double arcSize) {
        final Rectangle rectangle = new Rectangle(width, height);
        rectangle.setArcWidth(arcSize);
        rectangle.setArcHeight(arcSize);
        return rectangle;
    }

    private void openAboutDialog() {
        LegacyDialogSupport.showAbout(this.stage);
    }

    private void enableWindowDrag(final Node dragHandle) {
        dragHandle.setOnMousePressed(event -> {
            this.dragOffsetX = event.getSceneX();
            this.dragOffsetY = event.getSceneY();
        });
        dragHandle.setOnMouseDragged(event -> {
            if (this.windowMaximized) {
                return;
            }
            this.stage.setX(event.getScreenX() - this.dragOffsetX);
            this.stage.setY(event.getScreenY() - this.dragOffsetY);
        });
    }

    public void handlePrimaryStageCloseRequest(final WindowEvent event) {
        event.consume();
        if (!this.exiting) {
            requestExit();
        }
    }

    public void shutdown() {
        if (this.disposed) {
            return;
        }
        this.disposed = true;
        this.systemTrayBridge.remove();
        this.miniPlayerStage.hide();
        this.desktopLyricStage.hide();
        this.toastNotifier.destroy();
        this.playbackService.dispose();
    }

    private void requestExit() {
        this.exiting = true;
        shutdown();
        javafx.application.Platform.exit();
        System.exit(0);
    }

    private void enterMiniMode() {
        this.miniPlayerStage.showNearWindowEdge(this.stage);
        this.miniPlayerStage.toFront();
        final javafx.animation.PauseTransition hideTransition =
            new javafx.animation.PauseTransition(javafx.util.Duration.millis(120.0));
        hideTransition.setOnFinished(event -> {
            if (this.miniPlayerStage.isShowing()) {
                this.stage.hide();
            }
        });
        hideTransition.play();
    }

    private void restoreFromMiniMode() {
        this.miniPlayerStage.hide();
        showMainWindow();
    }

    private void toggleDesktopLyric(final Stage anchorStage) {
        this.desktopLyricStage.toggleVisibleBelow(anchorStage);
    }

    private void toggleDesktopLyricFromMiniMode() {
        this.desktopLyricStage.toggleVisible();
    }

    private void showMainWindow() {
        this.miniPlayerStage.hide();
        if (!this.stage.isShowing()) {
            this.stage.show();
        }
        this.stage.toFront();
        this.stage.requestFocus();
    }

    private boolean confirmAction(final String title, final String content) {
        return LegacyDialogSupport.showConfirm(this.stage, title, content);
    }

    private void showInformation(final String title, final String content) {
        this.toastNotifier.success(title, content);
    }

    private void showWarning(final String title, final String content) {
        this.toastNotifier.warn(title, content);
    }

    private String formatDuration(final java.time.Duration duration) {
        final long minutes = duration.toMinutes();
        final long seconds = duration.minus(minutes, ChronoUnit.MINUTES).toSeconds();
        return "%02d:%02d".formatted(minutes, seconds);
    }

    private void syncPlaybackState() {
        updatePlaybackModeButton();
        setRoundButtonGraphic(this.playPauseButton, this.playbackActive ? AppGlyphs.PAUSE : AppGlyphs.PLAY, 16.0);
        this.nowPlayingDrawerView.setPlaybackActive(this.playbackActive);
        this.miniPlayerStage.setPlaybackActive(this.playbackActive);
        this.desktopLyricStage.setPlaybackActive(this.playbackActive);
        this.systemTrayBridge.updateToolTip(this.currentSong == null ? null : this.currentSong.title());
    }

    private void togglePlayback() {
        if (this.playbackService.isPlaying()) {
            this.playbackService.pause();
            return;
        }
        if (this.playbackService.hasLoadedSong() && this.playbackService.isCurrentSong(this.currentSong)) {
            this.playbackService.resume();
            return;
        }
        playSong(this.currentSong, false);
    }

    private void cyclePlaybackMode() {
        this.playbackMode = switch (this.playbackMode) {
            case LIST_LOOP -> PlaybackMode.ORDER_PLAY;
            case ORDER_PLAY -> PlaybackMode.SINGLE_LOOP;
            case SINGLE_LOOP -> PlaybackMode.LIST_LOOP;
        };
        updatePlaybackModeButton();
    }

    private void updatePlaybackModeButton() {
        switch (this.playbackMode) {
            case LIST_LOOP -> {
                setRoundButtonGraphic(this.playbackModeButton, AppGlyphs.PLAY_MODE_REPEAT, 17.0);
                applyTooltip(this.playbackModeButton, "列表循环");
            }
            case ORDER_PLAY -> {
                setRoundButtonGraphic(this.playbackModeButton, AppGlyphs.PLAY_MODE_ORDER, 17.0);
                applyTooltip(this.playbackModeButton, "顺序播放");
            }
            case SINGLE_LOOP -> {
                setRoundButtonGraphic(this.playbackModeButton, AppGlyphs.PLAY_MODE_SINGLE, 17.0);
                applyTooltip(this.playbackModeButton, "单曲循环");
            }
            default -> {
            }
        }
    }

    private void playNextOnCompletion() {
        if (this.songRows.isEmpty()) {
            return;
        }
        if (this.queuedNextSong != null) {
            final SongSummary queuedSong = this.queuedNextSong;
            this.queuedNextSong = null;
            playSong(queuedSong, false);
            return;
        }
        switch (this.playbackMode) {
            case SINGLE_LOOP -> playSong(this.currentSong, false);
            case ORDER_PLAY -> {
                final int nextIndex = resolveCurrentPlaylistIndex() + 1;
                if (nextIndex >= this.songRows.size()) {
                    this.playbackService.stop();
                    return;
                }
                playSong(this.songRows.get(nextIndex), false);
            }
            case LIST_LOOP -> playRelativeSong(1);
            default -> {
            }
        }
    }

    private void setRoundButtonGraphic(final Button button, final String glyphPath, final double size) {
        button.setText("");
        button.setGraphic(SvgIconFactory.createIcon(glyphPath, size, Color.WHITE));
    }

    private void rebindDiscoverCardWidths() {
        for (final StackPane discoverCard : this.discoverCards) {
            bindDiscoverCardWidth(discoverCard);
        }
    }

    private void refreshDiscoverBatchSizeIfNeeded() {
        final int resolvedBatchSize = resolveDiscoverBatchSize();
        if (resolvedBatchSize == this.discoverVisibleBatchSize) {
            return;
        }
        this.discoverVisibleBatchSize = resolvedBatchSize;
        rebuildDiscoverCards();
    }

    private List<FxSampleData.PlaylistDetail> ensureMinimumDiscoverPlaylists(final List<FxSampleData.PlaylistDetail> onlinePlaylists) {
        final int minimumTarget = Math.max(8, resolveDiscoverBatchSize());
        if (onlinePlaylists.size() >= minimumTarget) {
            return onlinePlaylists;
        }
        final List<FxSampleData.PlaylistDetail> mergedPlaylists = new ArrayList<>(onlinePlaylists);
        for (final FxSampleData.PlaylistDetail fallbackDetail : FxSampleData.playlistDetails()) {
            final boolean alreadyPresent = mergedPlaylists.stream()
                .anyMatch(detail -> Objects.equals(detail.sourceId(), fallbackDetail.sourceId())
                    || Objects.equals(detail.title(), fallbackDetail.title()));
            if (alreadyPresent) {
                continue;
            }
            mergedPlaylists.add(fallbackDetail);
            if (mergedPlaylists.size() >= minimumTarget) {
                break;
            }
        }
        return mergedPlaylists;
    }

    private void bindDiscoverCardWidth(final StackPane discoverCard) {
        if (discoverCard == null || this.responsiveSceneWidthProperty == null) {
            return;
        }
        if (discoverCard.prefWidthProperty().isBound()) {
            discoverCard.prefWidthProperty().unbind();
        }
        if (discoverCard.minWidthProperty().isBound()) {
            discoverCard.minWidthProperty().unbind();
        }
        if (discoverCard.maxWidthProperty().isBound()) {
            discoverCard.maxWidthProperty().unbind();
        }
        discoverCard.prefWidthProperty().bind(
            Bindings.createDoubleBinding(
                () -> calculateDiscoverCardWidth(this.responsiveSceneWidthProperty.get()),
                this.responsiveSceneWidthProperty
            )
        );
        discoverCard.minWidthProperty().bind(discoverCard.prefWidthProperty());
        discoverCard.maxWidthProperty().bind(discoverCard.prefWidthProperty());
    }

    private double calculateDiscoverCardWidth(final double sceneWidth) {
        final double sidebarWidth = clamp(sceneWidth * 0.18, 208.0, 248.0);
        final double contentWidth = Math.max(560.0, sceneWidth - sidebarWidth - 112.0);
        if (contentWidth >= 1050.0) {
            return 214.0;
        }
        if (contentWidth >= 820.0) {
            return 198.0;
        }
        return Math.max(182.0, (contentWidth - 18.0) / 2.0);
    }

    private double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    private Rectangle2D resolveStageVisualBounds() {
        return Screen.getScreensForRectangle(
            this.stage.getX(),
            this.stage.getY(),
            Math.max(this.stage.getWidth(), 1.0),
            Math.max(this.stage.getHeight(), 1.0)
        ).stream().findFirst().orElseGet(Screen::getPrimary).getVisualBounds();
    }

    private Path resolveSongPath(final SongSummary song) {
        return this.localLibraryService.resolveSongPath(song);
    }

    private void openPathInFileManager(final Path path, final boolean revealFile) {
        if (path == null) {
            return;
        }
        try {
            final String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            if (osName.contains("win")) {
                final String command = revealFile
                    ? "explorer /select,\"" + path.toAbsolutePath() + "\""
                    : "explorer \"" + path.toAbsolutePath() + "\"";
                Runtime.getRuntime().exec(command);
                return;
            }
            if (osName.contains("mac")) {
                final String command = revealFile
                    ? "open -R \"" + path.toAbsolutePath() + "\""
                    : "open \"" + path.toAbsolutePath() + "\"";
                Runtime.getRuntime().exec(command);
                return;
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(revealFile ? path.getParent().toFile() : path.toFile());
            }
        } catch (IOException ignored) {
        }
    }

    private void configureProgressSeekBar() {
        if (this.progressSeekBar.getProperties().containsKey("configured")) {
            return;
        }
        this.progressSeekBar.getProperties().put("configured", Boolean.TRUE);
        this.progressSeekBar.setDisable(true);
        this.progressSeekBar.setSeekHandler(this.playbackService::seek);
    }

    private void configurePlaybackCallbacks() {
        this.playbackService.setPlaybackListener(new AudioPlaybackService.PlaybackListener() {
            @Override
            public void onPlaybackReady(final Duration totalDuration) {
                progressSeekBar.setDisable(false);
                if (!totalDuration.isZero()) {
                    rightTimeLabel.setText(formatDuration(totalDuration));
                    progressSeekBar.setTotalDuration(totalDuration);
                    nowPlayingDrawerView.updateTotalDuration(totalDuration);
                }
            }

            @Override
            public void onPlaybackTimeChanged(final Duration currentTime, final Duration totalDuration) {
                leftTimeLabel.setText(formatDuration(currentTime));
                if (!progressSeekBar.isDragInFlight()) {
                    progressSeekBar.setCurrentTime(currentTime);
                }
                if (!totalDuration.isZero()) {
                    rightTimeLabel.setText(formatDuration(totalDuration));
                    progressSeekBar.setTotalDuration(totalDuration);
                    nowPlayingDrawerView.updateTotalDuration(totalDuration);
                }
                nowPlayingDrawerView.updatePlaybackPosition(currentTime);
                desktopLyricStage.updatePlaybackPosition(currentTime);
            }

            @Override
            public void onPlaybackStateChanged(final boolean playing) {
                playbackActive = playing;
                syncPlaybackState();
            }

            @Override
            public void onPlaybackFinished() {
                playNextOnCompletion();
            }

            @Override
            public void onPlaybackFailed(final String message) {
                playbackActive = false;
                leftTimeLabel.setText("00:00");
                progressSeekBar.setDisable(true);
                progressSeekBar.reset();
                syncPlaybackState();
                if (!isBlank(message)) {
                    showWarning("播放失败", message);
                }
            }
        });
    }

    private boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }

    private enum PlaybackMode {
        LIST_LOOP,
        ORDER_PLAY,
        SINGLE_LOOP
    }
}
