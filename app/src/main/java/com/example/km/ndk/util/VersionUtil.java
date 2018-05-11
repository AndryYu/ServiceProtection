package com.example.km.ndk.util;

import android.os.Build;

/**
 * Created by KM-ZhangYufei on 2018/3/30.
 */

public class VersionUtil {

    /**
     * <p>isBelowLOLLIPOP</p>
     * @return
     * @Description  判断当前版本是否低于android 5.0
     */
    public static boolean isBelowLOLLIPOP(){
        // API< 21
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * <p>isBelowN</p>
     * @return
     * @Description 判断当前版本是否低于android  7.0
     */
    public static boolean isBelowN(){
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.N;
    }

    /**
     * <p>isUpperOrea</p>
     * @return
     * @Description  API版本是否大于等于android 8.0
     */
    public static boolean isUpperOrea(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }
}
