package jp.co.shiratsuki.walkietalkie.bean;

/**
 * 用户操作返回的实体类
 * Created at 2019/2/16 11:40
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class UserOperateResult {

    private String result;

    private String message;

    private User user;

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
}
