package jp.co.shiratsuki.walkietalkie.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.bean.User;
import jp.co.shiratsuki.walkietalkie.bean.WebSocketData;
import jp.co.shiratsuki.walkietalkie.broadcast.BaseBroadcastReceiver;
import jp.co.shiratsuki.walkietalkie.constant.NetWork;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.GsonUtils;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.voice.MusicPlay;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
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

    private List<WebSocketData> malfunctionList;
    private MsgWebSocketClient msgWebSocketClient;

    private ExecutorService threadPool;

    private MyReceiver myReceiver;

    private boolean first = true, flag = true;

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
        showNotification();
        malfunctionList = new ArrayList<>();
        webSocketServiceBinder = new WebSocketServiceBinder();
        MusicPlay.with(getApplicationContext()).play();
        initWebSocket();
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("USER_DELETE_MALFUNCTION");
        registerReceiver(myReceiver, intentFilter);
    }

    /**
     * 前台Service
     */
    private void showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel Channel = new NotificationChannel("124", "异常信息推送服务", NotificationManager.IMPORTANCE_HIGH);
            Channel.enableLights(true);                                         //设置提示灯
            Channel.setLightColor(Color.RED);                                   //设置提示灯颜色
            Channel.setShowBadge(true);                                         //显示logo
            Channel.setDescription("实时推送异常信息服务");                     //设置描述
            Channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);    //设置锁屏可见 VISIBILITY_PUBLIC=可见
            manager.createNotificationChannel(Channel);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "124");
            notification.setContentTitle(getString(R.string.app_name));
            notification.setContentText("实时推送异常信息服务运行中...");
            notification.setWhen(System.currentTimeMillis());
            notification.setSmallIcon(R.mipmap.ic_launcher);
            notification.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            startForeground(124, notification.build());
        } else {
            Notification notification = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.app_name))                                      //设置标题
                    .setContentText("实时推送异常信息服务运行中...")                                    //设置内容
                    .setWhen(System.currentTimeMillis())                                                //设置创建时间
                    .setSmallIcon(R.mipmap.ic_launcher)                                                 //设置状态栏图标
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))   //设置通知栏图标
                    .build();
            startForeground(124, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * 初始化并启动启动WebSocket
     */
    public void initWebSocket() {
        User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        String serverHost, webSocketPort, webSocketName;
        if (user.getMessage_ip().equals("") || user.getMessage_port().equals("")) {
            serverHost = NetWork.MESSAGE_SERVER_IP;
            webSocketPort = NetWork.MESSAGE_SERVER_PORT;
        } else {
            serverHost = user.getMessage_ip();
            webSocketPort = user.getMessage_port();
        }
        webSocketName = String.valueOf(NetWork.MESSAGE_SERVER_NAME);

        try {
            msgWebSocketClient = new MsgWebSocketClient(this, "ws://" + serverHost + ":" + webSocketPort + "/" + webSocketName, new IMsgWebSocket() {
                @Override
                public void openSuccess() {
                    if (first) {
                        threadPool.submit(heartBeatRunnable);
                    }
                    first = false;
                }

                @Override
                public void closed() {
                    reConnect();
                }
            });
            msgWebSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送心跳包
     */
    private Runnable heartBeatRunnable = () -> {
        while (flag) {
            LogUtils.d(TAG, "消息服务器WebSocket发送心跳包");
            sendMessage("");
            try {
                Thread.sleep(NetWork.HEART_BEAT_RATE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 重连WebSocket
     */
    public void reConnect() {
        LogUtils.d(TAG, "WebSocket重连");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(NetWork.WEBSOCKET_RECONNECT_RATE);
                    msgWebSocketClient.reconnectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        threadPool.submit(runnable);
    }

    /**
     * 关闭WebSocket
     */
    public void closeWebSocket() {
        if (msgWebSocketClient != null) {
            LogUtils.d(TAG, "手动关闭WebSocket");
            msgWebSocketClient.close();
        }
    }

    /**
     * WebSocket是否已连接
     *
     * @return 是否连接
     */
    public boolean isOpen() {
        return msgWebSocketClient != null && msgWebSocketClient.isOpen();
    }

    /**
     * WebSocket发送消息
     *
     * @param msg 需要发送的信息
     */
    public void sendMessage(String msg) {
        if (isOpen()) {
            msgWebSocketClient.send(msg);
        } else {
            reConnect();
        }
    }

    private class MyReceiver extends BaseBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            if (intent != null && intent.getAction() != null) {
                switch (intent.getAction()) {
                    case "USER_DELETE_MALFUNCTION":
                        int ListNo = intent.getIntExtra("ListNo", -1);
                        if (ListNo != -1) {
                            int position = -1;
                            for (int i = 0; i < malfunctionList.size(); i++) {
                                if (malfunctionList.get(i).getListNo() == ListNo) {
                                    position = i;
                                    break;
                                }
                            }
                            if (position != -1) {
                                malfunctionList.remove(position);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        flag = false;
        MusicPlay.with(WebSocketService.this.getApplicationContext()).release();
        closeWebSocket();
        if (myReceiver != null) {
            unregisterReceiver(myReceiver);
        }
    }

}