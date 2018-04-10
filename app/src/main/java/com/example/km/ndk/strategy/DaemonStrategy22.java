package com.example.km.ndk.strategy;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;

import com.example.km.ndk.data.Constants;
import com.example.km.ndk.jni.NativeJNI;
import com.example.km.ndk.strategy.base.BaseDaemonStrategy;
import com.example.km.ndk.strategy.base.DaemonConfigurations;

import java.io.File;

import static com.example.km.ndk.data.Constants.INDICATOR_PERSISTENT_FILENAME;

/**
 * Created by KM-ZhangYufei on 2018/4/9.
 */

public class DaemonStrategy22 extends BaseDaemonStrategy {


    @Override
    public boolean onInitialization(Context context) {
        return initAPI21(context);
    }

    @SuppressLint("Recycle")// when process dead, we should save time to restart and kill self, don`t take a waste of time to recycle
    private void initServiceParcel(Context context, String serviceName){
        Intent intent = new Intent();
        ComponentName component = new ComponentName(context.getPackageName(), serviceName);
        intent.setComponent(component);
        mServiceData = Parcel.obtain();
        mServiceData.writeInterfaceToken("android.app.IActivityManager");
        mServiceData.writeStrongBinder(null);
        intent.writeToParcel(mServiceData, 0);
        mServiceData.writeString(null);
        mServiceData.writeInt(0);
    }

    @Override
    public void onPersistentCreate(final Context context, DaemonConfigurations configs) {
        initAmsBinder();
        initServiceParcel(context, configs.DAEMON_ASSISTANT_CONFIG.SERVICE_NAME);
        startByAmsBinder(Constants.START_SERVICE_TRANSACTION);
        Thread t = new Thread(){
            public void run() {
                File indicatorDir = context.getDir(Constants.INDICATOR_DIR_NAME, Context.MODE_PRIVATE);
                new NativeJNI().doDaemon21(
                        new File(indicatorDir, Constants.INDICATOR_PERSISTENT_FILENAME).getAbsolutePath(),
                        new File(indicatorDir, Constants.INDICATOR_DAEMON_ASSISTANT_FILENAME).getAbsolutePath(),
                        new File(indicatorDir, Constants.OBSERVER_PERSISTENT_FILENAME).getAbsolutePath(),
                        new File(indicatorDir, Constants.OBSERVER_DAEMON_ASSISTANT_FILENAME).getAbsolutePath());
            };
        };
        t.start();

        if(configs.LISTENER != null){
            this.mConfigs = configs;
            configs.LISTENER.onPersistentStart(context);
        }
    }

    @Override
    public void onDaemonAssistantCreate(final Context context, DaemonConfigurations configs) {
        initAmsBinder();
        initServiceParcel(context, configs.PERSISTENT_CONFIG.SERVICE_NAME);
        startByAmsBinder(Constants.START_SERVICE_TRANSACTION);

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
        t.start();

        if(configs.LISTENER != null){
            this.mConfigs = configs;
            configs.LISTENER.onDaemonAssistantStart(context);
        }
    }

    @Override
    public void onDaemonDead() {
        if(startByAmsBinder(Constants.START_SERVICE_TRANSACTION)){
            if(mConfigs != null && mConfigs.LISTENER != null){
                mConfigs.LISTENER.onWatchDaemonDaed();
            }
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}
