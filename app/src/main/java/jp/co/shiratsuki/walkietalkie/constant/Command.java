package jp.co.shiratsuki.walkietalkie.constant;

/**
 * 指令类别标记
 * Created at 2018/12/12 13:00
 *
 * @author LiYuliang
 * @version 1.0
 */

public class Command {

    // Service向Activity发送的跨进程指令
    public static final String DISC_REQUEST = "DISC_REQUEST";
    public static final String DISC_RESPONSE = "DISC_RESPONSE";
    public static final String DISC_LEAVE = "DISC_LEAVE";

    // 前台Service
    public static final int FOREGROUND_SERVICE = 101;
}
