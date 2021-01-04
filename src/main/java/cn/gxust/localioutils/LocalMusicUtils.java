package cn.gxust.localioutils;

import cn.gxust.pojo.PlayBean;
import cn.gxust.utils.Log4jUtils;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Desktop;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>description: 处理本地音乐的工具类</p>
 * <p>create:2020/3/3 8:18</p>
 *
 * @author zhaoyijie
 * @version v1.0
 */
public class LocalMusicUtils {

    public final static String LOCAL_DIR = System.getProperty("user.dir") + "/LocalMusic/";

    public final static String LOCAL_LRC_DIR = System.getProperty("user.dir") + "/LocalMusic/Lrc/";

    public final static String LOCAL_MUSIC_DIR = System.getProperty("user.dir") + "/LocalMusic/Music/";

    static {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        Logger.getLogger("org.jaudiotagger.audio").setLevel(Level.OFF);
    }

    /**
     * 打开存放指定音乐文件的文件夹并选择这个音乐文件
     *
     * @param playBean 音乐对象
     */
    public static void openFileAndSelect(PlayBean playBean) {
        try {
            String osName = System.getProperty("os.name");
            osName = osName.toLowerCase();
            if (osName.contains("win")) {
                String absolutePath = new File(new URL(playBean.getMp3Url()).toURI()).getAbsolutePath();
                Runtime.getRuntime().exec("explorer /select, " + absolutePath);
            } else if (osName.contains("mac")) {
                String absolutePath = new File(new URL(playBean.getMp3Url()).toURI()).getAbsolutePath();
                Runtime.getRuntime().exec("open -R " + absolutePath);
            } else {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(new File(LOCAL_MUSIC_DIR));
                } else {
                    Log4jUtils.logger.warn("该操作系统不支持Desktop");
                }
            }
        } catch (Exception e) {
            Log4jUtils.logger.error("", e);
        }
    }

    /**
     * 打开本地音乐文件夹
     */
    public static void openLocalDir() {
        try {
            createLocalMusicDir();
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(LOCAL_DIR));
            }
        } catch (IOException e) {
            Log4jUtils.logger.error("", e);
        }
    }

    /**
     * 将文件列表里的文件批量复制到本地音乐文件目录
     *
     * @param files file list
     * @return Int array, the first element is the count of successfully copied
     * music files,and the second element is the count of successfully copied
     * LRC files
     */
    public static int[] copyMusicToLocalDir(List<File> files) {
        createLocalMusicDir();
        int[] countArr = new int[2];
        for (File f : files) {
            String name = f.getName();
            if (!name.contains(".")) {
                continue;/*无扩展名没办法判断文件类型本次循环跳过*/
            }
            int i = name.lastIndexOf(".");
            String suffix = name.substring(i).toLowerCase();
            File file;
            String fileType;
            if (suffix.equals(".mp3") || suffix.equals(".wav") || suffix.equals(".m4a") || suffix.equals(".pcm")) {
                file = new File(LOCAL_MUSIC_DIR, name);
                fileType = "music";
            } else if (suffix.equals(".lrc")) {
                file = new File(LOCAL_LRC_DIR, name);
                fileType = "lrc";
            } else {
                continue;
            }
            if (file.exists()) {
                continue;
            }
            if (!f.getAbsolutePath().equals(file.getAbsolutePath())) {
                try {
                    Files.copy(f.toPath(), file.toPath());
                    if (fileType.equals("music")) {
                        countArr[0] = countArr[0] + 1;
                    } else {
                        countArr[1] = countArr[1] + 1;
                    }
                } catch (Exception e) {
                    Log4jUtils.logger.error("", e);
                }
            }
        }
        return countArr;
    }

    /**
     * 删除本地音乐，该操作会将音乐文件和lrc文件都一起删除
     *
     * @param playBean 音乐对象
     * @return 是否删除成功
     */
    public static boolean deleteMusic(PlayBean playBean) {
        if (!playBean.isLocalMusic()) {
            return false;
        }
        String mp3Url = playBean.getMp3Url();
        try {
            URL url = new URL(mp3Url);
            File voiceFile = new File(url.toURI());
            if (voiceFile.exists()) {
                boolean b = voiceFile.delete();
                if (b) {
                    String localLrlPath = playBean.getLocalLrcPath();
                    File lrcFile = new File(localLrlPath);
                    if (lrcFile.exists()) {
                        lrcFile.delete();
                    }
                }
                return b;
            }
        } catch (Exception e) {
            Log4jUtils.logger.warn("", e);
        }
        return false;
    }

    /**
     * 获取本地音乐的歌词
     *
     * @param lrcPath 本地音乐lrc文件的路径
     * @return 返回本地音乐歌词，以字符串表示
     */
    public static String getLrc(String lrcPath) {
        String lrc = null;
        File lrcFile = new File(lrcPath);
        if (!lrcFile.exists()) {
            lrc = "[00:00.00]无歌词";
        } else {
            try {
                byte[] fileBytes = Files.readAllBytes(Paths.get(lrcFile.toURI()));
                lrc = new String(fileBytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                Log4jUtils.logger.error("", e);
                lrc = "[00:00.00]无歌词";
            }
        }
        return lrc;
    }

    /**
     * 创建一个文件夹用来保存本地音乐和歌词
     */
    public static void createLocalMusicDir() {
        File parentMusicFile = new File(LOCAL_MUSIC_DIR);
        if (!parentMusicFile.exists()) {
            parentMusicFile.mkdirs();
        }
        File parentLrcFile = new File(LOCAL_LRC_DIR);
        if (!parentLrcFile.exists()) {
            parentLrcFile.mkdirs();
        }
    }

    /**
     * 获取本地音乐的信息，包括本地音乐的头文件信息，嵌入音乐文件内的专辑图片
     *
     * @param list 本地音乐列表
     */
    public static void getLocalMusicInf(List<PlayBean> list) {
        if (list.size() != 0) {
            list.clear();
        }
        createLocalMusicDir();
        File[] filelist = new File(LOCAL_MUSIC_DIR).listFiles();
        for (File file : filelist) {
            PlayBean playBean = new PlayBean(file.getName());
            //解析文件
            AudioFile audioFile = null;
            try {
                audioFile = AudioFileIO.read(file);
            } catch (Exception e) {
                Log4jUtils.logger.error("", e);
                continue;
            }
            Tag tag = audioFile.getTag();
            String songName = tag.getFirst(FieldKey.TITLE);//歌名
            String artist = tag.getFirst(FieldKey.ARTIST);//演唱者
            String album = tag.getFirst(FieldKey.ALBUM);//专辑名称
            String fileName = null;
            try {
                fileName = playBean.getMusicName().substring(0, playBean.getMusicName().lastIndexOf('.'));
            } catch (Exception e) {
                Log4jUtils.logger.error("", e);
                fileName = "无名歌曲";
            }
            //为PlayBean赋值
            if (artist != null && !artist.equals("")) {
                playBean.setArtistName(artist);
            }
            if (album != null && !album.equals("")) {
                playBean.setAlbum(album);
            }
            if (songName != null && !songName.equals("")) {
                playBean.setMusicName(songName);
            } else {
                playBean.setMusicName(fileName);
            }
            playBean.setLocalMusic(true);
            playBean.setMp3Url(file.toURI().toString());
            String lrcPath = LOCAL_LRC_DIR + fileName + ".lrc";
            playBean.setLocalLrcPath(lrcPath);//记录本地lrc文件所在位置
            list.add(playBean);
        }
    }


    /**
     * 获取嵌入音乐文件内的专辑图片
     *
     * @param file 音乐文件file对象
     * @return a WritableImage object
     */
    public static WritableImage getLocalMusicArtwork(File file) {
        //解析文件
        AudioFile audioFile = null;
        try {
            audioFile = AudioFileIO.read(file);
        } catch (Exception e) {
            //Log4jUtils.logger.error("该歌曲无专辑图片", e);
            return null;
        }
        Tag tag = audioFile.getTag();
        /*获取音乐封面*/
        try {
            BufferedImage artwork = tag.getFirstArtwork().getImage();
            return SwingFXUtils.toFXImage(artwork, null);
        } catch (Exception e) {
            //Log4jUtils.logger.error("", e);
            return null;
        }
    }


    /**
     * 获取嵌入音乐文件内的专辑图片
     *
     * @param file   音乐文件file对象
     * @param width  返回图片对象的宽
     * @param height 返回图片的高
     * @return a WritableImage object
     */
    public static WritableImage getLocalMusicArtwork(File file, int width, int height) {
        //解析文件
        AudioFile audioFile = null;
        try {
            audioFile = AudioFileIO.read(file);
        } catch (Exception e) {
            //Log4jUtils.logger.error("该歌曲无专辑图片", e);
            return null;
        }//Log4jUtils.logger.error("", e);

        Tag tag = audioFile.getTag();
        ;
        /*获取音乐封面*/
        try {
            BufferedImage artwork = tag.getFirstArtwork().getImage();
            java.awt.Graphics2D graphics = (java.awt.Graphics2D) artwork.getGraphics();
            graphics.scale((width / artwork.getWidth()), (height / artwork.getHeight()));
            graphics.drawImage(artwork, 0, 0, null);
            graphics.dispose();
            WritableImage writableImage = SwingFXUtils.toFXImage(artwork, null);
            return writableImage;
        } catch (Exception e) {
            //Log4jUtils.logger.error("", e);
            return null;
        }
    }

    /**
     * 将音乐信息写入到音乐文件内
     *
     * @param playBean      音乐信息对象
     * @param musicPath     音乐文件的路径
     * @param bufferedImage 即将嵌入音乐文件的图片对象
     */
    public static void setMusicInf(PlayBean playBean, String musicPath, BufferedImage bufferedImage) {
        File file = new File(musicPath);
        AudioFile audioFile = null;
        try {
            audioFile = AudioFileIO.read(file);
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            Log4jUtils.logger.error("", e);
            return;
        }
        Tag tag = audioFile.getTag();
        try {
            tag.setField(FieldKey.TITLE, playBean.getMusicName());
            if (!playBean.getArtistName().equals("")) {
                tag.setField(FieldKey.ARTIST, playBean.getArtistName());
            }
            tag.setField(FieldKey.ALBUM, playBean.getAlbum());
        } catch (FieldDataInvalidException e) {
            Log4jUtils.logger.error("", e);
        }
        tag.deleteArtworkField();
        File file1 = new File(System.getProperty("user.dir") + "/LocalMusic/tmpImage.jpg");
        try {
            boolean b = ImageIO.write(bufferedImage, "jpg", file1);
            if (!b) {
                Log4jUtils.logger.warn("ImageIO无法把音乐封面写入到文件内！尝试下载获取");
                downloadFile(playBean.getImageUrl() + "?param=300y300", file1);
            }
            try {
                Artwork artwork = Artwork.createArtworkFromFile(file1);
                try {
                    tag.setField(artwork);
                } catch (FieldDataInvalidException e) {
                    Log4jUtils.logger.error("", e);
                }
            } catch (IOException e) {
                Log4jUtils.logger.error("", e);
            }
        } catch (IOException e) {
            Log4jUtils.logger.error("", e);
        }
        try {
            audioFile.commit();
        } catch (CannotWriteException e) {
            Log4jUtils.logger.error("", e);
        }
        file1.delete();
    }

    /**
     * 说明：根据指定URL将文件下载到指定目标位置
     *
     * @param urlPath 下载路径
     * @param file    保存文件
     * @return boolean 是否下载成功
     */
    public static boolean downloadFile(String urlPath, File file) {
        try {
            Connection.Response response = Jsoup.connect(urlPath)
                    .ignoreContentType(true).method(Connection.Method.GET)
                    .timeout(15000).maxBodySize(0)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.75 Safari/537.36")
                    .execute();
            byte[] fileBytes = response.bodyAsBytes();
            System.out.println("文件大小：" + fileBytes.length / 1024.0f + "kb");
            // 校验文件夹目录是否存在，不存在就创建一个目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(file);
            out.write(fileBytes);
            out.flush();
            out.close();
            System.out.println(urlPath);
            return true;
        } catch (Exception e) {
            Log4jUtils.logger.error(urlPath, e);
            return false;
        }
    }
}
