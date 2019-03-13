package jp.co.shiratsuki.walkietalkie.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;

import org.webrtc.MediaStream;

import java.util.ArrayList;
import java.util.UUID;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.bean.User;
import jp.co.shiratsuki.walkietalkie.broadcast.BaseBroadcastReceiver;
import jp.co.shiratsuki.walkietalkie.broadcast.MediaButtonReceiver;
import jp.co.shiratsuki.walkietalkie.broadcast.VolumeChangeObserver;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.DbcSbcUtils;
import jp.co.shiratsuki.walkietalkie.utils.GsonUtils;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.webrtc.IWebRTCHelper;
import jp.co.shiratsuki.walkietalkie.webrtc.WebRTCHelper;
import jp.co.shiratsuki.walkietalkie.constant.NetWork;

/**
 * 局域网通信服务
 * Created at 2018/12/12 13:06
 *
 * @author LiYuliang
 * @version 1.0
 */

public class VoiceService extends Service implements IWebRTCHelper, VolumeChangeObserver.VolumeChangeListener {

    private Ringtone ringtone;
    private boolean isInRoom = false;

    enum TYPE {
        EnterRoom, MaxTalker, LeaveRoom, LeaveGroup, StartRecord, StopRecord, UseSpeaker, UseEarpiece, DeleteUser
    }

    private final static String TAG = "VoiceService";
    private WebRTCHelper helper;

    private KeyEventBroadcastReceiver keyEventBroadcastReceiver;

    private RemoteCallbackList<IVoiceCallback> mCallbackList = new RemoteCallbackList<>();

    private AudioManager mAudioManager;
    private ComponentName mComponentName;

