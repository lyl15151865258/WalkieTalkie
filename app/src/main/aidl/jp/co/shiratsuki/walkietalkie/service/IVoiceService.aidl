package jp.co.shiratsuki.walkietalkie.service;

import jp.co.shiratsuki.walkietalkie.service.IVoiceCallback;

interface IVoiceService {

    void startRecord();
    void stopRecord();
    void leaveGroup();
    void registerCallback(IVoiceCallback callback);
    void unRegisterCallback(IVoiceCallback callback);
}
