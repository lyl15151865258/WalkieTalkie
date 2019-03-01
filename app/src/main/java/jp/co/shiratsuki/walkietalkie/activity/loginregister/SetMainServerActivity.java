package jp.co.shiratsuki.walkietalkie.activity.loginregister;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.activity.base.SwipeBackActivity;
import jp.co.shiratsuki.walkietalkie.constant.NetWork;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.utils.RegexUtils;
import jp.co.shiratsuki.walkietalkie.utils.ViewUtils;
import jp.co.shiratsuki.walkietalkie.widget.MyToolbar;

/**
 * 设置登录服务器
 * Created at 2019/3/1 20:00
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class SetMainServerActivity extends SwipeBackActivity {

    private EditText etPrimaryServerIP, etPrimaryServerPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_primary_server);
        MyToolbar toolbar = findViewById(R.id.myToolbar);
        toolbar.initToolBar(this, toolbar, R.string.MainServerSettings, R.drawable.back_white, onClickListener);
        findViewById(R.id.btn_modify).setOnClickListener(onClickListener);
        etPrimaryServerIP = findViewById(R.id.etPrimaryServerIP);
        etPrimaryServerPort = findViewById(R.id.etPrimaryServerPort);

        String ip = SPHelper.getString("PrimaryServerIp", "");
        String port = SPHelper.getString("PrimaryServerPort", "");

        if (TextUtils.isEmpty(ip)) {
            etPrimaryServerIP.setText(NetWork.SERVER_HOST_MAIN);
        } else {
            etPrimaryServerIP.setText(ip);
        }
        if (TextUtils.isEmpty(port)) {
            etPrimaryServerPort.setText(NetWork.SERVER_PORT_MAIN);
        } else {
            etPrimaryServerPort.setText(port);
        }

        ViewUtils.setCharSequence(etPrimaryServerIP);
        ViewUtils.setCharSequence(etPrimaryServerPort);
    }

    private View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.iv_left:
                ActivityController.finishActivity(this);
                break;
            case R.id.btn_modify:
                // 检查IP和端口格式是否正确
                String ip = etPrimaryServerIP.getText().toString().trim();
                String port = etPrimaryServerPort.getText().toString().trim();
                if (!TextUtils.isEmpty(ip)) {
                    if (!RegexUtils.checkIpAddress(ip)) {
                        // 不是正确的IP地址
                        showToast(R.string.IPError);
                        return;
                    } else {
                        SPHelper.save("PrimaryServerIp", ip);
                    }
                } else {
                    SPHelper.remove("PrimaryServerIp");
                }

                if (!TextUtils.isEmpty(port)) {
                    if (Integer.valueOf(port) < 0 || Integer.valueOf(port) > 65535) {
                        showToast(R.string.PortError);
                        return;
                    } else {
                        SPHelper.save("PrimaryServerPort", port);
                    }
                } else {
                    SPHelper.remove("PrimaryServerPort");
                }
                ActivityController.finishActivity(this);
                break;
            default:
                break;
        }
    };

}
