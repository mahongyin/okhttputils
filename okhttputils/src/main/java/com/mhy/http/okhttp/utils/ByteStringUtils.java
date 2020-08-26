package com.mhy.http.okhttp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created By Mahongyin
 * Date    2020/8/26 15:43
 */
public class ByteStringUtils {


    /**
     * 二进制数据编码为BASE64字符串
     *
     * @param bytes
     * @return
     * @throws Exception
     */
    public static String encode64(final byte[] bytes) {
        return new String(Base64.encode(bytes, Base64.DEFAULT));
    }

    public static byte[] decode64(final byte[] bytes) {
        return Base64.encode(bytes, Base64.DEFAULT);
    }

    public static byte[] decode64(final String string) {
        return Base64.encode(string.getBytes(), Base64.DEFAULT);
    }

    //将文件转换成Byte数组
    public static byte[] file2Bytes(String pathStr) {
        File file = new File(pathStr);
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            byte[] data = bos.toByteArray();
            bos.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //将Byte数组转换成文件
    public static File bytes2File(byte[] bytes, String outfilePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(outfilePath);
            if (!dir.exists() && dir.isDirectory()) {// 判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(outfilePath + "\\" + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    /**
     * 将图片转换成Base64编码的字符串
     */
    public static String imageToBase64(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try {
            is = new FileInputStream(path);
            //创建一个字符流大小的数组。
            data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用默认的编码格式进行编码
            result = Base64.encodeToString(data, Base64.NO_CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }

    /**
     * 将Base64编码转换为图片
     *
     * @param base64Str
     * @param path
     * @return true
     */
    public static boolean base64ToFile(String base64Str, String path) {
        byte[] data = Base64.decode(base64Str, Base64.NO_WRAP);
        for (int i = 0; i < data.length; i++) {
            if (data[i] < 0) {
                //调整异常数据
                data[i] += 256;
            }
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(path);
            os.write(data);
            os.flush();
            os.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Bitmap inputStream2Bitmap(InputStream ins, int fileSize) {
        if (ins == null) {
            return null;
        }
        byte[] b;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int size = -1;
            int len = 0;// 已经接收长度
            size = ins.read(buffer);
            while (size != -1) {
                len = len + size;//
                bos.write(buffer, 0, size);
                if (fileSize == len) {// 接收完毕
                    break;
                }
                size = ins.read(buffer);
            }
            b = bos.toByteArray();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return null;
    }

    public static Bitmap bytes2Bitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
