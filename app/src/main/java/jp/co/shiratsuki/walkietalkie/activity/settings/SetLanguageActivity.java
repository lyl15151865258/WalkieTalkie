package jp.co.shiratsuki.walkietalkie.activity.settings;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.activity.BaseActivity;
import jp.co.shiratsuki.walkietalkie.adapter.ChooseLanguageAdapter;
import jp.co.shiratsuki.walkietalkie.bean.Language;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.utils.ViewUtil;
import jp.co.shiratsuki.walkietalkie.widget.MyToolbar;
import jp.co.shiratsuki.walkietalkie.widget.RecyclerViewDivider;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择语言页面
 * Created at 2018/11/28 13:37
 *
 * @author LiYuliang
 * @version 1.0
 */

public class SetLanguageActivity extends BaseActivity {

    private String TAG = "SetLanguageActivity";
    private List<Language> languageList;
    private ChooseLanguageAdapter chooseLanguageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_language);
        MyToolbar toolbar = findViewById(R.id.myToolbar);
        toolbar.initToolBar(this, toolbar, R.string.ChooseLanguage, R.drawable.back_white, onClickListener);
        RecyclerView recyclerViewLanguage = findViewById(R.id.recyclerView_language);
        languageList = new ArrayList<>();
        addLanguages();
        //垂直线性布局
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewLanguage.setLayoutManager(linearLayoutManager);
        recyclerViewLanguage.addItemDecoration(new RecyclerViewDivider(this, LinearLayoutManager.HORIZONTAL, 1, ContextCompat.getColor(this, R.color.gray_slight)));
        chooseLanguageAdapter = new ChooseLanguageAdapter(this, languageList);
        recyclerViewLanguage.setAdapter(chooseLanguageAdapter);
        chooseLanguageAdapter.setOnItemClickListener((view, position) -> {
            SPHelper.save(getString(R.string.language), languageList.get(position).getLanguageCode());
            changeAppLanguage();

            languageList.clear();
            addLanguages();
            chooseLanguageAdapter.notifyDataSetChanged();

            // 通过EventBus通知其他页面更改语言
            EventBus.getDefault().post("CHANGE_LANGUAGE");
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStringEvent(String msg) {
        LogUtils.d(TAG, "走了更新语言的方法");
        ViewUtil.updateViewLanguage(findViewById(android.R.id.content));
    }

    private View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.iv_left:
                ActivityController.finishActivity(this);
                break;
            default:
                break;
        }
    };

    /**
     * 添加语言种类
     */
    private void addLanguages() {
        Language general = new Language();
        general.setLanguageName(getString(R.string.lan_default));
        general.setLanguageCode("");
        languageList.add(general);

        Language chinese = new Language();
        chinese.setLanguageName(getString(R.string.lan_chinese));
        chinese.setLanguageCode("zh");
        languageList.add(chinese);

        Language english = new Language();
        english.setLanguageName(getString(R.string.lan_en));
        english.setLanguageCode("en");
        languageList.add(english);

        Language japanese = new Language();
        japanese.setLanguageName(getString(R.string.lan_ja));
        japanese.setLanguageCode("ja");
        languageList.add(japanese);
    }

}
