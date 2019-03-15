package jp.co.shiratsuki.walkietalkie.constant;

import jp.co.shiratsuki.walkietalkie.webrtc.MyIceServer;

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

    //主账号IP地址
    public static final String SERVER_HOST_MAIN = "192.168.5.123";
    //主账号端口号
    public static final String SERVER_PORT_MAIN = "8080";
    //主账号项目名
    public static final String PROJECT_MAIN = "WalkieTalkieServer";

    // 语音服务器地址
    public static final String WEBRTC_SERVER_IP = "192.168.5.123";
    // 语音服务器端口
    public static final String WEBRTC_SERVER_PORT = "8080";
    // 默认房间号
    public static final String WEBRTC_SERVER_ROOM = "JSS";

    //消息服务器地址
    public static final String MESSAGE_SERVER_IP = "192.168.5.123";
    //消息服务器端口号
    public static final String MESSAGE_SERVER_PORT = "50100";
    //消息服务器名称
    public static final String MESSAGE_SERVER_NAME = "Interphone";
    //通话等待时间45秒
    public static final int CALL_WAIT_TIME = 45;

    //http请求超时时间
    public static final int TIME_OUT_HTTP = 10 * 1000;
    //WebSocket重连间隔（5秒）
    public static final int WEBSOCKET_RECONNECT_RATE = 5 * 1000;

    public static MyIceServer[] iceServers = {
            new MyIceServer("stun:47.254.34.146"),
            new MyIceServer("turn:47.254.34.146?transport=udp", "dds", "123456"),
            new MyIceServer("turn:47.254.34.146?transport=tcp", "dds", "123456")
    };
    //    private static String signal = "wss://47.254.34.146/wss";
}
