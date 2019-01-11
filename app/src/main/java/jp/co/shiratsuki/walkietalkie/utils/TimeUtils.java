package jp.co.shiratsuki.walkietalkie.utils;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import jp.co.shiratsuki.walkietalkie.R;

/**
 * 时间工具类
 * Created at 2018/11/28 13:54
 *
 * @author LiYuliang
 * @version 1.0
 */

public class TimeUtils {

    private static final long ONE_MINUTE_MILLIONS = 60 * 1000;
    private static final long ONE_HOUR_MILLIONS = 60 * ONE_MINUTE_MILLIONS;
    private static final long ONE_DAY_MILLIONS = 24 * ONE_HOUR_MILLIONS;

    /**
     * 判断用户的设备时区是否为东八区（中国） 2014年7月31日
     *
     * @return 判断是否是东八区
     */
    public static boolean isInEasternEightZones() {
        boolean defaultVaule = true;
        defaultVaule = TimeZone.getDefault() == TimeZone.getTimeZone("GMT+08");
        return defaultVaule;
    }

    /**
     * 根据不同时区，转换时间 2014年7月31日
     *
     * @param date    日期
     * @param oldZone 旧时区
     * @param newZone 新时区
     * @return 时间
     */
    public static Date transformTime(Date date, TimeZone oldZone, TimeZone newZone) {
        Date finalDate = null;
        if (date != null) {
            int timeOffset = oldZone.getOffset(date.getTime()) - newZone.getOffset(date.getTime());
            finalDate = new Date(date.getTime() - timeOffset);
        }
        return finalDate;
    }

