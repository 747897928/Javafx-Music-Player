package cn.gxust.cloudutils;

import cn.gxust.utils.Log4jUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import cn.gxust.pojo.PlayBean;
import cn.gxust.pojo.PlayListBean;

import java.util.*;

/**
 * <p>description: 处理网易云音乐爬虫</p>
 * <p>create: 2019/11/14 13:28</p>
 *
 * @author zhaoyijie
 * @version v1.0
 */
public class CloudMusicSpider {

    private final Random random;

    public CloudMusicSpider() {
        random = new Random();
    }

    public void getPlayList(ArrayList<PlayListBean> playListBeanList) {
        //目前网易云歌单38页，但是我们不超这个页数
        String offset = String.valueOf((random.nextInt(37) + 1) * 35);
        /*limit 表示单页显示的歌单数（修改无效） offset 表示当前页数，即 offset / limit + 1   limit=35*/
        String url = "https://music.163.com/discover/playlist/?order=hot&cat=全部&limit=35&offset=" + offset;
        Connection connection = Jsoup.connect(url).ignoreContentType(true);
        connection.timeout(10000);
        //设置请求方式
        connection.method(Connection.Method.GET);
        connection.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
        connection.header("Referer", "http//music.163.com");
        connection.header("Host", "music.163.com");
        //获得响应
        Connection.Response response = null;
        try {
            response = connection.execute();
            String html = response.body();
            Document document = Jsoup.parse(html);
            Elements liItems = document.getElementsByClass("u-cover u-cover-1");
            ListIterator<PlayListBean> playListBeanListIterator = playListBeanList.listIterator();
            for (Element liItem : liItems) {
                try {
                    Element element = liItem;
                    Element aItem = element.selectFirst("a");
                    String PlayListTitle = aItem.attr("title");
                    String tmpHref = aItem.attr("href");
                    if (tmpHref.contains("javascript")) {
                        continue;
                    }
                    String playListId = tmpHref.replace("/playlist?id=", "");
                    String imageUrl = null;
                    try {
                        Element img = element.selectFirst("img");
                        imageUrl = img.attr("src");
                    } catch (Exception e) {
                        Log4jUtils.logger.error("", e);
                    }
                    if (imageUrl == null) {
                        continue;
                    }
                    imageUrl = imageUrl.replace("?param=140y140", "?param=300y300");
                    if (playListBeanListIterator.hasNext()) {
                        PlayListBean playListBean = playListBeanListIterator.next();
                        playListBean.setAlbum(PlayListTitle);
                        playListBean.setImageUrl(imageUrl);
                        playListBean.setPlayListId(playListId);
                    } else {
                        return;
                    }
                } catch (Exception e) {
                    Log4jUtils.logger.error("", e);
                    break;
                }
            }
        } catch (Exception e) {
            Log4jUtils.logger.error("", e);
        }
    }

    /**
     * 获取指定网易云歌单里的每一首歌的名字和mp3链接
     *
     * @param playListBean 歌单对象
     * @param list         音乐对象数组
     */
    public void getSongList(PlayListBean playListBean, List<PlayBean> list) {
        if (list.size() != 0) {
            list.clear();
        }
        String playListId = playListBean.getPlayListId();
        try {
            Connection.Response response = Jsoup.connect("https://music.163.com/api/playlist/detail?id=" + playListId)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:57.0) Gecko/20100101 Firefox/57.0")
                    .header("Host", "music.163.com")
                    .method(Connection.Method.GET).ignoreContentType(true)
                    .timeout(10000).execute();
            String s = response.body();
            JSONObject resultJson = JSON.parseObject(s).getJSONObject("result");
            String description = resultJson.getString("description");
            if (description == null) {
                description = "该歌单无详细介绍";
            }
            description = description.replaceAll("[ ]|\n", "").trim();
            playListBean.setDescription(description);
            String tags = resultJson.getString("tags");
            if (tags == null || tags.equals("[]")) {
                tags = "音乐";
            }
            playListBean.setTags(tags);
            JSONArray jsonArray = resultJson.getJSONArray("tracks");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String namestring = jsonObject.getString("name");
                String idstring = jsonObject.getString("id");
                String artistsname = jsonObject.getJSONArray("artists").getJSONObject(0).getString("name");
                String url = "http://music.163.com/song/media/outer/url?id=" + idstring + ".mp3";
                JSONObject albumJsonObject = jsonObject.getJSONObject("album");
                String blurPicUrlstring = null;
                try {
                    blurPicUrlstring = albumJsonObject.getString("blurPicUrl");
                } catch (Exception e) {
                    Log4jUtils.logger.error("", e);
                }
                String album = albumJsonObject.getString("name");
                PlayBean playBean = new PlayBean(namestring, idstring,
                        url, artistsname, blurPicUrlstring, album);
                list.add(playBean);
            }
        } catch (Exception e) {
            Log4jUtils.logger.error("", e);
        }
    }
}
