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
import jp.co.shiratsuki.walkietalkie.activity.appmain.HtmlActivity;
import jp.co.shiratsuki.walkietalkie.activity.base.BaseActivity;
import jp.co.shiratsuki.walkietalkie.bean.LoginRegisterResult;
import jp.co.shiratsuki.walkietalkie.constant.Constants;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.network.ExceptionHandle;
import jp.co.shiratsuki.walkietalkie.network.NetClient;
import jp.co.shiratsuki.walkietalkie.network.NetworkSubscriber;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.utils.NetworkUtil;
import jp.co.shiratsuki.walkietalkie.utils.RegexUtils;
import jp.co.shiratsuki.walkietalkie.utils.StatusBarUtil;
import jp.co.shiratsuki.walkietalkie.utils.ViewUtils;
import jp.co.shiratsuki.walkietalkie.widget.SmoothCheckBox;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        mContext = this;

        String userName = SPHelper.getString("userName", Constants.EMPTY);
        String passWord = SPHelper.getString("passWord", Constants.EMPTY);

        ivUserIcon = findViewById(R.id.iv_userIcon);
        etPhoneNumberLogin = findViewById(R.id.et_phoneNumber_login);
        etPassWordLogin = findViewById(R.id.et_passWord_login);
        etPhoneNumberLogin.setText(userName);
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
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTranslucentForImageView(this, findViewById(R.id.myToolbar));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //展示头像
        showUserIcon();
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
                break;case R.id.tv_showLogin:
                //切换到登录页面
                rlLogin.setVisibility(View.VISIBLE);
                llRegister.setVisibility(View.GONE);
                break;
            case R.id.tv_registration_protocol:
                //打开用户注册协议
                Intent intent = new Intent(LoginRegisterActivity.this, HtmlActivity.class);
                String url = "file:////android_asset/html/RegisterProtocols/index.html";
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
                    showToast("请输入手机号");
                    return;
                }
                if (!RegexUtils.checkMobile(userNameRegister)) {
                    showToast("请输入正确的手机号");
                    return;
                }
                if (TextUtils.isEmpty(passWordRegister)) {
                    showToast("请输入密码");
                    return;
                }
                int passwordLength = 6;
                if (passWordRegister.length() < passwordLength) {
                    showToast("密码长度小于6位");
                    return;
                }
                if (TextUtils.isEmpty(confirm)) {
                    showToast("请再次输入密码");
                    return;
                }
                if (passWordRegister.equals(confirm)) {
                    //校验手机验证码
                    register();
                } else {
                    showToast("两次输入的密码不一致");
                }
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
        String userName = etPhoneNumberLogin.getText().toString().trim();
        String passWord = etPassWordLogin.getText().toString().trim();
        if (TextUtils.isEmpty(userName)) {
            showToast("请输入用户名");
            return;
        }
        if (TextUtils.isEmpty(passWord)) {
            showToast("请输入密码");
            return;
        }
        Map<String, String> params = new HashMap<>(4);
        params.put("loginName", userName);
        params.put("password", passWord);
        Observable<LoginRegisterResult> clientUserObservable = NetClient.getInstances(NetClient.BASE_URL_PROJECT).getNjMeterApi().login(params);
        clientUserObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new NetworkSubscriber<LoginRegisterResult>(mContext, getClass().getSimpleName()) {

            @Override
            public void onStart() {
                super.onStart();
                //接下来可以检查网络连接等操作
                if (!NetworkUtil.isNetworkAvailable(mContext)) {
                    showToast("当前网络不可用，请检查网络");
                    if (!isUnsubscribed()) {
                        unsubscribe();
                    }
                } else {
                    showLoadingDialog(mContext, "登陆中", true);
                }
            }

            @Override
            public void onError(ExceptionHandle.ResponseThrowable responseThrowable) {
                cancelDialog();
                showToast("" + responseThrowable.message);
            }

            @Override
            public void onNext(LoginRegisterResult loginRegisterResult) {
                cancelDialog();
                try {
                    String mark = loginRegisterResult.getResult();
                    String message = loginRegisterResult.getMessage();
                    switch (mark) {
                        case Constants.SUCCESS:
                            LogUtils.d("登陆成功");
                            //保存用户名密码
                            String phoneNumber = etPhoneNumberLogin.getText().toString();
                            String passWord = etPassWordLogin.getText().toString();
                            SPHelper.save("userName_main", phoneNumber);
                            SPHelper.save("passWord_main", passWord);
                            //更新用户配置信息
//                            mySharedPreferencesUtils.updateUserConfiguration(loginRegisterResult.getAccount().getDetails());

                            ActivityController.finishActivity(LoginRegisterActivity.this);
                            break;
                        case Constants.FAIL:
                            showToast("登陆失败，" + message);
                            break;
                        default:
                            showToast("登陆失败");
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
        String userName = etPhoneNumberRegister.getText().toString().trim();
        String passWord = etPassWordRegister.getText().toString().trim();
        if (TextUtils.isEmpty(userName)) {
            showToast("请输入用户名");
            return;
        }
        if (TextUtils.isEmpty(passWord)) {
            showToast("请输入密码");
            return;
        }
        Map<String, String> params = new HashMap<>(2);
        params.put("loginName", userName);
        params.put("password", passWord);
        Observable<LoginRegisterResult> clientUserCall = NetClient.getInstances(NetClient.BASE_URL_PROJECT).getNjMeterApi().register(params);
        clientUserCall.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new NetworkSubscriber<LoginRegisterResult>(mContext, getClass().getSimpleName()) {

            @Override
            public void onStart() {
                super.onStart();
                //接下来可以检查网络连接等操作
                if (!NetworkUtil.isNetworkAvailable(mContext)) {
                    showToast("当前网络不可用，请检查网络");
                    if (!isUnsubscribed()) {
                        unsubscribe();
                    }
                } else {
                    showLoadingDialog(mContext, "注册中", true);
                }
            }

            @Override
            public void onError(ExceptionHandle.ResponseThrowable responseThrowable) {
                cancelDialog();
                showToast("" + responseThrowable.message);
            }

            @Override
            public void onNext(LoginRegisterResult loginRegisterResult) {
                cancelDialog();
                if (loginRegisterResult == null) {
                    showToast("注册失败，返回值异常");
                } else {
                    String mark = loginRegisterResult.getResult();
                    String message = loginRegisterResult.getMessage();
                    switch (mark) {
                        case Constants.SUCCESS:
                            //保存用户名密码
                            String phoneNumber = etPhoneNumberRegister.getText().toString();
                            String passWord = etPassWordRegister.getText().toString();
                            SPHelper.save("userName_main", phoneNumber);
                            SPHelper.save("passWord_main", passWord);
                            showToast("注册成功");
                            //切换到登录页面
                            rlLogin.setVisibility(View.VISIBLE);
                            llRegister.setVisibility(View.GONE);
                            etPhoneNumberLogin.setText(phoneNumber);
                            etPassWordLogin.setText(passWord);
                            openActivity(ChooseHeadPortraitActivity.class);
                            break;
                        case Constants.FAIL:
                            showToast("注册失败，" + message);
                            break;
                        default:
                            showToast("注册失败");
                            break;
                    }
                }
            }
        });
    }

}
