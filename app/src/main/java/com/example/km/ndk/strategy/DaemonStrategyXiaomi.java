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

/**
 * Created by KM-ZhangYufei on 2018/4/9.
 */

public class DaemonStrategyXiaomi extends BaseDaemonStrategy {


    @Override
    public boolean onInitialization(Context context) {
        return  initAPI20(context, false);
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
    public void onPersistentCreate(final Context context, final DaemonConfigurations configs) {
        initAmsBinder();
        initServiceParcel(context, configs.DAEMON_ASSISTANT_CONFIG.SERVICE_NAME);
        Thread t = new Thread(){
            public void run() {
                File binaryFile = new File(context.getDir(Constants.BINARY_DEST_DIR_NAME, Context.MODE_PRIVATE), Constants.BINARY_FILE_NAME);
                new NativeJNI().doDaemon20(
                        context.getPackageName(),
                        configs.DAEMON_ASSISTANT_CONFIG.SERVICE_NAME,
                        binaryFile.getAbsolutePath());
            };
        };
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();

        if(configs.LISTENER != null){
            this.mConfigs = configs;
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
        if(startByAmsBinder(Constants.START_SERVICE_TRANSACTION)){
            if(mConfigs != null && mConfigs.LISTENER != null){
                mConfigs.LISTENER.onWatchDaemonDaed();
            }
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}
