package jp.co.shiratsuki.walkietalkie.network.wlan;

import jp.co.shiratsuki.walkietalkie.constant.Constants;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * 组播MulticastSocket单例
 * Created at 2018/12/12 13:06
 *
 * @author LiYuliang
 * @version 1.0
 */

public class Multicast {

    // 组播Socket
    private MulticastSocket multicastSocket;
    // IPV4地址
    private InetAddress inetAddress;

    private static final Multicast multicast = new Multicast();

    private Multicast() {
        try {
            inetAddress = InetAddress.getByName(Constants.MULTI_BROADCAST_IP);
            multicastSocket = new MulticastSocket(Constants.MULTI_BROADCAST_PORT);
            multicastSocket.setLoopbackMode(true);
            multicastSocket.joinGroup(inetAddress);
            multicastSocket.setTimeToLive(4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Multicast getMulticast() {
        return multicast;
    }

    public MulticastSocket getMulticastSocket() {
        return multicastSocket;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void free() {
        if (multicastSocket != null) {
            try {
                multicastSocket.leaveGroup(inetAddress);
                multicastSocket.close();
                multicastSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