    /**
     * 得到系统当前时间
     *
     * @return 当前时间（毫秒值）
     */
    public static String getCurrentTimeMillis() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * Unix时间戳转String型时间
     *
     * @param millSec 时间戳
     * @return String型时间
     */
    public static String unixTime2String(long millSec) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        Date date = new Date(millSec * 1000);
        return sdf.format(date);
    }

    /**
     * String型时间转Unix时间戳
     *
     * @param time String型时间
     * @return 时间戳
     */
    public static long stringToUnixTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        Date date = null;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) {
            return 0;
        } else {
            return date.getTime() / 1000;
        }
    }

    /**
     * Java时间戳转String型时间,不带秒
     *
     * @param millSec 时间戳
     * @return String型时间
     */
    public static String longTime2String(long millSec) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        Date date = new Date(millSec);
        return sdf.format(date);
    }

    /**
     * 得到系统当前年月日时分秒
     *
     * @return 当前时间（年月日时分秒）
     */
    public static String getCurrentDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    /**
     * 得到系统当前年月日时分秒
     *
     * @return 当前时间（年月日时分秒）
     */
    public static String getCurrentFormatDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    /**
     * 得到系统当前时分秒
     *
     * @return 当前时间（时分秒）
     */
    public static String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    /**
     * 得到系统当前时分秒结尾带空格
     *
     * @return 当前时间（时分秒）
     */
    public static String getCurrentTimeWithSpace() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss ", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    /**
     * 得到系统当前日期
     *
     * @return 当前时间（年月日）
     */
    public static String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    /**
     * 获取当前日期是星期几
     *
     * @return 返回当前日期是星期几
     */
    public static String getCurrentWeekOfDate() {
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        Date date = new Date(System.currentTimeMillis());
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weekDays[w];
    }

    /**
     * 获取某个日期是星期几
     *
     * @param date 日期
     * @return 返回该日期是星期几
     */
    public static String getWeekOfDate(Date date) {
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weekDays[w];
    }

    /**
     * 获取短时间格式
     *
     * @param dateStr "2016-01-06T09:37:04"
     * @return
     */
    public static String getShortTime(String dateStr) {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.CHINA);
        try {
            Date date = sdf.parse(dateStr);
            str = getShortTime(date.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return str;
    }

    /**
     * 获取短时间格式
     *
     * @return
     */
    public static String getShortTime(long millis) {
        Date date = new Date(millis);
        Date curDate = new Date();
        String str = "";
        long durTime = curDate.getTime() - date.getTime();
        int dayStatus = calculateDayStatus(date, new Date());
        if (durTime <= 10 * ONE_MINUTE_MILLIONS) {
            str = "刚刚";
        } else if (durTime < ONE_HOUR_MILLIONS) {
            str = durTime / ONE_MINUTE_MILLIONS + "分钟前";
        } else if (dayStatus == 0) {
            str = durTime / ONE_HOUR_MILLIONS + "小时前";
        } else if (dayStatus == -1) {
            str = "昨天" + DateFormat.format("HH:mm", date);
        } else if (isSameYear(date, curDate) && dayStatus < -1) {
            str = DateFormat.format("MM-dd", date).toString();
        } else {
            str = DateFormat.format("yyyy-MM", date).toString();
        }


//        if (durTime <= 10 * ONE_MINUTE_MILLIONS) {
//            str = "刚刚";
//        } else if (durTime < ONE_HOUR_MILLIONS) {
//            str = durTime / ONE_MINUTE_MILLIONS + "分钟前";
//        } else if (durTime < ONE_HOUR_MILLIONS * 24) {
//            str = durTime / ONE_HOUR_MILLIONS + "小时前";
//        } else {
//            Date date = new Date(millis);
//            str = DateFormat.format("MM-dd HH:mm", date) + "";
//        }
        return str;
    }

    /**
     * @param dateStr "2016-01-06T09:37:04"
     * @return
     */
    public static Date getDate(String dateStr) {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        try {
            Date date = sdf.parse(dateStr);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 年月日时分秒去除时分秒
     *
     * @param dateStr yyyy-MM-dd HH:mm:ss格式的日期
     * @return yyyy-MM-dd格式的日期
     */
    public static String getSimpleDate(String dateStr) {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        try {
            Date date = sdf.parse(dateStr);
            return DateFormat.format("yyyy-MM-dd", date).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 年月日时分秒去除时分秒（中国）
     *
     * @param dateStr yyyy-MM-dd HH:mm:ss格式的日期
     * @return yyyy-MM-dd格式的日期
     */
    public static String getChineseSimpleDate(String dateStr) {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        try {
            Date date = sdf.parse(dateStr);
            return DateFormat.format("yyyy年MM月dd日", date).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * @return
     */
    public static String toDateStr(Date date) {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        try {
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取时间倒计时
     *
     * @param dateStr "2016-01-06T09:37:04"
     * @return
     */
    public static String getTimeDown(String dateStr) {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.CHINA);
        try {
            Date date = sdf.parse(dateStr);
            Date curDate = new Date();

            long durTime = curDate.getTime() - date.getTime();
            int dayStatus = calculateDayStatus(date, curDate);

            if (durTime <= 10 * ONE_MINUTE_MILLIONS) {
                str = "刚刚";
            } else if (durTime < ONE_HOUR_MILLIONS) {
                str = durTime / ONE_MINUTE_MILLIONS + "分钟前";
            } else if (dayStatus == 0) {
                str = durTime / ONE_HOUR_MILLIONS + "小时前";
            } else if (dayStatus == -1) {
                str = "昨天" + DateFormat.format("HH:mm", date);
            } else if (isSameYear(date, curDate) && dayStatus < -1) {
                str = DateFormat.format("MM-dd", date).toString();
            } else {
                str = DateFormat.format("yyyy-MM", date).toString();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return str;
    }

    public static String getCurrentTimeDown(long currentTimeMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.CHINA);
        Date curDate = new Date(currentTimeMillis);
        String str = formatter.format(curDate);
        return getTimeDown(str);
    }

    /**
     * 返回 2016.1.1
     *
     * @return
     */
    public static String getTimePoint(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        SimpleDateFormat tosdf = new SimpleDateFormat("yyyy.MM.dd", Locale.CHINA);
        try {
            Date date = sdf.parse(dateStr);
            return tosdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 生成时间  时:分:秒
     *
     * @param position
     * @return
     */
    public static String generateTime(long position) {
        int totalSeconds = (int) (position / 1000);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
    }

    /**
     * 生成时间  时:分'秒''
     *
     * @param position
     * @return
     */
    public static String generateTimeFormatte(long position) {
        int totalSeconds = (int) (position / 1000);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format(Locale.US, "%02d’%02d’%02d”", hours, minutes, seconds);
        } else if (minutes > 0) {

            return String.format(Locale.US, "%02d’%02d”", minutes, seconds);
        } else {
            return String.format(Locale.US, "%02d”", seconds);
        }
    }

    /**
     * 生成时间  秒''
     *
     * @param position
     * @return
     */
    public static String generateSecFormatte(long position) {
        int totalSeconds = (int) (position / 1000);
        return String.format(Locale.US, "%02d”", totalSeconds);
    }

    /**
     * 计算某个时间距今多少天多少小时多少分多少秒
     *
     * @param str1 时间参数 1 格式：1990-01-01 12:00:00
     * @return String 返回值为：xx天xx小时xx分xx秒
     */
    public static String getDistanceTime(String str1) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.CHINA);
        Date one;
        String distanceTime = "-1";
        try {
            one = df.parse(str1);
            Date now = new Date();
            long time1 = one.getTime();
            long nowTime = now.getTime();
            long diff;
            if (time1 > nowTime) {
                diff = time1 - nowTime;
            } else {
                return "-1";
            }
            long day = 0;
            long hour = 0;
            long min = 0;
            long sec = 0;
            day = diff / (24 * 60 * 60 * 1000);
            hour = (diff / (60 * 60 * 1000) - day * 24);
            min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
            sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
            distanceTime = day + "天" + hour + "小时" + min + "分" + sec + "秒";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return distanceTime;

    }

    public static boolean isSameYear(Date targetTime, Date compareTime) {
        Calendar tarCalendar = Calendar.getInstance();
        tarCalendar.setTime(targetTime);
        int tarYear = tarCalendar.get(Calendar.YEAR);

        Calendar compareCalendar = Calendar.getInstance();
        compareCalendar.setTime(compareTime);
        int comYear = compareCalendar.get(Calendar.YEAR);

        return tarYear == comYear;
    }

    public static int calculateDayStatus(Date targetTime, Date compareTime) {
        Calendar tarCalendar = Calendar.getInstance();
        tarCalendar.setTime(targetTime);
        int tarDayOfYear = tarCalendar.get(Calendar.DAY_OF_YEAR);

        Calendar compareCalendar = Calendar.getInstance();
        compareCalendar.setTime(compareTime);
        int comDayOfYear = compareCalendar.get(Calendar.DAY_OF_YEAR);

        return tarDayOfYear - comDayOfYear;
    }

    /**
     * 年月日、时分秒格式的日期删除年月日格式
     *
     * @param time yyyy-MM-dd HH:mm:ss格式的时间
     * @return HH:mm:ss格式的时间
     */
    public static String dateTime2Time(String time) {
        String str = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        try {
            Date date = sdf.parse(time);
            str = new SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 返回 0小时0分
     *
     * @param timeMillis 毫秒
     * @return
     */
    public static String getTimeString(long timeMillis) {
        long minutes = timeMillis / ONE_MINUTE_MILLIONS;
        if (minutes < 60) {
            return minutes + "分钟";
        } else {
            long remainder = minutes % 60;
            long hour = minutes / 60;
            if (remainder == 0) {

                return hour + "小时";
            } else {
                return hour + "小时" + remainder + "分";
            }
        }
    }

    /**
     * 根据月日判断星座
     *
     * @param month
     * @param day
     * @return int
     */
    public static String getConstellation(int month, int day) {

        final String[] constellationArr = {"魔羯座", "水瓶座", "双鱼座", "牡羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "魔羯座"};

        final int[] constellationEdgeDay = {20, 18, 20, 20, 20, 21, 22, 22, 22, 22, 21, 21};
        if (day <= constellationEdgeDay[month - 1]) {
            month = month - 1;
        }
        if (month >= 0) {
            return constellationArr[month];
        }
        //default to return 魔羯
        return constellationArr[11];

    }

    /**
     * 根据出生日期获得年龄
     *
     * @param birthDay
     * @return
     */
    public static int getAge(Date birthDay) {
        Calendar cal = Calendar.getInstance();
        if (cal.before(birthDay)) {
            throw new IllegalArgumentException("The birthDay is before Now.It's unbelievable!");
        }
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH);
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(birthDay);

        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;

        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) {
                    age--;
                }
            } else {
                age--;
            }
        }
        return age;
    }

    public static String dateTime2Time(long timeMin) {
        int min = (int) (timeMin / ONE_MINUTE_MILLIONS);
        StringBuilder time = new StringBuilder();
        //有分钟
        time.append(min > 0 ? min + ":" : "");
        int sec = (int) (timeMin - min * ONE_MINUTE_MILLIONS) / 1000;
        time.append(min > 0 ? sec + "" : sec + "秒");
        return time.toString();
    }

    public static String zeroTime(int time) {
        return time < 10 ? "0" + time : time + "";
    }

    /**
     * 获取某一天是星期几
     *
     * @param date 日期
     * @return 返回星期
     */
    public static String getDayOfWeek(String date) {
        String dayOfWeek = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(format.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                dayOfWeek = "星期日";
                break;
            case Calendar.MONDAY:
                dayOfWeek = "星期一";
                break;
            case Calendar.TUESDAY:
                dayOfWeek = "星期二";
                break;
            case Calendar.WEDNESDAY:
                dayOfWeek = "星期三";
                break;
            case Calendar.THURSDAY:
                dayOfWeek = "星期四";
                break;
            case Calendar.FRIDAY:
                dayOfWeek = "星期五";
                break;
            case Calendar.SATURDAY:
                dayOfWeek = "星期六";
                break;
            default:
                break;
        }
        return dayOfWeek;
    }

    /**
     * 计算指定日期距现在多少秒（切勿修改顺序，用于蓝牙临时权限申请计算可用时间）
     *
     * @param date 指定的日期
     * @return 相差的秒数
     */
    public static int getSecondDelta(String date) {
        long a = new Date().getTime();
        long b = stringToUnixTime(date) * 1000;
        int delta = (int) ((b - a) / 1000);
        return b > a ? delta : 0;
    }

    /**
     * 计算两个日期之间相差的天数
     *
     * @param date1 较小的时间
     * @param date2 较大的时间
     * @return 相差天数
     */
    public static int getDaysBetween(String date1, String date2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            Date d1 = sdf.parse(date1);
            Date d2 = sdf.parse(date2);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d1);
            long time1 = cal.getTimeInMillis();
            cal.setTime(d2);
            long time2 = cal.getTimeInMillis();
            long days = (time2 - time1) / (1000 * 3600 * 24);
            return Math.abs(Integer.parseInt(String.valueOf(days)));
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 判断两个日期的月份差
     *
     * @param bt 日期1
     * @param et 日期2
     * @return 月份差，返回-1表示异常
     */
    public static int getMonthCount(String bt, String et) {
        int count = -1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            c1.setTime(sdf.parse(bt));
            c2.setTime(sdf.parse(et));
            count = c2.get(Calendar.YEAR) * 12 + c2.get(Calendar.MONTH) - c1.get(Calendar.YEAR) * 12 - c1.get(Calendar.MONTH);
            count = Math.abs(count);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * 弹出日期选择框Dialog并设置日期
     *
     * @param tv 文本控件
     */
    public static void selectData(Context context, TextView tv) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dataPickerDialog = new DatePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, (view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(year, monthOfYear, dayOfMonth);
            String date = DateFormat.format("yyyy-MM-dd", calendar).toString();
            tv.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        DatePicker datePicker = dataPickerDialog.getDatePicker();
        datePicker.setMaxDate(new Date(System.currentTimeMillis()).getTime());
        dataPickerDialog.show();
    }

    /**
     * 弹出时间选择框Dialog并设置时间
     *
     * @param context          Context上下文
     * @param tv               文本控件
     * @param defaultHourOfDay 默认小时
     * @param defaultMinute    默认分钟
     */
    public static void selectTime(Context context, TextView tv, int defaultHourOfDay, int defaultMinute) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            String s = "";
            int hour = calendar.get(Calendar.HOUR);
            if (hourOfDay >= 0 && hourOfDay < 6) {
                s = "凌晨";
            } else if (hourOfDay >= 6 && hourOfDay < 12) {
                s = "上午";
            } else if (hourOfDay == 12) {
                s = "中午";
                hour = 12;
            } else if (hourOfDay > 12 && hourOfDay < 18) {
                s = "下午";
            } else if (hourOfDay >= 18 && hourOfDay < 24) {
                s = "晚上";
            }
            tv.setText(s + MathUtils.addZeroForLeft(String.valueOf(hour), 2) + ":" + MathUtils.addZeroForLeft(String.valueOf(minute), 2));
        }, defaultHourOfDay, defaultMinute, true).show();
    }

    /**
     * 弹出日期选择框Dialog并设置日期和星期
     *
     * @param tv 文本控件
     */
    public static void selectDataWithBackground(Context context, TextView tv) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dataPickerDialog = new DatePickerDialog(context, (view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(year, monthOfYear, dayOfMonth);
            String date = DateFormat.format("yyyy-MM-dd", calendar).toString();
            tv.setText(date);
            tv.setBackgroundResource(R.drawable.round_rectangle_bg);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        DatePicker datePicker = dataPickerDialog.getDatePicker();
        datePicker.setMaxDate(new Date(System.currentTimeMillis()).getTime());
        dataPickerDialog.show();
    }

    /**
     * 获取指定日期所在月份的第一天
     *
     * @param date 指定日期，必须为 yyyy-MM-dd 格式
     * @return 所在月第一天
     */
    public static String getFirstDayOfMonth(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(format.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //设置为1号,当前日期既为本月第一天
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return DateFormat.format("yyyy-MM-dd", calendar).toString();
    }

    /**
     * 获取指定日期一个月前的日期
     *
     * @param date 指定日期，必须为 yyyy-MM-dd 格式
     * @return 一个月前的日期
     */
    public static String getLastMonthDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(format.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //月份减1
        calendar.add(Calendar.MONTH, -1);
        return DateFormat.format("yyyy-MM-dd", calendar).toString();
    }

    /**
     * 获取指定日期前n天的日期
     *
     * @param date 指定日期，必须为 yyyy-MM-dd 格式
     * @return 前n天的日期
     */
    public static String getLastDaysDate(String date, int days) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(format.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        return DateFormat.format("yyyy-MM-dd", calendar).toString();
    }

    /**
     * 获取指定日期的中国日历表达形式
     *
     * @param date 指定日期，必须为 yyyy-MM-dd 格式
     * @return 转换为的中国日历表达方式 yyyy年MM月dd日
     */
    public static String getChineseDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(format.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return DateFormat.format("yyyy年MM月dd日", calendar).toString();
    }

    /**
     * 获取中国日历的标准表达形式
     *
     * @param date 指定日期，必须为 yyyy年MM月dd日 格式
     * @return 转换为yyyy-MM-dd格式
     */
    public static String getNormalDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(format.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return DateFormat.format("yyyy-MM-dd", calendar).toString();
    }

    /**
     * 获取指定日期在一年的第几周
     *
     * @param date 指定日期，必须为 yyyy-MM-dd 格式
     * @return 在当年的周数
     */
    public static int getWeekNumber(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(format.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * 获取用于文件名字的当前时间
     * 由于文件名字不能出现空格，所以格式化的时候均以"-"连接
     *
     * @return 当前时间
     */
    public static String getCurrentTimeForFileName() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    /**
     * 毫秒数转时分秒
     *
     * @param times 毫秒值
     * @return 转换为时分秒字符串
     */
    public static String timeFormat(long times) {
        long hour = times / (60 * 60 * 1000);
        long minute = (times - hour * 60 * 60 * 1000) / (60 * 1000);
        long second = (times - hour * 60 * 60 * 1000 - minute * 60 * 1000) / 1000;
        if (second >= 60) {
            second = second % 60;
            minute += second / 60;
        }
        if (minute >= 60) {
            minute = minute % 60;
            hour += minute / 60;
        }
        String sh;
        String sm;
        String ss;
        if (hour < 10) {
            sh = "0" + String.valueOf(hour);
        } else {
            sh = String.valueOf(hour);
        }
        if (minute < 10) {
            sm = "0" + String.valueOf(minute);
        } else {
            sm = String.valueOf(minute);
        }
        if (second < 10) {
            ss = "0" + String.valueOf(second);
        } else {
            ss = String.valueOf(second);
        }
        return sh + ":" + sm + ":" + ss;
    }

    /**
     * 将时间转化为BCD码
     *
     * @param time yyyy-MM-dd HH:mm:ss格式的时间
     * @return BCD码格式
     * eg. 2018-08-22 20:42:41  →  41 42 20 22 08 18 20  →  41 42 20 22 08 18  →  414220220818
     */
    public static String convertTime2BCD(String time, int length) {
        String rx = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        SimpleDateFormat sdf1 = new SimpleDateFormat("ssmmHHddMMyyyy", Locale.CHINA);
        try {
            Date xd = sdf.parse(time);
            rx = sdf1.format(xd);
            if (length == 12) {
                rx = rx.substring(0, 10) + rx.substring(12, 14);
            } else if (length == 14) {
                rx = rx.substring(0, 10) + rx.substring(12, 14) + rx.substring(10, 12);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rx;
    }


    /**
     * 计算某个时间距离现在多少秒/分钟/小时/天/周/月/年
     *
     * @return 距离时间
     */
    public static String getIntervalTime(String str1) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date one;
        String distanceTime = str1;
        try {
            one = df.parse(str1);
            Date now = new Date();
            long time1 = one.getTime();
            long nowTime = now.getTime();
            long diff;
            if (nowTime > time1) {
                diff = nowTime - time1;
            } else {
                return str1;
            }
            long year, month, week, day, hour, min, sec;
            sec = diff / (1000);
            min = diff / (60 * 1000);
            hour = diff / (60 * 60 * 1000);
            day = diff / (24 * 60 * 60 * 1000);
            week = day / 7;
            month = day / 30;
            year = day / 365;
            if (year >= 1) {
                distanceTime = year + "年前";
            } else if (month >= 1) {
                distanceTime = month + "个月前";
            } else if (week >= 1) {
                distanceTime = week + "周前";
            } else if (day >= 1) {
                distanceTime = day + "天前";
            } else if (hour >= 1) {
                distanceTime = hour + "小时前";
            } else if (min >= 1) {
                distanceTime = min + "分钟前";
            } else if (sec >= 1) {
                distanceTime = sec + "秒前";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return distanceTime;
    }


    /**
     * 以秒为单位的时间区间转换为 日-时-分-秒
     *
     * @return 以秒为单位的时间段
     */
    public static String time(long seconds) {
        StringBuilder time = new StringBuilder();
        long day, hour, min, sec;
        day = seconds / (24 * 60 * 60);
        if (day >= 0) {
            time.append(day);
            time.append("天");
            seconds = seconds - day * 24 * 60 * 60;
        }
        hour = seconds / (60 * 60);
        if (hour >= 0) {
            time.append(hour);
            time.append("时");
            seconds = seconds - hour * 60 * 60;
        }
        min = seconds / 60;
        if (min >= 0) {
            time.append(min);
            time.append("分");
            seconds = seconds - min * 60;
        }
        sec = seconds;
        if (sec >= 0) {
            time.append(sec);
            time.append("秒");
        }
        return time.toString();
    }

    /**
     * 在现有时间上加上n秒
     *
     * @param seconds 增加的秒数
     * @return
     */
    public static String addTime(int seconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date date = new Date(System.currentTimeMillis());
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.add(Calendar.SECOND, seconds);
        return sdf.format(ca.getTime());
    }
}
