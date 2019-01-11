package jp.co.shiratsuki.walkietalkie.constant;

import android.media.AudioFormat;

/**
 * 语音参数常量
 * Created at 2018/11/24 13:43
 *
 * @author LiYuliang
 * @version 1.0
 */

public class VoiceConstant {

    /**
     * 声音采集频率
     */
    public static final int FREQUENCY = 8000;
    /**
     * 声音编码
     */
    public static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    /**
     * 局域网组播IP地址
     *
     * 组播的地址范围:
     *
     * 224.0.0.0～224.0.0.255为预留的组播地址（永久组地址），地址224.0.0.0保留不做分配，其它地址供路由协议使用；
     *
     * 224.0.1.0～224.0.1.255是公用组播地址，可以用于Internet；
     *
     * 224.0.2.0～238.255.255.255为用户可用的组播地址（临时组地址），全网范围内有效
     *
     * 239.0.0.0～239.255.255.255为本地管理组播地址，仅在特定的本地范围内有效。
     *
     */
    public static final String BROADCAST_IP = "224.0.0.1";
    /**
     * 局域网组播端口
     */
    public static final int BROADCAST_PORT = 30000;
    public static final int NOTICE_ID = 100;


    public static final String MUSIC_FILE_CHINESE_PATH = "tts/chinese/tts_%s.mp3";

    public static final String DOT_POINT = ".";
    //小数点
    public static final String DOT = "dot";
    //十
    public static final String TEN = "ten";
    //百
    public static final String HUNDRED = "hundred";
    //千
    public static final String THOUSAND = "thousand";
    //万
    public static final String TEN_THOUSAND = "ten_thousand";
    //亿
    public static final String TEN_MILLION = "ten_million";
    //收款成功
    public static final String SUCCESS = "success";
    //元
    public static final String YUAN = "yuan";
}
