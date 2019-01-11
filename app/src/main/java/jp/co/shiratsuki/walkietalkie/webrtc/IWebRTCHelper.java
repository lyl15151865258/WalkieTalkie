package jp.co.shiratsuki.walkietalkie.webrtc;

import org.webrtc.MediaStream;

/**
 * Created by dds on 2017/10/23.
 */

public interface IWebRTCHelper {

    void onSetLocalStream(MediaStream stream, String socketId);

    void onAddRemoteStream(MediaStream stream, String socketId);

    void onCloseWithId(String socketId);

}
