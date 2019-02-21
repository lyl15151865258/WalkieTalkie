package jp.co.shiratsuki.walkietalkie.service;

import jp.co.shiratsuki.walkietalkie.service.IVoiceCallback;

interface IVoiceService {
    void enterRoom(String roomId);
    void leaveRoom();
    void leaveGroup();
    void startRecord();
    void stopRecord();
    void useSpeaker();
    void useEarpiece();
    void callOthers(String userId);
    void cancelP2PCall(String userId);
    void rejectP2PCall(String userId);
    void acceptP2PCall(String userId);
    void registerCallback(IVoiceCallback callback);
    void unRegisterCallback(IVoiceCallback callback);
}
