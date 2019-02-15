package jp.co.shiratsuki.walkietalkie.activity.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.activity.base.SwipeBackActivity;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;
import jp.co.shiratsuki.walkietalkie.utils.ViewUtils;
import jp.co.shiratsuki.walkietalkie.widget.MyToolbar;

/**
 * 设置个人信息页面
 * Created at 2019/1/15 4:37
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class SetPersonalInfoActivity extends SwipeBackActivity {

    private Context mContext;
    private EditText etName, etCompanyName, etDepartment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_company);
        mContext = this;
        MyToolbar toolbar = findViewById(R.id.myToolbar);
        toolbar.initToolBar(this, toolbar, getString(R.string.PersonalInformation), R.drawable.back_white, onClickListener);
        findViewById(R.id.btn_modify).setOnClickListener(onClickListener);
        etName = findViewById(R.id.etName);
        etCompanyName = findViewById(R.id.etCompanyName);
        etDepartment = findViewById(R.id.etDepartment);
        etName.setText(SPHelper.getString("UserName", ""));
        etCompanyName.setText(SPHelper.getString("Company", ""));
        etDepartment.setText(SPHelper.getString("Department", ""));
        ViewUtils.setCharSequence(etCompanyName);
        ViewUtils.setCharSequence(etDepartment);
        ViewUtils.setCharSequence(etName);
    }

    private View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.iv_left:
                ActivityController.finishActivity(this);
                break;
            case R.id.btn_modify:
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
        String name = etName.getText().toString().trim();
        String companyName = etCompanyName.getText().toString().trim();
        String userPosition = etDepartment.getText().toString().trim();
        SPHelper.save("UserName", name);
        SPHelper.save("Company", companyName);
        SPHelper.save("Department", userPosition);
        ActivityController.finishActivity(this);
    }
}
