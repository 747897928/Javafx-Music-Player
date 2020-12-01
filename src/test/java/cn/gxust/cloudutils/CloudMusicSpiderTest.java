package cn.gxust.cloudutils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Test;

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
    public void getPlayList() {
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
}