package com.aquarius.wizard.player.fx.ui;

import com.aquarius.wizard.player.model.LyricLine;
import com.aquarius.wizard.player.model.PlaylistSummary;
import com.aquarius.wizard.player.model.SongSummary;

import java.time.Duration;
import java.util.List;

/**
 * Demo-only data used during the first migration stage.
 *
 * <p>The current goal is to let the rebuilt JavaFX shell reproduce the old
 * project flow before the Spring Boot backend takes over.</p>
 */
public final class FxSampleData {

    private static final String DEMO_AUDIO_SOURCE = resolveDemoAudioSource();

    private FxSampleData() {
    }

    /**
     * Lightweight playlist detail used by the rebuilt desktop shell.
     *
     * @param title       playlist title
     * @param tags        short playlist tags
     * @param description playlist description
     * @param accentColor accent color used by the card and cover
     * @param coverMark   short cover text shown in the mock cover
     * @param songs       songs rendered in the playlist table
     * @param sourceType  playlist source type
     * @param sourceId    source-specific identifier
     * @param coverImageUrl optional remote cover url
     */
    public record PlaylistDetail(
        String title,
        String tags,
        String description,
        String accentColor,
        String coverMark,
        List<SongSummary> songs,
        String sourceType,
        String sourceId,
        String coverImageUrl
    ) {

        public PlaylistDetail withSongs(final List<SongSummary> updatedSongs) {
            return new PlaylistDetail(
                this.title,
                this.tags,
                this.description,
                this.accentColor,
                this.coverMark,
                updatedSongs,
                this.sourceType,
                this.sourceId,
                this.coverImageUrl
            );
        }

        public boolean isBackendCompatPlaylist() {
            return "backend-compat".equals(this.sourceType) || "legacy-online".equals(this.sourceType);
        }

        public boolean isBackendCompatSearchPlaylist() {
            return "backend-compat-search".equals(this.sourceType) || "legacy-online-search".equals(this.sourceType);
        }
    }

    public static List<PlaylistSummary> playlists() {
        return playlistDetails().stream()
            .map(detail -> new PlaylistSummary(detail.title(), detail.description(), detail.accentColor()))
            .toList();
    }

