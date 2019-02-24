package jp.co.shiratsuki.walkietalkie.utils;

import java.net.HttpURLConnection;
import java.net.URL;

public class UrlCheckUtil {

    private static URL url;

    private static HttpURLConnection con;

    private static int state = -1;


    /**
     * 功能：检测当前URL是否可连接或是否有效,
     * 描述：最多连接网络 5 次, 如果 5 次都不成功，视为该地址不可用
     *
     * @param urlStr 指定URL网络地址
     * @return URL 
     */

    public synchronized URL isConnect(String urlStr) {

        int counts = 0;
        if (urlStr == null || urlStr.length() <= 0) {
            return null;
        }
        while (counts < 5) {
            try {
                url = new URL(urlStr);
                con = (HttpURLConnection) url.openConnection();
                state = con.getResponseCode();
                System.out.println(counts + "= " + state);
                if (state == 200) {
                    System.out.println("URL可用！");
                }
                break;
            } catch (Exception ex) {
                counts++;
                System.out.println("URL不可用，连接第 " + counts + " 次");
                urlStr = null;
                continue;
            }
        }
        return url;
    }

    /**
     * 检查URL是否存在（通过接口请求方式比较返回的状态码）
     *
     * @param url URL路径
     * @return 是否存在
     */
    public static boolean checkUrlExist(String url) {
        if (!url.contains("http://")) {
            url = "http://" + url;
        }
        String[] url0 = {url};
        boolean[] result = {false};
        Thread thread =
                new Thread(() -> {
                    try {
                        HttpURLConnection.setFollowRedirects(false);
                        HttpURLConnection con = (HttpURLConnection) new URL(url0[0]).openConnection();
                        con.setRequestMethod("HEAD");
                        result[0] = (con.getResponseCode() == HttpURLConnection.HTTP_OK);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtils.d("*******************音乐文件链接：" + (result[0] ? "文件存在" : "文件不存在") + "******************");
        return result[0];
    }
}
