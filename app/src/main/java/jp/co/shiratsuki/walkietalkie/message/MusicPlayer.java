package jp.co.shiratsuki.walkietalkie.message;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.LocaleList;
import android.os.Vibrator;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.bean.Music;
import jp.co.shiratsuki.walkietalkie.bean.MusicList;
import jp.co.shiratsuki.walkietalkie.bean.User;
import jp.co.shiratsuki.walkietalkie.constant.MusicPlay;
import jp.co.shiratsuki.walkietalkie.constant.NetWork;
import jp.co.shiratsuki.walkietalkie.contentprovider.SPHelper;
import jp.co.shiratsuki.walkietalkie.utils.GsonUtils;
import jp.co.shiratsuki.walkietalkie.utils.LanguageUtil;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.utils.NetworkUtil;
import jp.co.shiratsuki.walkietalkie.utils.UrlCheckUtil;
import jp.co.shiratsuki.walkietalkie.utils.VoiceFileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * 音乐播放工具
 * Created at 2018/11/29 9:53
 *
 * @author LiYuliang
 * @version 1.0
 */

public class MusicPlayer {

    private static final String TAG = "MusicPlayer";

    private volatile static MusicPlayer mVoicePlay;
    private ExecutorService mExecutorService;
    private Context mContext;
    private String serverHost;
    private volatile static List<MusicList> musicListList;
    private boolean flag = true;
    private int interval1, interval2;
    private Ringtone ringtone;
    private Vibrator vibrator;
    private VoiceFileUtils fileUtils;
    private AudioAsyncTask mAudioAsyncTask;

    private MusicPlayer(Context context) {
        this.mContext = context;
        this.mExecutorService = Executors.newCachedThreadPool();
        interval1 = MusicPlay.INTERVAL_ONE_LIST;
        interval2 = MusicPlay.INTERVAL_TOTAL_LIST;
        musicListList = Collections.synchronizedList(new ArrayList<>());
        User user = GsonUtils.parseJSON(SPHelper.getString("User", GsonUtils.convertJSON(new User())), User.class);
        if (user.getMessage_ip().equals("") || user.getMessage_port().equals("")) {
            serverHost = NetWork.MESSAGE_SERVER_IP;
        } else {
            serverHost = user.getMessage_ip();
        }
        vibrator = (Vibrator) mContext.getSystemService(VIBRATOR_SERVICE);
        fileUtils = new VoiceFileUtils(context);
    }

