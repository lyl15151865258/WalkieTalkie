package jp.co.shiratsuki.walkietalkie.widget.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import jp.co.shiratsuki.walkietalkie.R;

/**
 * 加载中Dialog
 * Created at 2018/11/28 13:56
 *
 * @author LiYuliang
 * @version 1.0
 */

public class LoadingDialog extends AlertDialog {

    private TextView tvMessage;

    public LoadingDialog(Context context, int theme) {
        super(context, theme);
    }

    public void setMessage(CharSequence message) {
        if (tvMessage != null) {
            tvMessage.setText(message);
            if (TextUtils.isEmpty(message)) {
                tvMessage.setVisibility(View.GONE);
            } else {
                tvMessage.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_loading);
        tvMessage = findViewById(R.id.tvMessage);
    }
}
