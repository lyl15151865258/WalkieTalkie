package jp.co.shiratsuki.walkietalkie.webrtc;

import org.webrtc.MediaStream;

import java.util.List;

import jp.co.shiratsuki.walkietalkie.bean.Contact;

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

    // 有人进入房间
    void addUser(String userIP, String userName);

    // 有人离开房间
    void removeUser(String userIP);

    // 更新联系人列表
    void updateContacts(List<Contact> contactList);

}
