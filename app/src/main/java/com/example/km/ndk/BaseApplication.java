package com.example.km.ndk;

import android.app.Application;
import android.content.Context;

import com.example.km.ndk.receiver.Receiver1;
import com.example.km.ndk.receiver.Receiver2;
import com.example.km.ndk.service.DaemonService;
import com.example.km.ndk.service.UploadService;
import com.example.km.ndk.strategy.DaemonClient;
import com.example.km.ndk.strategy.base.DaemonConfigurations;

/**
 * Created by KM-ZhangYufei on 2018/4/9.
 */

public class BaseApplication extends Application {


    private DaemonClient mClient;

    /**
     * <p>createConfigurations</p>
     * @return
     * @Descriptin  创建配置数据
     */
    private DaemonConfigurations createConfigurations(){
        DaemonConfigurations.DaemonConfiguration conf1 = new DaemonConfigurations.DaemonConfiguration(
                "com.example.km.ndk:process1",
                UploadService.class.getCanonicalName(),
                Receiver1.class.getCanonicalName());
        DaemonConfigurations.DaemonConfiguration conf2 = new DaemonConfigurations.DaemonConfiguration(
                "com.example.km.ndk:process2",
                DaemonService.class.getCanonicalName(),
                Receiver2.class.getCanonicalName());
        DaemonConfigurations.DaemonListener listener = new MyDaemonListener();
        return new DaemonConfigurations(conf1, conf2, listener);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mClient = new DaemonClient(createConfigurations());
        mClient.onAttachBaseContext(base);
    }

    class MyDaemonListener implements DaemonConfigurations.DaemonListener{
        @Override
        public void onPersistentStart(Context context) {
        }

        @Override
        public void onDaemonAssistantStart(Context context) {
        }

        @Override
        public void onWatchDaemonDaed() {
        }
    }
}
