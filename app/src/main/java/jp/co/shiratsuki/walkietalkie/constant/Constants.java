package jp.co.shiratsuki.walkietalkie.constant;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;

/**
 * 部分常量值
 * Created at 2018/11/28 13:42
 *
 * @author LiYuliang
 * @version 1.0
 */

public class Constants {

    public static final String FAIL = "fail";
    public static final String SUCCESS = "success";

    // 单播端口号
    public static final int UNICAST_PORT = 9998;
    // 组播端口号
    public static final int MULTI_BROADCAST_PORT = 9999;
    // 组播地址
    public static final String MULTI_BROADCAST_IP = "224.9.9.9";
    // 采样频率
    public static final int sampleRateInHz = 8000;
    // 音频数据格式:PCM 16位每个样本，保证设备支持。
    public static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    // 音频获取源
    public static final int audioSource = MediaRecorder.AudioSource.MIC;
    // 输入单声道
    public static final int inputChannelConfig = AudioFormat.CHANNEL_IN_MONO;

    // 音频播放端
    public static final int streamType = AudioManager.STREAM_MUSIC;
    // 输出单声道
    public static final int outputChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
    // 音频输出模式
    public static final int trackMode = AudioTrack.MODE_STREAM;

    // WebSocket地址
    public static final String WEBSOCKET_IP = "192.168.2.102";
    // WebSocket端口号
    public static final int WEBSOCKET_PORT = 50100;
    // WebSocket名称
    public static final String WEBSOCKET_NAME = "Interphone";

    // 音视频通讯相关内容
    public static final String APP_ID = "3768c59536565afb";
    public static final String APP_KEY = "df191ec457951c35b8796697c204382d0e12d4e8cb56f54df6a54394be74c5fe";
    public static final String SERVER_ADDRESS = "192.168.2.104:8080";
    public static final int K_ROOM_TYPE = 0x03 | 0x60 | 0x1800;

}
