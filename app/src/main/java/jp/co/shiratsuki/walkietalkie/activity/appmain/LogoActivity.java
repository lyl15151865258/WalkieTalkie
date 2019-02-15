package jp.co.shiratsuki.walkietalkie.activity.appmain;

import android.os.Bundle;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.activity.MainActivity;
import jp.co.shiratsuki.walkietalkie.activity.base.BaseActivity;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;

/**
 * Logo页面
 * Created at 2018/11/20 13:37
 *
 * @author LiYuliang
 * @version 1.0
 */

public class LogoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        try {
            Thread.sleep(800);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            openActivity(MainActivity.class);
            ActivityController.finishActivity(this);
        }
    }

    /**
     * Logo页面不允许退出
     */
    @Override
    public void onBackPressed() {

    }

}
