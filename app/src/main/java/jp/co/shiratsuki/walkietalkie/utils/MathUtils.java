package jp.co.shiratsuki.walkietalkie.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.Random;

/**
 * 数字转换工具类
 * Created at 2018/11/28 13:51
 *
 * @author LiYuliang
 * @version 1.0
 */

public class MathUtils {

    static String[] hexStr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    /**
     * 二进制转十六进制
     *
     * @param binary 二进制数据
     * @return 十六进制数据
     */
    public static String binary2hex(String binary) {
        int length = binary.length();
        int temp = length % 4;
        // 每四位2进制数字对应一位16进制数字
        // 补足4位
        if (temp != 0) {
            for (int i = 0; i < 4 - temp; i++) {
                binary = "0" + binary;
            }
        }
        // 重新计算长度
        length = binary.length();
        StringBuilder sb = new StringBuilder();
        // 每4个二进制数为一组进行计算
        for (int i = 0; i < length / 4; i++) {
            int num = 0;
            // 将4个二进制数转成整数
            for (int j = i * 4; j < i * 4 + 4; j++) {
                num <<= 1;// 左移
                num |= (binary.charAt(j) - '0');// 或运算
            }
            // 直接找到该整数对应的16进制，这里不用switch来做
            sb.append(hexStr[num]);
        }
        return sb.toString();
    }

    /**
     * 把16进制字符串转换成字节数组
     *
     * @param hex 16进制数
     * @return 字节数组
     */
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     * 把字节数组转换成16进制字符串
     *
     * @param bArray 字节数组
     * @param length 有效数组长度
     * @return 16进制字符串
     */
    public static String bytesToHexString(byte[] bArray, int length) {
        StringBuilder sb = new StringBuilder(bArray.length);
        String sTemp;
        for (int i = 0; i < length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 把字节数组转换成16进制字符串
     *
     * @param bArray 字节数组
     * @return 16进制字符串
     */
    public static String bytesToHexString(byte[] bArray) {
        StringBuilder sb = new StringBuilder(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 把字节数组转换为对象
     *
     * @param bytes 字节数组
     * @return 对象
     * @throws IOException            输入输出异常
     * @throws ClassNotFoundException 类未找到异常
     */
    public static Object bytesToObject(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream oi = new ObjectInputStream(in);
        Object o = oi.readObject();
        oi.close();
        return o;
    }

    /**
     * 把可序列化对象转换成字节数组
     *
     * @param s 序列化的对象
     * @return 字节数组
     * @throws IOException 输入输出异常
     */
    public static byte[] objectToBytes(Serializable s) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream ot = new ObjectOutputStream(out);
        ot.writeObject(s);
        ot.flush();
        ot.close();
        return out.toByteArray();
    }

    public static String objectToHexString(Serializable s) throws IOException {
        return bytesToHexString(objectToBytes(s));
    }

    public static Object hexStringToObject(String hex) throws IOException, ClassNotFoundException {
        return bytesToObject(hexStringToByte(hex));
    }

    /**
     * 字符串转16进制编码字符串
     *
     * @param param 字符串
     * @return 16进制编码字符串
     */
    public static String getHexStr(String param) {
        byte[] bytes = param.getBytes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toHexString(bytes[i]));
        }
        System.out.println(sb.toString().toUpperCase());
        return sb.toString().toUpperCase();
    }

    /**
     * 16进制字符串编码转字符串
     *
     * @param s
     * @return
     */
    public static String getStringHex(String s) {
        byte[] bytes = new byte[s.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            try {
                bytes[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(bytes, "utf-8");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    /**
     * BCD码转为10进制串(阿拉伯数据)
     *
     * @param bytes BCD码
     * @return 10进制串
     */
    public static String bcd2Str(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
            temp.append((byte) (bytes[i] & 0x0f));
        }
        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1) : temp.toString();
    }

    /**
     * 10进制串转为BCD码
     *
     * @param asc 10进制串
     * @return BCD码
     */
    public static byte[] str2Bcd(String asc) {
        int len = asc.length();
        int mod = len % 2;

        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }

        byte abt[] = new byte[len];
        if (len >= 2) {
            len = len / 2;
        }

        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j, k;

        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }

            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }

            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }

    /**
     * @函数功能: BCD码转ASC码
     * @输入参数: BCD串
     * @输出结果: ASC码
     */
//    public static String BCD2ASC(byte[] bytes) {
//        StringBuffer temp = new StringBuffer(bytes.length * 2);
//        for (int i = 0; i < bytes.length; i++) {
//            int h = ((bytes[i] & 0xf0) >>> 4);
//            int l = (bytes[i] & 0x0f);
//            temp.append(BToA[h]).append(BToA[l]);
//        }
//        return temp.toString();
//    }

    /**
     * MD5加密字符串，返回加密后的16进制字符串
     *
     * @param origin
     * @return
     */
    public static String MD5EncodeToHex(String origin) {
        return bytesToHexString(MD5Encode(origin));
    }

    /**
     * MD5加密字符串，返回加密后的字节数组
     *
     * @param origin
     * @return
     */
    public static byte[] MD5Encode(String origin) {
        return MD5Encode(origin.getBytes());
    }

    /**
     * MD5加密字节数组，返回加密后的字节数组
     *
     * @param bytes
     * @return
     */
    public static byte[] MD5Encode(byte[] bytes) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            return md.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * double数据不显示为科学计数法
     *
     * @param d 原始数据
     * @return String型
     */
    public static String getOriginNumber(double d) {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(false);
        return numberFormat.format(d);
    }

