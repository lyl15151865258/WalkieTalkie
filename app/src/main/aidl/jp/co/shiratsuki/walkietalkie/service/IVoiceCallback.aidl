package jp.co.shiratsuki.walkietalkie.service;

interface IVoiceCallback {
    void enterRoomSuccess();
    void startRecordSuccess();
    void stopRecordSuccess();
    void leaveGroupSuccess();
    void useSpeakerSuccess();
    void useEarpieceSuccess();
    void removeUser(String ipAddress,String name);
}
