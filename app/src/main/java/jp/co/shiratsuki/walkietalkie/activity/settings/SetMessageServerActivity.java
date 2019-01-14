package jp.co.shiratsuki.walkietalkie.activity.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.activity.BaseActivity;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;
import jp.co.shiratsuki.walkietalkie.utils.RegexUtils;
import jp.co.shiratsuki.walkietalkie.utils.ViewUtils;
import jp.co.shiratsuki.walkietalkie.widget.MyToolbar;

/**
 * 设置消息服务器页面
 * Created at 2019/1/15 5:34
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class SetMessageServerActivity extends BaseActivity {

    private Context mContext;
    private EditText etMessageServerIP, etMessageServerPort;
    private Button btnModify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_message_server);
        mContext = this;
        MyToolbar toolbar = findViewById(R.id.myToolbar);
        toolbar.initToolBar(this, toolbar, getString(R.string.VoiceServerSetting), R.drawable.back_white, onClickListener);
        btnModify = findViewById(R.id.btn_modify);
        btnModify.setOnClickListener(onClickListener);
        etMessageServerIP = findViewById(R.id.etMessageServerIP);
        etMessageServerPort = findViewById(R.id.etMessageServerPort);
        etMessageServerIP.addTextChangedListener(textWatcher);
        etMessageServerPort.addTextChangedListener(textWatcher);
        etMessageServerIP.setText(SPHelper.getString("MessageServerIP", ""));
        etMessageServerPort.setText(SPHelper.getString("MessageServerPort", ""));
        ViewUtils.setCharSequence(etMessageServerIP);
        ViewUtils.setCharSequence(etMessageServerPort);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (TextUtils.isEmpty(etMessageServerIP.getText().toString()) || TextUtils.isEmpty(etMessageServerPort.getText().toString())) {
                btnModify.setEnabled(false);
            } else {
                btnModify.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.iv_left:
                ActivityController.finishActivity(this);
                break;
            case R.id.btn_modify:
                // 检查IP和端口格式是否正确
                String ip = etMessageServerIP.getText().toString().trim();
                String port = etMessageServerPort.getText().toString().trim();
                if (!RegexUtils.checkIpAddress(ip)) {
                    // 不是正确的IP地址
                    showToast("Please enter the correct IP address!");
                    return;
                }
                if (Integer.valueOf(port) < 0 || Integer.valueOf(port) > 65535) {
                    showToast("Please enter the correct port!");
                    return;
                }
                modifyCompany();
                break;
            default:
                break;
        }
    };

    /**
     * 更新单位信息
     */
    private void modifyCompany() {
        String ip = etMessageServerIP.getText().toString().trim();
        String port = etMessageServerPort.getText().toString().trim();
        SPHelper.save("MessageServerIP", ip);
        SPHelper.save("MessageServerPort", port);
        setResult(Activity.RESULT_OK);
        ActivityController.finishActivity(this);
    }
}
