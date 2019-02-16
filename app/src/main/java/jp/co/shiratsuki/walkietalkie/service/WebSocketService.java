package jp.co.shiratsuki.walkietalkie.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import jp.co.shiratsuki.walkietalkie.bean.Music;
import jp.co.shiratsuki.walkietalkie.bean.MusicList;
import jp.co.shiratsuki.walkietalkie.bean.User;
import jp.co.shiratsuki.walkietalkie.bean.WebSocketData;
import jp.co.shiratsuki.walkietalkie.constant.NetWork;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.GsonUtils;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.utils.WifiUtil;
import jp.co.shiratsuki.walkietalkie.voice.MusicPlay;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 接收服务器异常信息的WebSocket
 * Created at 2018-12-17 13:50
 *
 * @author LiYuliang
 * @version 1.0
 */

public class WebSocketService extends Service {

    private final static String TAG = "WebSocketService";
    private WebSocketServiceBinder webSocketServiceBinder;

    private WebSocketClient mSocketClient;
    private String serverHost;

    private ExecutorService threadPool;

    private Handler mHandler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        return webSocketServiceBinder;
    }

    public class WebSocketServiceBinder extends Binder {
        /**
         * WebSocketServiceBinder
         *
         * @return SocketService对象
         */
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        threadPool = new ThreadPoolExecutor(5, 10, 60, TimeUnit.SECONDS,
                new SynchronousQueue<>(), (r) -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
        webSocketServiceBinder = new WebSocketServiceBinder();
        MusicPlay.with(WebSocketService.this).play();
        initWebSocket();
        threadPool.submit(heartBeatRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * 初始化并启动启动WebSocket
     */
    public void initWebSocket() {
        threadPool.execute(() -> {
            try {
                mSocketClient = getWebSocketClient();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            if (mSocketClient != null) {
                mSocketClient.connect();
            }
        });
    }

    /**
     * 获取WebSocketClient对象
     *
     * @return WebSocketClient对象
     * @throws URISyntaxException
     */
    public WebSocketClient getWebSocketClient() throws URISyntaxException {
        // 获取最新的WebSocket参数
        serverHost = SPHelper.getString("MessageServerIP", NetWork.WEBSOCKET_IP);
        String webSocketPort = SPHelper.getString("MessageServerPort", NetWork.WEBSOCKET_PORT);
        String webSocketName = String.valueOf(NetWork.WEBSOCKET_NAME);
        return new WebSocketClient(new URI("ws://" + serverHost + ":" + webSocketPort + "/" + webSocketName), new Draft_6455(), null, 5000) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                //通道打开
                LogUtils.d(TAG, "建立连接");

                // 通知异常页面清空数据
                Intent intent = new Intent();
                intent.setAction("MESSAGE_WEBSOCKET_CLOSED");
                sendBroadcast(intent);

                // 告诉服务器本机IP和用户名
                Map<String, String> map = new HashMap<>();
                map.put("IPAddress", WifiUtil.getLocalIPAddress());
                User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
                map.put("UserName", user.getUser_id());
                sendMessage(GsonUtils.convertJSON(map));
            }

            @Override
            public void onMessage(String message) {
                LogUtils.d(TAG, message);

                WebSocketData webSocketData = GsonUtils.parseJSON(message, WebSocketData.class);
                Intent intent = new Intent();
                intent.setAction("RECEIVE_MALFUNCTION");
                intent.putExtra("data", webSocketData);
                WebSocketService.this.sendBroadcast(intent);

                List<String> voiceList = webSocketData.getFileName();
                if (voiceList != null && voiceList.size() > 0) {
                    if (webSocketData.isStatus()) {
                        if (webSocketData.getPlayCount() > 0) {
                            List<Music> musicList = new ArrayList<>();
                            for (String voiceName : voiceList) {
                                String musicPath = "http://" + serverHost + "/andonvoicedata/01_Japanese/" + voiceName;
                                LogUtils.d(TAG, "音乐文件路径：" + musicPath);
                                musicList.add(new Music(webSocketData.getListNo(), musicPath, webSocketData.getPlayCount(), 0));
                            }
                            int interval1 = webSocketData.getVoiceInterval1();
                            int interval2 = webSocketData.getVoiceInterval2();
                            MusicPlay.with(WebSocketService.this).addMusic(new MusicList(webSocketData.getListNo(), musicList, webSocketData.getPlayCount(), 0), interval1, interval2);
                        }
                    } else {
                        MusicPlay.with(WebSocketService.this).removeMusic(webSocketData.getListNo());
                    }
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                //通道关闭
                // 通知主页面列表清空
                Intent intent = new Intent();
                intent.setAction("MESSAGE_WEBSOCKET_CLOSED");
                sendBroadcast(intent);
                // 清空异常信息音乐列表
                MusicPlay.with(WebSocketService.this).getMusicListList().clear();
            }

            @Override
            public void onError(Exception ex) {
                //发生错误
                LogUtils.d(TAG, "发生错误：" + ex.getMessage());
            }
        };
    }

    /**
     * 发送心跳包
     */
    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            // 心跳包只需发送一个SocketId过去, 以节约数据流量
            // 如果发送失败，就重新初始化一个socket
            Runnable runnable = () -> {
                LogUtils.d(TAG, "WebSocket发送心跳包");
                sendMessage("");
            };
            threadPool.submit(runnable);
            mHandler.postDelayed(this, NetWork.HEART_BEAT_RATE);
        }
    };

    /**
     * 重连WebSocket
     */
    public void reConnect() {
        threadPool.execute(() -> {
            try {
                // 等待5秒再重连
                Thread.sleep(NetWork.WEBSOCKET_RECONNECT_TIME_INTERVAL);
                try {
                    mSocketClient = getWebSocketClient();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                mSocketClient.connect();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 关闭WebSocket
     */
    public void closeWebSocket() {
        if (mSocketClient != null) {
            LogUtils.d(TAG, "手动关闭WebSocket");
            mSocketClient.close();
        }
    }

    /**
     * WebSocket是否已连接
     *
     * @return
     */
    public boolean isOpen() {
        return mSocketClient != null && mSocketClient.isOpen();
    }

    /**
     * WebSocket发送消息
     *
     * @param msg 需要发送的信息
     */
    public void sendMessage(String msg) {
        if (isOpen()) {
            mSocketClient.send(msg);
        } else {
            reConnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MusicPlay.with(WebSocketService.this).release();
        closeWebSocket();
    }

}
