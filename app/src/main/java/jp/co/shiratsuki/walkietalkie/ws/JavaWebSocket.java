package jp.co.shiratsuki.walkietalkie.ws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.webrtc.IceCandidate;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.X509TrustManager;

import jp.co.shiratsuki.walkietalkie.utils.LogUtils;

/**
 * Created by dds on 2019/1/3.
 * android_shuai@163.com
 */
public class JavaWebSocket implements IWebSocket {

    private final static String TAG = "dds_JavaWebSocket";

    private WebSocketClient mWebSocketClient;

    private ISignalingEvents events;

    public JavaWebSocket(ISignalingEvents events) {
        this.events = events;
    }

    public void connect(String wss, final String room) {
        URI uri;
        try {
            uri = new URI(wss);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        if (mWebSocketClient == null) {
            mWebSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    joinRoom(room);
                }

                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    LogUtils.d(TAG, "关闭WebSocket连接：" + reason);
                }

                @Override
                public void onError(Exception ex) {
                    LogUtils.d(TAG, "WebSocket连接出错：" + ex.toString());
                }
            };
        }
//        try {
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            if (sslContext != null) {
//                sslContext.init(null, new TrustManager[]{new TrustManagerTest()}, new SecureRandom());
//            }
//
//            SSLSocketFactory factory = null;
//            if (sslContext != null) {
//                factory = sslContext.getSocketFactory();
//            }
//
//            if (factory != null) {
//                mWebSocketClient.setSocket(factory.createSocket());
//            }
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        mWebSocketClient.connect();
    }

    public void close() {
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
        }

    }

    //============================需要发送的=====================================
    @Override
    public void joinRoom(String room) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__join");
        Map<String, String> childMap = new HashMap<>();
        childMap.put("room", room);
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        mWebSocketClient.send(jsonString);
    }


    public void sendAnswer(String socketId, String sdp) {
        Map<String, Object> childMap1 = new HashMap();
        childMap1.put("type", "answer");
        childMap1.put("sdp", sdp);
        HashMap<String, Object> childMap2 = new HashMap();
        childMap2.put("socketId", socketId);
        childMap2.put("sdp", childMap1);
        HashMap<String, Object> map = new HashMap();
        map.put("eventName", "__answer");
        map.put("data", childMap2);
        JSONObject object = new JSONObject(map);
        String jsonString = object.toString();
        mWebSocketClient.send(jsonString);
    }


    public void sendOffer(String socketId, String sdp) {
        HashMap<String, Object> childMap1 = new HashMap();
        childMap1.put("type", "offer");
        childMap1.put("sdp", sdp);

        HashMap<String, Object> childMap2 = new HashMap();
        childMap2.put("socketId", socketId);
        childMap2.put("sdp", childMap1);

        HashMap<String, Object> map = new HashMap();
        map.put("eventName", "__offer");
        map.put("data", childMap2);

        JSONObject object = new JSONObject(map);
        String jsonString = object.toString();
        mWebSocketClient.send(jsonString);

    }

    public void sendIceCandidate(String socketId, IceCandidate iceCandidate) {
        HashMap<String, Object> childMap = new HashMap();
        childMap.put("id", iceCandidate.sdpMid);
        childMap.put("label", iceCandidate.sdpMLineIndex);
        childMap.put("candidate", iceCandidate.sdp);
        childMap.put("socketId", socketId);
        HashMap<String, Object> map = new HashMap();
        map.put("eventName", "__ice_candidate");
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        String jsonString = object.toString();
        mWebSocketClient.send(jsonString);
    }
    //============================需要发送的=====================================

    //============================需要接收的=====================================

    @Override
    public void handleMessage(String message) {
        Map map = JSON.parseObject(message, Map.class);
        String eventName = (String) map.get("eventName");
        LogUtils.d(TAG, "收到信息：" + eventName);
        if (eventName.equals("_peers")) {
            handleJoinToRoom(map);
        }
        if (eventName.equals("_new_peer")) {
            handleRemoteInRoom(map);
        }
        if (eventName.equals("_ice_candidate")) {
            handleRemoteCandidate(map);
        }
        if (eventName.equals("_remove_peer")) {
            handleRemoteOutRoom(map);
        }
        if (eventName.equals("_offer")) {
            handleOffer(map);
        }

        if (eventName.equals("_answer")) {
            handleAnswer(map);
        }
    }


    // 自己进入房间
    private void handleJoinToRoom(Map map) {
        Map data = (Map) map.get("data");
        JSONArray arr = (JSONArray) data.get("connections");
        String js = JSONObject.toJSONString(arr, SerializerFeature.WriteClassName);
        ArrayList<String> connections = (ArrayList<String>) JSONObject.parseArray(js, String.class);
        String myId = (String) data.get("you");
        events.onJoinToRoom(connections, myId);
    }

    // 自己已经在房间，有人进来
    private void handleRemoteInRoom(Map map) {
        Map data = (Map) map.get("data");
        String socketId = (String) data.get("socketId");
        events.onRemoteJoinToRoom(socketId);
    }

    // 处理交换信息
    private void handleRemoteCandidate(Map map) {
        Map data = (Map) map.get("data");
        String socketId = (String) data.get("socketId");
        String sdpMid = (String) data.get("id");
        sdpMid = (null == sdpMid) ? "video" : sdpMid;
        Integer sdpMLineIndex = (Integer) data.get("label");
        String candidate = (String) data.get("candidate");
        IceCandidate iceCandidate = new IceCandidate(sdpMid, sdpMLineIndex, candidate);
        events.onRemoteIceCandidate(socketId, iceCandidate);

    }

    // 有人离开了房间
    private void handleRemoteOutRoom(Map map) {
        Map data = (Map) map.get("data");
        String socketId = (String) data.get("socketId");
        events.onRemoteOutRoom(socketId);
    }

    // 处理Offer
    private void handleOffer(Map map) {
        Map data = (Map) map.get("data");
        Map sdpDic = (Map) data.get("sdp");
        String socketId = (String) data.get("socketId");
        String sdp = (String) sdpDic.get("sdp");
        events.onReceiveOffer(socketId, sdp);
    }

    // 处理Answer
    private void handleAnswer(Map map) {
        Map data = (Map) map.get("data");
        Map sdpDic = (Map) data.get("sdp");
        String socketId = (String) data.get("socketId");
        String sdp = (String) sdpDic.get("sdp");
        events.onReceiverAnswer(socketId, sdp);
    }
    //============================需要接收的=====================================


    // 忽略证书
    class TrustManagerTest implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }


}
