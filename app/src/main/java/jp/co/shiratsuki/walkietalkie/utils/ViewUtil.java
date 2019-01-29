package jp.co.shiratsuki.walkietalkie.utils;

import android.view.View;
import android.view.ViewGroup;

/**
 * 更新文本控件的方法
 * Created at 2019/1/28 19:33
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class ViewUtil {
    public static void updateViewLanguage(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            int count = vg.getChildCount();
            for (int i = 0; i < count; i++) {
                updateViewLanguage(vg.getChildAt(i));
            }
        } else if (view instanceof LanguageChangableView) {
            LanguageChangableView tv = (LanguageChangableView) view;
            tv.reLoadLanguage();
        }
    }
}
