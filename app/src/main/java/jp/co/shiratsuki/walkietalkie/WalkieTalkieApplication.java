package jp.co.shiratsuki.walkietalkie;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.CrashHandler;
import jp.co.shiratsuki.walkietalkie.utils.MyLifecycleHandler;

/**
 * Application类
 * Created by Li Yuliang on 2017/03/01.
 *
 * @author LiYuliang
 * @version 2018/02/24
 */

public class WalkieTalkieApplication extends Application {

    private static WalkieTalkieApplication instance;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SPHelper.init(this);
        //注册Activity生命周期回调
        registerActivityLifecycleCallbacks(new MyLifecycleHandler());
        // 捕捉异常
        CrashHandler.getInstance().init(this);
        // android 7.0系统解决拍照的问题
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }
    }

    /**
     * 单例模式中获取唯一的MyApplication实例
     *
     * @return application实例
     */
    public static WalkieTalkieApplication getInstance() {
        if (instance == null) {
            instance = new WalkieTalkieApplication();
        }
        return instance;
    }

}
