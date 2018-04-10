package com.example.km.ndk.strategy.base;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.example.km.ndk.data.Constants;
import com.example.km.ndk.util.StrategyUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import static com.example.km.ndk.util.StrategyUtil.install;

/**
 * Created by KM-ZhangYufei on 2018/4/9.
 */

public abstract class BaseDaemonStrategy implements IDaemonStrategy {

    protected AlarmManager mAlarmManager;
    protected PendingIntent mPendingIntent;
    protected DaemonConfigurations 	mConfigs;
    protected IBinder mRemote;
    protected Parcel mServiceData;

    /**
     * <p>initAPI21</p>
     * @param context
     * @return
     * @Description  api21版本以上的初始化
     */
    protected boolean initAPI21(Context context){
        File dirFile = context.getDir(Constants.INDICATOR_DIR_NAME, Context.MODE_PRIVATE);
        if(!dirFile.exists()){
            dirFile.mkdirs();
        }
        try {
            StrategyUtil.createNewFile(dirFile, Constants.INDICATOR_PERSISTENT_FILENAME);
            StrategyUtil.createNewFile(dirFile, Constants.INDICATOR_DAEMON_ASSISTANT_FILENAME);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    };

    /**
     * <p>initAPI20</p>
     * @param context
     * @return
     * api20版本及以下的初始化
     */
    protected boolean initAPI20(Context context, boolean isDist){
        String binaryDirName = null;
        if(isDist) {
            String abi = Build.CPU_ABI;
            if (abi.startsWith("armeabi-v7a")) {
                binaryDirName = "armeabi-v7a";
            } else if (abi.startsWith("x86")) {
                binaryDirName = "x86";
            } else {
                binaryDirName = "armeabi";
            }
        }
        return install(context, Constants.BINARY_DEST_DIR_NAME, binaryDirName, Constants.BINARY_FILE_NAME);
    }

    /**
     * <p>startIntent</p>
     * @param context
     * @param clsName
     * @Description 启动service
     */
    protected void startIntent(Context context, String clsName){
        Intent intent = new Intent();
        ComponentName component = new ComponentName(context.getPackageName(), clsName);
        intent.setComponent(component);
        context.startService(intent);
    }

    /**
     * <p>initAmsBinder</p>
     * @Description 通过反射获取AMN
     */
    protected void initAmsBinder(){
        Class<?> activityManagerNative;
        try {
            activityManagerNative = Class.forName("android.app.ActivityManagerNative");
            Object amn = activityManagerNative.getMethod("getDefault").invoke(activityManagerNative);
            Field mRemoteField = amn.getClass().getDeclaredField("mRemote");
            mRemoteField.setAccessible(true);
            mRemote = (IBinder) mRemoteField.get(amn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>initAlarm</p>
     * @param context
     * @param serviceName
     * @Description 取消Alarm定时器
     */
    protected void initAlarm(Context context, String serviceName){
        if(mAlarmManager == null){
            mAlarmManager = ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE));
        }
        if(mPendingIntent == null){
            Intent intent = new Intent();
            ComponentName component = new ComponentName(context.getPackageName(), serviceName);
            intent.setComponent(component);
            intent.setFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);
            mPendingIntent = PendingIntent.getService(context, 0, intent, 0);
        }
        mAlarmManager.cancel(mPendingIntent);
    }

    protected boolean startByAmsBinder(int intentFlag){
        try {
            if(mRemote == null || mServiceData == null){
                Log.e("Daemon", "REMOTE IS NULL or PARCEL IS NULL !!!");
                return false;
            }
            mRemote.transact(intentFlag, mServiceData, null, 0);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }
}
