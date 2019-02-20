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
import jp.co.shiratsuki.walkietalkie.webrtc.websocket.IWebSocket;
import jp.co.shiratsuki.walkietalkie.webrtc.websocket.JavaWebSocket;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
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

/**
 * WebRTC辅助类
 * Created at 2019/1/15 2:41
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class WebRTCHelper implements ISignalingEvents {

    public final static String TAG = "WebRTCHelper";

    private PeerConnectionFactory _factory;
    private MediaStream _localStream;
    private AudioTrack _localAudioTrack;
    private VideoCapturerAndroid captureAndroid;
    private VideoSource videoSource;

    private AudioManager mAudioManager;

    private ArrayList<String> _connectionIdArray;
    private Map<String, Peer> _connectionPeerDic;

    private String _myId;
    private IWebRTCHelper IHelper;

    private ArrayList<PeerConnection.IceServer> ICEServers;
    private boolean videoEnable;

    enum Role {Caller, Receiver,}

    private Role _role;

    private IWebSocket webSocket;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public WebRTCHelper(Context context, IWebRTCHelper IHelper, Parcelable[] servers) {
        this.IHelper = IHelper;
        this._connectionPeerDic = new HashMap<>();
        this._connectionIdArray = new ArrayList<>();
        this.ICEServers = new ArrayList<>();
        for (Parcelable parcelable : servers) {
            MyIceServer myIceServer = (MyIceServer) parcelable;
            PeerConnection.IceServer iceServer = new PeerConnection.IceServer(myIceServer.uri, myIceServer.username, myIceServer.password);
            ICEServers.add(iceServer);
        }
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        LogUtils.d(TAG, "初始化PeerConnection");
        PeerConnectionFactory.initializeAndroidGlobals(IHelper, true, true, true);
        _factory = new PeerConnectionFactory();
    }

    public void initSocket(String ws, boolean videoEnable) {
        this.videoEnable = videoEnable;
        webSocket = new JavaWebSocket(this);
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
        // 连接成功后，就准备发送心跳包
        mHandler.postDelayed(heartBeatRunnable, NetWork.HEART_BEAT_RATE);
    }

    @Override  // 我加入到房间
    public void onJoinToRoom(List<String> connections, String myId) {
        LogUtils.d(TAG, "自己加入到房间");
        _connectionIdArray.addAll(connections);
        _myId = myId;
        if (_localStream == null) {
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

    /**
     * 发送心跳包
     */
    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            // 心跳包发送一个SocketId过去
            if (webSocket != null) {
                try {
                    LogUtils.d(TAG, "WebSocket发送心跳包");
                    HashMap<String, Object> childMap = new HashMap<>();
                    childMap.put("userId", _myId);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("eventName", "__ping");
                    map.put("data", childMap);
                    JSONObject object = new JSONObject(map);
                    String jsonString = object.toString();
                    sendMessage(jsonString);
                } catch (Exception e) {
                    LogUtils.d(TAG, "WebSocket发送心跳包失败");
                    e.printStackTrace();
                }
            }
            mHandler.postDelayed(this, NetWork.HEART_BEAT_RATE);
        }
    };

    @Override  // 其他人加入到房间
    public void onRemoteJoinToRoom(String userId, ArrayList<User> userList) {
        LogUtils.d(TAG, "有人加入到房间：" + userId);

        IHelper.updateRoomContacts(userList);

        if (_localStream == null) {
            createLocalStream();
        }
        Peer mPeer = new Peer(userId);
        mPeer.pc.addStream(_localStream);

        _connectionIdArray.add(userId);
        _connectionPeerDic.put(userId, mPeer);
    }

    @Override
    public void onRemoteIceCandidate(String userId, IceCandidate iceCandidate) {
        Peer mPeer = _connectionPeerDic.get(userId);
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
        Peer mPeer = _connectionPeerDic.get(userId);
        SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.OFFER, sdp);
        if (mPeer != null) {
            mPeer.pc.setRemoteDescription(mPeer, sessionDescription);
        }
    }

    @Override
    public void onReceiverAnswer(String userId, String sdp) {
        Peer mPeer = _connectionPeerDic.get(userId);
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
        leaveGroup();
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
        if (_localAudioTrack != null) {
            _localAudioTrack.setEnabled(enable);
        }
    }

    public void toggleSpeaker(boolean enable) {
        if (mAudioManager != null) {
            mAudioManager.setSpeakerphoneOn(enable);
        }

    }

    // 发送消息
    private void sendMessage(String message) {
        if (webSocket != null && webSocket.socketIsOpen()) {
            webSocket.sendMessage(message);
        }
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
        final String jsonString = object.toString();
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
        ArrayList myCopy = (ArrayList) _connectionIdArray.clone();
        for (Object Id : myCopy) {
            closePeerConnection((String) Id);
        }
        if (_connectionIdArray != null) {
            _connectionIdArray.clear();
        }
        _localStream = null;

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
        ArrayList myCopy = (ArrayList) _connectionIdArray.clone();
        for (Object Id : myCopy) {
            closePeerConnection((String) Id);
        }

        if (webSocket != null) {
            webSocket.close();
        }
        if (_connectionIdArray != null) {
            _connectionIdArray.clear();
        }
        _localStream = null;

        if (IHelper != null) {
            IHelper.onLeaveGroup();
            SPHelper.save("KEY_STATUS_UP", true);
        }
    }

    // 创建本地流
    private void createLocalStream() {
        _localStream = _factory.createLocalMediaStream("ARDAMS");
        // 音频
        AudioSource audioSource = _factory.createAudioSource(new MediaConstraints());
        _localAudioTrack = _factory.createAudioTrack("ARDAMSa0", audioSource);
        _localStream.addTrack(_localAudioTrack);

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
            videoSource = _factory.createVideoSource(captureAndroid, audioConstraints);
            VideoTrack localVideoTrack = _factory.createVideoTrack("ARDAMSv0", videoSource);
            _localStream.addTrack(localVideoTrack);
        }


        if (IHelper != null) {
            IHelper.onSetLocalStream(_localStream, _myId);
        }

    }

    // 创建所有连接
    private void createPeerConnections() {
        for (Object str : _connectionIdArray) {
            Peer peer = new Peer((String) str);
            _connectionPeerDic.put((String) str, peer);
        }
    }

    // 为所有连接添加流
    private void addStreams() {
        LogUtils.d(TAG, "为所有连接添加流");
        for (Map.Entry<String, Peer> entry : _connectionPeerDic.entrySet()) {
            if (_localStream == null) {
                createLocalStream();
            }
            entry.getValue().pc.addStream(_localStream);
        }

    }

    // 为所有连接创建offer
    private void createOffers() {
        LogUtils.d(TAG, "为所有连接创建offer");

        for (Map.Entry<String, Peer> entry : _connectionPeerDic.entrySet()) {
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
        Peer mPeer = _connectionPeerDic.get(connectionId);
        if (mPeer != null) {
            mPeer.pc.close();
        }
        _connectionPeerDic.remove(connectionId);
        _connectionIdArray.remove(connectionId);

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
            if (_factory == null) {
                PeerConnectionFactory.initializeAndroidGlobals(IHelper, true, true, true);
                _factory = new PeerConnectionFactory();
            }
            // 管道连接抽象类实现方法
            return _factory.createPeerConnection(ICEServers, peerConnectionConstraints(), this);
        }

    }

}



