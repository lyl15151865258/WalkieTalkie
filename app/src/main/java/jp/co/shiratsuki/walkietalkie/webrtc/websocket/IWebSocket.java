package jp.co.shiratsuki.walkietalkie.webrtc.websocket;

import org.webrtc.IceCandidate;

/**
 * WebSocket回调接口
 * Created at 2019/1/15 2:39
 *
 * @author Li Yuliang
 * @version 1.0
 */

public interface IWebSocket {

    void connect(String wss);

    void close();

    boolean socketIsOpen();

    // 加入房间
    void joinRoom(String room);

    //处理回调消息
    void handleMessage(String message);

    void sendIceCandidate(String userId, IceCandidate iceCandidate);

    void sendAnswer(String userId, String sdp);

    void sendOffer(String userId, String sdp);

    // 发送消息
    void sendMessage(String msg);
}
