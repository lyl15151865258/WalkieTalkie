package jp.co.shiratsuki.walkietalkie.webrtc;

import org.webrtc.MediaStream;

import java.util.ArrayList;

import jp.co.shiratsuki.walkietalkie.bean.User;

/**
 * 回调接口
 * Created at 2019/1/15 2:40
 *
 * @author Li Yuliang
 * @version 1.0
 */

public interface IWebRTCHelper {

    void onOverMaxTalker(String roomId);

    void onSetLocalStream(MediaStream stream, String userId);

    void onAddRemoteStream(MediaStream stream, String userId);

    void onEnterRoom();

    void onLeaveRoom();

    void onLeaveGroup();

    void onCloseWithId(String userId);

    // 有人与服务器断开连接
    void removeUser(String userId);

    // 有人离开了房间
    void someoneLeaveRoom(String roomId, ArrayList<User> userList);

    // 更新房间内联系人列表
    void updateRoomContacts(ArrayList<User> userList);

    // 更新房间内说话状态
    void updateRoomSpeakStatus(ArrayList<User> userList);

    // 更新所有联系人列表
    void updateContacts(ArrayList<User> userList);

}