    /**
     * 单例模式（懒汉式）
     *
     * @return MusicPlay对象
     */
    public static MusicPlayer with(Context context) {
        if (mVoicePlay == null) {
            mVoicePlay = new MusicPlayer(context);
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
        if (!musicListList.contains(musicList)) {
            // 不包含了这条异常信息
            LogUtils.d(TAG, "添加音乐，编号：" + musicList.getListNo() + "，列表中不包含该音乐，添加成功");
            musicListList.add(musicList);
            this.interval1 = interval1;
            this.interval2 = interval2;
        } else {
            LogUtils.d(TAG, "添加音乐，编号：" + musicList.getListNo() + "，列表中包含该音乐，修改其播放次数为0");
            for (int i = 0; i < musicListList.size(); i++) {
                if (musicListList.get(i).getListNo() == musicList.getListNo()) {
                    musicListList.get(i).setAlreadyPlayCount(0);
                    for (int j = 0; j < musicListList.get(i).getMusicList().size(); j++) {
                        Music music = musicListList.get(i).getMusicList().get(j);
                        music.setAlreadyPlayCount(0);
                        music.setFirstPlay(true);
                    }
                }
            }
        }
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
                        // 如果已经播放次数小于需要播放的次数，或者，需要播放的次数为-1（无限循环播放）且已播放次数（默认为0，除非用户手动取消播放，会设置成需要播放次数）不等于需要播放次数（-1）
                        if (musicListList.get(i).getAlreadyPlayCount() < musicListList.get(i).getPlayCount() ||
                                (musicListList.get(i).getPlayCount() == -1 && (musicListList.get(i).getAlreadyPlayCount() != musicListList.get(i).getPlayCount()))) {

                            if (SPHelper.getBoolean("CanPlay", false)) {
                                playOneList(i);
                                LogUtils.d(TAG, "播放ID：" + musicListList.get(i).getListNo() + ",已经播放次数：" + musicListList.get(i).getAlreadyPlayCount());
                                int alreadyPlayCount = musicListList.get(i).getAlreadyPlayCount();
                                // 如果播放次数不为-1（为-1的话需要无限循环播放）而且当前是允许播放，播放次数加1
                                if (musicListList.get(i).getPlayCount() != -1 && SPHelper.getBoolean("CanPlay", false)) {
                                    alreadyPlayCount = musicListList.get(i).getAlreadyPlayCount() + 1;
                                } else {
                                    LogUtils.d(TAG, "需要播放次数为-1或者当前以及暂停播放，因此播放次数无需增加");
                                }
                                musicListList.get(i).setAlreadyPlayCount(alreadyPlayCount);
                                LogUtils.d(TAG, "播放ID：" + musicListList.get(i).getListNo() + ",已经播放次数：" + alreadyPlayCount);
                                // 如果需要播放次数不为-1（无限循环播放），而且已经播放次数大于等于需要播放次数，添加到删除列表中
                                if (musicListList.get(i).getPlayCount() != -1 && alreadyPlayCount >= musicListList.get(i).getPlayCount()) {
                                    delList.add(musicListList.get(i));
                                    // 通知页面布局更新
                                    Intent intent1 = new Intent();
                                    intent1.setAction("NO_LONGER_PLAYING");
                                    intent1.putExtra("number", musicListList.get(i).getListNo());
                                    mContext.sendBroadcast(intent1);
                                }
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
                            LogUtils.d(TAG, "不满足播放条件");
                            delList.add(musicListList.get(i));
                            // 通知页面布局更新
                            Intent intent1 = new Intent();
                            intent1.setAction("NO_LONGER_PLAYING");
                            intent1.putExtra("number", musicListList.get(i).getListNo());
                            mContext.sendBroadcast(intent1);
                        }
                    }
                    musicListList.removeAll(delList);
                } catch (Exception e) {
                    LogUtils.d(TAG, "播放音乐发生错误");
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
        synchronized (MusicPlayer.this) {
            // CountDownLatch允许一个或多个线程等待其他线程执行完毕后再运行
            // CountDownLatch的构造函数接收int类型的参数作为计数器，若要等待N个点再执行后续逻辑，就传入N。
            // 这里的N可以是N个线程，也可以是N个执行步骤。
            // 当我们调用countDown( )方法时，N会减一。
            // 调用await( ) 方法来阻塞当前线程，直到N减为0。
            CountDownLatch mCountDownLatch = new CountDownLatch(1);
            if (SPHelper.getBoolean("CanPlay", false)) {
                MediaPlayer mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                List<Music> musicList = musicListList.get(position).getMusicList();
                final int[] counter = {0};
                if (musicList.get(counter[0]).isFirstPlay()) {
                    LogUtils.d(TAG, "第一次播放，播放叮咚声音并震动");
                    // 通知主页面刷新布局
                    Intent intent = new Intent();
                    intent.setAction("CURRENT_PLAYING");
                    intent.putExtra("number", musicList.get(counter[0]).getListNo());
                    mContext.sendBroadcast(intent);
                    // 如果是第一次播放的话，播放叮咚声音
                    CountDownLatch mCountDownLatch1 = new CountDownLatch(1);
                    playDingDong(mCountDownLatch1);
                    try {
                        mCountDownLatch1.await();
                        notifyAll();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    musicList.get(counter[0]).setFirstPlay(false);
                    LogUtils.d(TAG, "回到播放线程，开始播放音乐列表");
                    // 播放音乐列表
                    playMusic(mMediaPlayer, mCountDownLatch, musicList, position, counter);
                } else {
                    // 通知主页面刷新布局
                    Intent intent = new Intent();
                    intent.setAction("CURRENT_PLAYING");
                    intent.putExtra("number", musicList.get(counter[0]).getListNo());
                    mContext.sendBroadcast(intent);
                    // 播放音乐列表
                    playMusic(mMediaPlayer, mCountDownLatch, musicList, position, counter);
                }
                // 阻塞线程，等待中
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
            } else {
                LogUtils.d(TAG, "当前异常信息的音乐暂停播放");
                mCountDownLatch.countDown();
            }
        }
    }

    /**
     * 获取文件真正的路径
     *
     * @param musicList 音乐列表对象
     * @param fileName  音乐文件名
     * @return 音乐文件的全路径
     */
    private String getMusicPath(MusicList musicList, String fileName) {
        String directory = "";
        switch (LanguageUtil.getLanguageLocal(mContext)) {
            case "":
                // 手机设置的语言是跟随系统
                Locale locale;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    locale = LocaleList.getDefault().get(0);
                } else {
                    locale = Locale.getDefault();
                }
                String language = locale.getLanguage();
                switch (language) {
                    case "zh":
                        directory = musicList.getChineseFolder();
                        break;
                    case "ja":
                        directory = musicList.getJapaneseFolder();
                        break;
                    default:
                        directory = musicList.getEnglishFolder();
                        break;
                }
                break;
            case "zh":
                directory = musicList.getChineseFolder();
                break;
            case "ja":
                directory = musicList.getJapaneseFolder();
                break;
            case "en":
                directory = musicList.getEnglishFolder();
                break;
            default:
                break;
        }
        return "http://" + serverHost + "/" + directory + "/" + fileName;
    }

    /**
     * 子线程中播放音乐列表
     *
     * @param mMediaPlayer    音乐播放器对象
     * @param mCountDownLatch CountDownLatch对象
     * @param musicList       音乐列表
     * @param position        当前播放的音乐列表在总列表的位置
     * @param counter         计数器数组
     */
    private void playMusic(MediaPlayer mMediaPlayer, CountDownLatch mCountDownLatch, List<Music> musicList, int position, int[] counter) {
        mExecutorService.execute(() -> {
            try {
                if (musicListList.size() == 0) {
                    return;
                }
//                if (musicList.get(0).getPlayCount() != musicList.get(0).getAlreadyPlayCount()) {
                // 根据语言获取音乐路径
                String filePath = getMusicPath(musicListList.get(position), musicList.get(counter[0]).getFilePath());
                LogUtils.d(TAG, "检查文件是否存在，文件路径：" + filePath);
                if (SPHelper.getBoolean("CanPlay", false)) {
                    Callable<String> call = new Callable<String>() {
                        @Override
                        public String call() {
                            // 开始执行耗时操作
                            LogUtils.d(TAG, "Future限时任务——————————————开始");
                            // 用于阻塞Future限时任务
                            CountDownLatch mCountDownLatch1 = new CountDownLatch(1);
                            try {
                                mMediaPlayer.reset();

                                // 检查本地缓存
                                String localFile = fileUtils.exists(filePath);
                                if (localFile == null) {
                                    // 本地无缓存，则播放服务端音乐并缓存
                                    if (NetworkUtil.isNetworkAvailable(mContext) && UrlCheckUtil.checkUrlExist(filePath)) {
                                        LogUtils.d(TAG, "播放的是服务端的音乐文件：" + filePath);
                                        mMediaPlayer.setDataSource(filePath);
                                        mAudioAsyncTask = new AudioAsyncTask(fileUtils);
                                        mAudioAsyncTask.execute(filePath);
                                        mMediaPlayer.prepareAsync();
                                    } else {
                                        // 如果网络未连接或者音乐链接不存在
                                        mCountDownLatch1.countDown();
                                    }
                                } else {
                                    // 本地有缓存
                                    LogUtils.d(TAG, "播放的是本地的音乐文件：" + localFile);
                                    mMediaPlayer.setDataSource(localFile);
                                    mMediaPlayer.prepareAsync();
                                }

                            } catch (IllegalArgumentException | IllegalStateException | IOException e) {
                                e.printStackTrace();
                            }
                            mMediaPlayer.setOnPreparedListener(mediaPlayer -> mMediaPlayer.start());
                            mMediaPlayer.setOnErrorListener((mediaPlayer, what, extra) -> {
                                // 遇到错误就重置MediaPlayer
                                LogUtils.d(TAG, "媒体文件获取异常，播放失败");
                                mediaPlayer.stop();
                                mediaPlayer.reset();
                                return false;
                            });
                            mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
                                // 如果播放次数达到上限，则通知页面布局更新
                                if (musicList.get(counter[0]).getPlayCount() != -1 &&
                                        musicList.get(counter[0]).getAlreadyPlayCount() >= musicList.get(counter[0]).getPlayCount()) {
                                    try {
                                        Intent intent1 = new Intent();
                                        intent1.setAction("NO_LONGER_PLAYING");
                                        intent1.putExtra("number", musicListList.get(counter[0]).getListNo());
                                        mContext.sendBroadcast(intent1);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                mediaPlayer.reset();
                                counter[0]++;
                                if (musicListList.size() == 0) {
                                    return;
                                }
                                List<Music> musicList1 = musicListList.get(position).getMusicList();

//                    if (counter[0] < musicList1.size() && musicList.get(0).getPlayCount() != musicList.get(0).getAlreadyPlayCount()) {
                                if (counter[0] < musicList1.size()) {
                                    String filePath1 = getMusicPath(musicListList.get(position), musicList.get(counter[0]).getFilePath());
                                    LogUtils.d(TAG, "检查文件是否存在，文件路径：" + filePath1);
                                    if (SPHelper.getBoolean("CanPlay", false)) {
                                        try {

                                            // 检查本地缓存
                                            String localFile = fileUtils.exists(filePath1);
                                            if (localFile == null) {
                                                // 本地无缓存，则缓存音乐
                                                if (NetworkUtil.isNetworkAvailable(mContext) && UrlCheckUtil.checkUrlExist(filePath1)) {
                                                    LogUtils.d(TAG, "播放的是服务端的音乐文件：" + filePath1);
                                                    mMediaPlayer.setDataSource(filePath1);
                                                    mAudioAsyncTask = new AudioAsyncTask(fileUtils);
                                                    mAudioAsyncTask.execute(filePath1);
                                                    mediaPlayer.prepareAsync();
                                                } else {
                                                    // 网络异常或者音乐文件不存在
                                                    mCountDownLatch1.countDown();
                                                }
                                            } else {
                                                // 本地有缓存
                                                LogUtils.d(TAG, "播放的是本地的音乐文件：" + localFile);
                                                mMediaPlayer.setDataSource(localFile);
                                                mediaPlayer.prepareAsync();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            mCountDownLatch1.countDown();
                                        }
                                    } else {
                                        mediaPlayer.release();
                                        mCountDownLatch1.countDown();
                                    }
                                } else {
                                    mediaPlayer.release();
                                    mCountDownLatch1.countDown();
                                }
                            });
                            // 阻塞线程，等待中
                            try {
                                mCountDownLatch1.await();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                mCountDownLatch1.countDown();
                            }
                            return "音乐文件播放成功";
                        }
                    };
                    try {
                        Future<String> future = mExecutorService.submit(call);
                        //任务处理超时时间设为 10 秒
                        String obj = future.get(MusicPlay.MAX_PLAY_TIME_ONE_LIST, TimeUnit.SECONDS);
                        LogUtils.d(TAG, "Future限时任务——————————————任务成功返回：" + obj);
                    } catch (TimeoutException ex) {
                        LogUtils.d(TAG, "Future限时任务——————————————处理超时");
                        ex.printStackTrace();
                    } catch (Exception e) {
                        LogUtils.d(TAG, "Future限时任务——————————————处理失败：" + e.getMessage());
                        e.printStackTrace();
                    }
                    mMediaPlayer.release();
                    mCountDownLatch.countDown();

                } else {
                    // 不需要播放
                    mMediaPlayer.release();
                    mCountDownLatch.countDown();
                }

//                }
            } catch (Exception e) {
                e.printStackTrace();
                mMediaPlayer.release();
                mCountDownLatch.countDown();
            }
        });
    }

    /**
     * 播放叮咚声音（第一次播放一条报警信息的时候）
     *
     * @param mCountDownLatch 计数器
     */
    private void playDingDong(CountDownLatch mCountDownLatch) {
        mExecutorService.execute(() -> {
            // 播放提示音
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }
            Uri uri = Uri.parse("android.resource://jp.co.shiratsuki.walkietalkie/" + R.raw.dingdong);
            ringtone = RingtoneManager.getRingtone(mContext, uri);
            ringtone.setStreamType(AudioManager.STREAM_MUSIC);
            ringtone.play();
            // 震动0.5秒
            vibrator.vibrate(500);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mCountDownLatch.countDown();
        });
    }

    public void release() {
        flag = false;
        mExecutorService.shutdown();
        mVoicePlay = null;
    }
}
