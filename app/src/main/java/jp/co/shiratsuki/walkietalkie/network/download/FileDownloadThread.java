package jp.co.shiratsuki.walkietalkie.network.download;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

/**
 * 下载文件线程
 * Created at 2018/11/28 13:45
 *
 * @author LiYuliang
 * @version 1.0
 */

public class FileDownloadThread extends Thread {
    private static final String TAG = "FileDownloadThread";
    //缓冲区
    private static final int BUFF_SIZE = 1024;
    //需要下载的URL
    private URL url;
    //缓存的FIle
    private File file;
    //开始位置
    private int startPosition;
    //结束位置
    private int endPosition;
    //当前位置
    private int curPosition;
    //完成
    private boolean finished = false;
    //已经下载多少
    private int downloadSize = 0;

    /***
     * 分块文件下载
     * @param url   下载的URL
     * @param file  下载的文件
     * @param startPosition 开始位置
     * @param endPosition   结束位置
     */
    public FileDownloadThread(URL url, File file, int startPosition, int endPosition) {
        this.url = url;
        this.file = file;
        this.startPosition = startPosition;
        this.curPosition = startPosition;
        this.endPosition = endPosition;
        Log.e(TAG, toString());
    }

    @Override
    public void run() {
        BufferedInputStream bis;
        RandomAccessFile fos;
        byte[] buf = new byte[BUFF_SIZE];
        URLConnection conn;
        try {
            conn = url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setAllowUserInteraction(true);
            if ((file.length() + startPosition) == endPosition) {
                this.finished = true;
            } else {
                conn.setRequestProperty("Range", "bytes=" + (file.length() + startPosition) + "-" + endPosition);  //取剩余未下载的
                fos = new RandomAccessFile(file, "rw");//读写
                fos.seek(file.length());
                bis = new BufferedInputStream(conn.getInputStream(), BUFF_SIZE);
                while (curPosition < endPosition)  //当前位置小于结束位置  继续下载
                {
                    int len = bis.read(buf, 0, BUFF_SIZE);
                    if (len == -1)   //下载完成
                    {
                        break;
                    }
                    fos.write(buf, 0, len);
                    curPosition = curPosition + len;
                    if (curPosition > endPosition) {
                        downloadSize += len - (curPosition - endPosition) + 1;
                    } else {
                        downloadSize += len;
                    }
                }
                this.finished = true;  //当前阶段下载完成
                Log.e(TAG, "当前" + this.getName() + "下载完成");
                bis.close();  //关闭流
                fos.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "download error");
            e.printStackTrace();
        }
        super.run();
    }

    /**
     * 是否完成当前段下载完成
     *
     * @return boolean是否完成
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * 已经下载多少
     *
     * @return 下载量
     */
    public int getDownloadSize() {
        return downloadSize;
    }

    @Override
    public String toString() {
        return "FileDownloadThread [url=" + url + ", file=" + file
                + ", startPosition=" + startPosition + ", endPosition="
                + endPosition + ", curPosition=" + curPosition + ", finished="
                + finished + ", downloadSize=" + downloadSize + "]";
    }

}
