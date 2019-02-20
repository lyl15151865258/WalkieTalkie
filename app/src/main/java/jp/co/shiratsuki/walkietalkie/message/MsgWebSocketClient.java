package jp.co.shiratsuki.walkietalkie.message;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.LocaleList;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.co.shiratsuki.walkietalkie.bean.Music;
import jp.co.shiratsuki.walkietalkie.bean.MusicList;
import jp.co.shiratsuki.walkietalkie.bean.User;
import jp.co.shiratsuki.walkietalkie.bean.WebSocketData;
import jp.co.shiratsuki.walkietalkie.constant.NetWork;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.GsonUtils;
import jp.co.shiratsuki.walkietalkie.utils.LanguageUtil;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.utils.WifiUtil;
import jp.co.shiratsuki.walkietalkie.voice.MusicPlay;

public class MsgWebSocketClient extends WebSocketClient {

    private String TAG = "MsgWebSocketClient";
    private Context mContext;
    private List<WebSocketData> malfunctionList;
    private String serverHost;
    private User user;

    private IMsgWebSocket iMsgWebSocket;

    public MsgWebSocketClient(Context mContext, String url, IMsgWebSocket iMsgWebSocket) throws URISyntaxException {
        super(new URI(url));
        this.mContext = mContext;
        this.iMsgWebSocket = iMsgWebSocket;
        user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        malfunctionList = new ArrayList<>();
        if (user.getMessage_ip().equals("") || user.getMessage_port().equals("")) {
            serverHost = NetWork.MESSAGE_SERVER_IP;
        } else {
            serverHost = user.getMessage_ip();
        }
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

        WebSocketData webSocketData = GsonUtils.parseJSON(message, WebSocketData.class);
        Intent intent = new Intent();
        intent.setAction("RECEIVE_MALFUNCTION");
        intent.putExtra("data", webSocketData);
        mContext.sendBroadcast(intent);

        List<String> voiceList = webSocketData.getFileName();
        if (voiceList != null && voiceList.size() > 0) {
            if (webSocketData.isStatus()) {
                if (!malfunctionList.contains(webSocketData)) {
                    List<Music> musicList = new ArrayList<>();
                    for (String voiceName : voiceList) {
                        String directory = "";
                        switch (LanguageUtil.getLanguageLocal(mContext)) {
                            case "":
                                // 手机设置的语言是跟随系统
                                Locale locale;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    locale = LocaleList.getDefault().get(0);
                                } else {
                                    locale = Locale.getDefault();
                                }
                                String language = locale.getLanguage();
                                switch (language) {
                                    case "zh":
                                        directory = webSocketData.getChinese();
                                        break;
                                    case "ja":
                                        directory = webSocketData.getJapanese();
                                        break;
                                    default:
                                        directory = webSocketData.getEnglish();
                                        break;
                                }
                                break;
                            case "zh":
                                directory = webSocketData.getChinese();
                                break;
                            case "ja":
                                directory = webSocketData.getJapanese();
                                break;
                            case "en":
                                directory = webSocketData.getEnglish();
                                break;
                            default:
                                break;
                        }
                        String musicPath = "http://" + serverHost + "/" + directory + "/" + voiceName;
                        LogUtils.d(TAG, "音乐文件路径：" + musicPath);
                        musicList.add(new Music(webSocketData.getListNo(), musicPath, webSocketData.getPlayCount(), 0));
                    }
                    int interval1 = webSocketData.getVoiceInterval1();
                    int interval2 = webSocketData.getVoiceInterval2();
                    MusicPlay.with(mContext.getApplicationContext()).addMusic(new MusicList(webSocketData.getListNo(), musicList, webSocketData.getPlayCount(), 0), interval1, interval2);
                }
            } else {
                MusicPlay.with(mContext.getApplicationContext()).removeMusic(webSocketData.getListNo());
            }
        }

        receiveMalfunction(webSocketData);
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
        // 清空异常信息音乐列表
        MusicPlay.with(mContext.getApplicationContext()).getMusicListList().clear();
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
