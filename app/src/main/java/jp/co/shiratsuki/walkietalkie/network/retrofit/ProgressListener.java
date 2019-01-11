package jp.co.shiratsuki.walkietalkie.network.retrofit;

/**
 * 请求进度监听接口
 * Created at 2018/11/28 13:46
 *
 * @author LiYuliang
 * @version 1.0
 */

public interface ProgressListener {
    void onProgress(long currentBytes, long contentLength, boolean done);
}
