package jp.co.shiratsuki.walkietalkie.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;

/**
 * 耳机媒体键广播
 * Created at 2018-12-13 17:00
 *
 * @author LiYuliang
 * @version 1.0
 */

public class MediaButtonReceiver extends BroadcastReceiver {

    private static final String TAG = "MediaButtonReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            LogUtils.d(TAG, "KeyEvent----->" + keyEvent.getKeyCode() + "，KeyAction----->" + keyEvent.getAction());

            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                if (SPHelper.getBoolean("KEY_STATUS_UP", true)) {
                    LogUtils.d(TAG, "发送按下的广播");
                    Intent intent1 = new Intent();
                    intent1.setAction("KEY_DOWN");
                    context.sendBroadcast(intent1);
                } else {
                    LogUtils.d(TAG, "发送抬起的广播");
                    Intent intent1 = new Intent();
                    intent1.setAction("KEY_UP");
                    context.sendBroadcast(intent1);
                }
            }
        }
    }
}