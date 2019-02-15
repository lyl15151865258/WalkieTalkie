package jp.co.shiratsuki.walkietalkie.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

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
    private String department_id = "";
    private String department_name = "";
    private String icon_url = "";
    private String room_id = "";
    private String room_name = "";
    private boolean inroom = false;
    private boolean speaking = false;
    private String register_time = "";
    private String login_time = (new Date()).toString();

    public User(String user_id, String user_pwd, String user_name, String company, String department_id, String department_name,
                String icon_url, String room_id, String room_name, boolean inroom, boolean speaking, String register_time, String login_time) {
        super();
        this.user_id = user_id;
        this.user_pwd = user_pwd;
        this.user_name = user_name;
        this.company = company;
        this.department_id = department_id;
        this.department_name = department_name;
        this.icon_url = icon_url;
        this.room_id = room_id;
        this.room_name = room_name;
        this.inroom = inroom;
        this.speaking = speaking;
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

    public String getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(String department_id) {
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

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
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

    @Override
    public String toString() {
        return "User [user_id=" + user_id + ", user_pwd=" + user_pwd + ", user_name=" + user_name + ", company="
                + company + ", department_id=" + department_id + ", department_name=" + department_name + ", icon_url="
                + icon_url + ", room_id=" + room_id + ", room_name=" + room_name + ", inroom=" + inroom + ", speaking="
                + speaking + ", register_time=" + register_time + ", login_time=" + login_time + "]";
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
        dest.writeString(department_id);
        dest.writeString(department_name);
        dest.writeString(icon_url);
        dest.writeString(room_id);
        dest.writeString(room_name);
        dest.writeByte((byte) (inroom ? 1 : 0));
        dest.writeByte((byte) (speaking ? 1 : 0));
        dest.writeString(register_time);
        dest.writeString(login_time);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source.readString(), source.readString(), source.readString(), source.readString(), source.readString(), source.readString(),
                    source.readString(), source.readString(), source.readString(), source.readByte() != 0, source.readByte() != 0, source.readString(), source.readString());
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
