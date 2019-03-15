package jp.co.shiratsuki.walkietalkie.utils;

import android.content.Context;
import android.os.PowerManager;

/**
 * 当手机灭屏状态下保持一段时间后，系统会进入休眠，一些后台任务比如网络下载，播放音乐会得不到正常的执行。WakeLock API可以确保应用程序中关键代码的正确执行，使应用程序有能力控制AP的休眠状态。
 * Created at 2019/3/15 9:31
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class WakeLocKManager {

    // 定义PowerManager对象
    private PowerManager mPowerManager;

    // 定义一个WakeLock
    private PowerManager.WakeLock mWakeLock;

    public WakeLocKManager(Context context) {
        // 取得PowerManager对象
        mPowerManager = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        //第一种方式
//        creatWakeLock("WakeLocKManager");
        //第二种方式
        createWakeLock("WakeLocKManager", PowerManager.PARTIAL_WAKE_LOCK);
    }

    /**
     * 创建一个WifiLock
     *
     * @param lockName     名称
     * @param levelAndFlag levelAndFlags	        CPU是否运行	            屏幕是否亮着	        键盘灯是否亮着
     *                     <p>
     *                     PARTIAL_WAKE_LOCK	            是	                    否	                      否
     *                     SCREEN_DIM_WAKE_LOCK	            是	                  低亮度	                  否
     *                     SCREEN_BRIGHT_WAKE_LOCK	        是	                  高亮度	                  否
     *                     FULL_WAKE_LOCK	                是	                    是	                      是
     *                     自API等级17开始，FULL_WAKE_LOCK将被弃用。应用应使用FLAG_KEEP_SCREEN_ON。
     */
    public void createWakeLock(String lockName, int levelAndFlag) {
        mWakeLock = mPowerManager.newWakeLock(levelAndFlag, lockName);
    }

    /**
     * 创建一个WakeLock
     *
     * @param lockName 名称
     */
    public void createWakeLock(String lockName) {
        mWakeLock = mPowerManager.newWakeLock(PowerManager.ON_AFTER_RELEASE | PowerManager.PARTIAL_WAKE_LOCK, lockName);
    }

    /**
     * 锁定WakeLock
     */
    public void acquireWakeLock() {
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }

    /**
     * 释放WakeLock
     * 当使用wakeLock.acquire(timeout)的方式时系统会自动释放
     */
    public void releaseWakeLock() {
        // 判断时候锁定
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

}