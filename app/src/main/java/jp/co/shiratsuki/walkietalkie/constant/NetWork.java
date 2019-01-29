package jp.co.shiratsuki.walkietalkie.constant;

/**
 * 网络常量值
 * Created at 2018/11/28 13:42
 *
 * @author LiYuliang
 * @version 1.0
 */

public class NetWork {

    public static final String FAIL = "fail";
    public static final String SUCCESS = "success";

    /**
     * 主账号IP地址
     */
    public static final String SERVER_HOST_MAIN = "www.metter.com.cn";
    /**
     * 主账号端口号
     */
    public static final String SERVER_PORT_MAIN = "8073";
    /**
     * 主账号项目名
     */
    public static final String PROJECT_MAIN = "AndroidManager";

    /**
     * WebSocket地址
     */
    public static final String WEBSOCKET_IP = "192.168.51.123";
    /**
     * WebSocket端口号
     */
    public static final String WEBSOCKET_PORT = "50100";
    /**
     * WebSocket名称
     */
    public static final String WEBSOCKET_NAME = "Interphone";
    /**
     * WebSocket重连时间间隔
     */
    public static final int WEBSOCKET_RECONNECT_TIME_INTERVAL = 5000;

    /**
     * http请求超时时间
     */
    public static final int TIME_OUT_HTTP = 10 * 1000;
    /**
     * 心跳包发送间隔（30秒）
     */
    public static final int HEART_BEAT_RATE = 30 * 1000;
}
