package jp.co.shiratsuki.walkietalkie.webrtc;

import org.webrtc.MediaStream;

/**
 * 回调接口
 * Created at 2019/1/15 2:40
 *
 * @author Li Yuliang
 * @version 1.0
 */

public interface IWebRTCHelper {

    void onSetLocalStream(MediaStream stream, String socketId);

    void onAddRemoteStream(MediaStream stream, String socketId);

    void onEnterRoom();

    void onLeaveRoom();

    void onCloseWithId(String socketId);

    // 收到说话状态变化标志
    void receiveSpeakStatus(boolean someoneSpeaking);

}
