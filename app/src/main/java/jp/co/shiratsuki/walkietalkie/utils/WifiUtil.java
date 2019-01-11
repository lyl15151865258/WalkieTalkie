package jp.co.shiratsuki.walkietalkie.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import jp.co.shiratsuki.walkietalkie.WalkieTalkieApplication;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static android.content.Context.WIFI_SERVICE;

/**
 * WiFi工具类
 * Created at 2018/11/28 13:55
 *
 * @author LiYuliang
 * @version 1.0
 */

public class WifiUtil {

    /**
     * 判断当前网络是否为 Wifi 网络连接
     *
     * @param context
     * @return
     */
    public static boolean WifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static String getBSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getBSSID();
    }

    public static String getSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    public static int getIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getIpAddress();
    }

    //获取本地IP函数
    public static String getLocalIPAddress() {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) WalkieTalkieApplication.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return (ipAddress & 0xFF) + "." + ((ipAddress >> 8) & 0xFF) + "." + ((ipAddress >> 16) & 0xFF) + "." + (ipAddress >> 24 & 0xFF);
    }

    public static String getMacAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getMacAddress();
    }

    public static int getNetworkId(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getNetworkId();
    }

    public static int getLinkSpeed(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getLinkSpeed();
    }

    public static int getRssi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getRssi();
    }

    /**
     * 获取本网段的直接广播地址
     *
     * @param context Context对象
     * @return InetAddress地址
     * @throws UnknownHostException
     */
    public static InetAddress getBroadcastAddress(Context context) throws UnknownHostException {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp == null) {
            return InetAddress.getByName("255.255.255.255");
        }
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }
        return InetAddress.getByAddress(quads);
    }

}
