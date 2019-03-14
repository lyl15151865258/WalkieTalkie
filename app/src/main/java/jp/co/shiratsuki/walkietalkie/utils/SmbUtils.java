package jp.co.shiratsuki.walkietalkie.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

/**
 * SMB工具类，用于访问局域网共享文件夹
 * Created at 2019/3/14 16:01
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class SmbUtils {

    // smbGet("smb://C1307890:Ivo123@10.20.2.33/CIMPublic/02 MES/SPC/Spc_Check_IVO_V1_00.ppt", "D:/ap_log");
    // smbPut("smb://C1307890:Ivo123@10.20.2.33/CIMPublic/", "D:/qra/fileUpload/SQL_JOIN.pptx");

    // 如果是无需密码的共享，则类似如下格式：smb://ip/sharefolder(例如：smb://192.168.0.77/test)
    // 如果需要用户名、密码，则类似如下格式：smb://username:password@ip/sharefolder(例如：smb://chb:123456@192.168.0.1/test)
    // String url="smb://192.168.5.55/";
    // SmbFile file = new SmbFile(url);

    // 从共享目录下载文件
    @SuppressWarnings("unused")
    public static void smbGet(String remoteUrl, String localDir) {
        InputStream in = null;
        OutputStream out = null;
        try {
            SmbFile remoteFile = new SmbFile(remoteUrl);

//            如果获取 SmbFile异常， 用下面方法登录
//	        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
//	                DOMAIN, USER_NAME, PASSWORD);
//	        path = NETWORK_FOLDER + fileName;
//	        sFile = new SmbFile(path, auth);

            if (remoteFile == null) {
                System.out.println("共享文件不存在");
                return;
            }
            String fileName = remoteFile.getName();
            File localFile = new File(localDir + File.separator + fileName);
            in = new BufferedInputStream(new SmbFileInputStream(remoteFile));
            out = new BufferedOutputStream(new FileOutputStream(localFile));
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                out.write(buffer);
                buffer = new byte[1024];
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 向共享目录上传文件
    public static void smbPut(String remoteUrl, String localFilePath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File localFile = new File(localFilePath);

            String fileName = localFile.getName();
            SmbFile remoteFile = new SmbFile(remoteUrl + "/" + fileName);
            in = new BufferedInputStream(new FileInputStream(localFile));
            out = new BufferedOutputStream(new SmbFileOutputStream(remoteFile));
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                out.write(buffer);
                buffer = new byte[1024];
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
