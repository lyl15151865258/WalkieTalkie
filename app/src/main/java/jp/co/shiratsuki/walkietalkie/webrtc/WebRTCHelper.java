package jp.co.shiratsuki.walkietalkie.webrtc;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;

import com.alibaba.fastjson.JSONObject;

import jp.co.shiratsuki.walkietalkie.bean.User;
import jp.co.shiratsuki.walkietalkie.constant.NetWork;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.GsonUtils;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.webrtc.websocket.ISignalingEvents;
import jp.co.shiratsuki.walkietalkie.webrtc.websocket.JavaWebSocket;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnection.IceServer;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * WebRTC辅助类
 * Created at 2019/1/15 2:41
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class WebRTCHelper implements ISignalingEvents {

    public final static String TAG = "WebRTCHelper";

    private Context mContext;
    private PeerConnectionFactory peerConnectionFactory;
    private MediaStream localStream;
    private AudioTrack mAudioTrack;
    private VideoCapturerAndroid captureAndroid;
    private VideoSource videoSource;

    private AudioManager mAudioManager;

    private ArrayList<String> connectionIdList;
    private Map<String, Peer> connectionPeerList;

    private String _myId;
    private IWebRTCHelper IHelper;

    private ArrayList<IceServer> ICEServers;
    private boolean videoEnable;

    enum Role {Caller, Receiver,}

    private Role _role;

    private JavaWebSocket webSocket;

    private ExecutorService threadPool;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean flag = true;

    public WebRTCHelper(Context context, IWebRTCHelper IHelper, Parcelable[] servers) {
        this.IHelper = IHelper;
        this.connectionPeerList = new HashMap<>();
        this.connectionIdList = new ArrayList<>();
        this.ICEServers = new ArrayList<>();
        for (Parcelable parcelable : servers) {
            MyIceServer myIceServer = (MyIceServer) parcelable;
            IceServer iceServer = new IceServer(myIceServer.uri, myIceServer.username, myIceServer.password);
            ICEServers.add(iceServer);
        }
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        LogUtils.d(TAG, "初始化PeerConnection");
        PeerConnectionFactory.initializeAndroidGlobals(IHelper, true, true, true);
        peerConnectionFactory = new PeerConnectionFactory();

        threadPool = new ThreadPoolExecutor(5, 10, 60, TimeUnit.SECONDS,
                new SynchronousQueue<>(), (r) -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
    }

    public void initSocket(String ws, boolean videoEnable) {
        this.videoEnable = videoEnable;
        webSocket = new JavaWebSocket(mContext, this);
        webSocket.connect(ws);
    }

    public boolean socketIsOpen() {
        if (webSocket == null) {
            return false;
        }
        return webSocket.socketIsOpen();
    }

    public void joinRoom(String roomId) {
        webSocket.joinRoom(roomId);
    }


    // ===================================webSocket回调信息=======================================

    @Override
    public void onUserInOrOut(ArrayList<User> userList) {
        IHelper.updateContacts(userList);
    }

    @Override
    public void onWebSocketConnected() {

    }

    @Override  // 我加入到房间
    public void onJoinToRoom(List<String> connections, String myId) {
        LogUtils.d(TAG, "自己加入到房间");
        connectionIdList.addAll(connections);
        _myId = myId;
        if (localStream == null) {
            LogUtils.d(TAG, "创建本地流");
            createLocalStream();
        }
        createPeerConnections();
        addStreams();
        createOffers();
        LogUtils.d(TAG, "创建Offers");
        if (IHelper != null) {
            IHelper.onEnterRoom();
        }
    }

    @Override  // 其他人加入到房间
    public void onRemoteJoinToRoom(String userId, ArrayList<User> userList) {
        LogUtils.d(TAG, "有人加入到房间：" + userId);

        IHelper.updateRoomContacts(userList);

        if (localStream == null) {
            createLocalStream();
        }

        Peer mPeer = new Peer(userId);
        mPeer.pc.addStream(localStream);

        if (!connectionIdList.contains(userId)) {
            connectionIdList.add(userId);
        }
        connectionPeerList.remove(userId);
        connectionPeerList.put(userId, mPeer);
    }

    @Override
    public void onRemoteIceCandidate(String userId, IceCandidate iceCandidate) {
        Peer mPeer = connectionPeerList.get(userId);
        if (mPeer != null) {
            mPeer.pc.addIceCandidate(iceCandidate);
        }
    }

    @Override
    public void onRemoteOutRoom(String userId) {
        LogUtils.d(TAG, "有人离开房间，ID为：" + userId);
        closePeerConnection(userId);
    }

    @Override
    public void onReceiveOffer(String userId, String sdp) {
        _role = Role.Receiver;
        Peer mPeer = connectionPeerList.get(userId);
        SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.OFFER, sdp);
        if (mPeer != null) {
            mPeer.pc.setRemoteDescription(mPeer, sessionDescription);
        }
    }

    @Override
    public void onReceiverAnswer(String userId, String sdp) {
        Peer mPeer = connectionPeerList.get(userId);
        SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
        if (mPeer != null) {
            mPeer.pc.setRemoteDescription(mPeer, sessionDescription);
        }
    }

    @Override
    public void onReceiveSpeakStatus(ArrayList<User> userList) {
        IHelper.updateRoomSpeakStatus(userList);
    }

    @Override
    public void onReceiveSomeoneLeave(String userId, ArrayList<User> userList) {
        IHelper.updateRoomContacts(userList);
        closePeerConnection(userId);
    }

    @Override
    public void onWebSocketClosed() {
        websocketDisconnect();
        reConnect();
    }


    //**************************************逻辑控制**************************************
    // 调整摄像头前置后置
    public void switchCamera() {

        captureAndroid.switchCamera(new VideoCapturerAndroid.CameraSwitchHandler() {
            @Override
            public void onCameraSwitchDone(boolean b) {
                LogUtils.d(TAG, "切换摄像头");
            }

            @Override
            public void onCameraSwitchError(String s) {
                LogUtils.d(TAG, "切换摄像头失败");
            }
        });

    }

    // 设置自己静音
    public void toggleMute(boolean enable) {
        if (mAudioTrack != null) {
            mAudioTrack.setEnabled(enable);
        }
    }

    public void toggleSpeaker(boolean enable) {
        if (mAudioManager != null) {
            mAudioManager.setSpeakerphoneOn(enable);
        }

    }

    // 发送消息
    private void sendMessage(String message) {
        LogUtils.d(TAG, "WebRTC————————————————发送消息");
        if (webSocket != null && webSocket.socketIsOpen()) {
            webSocket.sendMessage(message);
        }
    }

    /**
     * 重连WebSocket
     */
    private void reConnect() {
        // 如果程序退出了就不要重连了
        Runnable runnable = () -> {
            try {
                Thread.sleep(NetWork.WEBSOCKET_RECONNECT_RATE);
                if (flag) {
                    LogUtils.d(TAG, "WebRTC————————————————WebSocket重连");
                    webSocket.reconnectBlocking();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        mHandler.post(runnable);
    }

    // 给服务器发送当前是否在讲话的标记
    public void sendSpeakStatus(boolean isSpeaking) {
        User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__speakStatus");
        user.setInroom(true);
        user.setSpeaking(isSpeaking);
        map.put("data", user);

        JSONObject object = new JSONObject(map);
        String jsonString = object.toString();
        sendMessage(jsonString);
    }

    /**
     * 给别人打电话
     *
     * @param userId 目标的UserId
     */
    public void callOthers(String userId) {
        User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__p2p_request");
        map.put("data", user);
        map.put("destinationId", userId);
        JSONObject object = new JSONObject(map);
        String jsonString = object.toString();
        sendMessage(jsonString);
    }

    /**
     * 取消给别人打电话
     *
     * @param userId 目标的UserId
     */
    public void cancelP2PCall(String userId) {
        User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__p2p_request_cancel");
        map.put("data", user);
        map.put("destinationId", userId);
        JSONObject object = new JSONObject(map);
        String jsonString = object.toString();
        sendMessage(jsonString);
    }

    /**
     * 拒接别人的音频邀请
     *
     * @param userId 目标的UserId
     */
    public void rejectP2PCall(String userId) {
        User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__p2p_request_reject");
        map.put("data", user);
        map.put("destinationId", userId);
        JSONObject object = new JSONObject(map);
        String jsonString = object.toString();
        sendMessage(jsonString);
    }

    /**
     * 接受别人的音频邀请
     *
     * @param userId 目标的UserId
     */
    public void acceptP2PCall(String userId) {
        User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__p2p_request_accept");
        map.put("data", user);
        map.put("destinationId", userId);
        JSONObject object = new JSONObject(map);
        String jsonString = object.toString();
        sendMessage(jsonString);
    }

    /**
     * 音频邀请等待超时
     *
     * @param userId 目标的UserId
     */
    public void timeOut(String userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventName", "__p2p_time_out");
        map.put("userId", userId);
        JSONObject object = new JSONObject(map);
        String jsonString = object.toString();
        sendMessage(jsonString);
    }

    /**
     * 退出房间
     */
    public void exitRoom() {
        User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("eventName", "__leave_room");
        map.put("data", user);
        sendMessage(GsonUtils.convertJSON(map));

        if (videoSource != null) {
            videoSource.stop();
        }
        ArrayList myCopy = (ArrayList) connectionIdList.clone();
        for (Object Id : myCopy) {
            closePeerConnection((String) Id);
        }
        if (connectionIdList != null) {
            connectionIdList.clear();
        }
        localStream = null;

        if (IHelper != null) {
            IHelper.onLeaveRoom();
            SPHelper.save("KEY_STATUS_UP", true);
        }
    }

    public void leaveGroup() {
        User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("eventName", "__leave_room");
        map.put("data", user);
        sendMessage(GsonUtils.convertJSON(map));

        if (videoSource != null) {
            videoSource.stop();
        }
        ArrayList myCopy = (ArrayList) connectionIdList.clone();
        for (Object Id : myCopy) {
            closePeerConnection((String) Id);
        }

//        if (webSocket != null) {
//            webSocket.close();
//        }
        if (connectionIdList != null) {
            connectionIdList.clear();
        }
        localStream = null;

        if (IHelper != null) {
            IHelper.onLeaveGroup();
            SPHelper.save("KEY_STATUS_UP", true);
        }
    }

    private void websocketDisconnect() {
        if (videoSource != null) {
            videoSource.stop();
        }
        ArrayList myCopy = (ArrayList) connectionIdList.clone();
        for (Object Id : myCopy) {
            closePeerConnection((String) Id);
        }
        if (connectionIdList != null) {
            connectionIdList.clear();
        }
        localStream = null;
        if (IHelper != null) {
            IHelper.onLeaveGroup();
            SPHelper.save("KEY_STATUS_UP", true);
        }
    }

    public void closeWebSocket() {
        webSocket.close();
        threadPool.shutdown();
    }

    // 创建本地流
    private void createLocalStream() {
        localStream = peerConnectionFactory.createLocalMediaStream("ARDAMS");
        // 音频
        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        mAudioTrack = peerConnectionFactory.createAudioTrack("ARDAMSa0", audioSource);
        localStream.addTrack(mAudioTrack);

        if (videoEnable) {
            String frontFacingDevice = CameraEnumerationAndroid.getNameOfFrontFacingDevice();
            //创建需要传入设备的名称
            captureAndroid = VideoCapturerAndroid.create(frontFacingDevice, new VideoCapturerAndroid.CameraEventsHandler() {
                @Override
                public void onCameraError(String s) {

                }

                @Override
                public void onCameraFreezed(String s) {

                }

                @Override
                public void onCameraOpening(int i) {

                }

                @Override
                public void onFirstFrameAvailable() {

                }

                @Override
                public void onCameraClosed() {

                }
            });
            // 视频
            MediaConstraints audioConstraints = localVideoConstraints();
            videoSource = peerConnectionFactory.createVideoSource(captureAndroid, audioConstraints);
            VideoTrack localVideoTrack = peerConnectionFactory.createVideoTrack("ARDAMSv0", videoSource);
            localStream.addTrack(localVideoTrack);
        }

        if (IHelper != null) {
            IHelper.onSetLocalStream(localStream, _myId);
        }

    }

    // 创建所有连接
    private void createPeerConnections() {
        for (Object str : connectionIdList) {
            Peer peer = new Peer((String) str);
            connectionPeerList.put((String) str, peer);
        }
    }

    // 为所有连接添加流
    private void addStreams() {
        LogUtils.d(TAG, "为所有连接添加流");
        for (Map.Entry<String, Peer> entry : connectionPeerList.entrySet()) {
            if (localStream == null) {
                createLocalStream();
            }
            entry.getValue().pc.addStream(localStream);
        }

    }

    // 为所有连接创建offer
    private void createOffers() {
        LogUtils.d(TAG, "为所有连接创建offer");

        for (Map.Entry<String, Peer> entry : connectionPeerList.entrySet()) {
            _role = Role.Caller;
            Peer mPeer = entry.getValue();
            mPeer.pc.createOffer(mPeer, offerOrAnswerConstraint());
        }

    }

    // 关闭通道流
    private void closePeerConnection(String connectionId) {
        if (IHelper != null) {
            IHelper.removeUser(connectionId);
        }
        LogUtils.d(TAG, "关闭" + connectionId + "通道流");
        Peer mPeer = connectionPeerList.get(connectionId);
        if (mPeer != null) {
            mPeer.pc.close();
        }
        connectionPeerList.remove(connectionId);
        connectionIdList.remove(connectionId);

        IHelper.onCloseWithId(connectionId);
    }

    //**************************************各种约束******************************************/
    private MediaConstraints localVideoConstraints() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        ArrayList<MediaConstraints.KeyValuePair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new MediaConstraints.KeyValuePair("maxWidth", "360"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("minWidth", "160"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("maxHeight", "640"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("minHeight", "120"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("minFrameRate", "1"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("maxFrameRate", "5"));
        mediaConstraints.mandatory.addAll(keyValuePairs);
        return mediaConstraints;
    }

    private MediaConstraints peerConnectionConstraints() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        ArrayList<MediaConstraints.KeyValuePair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("minFrameRate", "1"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("maxFrameRate", "5"));

        mediaConstraints.optional.addAll(keyValuePairs);
        return mediaConstraints;
    }

    private MediaConstraints offerOrAnswerConstraint() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        ArrayList<MediaConstraints.KeyValuePair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        mediaConstraints.mandatory.addAll(keyValuePairs);
        return mediaConstraints;
    }

    //**************************************内部类******************************************/
    private class Peer implements SdpObserver, PeerConnection.Observer {
        private PeerConnection pc;
        private String userId;

        private Peer(String userId) {
            this.pc = createPeerConnection();
            this.userId = userId;
        }

        //****************************PeerConnection.Observer****************************/
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            LogUtils.d(TAG, "ice 状态改变 " + signalingState);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        }


        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            // 发送IceCandidate
            webSocket.sendIceCandidate(userId, iceCandidate);
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            if (IHelper != null) {
                IHelper.onAddRemoteStream(mediaStream, userId);
            }
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            if (IHelper != null) {
                IHelper.onCloseWithId(userId);
            }
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {

        }

        @Override
        public void onRenegotiationNeeded() {

        }

        //****************************SdpObserver****************************/

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            LogUtils.d(TAG, "sdp创建成功       " + sessionDescription.type);
            //设置本地的SDP
            pc.setLocalDescription(Peer.this, sessionDescription);
        }

        @Override
        public void onSetSuccess() {
            LogUtils.d(TAG, "sdp连接成功        " + pc.signalingState().toString());

            if (pc.signalingState() == PeerConnection.SignalingState.HAVE_REMOTE_OFFER) {
                pc.createAnswer(Peer.this, offerOrAnswerConstraint());
            } else if (pc.signalingState() == PeerConnection.SignalingState.HAVE_LOCAL_OFFER) {
                //判断连接状态为本地发送offer
                if (_role == Role.Receiver) {
                    //接收者，发送Answer
                    webSocket.sendAnswer(userId, pc.getLocalDescription().description);

                } else if (_role == Role.Caller) {
                    //发送者,发送自己的offer
                    webSocket.sendOffer(userId, pc.getLocalDescription().description);
                }

            } else if (pc.signalingState() == PeerConnection.SignalingState.STABLE) {
                // Stable 稳定的
                if (_role == Role.Receiver) {
                    webSocket.sendAnswer(userId, pc.getLocalDescription().description);
                }
            }
        }

        @Override
        public void onCreateFailure(String s) {

        }

        @Override
        public void onSetFailure(String s) {

        }

        //初始化 RTCPeerConnection 连接管道
        private PeerConnection createPeerConnection() {
            if (peerConnectionFactory == null) {
                PeerConnectionFactory.initializeAndroidGlobals(IHelper, true, true, true);
                peerConnectionFactory = new PeerConnectionFactory();
            }
            // 管道连接抽象类实现方法
            return peerConnectionFactory.createPeerConnection(ICEServers, peerConnectionConstraints(), this);
        }

    }

    public void release() {
        leaveGroup();
        closeWebSocket();
        flag = false;
    }

}



