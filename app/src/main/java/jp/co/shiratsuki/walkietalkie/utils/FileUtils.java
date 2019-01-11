package jp.co.shiratsuki.walkietalkie.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import java.io.IOException;

/**
 * 文件处理类
 * Created at 2018/11/28 13:50
 *
 * @author LiYuliang
 * @version 1.0
 */

public class FileUtils {

    /**
     * Assets获取资源
     *
     * @param context
     * @param filename
     * @return
     * @throws IOException
     */
    public static AssetFileDescriptor getAssetFileDescription(Context context, String filename) throws IOException {
        AssetManager manager = context.getApplicationContext().getAssets();
        return manager.openFd(filename);
    }
}
