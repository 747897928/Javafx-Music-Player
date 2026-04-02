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

        public boolean isSamplePlaylist() {
            return "sample".equals(this.sourceType);
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
                "适合深夜独处时循环播放的一组安静电子与轻声人声。",
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
                "把通勤路上的城市灯光、耳机低频和轻微雨声都收进一张歌单里。",
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
                "偏复古的黑胶氛围，适合周末午后把节奏放慢一点。",
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
                "明亮、轻盈又不吵闹，适合白天工作时当作背景音乐。",
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
                "更偏器乐和轻节奏的一组曲目，适合专注时连续播放。",
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
                "偏抒情和夜色感的歌单，适合在安静的时候慢慢听。",
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
                "带一点复古颗粒感和午后温度，听起来松弛又柔和。",
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
                "为夜跑和城市霓虹准备的节奏型歌单，适合想提一点速度的时候。",
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
                "钢琴与都市叙事感更重的一组歌，适合下班后慢慢切换状态。",
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
                "电影感更强的一张歌单，适合雨天、阴天和偏安静的夜晚。",
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
                "清晨海风和器乐线条感更明显，适合放空或者慢慢醒神。",
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
            "扫描并展示当前设备上的本地音乐与歌词文件。",
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
            "用于查看最近保存到本地音乐目录的歌曲和相关资源。",
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
            "展示当前应用常用的本地存储位置与音乐资源目录。",
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


