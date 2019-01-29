package jp.co.shiratsuki.walkietalkie.voice;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import jp.co.shiratsuki.walkietalkie.bean.Music;
import jp.co.shiratsuki.walkietalkie.bean.MusicList;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 音乐播放工具
 * Created at 2018/11/29 9:53
 *
 * @author LiYuliang
 * @version 1.0
 */

public class MusicPlay {

    private static final String TAG = "MusicPlay";

    private volatile static MusicPlay mVoicePlay;
    private ExecutorService mExecutorService;
    private Context mContext;
    private volatile static List<MusicList> musicListList;
    private boolean flag = true;
    private int interval1 = 1000, interval2 = 3000;

    private MusicPlay(Context context) {
        this.mContext = context;
        this.mExecutorService = Executors.newCachedThreadPool();
        musicListList = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * 单例
     *
     * @return
     */
    public static MusicPlay with(Context context) {
        if (mVoicePlay == null) {
            synchronized (MusicPlay.class) {
                if (mVoicePlay == null) {
                    mVoicePlay = new MusicPlay(context);
                }
            }
        }
        return mVoicePlay;
    }

    public List<MusicList> getMusicListList() {
        return musicListList;
    }

    /**
     * 播放音乐
     */
    public void play() {
        LogUtils.d(TAG, "播放音乐");
        mExecutorService.execute(() -> start());
    }

    /**
     * 添加需要播放的音乐
     *
     * @param musicList 音乐列表对象
     * @param interval1 列表内间隔
     * @param interval2 音乐列表对象
     */
    public void addMusic(MusicList musicList, int interval1, int interval2) {
        LogUtils.d(TAG, "添加音乐，编号：" + musicList.getListNo());
        musicListList.add(musicList);
        this.interval1 = interval1;
        this.interval2 = interval2;
    }

    /**
     * 删除不需要播放的音乐（由于多线程操作会出问题，此处并不是真正移除，而是修改它的已播放次数为最大播放次数）
     *
     * @param listNo 异常ID
     */
    public void removeMusic(int listNo) {
        LogUtils.d(TAG, "移除音乐，编号：" + listNo);
        for (int i = 0; i < musicListList.size(); i++) {
            MusicList musicList = musicListList.get(i);
            if (musicList.getListNo() == listNo) {
                musicList.setAlreadyPlayCount(musicList.getPlayCount());
                for (int j = 0; j < musicList.getMusicList().size(); j++) {
                    Music music = musicList.getMusicList().get(j);
                    music.setAlreadyPlayCount(music.getPlayCount());
                }
            }
        }
    }

    /**
     * 开始播放音乐
     */
    private void start() {
        while (flag) {
            if (musicListList != null && musicListList.size() > 0) {
                try {
                    // 如果播放列表不为空且长度大于0
                    for (MusicList music : musicListList) {
                        LogUtils.d(TAG, "播放ID：" + music.getListNo() + ",已经播放次数：" + music.getAlreadyPlayCount());
                    }
                    List<MusicList> delList = new ArrayList<>();
                    for (int i = 0; i < musicListList.size(); i++) {
                        if (musicListList.get(i).getAlreadyPlayCount() < musicListList.get(i).getPlayCount()) {
                            playOneList(i);
                            LogUtils.d(TAG, "播放ID：" + musicListList.get(i).getListNo() + ",已经播放次数：" + musicListList.get(i).getAlreadyPlayCount());
                            int alreadyPlayCount = musicListList.get(i).getAlreadyPlayCount() + 1;
                            musicListList.get(i).setAlreadyPlayCount(alreadyPlayCount);
                            LogUtils.d(TAG, "播放ID：" + musicListList.get(i).getListNo() + ",已经播放次数：" + alreadyPlayCount);
                            if (alreadyPlayCount == musicListList.get(i).getPlayCount()) {
                                delList.add(musicListList.get(i));
                            }
                            // 如果是列表末尾位置,等待interval2
                            if (i >= musicListList.size() - 1) {
                                // 列表整体循环播放间隔
                                LogUtils.d("Sleep", "睡眠等待interval2");
                                try {
                                    Thread.sleep(interval2);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // 列表间播放间隔，等待interval1
                                LogUtils.d("Sleep", "睡眠等待interval1");
                                try {
                                    Thread.sleep(interval1);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            delList.add(musicListList.get(i));
                        }
                    }
                    musicListList.removeAll(delList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 播放一个音乐列表
     *
     * @param position 音乐列表
     */
    private void playOneList(int position) {
        synchronized (MusicPlay.this) {
            MediaPlayer mMediaPlayer = new MediaPlayer();
            CountDownLatch mCountDownLatch = new CountDownLatch(1);
            try {
                final int[] counter = {0};
                if (musicListList.size() == 0) {
                    return;
                }
                List<Music> musicList = musicListList.get(position).getMusicList();
                // 如果播放次数没有达到上限，则开始播放
//                if (musicList.get(0).getPlayCount() != musicList.get(0).getAlreadyPlayCount()) {
                // 通知主页面刷新布局
                Intent intent = new Intent();
                intent.setAction("CURRENT_PLAYING");
                intent.putExtra("number", musicList.get(counter[0]).getListNo());
                mContext.sendBroadcast(intent);

                mMediaPlayer.setDataSource(musicList.get(counter[0]).getFilePath());
                mMediaPlayer.prepareAsync();
                mMediaPlayer.setScreenOnWhilePlaying(true);
                mMediaPlayer.setOnPreparedListener(mediaPlayer -> mMediaPlayer.start());
                mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        // 遇到错误就重置MediaPlayer
                        mp.reset();
                        return false;
                    }
                });
                mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
                    mediaPlayer.reset();
                    counter[0]++;
                    if (musicListList.size() == 0) {
                        return;
                    }
                    List<Music> musicList1 = musicListList.get(position).getMusicList();
//                    if (counter[0] < musicList1.size() && musicList.get(0).getPlayCount() != musicList.get(0).getAlreadyPlayCount()) {
                    if (counter[0] < musicList1.size()) {
                        try {
                            mediaPlayer.setDataSource(musicList1.get(counter[0]).getFilePath());
                            mediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                            mCountDownLatch.countDown();
                        }
                    } else {
                        mediaPlayer.release();
                        mCountDownLatch.countDown();
                    }
                });
//                }
            } catch (Exception e) {
                e.printStackTrace();
                mCountDownLatch.countDown();
            }
            try {
                mCountDownLatch.await();
                notifyAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 播放完毕通知主页面刷新布局
            Intent intent1 = new Intent();
            intent1.setAction("CURRENT_PLAYING");
            intent1.putExtra("number", -1);
            mContext.sendBroadcast(intent1);
        }
    }

    public void release() {
        flag = false;
    }
}