    /**
     * float数据不显示为科学计数法
     *
     * @param f 原始数据
     * @return String型
     */
    public static String getOriginNumber(float f) {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(false);
        return numberFormat.format(f);
    }

    /**
     * 获取double类型数有几个小数位
     *
     * @param d 原始数据
     * @return 小数位个数
     */
    public static int getDecimalDigits(double d) {
        int i = 0;
        if (d == 0) {
            i = 0;
        } else {
            d = Math.abs(d);
            while (d < 1) {
                d *= 10;
                i++;
            }
        }
        return i;
    }

    public static int HexS1ToInt(char ch) {
        if ('a' <= ch && ch <= 'f') {
            return ch - 'a' + 10;
        }
        if ('A' <= ch && ch <= 'F') {
            return ch - 'A' + 10;
        }
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        throw new IllegalArgumentException(String.valueOf(ch));
    }

    public static int hexS2ToInt(String s) {
        int r;
        char a[] = s.toCharArray();
        r = HexS1ToInt(a[0]) * 16 + HexS1ToInt(a[1]);
        return r;
    }

    /**
     * 位数不够补零（左补0）
     *
     * @param str       原数字字符串
     * @param strLength 最终需要的长度
     * @return 补0后的数字字符串
     */
    public static String addZeroForLeft(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuilder sb = new StringBuilder(str);
                sb.insert(0, "0");
                str = sb.toString();
                strLen = str.length();
            }
        } else {
            //长度够则截取最后的strLength位
            str = str.substring(strLen - strLength, strLen);
        }
        return str;
    }

    /**
     * 位数不够补零（右补0）
     *
     * @param str       原数字字符串
     * @param strLength 最终需要的长度
     * @return 补0后的数字字符串
     */
    public static String addZeroForRight(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuilder sb = new StringBuilder(str);
                sb.insert(strLen, "0");
                str = sb.toString();
                strLen = str.length();
            }
        } else {
            //长度够则截取最前的strLength位
            str = str.substring(0, strLength);
        }
        return str;
    }

    /**
     * 格式化浮点型数据
     *
     * @param f      原数据（Float）
     * @param length 需要保留的小数点位数
     * @return 返回值
     */
    public static float formatFloat(float f, int length) {
        BigDecimal b = new BigDecimal(f);
        return b.setScale(length, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * 格式化浮点型数据
     *
     * @param f      原数据（Double）
     * @param length 需要保留的小数点位数
     * @return 返回值
     */
    public static double formatDouble(double f, int length) {
        BigDecimal b = new BigDecimal(f);
        return b.setScale(length, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 将String转化为byte[]数组
     *
     * @param arg 需要转换的String对象
     * @return 转换后的byte[]数组
     */
    public static byte[] toByteArray(String arg) {
        if (arg != null) {
            // 1.先去除String中的' '，然后将String转换为char数组
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (char c : array) {
                if (c != ' ') {
                    NewArray[length] = c;
                    length++;
                }
            }
            // 将char数组中的值转成一个实际的十进制数组
            int EvenLength = (length % 2 == 0) ? length : length + 1;
            if (EvenLength != 0) {
                int[] data = new int[EvenLength];
                data[EvenLength - 1] = 0;
                for (int i = 0; i < length; i++) {
                    if (NewArray[i] >= '0' && NewArray[i] <= '9') {
                        data[i] = NewArray[i] - '0';
                    } else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
                        data[i] = NewArray[i] - 'a' + 10;
                    } else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
                        data[i] = NewArray[i] - 'A' + 10;
                    }
                }
                /* 将 每个char的值每两个组成一个16进制数据 */
                byte[] byteArray = new byte[EvenLength / 2];
                for (int i = 0; i < EvenLength / 2; i++) {
                    byteArray[i] = (byte) (data[i * 2] * 16 + data[i * 2 + 1]);
                }
                return byteArray;
            }
        }
        return new byte[]{};
    }

    /**
     * 将String转化为byte[]数组
     *
     * @param arg 需要转换的String对象
     * @return 转换后的byte[]数组
     */
    private byte[] toByteArray2(String arg) {
        if (arg != null) {
            // 1.先去除String中的' '，然后将String转换为char数组
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (char c : array) {
                if (c != ' ') {
                    NewArray[length] = c;
                    length++;
                }
            }
            NewArray[length] = 0x0D;
            NewArray[length + 1] = 0x0A;
            length += 2;

            byte[] byteArray = new byte[length];
            for (int i = 0; i < length; i++) {
                byteArray[i] = (byte) NewArray[i];
            }
            return byteArray;

        }
        return new byte[]{};
    }

    /**
     * 获取数量
     *
     * @param original 原始数量
     * @param max      最大显示数量
     * @return 例如：原始20，最大99，显示20；原始120，最大99，显示99+；
     */
    public static String getNewCount(int original, int max) {
        if (original > max) {
            return max + "+";
        } else {
            return String.valueOf(original);
        }
    }

    /**
     * 获取n位长度的数值型字符串
     *
     * @param strLength 长度
     * @return
     */
    public static String getRandomString(int strLength) {
        String sourceChar = "0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strLength; i++) {
            sb.append(sourceChar.charAt(random.nextInt(sourceChar.length())));
        }
        return sb.toString();
    }
}
