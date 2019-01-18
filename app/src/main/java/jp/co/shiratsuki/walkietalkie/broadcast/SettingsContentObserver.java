package jp.co.shiratsuki.walkietalkie.broadcast;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;

/**
 * 监听系统音量变化
 * Created at 2018/12/29 17:33
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class SettingsContentObserver extends ContentObserver {

    private static final String TAG = "SettingsContentObserver";
    private Context context;

    public SettingsContentObserver(Context c, Handler handler) {
        super(handler);
        context = c;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (SPHelper.getBoolean("SomeoneSpeaking", false)) {
            // 有人正在讲话
            SPHelper.save("defaultVolume", audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) + 4);

        } else {
            // 没有人在讲话
            SPHelper.save("defaultVolume", currentVolume);
        }
    }
}
