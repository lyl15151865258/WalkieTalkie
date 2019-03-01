package jp.co.shiratsuki.walkietalkie.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Random;

import jp.co.shiratsuki.walkietalkie.R;

/**
 * 图片处理工具（压缩、转base64、添加水印、添加文字、放大缩小、旋转、生成图片验证码）
 * Created by LiYuliang on 2017/3/7 0007.
 *
 * @author LiYuliang
 * @version 2018/03/15
 */

public class BitmapUtils {

    public interface OnGetMapHeadListener {
        void success(Bitmap bitmap);

        void fail();
    }

    private int width = 140, height = 40, codeLen = 4;
    private String checkCode = "";
    private Random random = new Random();

    /**
     * 从资源文件获取bitmap
     *
     * @param context    Context对象
     * @param resourceId 资源ID
     * @return bitmap
     */
    public static Bitmap getBitmapFromResource(Context context, int resourceId) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 从网络路径获取bitmap
     *
     * @param url 图片地址
     * @return bitmap
     */
    public static Bitmap getBitmapFromNetwork(String url) {
        Bitmap bitmap = null;
        try {
            //建立网络连接
            URL imageURl = new URL(url);
            URLConnection con = imageURl.openConnection();
            con.connect();
            InputStream in = con.getInputStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 图片质量压缩方法
     */
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 90;
        while (baos.toByteArray().length / 1024 > 10) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            LogUtils.d("photo", "Utils处理中，长度为" + baos.toByteArray().length / 1024);
            baos.reset(); // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        return BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
    }

    /**
     * 图片按比例大小压缩方法
     */
    public static Bitmap getimage(String srcPath) {

        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;// 这里设置高度为800f
        float ww = 480f;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) {
            be = 1;
        }
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
    }

