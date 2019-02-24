package jp.co.shiratsuki.walkietalkie.activity.settings;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.activity.base.SwipeBackActivity;
import jp.co.shiratsuki.walkietalkie.bean.User;
import jp.co.shiratsuki.walkietalkie.bean.UserOperateResult;
import jp.co.shiratsuki.walkietalkie.constant.Constants;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.network.ExceptionHandle;
import jp.co.shiratsuki.walkietalkie.network.NetClient;
import jp.co.shiratsuki.walkietalkie.network.NetworkSubscriber;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;
import jp.co.shiratsuki.walkietalkie.utils.GsonUtils;
import jp.co.shiratsuki.walkietalkie.utils.NetworkUtil;
import jp.co.shiratsuki.walkietalkie.utils.RegexUtils;
import jp.co.shiratsuki.walkietalkie.utils.ViewUtils;
import jp.co.shiratsuki.walkietalkie.widget.MyToolbar;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
    private User user;

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
        user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        etVoiceServerIP.setText(user.getVoice_ip());
        etVoiceServerPort.setText(user.getVoice_port());
        etVoiceRoomId.setText(user.getRoom_id());
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
                    showToast(R.string.IPError);
                    return;
                }
                if (Integer.valueOf(port) < 0 || Integer.valueOf(port) > 65535) {
                    showToast(R.string.PortError);
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

        Map<String, Object> params = new HashMap<>(4);
        params.put("userId", user.getUser_id());
        params.put("serverIP", ip);
        params.put("serverPort", port);
        params.put("roomId", room);
        Observable<UserOperateResult> clientUserObservable = NetClient.getInstances(NetClient.BASE_URL_PROJECT).getNjMeterApi().updateVoiceServer(params);
        clientUserObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new NetworkSubscriber<UserOperateResult>(mContext, getClass().getSimpleName()) {

            @Override
            public void onStart() {
                super.onStart();
                //接下来可以检查网络连接等操作
                if (!NetworkUtil.isNetworkAvailable(mContext)) {
                    showToast(getString(R.string.NetworkUnavailable));
                    if (!isUnsubscribed()) {
                        unsubscribe();
                    }
                } else {
                    showLoadingDialog(mContext, getString(R.string.updating), true);
                }
            }

            @Override
            public void onError(ExceptionHandle.ResponseThrowable responseThrowable) {
                cancelDialog();
                showToast("" + responseThrowable.message);
            }

            @Override
            public void onNext(UserOperateResult userOperateResult) {
                cancelDialog();
                try {
                    String mark = userOperateResult.getResult();
                    String message = userOperateResult.getMessage();
                    switch (mark) {
                        case Constants.SUCCESS:
                            showToast(R.string.UpdateSuccess);
                            SPHelper.save("User", GsonUtils.convertJSON(userOperateResult.getUser()));
                            ActivityController.finishActivity(SetVoiceServerActivity.this);
                            break;
                        case Constants.FAIL:
                            showToast(getString(R.string.UpdateFailed) + " " + message);
                            break;
                        default:
                            showToast(R.string.UpdateFailed);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
