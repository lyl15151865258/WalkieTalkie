package jp.co.shiratsuki.walkietalkie.bean;

import jp.co.shiratsuki.walkietalkie.bean.version.Version;

/**
 * 用户登录返回的实体类
 * Created at 2019/3/1 14:05
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class LoginResult {

    private String result;

    private String message;

    private User user;

    private Version version;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
}
