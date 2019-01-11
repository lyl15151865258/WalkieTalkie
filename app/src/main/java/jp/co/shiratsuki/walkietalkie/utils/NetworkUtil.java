package jp.co.shiratsuki.walkietalkie.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网络工具类
 * Created at 2018/5/5 0005 11:48
 *
 * @author LiYuliang
 * @version 1.0
 */

public class NetworkUtil {

    /**
     * Returns true if device is connected to wifi or mobile network, false
     * otherwise.
     *
     * @param context Context对象
     * @return 是否有网络连接
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        } else {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (conMan != null) {
                NetworkInfo infoWifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (infoWifi != null) {
                    NetworkInfo.State wifi = infoWifi.getState();
                    if (wifi == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }

                NetworkInfo infoMobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (infoMobile != null) {
                    NetworkInfo.State mobile = infoMobile.getState();
                    return mobile == NetworkInfo.State.CONNECTED;
                }
                return false;
            } else {
                return false;
            }
        }
    }

    /**
     * Check if there is any connectivity to a Wifi network
     *
     * @param context Context对象
     * @return 是否是WiFi连接
     */
    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Check if there is any connectivity to a mobile network
     *
     * @param context Context对象
     * @return 是否是移动网络连接
     */
    public static boolean isConnectedMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Get the network info
     *
     * @param context Context对象
     * @return 获取NetworkInfo对象
     */
    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * 检查 URL 是否合法
     *
     * @param url 输入的URL
     * @return true 合法，false 非法
     */
    public static boolean isNetworkUrl(String url) {
        String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        Pattern patt = Pattern.compile(regex);
        Matcher matcher = patt.matcher(url);
        return matcher.matches();
    }

    /**
     * 获取SIM卡运营商
     *
     * @param context
     * @return
     */
    public static String getOperators(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = "";
        String IMSI = tm.getSubscriberId();
        if (IMSI == null || IMSI.equals("")) {
            return operator;
        }
        if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
            operator = "中国移动";
        } else if (IMSI.startsWith("46001")) {
            operator = "中国联通";
        } else if (IMSI.startsWith("46003")) {
            operator = "中国电信";
        }
        return operator;
    }

}
