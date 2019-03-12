package jp.co.shiratsuki.walkietalkie.message;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import jp.co.shiratsuki.walkietalkie.utils.VoiceFileUtils;

/**
 * 音乐下载线程工具类
 * Created at 2019/3/12 13:42
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class AudioAsyncTask extends AsyncTask<String, Void, Void> {
    private VoiceFileUtils audioFile;

    public AudioAsyncTask(VoiceFileUtils audioFile) {
        this.audioFile = audioFile;
    }

    public void checkDownFile() {
        audioFile.checkDownFile();
    }

    @Override
    protected Void doInBackground(String... params) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(params[0]); // 构建URL
            // 构造网络连接
            conn = (HttpURLConnection) url.openConnection();
            // 保存音频文件
            audioFile.exists(params[0]);
            audioFile.saveFile(conn.getInputStream(), params[0]);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assert conn != null;
            conn.disconnect(); // 断开网络连接
        }
        return null;
    }
}