    private VolumeChangeObserver mVolumeChangeObserver;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "VoiceService——————生命周期——————:onCreate");

        keyEventBroadcastReceiver = new KeyEventBroadcastReceiver();
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction("KEY_DOWN");
        filter1.addAction("KEY_UP");
        filter1.addAction("MEDIA_BUTTON_LONG_PRESS");
        filter1.setPriority(1000);
        registerReceiver(keyEventBroadcastReceiver, filter1);

        IntentFilter filter3 = new IntentFilter();
        filter3.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        filter3.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(headsetPlugReceiver, filter3);

        showNotification();

        helper = new WebRTCHelper(this, this, NetWork.iceServers);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // 注册媒体按键
        registerMediaButton();

        // 记录当前媒体音量
        SPHelper.save("defaultVolume", mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        //实例化对象并设置监听器
        mVolumeChangeObserver = new VolumeChangeObserver(this);
        mVolumeChangeObserver.setVolumeChangeListener(this);
        mVolumeChangeObserver.registerReceiver();

        User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);

        String uniqueCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        if (user.getVoice_ip().equals("") || user.getVoice_port().equals("")) {
            // 如果用户没有填写完整语音服务器信息，则采用默认值
            String signal = DbcSbcUtils.getPatStr("ws://" + NetWork.WEBRTC_SERVER_IP + ":" + NetWork.WEBRTC_SERVER_PORT + "/WalkieTalkieServer/" + user.getUser_id() + "/" + user.getUser_id() + uniqueCode);
            helper.initSocket(signal, false);
        } else {
            String signal = DbcSbcUtils.getPatStr("ws://" + user.getVoice_ip() + ":" + user.getVoice_port() + "/WalkieTalkieServer/" + user.getUser_id() + "/" + user.getUser_id() + uniqueCode);
            helper.initSocket(signal, false);
        }
    }

    // 注册耳机按钮事件
    private void registerMediaButton() {
        //对媒体播放按钮进行封装
        mComponentName = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
        //注册封装的ComponentName
        mAudioManager.registerMediaButtonEventReceiver(mComponentName);
        //当应用开始播放的时候首先需要请求焦点，调用该方法后，原先获取焦点的应用会释放焦点
        mAudioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    //焦点问题
    private AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:// 长时间失去
                    mAudioManager.unregisterMediaButtonEventReceiver(mComponentName);
                    mAudioManager.abandonAudioFocus(focusChangeListener);//放弃焦点监听
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:// 短时间失去
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:// 短时间失去，但可以共用
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:// 获得音频焦点
                    mAudioManager.registerMediaButtonEventReceiver(mComponentName);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d(TAG, "VoiceService——————生命周期——————:onBind");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "VoiceService——————生命周期——————:onStartCommand");
        return super.onStartCommand(intent, flags, startId);
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

    public IVoiceService.Stub mBinder = new IVoiceService.Stub() {

        @Override
        public void enterRoom(String roomId) {
            // 进入房间
            if (helper.socketIsOpen()) {
                try {
                    helper.joinRoom(roomId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void leaveRoom() {
            // 离开房间
            try {
                mBinder.stopRecord();
                helper.exitRoom();
                broadcastCallback(TYPE.LeaveRoom, null);
                SPHelper.save("KEY_STATUS_UP", true);
                isInRoom = false;
                LogUtils.d(TAG, "离开房间");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void leaveGroup() {
            // 与服务器断开连接
            try {
                mBinder.stopRecord();
                helper.exitRoom();
                broadcastCallback(TYPE.LeaveGroup, null);
                SPHelper.save("KEY_STATUS_UP", true);
                isInRoom = false;
                LogUtils.d(TAG, "离开房间");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void startRecord() {
            try {
                if (isInRoom) {
                    // 播放提示音
                    if (ringtone != null && ringtone.isPlaying()) {
                        ringtone.stop();
                    }
                    Uri uri = Uri.parse("android.resource://jp.co.shiratsuki.walkietalkie/" + R.raw.dingdong);
                    ringtone = RingtoneManager.getRingtone(VoiceService.this, uri);
                    ringtone.setStreamType(AudioManager.STREAM_RING);
                    ringtone.play();

                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // 打开麦克风
                        helper.sendSpeakStatus(true);
                        helper.toggleMute(true);
                        broadcastCallback(TYPE.StartRecord, null);
                    }).start();

                    SPHelper.save("KEY_STATUS_UP", false);
                } else {
                    if (ringtone != null && ringtone.isPlaying()) {
                        ringtone.stop();
                    }
                    Uri uri = Uri.parse("android.resource://jp.co.shiratsuki.walkietalkie/" + R.raw.du);
                    ringtone = RingtoneManager.getRingtone(VoiceService.this, uri);
                    ringtone.setStreamType(AudioManager.STREAM_RING);
                    ringtone.play();
                    SPHelper.save("KEY_STATUS_UP", true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void stopRecord() {
            try {
                if (isInRoom) {
                    // 关闭麦克风
                    helper.sendSpeakStatus(false);
                    helper.toggleMute(false);
                    broadcastCallback(TYPE.StopRecord, null);
                    // 播放提示音
                    if (ringtone != null && ringtone.isPlaying()) {
                        ringtone.stop();
                    }
                    Uri uri = Uri.parse("android.resource://jp.co.shiratsuki.walkietalkie/" + R.raw.du);
                    ringtone = RingtoneManager.getRingtone(VoiceService.this, uri);
                    ringtone.setStreamType(AudioManager.STREAM_RING);
                    ringtone.play();
                    SPHelper.save("KEY_STATUS_UP", true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void useSpeaker() {
            try {
                helper.toggleSpeaker(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            broadcastCallback(TYPE.UseSpeaker, null);
        }

        @Override
        public void useEarpiece() {
            try {
                helper.toggleSpeaker(false);
                broadcastCallback(TYPE.UseEarpiece, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void callOthers(String userId) {
            try {
                helper.callOthers(userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void cancelP2PCall(String userId) {
            try {
                helper.cancelP2PCall(userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void rejectP2PCall(String userId) {
            try {
                helper.rejectP2PCall(userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void acceptP2PCall(String userId) {
            try {
                helper.acceptP2PCall(userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void timeOut(String userId) {
            // 等待或者拨号超时
            try {
                helper.timeOut(userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void registerCallback(IVoiceCallback callback) {
            mCallbackList.register(callback);
        }

        @Override
        public void unRegisterCallback(IVoiceCallback callback) {
            mCallbackList.unregister(callback);
        }

        @Override
        public void stopSelf() {
            VoiceService.this.stopSelf();
        }
    };

    /**
     * 前台Service
     */
    private void showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel Channel = new NotificationChannel("123", getString(R.string.VoiceService), NotificationManager.IMPORTANCE_NONE);
            Channel.enableLights(true);                                             //设置提示灯
            Channel.setLightColor(Color.RED);                                       //设置提示灯颜色
            Channel.setShowBadge(true);                                             //显示logo
            Channel.setDescription(getString(R.string.UsingWalkieTalkie));          //设置描述
            Channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);        //设置锁屏不可见 VISIBILITY_SECRET=不可见
            manager.createNotificationChannel(Channel);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "123");
            notification.setContentTitle(getString(R.string.app_name));
            notification.setContentText(getString(R.string.VoiceServiceRunning));
            notification.setWhen(System.currentTimeMillis());
            notification.setSmallIcon(R.mipmap.ic_launcher);
            notification.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            startForeground(123, notification.build());
        } else {
            Notification notification = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.app_name))                                      //设置标题
                    .setContentText(getString(R.string.UsingWalkieTalkie))                              //设置内容
                    .setWhen(System.currentTimeMillis())                                                //设置创建时间
                    .setSmallIcon(R.mipmap.ic_launcher)                                                 //设置状态栏图标
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))   //设置通知栏图标
                    .build();
            startForeground(123, notification);
        }
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
            } else if (("MEDIA_BUTTON_LONG_PRESS").equals(intent.getAction())) {
                LogUtils.d(TAG, "收到MEDIA_BUTTON_LONG_PRESS广播");
                // 标记为用户正常退出房间
                SPHelper.save("NormalExit", true);
                try {
                    mBinder.leaveRoom();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onOverMaxTalker(String roomId) {
        Intent intent = new Intent();
        intent.putExtra("roomId", roomId);
        broadcastCallback(TYPE.MaxTalker, intent);
    }

    @Override
    public void onSetLocalStream(MediaStream stream, String socketId) {

    }

    @Override
    public void onAddRemoteStream(MediaStream stream, String socketId) {

    }

    @Override
    public void onEnterRoom() {
        isInRoom = true;
        broadcastCallback(TYPE.EnterRoom, null);
        helper.toggleMute(false);
        helper.toggleSpeaker(false);
    }

    @Override
    public void onLeaveRoom() {
        broadcastCallback(TYPE.LeaveRoom, null);
        isInRoom = false;
    }

    @Override
    public void onLeaveGroup() {
        broadcastCallback(TYPE.LeaveGroup, null);
        isInRoom = false;
    }

    @Override
    public void onCloseWithId(String socketId) {

    }

    @Override
    public void removeUser(String userId) {
        Intent intent = new Intent();
        intent.putExtra("userId", userId);
        broadcastCallback(TYPE.DeleteUser, intent);
    }

    @Override
    public void someoneLeaveRoom(String roomId, ArrayList<User> userList) {
        // 有人离开了房间，判断这个人是不是与自己进行一对一通话
        if (isInRoom && SPHelper.getBoolean("P2PChat", false)) {
            // 标记为用户正常退出房间
            SPHelper.save("NormalExit", true);
            Intent intent = new Intent();
            intent.setAction("MEDIA_BUTTON_LONG_PRESS");
            sendBroadcast(intent);
        }
        // 更新房间联系人
        updateVolume(userList, "UPDATE_CONTACTS_ROOM");
    }

    @Override
    public void updateRoomContacts(ArrayList<User> userList) {
        updateVolume(userList, "UPDATE_CONTACTS_ROOM");
    }

    @Override
    public void updateRoomSpeakStatus(ArrayList<User> userList) {
        updateVolume(userList, "UPDATE_SPEAK_STATUS");
    }

    @Override
    public void updateContacts(ArrayList<User> userList) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("userList", userList);
        intent.setAction("UPDATE_CONTACTS");
        LogUtils.d(TAG, "总联系人数量：" + userList.size());
        sendBroadcast(intent);
    }

    /**
     * 更新音量
     *
     * @param userList 联系人列表
     * @param action   发送广播的Action
     */
    private void updateVolume(ArrayList<User> userList, String action) {
        // 调节音量
        boolean someoneSpeaking = false;
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).isSpeaking()) {
                someoneSpeaking = true;
                break;
            }
        }

        Intent intent = new Intent();
        // 如果所有人都不讲话了
        if (!someoneSpeaking) {
            SPHelper.save("SomeoneSpeaking", false);
            int silentVolume = SPHelper.getInt("SilentVolume", mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 4 * 3);
            LogUtils.d(TAG, "所有人都不讲话了，当前音量为：" + silentVolume);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, silentVolume, AudioManager.FLAG_VIBRATE);

            // 通知Activity修改音量键默认调节的音量类型
            intent.putExtra("VolumeControlStream", AudioManager.STREAM_MUSIC);
        } else {
            SPHelper.save("SomeoneSpeaking", true);
            int talkVolume = SPHelper.getInt("TalkVolume", mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2);
            LogUtils.d(TAG, "当前有人在讲话，当前音量为：" + talkVolume);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, talkVolume, AudioManager.FLAG_VIBRATE);

            // 通知Activity修改音量键默认调节的音量类型
            intent.putExtra("VolumeControlStream", AudioManager.STREAM_VOICE_CALL);
        }
        intent.putParcelableArrayListExtra("userList", userList);
        intent.setAction(action);
        LogUtils.d(TAG, "房间内联系人数量：" + userList.size());
        sendBroadcast(intent);
    }

    // 回调公共方法
    private synchronized void broadcastCallback(TYPE type, Intent intent) {
        try {
            final int size = mCallbackList.beginBroadcast();
            for (int i = 0; i < size; i++) {
                LogUtils.d(TAG, "走回调方法broadcastCallback");
                IVoiceCallback callback = mCallbackList.getBroadcastItem(i);
                if (callback != null) {
                    if (type == TYPE.EnterRoom) {
                        callback.enterRoomSuccess();
                        LogUtils.d(TAG, "走回调方法broadcastCallback，EnterRoom");
                    } else if (type == TYPE.MaxTalker) {
                        String roomId = intent.getStringExtra("roomId");
                        callback.onOverMaxTalker(roomId);
                        LogUtils.d(TAG, "走回调方法broadcastCallback，MaxTalker");
                    } else if (type == TYPE.LeaveRoom) {
                        callback.leaveRoomSuccess();
                        LogUtils.d(TAG, "走回调方法broadcastCallback，LeaveRoom");
                    } else if (type == TYPE.LeaveGroup) {
                        callback.leaveGroupSuccess();
                        LogUtils.d(TAG, "走回调方法broadcastCallback，LeaveGroup");
                    } else if (type == TYPE.StartRecord) {
                        callback.startRecordSuccess();
                        LogUtils.d(TAG, "走回调方法broadcastCallback，StartRecord");
                    } else if (type == TYPE.StopRecord) {
                        callback.stopRecordSuccess();
                        LogUtils.d(TAG, "走回调方法broadcastCallback，StopRecord");
                    } else if (type == TYPE.UseSpeaker) {
                        callback.useSpeakerSuccess();
                        LogUtils.d(TAG, "走回调方法broadcastCallback，UseSpeaker");
                    } else if (type == TYPE.UseEarpiece) {
                        callback.useEarpieceSuccess();
                        LogUtils.d(TAG, "走回调方法broadcastCallback，UseEarpiece");
                    } else if (type == TYPE.DeleteUser) {
                        if (intent != null) {
                            String userId = intent.getStringExtra("userId");
                            callback.removeUser(userId, "");
                        }
                        LogUtils.d(TAG, "走回调方法broadcastCallback，DeleteUser");
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            mCallbackList.finishBroadcast();
        }
    }

    //网络状态广播
    public class NetworkStatusReceiver extends BaseBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 监听wifi的打开与关闭，与wifi的连接无关
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                    //wifi关闭
                    LogUtils.d(TAG, "wifi已关闭");
                    Intent intent1 = new Intent();
                    intent1.setAction("WIFI_DISCONNECTED");
                    VoiceService.this.sendBroadcast(intent1);
                } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    //wifi开启
                    LogUtils.d(TAG, "wifi已开启");
                } else if (wifiState == WifiManager.WIFI_STATE_ENABLING) {
                    //wifi开启中
                    LogUtils.d(TAG, "wifi开启中");
                } else if (wifiState == WifiManager.WIFI_STATE_DISABLING) {
                    //wifi关闭中
                    LogUtils.d(TAG, "wifi关闭中");
                }
            }
            // 监听wifi的连接状态即是否连上了一个有效无线路由
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (parcelableExtra != null) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        //已连接网络
                        LogUtils.d(TAG, "wifi 已连接网络");
                        if (networkInfo.isAvailable()) {//并且网络可用
                            LogUtils.d(TAG, "wifi 已连接网络，并且可用");
                        } else {//并且网络不可用
                            LogUtils.d(TAG, "wifi 已连接网络，但不可用");
                        }
                        Intent intent1 = new Intent();
                        intent1.setAction("WIFI_CONNECTED");
                        VoiceService.this.sendBroadcast(intent1);
                    } else {
                        //网络未连接
                        LogUtils.d(TAG, "wifi 未连接网络");
                    }
                } else {
                    LogUtils.d(TAG, "wifi parcelableExtra为空");
                }
            }
            // 监听网络连接，总网络判断，即包括wifi和移动网络的监听
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                //连上的网络类型判断：wifi还是移动网络
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    LogUtils.d(TAG, "总网络 连接的是wifi网络");
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    LogUtils.d(TAG, "总网络 连接的是移动网络");
                }
                //具体连接状态判断
                checkNetworkStatus(networkInfo);
            }
        }

        private void checkNetworkStatus(NetworkInfo networkInfo) {
            if (networkInfo != null) {
                LogUtils.d(TAG, "总网络 info非空");
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {//已连接网络
                    LogUtils.d(TAG, "总网络 已连接网络");
                    if (networkInfo.isAvailable()) {//并且网络可用
                        LogUtils.d(TAG, "总网络 已连接网络，并且可用");
                    } else {//并且网络不可用
                        LogUtils.d(TAG, "总网络 已连接网络，但不可用");
                    }
                } else if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {//网络未连接
                    LogUtils.d(TAG, "总网络 未连接网络");
                }
            } else {
                LogUtils.d(TAG, "总网络 info为空");
            }
        }
    }

    @Override
    public void onVolumeChanged(int currentVolume) {
        LogUtils.d(TAG, "系统媒体音量发生变化，当前媒体音量为：" + currentVolume);
        if (SPHelper.getBoolean("SomeoneSpeaking", false)) {
            // 有人正在讲话
            SPHelper.save("TalkVolume", currentVolume);
            LogUtils.d(TAG, "当前有人在讲话，保存默认音量：" + currentVolume);
        } else {
            // 没有人在讲话
            LogUtils.d(TAG, "所有人都不讲话了，保存默认音量：" + currentVolume);
            SPHelper.save("SilentVolume", currentVolume);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "VoiceService——————生命周期——————:onDestroy");
        helper.release();
        if (keyEventBroadcastReceiver != null) {
            unregisterReceiver(keyEventBroadcastReceiver);
        }
        if (headsetPlugReceiver != null) {
            unregisterReceiver(headsetPlugReceiver);
        }
        mCallbackList.kill();
        mAudioManager.unregisterMediaButtonEventReceiver(mComponentName);
        mVolumeChangeObserver.unregisterReceiver();
        // 停止前台Service
        stopForeground(true);
        stopSelf();
    }
}