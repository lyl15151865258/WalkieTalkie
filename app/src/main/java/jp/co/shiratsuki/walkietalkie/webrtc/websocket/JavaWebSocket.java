package jp.co.shiratsuki.walkietalkie.webrtc.websocket;

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
import java.util.List;
import java.util.Map;

import javax.net.ssl.X509TrustManager;

import jp.co.shiratsuki.walkietalkie.bean.Contact;
import jp.co.shiratsuki.walkietalkie.bean.websocket.ContactsList;
import jp.co.shiratsuki.walkietalkie.utils.GsonUtils;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;

/**
 * WebSocket类
 * Created at 2019/1/15 2:40
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class JavaWebSocket implements IWebSocket {

    private final static String TAG = "JavaWebSocket";

    private WebSocketClient mWebSocketClient;

    private ISignalingEvents events;

    public JavaWebSocket(ISignalingEvents events) {
        this.events = events;
    }

    public void connect(String wss, final String room, final String userIP, final String userName) {
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
                    joinRoom(room, userIP, userName);
                }

                @Override
                public void onMessage(String message) {
                    LogUtils.d("JavaWebSocket", message);
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    LogUtils.d(TAG, "关闭WebSocket连接：" + reason);
                    // WebSocket断开连接，退出房间
                    events.onWebSocketClosed();
                }

                @Override
                public void onError(Exception ex) {
                    LogUtils.d(TAG, "WebSocket连接出错：" + ex.toString());
                    ex.printStackTrace();
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
    public void joinRoom(String room, String userIP, String userName) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__join");
        Map<String, String> childMap = new HashMap<>();
        childMap.put("room", room);
        childMap.put("userIP", userIP);
        childMap.put("userName", userName);
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        final String jsonString = object.toString();
        sendMessage(jsonString);
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
        sendMessage(jsonString);
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
        sendMessage(jsonString);

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
        sendMessage(jsonString);
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
            handleRemoteInRoom(message);
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
        if (eventName.equals("_pong")) {
            handlePong(map);
        }
        if (eventName.equals("_speak_status")) {
            handleVoice(map);
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
    private void handleRemoteInRoom(String message) {
        ContactsList contactsList = GsonUtils.parseJSON(message, ContactsList.class);
        String socketId = contactsList.getData().getSocketId();
        String socketName = contactsList.getData().getSocketName();
        List<Contact> contactList = contactsList.getData().getContacts();
        for (int i = 0; i < contactList.size(); i++) {
            LogUtils.d(TAG, "联系人数量：" + contactList.size() + "," + contactList.get(i).getUserIP());
        }
        events.onRemoteJoinToRoom(socketId, socketName, contactList);
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

    // 处理服务器传回的心跳
    private void handlePong(Map map) {
        LogUtils.d(TAG, "服务器传来的心跳回复");
    }

    // 处理声音状态位
    private void handleVoice(Map map) {
        Map data = (Map) map.get("data");
        boolean someoneSpeaking = (boolean) data.get("speakStatus");
        events.onReceiveSpeakStatus(someoneSpeaking);
        LogUtils.d(TAG, "收到服务端传回来的声音状态位：" + someoneSpeaking);
    }

    // 发送消息的方法
    public void sendMessage(String message) {
        mWebSocketClient.send(message);
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
