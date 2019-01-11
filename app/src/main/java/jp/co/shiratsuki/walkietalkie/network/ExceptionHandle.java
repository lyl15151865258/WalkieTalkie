package jp.co.shiratsuki.walkietalkie.network;

import com.google.gson.JsonParseException;

import jp.co.shiratsuki.walkietalkie.utils.LogUtils;

import org.json.JSONException;

import java.net.ConnectException;

import retrofit2.adapter.rxjava.HttpException;

/**
 * 网络请求异常类，结合Retrofit使用
 * Created at 2018/11/28 13:48
 *
 * @author LiYuliang
 * @version 1.0
 */

public class ExceptionHandle {

    public static ResponseThrowable handleException(Throwable e) {
        ResponseThrowable ex;
        e.printStackTrace();
        LogUtils.d("okhttp",e.getMessage());
        if (e instanceof ConnectException) {
            ex = new ResponseThrowable(e, ERROR.CONNECT_ERROR);
            ex.message = "连接服务器失败";
            return ex;
        } else if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            ex = new ResponseThrowable(e, ERROR.HTTP_ERROR);
            ex.message = httpException.code() + "错误";
            return ex;
        } else if (e instanceof ServerException) {
            ServerException resultException = (ServerException) e;
            ex = new ResponseThrowable(resultException, resultException.code);
            ex.message = resultException.message;
            return ex;
        } else if (e instanceof JsonParseException || e instanceof JSONException
            /*|| e instanceof ParseException*/) {
            ex = new ResponseThrowable(e, ERROR.PARSE_ERROR);
            ex.message = "Json数据解析错误";
            return ex;
        } else if (e instanceof javax.net.ssl.SSLHandshakeException) {
            ex = new ResponseThrowable(e, ERROR.SSL_ERROR);
            ex.message = "证书验证失败";
            return ex;
        } else {
            ex = new ResponseThrowable(e, ERROR.UNKNOWN);
            ex.message = "未知错误，请联系管理员";
            return ex;
        }
    }


    /**
     * 约定异常
     */
    class ERROR {
        /**
         * 未知错误
         */
        public static final int UNKNOWN = 1000;
        /**
         * 解析错误
         */
        public static final int PARSE_ERROR = 1001;
        /**
         * 连接错误
         */
        public static final int CONNECT_ERROR = 1002;
        /**
         * 协议出错
         */
        public static final int HTTP_ERROR = 1003;
        /**
         * 证书出错
         */
        public static final int SSL_ERROR = 1005;
    }

    public static class ResponseThrowable extends Exception {
        public int code;
        public String message;

        public ResponseThrowable(Throwable throwable, int code) {
            super(throwable);
            this.code = code;
        }
    }

    /**
     * ServerException发生后，将自动转换为ResponeThrowable返回
     */
    class ServerException extends RuntimeException {
        int code;
        String message;
    }

}
