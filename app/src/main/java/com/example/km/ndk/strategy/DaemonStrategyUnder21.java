package com.example.km.ndk.strategy;

import android.app.AlarmManager;
import android.content.Context;
import android.os.SystemClock;

import com.example.km.ndk.data.Constants;
import com.example.km.ndk.jni.NativeJNI;
import com.example.km.ndk.strategy.base.BaseDaemonStrategy;
import com.example.km.ndk.strategy.base.DaemonConfigurations;

import java.io.File;

/**
 * Created by KM-ZhangYufei on 2018/4/9.
 */

public class DaemonStrategyUnder21 extends BaseDaemonStrategy {


    @Override
    public boolean onInitialization(Context context) {
        return initAPI20(context, true);
    }

    @Override
    public void onPersistentCreate(final Context context, final DaemonConfigurations configs) {
        initAlarm(context, configs.DAEMON_ASSISTANT_CONFIG.SERVICE_NAME);
        Thread t = new Thread(){
            public void run() {
                File binaryFile = new File(context.getDir(Constants.BINARY_DEST_DIR_NAME, Context.MODE_PRIVATE),
                        Constants.BINARY_FILE_NAME);
                new NativeJNI().doDaemon20(
                        context.getPackageName(),
                        configs.DAEMON_ASSISTANT_CONFIG.SERVICE_NAME,
                        binaryFile.getAbsolutePath());
            };
        };
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();

        if(configs.LISTENER != null){
            configs.LISTENER.onPersistentStart(context);
        }
    }

    @Override
    public void onDaemonAssistantCreate(Context context, DaemonConfigurations configs) {
        startIntent(context, configs.PERSISTENT_CONFIG.SERVICE_NAME);
        if(configs.LISTENER != null){
            configs.LISTENER.onWatchDaemonDaed();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onDaemonDead() {
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 100, mPendingIntent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
