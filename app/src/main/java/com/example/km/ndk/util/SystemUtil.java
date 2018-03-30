package com.example.km.ndk.util;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by KM-ZhangYufei on 2018/3/30.
 */

public class SystemUtil {

    /**
     * <p>isAppAlive</p>
     * @param context
     * @param packageName
     * @return
     * @Description  判断本应用是否存活
     */
    public static boolean isAppAlive(Context context, String packageName){
        boolean isAPPRunning = false;
        //获取activity管理对象
        ActivityManager mManger = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //获取所有正在运行的app
        List<ActivityManager.RunningAppProcessInfo> processList = mManger.getRunningAppProcesses();
        //遍历，进程名即包名
        for (ActivityManager.RunningAppProcessInfo appInfo:processList){
            if(packageName.equals(appInfo.processName)){
                isAPPRunning = true;
                break;
            }
        }

        return isAPPRunning;
    }
}
