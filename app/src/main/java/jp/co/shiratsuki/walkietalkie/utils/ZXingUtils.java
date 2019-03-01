package jp.co.shiratsuki.walkietalkie.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * 生成条形码和二维码的工具
 * Created at 2018/8/27 0027 16:40
 *
 * @author LiYuliang
 * @version 1.0
 */

public class ZXingUtils {

    /**
     * 生成二维码 要转换的地址或字符串,可以是中文
     *
     * @param url    URL地址
     * @param width  宽度
     * @param height 高度
     * @param height 高度
     * @return 二维码bitmap
     */
    public static Bitmap createQRImage(String url, final int width, final int height) {
        try {
            // 判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成条形码
     *
     * @param context       Context对象
     * @param contents      需要生成的内容
     * @param desiredWidth  生成条形码的宽带
     * @param desiredHeight 生成条形码的高度
     * @param displayCode   是否在条形码下方显示内容
     * @return 条形码bitmap
     */
    public static Bitmap creatBarcode(Context context, String contents, int desiredWidth, int desiredHeight, boolean displayCode) {
        Bitmap ruseltBitmap;
        //图片两端所保留的空白的宽度
        int marginW = 20;
        //条形码的编码类型
        BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;

        if (displayCode) {
            Bitmap barcodeBitmap = encodeAsBitmap(contents, barcodeFormat, desiredWidth, desiredHeight);
            Bitmap codeBitmap = createCodeBitmap(contents, desiredWidth + 2 * marginW, desiredHeight, context);
            ruseltBitmap = mixtureBitmap(barcodeBitmap, codeBitmap, new PointF(0, desiredHeight));
        } else {
            ruseltBitmap = encodeAsBitmap(contents, barcodeFormat, desiredWidth, desiredHeight);
        }

        return ruseltBitmap;
    }

    /**
     * 生成条形码的Bitmap
     *
     * @param contents      需要生成的内容
     * @param format        编码格式
     * @param desiredWidth  宽度
     * @param desiredHeight 高度
     * @return 条形码bitmap
     */
    public static Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int desiredWidth, int desiredHeight) {
        final int WHITE = 0xFFFFFFFF;
        final int BLACK = 0xFF000000;

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = null;
        try {
            result = writer.encode(contents, format, desiredWidth, desiredHeight, null);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 生成显示编码的Bitmap
     *
     * @param contents 文本内容
     * @param width    宽度
     * @param height   高度
     * @param context  Context对象
     * @return 条形码bitmap
     */
    protected static Bitmap createCodeBitmap(String contents, int width, int height, Context context) {
        TextView tv = new TextView(context);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        tv.setText(contents);
        tv.setHeight(height);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setWidth(width);
        tv.setDrawingCacheEnabled(true);
        tv.setTextColor(Color.BLACK);
        tv.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());

        tv.buildDrawingCache();
        return tv.getDrawingCache();
    }

    /**
     * 将两个Bitmap合并成一个
     *
     * @param first     第一个bitmap
     * @param second    第二个bitmap
     * @param fromPoint 第二个Bitmap开始绘制的起始位置（相对于第一个Bitmap）
     * @return 拼合的bitmap
     */
    protected static Bitmap mixtureBitmap(Bitmap first, Bitmap second, PointF fromPoint) {
        if (first == null || second == null || fromPoint == null) {
            return null;
        }
        int marginW = 20;
        Bitmap newBitmap = Bitmap.createBitmap(first.getWidth() + second.getWidth() + marginW, first.getHeight() + second.getHeight(), Config.ARGB_4444);
        Canvas cv = new Canvas(newBitmap);
        cv.drawBitmap(first, marginW, 0, null);
        cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
        cv.save();
        cv.restore();

        return newBitmap;
    }

    /**
     * 生成中间带Logo的二维码Bitmap
     *
     * @param content   内容
     * @param widthPix  图片宽度
     * @param heightPix 图片高度
     * @param logoBm    二维码中心的Logo图标（可以为null）
     * @param filePath  用于存储二维码图片的文件路径
     * @return 生成二维码及保存文件是否成功
     */
    public static boolean createQRImage(Context context, String content, int widthPix, int heightPix, Bitmap logoBm, String filePath) {
        try {
            if (content == null || "".equals(content)) {
                return false;
            }

            //配置参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //容错级别设置为最高
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            //设置空白边距的宽度
            hints.put(EncodeHintType.MARGIN, 2); //default is 4

            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints);
            int[] pixels = new int[widthPix * heightPix];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = 0xff000000;
                    } else {
                        pixels[y * widthPix + x] = 0xffffffff;
                    }
                }
            }

            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);

            if (logoBm != null) {
                // 1、先给原图片加上圆角
                // 2、再给加了圆角的原图片加上白色边界，用于区分底图
                // 3、再把图片加上圆角
                bitmap = addLogo(bitmap, GetRoundedCornerBitmap(context, drawBitmapBorder(GetRoundedCornerBitmap(context, logoBm),
                        DeviceUtil.dp2px(context, 10), Color.WHITE)));
            }

            //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
            return bitmap != null && bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(filePath));
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 在二维码中间添加Logo图案
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }

        if (logo == null) {
            return src;
        }

        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }

        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);

            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }

        return bitmap;
    }

    /**
     * 生成圆角图片
     *
     * @param bitmap 原图片
     * @return 圆角图片
     */
    public static Bitmap GetRoundedCornerBitmap(Context context, Bitmap bitmap) {
        try {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
            float roundPx = DeviceUtil.dp2px(context, 15);
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            canvas.drawBitmap(bitmap, src, rect, paint);
            return output;
        } catch (Exception e) {
            return bitmap;
        }
    }

    /**
     * 获得添加边框了的Bitmap
     *
     * @param bm          原始图片Bitmap
     * @param borderWidth 边框宽度
     * @param color       边框颜色值
     * @return Bitmap 添加边框了的Bitmap
     */
    private static Bitmap drawBitmapBorder(Bitmap bm, int borderWidth, int color) {
        //防止空指针异常
        if (bm == null) {
            return null;
        }

        // 原图片的宽高
        final int bigW = bm.getWidth();
        final int bigH = bm.getHeight();

        // 重新定义大小
        int newW = bigW + borderWidth * 2;
        int newH = bigH + borderWidth * 2;

        // 绘原图
        Bitmap newBitmap = Bitmap.createBitmap(newW, newH, Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        Paint p = new Paint();
        p.setColor(color);
        canvas.drawRect(new Rect(0, 0, newW, newH), p);

        // 绘边框
        canvas.drawBitmap(bm, (newW - bigW - 2 * borderWidth) / 2 + borderWidth, (newH - bigH - 2 * borderWidth) / 2 + borderWidth, null);
        canvas.save();
        canvas.restore();

        return newBitmap;
    }

}
