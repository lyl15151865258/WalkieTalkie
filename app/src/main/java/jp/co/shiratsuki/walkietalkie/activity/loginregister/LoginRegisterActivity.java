package jp.co.shiratsuki.walkietalkie.activity.loginregister;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.HashMap;
import java.util.Map;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.activity.MainActivity;
import jp.co.shiratsuki.walkietalkie.activity.appmain.HtmlActivity;
import jp.co.shiratsuki.walkietalkie.activity.base.BaseActivity;
import jp.co.shiratsuki.walkietalkie.activity.settings.SetPersonalInfoActivity;
import jp.co.shiratsuki.walkietalkie.bean.LoginResult;
import jp.co.shiratsuki.walkietalkie.bean.UserOperateResult;
import jp.co.shiratsuki.walkietalkie.constant.ApkInfo;
import jp.co.shiratsuki.walkietalkie.constant.Constants;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.network.ExceptionHandle;
import jp.co.shiratsuki.walkietalkie.network.NetClient;
import jp.co.shiratsuki.walkietalkie.network.NetworkSubscriber;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;
import jp.co.shiratsuki.walkietalkie.utils.GsonUtils;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.utils.NetworkUtil;
import jp.co.shiratsuki.walkietalkie.utils.PermissionUtil;
import jp.co.shiratsuki.walkietalkie.utils.StatusBarUtil;
import jp.co.shiratsuki.walkietalkie.utils.ViewUtils;
import jp.co.shiratsuki.walkietalkie.widget.SmoothCheckBox;
import jp.co.shiratsuki.walkietalkie.widget.dialog.CommonWarningDialog;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 登录注册页面
 * Created at 2019/2/15 13:08
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class LoginRegisterActivity extends BaseActivity {

    private Context mContext;
    private EditText etPhoneNumberLogin, etPassWordLogin, etPhoneNumberRegister, etPassWordRegister, etConfirmRegister;
    private Button btnRegister;
    private RelativeLayout rlLogin;
    private LinearLayout llRegister;
    private ImageView ivUserIcon;

    private CommonWarningDialog commonWarningDialog;

    private boolean isAutoLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        mContext = this;

        String userId = SPHelper.getString("userId", Constants.EMPTY);
        String passWord = SPHelper.getString("passWord", Constants.EMPTY);

        ivUserIcon = findViewById(R.id.iv_userIcon);
        etPhoneNumberLogin = findViewById(R.id.et_phoneNumber_login);
        etPassWordLogin = findViewById(R.id.et_passWord_login);
        etPhoneNumberLogin.setText(userId);
        etPassWordLogin.setText(passWord);
        ViewUtils.setCharSequence(etPhoneNumberLogin);
        ViewUtils.setCharSequence(etPassWordLogin);
        etPassWordLogin.setOnEditorActionListener(onEditorActionListener);
        findViewById(R.id.tv_showRegister).setOnClickListener(onClickListener);
        findViewById(R.id.tv_forgetPassword_login).setOnClickListener(onClickListener);
        findViewById(R.id.btn_Login).setOnClickListener(onClickListener);
        etPhoneNumberRegister = findViewById(R.id.et_phoneNumber_register);
        etPassWordRegister = findViewById(R.id.et_passWord_register);
        etConfirmRegister = findViewById(R.id.et_confirm_register);
        btnRegister = findViewById(R.id.btn_register);
        rlLogin = findViewById(R.id.rl_login);
        llRegister = findViewById(R.id.ll_register);
        btnRegister.setOnClickListener(onClickListener);
        TextView tvLogin = findViewById(R.id.tv_showLogin);
        TextView tvForgetPassword = findViewById(R.id.tv_forgetPassword_register);
        tvLogin.setOnClickListener(onClickListener);
        tvForgetPassword.setOnClickListener(onClickListener);
        SmoothCheckBox checkboxAgree = findViewById(R.id.checkbox_agree);
        checkboxAgree.setOnCheckedChangeListener((buttonView, isChecked) -> btnRegister.setEnabled(isChecked));
        TextView tvRegistrationProtocol = findViewById(R.id.tv_registration_protocol);
        setTextStyle(tvRegistrationProtocol);
        tvRegistrationProtocol.setOnClickListener(onClickListener);
        tvRegistrationProtocol.setOnClickListener(onClickListener);
        findViewById(R.id.ivSetServer).setOnClickListener(onClickListener);

        String action = getIntent().getAction();
        if (action != null) {
            isAutoLogin = false;
            switch (action) {
                case "RemoteLogin":
                    LogUtils.d(TAG, "Action：" + action + "，您的账号在其他设备登录");
                    showRemoteLoginDialog();
                    break;
                case "SwitchAccount":
                    LogUtils.d(TAG, "Action：" + action + "，这是切换账号");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTranslucentForImageView(this, findViewById(R.id.myToolbar));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 展示头像
        showUserIcon();
        //权限检查
        boolean permission = PermissionUtil.isNeedRequestPermission(this);
        // 如果是刚进入页面且不是从主页面切换账号打开的话，且账号密码不为空，权限都已经授予的话，执行自动登录
        if (!TextUtils.isEmpty(etPhoneNumberLogin.getText().toString().trim()) &&
                !TextUtils.isEmpty(etPassWordLogin.getText().toString().trim()) &&
                isAutoLogin && !permission) {
            login();
            isAutoLogin = false;
        }
    }

    private TextView.OnEditorActionListener onEditorActionListener = (textView, actionId, keyEvent) -> {
        boolean isGo = actionId == EditorInfo.IME_ACTION_GO || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
        if (isGo) {
            login();
        }
        return false;
    };

    private View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.iv_left:
                ActivityController.finishActivity(this);
                break;
            case R.id.tv_forgetPassword_login:
                //登录页面的忘记密码
            case R.id.tv_forgetPassword_register:
                //注册页面的忘记密码
//                openActivity(RetrievePasswordActivity.class);
                break;
            case R.id.btn_Login:
                login();
                break;
            case R.id.tv_showRegister:
                //切换到注册页面
                rlLogin.setVisibility(View.GONE);
                llRegister.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_showLogin:
                //切换到登录页面
                rlLogin.setVisibility(View.VISIBLE);
                llRegister.setVisibility(View.GONE);
                break;
            case R.id.tv_registration_protocol:
                //打开用户注册协议
                Intent intent = new Intent(LoginRegisterActivity.this, HtmlActivity.class);
                String url = NetClient.getBaseUrlProject() + "html/RegisterProtocols/RegisterProtocols.html";
                intent.putExtra("title", getString(R.string.RegistrationProtocol));
                intent.putExtra("URL", url);
                startActivity(intent);
                break;
            case R.id.btn_register:
                //点击注册按钮的逻辑
                String userNameRegister = etPhoneNumberRegister.getText().toString().trim();
                String passWordRegister = etPassWordRegister.getText().toString().trim();
                String confirm = etConfirmRegister.getText().toString().trim();
                if (TextUtils.isEmpty(userNameRegister)) {
                    showToast(R.string.EnterPhoneNumber);
                    return;
                }
                if (TextUtils.isEmpty(passWordRegister)) {
                    showToast(R.string.EnterPassword);
                    return;
                }
                int passwordLength = 6;
                if (passWordRegister.length() < passwordLength) {
                    showToast(R.string.LessThanSixDigits);
                    return;
                }
                if (TextUtils.isEmpty(confirm)) {
                    showToast(R.string.EnterPasswordAgain);
                    return;
                }
                if (passWordRegister.equals(confirm)) {
                    // 两次密码一致
                    register();
                } else {
                    showToast(R.string.InconsistentPassword);
                }
                break;
            case R.id.ivSetServer:
                // 设置登录服务器
                openActivity(SetMainServerActivity.class);
                break;
            default:
                break;
        }
    };

    /**
     * 加载头像
     */
    private void showUserIcon() {
        String photoPath = SPHelper.getString("userIconPath", Constants.EMPTY).replace("\\", "/");
        LogUtils.d(NetClient.TAG_POST, "图片路径：" + photoPath);
        RequestOptions options = new RequestOptions()
                .error(R.drawable.photo_user)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.photo_user)
                .dontAnimate();
        Glide.with(this).load(photoPath).apply(options).into(ivUserIcon);
    }

    /**
     * 设置字体格式
     *
     * @param textView 文本控件
     */
    private void setTextStyle(TextView textView) {
        String text = textView.getText().toString();
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        ForegroundColorSpan blueSpan = new ForegroundColorSpan(Color.BLUE);
        UnderlineSpan lineSpan = new UnderlineSpan();
        int start = text.indexOf(getString(R.string.RegistrationProtocol));
        int end = start + getString(R.string.RegistrationProtocol).length();
        //下划线
        builder.setSpan(lineSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //字体颜色
        builder.setSpan(blueSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(builder);
    }

    /**
     * 主账号登录
     */
    private void login() {
        LogUtils.d("login", "LoginRegisterActivity登陆方法");
        String userId = etPhoneNumberLogin.getText().toString().trim();
        String passWord = etPassWordLogin.getText().toString().trim();
        if (TextUtils.isEmpty(userId)) {
            showToast(getString(R.string.EnterPhoneNumber));
            return;
        }
        if (TextUtils.isEmpty(passWord)) {
            showToast(getString(R.string.EnterPassword));
            return;
        }
        Map<String, Object> params = new HashMap<>(4);
        params.put("userId", userId);
        params.put("password", passWord);
        params.put("apkTypeId", ApkInfo.APK_TYPE_ID_WALKIE_TALKIE);

        Observable<LoginResult> clientUserObservable = NetClient.getInstances(NetClient.getBaseUrlProject()).getNjMeterApi().login(params);
        clientUserObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new NetworkSubscriber<LoginResult>(mContext, getClass().getSimpleName()) {

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
                    showLoadingDialog(mContext, getString(R.string.LoggingIn), true);
                }
            }

            @Override
            public void onError(ExceptionHandle.ResponseThrowable responseThrowable) {
                cancelDialog();
                showToast(responseThrowable.message);
            }

            @Override
            public void onNext(LoginResult loginResult) {
                cancelDialog();
                try {
                    String mark = loginResult.getResult();
                    String message = loginResult.getMessage();
                    switch (mark) {
                        case Constants.SUCCESS:
                            //保存用户名密码
                            String phoneNumber = etPhoneNumberLogin.getText().toString();
                            String passWord = etPassWordLogin.getText().toString();
                            SPHelper.save("userId", phoneNumber);
                            SPHelper.save("passWord", passWord);
                            //保存最新的版本信息
                            SPHelper.save("version", GsonUtils.convertJSON(loginResult.getVersion()));
                            //更新用户配置信息
//                            mySharedPreferencesUtils.updateUserConfiguration(userOperateResult.getAccount().getDetails());

                            SPHelper.save("User", GsonUtils.convertJSON(loginResult.getUser()));
                            if (TextUtils.isEmpty(loginResult.getUser().getIcon_url())) {
                                openActivity(ChooseHeadPortraitActivity.class);
                            } else if (TextUtils.isEmpty(loginResult.getUser().getUser_name()) || TextUtils.isEmpty(loginResult.getUser().getDepartment_name())) {
                                openActivity(SetPersonalInfoActivity.class);
                            } else {
                                openActivity(MainActivity.class);
                            }
                            ActivityController.finishActivity(LoginRegisterActivity.this);
                            break;
                        case Constants.FAIL:
                            showToast(getString(R.string.LogInFailed) + message);
                            break;
                        default:
                            showToast(getString(R.string.LogInFailed));
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 主账号注册
     */
    private void register() {
        String userId = etPhoneNumberRegister.getText().toString().trim();
        String passWord = etPassWordRegister.getText().toString().trim();
        Map<String, Object> params = new HashMap<>(2);
        params.put("userId", userId);
        params.put("password", passWord);
        Observable<UserOperateResult> clientUserCall = NetClient.getInstances(NetClient.getBaseUrlProject()).getNjMeterApi().register(params);
        clientUserCall.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new NetworkSubscriber<UserOperateResult>(mContext, getClass().getSimpleName()) {

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
                    showLoadingDialog(mContext, getString(R.string.Registering), true);
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
                if (userOperateResult == null) {
                    showToast(R.string.RegisterFailed);
                } else {
                    String mark = userOperateResult.getResult();
                    String message = userOperateResult.getMessage();
                    switch (mark) {
                        case Constants.SUCCESS:
                            //保存用户名密码
                            String phoneNumber = etPhoneNumberRegister.getText().toString();
                            String passWord = etPassWordRegister.getText().toString();
                            SPHelper.save("userName_main", phoneNumber);
                            SPHelper.save("passWord_main", passWord);
                            showToast(R.string.RegisteredSuccess);
                            //切换到登录页面
                            rlLogin.setVisibility(View.VISIBLE);
                            llRegister.setVisibility(View.GONE);
                            etPhoneNumberLogin.setText(phoneNumber);
                            etPassWordLogin.setText(passWord);
//                            openActivity(ChooseHeadPortraitActivity.class);
                            break;
                        case Constants.FAIL:
                            showToast(R.string.RegisterFailed + " " + message);
                            break;
                        default:
                            showToast(R.string.RegisterFailed);
                            break;
                    }
                }
            }
        });
    }

    /**
     * 提示用户账号在其他设备登录
     */
    private void showRemoteLoginDialog() {
        if (commonWarningDialog == null) {
            commonWarningDialog = new CommonWarningDialog(mContext, getString(R.string.remote_login));
            commonWarningDialog.setButtonText(getString(R.string.Exit), getString(R.string.LoginAgain));
            commonWarningDialog.setCancelable(false);
            commonWarningDialog.setOnDialogClickListener(new CommonWarningDialog.OnDialogClickListener() {
                @Override
                public void onOKClick() {
                    // 重新登录
                    findViewById(R.id.btn_Login).performClick();
                }

                @Override
                public void onCancelClick() {
                    // 退出
                    ActivityController.finishActivity(LoginRegisterActivity.this);
                }
            });
        }
        if (!commonWarningDialog.isShowing()) {
            commonWarningDialog.show();
        }
    }

}
