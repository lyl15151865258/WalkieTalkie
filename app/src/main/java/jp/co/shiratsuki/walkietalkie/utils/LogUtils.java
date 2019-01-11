package jp.co.shiratsuki.walkietalkie.utils;

import android.util.Log;

/**
 * 日志工具类
 * Created at 2018/11/28 13:51
 *
 * @author LiYuliang
 * @version 1.0
 */

public class LogUtils {


    public static final String TAG = "NjMeter";

    private static final String TAG_TRACE = "NjMeter";

    private static final class StackTraceDebug extends RuntimeException {
        final static private long serialVersionUID = 27058374L;
    }

    /**
     * The debug flag is cached here so that we don't need to access the settings every time we have to evaluate it.
     */
    private static boolean isDebug = true;

    private static final boolean logTrace = true;

    private LogUtils() {
        // utility class
    }

    public static void trace(Object object) {
        if (logTrace) {
            StackTraceElement[] traces = Thread.currentThread().getStackTrace();
            StackTraceElement trace = traces[3];
            Log.d(TAG_TRACE, addThreadInfo(object.getClass().getSimpleName() + " : " + trace.getMethodName()));
        }
    }


    public static boolean isDebug() {
        return isDebug;
    }

    public static boolean isLogTrace() {
        return logTrace;
    }

    /**
     * Save a copy of the debug flag from the settings for performance reasons.
     */
    public static void setDebug(final boolean isDebug) {
        LogUtils.isDebug = isDebug;
    }

    private static String addThreadInfo(final String msg) {
        final String threadName = Thread.currentThread().getName();
        final String shortName = threadName.startsWith("OkHttp") ? "OkHttp" : threadName;
        return "[" + shortName + "] " + msg;
    }

    public static void v(final String msg) {
        if (isDebug) {
            Log.v(TAG, addThreadInfo(msg));
        }
    }

    public static void v(final String msg, final Throwable t) {
        if (isDebug) {
            Log.v(TAG, addThreadInfo(msg), t);
        }
    }

    public static void d(final String msg, final Throwable t) {
        if (isDebug) {
            Log.d(TAG, addThreadInfo(msg), t);
        }
    }

    public static void i(final String msg) {
        if (isDebug) {
            Log.i(TAG, addThreadInfo(msg));
        }
    }

    public static void i(final String msg, final Throwable t) {
        if (isDebug) {
            Log.i(TAG, addThreadInfo(msg), t);
        }
    }

    public static void w(final String msg) {
        Log.w(TAG, addThreadInfo(msg));
    }

    public static void w(final String msg, final Throwable t) {
        Log.w(TAG, addThreadInfo(msg), t);
    }

    public static void e(final String msg) {
        Log.e(TAG, addThreadInfo(msg));
    }

    public static void e(final String msg, final Throwable t) {
        Log.e(TAG, addThreadInfo(msg), t);
    }

    /**
     * Record a debug message with the actual stack trace.
     *
     * @param msg the debug message
     */
    public static void logStackTrace(final String msg) {
        try {
            throw new StackTraceDebug();
        } catch (final StackTraceDebug dbg) {
            d(msg, dbg);
        }
    }

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.d(tag, msg);
        }
    }

    public static void d(String msg) {
        if (isDebug) {
            Log.d(LogUtils.TAG, msg);
        }
    }

}
