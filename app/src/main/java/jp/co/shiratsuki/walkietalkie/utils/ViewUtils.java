package jp.co.shiratsuki.walkietalkie.utils;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 控件辅助工具
 * Created by LiYuliang on 2017/10/20.
 *
 * @author LiYuliang
 * @version 2018/03/15
 */

public class ViewUtils {

    /**
     * 将EditText光标置于末尾
     *
     * @param editText EditText控件
     */
    public static void setCharSequence(EditText editText) {
        CharSequence charSequence = editText.getText();
        editText.setSelection(charSequence.length());
    }

    /**
     * 改变日期（加减一天或一个月），修改日期显示并刷新数据
     *
     * @param mode  模式（年、月、日）
     * @param value 负数向前/正数向后
     */
    public static void changeDate(Context context, TextView tvDate, int mode, int value) {
        String date = tvDate.getText().toString().substring(0, 10);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(format.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        switch (mode) {
            case 0:
                //当前时间加上value年，负数为向前value年
                calendar.add(Calendar.YEAR, value);
                break;
            case 1:
                //当前时间加上value月，负数为向前value月
                calendar.add(Calendar.MONTH, value);
                break;
            case 2:
                //当前时间加上value星期，负数为向前value星期
                calendar.add(Calendar.WEEK_OF_YEAR, value);
                break;
            case 3:
                //当前时间加上value天，负数为向前value天
                calendar.add(Calendar.DAY_OF_YEAR, value);
                break;
            default:
                break;
        }
        calendar.getTime();
        //如果获得的时间大于当前时间则保持日期不变（主要针对向后的日期）
        Date currentDate = new Date();
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(currentDate);
        //利用compareTo比较两者，大于返回1，小于返回-1，等于返回0
        if (calendar.compareTo(calendar1) != 1) {
            String newDate = DateFormat.format("yyyy-MM-dd", calendar).toString();
            tvDate.setText(newDate);
        }
    }

    /**
     * 刷新文本框(文字行数多余文本框高度时自动滚动，始终保证最后一行显示)
     *
     * @param msg      新增的信息
     * @param textView TextView控件
     */
    public static void refreshSocketText(String msg, TextView textView) {
        textView.append(msg);
        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > textView.getHeight()) {
            textView.scrollTo(0, offset - textView.getHeight());
        }
    }

    /**
     * 获取红色文字的方法
     *
     * @param string 完整的字符串
     * @param change 需要变色的文字
     * @return 变色后的文字
     */
    public static SpannableStringBuilder getRedText(String string, String change) {
        int fstart = string.indexOf(change);
        int fend = fstart + change.length();
        SpannableStringBuilder style = new SpannableStringBuilder(string);
        style.setSpan(new ForegroundColorSpan(Color.RED), fstart, fend, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return style;
    }

    /**
     * 获取绿色文字的方法
     *
     * @param string 完整的字符串
     * @param change 需要变色的文字
     * @return 变色后的文字
     */
    public static SpannableStringBuilder getGreenText(String string, String change) {
        int fstart = string.indexOf(change);
        int fend = fstart + change.length();
        SpannableStringBuilder style = new SpannableStringBuilder(string);
        style.setSpan(new ForegroundColorSpan(Color.parseColor("#00A000")), fstart, fend, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return style;
    }
}
