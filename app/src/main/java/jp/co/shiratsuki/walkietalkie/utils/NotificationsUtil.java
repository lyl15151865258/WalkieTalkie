package jp.co.shiratsuki.walkietalkie.utils;

import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;

/**
 * 通知工具类
 * Created at 2018/11/28 13:54
 *
 * @author LiYuliang
 * @version 1.0
 */

public class NotificationsUtil {

    public static boolean isNotificationEnabled(Context context) {
        //API 19以下的版本无法获得通知栏权限，该方法默认会返回true
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        return manager.areNotificationsEnabled();
    }
}
