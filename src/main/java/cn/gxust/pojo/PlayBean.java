package cn.gxust.pojo;

/**
 * <p>description: 音乐实体类 </p>
 * <p>create: 2020/2/29 13:28</p>
 *
 * @author zhaoyijie
 * @version v1.0
 */
public class PlayBean {

    private String musicName;

    private String musicId;

    private String mp3Url;

    private String artistName;

    private String imageUrl;

    private String album;

    private boolean isLocalMusic = false;

    private String localLrcPath;

    private boolean isPlayable = true;

    private String lrc;

    public PlayBean() {

    }

    public PlayBean(String musicName) {
        this.musicName = musicName;
    }

    public PlayBean(String musicName, String musicId, String mp3Url, String artistName, String imageUrl, String album) {
        this.musicName = musicName;
        this.musicId = musicId;
        this.mp3Url = mp3Url;
        this.artistName = artistName;
        this.imageUrl = imageUrl;
        this.album = album;
    }


    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public String getMp3Url() {
        return mp3Url;
    }

    public void setMp3Url(String mp3Url) {
        this.mp3Url = mp3Url;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }


    public boolean isLocalMusic() {
        return isLocalMusic;
    }

    public void setLocalMusic(boolean localMusic) {
        isLocalMusic = localMusic;
    }

    public String getLocalLrcPath() {
        return localLrcPath;
    }

    public void setLocalLrcPath(String localLrcPath) {
        this.localLrcPath = localLrcPath;
    }

    public void setPlayable(boolean playable) {
        isPlayable = playable;
    }

    public boolean isPlayable() {
        return isPlayable;
    }

    public String getLrc() {
        return lrc;
    }

    public void setLrc(String lrc) {
        this.lrc = lrc;
    }

    public String getSaveFileName() {
        String s = musicName;
        if (!artistName.equals("")) {
            s = s + "-" + artistName;
        }
        return s;
    }

    public String getMusicInf() {
        return musicName + '-' + artistName + '-' + album + '-' + imageUrl;
    }

    @Override
    public String toString() {
        return "PlayBean{" +
                "musicName='" + musicName + '\'' +
                ", musicId='" + musicId + '\'' +
                ", mp3Url='" + mp3Url + '\'' +
                ", artistName='" + artistName + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", album='" + album + '\'' +
                ", isLocalMusic=" + isLocalMusic +
                ", localLrlPath='" + localLrcPath + '\'' +
                ", isPlayable=" + isPlayable +
                ", lrc='" + lrc + '\'' +
                '}';
    }
}
