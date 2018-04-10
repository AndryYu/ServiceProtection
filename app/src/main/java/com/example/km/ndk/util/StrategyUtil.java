package com.example.km.ndk.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by KM-ZhangYufei on 2018/4/4.
 */

public class StrategyUtil {

    /**
     * <p>createNewFile</p>
     * @param dirFile
     * @param fileName
     * @throws IOException
     * @Description  创建新文件
     */
    public static void createNewFile(File dirFile, String fileName) throws IOException {
        File file = new File(dirFile, fileName);
        if(!file.exists()){
            file.createNewFile();
        }
    }

    /**
     * <p>copyFile</p>
     * @param file
     * @param is
     * @param mode
     * @throws IOException
     * @throws InterruptedException
     * @Description  拷贝文件并且动态修改访问权限
     */
    public static void copyFile(File file, InputStream is, String mode) throws IOException, InterruptedException {
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        final String abspath = file.getAbsolutePath();
        final FileOutputStream out = new FileOutputStream(file);
        byte buf[] = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        is.close();
        Runtime.getRuntime().exec("chmod " + mode + " " + abspath).waitFor();
    }

    /**
     * <p>install</p>
     * @param context
     * @param destDirName
     * @param assetsDirName
     * @param filename
     * @return
     */
    public static boolean install(Context context, String destDirName, String assetsDirName, String filename) {
        File file = new File(context.getDir(destDirName, Context.MODE_PRIVATE), filename);
        if (file.exists()) {
            return true;
        }
        try {
            String assetsFilename = (TextUtils.isEmpty(assetsDirName) ? "" : (assetsDirName + File.separator)) + filename;
            AssetManager manager = context.getAssets();
            final InputStream is = manager.open(assetsFilename);
            StrategyUtil.copyFile(file, is, "700");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
