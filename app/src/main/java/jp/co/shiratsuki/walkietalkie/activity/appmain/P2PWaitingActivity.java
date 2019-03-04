package jp.co.shiratsuki.walkietalkie.activity.appmain;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.activity.base.BaseActivity;
import jp.co.shiratsuki.walkietalkie.bean.User;
import jp.co.shiratsuki.walkietalkie.constant.NetWork;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.service.IVoiceService;
import jp.co.shiratsuki.walkietalkie.service.VoiceService;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;
import jp.co.shiratsuki.walkietalkie.utils.GsonUtils;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;

/**
 * 一对一通话等待接听页面
 * Created at 2019/2/21 2:24
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class P2PWaitingActivity extends BaseActivity {

    private final static String TAG = "P2PWaitingActivity";
    private String myUserId;
    private String userId, iconUrl, destination;
    private TextView tvDestination, tvMessage;
    private ImageView ivUserIcon;
    private Ringtone ringtone;

    private Vibrator vibrator;
    private IVoiceService iVoiceService;
    private SyncTimeTask syncTimeTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_p2p_waiting);
        SPHelper.save("CanPlay", false);
        myUserId = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class).getUser_id();
        ivUserIcon = findViewById(R.id.iv_userIcon);
        tvDestination = findViewById(R.id.tv_destination);
        tvMessage = findViewById(R.id.tvMessage);
        findViewById(R.id.ivCancel).setOnClickListener(onClickListener);
        User user = getIntent().getParcelableExtra("user");
        userId = user.getUser_id();
        destination = user.getUser_name();
        iconUrl = ("http://" + NetWork.SERVER_HOST_MAIN + ":" + NetWork.SERVER_PORT_MAIN + user.getIcon_url()).replace("\\", "/");

        // 如果铃声音量为0，将铃声调至最大铃声音量的一半
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int ringVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        if (ringVolume == 0) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING) / 2, AudioManager.FLAG_VIBRATE);
        }

        // 播放手机系统自带来电铃声
//        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Uri uri = Uri.parse("android.resource://jp.co.shiratsuki.walkietalkie/" + R.raw.dengdaijieting);
        ringtone = RingtoneManager.getRingtone(this, uri);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        Intent intent = new Intent(this, VoiceService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter();
        filter.addAction("P2P_VOICE_REQUEST_REJECT");
        filter.addAction("P2P_VOICE_REQUEST_ACCEPT");
        filter.addAction("P2P_VOICE_REQUEST_ERROR");
        filter.addAction("VOICE_WEBSOCKET_DISCONNECT");
        registerReceiver(myReceiver, filter);

        syncTimeTask = new SyncTimeTask(this, NetWork.CALL_WAIT_TIME);
        syncTimeTask.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvDestination.setText(destination);
        RequestOptions options = new RequestOptions().error(R.drawable.photo_user).placeholder(R.drawable.photo_user).dontAnimate();
        Glide.with(this).load(iconUrl).apply(options).into(ivUserIcon);
        playRingtone();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iVoiceService = IVoiceService.Stub.asInterface(service);
            LogUtils.d(TAG, "绑定Service成功");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iVoiceService = null;
        }
    };

    private View.OnClickListener onClickListener = (v) -> {
        vibrator.vibrate(50);
        switch (v.getId()) {
            case R.id.ivCancel:
                // 取消
                try {
                    iVoiceService.cancelP2PCall(userId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                playMusicAndFinish();
                break;
            default:
                break;
        }
    };

    //反射设置闹铃重复播放
    private void setRingtoneRepeat(Ringtone ringtone) {
        Class<Ringtone> clazz = Ringtone.class;
        try {
            Field field = clazz.getDeclaredField("mLocalPlayer");//返回一个 Field 对象，它反映此 Class 对象所表示的类或接口的指定公共成员字段（※这里要进源码查看属性字段）
            field.setAccessible(true);
            MediaPlayer target = (MediaPlayer) field.get(ringtone);     //返回指定对象上此 Field 表示的字段的值
            target.setLooping(true);//设置循环
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    //播放铃声
    private void playRingtone() {
        ringtone.setStreamType(AudioManager.STREAM_RING);   //因为rt.stop()使得MediaPlayer置null,所以要重新创建（具体看源码）
        setRingtoneRepeat(ringtone);                        //设置重复提醒
        ringtone.play();
    }

    //停止铃声
    private void stopRingtone() {
        ringtone.stop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRingtone();
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("P2P_VOICE_REQUEST_REJECT".equals(action)) {
                tvMessage.setText(getString(R.string.TargetReject));
                playMusicAndFinish();
            }
            if ("P2P_VOICE_REQUEST_ACCEPT".equals(action)) {
                // 标记这是一对一通话
                SPHelper.save("P2PChat", true);
                ActivityController.finishActivity(P2PWaitingActivity.this);
            }
            if ("VOICE_WEBSOCKET_DISCONNECT".equals(action)) {
                tvMessage.setText(getString(R.string.NetworkError));
                playMusicAndFinish();
            }
            if ("P2P_VOICE_REQUEST_ERROR".equals(action)) {
                tvMessage.setText(getString(R.string.TargetBusy));
                playMusicAndFinish();
            }
        }
    };

    private static class SyncTimeTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<P2PWaitingActivity> p2PWaitingActivityWeakReference;
        private int leftTime;

        private SyncTimeTask(P2PWaitingActivity p2PWaitingActivity, int leftTime) {
            LogUtils.d(TAG, "开始倒计时");
            p2PWaitingActivityWeakReference = new WeakReference<>(p2PWaitingActivity);
            this.leftTime = leftTime;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (leftTime >= 0) {
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
                LogUtils.d(TAG, "倒计时时间剩余：" + leftTime);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate();
            if (isCancelled()) {
                return;
            }
            if (leftTime <= 0) {
                P2PWaitingActivity p2PWaitingActivity = p2PWaitingActivityWeakReference.get();
                try {
                    p2PWaitingActivity.iVoiceService.timeOut(p2PWaitingActivity.myUserId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                p2PWaitingActivity.tvMessage.setText(p2PWaitingActivity.getString(R.string.TargetUnanswered));
                p2PWaitingActivity.playMusicAndFinish();
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    // 播放du音乐并关闭当前Activity
    private void playMusicAndFinish() {
        // 播放提示音
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
        Uri uri = Uri.parse("android.resource://jp.co.shiratsuki.walkietalkie/" + R.raw.du);
        ringtone = RingtoneManager.getRingtone(P2PWaitingActivity.this, uri);
        ringtone.setStreamType(AudioManager.STREAM_RING);
        ringtone.play();
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SPHelper.save("CanPlay", true);
            ActivityController.finishActivity(P2PWaitingActivity.this);
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (syncTimeTask != null && syncTimeTask.getStatus() == AsyncTask.Status.RUNNING) {
            syncTimeTask.cancel(true);
        }
        unregisterReceiver(myReceiver);
        unbindService(serviceConnection);
    }
}
