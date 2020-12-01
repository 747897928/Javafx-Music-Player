package cn.gxust.pojo;

/**
 * <p>description: 歌单列表</p>
 * <p>create: 2020/3/2 21:18</p>
 * @author zhaoyijie
 * @version v1.0
 */
public class PlayListBean {

    private String playListUrl;

    private String album;

    private String imageUrl;

    public PlayListBean() {
    }

    public PlayListBean(String playListUrl, String album, String imageUrl) {
        this.playListUrl = playListUrl;
        this.album = album;
        this.imageUrl = imageUrl;
    }

    public String getPlayListUrl() {
        return playListUrl;
    }

    public void setPlayListUrl(String playListUrl) {
        this.playListUrl = playListUrl;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "PlayListBean{" +
                "playListUrl='" + playListUrl + '\'' +
                ", album='" + album + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
