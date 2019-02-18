package jp.co.shiratsuki.walkietalkie.service;

interface IVoiceCallback {
    void enterRoomSuccess();
    void leaveRoomSuccess();
    void leaveGroupSuccess();
    void startRecordSuccess();
    void stopRecordSuccess();
    void useSpeakerSuccess();
    void useEarpieceSuccess();
    void removeUser(String ipAddress,String name);
}
