package jp.co.shiratsuki.walkietalkie.webrtc.websocket;

import org.webrtc.IceCandidate;

import java.util.ArrayList;

/**
 * WebSocket回调接口
 * Created at 2019/1/15 2:39
 *
 * @author Li Yuliang
 * @version 1.0
 */

public interface ISignalingEvents {

    // 进入房间
    void onJoinToRoom(ArrayList<String> connections, String myId);

    // 有新人进入房间
    void onRemoteJoinToRoom(String socketId);

    void onRemoteIceCandidate(String socketId, IceCandidate iceCandidate);

    void onRemoteOutRoom(String socketId);

    void onReceiveOffer(String socketId, String sdp);

    void onReceiverAnswer(String socketId, String sdp);

    void onReceiveSpeakStatus(boolean someoneSpeaking);

}
