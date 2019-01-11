package jp.co.shiratsuki.walkietalkie.constant;

/**
 * 网络常量值
 * Created at 2018/11/28 13:42
 *
 * @author LiYuliang
 * @version 1.0
 */

public class NetWork {

    /**
     * 主账号IP地址
     */
    public static final String SERVER_HOST_MAIN = "www.metter.com.cn";
    /**
     * 主账号端口号
     */
    public static final String SERVER_PORT_MAIN = "8073";
    /**
     * 主账号项目名
     */
    public static final String PROJECT_MAIN = "AndroidManager";
//    public static final String SERVER_HOST_MAIN = "58.240.47.50";
//    public static final String SERVER_PORT_MAIN = "5068";
//    public static final String PROJECT_MAIN = "android";

    //网络超时、socket心跳包发送间隔等
    /**
     * http请求超时时间
     */
    public static final int TIME_OUT_HTTP = 10 * 1000;
    /**
     * socket请求超时时间
     */
    public static final int TIME_OUT_SOCKET = 5 * 1000;
    /**
     * 安卓注册Socket通信
     */
    public static String ANDROID_LOGIN = "*#ANDROIDLOGIN:";
    /**
     * 安卓指令开始标记
     */
    public static String ANDROID_CMD = "*#ADNROIDCMD:";
    /**
     * 安卓数据结束
     */
    public static String ANDROID_END = "END#*";
    /**
     * 心跳包发送内容
     */
    public static final String HEART_BEAT_PACKAGE = "\r\n";
    /**
     * 心跳包发送间隔（10秒）
     */
    public static final int HEART_BEAT_RATE = 10 * 1000;
}
