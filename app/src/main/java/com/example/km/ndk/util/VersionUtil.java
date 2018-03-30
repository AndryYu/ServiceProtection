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
}
