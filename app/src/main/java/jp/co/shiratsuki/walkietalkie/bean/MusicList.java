package jp.co.shiratsuki.walkietalkie.bean;

import java.util.List;

public class MusicList {

    private int listNo;

    private int Priority;

    private List<Music> musicList;

    private String JapaneseFolder;

    private String ChineseFolder;

    private String EnglishFolder;

    private int playCount;

    private int alreadyPlayCount;

    public MusicList(int listNo,int Priority, List<Music> musicList, String JapaneseFolder, String ChineseFolder, String EnglishFolder,
                     int playCount, int alreadyPlayCount) {
        this.listNo = listNo;
        this.Priority = Priority;
        this.musicList = musicList;
        this.JapaneseFolder = JapaneseFolder;
        this.ChineseFolder = ChineseFolder;
        this.EnglishFolder = EnglishFolder;
        this.playCount = playCount;
        this.alreadyPlayCount = alreadyPlayCount;
    }

    public int getListNo() {
        return listNo;
    }

    public void setListNo(int listNo) {
        this.listNo = listNo;
    }

    public int getPriority() {
        return Priority;
    }

    public void setPriority(int priority) {
        Priority = priority;
    }

    public List<Music> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
    }

    public String getJapaneseFolder() {
        return JapaneseFolder;
    }

    public void setJapaneseFolder(String japaneseFolder) {
        JapaneseFolder = japaneseFolder;
    }

    public String getChineseFolder() {
        return ChineseFolder;
    }

    public void setChineseFolder(String chineseFolder) {
        ChineseFolder = chineseFolder;
    }

    public String getEnglishFolder() {
        return EnglishFolder;
    }

    public void setEnglishFolder(String englishFolder) {
        EnglishFolder = englishFolder;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public int getAlreadyPlayCount() {
        return alreadyPlayCount;
    }

    public void setAlreadyPlayCount(int alreadyPlayCount) {
        this.alreadyPlayCount = alreadyPlayCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MusicList)) {
            return false;
        } else {
            try {
                MusicList that = (MusicList) o;
                return listNo == that.listNo;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
