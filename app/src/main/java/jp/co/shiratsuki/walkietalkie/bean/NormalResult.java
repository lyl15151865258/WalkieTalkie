package jp.co.shiratsuki.walkietalkie.bean;

/**
 * 通用请求结果实体类
 * Created at 2018/11/28 13:41
 *
 * @author LiYuliang
 * @version 1.0
 */

public class NormalResult {

    private String result;
    private String message;

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
}
