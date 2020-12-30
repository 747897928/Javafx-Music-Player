package cn.gxust.pojo;

/**
 * <p>description: 歌单列表</p>
 * <p>create: 2020/3/2 21:18</p>
 * @author zhaoyijie
 * @version v1.0
 */
public class PlayListBean {

    private String playListId;

    private String album;

    private String imageUrl;

    private String tags;

    private String description;

    public PlayListBean() {
    }

    public PlayListBean(String playListId, String album, String imageUrl, String tags, String description) {
        this.playListId = playListId;
        this.album = album;
        this.imageUrl = imageUrl;
        this.tags = tags;
        this.description = description;
    }

    public String getPlayListId() {
        return playListId;
    }

    public void setPlayListId(String playListId) {
        this.playListId = playListId;
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "PlayListBean{" +
                "playListId='" + playListId + '\'' +
                ", album='" + album + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", tags='" + tags + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
