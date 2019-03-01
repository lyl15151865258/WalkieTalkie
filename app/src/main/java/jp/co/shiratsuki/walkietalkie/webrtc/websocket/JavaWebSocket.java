package jp.co.shiratsuki.walkietalkie.webrtc.websocket;

import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.webrtc.IceCandidate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import jp.co.shiratsuki.walkietalkie.activity.appmain.P2PRingingActivity;
import jp.co.shiratsuki.walkietalkie.activity.appmain.P2PWaitingActivity;
import jp.co.shiratsuki.walkietalkie.bean.User;
import jp.co.shiratsuki.walkietalkie.bean.websocket.ContactsList;
import jp.co.shiratsuki.walkietalkie.bean.websocket.P2PAccept;
import jp.co.shiratsuki.walkietalkie.bean.websocket.P2PRequest;
import jp.co.shiratsuki.walkietalkie.bean.websocket.P2PResult;
import jp.co.shiratsuki.walkietalkie.bean.websocket.Peers;
import jp.co.shiratsuki.walkietalkie.bean.websocket.UserInOrOut;
import jp.co.shiratsuki.walkietalkie.constant.Constants;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.GsonUtils;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;

/**
 * WebSocket类
 * Created at 2019/1/15 2:40
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class JavaWebSocket {

    private final static String TAG = "JavaWebSocket";

    private Context mContext;
    private WebSocketClient mWebSocketClient;

    private ISignalingEvents events;

    public JavaWebSocket(Context mContext, ISignalingEvents events) {
        this.mContext = mContext;
        this.events = events;
    }

    public void connect(String wss) {
        LogUtils.d(TAG, "WebRTC服务器地址：" + wss);
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
                    events.onWebSocketConnected();
                }

                @Override
                public void onMessage(String message) {
                    LogUtils.d("JavaWebSocket", message);
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    LogUtils.d(TAG, "关闭WebSocket连接：" + reason + "，是远端关闭的吗：" + remote);
                    // 通知其他页面退出（音频邀请和等待接听页面）
                    Intent intent = new Intent();
                    intent.setAction("VOICE_WEBSOCKET_DISCONNECT");
                    mContext.sendBroadcast(intent);
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
//        setSSL();
        mWebSocketClient.connect();
    }

    public void close() {
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
        }
    }

    public boolean socketIsOpen() {
        return mWebSocketClient.isOpen();
    }

    //============================需要发送的=====================================

    public void joinRoom(String room) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__join");
        User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        user.setChatRoomId(room);
        user.setInroom(false);
        user.setSpeaking(false);
        SPHelper.save("User", GsonUtils.convertJSON(user));
        map.put("data", user);
        sendMessage(GsonUtils.convertJSON(map));

        SPHelper.save("TemporaryRoom", room);
        // 标记为用户非正常退出房间
        SPHelper.save("NormalExit", false);
    }

    public void sendAnswer(String userId, String sdp) {
        Map<String, Object> childMap1 = new HashMap<>();
        childMap1.put("type", "answer");
        childMap1.put("sdp", sdp);
        HashMap<String, Object> childMap2 = new HashMap<>();
        childMap2.put("userId", userId);
        childMap2.put("sdp", childMap1);
        HashMap<String, Object> map = new HashMap<>();
        map.put("eventName", "__answer");
        map.put("data", childMap2);
        sendMessage(GsonUtils.convertJSON(map));
    }

    public void sendOffer(String userId, String sdp) {
        HashMap<String, Object> childMap1 = new HashMap<>();
        childMap1.put("type", "offer");
        childMap1.put("sdp", sdp);

        HashMap<String, Object> childMap2 = new HashMap<>();
        childMap2.put("userId", userId);
        childMap2.put("sdp", childMap1);

        HashMap<String, Object> map = new HashMap<>();
        map.put("eventName", "__offer");
        map.put("data", childMap2);

        sendMessage(GsonUtils.convertJSON(map));

    }

    public void sendIceCandidate(String userId, IceCandidate iceCandidate) {
        HashMap<String, Object> childMap = new HashMap<>();
        childMap.put("id", iceCandidate.sdpMid);
        childMap.put("label", iceCandidate.sdpMLineIndex);
        childMap.put("candidate", iceCandidate.sdp);
        childMap.put("userId", userId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("eventName", "__ice_candidate");
        map.put("data", childMap);
        sendMessage(GsonUtils.convertJSON(map));
    }

    //============================需要发送的=====================================

    //============================需要接收的=====================================

    private void handleMessage(String message) {
        Map map = JSON.parseObject(message, Map.class);
        String eventName = (String) map.get("eventName");
        LogUtils.d(TAG, "收到信息：" + message);
        if (eventName != null) {
            switch (eventName) {
                case "_new_user":
                    handleNewUser(message);
                    break;
                case "_peers":
                    handleJoinToRoom(message);
                    break;
                case "_new_peer":
                    handleRemoteInRoom(message);
                    break;
                case "_ice_candidate":
                    handleRemoteCandidate(map);
                    break;
                case "_remove_peer":
                    handleRemoteOutRoom(map);
                    break;
                case "_offer":
                    handleOffer(map);
                    break;
                case "_answer":
                    handleAnswer(map);
                    break;
                case "_pong":
                    handlePong();
                    break;
                case "_speak_status":
                    handleVoice(message);
                    break;
                case "_p2p_request":
                    handleP2PVoiceRequest(message);
                    break;
                case "_p2p_request_cancel":
                    handleP2PVoiceRequestCancel(message);
                    break;
                case "_p2p_result":
                    handleP2PVoiceResult(message);
                    break;
                case "_p2p_request_reject":
                    handleP2PVoiceReject(message);
                    break;
                case "_p2p_request_accept":
                    handleP2PVoiceAccept(message);
                    break;
                case "_someone_leave":
                    handleLeaveRoom(message);
                    break;
                default:
                    break;
            }
        }
    }

    // 新用户连接到服务器
    private void handleNewUser(String message) {
        UserInOrOut userInOrOut = GsonUtils.parseJSON(message, UserInOrOut.class);
        ArrayList<User> userList = userInOrOut.getData().getContacts();
        events.onUserInOrOut(userList);
    }

    // 自己进入房间
    private void handleJoinToRoom(String message) {
        User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        user.setInroom(true);
        user.setSpeaking(false);
        SPHelper.save("User", GsonUtils.convertJSON(user));
        Peers peers = GsonUtils.parseJSON(message, Peers.class);
        String myId = peers.getData().getYou();
        List<String> connections = peers.getData().getConnections();
        events.onJoinToRoom(connections, myId);
    }

    // 自己已经在房间，有人进来
    private void handleRemoteInRoom(String message) {
        ContactsList contactsList = GsonUtils.parseJSON(message, ContactsList.class);
        String userId = contactsList.getData().getUserId();
        ArrayList<User> userList = contactsList.getData().getContacts();
        for (int i = 0; i < userList.size(); i++) {
            LogUtils.d(TAG, "联系人数量：" + userList.size() + "," + userList.get(i).getUser_id());
        }
        events.onRemoteJoinToRoom(userId, userList);
    }

    // 处理交换信息
    private void handleRemoteCandidate(Map map) {
        Map data = (Map) map.get("data");
        String userId = (String) data.get("userId");
        String sdpMid = (String) data.get("id");
        sdpMid = (null == sdpMid) ? "video" : sdpMid;
        Integer sdpMLineIndex = (Integer) data.get("label");
        String candidate = (String) data.get("candidate");
        IceCandidate iceCandidate = new IceCandidate(sdpMid, sdpMLineIndex, candidate);
        events.onRemoteIceCandidate(userId, iceCandidate);
    }

    // 有人离开了房间
    private void handleRemoteOutRoom(Map map) {
        LogUtils.d(TAG, "有人离开房间");
        Map data = (Map) map.get("data");
        String userId = (String) data.get("userId");
        events.onRemoteOutRoom(userId);
    }

    // 处理Offer
    private void handleOffer(Map map) {
        Map data = (Map) map.get("data");
        Map sdpDic = (Map) data.get("sdp");
        String userId = (String) data.get("userId");
        String sdp = (String) sdpDic.get("sdp");
        events.onReceiveOffer(userId, sdp);
    }

    // 处理Answer
    private void handleAnswer(Map map) {
        Map data = (Map) map.get("data");
        Map sdpDic = (Map) data.get("sdp");
        String userId = (String) data.get("userId");
        String sdp = (String) sdpDic.get("sdp");
        events.onReceiverAnswer(userId, sdp);
    }

    // 处理服务器传回的心跳
    private void handlePong() {
        LogUtils.d(TAG, "回复服务器的心跳");
        User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        HashMap<String, Object> childMap = new HashMap<>();
        childMap.put("userId", user.getUser_id());
        HashMap<String, Object> map = new HashMap<>();
        map.put("eventName", "__ping");
        map.put("data", childMap);
        JSONObject object = new JSONObject(map);
        String jsonString = object.toString();
        sendMessage(jsonString);
    }

    // 处理声音状态位
    private void handleVoice(String message) {
        ContactsList contactsList = GsonUtils.parseJSON(message, ContactsList.class);
        ArrayList<User> userList = contactsList.getData().getContacts();
        events.onReceiveSpeakStatus(userList);
    }

    // 收到一对一通话请求
    private void handleP2PVoiceRequest(String message) {
        P2PRequest p2PRequest = GsonUtils.parseJSON(message, P2PRequest.class);
        User user = p2PRequest.getData().getUser();
        Intent intent = new Intent(mContext, P2PRingingActivity.class);
        intent.putExtra("user", user);
        mContext.startActivity(intent);
    }

    // 收到一对一通话取消请求
    private void handleP2PVoiceRequestCancel(String message) {
        P2PRequest p2PRequest = GsonUtils.parseJSON(message, P2PRequest.class);
        User user = p2PRequest.getData().getUser();
        Intent intent = new Intent();
        intent.setAction("P2P_VOICE_REQUEST_CANCEL");
        mContext.sendBroadcast(intent);
    }

    // 收到一对一通话拒绝请求
    private void handleP2PVoiceReject(String message) {
        P2PRequest p2PRequest = GsonUtils.parseJSON(message, P2PRequest.class);
        User user = p2PRequest.getData().getUser();
        Intent intent = new Intent();
        intent.setAction("P2P_VOICE_REQUEST_REJECT");
        mContext.sendBroadcast(intent);
    }

    // 收到一对一通话接受请求
    private void handleP2PVoiceAccept(String message) {
        P2PAccept p2PAccept = GsonUtils.parseJSON(message, P2PAccept.class);
        String roomId = p2PAccept.getData().getRoomId();
        events.startP2PChat(roomId);
    }

    // 收到一对一通话请求结果
    private void handleP2PVoiceResult(String message) {
        P2PResult p2PResult = GsonUtils.parseJSON(message, P2PResult.class);
        String result = p2PResult.getData().getResult();
        String msg = p2PResult.getData().getMessage();
        User user = p2PResult.getData().getUser();
        switch (result) {
            case Constants.SUCCESS:
                Intent intent = new Intent(mContext, P2PWaitingActivity.class);
                intent.putExtra("user", user);
                mContext.startActivity(intent);
                break;
            case Constants.FAIL:
                Intent intent1 = new Intent();
                intent1.setAction("P2P_VOICE_REQUEST_ERROR");
                intent1.putExtra("errorMsg", msg);
                mContext.sendBroadcast(intent1);
                break;
            default:
                break;
        }
    }

    // 处理有人离开房间
    private void handleLeaveRoom(String message) {
        ContactsList contactsList = GsonUtils.parseJSON(message, ContactsList.class);
        ArrayList<User> userList = contactsList.getData().getContacts();
        String userId = contactsList.getData().getUserId();
        events.onReceiveSomeoneLeave(userId, userList);
    }

    // 发送消息的方法
    public void sendMessage(String message) {
        LogUtils.d(TAG, "用户往服务端发送数据" + message);
        if (mWebSocketClient.isOpen()) {
            mWebSocketClient.send(message);
        } else {
            // WebSocket断开连接，退出房间
            events.onWebSocketClosed();
        }
    }

    public void reconnectBlocking() {
        try {
            mWebSocketClient.reconnectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //============================需要接收的=====================================

    // 设置SSL
    private void setSSL() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            if (sslContext != null) {
                sslContext.init(null, new TrustManager[]{new TrustManagerTest()}, new SecureRandom());
            }

            SSLSocketFactory factory = null;
            if (sslContext != null) {
                factory = sslContext.getSocketFactory();
            }

            if (factory != null) {
                mWebSocketClient.setSocket(factory.createSocket());
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
