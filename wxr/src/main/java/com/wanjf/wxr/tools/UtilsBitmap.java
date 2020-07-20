package com.wanjf.wxr.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class UtilsBitmap {

    /**
     * 压缩到指定的姿势。此过程很耗时，建议在工作线程中使用
     * @param bitmap 位图
     * @param maxSize 最大不大于 (maxSize)k
     */
    public static byte[] compressBitmap(Bitmap bitmap, int maxSize) {
        //根据地址获取bitmap
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int quality = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        // 循环判断如果压缩后图片是否大于500kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > maxSize) {
            // 重置baos即清空baos
            baos.reset();
            // 每次都减少10
            quality -= 10;
            // 这里压缩quality，把压缩后的数据存放到baos中
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }
        return baos.toByteArray();
    }

    /**
     *
     * @param bytes
     * @return
     */
    private static Bitmap byteToBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * File转为bitmap
     * @param file
     * @return
     */
    public static byte[] fileToBitmap(File file) {
        byte[] buffer = null;
        if (!file.exists()) {
            return null;
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;

    }


    public static byte[] bitmapToByte(Bitmap bitmap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将图片写入到磁盘
     * @param img      图片数据流
     * @param fileName 文件保存时的名称
     */
    public static void writeFileToDisk(Context context, byte[] img, String fileName) {
        try {
            File file = new File(context.getExternalFilesDir("images") + File.separator + fileName);
            if (!file.exists()) {
                boolean b = file.createNewFile();
                if (!b) {
                    return;
                }
            }
            FileOutputStream fops = new FileOutputStream(file);
            fops.write(img);
            fops.flush();
            fops.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
