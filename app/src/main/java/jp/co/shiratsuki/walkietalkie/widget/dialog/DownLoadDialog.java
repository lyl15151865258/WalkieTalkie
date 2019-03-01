package jp.co.shiratsuki.walkietalkie.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import jp.co.shiratsuki.walkietalkie.R;

/**
 * Created by LiYuliang on 2017/8/13 0013.
 * 下载文件的弹窗
 */

public class DownLoadDialog extends Dialog {
    private Context context;

    public DownLoadDialog(Context context) {
        super(context);
        this.context = context;
        setContentView(R.layout.dialog_download);
        initWindow();
    }

    private void initWindow() {
        Window dialogWindow = getWindow();
        if (dialogWindow != null) {
            dialogWindow.setBackgroundDrawable(new ColorDrawable(0));
            dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            DisplayMetrics d = context.getResources().getDisplayMetrics();
            lp.width = (int) (d.widthPixels * 0.8);
            lp.gravity = Gravity.CENTER;
            dialogWindow.setAttributes(lp);
        }
    }
}

