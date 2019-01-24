package jp.co.shiratsuki.walkietalkie.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 半角、全角文字处理工具类
 */

public class DbcSbcUtils {

	/**
	 * 半角、全角字符判断
	 * 
	 * @param c
	 *            字符
	 * @return true：半角； false：全角
	 */
	public static boolean isDbcCase(char c) {
		// 基本拉丁字母（即键盘上可见的，空格、数字、字母、符号）
		if (c >= 32 && c <= 127) {
			return true;
		}
		// 日文半角片假名和符号
		else if (c >= 65377 && c <= 65439) {
			return true;
		}
		return false;
	}

	/**
	 * 字符串截取（区分半角、全角）
	 * 
	 * @param str
	 *            字符串
	 * @return
	 */
	public static String getPatStr(String str) {
		char[] chars = str.toCharArray();
		String result = "";
		for (int i = 0; i < chars.length; i++) {
			if (isDbcCase(chars[i])) {
				result += chars[i];
			} else {
				try {
					result += URLEncoder.encode(String.valueOf(chars[i]), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
