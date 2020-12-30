package cn.gxust.cloudutils;

import cn.gxust.pojo.PlayBean;
import cn.gxust.utils.Log4jUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>description:  </p>
 * <p>create:  2020/11/23 22:01</p>
 *
 * @author zhaoyijie(AquariusGenius)
 */
public class CloudMusicSpiderTest {

    CloudMusicSpider cloudMusicSpider;

    @Before
    public void before(){
        cloudMusicSpider= new CloudMusicSpider();
    }

    @Test
    public void getPlayList1() {
        String playListId = "5341024670";
        try {
            String reqStr = "{\"total\":\"True\",\"id\":\"" + playListId + "\",\"csrf_token\":\"nothing\",\"limit\":100}";
            //System.out.println("req_str:" + reqStr);
            Connection.Response
                    response = Jsoup.connect("http://music.163.com/weapi/v3/playlist/detail?csrf_token=")
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:57.0) Gecko/20100101 Firefox/57.0")
                    .header("Accept", "*/*")
                    .header("Cache-Control", "no-cache")
                    .header("Connection", "keep-alive")
                    .header("Host", "music.163.com")
                    .header("Accept-Language", "zh-CN,en-US;q=0.7,en;q=0.3")
                    .header("DNT", "1")
                    .header("Pragma", "no-cache")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .data(JavaEncrypt.encrypt(reqStr))
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .timeout(10000)
                    .execute();
            String s = response.body();
            System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getPlayList2() {
        String playListId = "5285572679";
        try {
            Connection.Response response = Jsoup.connect("https://music.163.com/api/playlist/detail?id="+playListId)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:57.0) Gecko/20100101 Firefox/57.0")
                    .header("Host", "music.163.com")
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .timeout(10000)
                    .execute();
            String s = response.body();
            System.out.println(s);
            JSONObject resultJson = JSON.parseObject(s).getJSONObject("result");
            String description = resultJson.getString("description");
            System.out.println("description = " + description);
            String tags = resultJson.getString("tags");
            System.out.println("tags = " + tags);
            List<PlayBean> list = new ArrayList<>();
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
            System.out.println(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}