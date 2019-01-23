package jp.co.shiratsuki.walkietalkie.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jp.co.shiratsuki.walkietalkie.service.VoiceService;
import jp.co.shiratsuki.walkietalkie.service.WebSocketService;

/**
 * Activity栈队列管理类
 * Activity管理类，获取当前显示的Activity实例
 * Created at 2018/11/28 13:50
 *
 * @author LiYuliang
 * @version 1.0
 */

public class ActivityController {

    private static ActivityController sInstance = new ActivityController();
    /**
     * 采用弱引用持有 Activity ，避免造成 内存泄露
     */
    private WeakReference<Activity> sCurrentActivityWeakRef;

    /**
     * Activity栈队列，存放activity对象
     */
    public static List<Activity> activities = new LinkedList<>();

    private ActivityController() {

    }

    public static ActivityController getInstance() {
        return sInstance;
    }

    /**
     * 添加Activity对象到集合中
     *
     * @param activity Activity对象
     */
    public static void addActivity(Activity activity) {
        activities.add(activity);
        LogUtils.d("ActivityController", "addActivity：" + activity.getLocalClassName() + ",当前Activity栈队列中activity对象共有" + activities.size() + "个");
    }

    /**
     * 从集合中删除Activity对象
     *
     * @param activity Activity对象
     */
    public static void removeActivity(Activity activity) {
        if (activities.contains(activity)) {
            activities.remove(activity);
        }
        LogUtils.d("ActivityController", "removeActivity：" + activity.getLocalClassName() + ",当前Activity栈队列中activity对象共有" + activities.size() + "个");
    }

    /**
     * 从集合中删除并关闭Activity对象
     *
     * @param activity Activity对象
     */
    public static void finishActivity(Activity activity) {
        activity.finish();
        activities.remove(activity);
        LogUtils.d("ActivityController", "finishActivity：" + activity.getLocalClassName() + ",当前Activity栈队列中activity对象共有" + activities.size() + "个");
    }

    /**
     * 关闭并删除除了本Activity以外的所有Activity
     *
     * @param currentActivity 当前Activity对象（防止误操作把当前Activity也finish掉）
     *                        不能在对一个List进行遍历的时候将其中的元素删除掉，否则报异常：java.util.ConcurrentModificationException
     */
    public static void finishOtherActivity(Activity currentActivity) {
        //用来装需要删除的Activity对象
        List<Activity> delList = new ArrayList<>();
        for (Activity activity : activities) {
            if (!activity.isFinishing() && currentActivity != activity) {
                delList.add(activity);
                activity.finish();
            }
        }
        activities.removeAll(delList);
    }

    /**
     * 退出程序
     */
    public static void exit(Context context) {

        Intent intent = new Intent(context, WebSocketService.class);
        context.stopService(intent);
        Intent intent1 = new Intent(context, VoiceService.class);
        context.stopService(intent1);

        List<Activity> delList = new ArrayList<>();
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                delList.add(activity);
                activity.finish();
            }
        }
        activities.removeAll(delList);

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public Activity getCurrentActivity() {
        Activity currentActivity = null;
        if (sCurrentActivityWeakRef != null) {
            currentActivity = sCurrentActivityWeakRef.get();
        }
        return currentActivity;
    }

    public void setCurrentActivity(Activity activity) {
        sCurrentActivityWeakRef = new WeakReference<>(activity);
    }
}
