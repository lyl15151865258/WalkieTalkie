package jp.co.shiratsuki.walkietalkie.activity.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.activity.BaseActivity;
import jp.co.shiratsuki.walkietalkie.activity.MainActivity;
import jp.co.shiratsuki.walkietalkie.adapter.ChooseLanguageAdapter;
import jp.co.shiratsuki.walkietalkie.bean.Language;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;
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

public class LanguageActivity extends BaseActivity {

    private List<Language> languageList;
    private ChooseLanguageAdapter chooseLanguageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_language);
        MyToolbar toolbar = findViewById(R.id.myToolbar);
        toolbar.initToolBar(this, toolbar, getString(R.string.ChooseLanguage), R.drawable.back_white, onClickListener);
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
            for (int i = 0; i < chooseLanguageAdapter.getItemCount(); i++) {
                ((ImageView) recyclerViewLanguage.getChildAt(i).findViewById(R.id.iv_select)).setImageResource(R.drawable.checkbox_choose_language_normal);
            }
            ((ImageView) view.findViewById(R.id.iv_select)).setImageResource(R.drawable.checkbox_choose_language_selected);

            SPHelper.save(getString(R.string.language), languageList.get(position).getLanguageCode());

            changeAppLanguage();

            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.M) {
                //Android6.0以上直接调用recreate()方法刷新页面
                recreate();
            } else {
                //Android6.0及以下调用recreate()方法刷新页面会闪屏，直接重新打开MainActivity
                goBack();
            }
        });
    }

    private View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.iv_left:
                goBack();
                break;
            default:
                break;
        }
    };

    private void goBack() {
        //让之前打开的所有界面全部彻底关闭
        ActivityController.finishOtherActivity(this);
        //回到应用的首页
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        ActivityController.finishActivity(this);
    }

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
