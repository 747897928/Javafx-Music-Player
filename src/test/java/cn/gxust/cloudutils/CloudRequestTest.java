package cn.gxust.cloudutils;

import cn.gxust.pojo.PlayBean;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

/**
 * <p>description:  </p>
 * <p>create:  2020/11/23 19:48</p>
 *
 * @author zhaoyijie(AquariusGenius)
 */
public class CloudRequestTest {

    CloudRequest cloudRequest;

    @Before
    public void before() {
        cloudRequest = new CloudRequest();
    }

    @Test
    public void randomTest(){
        System.out.println(new Random().nextInt(37) + 1);
    }
    @Test
    public void getMusicDetail() {
        PlayBean playBean = new PlayBean();
        playBean.setMusicId("16435049");
        cloudRequest.getMusicDetail(playBean);
        System.out.println(playBean);
    }
}