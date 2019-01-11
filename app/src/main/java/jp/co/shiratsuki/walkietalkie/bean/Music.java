package jp.co.shiratsuki.walkietalkie.bean;

/**
 * 需要播放的音乐
 * Created at 2018-12-18 15:30
 *
 * @author LiYuliang
 * @version 1.0
 */

public class Music {

    private int listNo;

    private String filePath;

    private int playCount;

    private int alreadyPlayCount;

    public Music(int listNo, String filePath, int playCount, int alreadyPlayCount) {
        this.listNo = listNo;
        this.filePath = filePath;
        this.playCount = playCount;
        this.alreadyPlayCount = alreadyPlayCount;
    }

    public int getListNo() {
        return listNo;
    }

    public void setListNo(int listNo) {
        this.listNo = listNo;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
        if (!(o instanceof Music)) {
            return false;
        } else {
            try {
                Music that = (Music) o;
                return listNo == that.listNo;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
