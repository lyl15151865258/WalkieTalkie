package jp.co.shiratsuki.walkietalkie.widget;

import android.content.Context;
import android.support.annotation.ArrayRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import jp.co.shiratsuki.walkietalkie.interfaces.LanguageChangableView;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.utils.StringUtil;

/**
 * 自动跑马灯TextView
 * Created at 2018/11/28 13:56
 *
 * @author LiYuliang
 * @version 1.0
 */

public class MarqueeTextView extends AppCompatTextView implements LanguageChangableView {

    private String TAG = "MarqueeTextView";
    private int textId;//文字id
    private int hintId;//hint的id
    private int arrResId, arrResIndex;

    public MarqueeTextView(Context context) {
        super(context);
        init(context, null);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    public boolean isFocused() {
        return true;
    }


    /**
     * 初始化获取xml的资源id
     *
     * @param context
     * @param attributeSet
     */
    private void init(Context context, AttributeSet attributeSet) {
        if (attributeSet != null) {
            String textValue = attributeSet.getAttributeValue(ANDROIDXML, "text");


            LogUtils.d(TAG, (textValue == null) + ",");

            if (!(textValue == null || textValue.length() < 2)) {
                //如果是 android:text="@string/testText"
                //textValue会是 @0x7f080000,去掉@号就是资源id
                textId = StringUtil.string2int(textValue.substring(1, textValue.length()));
            }

            String hintValue = attributeSet.getAttributeValue(ANDROIDXML, "hint");
            if (!(hintValue == null || hintValue.length() < 2)) {
                hintId = StringUtil.string2int(hintValue.substring(1, hintValue.length()));
            }
        } else {
            LogUtils.d(TAG, "attributeSet==null");
        }
    }

    @Override
    public void setTextById(@StringRes int strId) {
        this.textId = strId;
        setText(strId);
    }

    @Override
    public void setTextWithString(String text) {
        this.textId = 0;
        setText(text);
    }

    @Override
    public void setTextByArrayAndIndex(@ArrayRes int arrId, @StringRes int arrIndex) {
        arrResId = arrId;
        arrResIndex = arrIndex;
        String[] strs = getContext().getResources().getStringArray(arrId);
        setText(strs[arrIndex]);
    }

    @Override
    public void reLoadLanguage() {

        LogUtils.d(TAG, "textId = " + textId + ",hintId = " + hintId);
        try {
            if (textId > 0) {
                LogUtils.d(TAG, "走了MarqueeTextView的reLoadLanguage.setText(textId);方法");
                setText(textId);
            } else if (arrResId > 0) {
                LogUtils.d(TAG, "走了MarqueeTextView的reLoadLanguage.setText(strs[arrResIndex]);方法");
                String[] strs = getContext().getResources().getStringArray(arrResId);
                setText(strs[arrResIndex]);
            }

            if (hintId > 0) {
                LogUtils.d(TAG, "走了MarqueeTextView的reLoadLanguage.setHint(hintId);方法");
                setHint(hintId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
