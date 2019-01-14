package jp.co.shiratsuki.walkietalkie.interfaces;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * 图片选择的回调接口
 * Created at 2018/8/4 0004 16:17
 * 
 * @author LiYuliang
 * @version 1.0
 */

public interface OnPictureSelectedListener {
    /**
     * 图片选择的监听回调
     *
     * @param fileUri 图片路径
     * @param bitmap  图片
     */
    void onPictureSelected(Uri fileUri, Bitmap bitmap);
}
