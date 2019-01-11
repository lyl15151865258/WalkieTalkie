package jp.co.shiratsuki.walkietalkie.bean;

import java.io.Serializable;
import java.util.List;

/**
 * WebSocket传输的信息
 * Created at 2018-12-16 22:50
 *
 * @author LiYuliang
 * @version 1.0
 */

public class WebSocketData implements Serializable {

    private int BackColor;
    private List<String> FileName;
    private int ForeColor;
    private int ListNo;
    private int PlayCount;
    private String ServerAddress;
    private boolean Status;
    private String Text;
    private String Time;
    private String Type;
    private int VoiceInterval1;
    private int VoiceInterval2;

    private boolean isPalying = false;

    public int getBackColor() {
        return BackColor;
    }

    public void setBackColor(int backColor) {
        BackColor = backColor;
    }

    public List<String> getFileName() {
        return FileName;
    }

    public void setFileName(List<String> fileName) {
        FileName = fileName;
    }

    public int getForeColor() {
        return ForeColor;
    }

    public void setForeColor(int foreColor) {
        ForeColor = foreColor;
    }

    public int getListNo() {
        return ListNo;
    }

    public void setListNo(int listNo) {
        ListNo = listNo;
    }

    public int getPlayCount() {
        return PlayCount;
    }

    public void setPlayCount(int playCount) {
        PlayCount = playCount;
    }

    public String getServerAddress() {
        return ServerAddress;
    }

    public void setServerAddress(String serverAddress) {
        ServerAddress = serverAddress;
    }

    public boolean isStatus() {
        return Status;
    }

    public void setStatus(boolean status) {
        Status = status;
    }

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public int getVoiceInterval1() {
        return VoiceInterval1;
    }

    public void setVoiceInterval1(int voiceInterval1) {
        VoiceInterval1 = voiceInterval1;
    }

    public int getVoiceInterval2() {
        return VoiceInterval2;
    }

    public void setVoiceInterval2(int voiceInterval2) {
        VoiceInterval2 = voiceInterval2;
    }

    public boolean isPalying() {
        return isPalying;
    }

    public void setPalying(boolean palying) {
        isPalying = palying;
    }
}
