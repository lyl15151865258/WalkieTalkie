package jp.co.shiratsuki.walkietalkie.bean;

import java.io.Serializable;

/**
 * 联系人实体类
 * Created at 2019/1/19 13:16
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class Contact implements Serializable {

    private String userId;
    private String userName;
    private String roomId;
    private String roomName;
    private String iconUrl;
    private boolean speaking;

    public Contact(String userId, String userName, String roomId, String roomName, String iconUrl, boolean speaking) {
        super();
        this.userId = userId;
        this.userName = userName;
        this.roomId = roomId;
        this.roomName = roomName;
        this.iconUrl = iconUrl;
        this.speaking = speaking;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public boolean isSpeaking() {
        return speaking;
    }

    public void setSpeaking(boolean speaking) {
        this.speaking = speaking;
    }
}
