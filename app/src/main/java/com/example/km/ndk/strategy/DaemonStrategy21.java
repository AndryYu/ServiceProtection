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

public class DaemonStrategy21 extends BaseDaemonStrategy{


    @Override
    public boolean onInitialization(Context context) {
        return initAPI21(context);
    }

    @Override
    public void onPersistentCreate(final Context context, DaemonConfigurations configs) {
        startIntent(context, configs.DAEMON_ASSISTANT_CONFIG.SERVICE_NAME);
        initAlarm(context, configs.PERSISTENT_CONFIG.SERVICE_NAME);

        Thread t = new Thread(){
            @Override
            public void run() {
                File indicatorDir = context.getDir(Constants.INDICATOR_DIR_NAME, Context.MODE_PRIVATE);
                new NativeJNI().doDaemon21(
                        new File(indicatorDir, Constants.INDICATOR_PERSISTENT_FILENAME).getAbsolutePath(),
                        new File(indicatorDir, Constants.INDICATOR_DAEMON_ASSISTANT_FILENAME).getAbsolutePath(),
                        new File(indicatorDir, Constants.OBSERVER_PERSISTENT_FILENAME).getAbsolutePath(),
                        new File(indicatorDir, Constants.OBSERVER_DAEMON_ASSISTANT_FILENAME).getAbsolutePath());
            }
        };
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();

        if(configs.LISTENER != null){
            this.mConfigs = configs;
            configs.LISTENER.onPersistentStart(context);
        }
    }

    @Override
    public void onDaemonAssistantCreate(final Context context, DaemonConfigurations configs) {
        startIntent(context, configs.PERSISTENT_CONFIG.SERVICE_NAME);
        initAlarm(context, configs.PERSISTENT_CONFIG.SERVICE_NAME);

        Thread t = new Thread(){
            public void run() {
                File indicatorDir = context.getDir(Constants.INDICATOR_DIR_NAME, Context.MODE_PRIVATE);
                new NativeJNI().doDaemon21(
                        new File(indicatorDir, Constants.INDICATOR_DAEMON_ASSISTANT_FILENAME).getAbsolutePath(),
                        new File(indicatorDir, Constants.INDICATOR_PERSISTENT_FILENAME).getAbsolutePath(),
                        new File(indicatorDir, Constants.OBSERVER_DAEMON_ASSISTANT_FILENAME).getAbsolutePath(),
                        new File(indicatorDir, Constants.OBSERVER_PERSISTENT_FILENAME).getAbsolutePath());
            };
        };
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();

        if(configs.LISTENER != null){
            this.mConfigs = configs;
            configs.LISTENER.onDaemonAssistantStart(context);
        }
    }

    @Override
    public void onDaemonDead() {
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 100, mPendingIntent);
        if(mConfigs != null && mConfigs.LISTENER != null){
            mConfigs.LISTENER.onWatchDaemonDaed();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
