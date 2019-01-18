package jp.co.shiratsuki.walkietalkie.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import jp.co.shiratsuki.walkietalkie.bean.Music;
import jp.co.shiratsuki.walkietalkie.bean.MusicList;
import jp.co.shiratsuki.walkietalkie.bean.WebSocketData;
import jp.co.shiratsuki.walkietalkie.constant.NetWork;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.GsonUtils;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.voice.MusicPlay;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private long currentThreadId = -1;

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
        webSocketServiceBinder = new WebSocketServiceBinder();
        MusicPlay.with(WebSocketService.this).play();
        initWebSocket();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connectWebSocket();
        return START_STICKY;
    }

    /**
     * 初始化并启动启动WebSocket
     */
    public void initWebSocket() {
        threadPool = Executors.newScheduledThreadPool(1);
        try {
            mSocketClient = getWebSocketClient(Thread.currentThread());
            LogUtils.d(TAG, "创建线程：" + Thread.currentThread().getId());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取WebSocketClient对象
     *
     * @return WebSocketClient对象
     * @throws URISyntaxException
     */
    public WebSocketClient getWebSocketClient(final Thread currentThread) throws URISyntaxException {
        // 获取最新的WebSocket参数
        serverHost = SPHelper.getString("MessageServerIP", NetWork.WEBSOCKET_IP);
        String webSocketPort = SPHelper.getString("MessageServerPort", NetWork.WEBSOCKET_PORT);
        String webSocketName = String.valueOf(NetWork.WEBSOCKET_NAME);
        return new WebSocketClient(new URI("ws://" + serverHost + ":" + webSocketPort + "/" + webSocketName), new Draft_6455()) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                //通道打开
                LogUtils.d(TAG, "建立连接");
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
//                        Intent intent = new Intent();
//                        intent.setAction("MESSAGE_WEBSOCKET_CLOSED");
//                        sendBroadcast(intent);
                // 清空异常信息音乐列表
                MusicPlay.with(WebSocketService.this).getMusicListList().clear();
                // 如果code等于CloseFrame.NORMAL，表示是用户手动断开的，否则是异常断开的，就需要重连
                if (code == CloseFrame.NORMAL) {
                    // 调用close 方法
                    LogUtils.d(TAG, "WebSocket正常关闭，关闭代码：" + code + ",关闭线程：" + currentThread.getId());
                    currentThread.interrupt();
                } else {
                    // 出现异常，重新连接
                    LogUtils.d(TAG, "WebSocket异常关闭，关闭代码：" + code + ",重新连接");
                    reConnect();
                }
            }

            @Override
            public void onError(Exception ex) {
                //发生错误
                LogUtils.d(TAG, "发生错误：" + ex.getMessage());
            }
        };
    }

    /**
     * 重连WebSocket
     */
    public void reConnect() {
        // 等待5秒再重连
        LogUtils.d(TAG, "WebSocket重连");
        threadPool.execute(() -> {
            currentThreadId = Thread.currentThread().getId();
            try {
                Thread.sleep(NetWork.WEBSOCKET_RECONNECT_TIME_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    LogUtils.d(TAG, "创建线程：" + Thread.currentThread().getId());
                    mSocketClient = getWebSocketClient(Thread.currentThread());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                mSocketClient.connect();
            }
        });
    }

    /**
     * 关闭WebSocket
     */
    public void closeWebSocket() {
        if (mSocketClient != null) {
            LogUtils.d(TAG, "手动关闭WebSocket");
            mSocketClient.onClose(CloseFrame.NORMAL, "用户手动关闭", true);
//            mSocketClient.close(CloseFrame.NORMAL, "用户手动关闭");
        }
    }

    /**
     * 连接WebSocket
     */
    public void connectWebSocket() {
        threadPool.execute(() -> {
            currentThreadId = Thread.currentThread().getId();
            if (mSocketClient != null) {
                mSocketClient.connect();
            }
        });
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
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MusicPlay.with(WebSocketService.this).release();
        closeWebSocket();
        threadPool.shutdown();
    }

}
