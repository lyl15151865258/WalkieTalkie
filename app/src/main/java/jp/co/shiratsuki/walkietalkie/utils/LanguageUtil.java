package jp.co.shiratsuki.walkietalkie.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;

import java.util.Locale;

/**
 * 软件语言设置工具
 * Created at 2018/11/28 13:51
 *
 * @author LiYuliang
 * @version 1.0
 */

public class LanguageUtil {

    public static String getLanguageLocal(Context context) {
        return SPHelper.getString(context.getString(R.string.language), "");
    }

    public static Context attachBaseContext(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, getLanguageLocal(context));
        } else {
            return context;
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Resources resources = context.getResources();
        Locale locale = new Locale(getLanguageLocal(context));

        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLocales(new LocaleList(locale));
        return context.createConfigurationContext(configuration);
    }
}
