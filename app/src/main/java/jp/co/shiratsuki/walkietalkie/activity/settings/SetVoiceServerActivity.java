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
import jp.co.shiratsuki.walkietalkie.activity.base.SwipeBackActivity;
import jp.co.shiratsuki.walkietalkie.constant.NetWork;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;
import jp.co.shiratsuki.walkietalkie.utils.RegexUtils;
import jp.co.shiratsuki.walkietalkie.utils.ViewUtils;
import jp.co.shiratsuki.walkietalkie.widget.MyToolbar;

/**
 * 设置语音服务器信息
 * Created at 2019/1/15 5:05
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class SetVoiceServerActivity extends SwipeBackActivity {

    private Context mContext;
    private EditText etVoiceServerIP, etVoiceServerPort, etVoiceRoomId;
    private Button btnModify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_voice_server);
        mContext = this;
        MyToolbar toolbar = findViewById(R.id.myToolbar);
        toolbar.initToolBar(this, toolbar, getString(R.string.VoiceServerSetting), R.drawable.back_white, onClickListener);
        btnModify = findViewById(R.id.btn_modify);
        btnModify.setOnClickListener(onClickListener);
        etVoiceServerIP = findViewById(R.id.etVoiceServerIP);
        etVoiceServerPort = findViewById(R.id.etVoiceServerPort);
        etVoiceRoomId = findViewById(R.id.etVoiceRoomId);
        etVoiceServerIP.addTextChangedListener(textWatcher);
        etVoiceServerPort.addTextChangedListener(textWatcher);
        etVoiceRoomId.addTextChangedListener(textWatcher);
        etVoiceServerIP.setText(SPHelper.getString("VoiceServerIP", NetWork.WEBRTC_SERVER_IP));
        etVoiceServerPort.setText(SPHelper.getString("VoiceServerPort", NetWork.WEBRTC_SERVER_PORT));
        etVoiceRoomId.setText(SPHelper.getString("VoiceRoomId", NetWork.WEBRTC_SERVER_ROOM));
        ViewUtils.setCharSequence(etVoiceServerIP);
        ViewUtils.setCharSequence(etVoiceServerPort);
        ViewUtils.setCharSequence(etVoiceRoomId);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (TextUtils.isEmpty(etVoiceServerPort.getText().toString()) || TextUtils.isEmpty(etVoiceRoomId.getText().toString()) || TextUtils.isEmpty(etVoiceServerIP.getText().toString())) {
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
                String ip = etVoiceServerIP.getText().toString().trim();
                String port = etVoiceServerPort.getText().toString().trim();
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
        String ip = etVoiceServerIP.getText().toString().trim();
        String port = etVoiceServerPort.getText().toString().trim();
        String room = etVoiceRoomId.getText().toString().trim();
        SPHelper.save("VoiceServerIP", ip);
        SPHelper.save("VoiceServerPort", port);
        SPHelper.save("VoiceRoomId", room);
        setResult(Activity.RESULT_OK);
        ActivityController.finishActivity(this);
    }
}
