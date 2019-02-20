package jp.co.shiratsuki.walkietalkie.activity.appmain;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.activity.base.BaseActivity;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;
import jp.co.shiratsuki.walkietalkie.widget.MyToolbar;

/**
 * 长按耳机中键打开的页面
 * Created at 2019/2/20 9:55
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class LongPressActivity extends BaseActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_longpress);
        mContext = this;
        MyToolbar toolbar = findViewById(R.id.myToolbar);
        toolbar.initToolBar(this, toolbar, "长按耳机", R.drawable.back_white, onClickListener);
    }

    private View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.iv_left:
                ActivityController.finishActivity(this);
                break;
            default:
                break;
        }
    };

}
