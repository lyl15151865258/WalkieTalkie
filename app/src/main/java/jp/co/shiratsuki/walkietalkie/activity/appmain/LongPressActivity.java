package jp.co.shiratsuki.walkietalkie.activity.appmain;

import android.content.Intent;
import android.os.Bundle;

import jp.co.shiratsuki.walkietalkie.activity.base.BaseActivity;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;

/**
 * 长按耳机中键，打开本Activity发送一个广播立刻关闭，不显示
 * Created at 2019/2/20 9:55
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class LongPressActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 发送一个广播
        Intent intent = new Intent();
        intent.setAction("MEDIA_BUTTON_LONG_PRESS");
        sendBroadcast(intent);
        ActivityController.finishActivity(this);
    }

}
