package jp.co.shiratsuki.walkietalkie.service;

interface IVoiceCallback {

    void findNewUser(String ipAddress,String name,String speakStatus);
    void removeUser(String ipAddress,String name,String speakStatus);
}
