package jp.co.shiratsuki.walkietalkie.bean;

import java.util.List;

public class MusicList {

    private int listNo;

    private List<Music> musicList;

    private int playCount;

    private int alreadyPlayCount;

    public MusicList(int listNo, List<Music> musicList, int playCount, int alreadyPlayCount) {
        this.listNo = listNo;
        this.musicList = musicList;
        this.playCount = playCount;
        this.alreadyPlayCount = alreadyPlayCount;
    }

    public int getListNo() {
        return listNo;
    }

    public void setListNo(int listNo) {
        this.listNo = listNo;
    }

    public List<Music> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
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
