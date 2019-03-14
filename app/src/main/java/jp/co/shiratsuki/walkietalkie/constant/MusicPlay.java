package jp.co.shiratsuki.walkietalkie.constant;

/**
 * 播放音乐的一些常量值
 * Created at 2019/3/14 13:56
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class MusicPlay {

    // 音乐列表（异常信息）之间的默认播放间隔时间，单位：毫秒
    public static final int INTERVAL_ONE_LIST = 1000;

    // 整个音乐列表循环播放时的播放间隔，单位：毫秒
    public static final int INTERVAL_TOTAL_LIST = 3000;

    // 单个音乐列表（一个异常信息）最长播放时间，单位：秒
    public static final int MAX_PLAY_TIME_ONE_LIST = 10;

}
