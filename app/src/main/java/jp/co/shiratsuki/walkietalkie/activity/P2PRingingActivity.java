package jp.co.shiratsuki.walkietalkie.activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.lang.reflect.Field;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;

/**
 * 一对一通话响铃
 * Created at 2019/2/13 8:16
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class P2PRingingActivity extends BaseActivity {

    private Context mContext;
    private String iconUrl, inviter;
    private TextView tvInviter;
    private ImageView ivUserIcon;
    private Ringtone ringtone;

    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_p2p_ringing);
        mContext = this;
        ivUserIcon = findViewById(R.id.iv_userIcon);
        tvInviter = findViewById(R.id.tv_inviter);
        findViewById(R.id.ring_hangoff).setOnClickListener(onClickListener);
        findViewById(R.id.ring_pickup).setOnClickListener(onClickListener);
        iconUrl = getIntent().getStringExtra("IconUrl");
        inviter = getIntent().getStringExtra("Inviter");
        // 播放手机系统自带来电铃声
//        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Uri uri = Uri.parse("android.resource://jp.co.shiratsuki.walkietalkie/" + R.raw.dengdaijieting);
        ringtone = RingtoneManager.getRingtone(mContext, uri);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvInviter.setText(inviter);
        RequestOptions options = new RequestOptions().error(R.drawable.photo_user).placeholder(R.drawable.photo_user).dontAnimate();
        Glide.with(this).load(iconUrl).apply(options).into(ivUserIcon);
        playRingtone();
    }

    private View.OnClickListener onClickListener = (v) -> {
        vibrator.vibrate(50);
        switch (v.getId()) {
            case R.id.ring_hangoff:
                // 挂断
                ActivityController.finishActivity(this);
                break;
            case R.id.ring_pickup:
                // 接听

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
}
