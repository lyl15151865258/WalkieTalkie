package jp.co.shiratsuki.walkietalkie.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 * 生命周期处理类
 * Created at 2018/11/28 13:52
 *
 * @author LiYuliang
 * @version 1.0
 */

public class MyLifecycleHandler implements Application.ActivityLifecycleCallbacks {

    private static int resumed, paused, started, stopped;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        ++started;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        resumed++;
        ActivityController.getInstance().setCurrentActivity(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        paused++;
        Log.d("MyLifecycleHandler", "application is in foreground: " + (resumed > paused));
    }

    @Override
    public void onActivityStopped(Activity activity) {
        stopped++;
        Log.d("MyLifecycleHandler", "application is visible: " + (started > stopped));
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        resumed--;
        paused++;
        stopped++;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public static boolean isApplicationVisible() {
        return started > stopped;
    }

    public static boolean isApplicationInForeground() {
        // 当所有 Activity 的状态中处于 resumed 的大于 paused 状态的，即可认为有Activity处于前台状态中
        return resumed > paused;
    }
}
