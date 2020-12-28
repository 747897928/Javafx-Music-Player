package cn.gxust.cloudutils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cn.gxust.utils.Log4jUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import cn.gxust.pojo.PlayBean;

import java.util.*;

/**
 * <p>description: 处理网易云歌单爬虫</p>
 * <p>create:2019/11/14 13:28</p>
 *
 * @author zhaoyijie
 * @version v1.0
 */
public class CloudRequest {

    public CloudRequest() {
    }

    public void searchMusic(String name, List<PlayBean> list) {
        if (list.size() != 0) {
            list.clear();
        }
        try {
            String reqStr = "{\"total\":\"True\",\"s\":\"" + name + "\",\"offset\":0,\"csrf_token\":\"nothing\",\"limit\":20,\"type\":\"1\",\"n\":1000}";
            //System.out.println("req_str:" + reqStr);
            Connection.Response
                    response = Jsoup.connect("http://music.163.com/weapi/cloudsearch/get/web?csrf_token=")
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:57.0) Gecko/20100101 Firefox/57.0")
                    .header("Accept", "*/*")
                    .header("Cache-Control", "no-cache")
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
            String html = response.body();
            JSONArray jsonArray = JSON.parseObject(html).getJSONObject("result").getJSONArray("songs");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String namestring = jsonObject.getString("name");
                String idstring = jsonObject.getString("id");
                String artistsname = jsonObject.getJSONArray("ar").getJSONObject(0).getString("name");
                String url = "http://music.163.com/song/media/outer/url?id=" + idstring + ".mp3";
                JSONObject albumJsonObject = jsonObject.getJSONObject("al");
                String blurPicUrlstring = null;
                try {
                    blurPicUrlstring = albumJsonObject.getString("picUrl");
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

    public String spiderLrc(String songId) {
        try {
            //歌词请求接口
            String url = "http://music.163.com/api/song/lyric?id=" + songId + "&lv=1&kv=1&tv=-1";
            Connection connection = Jsoup.connect(url).ignoreContentType(true);
            connection.timeout(10000);
            //设置请求方式
            connection.method(Connection.Method.GET);
            connection.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
            //获得响应
            Connection.Response response = connection.execute();
            String html = response.body();
            //解析json，提取json里的lrc
            return JSON.parseObject(html).getJSONObject("lrc").getString("lyric");
        } catch (Exception e) {
            //Log4jUtils.logger.error("", e);
            return "[00:00.000] 未找到歌词";
        }
    }

    public void getMusicDetail(PlayBean playBean) {
        String songId = playBean.getMusicId();
        try {
            String reqStr = "[{\"id\":\"" + songId + "\"}]";
            Connection.Response
                    response = Jsoup.connect("http://music.163.com/api/v3/song/detail?id=" + songId + "&c=" + reqStr)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:57.0) Gecko/20100101 Firefox/57.0")
                    .header("Accept", "*/*")
                    .header("Cache-Control", "no-cache")
                    .header("Host", "music.163.com")
                    .header("Accept-Language", "zh-CN,en-US;q=0.7,en;q=0.3")
                    .header("DNT", "1")
                    .header("Pragma", "no-cache")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .requestBody("{csrf_token: \"fd1acbd02cc87df18472e5ecf775d12b\"}")
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .timeout(10000)
                    .execute();
            String s = response.body();
            //System.out.println(s);
            JSONObject jsonObject = JSON.parseObject(s).getJSONArray("songs").getJSONObject(0);
            String idstring = jsonObject.getString("id");
            if (idstring.equalsIgnoreCase(songId)) {
                String artistsname = jsonObject.getJSONArray("ar")
                        .getJSONObject(0)
                        .getString("name");
                JSONObject albumJsonObject = jsonObject.getJSONObject("al");
                String blurPicUrlstring = null;
                try {
                    blurPicUrlstring = albumJsonObject.getString("picUrl");
                    playBean.setImageUrl(blurPicUrlstring);
                } catch (Exception e) {
                    Log4jUtils.logger.error("", e);
                }
                String album = albumJsonObject.getString("name");
                playBean.setArtistName(artistsname);
                playBean.setAlbum(album);
                playBean.setImageUrl(blurPicUrlstring);
            }
        } catch (Exception e) {
            Log4jUtils.logger.error("", e);
        }
    }

    /**
     * 获取 302 跳转后的真实地址
     *
     * @param url 原始地址
     * @return 真实地址
     */
    public String getReal(String url) {
        if (url.contains("http://music.163.com/song/media/outer/url")) {
            try {
                return Jsoup.connect(url).ignoreContentType(true).
                        timeout(4000)
                        .header("User-Agent",
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.9 Safari/537.36").execute().url().toString();
            } catch (Exception e) {
                Log4jUtils.logger.error("", e);
                return url;
            }
        } else {
            return url;
        }
    }
}
