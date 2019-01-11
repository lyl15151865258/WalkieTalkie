package jp.co.shiratsuki.walkietalkie.network.download;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

/**
 * 多线程下载文件
 * Created at 2018/11/28 13:45
 *
 * @author LiYuliang
 * @version 1.0
 */

public class MultiThreadDownload extends Thread {
    private static final String TAG = "MultiThreadDownload";
    /**
     * 缓冲区
     */
    private static final int BUFF_SIZE = 1024;
    /**
     * 每一个线程需要下载的大小
     */
    private int blockSize;
    /*** 线程数量<br> 默认为5个线程下载*/
    private int threadNum = 5;
    /*** 文件大小 */
    private int fileSize;
    /**
     * 已经下载多少
     */
    private int downloadSize;
    /**
     * 文件的url,线程编号，文件名称
     */
    private String UrlStr, ThreadNo, fileName;
    /***保存的路径*/
    private String savePath;
    /**
     * 下载的百分比
     */
    private int downloadPercent = 0;
    /**
     * 下载的速度
     */
    private int downloadSpeed = 0;
    /**
     * 下载用的时间
     */
    private int usedTime = 0;
    /**
     * 开始时间
     */
    private long startTime;
    /**
     * 当前时间
     */
    private long curTime;
    /**
     * 是否已经下载完成
     */
    private boolean completed = false;
    private Handler handler;

    /**
     * 下载的构造函数
     *
     * @param url      请求下载的URL
     * @param savePath 保存的路径
     * @param fileName 保存的名字
     */
    public MultiThreadDownload(Handler handler, String url, String savePath, String fileName) {
        this.handler = handler;
        this.UrlStr = url;
        this.savePath = savePath;
        this.fileName = fileName;
        Log.e(TAG, toString());
    }

    @Override
    public void run() {
        FileDownloadThread[] fds = new FileDownloadThread[threadNum];//设置线程数量
        try {
            URL url = new URL(UrlStr);
            URLConnection conn = url.openConnection();
            fileSize = conn.getContentLength();
            sendMsg(0);
            Log.e(TAG, "文件一共：" + fileSize);
            blockSize = fileSize / threadNum;   //获取每一段要下载多少
            File[] file = new File[threadNum];
            for (int i = 0; i < file.length; i++) {
                file[i] = new File(savePath + fileName + ".part" + String.valueOf(i));
                FileDownloadThread fdt = new FileDownloadThread(url, file[i], i * blockSize, (i + 1) != threadNum ? ((i + 1) * blockSize + 1) : fileSize);
                fdt.setName("thread" + i);
                fdt.start();
                fds[i] = fdt;
            }
            startTime = System.currentTimeMillis();
            boolean finished = false;
            while (!finished) {
                downloadSize = 0;
                finished = true;
                for (FileDownloadThread fileDownloadThread : fds) {
                    downloadSize += fileDownloadThread.getDownloadSize();
                    if (!fileDownloadThread.isFinished()) {
                        finished = false;
                    }
                }
                downloadPercent = (downloadSize * 100) / fileSize;
                curTime = System.currentTimeMillis();
                usedTime = (int) ((curTime - startTime) / 1000);

                if (usedTime == 0) usedTime = 1;
                downloadSpeed = (downloadSize / usedTime) / 1024;
                sleep(1000);
                sendMsg(1);
            }
            Log.e(TAG, "下载完成,准备整合");
            completed = true;
            RandomAccessFile raf = new RandomAccessFile(savePath + fileName, "rw");
            byte[] bytes = new byte[BUFF_SIZE];
            InputStream is;
            int byteRead;
            for (int i = 0; i < threadNum; i++) {
                is = new FileInputStream(file[i]);
                while ((byteRead = is.read(bytes)) != -1) {
                    raf.write(bytes, 0, byteRead);
                }
                is.close();
                file[i].delete();
            }
            raf.close();
            Log.e(TAG, "ok");
            sendMsg(2);
        } catch (Exception e) {
            Log.e(TAG, "multi file error");
            e.printStackTrace();
        }
        super.run();
    }

    /**
     * 得到文件的大小
     *
     * @return 文件大小
     */
    public int getFileSize() {
        return this.fileSize;
    }

    /**
     * 得到已经下载的数量
     *
     * @return 下载量
     */
    public int getDownloadSize() {
        return this.downloadSize;
    }

    /**
     * 获取下载百分比
     *
     * @return 下载百分比
     */
    public int getDownloadPercent() {
        return this.downloadPercent;
    }

    /**
     * 获取下载速度
     *
     * @return 下载速度
     */
    public int getDownloadSpeed() {
        return this.downloadSpeed;
    }

    /**
     * 修改默认线程数
     *
     * @param threadNum 多线程数量
     */
    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    /**
     * 分块下载完成的标志
     *
     * @return 分块是否已完成
     */
    public boolean isCompleted() {
        return this.completed;
    }

    @Override
    public String toString() {
        return "MultiThreadDownload [threadNum=" + threadNum + ", fileSize="
                + fileSize + ", UrlStr=" + UrlStr + ", ThreadNo=" + ThreadNo
                + ", fileName=" + fileName + ", savePath=" + savePath + "]";
    }

    private void sendMsg(int what) {
        Message msg = new Message();
        msg.what = what;
        handler.sendMessage(msg);
    }
}
