package jp.co.shiratsuki.walkietalkie.bean;

import android.support.annotation.Nullable;

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
    private int Priority;
    private String ServerAddress;
    private boolean Status;
    private String Text_Japanese;
    private String Text_Chinese;
    private String Text_English;
    private String Time;
    private String Type;
    private int VoiceInterval1;
    private int VoiceInterval2;

    private String Japanese;
    private String Chinese;
    private String English;
    private String Melody;

    private boolean isPlaying = false;
    private boolean finishPlay = false;

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

    public int getPriority() {
        return Priority;
    }

    public void setPriority(int priority) {
        Priority = priority;
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

    public String getText_Japanese() {
        return Text_Japanese;
    }

    public void setText_Japanese(String text_Japanese) {
        Text_Japanese = text_Japanese;
    }

    public String getText_Chinese() {
        return Text_Chinese;
    }

    public void setText_Chinese(String text_Chinese) {
        Text_Chinese = text_Chinese;
    }

    public String getText_English() {
        return Text_English;
    }

    public void setText_English(String text_English) {
        Text_English = text_English;
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


    public String getJapanese() {
        return Japanese;
    }

    public void setJapanese(String japanese) {
        Japanese = japanese;
    }

    public String getChinese() {
        return Chinese;
    }

    public void setChinese(String chinese) {
        Chinese = chinese;
    }

    public String getEnglish() {
        return English;
    }

    public void setEnglish(String english) {
        English = english;
    }

    public String getMelody() {
        return Melody;
    }

    public void setMelody(String melody) {
        Melody = melody;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isFinishPlay() {
        return finishPlay;
    }

    public void setFinishPlay(boolean finishPlay) {
        this.finishPlay = finishPlay;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WebSocketData)) {
            return false;
        } else {
            try {
                WebSocketData that = (WebSocketData) obj;
                return ListNo == that.ListNo;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
