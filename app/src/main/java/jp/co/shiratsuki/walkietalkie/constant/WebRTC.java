package jp.co.shiratsuki.walkietalkie.constant;

import jp.co.shiratsuki.walkietalkie.webrtc.MyIceServer;

/**
 * WebRTC网络配置
 * Created at 2019/1/11 17:19
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class WebRTC {

    public static MyIceServer[] iceServers = {
            new MyIceServer("stun:47.254.34.146"),
            new MyIceServer("turn:47.254.34.146?transport=udp", "dds", "123456"),
            new MyIceServer("turn:47.254.34.146?transport=tcp", "dds", "123456")
    };
    //    private static String signal = "wss://47.254.34.146/wss";

    public static final String WEBRTC_SERVER_IP = "192.168.1.134";
    public static final String WEBRTC_SERVER_PORT = "8080";
    public static final String WEBRTC_SERVER_ROOM = "jss";

}
