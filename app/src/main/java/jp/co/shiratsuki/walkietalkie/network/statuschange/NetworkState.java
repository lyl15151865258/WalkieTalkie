package jp.co.shiratsuki.walkietalkie.network.statuschange;

/**
 * 网络状态实体类
 * Created at 2018/11/28 13:47
 *
 * @author LiYuliang
 * @version 1.0
 */

public class NetworkState {

    private boolean wifi;
    private boolean mobile;
    private boolean connected;

    public NetworkState() {
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isWifi() {
        return wifi;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public boolean isMobile() {
        return mobile;
    }

    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

}
