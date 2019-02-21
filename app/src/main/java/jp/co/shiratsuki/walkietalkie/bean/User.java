package jp.co.shiratsuki.walkietalkie.bean;

import android.os.Parcel;
import android.os.Parcelable;

import jp.co.shiratsuki.walkietalkie.utils.TimeUtils;

/**
 * 联系人实体类
 * Created at 2019/1/19 13:16
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class User implements Parcelable {

    private String user_id = "";
    private String user_pwd = "";
    private String user_name = "";
    private String company = "";
    private int department_id = 0;
    private String department_name = "";
    private String icon_url = "";
    private String message_ip = "";
    private String message_port = "";
    private String voice_ip = "";
    private String voice_port = "";
    private String room_id = "";
    private boolean inroom = false;
    private boolean speaking = false;
    private boolean busy = false;
    private String register_time = "";
    private String login_time = TimeUtils.getCurrentDateTime();

    public User(String user_id, String user_pwd, String user_name, String company, int department_id, String department_name,
                String icon_url, String message_ip, String message_port, String voice_ip, String voice_port, String room_id,
                boolean inroom, boolean speaking, boolean busy, String register_time, String login_time) {
        super();
        this.user_id = user_id;
        this.user_pwd = user_pwd;
        this.user_name = user_name;
        this.company = company;
        this.department_id = department_id;
        this.department_name = department_name;
        this.icon_url = icon_url;
        this.message_ip = message_ip;
        this.message_port = message_port;
        this.voice_ip = voice_ip;
        this.voice_port = voice_port;
        this.room_id = room_id;
        this.inroom = inroom;
        this.speaking = speaking;
        this.busy = busy;
        this.register_time = register_time;
        this.login_time = login_time;
    }

    public User() {
        super();
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_pwd() {
        return user_pwd;
    }

    public void setUser_pwd(String user_pwd) {
        this.user_pwd = user_pwd;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public int getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(int department_id) {
        this.department_id = department_id;
    }

    public String getDepartment_name() {
        return department_name;
    }

    public void setDepartment_name(String department_name) {
        this.department_name = department_name;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    public String getMessage_ip() {
        return message_ip;
    }

    public void setMessage_ip(String message_ip) {
        this.message_ip = message_ip;
    }

    public String getMessage_port() {
        return message_port;
    }

    public void setMessage_port(String message_port) {
        this.message_port = message_port;
    }

    public String getVoice_ip() {
        return voice_ip;
    }

    public void setVoice_ip(String voice_ip) {
        this.voice_ip = voice_ip;
    }

    public String getVoice_port() {
        return voice_port;
    }

    public void setVoice_port(String voice_port) {
        this.voice_port = voice_port;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public boolean isInroom() {
        return inroom;
    }

    public void setInroom(boolean inroom) {
        this.inroom = inroom;
    }

    public boolean isSpeaking() {
        return speaking;
    }

    public void setSpeaking(boolean speaking) {
        this.speaking = speaking;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public String getRegister_time() {
        return register_time;
    }

    public void setRegister_time(String register_time) {
        this.register_time = register_time;
    }

    public String getLogin_time() {
        return login_time;
    }

    public void setLogin_time(String login_time) {
        this.login_time = login_time;
    }

    public static Creator<User> getCREATOR() {
        return CREATOR;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", user_pwd='" + user_pwd + '\'' +
                ", user_name='" + user_name + '\'' +
                ", company='" + company + '\'' +
                ", department_id=" + department_id +
                ", department_name='" + department_name + '\'' +
                ", icon_url='" + icon_url + '\'' +
                ", message_ip='" + message_ip + '\'' +
                ", message_port='" + message_port + '\'' +
                ", voice_ip='" + voice_ip + '\'' +
                ", voice_port='" + voice_port + '\'' +
                ", room_id='" + room_id + '\'' +
                ", inroom=" + inroom +
                ", speaking=" + speaking +
                ", busy=" + busy +
                ", register_time='" + register_time + '\'' +
                ", login_time='" + login_time + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(user_pwd);
        dest.writeString(user_name);
        dest.writeString(company);
        dest.writeInt(department_id);
        dest.writeString(department_name);
        dest.writeString(icon_url);
        dest.writeString(message_ip);
        dest.writeString(message_port);
        dest.writeString(voice_ip);
        dest.writeString(voice_port);
        dest.writeString(room_id);
        dest.writeByte((byte) (inroom ? 1 : 0));
        dest.writeByte((byte) (speaking ? 1 : 0));
        dest.writeByte((byte) (busy ? 1 : 0));
        dest.writeString(register_time);
        dest.writeString(login_time);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source.readString(), source.readString(), source.readString(), source.readString(), source.readInt(), source.readString(),
                    source.readString(), source.readString(), source.readString(), source.readString(), source.readString(), source.readString(),
                    source.readByte() != 0, source.readByte() != 0, source.readByte() != 0, source.readString(), source.readString());
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
