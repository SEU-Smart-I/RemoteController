package com.example.controller;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class ImageBufferManager {

    Context context;

    public ImageBufferManager(Context context) {
        this.context = context;
    }

    public String imageCacheInitiate() {
        String path = context.getExternalFilesDir(null).toString()+"/temp/imagebuffer";
        System.out.println(path);

        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Toast.makeText(context, "Fail to create file: image buffer", Toast.LENGTH_SHORT).show();
            }
        }
        return path;
    }

    public void imageStoreInitiate() {
        //获取系统路径
        String savePath = getSDPath();
        System.out.println(savePath);
        savePath += "/SI Controller/image/";

        File cache = new File(savePath);
        if (!cache.exists()) {
            cache.mkdirs();
        }
    }

    public static void checkFileStatus(String savePath) {
        File file = new File(savePath);
        if (file.exists()) {
            file.delete();
        }
    }

    //图像本地保存路径获取
    public String getSDPath() {
        File sdDir = null;
        //判断sd卡是否存在
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取根目录
            Log.e("qq", "外部存储可用..." + sdDir.toString());
        }
        return sdDir.toString();
    }

    public int getFilesNumber(String string) {
        // TODO Auto-generated method stub 
        File file = new File(string);
        if (!file.exists()) {
            Toast.makeText(context, "The file path is invalid!", Toast.LENGTH_SHORT).show();
            return -1;
        }
        File[] files = file.listFiles();
        return files.length;
    }

    private int getImageFilesNumber(String string) {
        int i = 0;
        // TODO Auto-generated method stub 
        File file = new File(string);
        File[] files = file.listFiles();
        System.out.println("文件数量为："+files.length);
        for (int j = 0; j < files.length; j++) {
            String name = files[j].getName();
            if (files[j].isDirectory()) {
                String dirPath = files[j].toString().toLowerCase();
                System.out.println(dirPath);
                getImageFilesNumber(dirPath + "/");
            } else if (files[j].isFile() & name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".bmp") || name.endsWith(".gif") || name.endsWith(".jpeg")) {
                System.out.println("FileName===" + files[j].getName());
                i++;
            }
        }
        return i;
    }

    private void deleteImg() {
        Log.v("BaseActivity","deleteImg()");
        String img_path = context.getExternalFilesDir(null).toString() + "/temp/imagebuffer";
        Log.v("BaseActivity","img_path" + img_path);

        File file = new File(img_path);
        recursionDeleteFile(file);
    }

    public static void recursionDeleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            Log.v("RecursionDeleteFile","file.isDirectory()");
            File[] childFile = file.listFiles();
            Log.v("RecursionDeleteFile","childFile.length: " + childFile.length);
            if (childFile == null || childFile.length == 0) {
                file.delete(); //文件夹删除,每次缓存都需要新建文件夹
                System.out.println("111");
                return;
            }
            int count = 1;
            for (File f : childFile) {
                Log.v("RecursionDeleteFile","count: " + count++);
                recursionDeleteFile(f);
            }
            file.delete();//删除文件
        }
    }
}
