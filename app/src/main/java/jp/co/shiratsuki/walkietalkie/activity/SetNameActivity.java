package jp.co.shiratsuki.walkietalkie.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;
import jp.co.shiratsuki.walkietalkie.widget.MyToolbar;

/**
 * 姓名设置页面
 * Created at 2018-12-14 22:50
 *
 * @author LiYuliang
 * @version 1.0
 */

public class SetNameActivity extends BaseActivity {

    private EditText etNickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_name);
        MyToolbar toolbar = findViewById(R.id.myToolbar);
        toolbar.initToolBar(this, toolbar, getString(R.string.ModifyName), R.drawable.back_white, onClickListener);
        Button btnModify = findViewById(R.id.btn_modify);
        btnModify.setOnClickListener(onClickListener);
        etNickName = findViewById(R.id.et_nickName);
        etNickName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(charSequence.toString())) {
                    btnModify.setEnabled(false);
                } else {
                    btnModify.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etNickName.setText(SPHelper.getString("UserName", "Not Defined"));
    }

    private View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.iv_left:
                ActivityController.finishActivity(this);
                break;
            case R.id.btn_modify:
                SPHelper.save("UserName", etNickName.getText().toString());
                Intent intent = new Intent();
                intent.setAction("CHANGE_NAME");
                sendBroadcast(intent);
                ActivityController.finishActivity(this);
                break;
            default:
                break;
        }
    };

}
