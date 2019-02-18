package jp.co.shiratsuki.walkietalkie.service;

import jp.co.shiratsuki.walkietalkie.service.IVoiceCallback;

interface IVoiceService {
    void enterRoom();
    void leaveRoom();
    void leaveGroup();
    void startRecord();
    void stopRecord();
    void useSpeaker();
    void useEarpiece();
    void registerCallback(IVoiceCallback callback);
    void unRegisterCallback(IVoiceCallback callback);
}
