package jp.co.shiratsuki.walkietalkie.service;

interface IVoiceCallback {
    void enterRoomSuccess();
    void startRecordSuccess();
    void stopRecordSuccess();
    void leaveGroupSuccess();
    void useSpeakerSuccess();
    void useEarpieceSuccess();
    void findNewUser(String ipAddress,String name);
    void removeUser(String ipAddress,String name);
}
