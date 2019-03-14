package jp.co.shiratsuki.walkietalkie.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

/**
 * Android系统下载工具类
 * Created at 2019/3/14 17:38
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class DownloadManagerUtil {

    /**
     * 交给Android系统后台下载文件
     *
     * @param path 文件路径
     */
    private void downloadApk(Context mContext, String path) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(path));
        //设置在什么网络情况下进行下载
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        //设置通知栏标题
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle("下载");
        request.setDescription("今日头条正在下载");
        request.setAllowedOverRoaming(false);
        //设置文件存放目录
        request.setDestinationInExternalFilesDir(mContext, Environment.DIRECTORY_DOWNLOADS, "Apk");

        //获取下载管理器
        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        //将下载任务加入下载队列，否则不会进行下载
        downloadManager.enqueue(request);
    }

}
