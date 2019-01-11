package jp.co.shiratsuki.walkietalkie.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.view.KeyEvent;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.WalkieTalkieApplication;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;

import java.lang.ref.WeakReference;

/**
 * 耳机媒体键广播
 * Created at 2018-12-13 17:00
 *
 * @author LiYuliang
 * @version 1.0
 */

public class MediaButtonReceiver extends BroadcastReceiver {

    private static final String TAG = "MediaButtonReceiver";
    private Context mContext;
    private static SyncTimeTask syncTimeTask;
    private static int leftSeconds;
    private static MediaPlayer mediaPlayer;
    private static boolean addTime = false;
    private static int leftTime;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        leftSeconds = SPHelper.getInt("SpeakTime", 30);
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            LogUtils.d(TAG, "KeyEvent----->" + keyEvent.getKeyCode() + "，KeyAction----->" + keyEvent.getAction());

            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {

                // 停止播放任何警报声
//                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//                    mediaPlayer.stop();
//                    mediaPlayer.release();
//                    mediaPlayer = null;
//                }

                if (SPHelper.getBoolean("KEY_STATUS_UP", true)) {
                    // 之前是抬起的，直接发送按下广播、停止音乐、开启倒计时
                    LogUtils.d(TAG, "之前是抬起的，发送按下的广播");
//                    if (SPHelper.getBoolean("CANT_SPEAK", true)) {
//                        // 被标记为不能讲话
//                        LogUtils.d(TAG, "你现在不能讲话");
//                        try {
//                            mediaPlayer = MediaPlayer.create(mContext, R.raw.dududu);
//                            mediaPlayer.setLooping(false);
//                            mediaPlayer.start();
//                            mediaPlayer.setVolume(0.1f, 0.1f);
//                        } catch (IllegalStateException e) {
//                            e.printStackTrace();
//                        }
//                    } else {
                        Intent intent1 = new Intent();
                        intent1.setAction("KEY_DOWN");
                        context.sendBroadcast(intent1);
                        SPHelper.save("KEY_STATUS_UP", false);
//                        startTimeDown();
//                    }
                } else {
                    // 之前是按下的
//                    if (addTime) {
//                        // 如果需要增加时间，直接重置倒计时时间
//                        LogUtils.d(TAG, "需要增加时间，恢复倒计时时间");
//                        leftTime = leftSeconds;
//                    } else {
                        // 如果不需要增加时间，则发送抬起的广播
                        LogUtils.d(TAG, "不需要增加时间，发送抬起的广播");
                        Intent intent1 = new Intent();
                        intent1.setAction("KEY_UP");
                        context.sendBroadcast(intent1);
                        SPHelper.save("KEY_STATUS_UP", true);
//                        if (syncTimeTask != null) {
//                            syncTimeTask.cancel(true);
//                            LogUtils.d(TAG, "取消syncTimeTask");
//                            syncTimeTask = null;
//                        } else {
//                            LogUtils.d(TAG, "syncTimeTask为空");
//                        }
//                    }
                }
            }
        }
    }

    /**
     * 开始录音并开始倒计时
     */
    public void startTimeDown() {
        leftTime = leftSeconds;
        if (syncTimeTask != null) {
            syncTimeTask.cancel(true);
            syncTimeTask = null;
        }
        syncTimeTask = new SyncTimeTask(this);
        syncTimeTask.execute();
    }

    private static class SyncTimeTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<MediaButtonReceiver> mediaButtonReceiverWeakReference;

        private SyncTimeTask(MediaButtonReceiver fragment) {
            mediaButtonReceiverWeakReference = new WeakReference<>(fragment);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (leftTime >= 0) {
                LogUtils.d(TAG, "循环中，是否被取消：" + isCancelled());
                if (isCancelled()) {
                    break;
                }
                publishProgress();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                leftTime--;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate();
            if (!isCancelled()) {
                MediaButtonReceiver mediaButtonReceiver = mediaButtonReceiverWeakReference.get();
                addTime = leftTime <= 6;
                LogUtils.d(TAG, "leftTime：" + leftTime + "，addTime：" + addTime);
                if (leftTime == 6) {
                    try {
                        mediaPlayer = MediaPlayer.create(WalkieTalkieApplication.getInstance(), R.raw.dididi);
                        mediaPlayer.setLooping(false);
                        mediaPlayer.start();
                        mediaPlayer.setVolume(0.1f, 0.1f);
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
                if (leftTime <= 0) {
                    Intent intent1 = new Intent();
                    intent1.setAction("KEY_UP");
                    mediaButtonReceiver.mContext.sendBroadcast(intent1);
                    SPHelper.save("KEY_STATUS_UP", true);
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}