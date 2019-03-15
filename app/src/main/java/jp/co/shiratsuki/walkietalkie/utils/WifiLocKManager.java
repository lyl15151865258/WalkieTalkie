package jp.co.shiratsuki.walkietalkie.utils;

import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * Wifi锁，可以避免应用长时间息屏后Wifi断开的问题
 * 手机屏幕关闭之后，并且其他的应用程序没有在使用wifi的时候，系统大概在两分钟之后，会关闭wifi，使得wifi处于睡眠状态
 * Created at 2019/3/15 9:18
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class WifiLocKManager {

    // 定义WifiManager对象
    private WifiManager mWifiManager;

    // 定义一个WifiLock
    private WifiManager.WifiLock mWifiLock;

    public WifiLocKManager(Context context) {
        // 取得WifiManager对象
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //第一种方式
        createWifiLock("WifiLocKManager");
        //第二种方式 //注意，在create wifilock的时候，一定要注意传WifiManager.WIFI_MODE_FULL_HIGH_PERF，否则的话在有些手机上貌似不起作用。不过估计对手机性能有所损耗
        //  creatWifiLock("WifiLocKManager", WifiManager.WIFI_MODE_FULL_HIGH_PERF);
    }

    /**
     * 创建一个WifiLock
     *
     * @param locakName 名称
     * @param lockType  WIFI_MODE_FULL == 1  扫描，自动的尝试去连接一个曾经配置过的点
     *                   WIFI_MODE_SCAN_ONLY == 2  只剩下扫描
     *                   WIFI_MODE_FULL_HIGH_PERF = 3  在第一种模式的基础上，保持最佳性能
     *                   
     */
    public void createWifiLock(String locakName, int lockType) {
        mWifiLock = mWifiManager.createWifiLock(lockType, locakName);
    }

    /**
     * 创建一个WifiLock
     *
     * @param locakName 名称
     */
    public void createWifiLock(String locakName) {
        mWifiLock = mWifiManager.createWifiLock(locakName);
    }


    /**
     * 锁定WifiLock 
     */
    public void acquireWifiLock() {
        if (mWifiLock != null) {
            mWifiLock.acquire();
        }
    }

    /**
     * 解锁WifiLock
     */
    public void releaseWifiLock() {
        // 判断时候锁定
        if (mWifiLock != null && mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

}