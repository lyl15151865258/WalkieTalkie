package jp.co.shiratsuki.walkietalkie.message;

import android.content.Context;
import android.content.Intent;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.shiratsuki.walkietalkie.bean.Music;
import jp.co.shiratsuki.walkietalkie.bean.MusicList;
import jp.co.shiratsuki.walkietalkie.bean.User;
import jp.co.shiratsuki.walkietalkie.bean.WebSocketData;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.GsonUtils;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.utils.WifiUtil;

public class MsgWebSocketClient extends WebSocketClient {

    private String TAG = "MsgWebSocketClient";
    private Context mContext;
    private List<WebSocketData> malfunctionList;
    private User user;

    private IMsgWebSocket iMsgWebSocket;

    public MsgWebSocketClient(Context mContext, String url, IMsgWebSocket iMsgWebSocket) throws URISyntaxException {
        super(new URI(url));
        this.mContext = mContext;
        this.iMsgWebSocket = iMsgWebSocket;
        user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        malfunctionList = new ArrayList<>();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        //通道打开
        LogUtils.d(TAG, "建立连接");

        // 告诉服务器本机IP和用户名
        Map<String, String> map = new HashMap<>();
        map.put("IPAddress", WifiUtil.getLocalIPAddress());
        map.put("UserName", user.getUser_id());
        send(GsonUtils.convertJSON(map));

        iMsgWebSocket.openSuccess();
    }

    @Override
    public void onMessage(String message) {
        LogUtils.d(TAG, message);

        if (message.contains("PingPong")) {
            // 收到心跳包，原文返回
            send(message);
        } else {
            // 收到异常信息的内容
            WebSocketData webSocketData = GsonUtils.parseJSON(message, WebSocketData.class);
            Intent intent = new Intent();
            intent.setAction("RECEIVE_MALFUNCTION");
            intent.putExtra("data", webSocketData);
            mContext.sendBroadcast(intent);

            List<String> voiceList = webSocketData.getFileName();
            if (voiceList != null && voiceList.size() > 0) {
                // 判断播放次数，-1为无穷播放，0为不播放，正整数为相应播放次数
                if (webSocketData.getPlayCount() != 0) {
                    //播放次数为-1或者正整数
                    if (webSocketData.isStatus()) {
                        if (!malfunctionList.contains(webSocketData)) {
                            List<Music> musicList = new ArrayList<>();
                            for (String voiceName : voiceList) {
                                LogUtils.d(TAG, "音乐文件名称：" + voiceName);
                                musicList.add(new Music(webSocketData.getListNo(), voiceName, webSocketData.getPlayCount(), 0));
                            }
                            int interval1 = webSocketData.getVoiceInterval1();
                            int interval2 = webSocketData.getVoiceInterval2();
                            MusicPlayer.with(mContext.getApplicationContext()).addMusic(new MusicList(webSocketData.getListNo(), webSocketData.getPriority(), musicList,
                                            webSocketData.getJapanese(), webSocketData.getChinese(), webSocketData.getEnglish(), webSocketData.getPlayCount(), 0),
                                    interval1, interval2);
                        }

                    } else {
                        MusicPlayer.with(mContext.getApplicationContext()).removeMusic(webSocketData.getListNo());
                    }
                } else {
                    LogUtils.d(TAG, "播放次数为0，不添加到音乐播放列表");
                }
            }
            receiveMalfunction(webSocketData);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // 通道关闭
        LogUtils.d(TAG, "连接断开");
        iMsgWebSocket.closed();
        // 通知主页面列表清空
        Intent intent = new Intent();
        intent.setAction("MESSAGE_WEBSOCKET_CLOSED");
        mContext.sendBroadcast(intent);

        // 清空自身列表
        malfunctionList.clear();
        // 清空异常信息音乐列表
        MusicPlayer.with(mContext.getApplicationContext()).getMusicListList().clear();
    }

    @Override
    public void onError(Exception ex) {
        //发生错误
        LogUtils.d(TAG, "发生错误：" + ex.getMessage());
        ex.printStackTrace();
    }


    /**
     * 收到异常
     *
     * @param webSocketData 异常信息实体
     */
    private void receiveMalfunction(WebSocketData webSocketData) {
        if (webSocketData.isStatus()) {
            // 遍历对比是否包含了这个ListNo
            if (!malfunctionList.contains(webSocketData)) {
                malfunctionList.add(malfunctionList.size(), webSocketData);
            }
        } else {
            int position = -1;
            for (int i = 0; i < malfunctionList.size(); i++) {
                if (malfunctionList.get(i).getListNo() == webSocketData.getListNo()) {
                    position = i;
                    break;
                }
            }
            if (position != -1) {
                malfunctionList.remove(position);
            }
        }
    }
}
