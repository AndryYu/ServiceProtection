package com.example.km.ndk.strategy;

import android.annotation.SuppressLint;
import android.app.Activity;
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

public class DaemonStrategy23 extends BaseDaemonStrategy {


    @Override
    public boolean onInitialization(Context context) {
        return initAPI21(context);
    }

    @SuppressLint("Recycle")// when process dead, we should save time to restart and kill self, don`t take a waste of time to recycle
    private void initBroadcastParcel(Context context, String broadcastName){
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(context.getPackageName(), broadcastName);
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

        mServiceData = Parcel.obtain();
        mServiceData.writeInterfaceToken("android.app.IActivityManager");
        mServiceData.writeStrongBinder(null);
        intent.writeToParcel(mServiceData, 0);
        mServiceData.writeString(intent.resolveTypeIfNeeded(context.getContentResolver()));
        mServiceData.writeStrongBinder(null);
        mServiceData.writeInt(Activity.RESULT_OK);
        mServiceData.writeString(null);
        mServiceData.writeBundle(null);
        mServiceData.writeString(null);
        mServiceData.writeInt(-1);
        mServiceData.writeInt(0);
        mServiceData.writeInt(0);
        mServiceData.writeInt(0);
    }

    @Override
    public void onPersistentCreate(final Context context, DaemonConfigurations configs) {
        initAmsBinder();
        initBroadcastParcel(context, configs.DAEMON_ASSISTANT_CONFIG.RECEIVER_NAME);
        startByAmsBinder(Constants.BROADCAST_INTENT_TRANSACTION);

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

        startIntent(context, configs.PERSISTENT_CONFIG.SERVICE_NAME);
        if(configs.LISTENER != null){
            this.mConfigs = configs;
            configs.LISTENER.onPersistentStart(context);
        }
    }

    @Override
    public void onDaemonAssistantCreate(final Context context, DaemonConfigurations configs) {
        initAmsBinder();
        initBroadcastParcel(context, configs.PERSISTENT_CONFIG.RECEIVER_NAME);
        startByAmsBinder(Constants.BROADCAST_INTENT_TRANSACTION);

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
        startIntent(context, configs.DAEMON_ASSISTANT_CONFIG.SERVICE_NAME);
        if(configs.LISTENER != null){
            this.mConfigs = configs;
            configs.LISTENER.onDaemonAssistantStart(context);
        }
    }

    @Override
    public void onDaemonDead() {
        if(startByAmsBinder(Constants.BROADCAST_INTENT_TRANSACTION)){
            if(mConfigs != null && mConfigs.LISTENER != null){
                mConfigs.LISTENER.onWatchDaemonDaed();
            }
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}
