package jp.co.shiratsuki.walkietalkie.webrtc;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * ICE服务器实体类
 * Created at 2019/1/15 2:40
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class MyIceServer implements Parcelable {
    public final String uri;
    public final String username;
    public final String password;

    public MyIceServer(String uri) {
        this(uri, "", "");
    }

    public MyIceServer(String uri, String username, String password) {
        this.uri = uri;
        this.username = username;
        this.password = password;
    }


    protected MyIceServer(Parcel in) {
        uri = in.readString();
        username = in.readString();
        password = in.readString();
    }

    public static final Creator<MyIceServer> CREATOR = new Creator<MyIceServer>() {
        @Override
        public MyIceServer createFromParcel(Parcel in) {
            return new MyIceServer(in);
        }

        @Override
        public MyIceServer[] newArray(int size) {
            return new MyIceServer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uri);
        dest.writeString(username);
        dest.writeString(password);
    }
}