    public static List<PlaylistDetail> playlistDetails() {
        return List.of(
            playlist(
                "深夜编码室",
                "电子 / 专注 / 女声",
                "对应旧版的“发现音乐”歌单卡片，先把列表和切页流程迁进新 UI。",
                "#4d8fff",
                "夜",
                List.of(
                    song("Blue Horizon", "Studio River", "Night Sketches", 3, 24, "#4fa7ff"),
                    song("Late Commit", "Merge Lane", "Build Stories", 4, 12, "#5db7ff"),
                    song("Run After Test", "Static Bloom", "Quiet Runner", 3, 48, "#6d96ff"),
                    song("Light On Terminal", "Neon Harbor", "After Hours", 4, 2, "#7da8ff"),
                    song("Echo In Tabs", "Paper Coast", "Night Switch", 3, 17, "#5f8eff")
                )
            ),
            playlist(
                "玻璃城市",
                "流行 / 通勤 / 氛围",
                "更接近旧版随机推荐歌单的卡片区效果，强调封面、标题和点击跳转。",
                "#ff8d7a",
                "城",
                List.of(
                    song("Glass Avenue", "Hikari Pulse", "Metro Bloom", 3, 55, "#ff8d7a"),
                    song("Say Yes", "Punch", "Dorm Window", 3, 24, "#ff947e"),
                    song("Sunset Cursor", "Velvet Syntax", "Metro Bloom", 4, 5, "#ff7f67"),
                    song("Cloud Delay", "Night Diner", "Static View", 3, 11, "#ff9c73"),
                    song("Rain On Neon", "Paper Taxi", "Glass City", 4, 8, "#ff9172")
                )
            ),
            playlist(
                "唱片星期天",
                "黑胶 / Lo-Fi / 轻复古",
                "给抽屉页和唱片视觉留出风格上的连续性，不让主界面和详情页断层。",
                "#8d78ff",
                "盘",
                List.of(
                    song("Vinyl Sunday", "Amber Needle", "Soft Rotation", 3, 46, "#8d78ff"),
                    song("Dust On Groove", "Loft Echo", "Needle Light", 4, 4, "#917fff"),
                    song("Warm Stylus", "Record Bloom", "Soft Rotation", 3, 31, "#9d85ff"),
                    song("Needle Glow", "Clock Tape", "Quiet Turn", 4, 15, "#7d72ff"),
                    song("Paper Sleeve", "Room Static", "Needle Light", 2, 58, "#8a74ff")
                )
            ),
            playlist(
                "蓝色动线",
                "女声 / 电子 / 明亮",
                "承接旧版主界面的蓝色主基调，保持推荐卡片区的辨识度。",
                "#2dc8aa",
                "蓝",
                List.of(
                    song("Blue Motion", "Nova Frame", "Signal Dance", 3, 38, "#2dc8aa"),
                    song("Clean Orbit", "Mira Tone", "Signal Dance", 3, 50, "#2fc4a6"),
                    song("Soft Reactor", "Coastline", "Bright Mode", 3, 40, "#23ba9d"),
                    song("Paper Satellite", "Neon Frame", "Bright Mode", 4, 1, "#2ab7a0"),
                    song("Pulse Marker", "Juniper", "Signal Dance", 3, 7, "#35c8b1")
                )
            ),
            playlist(
                "工作台回放",
                "器乐 / 轻节奏 / 专注",
                "对应旧版切歌、双击表格播放等工作流，先把歌单列表页架起来。",
                "#ffc95c",
                "工",
                List.of(
                    song("Studio Replay", "Minor Loop", "Desk Session", 3, 15, "#ffc95c"),
                    song("Parallel Build", "North Tab", "Desk Session", 4, 13, "#f7c04b"),
                    song("Compile Sunrise", "Breeze Key", "Desk Session", 2, 59, "#ffce68"),
                    song("Shadow Workspace", "Copper Hour", "Long Task", 3, 44, "#ffb84f"),
                    song("Trace Window", "Silver Ink", "Long Task", 3, 36, "#ffd16d")
                )
            ),
            playlist(
                "粉色余温",
                "抒情 / 氛围 / 夜色",
                "用于补足旧版推荐区的色彩变化，让主界面不至于一片同色。",
                "#ff6d98",
                "粉",
                List.of(
                    song("Pink Outline", "Lune Harbor", "Warm Fade", 3, 42, "#ff6d98"),
                    song("Afterglow Tab", "Velvet Port", "Warm Fade", 4, 9, "#ff7aa2"),
                    song("Quiet Balcony", "Rose Frame", "Evening Deck", 3, 53, "#ff5f92"),
                    song("Falling Cursor", "Paper Bloom", "Evening Deck", 3, 29, "#ff82a8"),
                    song("Night Window", "Milk Route", "Warm Fade", 4, 18, "#ff709d")
                )
            ),
            playlist(
                "午后磁带",
                "复古 / 午后 / 轻松",
                "补齐旧版发现区“换一组”的轮播语义，让推荐歌单不再只有一屏静态样例。",
                "#ffb86c",
                "午",
                List.of(
                    song("Amber Tape", "Slow Ferry", "Tea Window", 3, 18, "#ffb86c"),
                    song("Sunroom Cassette", "Velvet Dust", "Tea Window", 4, 2, "#ffbe75"),
                    song("Half Day Replay", "Mellow Crane", "Paper Sofa", 3, 31, "#ffb15f"),
                    song("Quiet Lemon", "North Porch", "Paper Sofa", 3, 44, "#ffba70"),
                    song("Satin Clock", "Peach Harbor", "Tea Window", 4, 10, "#ffb76b")
                )
            ),
            playlist(
                "霓虹通道",
                "夜跑 / 合成器 / 城市",
                "承接旧版推荐区里偏赛博感和夜色感的封面气质，方便测试卡片刷新和切页。",
                "#49d2ff",
                "霓",
                List.of(
                    song("Neon Corridor", "Metro Pulse", "Night Transit", 3, 36, "#49d2ff"),
                    song("Signal Runner", "Blue Meter", "Night Transit", 4, 6, "#53d8ff"),
                    song("Late Platform", "Chrome Wind", "Glass Track", 3, 27, "#3dc7f5"),
                    song("Street Echo", "Tunnel Frame", "Glass Track", 3, 54, "#45d1fb"),
                    song("Electric Rain", "North Signal", "Night Transit", 4, 13, "#5ad7ff")
                )
            ),
            playlist(
                "夜色写字楼",
                "钢琴 / 都市 / 叙事",
                "补一组更偏旧版“封面下短文案”的歌单，让发现页在淡色主界面里层次更完整。",
                "#7d8dff",
                "楼",
                List.of(
                    song("Office Lights", "Silver Hour", "After Five", 3, 41, "#7d8dff"),
                    song("Lift Hall", "Window Route", "After Five", 3, 59, "#8394ff"),
                    song("Paper Badge", "Calm Story", "Desk Shadow", 4, 3, "#7081fa"),
                    song("Night Elevator", "Minor Avenue", "Desk Shadow", 3, 21, "#8795ff"),
                    song("Last Desk Lamp", "Quiet Mile", "After Five", 4, 9, "#7c8cff")
                )
            ),
            playlist(
                "雨幕胶片",
                "电影感 / 女声 / 阴天",
                "用于补足旧版封面图更强的氛围感，方便后续把真实封面迁进来时做对照。",
                "#5e8cff",
                "雨",
                List.of(
                    song("Rain Frame", "Cinema Vale", "Window Season", 3, 33, "#5e8cff"),
                    song("After The Glass", "Mina Shore", "Window Season", 4, 11, "#6893ff"),
                    song("Wet Letter", "Blue Screen", "Cloud Reel", 3, 25, "#547eff"),
                    song("Street Reflection", "North Bloom", "Cloud Reel", 3, 52, "#5d87ff"),
                    song("Slow Lantern", "Velvet Rain", "Window Season", 4, 5, "#628dff")
                )
            ),
            playlist(
                "海岸复写",
                "器乐 / 清晨 / 放空",
                "继续扩大发现区样本量，确保刷新、搜索、点卡片进歌单这些链路都能连续验证。",
                "#4dc7c2",
                "海",
                List.of(
                    song("Coast Rewrite", "Morning Draft", "Open Tide", 3, 14, "#4dc7c2"),
                    song("Salt Notes", "Harbor Ink", "Open Tide", 3, 48, "#51cfc9"),
                    song("Wave Margin", "Pale Ferry", "Quiet Shore", 4, 4, "#46bfb9"),
                    song("Blue Current", "Drift Field", "Quiet Shore", 3, 37, "#59cdc7"),
                    song("Pier Window", "North Foam", "Open Tide", 4, 1, "#4ac5c0")
                )
            )
        );
    }

