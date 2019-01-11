package jp.co.shiratsuki.walkietalkie.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import jp.co.shiratsuki.walkietalkie.R;

/**
 * 通用的Dialog
 * Created at 2018/11/28 13:55
 *
 * @author LiYuliang
 * @version 1.0
 */

public class CommonWarningDialog extends Dialog {
    private Context context;
    private String text;
    private OnDialogClickListener dialogClickListener;

    public CommonWarningDialog(Context context, String text) {
        super(context);
        this.context = context;
        this.text = text;
        initView();
    }

    //初始化View
    private void initView() {
        setContentView(R.layout.dialog_warning_common);
        initWindow();
        Button okBtn = findViewById(R.id.btn_ok);
        Button cancelBtn = findViewById(R.id.btn_cancel);
        TextView tvWarning = findViewById(R.id.tv_warning);
        tvWarning.setText(text);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (dialogClickListener != null) {
                    dialogClickListener.onOKClick();
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                if (dialogClickListener != null) {
                    dialogClickListener.onCancelClick();
                }
            }
        });
    }

    /**
     * 添加黑色半透明背景
     */
    private void initWindow() {
        Window dialogWindow = getWindow();
        if (dialogWindow != null) {
            dialogWindow.setBackgroundDrawable(new ColorDrawable(0));//设置window背景
            dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);//设置输入法显示模式
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            DisplayMetrics d = context.getResources().getDisplayMetrics();//获取屏幕尺寸
            lp.width = (int) (d.widthPixels * 0.9); //宽度为屏幕80%
            lp.gravity = Gravity.CENTER;  //中央居中
            dialogWindow.setAttributes(lp);
        }
    }

    public void setOnDialogClickListener(OnDialogClickListener clickListener) {
        dialogClickListener = clickListener;
    }

    /**
     * 添加按钮点击事件
     */
    public interface OnDialogClickListener {
        void onOKClick();

        void onCancelClick();
    }
}

