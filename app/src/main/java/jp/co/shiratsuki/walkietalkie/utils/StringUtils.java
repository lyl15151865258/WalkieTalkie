package jp.co.shiratsuki.walkietalkie.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串处理工具类
 * Created at 2018/11/28 13:54
 *
 * @author LiYuliang
 * @version 1.0
 */

public class StringUtils {

    /**
     * 提取字符串中的 数字 带小数点 ，没有就返回""
     *
     * @param money
     * @return
     */
    public static String getMoney(String money) {
        Pattern pattern = Pattern.compile("(\\d+\\.\\d+)");
        Matcher m = pattern.matcher(money);
        if (m.find()) {
            money = m.group(1) == null ? "" : m.group(1);
        } else {
            pattern = Pattern.compile("(\\d+)");
            m = pattern.matcher(money);
            if (m.find()) {
                money = m.group(1) == null ? "" : m.group(1);
            } else {
                money = "";
            }
        }
        return money;
    }
}
