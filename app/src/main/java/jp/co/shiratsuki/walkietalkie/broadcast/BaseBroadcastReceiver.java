package jp.co.shiratsuki.walkietalkie.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import jp.co.shiratsuki.walkietalkie.R;

/**
 * 广播父类，主要是重写Toast方法，采用自定义控件，保证整个App的Toast样式一致
 * Created at 2018/11/20 13:41
 *
 * @author LiYuliang
 * @version 1.0
 */

public class BaseBroadcastReceiver extends BroadcastReceiver {

    private Context context;
    private Toast toast;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
    }

    public void showToast(int resId) {
        showToast(context.getString(resId));
    }

    /**
     * 自定义的Toast，避免重复出现
     *
     * @param msg 需要显示的文本
     */
    public void showToast(String msg) {
        if (toast == null) {
            toast = new Toast(context);
            //获取自定义视图
            View view = LayoutInflater.from(context).inflate(R.layout.view_toast, null);
            TextView tvMessage = view.findViewById(R.id.tv_toast_text);
            //设置文本
            tvMessage.setText(msg);
            //设置视图
            toast.setView(view);
            //设置显示时长
            toast.setDuration(Toast.LENGTH_SHORT);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.view_toast, null);
            TextView tvMessage = view.findViewById(R.id.tv_toast_text);
            //设置文本
            tvMessage.setText(msg);
            //设置视图
            toast.setView(view);
            //设置显示时长
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }
}