    /**
     * 图片按比例大小压缩方法
     */
    public static Bitmap compressScale(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        // 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
        if (baos.toByteArray().length / 1024 > 1024) {
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 80, baos);// 这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        // float hh = 800f;// 这里设置高度为800f
        // float ww = 480f;// 这里设置宽度为480f
        float hh = 512f;
        float ww = 512f;
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) { // 如果高度高的话根据高度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) {
            be = 1;
        }
        newOpts.inSampleSize = be; // 设置缩放比例
        // newOpts.inPreferredConfig = Config.RGB_565;//降低图片从ARGB888到RGB565

        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);

        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩

        //return bitmap;
    }

    //压缩图片，参数为原图片路径和输出图片路径
    public static void compressPicture(String srcPath, String desPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        // options.inJustDecodeBounds = true 表示只读图片，不加载到内存中;
        // 设置这个参数为ture，就不会给图片分配内存空间，但是可以获取到图片的大小等属性;
        // 设置为false, 就是要加载这个图片.
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, options);
        // 缩放图片的尺寸
        float w = options.outWidth;
        float h = options.outHeight;
        float hh = 1024f;
        float ww = 1024f;
        // 最长宽度或高度1024
        float be = 1.0f;
        if (w > h && w > ww) {
            be = w / ww;
        } else if (w < h && h > hh) {
            be = h / hh;
        }
        if (be <= 0) {
            be = 1.0f;
        }
        options.inSampleSize = (int) be;// 设置缩放比例,这个数字越大,图片大小越小.
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(srcPath, options);
        int desWidth = (int) Math.ceil(w / be);
        int desHeight = (int) Math.ceil(h / be);
        bitmap = Bitmap.createScaledBitmap(bitmap, desWidth, desHeight, true);
        try {
            FileOutputStream fos = new FileOutputStream(desPath);
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 60, fos);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * bitmap转为base64
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * base64转为bitmap
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * 压缩图片
     *
     * @param bitmap   源图片
     * @param width    想要的宽度
     * @param height   想要的高度
     * @param isAdjust 是否自动调整尺寸, true图片就不会拉伸，false严格按照你的尺寸压缩
     * @return Bitmap
     */
    public Bitmap reduce(Bitmap bitmap, int width, int height, boolean isAdjust) {
        // 如果想要的宽度和高度都比源图片小，就不压缩了，直接返回原图
        if (bitmap.getWidth() < width && bitmap.getHeight() < height) {
            return bitmap;
        }
        // 根据想要的尺寸精确计算压缩比例, 方法详解：public BigDecimal divide(BigDecimal divisor, int scale, int roundingMode);
        // scale表示要保留的小数位, roundingMode表示如何处理多余的小数位，BigDecimal.ROUND_DOWN表示自动舍弃
        float sx = new BigDecimal(width).divide(new BigDecimal(bitmap.getWidth()), 4, BigDecimal.ROUND_DOWN).floatValue();
        float sy = new BigDecimal(height).divide(new BigDecimal(bitmap.getHeight()), 4, BigDecimal.ROUND_DOWN).floatValue();
        if (isAdjust) {// 如果想自动调整比例，不至于图片会拉伸
            sx = (sx < sy ? sx : sy);
            sy = sx;// 哪个比例小一点，就用哪个比例
        }
        Matrix matrix = new Matrix();
        matrix.postScale(sx, sy);// 调用api中的方法进行压缩，就大功告成了
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 旋转图片
     *
     * @param bitmap 源图片
     * @param angle  旋转角度(90为顺时针旋转,-90为逆时针旋转)
     * @return Bitmap
     */
    public Bitmap rotate(Bitmap bitmap, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 放大或缩小图片
     *
     * @param bitmap 源图片
     * @param ratio  放大或缩小的倍数，大于1表示放大，小于1表示缩小
     * @return Bitmap
     */
    public Bitmap zoom(Bitmap bitmap, float ratio) {
        if (ratio < 0f) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 在图片上印字
     *
     * @param bitmap 源图片
     * @param text   印上去的字
     * @param param  字体参数分别为：颜色,大小,是否加粗,起点x,起点y; 比如：{color : 0xFF000000, size : 30, bold : true, x : 20, y : 20}
     * @return Bitmap
     */
    public Bitmap printWord(Bitmap bitmap, String text, Map<String, Object> param) {
        if (text.trim().equals("") || null == param) {
            return bitmap;
        }
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.save();
        canvas.restore();
        Paint paint = new Paint();
        paint.setColor(null != param.get("color") ? (Integer) param.get("color") : Color.BLACK);
        paint.setTextSize(null != param.get("size") ? (Integer) param.get("size") : 20);
        paint.setFakeBoldText(null != param.get("bold") ? (Boolean) param.get("bold") : false);
        canvas.drawText(text, null != param.get("x") ? (Integer) param.get("x") : 0, null != param.get("y") ? (Integer) param.get("y") : 0, paint);
        canvas.save();
        canvas.restore();
        return newBitmap;
    }

    /**
     * 创建logo(给图片加水印),
     *
     * @param bitmaps 原图片和水印图片
     * @param left    左边起点坐标
     * @param top     顶部起点坐标t
     * @return Bitmap
     */
    public Bitmap createLogo(Bitmap[] bitmaps, int left, int top) {
        Bitmap newBitmap = Bitmap.createBitmap(bitmaps[0].getWidth(), bitmaps[0].getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        for (int i = 0; i < bitmaps.length; i++) {
            if (i == 0) {
                canvas.drawBitmap(bitmaps[0], 0, 0, null);
            } else {
                canvas.drawBitmap(bitmaps[i], left, top, null);
            }
            canvas.save();
            canvas.restore();
        }
        return newBitmap;
    }

    /**
     * 产生一个4位随机数字的图片验证码
     *
     * @return Bitmap
     */
    public Bitmap createCode() {
        checkCode = "";
        String[] chars = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        for (int i = 0; i < codeLen; i++) {
            checkCode += chars[random.nextInt(chars.length)];
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setTextSize(30);
        paint.setColor(Color.BLUE);
        for (int i = 0; i < checkCode.length(); i++) {
            paint.setColor(randomColor(1));
            paint.setFakeBoldText(random.nextBoolean());
            float skewX = random.nextInt(11) / 10;
            paint.setTextSkewX(random.nextBoolean() ? skewX : -skewX);
            int x = width / codeLen * i + random.nextInt(10);
            canvas.drawText(String.valueOf(checkCode.charAt(i)), x, 28, paint);
        }
        for (int i = 0; i < 3; i++) {
            drawLine(canvas, paint);
        }
        for (int i = 0; i < 255; i++) {
            drawPoints(canvas, paint);
        }
        canvas.save();
        canvas.restore();
        return bitmap;
    }

    /**
     * 获得一个随机的颜色
     *
     * @param rate
     * @return
     */
    private int randomColor(int rate) {
        int red = random.nextInt(256) / rate, green = random.nextInt(256) / rate, blue = random.nextInt(256) / rate;
        return Color.rgb(red, green, blue);
    }

    /**
     * 画随机线条
     *
     * @param canvas
     * @param paint
     */
    private void drawLine(Canvas canvas, Paint paint) {
        int startX = random.nextInt(width), startY = random.nextInt(height);
        int stopX = random.nextInt(width), stopY = random.nextInt(height);
        paint.setStrokeWidth(1);
        paint.setColor(randomColor(1));
        canvas.drawLine(startX, startY, stopX, stopY, paint);
    }

    /**
     * 画随机干扰点
     *
     * @param canvas
     * @param paint
     */
    private void drawPoints(Canvas canvas, Paint paint) {
        int stopX = random.nextInt(width), stopY = random.nextInt(height);
        paint.setStrokeWidth(1);
        paint.setColor(randomColor(1));
        canvas.drawPoint(stopX, stopY, paint);
    }

    /**
     * 返回真实验证码字符串
     *
     * @return String
     */
    public String getCheckCode() {
        return checkCode;
    }

    /**
     * 从Assets中读取图片
     */
    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 将ViewGroup转成bitmap
     *
     * @param v
     * @return
     */
    public static Bitmap loadBitmapFromView(View v) {
        int w = v.getWidth();
        int h = v.getHeight();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        //如果不设置canvas画布为白色，则生成透明
        c.drawColor(Color.WHITE);
        v.layout(0, 0, w, h);
        v.draw(c);
        return bmp;
    }

    //保存文件到指定路径
    public static boolean saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dearxy";
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();

            //把文件插入到系统图库
            //MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            return isSuccess;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
