package jp.co.shiratsuki.walkietalkie.webrtc;

/**
 * WebRTC网络配置
 * Created at 2019/1/11 17:19
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class WebRTCUtil {

    public static MyIceServer[] iceServers = {
            new MyIceServer("stun:47.254.34.146"),
            new MyIceServer("turn:47.254.34.146?transport=udp", "dds", "123456"),
            new MyIceServer("turn:47.254.34.146?transport=tcp", "dds", "123456")
    };
    //    private static String signal = "wss://47.254.34.146/wss";
    public static String signal = "ws://192.168.2.102:3000";

}
