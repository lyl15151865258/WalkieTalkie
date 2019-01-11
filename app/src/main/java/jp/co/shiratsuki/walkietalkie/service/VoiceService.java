package jp.co.shiratsuki.walkietalkie.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.activity.MainActivity;
import jp.co.shiratsuki.walkietalkie.broadcast.BaseBroadcastReceiver;
import jp.co.shiratsuki.walkietalkie.constant.Command;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
/**
 * 局域网通信服务
 * Created at 2018/12/12 13:06
 *
 * @author LiYuliang
 * @version 1.0
 */

public class VoiceService extends Service {

    private final static String TAG = "VoiceService";

    private KeyEventBroadcastReceiver keyEventBroadcastReceiver;

    private MyHandler handler = new MyHandler(this);

    /**
     * Service与Runnable的通信
     */
    private static class MyHandler extends Handler {

        private VoiceService service;

        private MyHandler(VoiceService service) {
            this.service = service;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    }

    private BroadcastReceiver headsetPlugReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (BluetoothProfile.STATE_DISCONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                    // 蓝牙耳机移除
                    LogUtils.d(TAG, "蓝牙耳机移除");
                }
            } else if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                // 有线耳机移除，标记耳机按键状态为抬起并发送广播，停止录音
                Intent intent1 = new Intent();
                intent1.setAction("KEY_UP");
                context.sendBroadcast(intent1);
                SPHelper.save("KEY_STATUS_UP", true);
                LogUtils.d(TAG, "有线耳机移除");
            }
        }
    };

    private RemoteCallbackList<IVoiceCallback> mCallbackList = new RemoteCallbackList<>();

    public IVoiceService.Stub mBinder = new IVoiceService.Stub() {
        @Override
        public void startRecord() throws RemoteException {
            // 开始录音

        }

        @Override
        public void stopRecord() throws RemoteException {
            // 结束录音

        }

        @Override
        public void leaveGroup() throws RemoteException {
            // 发送离线消息

        }

        @Override
        public void registerCallback(IVoiceCallback callback) throws RemoteException {
            mCallbackList.register(callback);
        }

        @Override
        public void unRegisterCallback(IVoiceCallback callback) throws RemoteException {
            mCallbackList.unregister(callback);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        keyEventBroadcastReceiver = new KeyEventBroadcastReceiver();
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction("KEY_DOWN");
        filter1.addAction("KEY_UP");
        registerReceiver(keyEventBroadcastReceiver, filter1);

        IntentFilter filter3 = new IntentFilter();
        filter3.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        filter3.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(headsetPlugReceiver, filter3);

        showNotification();
    }

    /**
     * 前台Service
     */
    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
//        notificationIntent.setAction(Command.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("对讲机")
                .setTicker("对讲机")
                .setContentText("正在使用对讲机")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();

        startForeground(Command.FOREGROUND_SERVICE, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("IntercomService", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    // 按键事件广播
    private class KeyEventBroadcastReceiver extends BaseBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            if (("KEY_DOWN").equals(intent.getAction())) {
                LogUtils.d(TAG, "收到KEY_DOWN广播");
                try {
                    mBinder.startRecord();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (("KEY_UP").equals(intent.getAction())) {
                LogUtils.d(TAG, "收到KEY_UP广播");
                try {
                    mBinder.stopRecord();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (keyEventBroadcastReceiver != null) {
            unregisterReceiver(keyEventBroadcastReceiver);
        }
        if (headsetPlugReceiver != null) {
            unregisterReceiver(headsetPlugReceiver);
        }
        // 停止前台Service
        stopForeground(true);
        stopSelf();
    }
}
