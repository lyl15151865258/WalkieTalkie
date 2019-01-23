package jp.co.shiratsuki.walkietalkie.webrtc.websocket;

import org.webrtc.IceCandidate;

import java.util.ArrayList;
import java.util.List;

import jp.co.shiratsuki.walkietalkie.bean.Contact;

/**
 * WebSocket回调接口
 * Created at 2019/1/15 2:39
 *
 * @author Li Yuliang
 * @version 1.0
 */

public interface ISignalingEvents {

    // 进入房间
    void onJoinToRoom(List<String> connections, String myId);

    // 有新人进入房间
    void onRemoteJoinToRoom(String socketId, String socketName, List<Contact> contactList);

    void onRemoteIceCandidate(String socketId, IceCandidate iceCandidate);

    void onRemoteOutRoom(String socketId);

    void onReceiveOffer(String socketId, String sdp);

    void onReceiverAnswer(String socketId, String sdp);

    void onReceiveSpeakStatus(List<Contact> contactList);

    // WebSocket断开
    void onWebSocketClosed();

}
