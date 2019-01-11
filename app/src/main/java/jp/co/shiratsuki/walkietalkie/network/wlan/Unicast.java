package jp.co.shiratsuki.walkietalkie.network.wlan;

import jp.co.shiratsuki.walkietalkie.constant.Constants;

import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * 单播DatagramSocket单例
 * Created at 2018/12/12 13:06
 *
 * @author LiYuliang
 * @version 1.0
 */

public class Unicast {

    private DatagramSocket datagramSocket;

    private static final Unicast unicast = new Unicast();

    private Unicast() {
        try {
            // 初始化接收Socket
            datagramSocket = new DatagramSocket(Constants.UNICAST_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static Unicast getUnicast() {
        return unicast;
    }

    public DatagramSocket getUnicastDatagramSocket() {
        return datagramSocket;
    }

    public void free() {
        if (datagramSocket != null) {
            datagramSocket.close();
            datagramSocket = null;
        }
    }
}
