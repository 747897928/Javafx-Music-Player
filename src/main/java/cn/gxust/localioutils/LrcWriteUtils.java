package cn.gxust.localioutils;

import cn.gxust.utils.Log4jUtils;

import java.io.*;

/**
 * <p>description: 将歌词io到本地的工具类</p>
 * <p>create:2020/3/8 17:31</p>
 * @author zhaoyijie
 * @version v1.0
 */
public class LrcWriteUtils {
    /**
     * 将字符串写入文件
     *
     * @param str         字符串
     * @param LrcSavepath 歌词文件保存路径
     */
    public static void writeFile(String str, String LrcSavepath) {
        File file = new File(LrcSavepath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log4jUtils.logger.error("", e);
                return;
            }
        }
        BufferedWriter bwriter;
        try {
            bwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            bwriter.write(str);
            bwriter.flush();
            bwriter.close();
        } catch (Exception e) {
            Log4jUtils.logger.error("", e);
        }
    }
}
