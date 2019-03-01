package jp.co.shiratsuki.walkietalkie.bean.version;

import java.util.List;

/**
 * 软件历史版本更新日志
 * Created by LiYuliang on 2017/11/8.
 *
 * @author LiYuliang
 * @version 2017/11/08
 */

public class VersionLog {

    private int versionCode;

    private List<Version> version;

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersion(List<Version> version) {
        this.version = version;
    }

    public List<Version> getVersion() {
        return version;
    }
}