    public static PlaylistDetail localLibrary() {
        return playlist(
            "本地音乐",
            "LocalMusic / 相对路径",
            "对应旧版左侧“本地音乐”入口，后续会先接旧扫描逻辑，再切到 Spring Boot 4。",
            "#5a8dff",
            "本",
            List.of(
                song("Folder Echo", "Disk Reader", "Local Session", 3, 26, "#5a8dff"),
                song("Relative Path", "Byte Harbor", "Local Session", 4, 7, "#648dff"),
                song("Tag Reader", "Jaudio Trace", "Meta Line", 3, 19, "#6f94ff"),
                song("Cover Cache", "Frame Layer", "Meta Line", 3, 57, "#5386ff"),
                song("Library Scan", "Node Drift", "Meta Line", 2, 55, "#7297ff")
            )
        );
    }

    public static PlaylistDetail downloadQueue() {
        return playlist(
            "下载队列",
            "Legacy / 迁移占位",
            "对应旧版“下载当前音乐”入口。第一阶段先迁入口和交互，后续再用后端接管。",
            "#ff9d56",
            "下",
            List.of(
                song("Pending Track", "Legacy Source", "Queue One", 3, 12, "#ff9d56"),
                song("Retry Download", "Legacy Source", "Queue One", 4, 1, "#ffa969"),
                song("Cached Cover", "Legacy Source", "Queue Two", 3, 37, "#ff9650")
            )
        );
    }

    public static PlaylistDetail workspaceLibrary() {
        return playlist(
            "本地音乐文件夹",
            "Workspace / runtime / LocalMusic",
            "对应旧版“本地音乐文件夹”入口，当前先展示相对路径目录语义和迁移承接点。",
            "#57b5ff",
            "夹",
            List.of(
                song("runtime/musicbox.db", "SQLite", "Workspace", 0, 45, "#57b5ff"),
                song("./LocalMusic/Music", "Storage Root", "Workspace", 0, 52, "#4aa9f8"),
                song("./LocalMusic/Lrc", "Lyrics Root", "Workspace", 0, 48, "#62bcff")
            )
        );
    }

    public static SongSummary nowPlaying() {
        return new SongSummary(
            "Blue Horizon",
            "Studio River",
            "Night Sketches",
            Duration.ofSeconds(48),
            "#4fa7ff",
            List.of(
                new LyricLine(Duration.ZERO, "Soft light settles over the city line"),
                new LyricLine(Duration.ofSeconds(6), "Every window keeps a second heartbeat"),
                new LyricLine(Duration.ofSeconds(12), "Your voice turns static into color"),
                new LyricLine(Duration.ofSeconds(18), "Slow motion shadows drift across the room"),
                new LyricLine(Duration.ofSeconds(24), "We trade the night for something brighter"),
                new LyricLine(Duration.ofSeconds(30), "Hold the rhythm steady while the skyline moves"),
                new LyricLine(Duration.ofSeconds(36), "All the noise becomes a calm horizon"),
                new LyricLine(Duration.ofSeconds(42), "Stay here until the last echo fades")
            ),
            "Drawer Demo",
            DEMO_AUDIO_SOURCE,
            "sample",
            "",
            ""
        );
    }

    private static PlaylistDetail playlist(
        final String title,
        final String tags,
        final String description,
        final String accentColor,
        final String coverMark,
        final List<SongSummary> songs
    ) {
        return new PlaylistDetail(title, tags, description, accentColor, coverMark, songs, "sample", "", "");
    }

    private static SongSummary song(
        final String title,
        final String artist,
        final String album,
        final int minutes,
        final int seconds,
        final String accentColor
    ) {
        final Duration duration = Duration.ofMinutes(minutes).plusSeconds(seconds);
        return new SongSummary(
            title,
            artist,
            album,
            duration,
            accentColor,
            List.of(
                new LyricLine(Duration.ZERO, title),
                new LyricLine(Duration.ofSeconds(Math.max(6, seconds / 2)), artist),
                new LyricLine(Duration.ofSeconds(Math.max(12, seconds)), album)
            ),
            "Legacy Migration",
            DEMO_AUDIO_SOURCE,
            "sample",
            "",
            ""
        );
    }

    private static String resolveDemoAudioSource() {
        final var resource = FxSampleData.class.getResource("/audio/demo-loop.wav");
        return resource == null ? "" : resource.toExternalForm();
    }
}


