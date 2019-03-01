package jp.co.shiratsuki.walkietalkie.bean.version;

/**
 * 软件版本
 * Created by LiYuliang on 2017/12/4.
 *
 * @author LiYuliang
 * @version 2017/12/04
 */

public class Version {

    private String createTime;
    /**
     * 软件ID（后期可能有更多软件）
     */
    private int apkTypeId;
    /**
     * 新版本文件MD5值
     */
    private String md5Value;
    /**
     * 新版本号
     */
    private int versionCode;

    private int versionCount;

    /**
     * 版本类型（beta:预览版；stable:正式版）
     */
    private String versionType;

    /**
     * 新版本文件名
     */
    private String versionFileName;

    private String versionFileUrl;

    private int versionId;

    /**
     * 新版本更新日志
     */
    private String versionLog;
    /**
     * 新版本名
     */
    private String versionName;

    private int versionSize;
    /**
     * 新版本下载地址
     */
    private String versionUrl;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getApkTypeId() {
        return apkTypeId;
    }

    public void setApkTypeId(int apkTypeId) {
        this.apkTypeId = apkTypeId;
    }

    public String getMd5Value() {
        return md5Value;
    }

    public void setMd5Value(String md5Value) {
        this.md5Value = md5Value;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public int getVersionCount() {
        return versionCount;
    }

    public void setVersionCount(int versionCount) {
        this.versionCount = versionCount;
    }

    public String getVersionType() {
        return versionType;
    }

    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    public String getVersionFileName() {
        return versionFileName;
    }

    public void setVersionFileName(String versionFileName) {
        this.versionFileName = versionFileName;
    }

    public String getVersionFileUrl() {
        return versionFileUrl;
    }

    public void setVersionFileUrl(String versionFileUrl) {
        this.versionFileUrl = versionFileUrl;
    }

    public int getVersionId() {
        return versionId;
    }

    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    public String getVersionLog() {
        return versionLog;
    }

    public void setVersionLog(String versionLog) {
        this.versionLog = versionLog;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionSize() {
        return versionSize;
    }

    public void setVersionSize(int versionSize) {
        this.versionSize = versionSize;
    }

    public String getVersionUrl() {
        return versionUrl;
    }

    public void setVersionUrl(String versionUrl) {
        this.versionUrl = versionUrl;
    }
}
