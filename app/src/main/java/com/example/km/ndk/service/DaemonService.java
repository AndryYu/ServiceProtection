package com.example.km.ndk.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.km.ndk.R;
import com.example.km.ndk.data.Constants;
import com.example.km.ndk.jni.NativeJNI;
import com.example.km.ndk.util.VersionUtil;

/**
 * 前台Service，使用startForeground
 * 这个Service尽量要轻，不要占用过多的系统资源，否则系统在资源紧张时，照样会将其杀死
 *
 * Created by KM-ZhangYufei on 2018/3/30.
 */

public class DaemonService extends Service {

    private static final String TAG = "DaemonService";
    public static final int NOTICE_ID = 20170426;
    public static final String id = "channel_1";
    public static final String name = "channel_name_1";

    private Notification.Builder mBuilder;
    private NotificationCompat.Builder mOBuilder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(VersionUtil.isUpperOrea()){
            initOreaNotification();
        }else{
            initNotification();
        }

        if(VersionUtil.isBelowLOLLIPOP()){
            try {
                NativeJNI jni = new NativeJNI();
                Log.i("Android-Zhang", "ndk守护进程开始");
                jni.createWatcher(String.valueOf(Process.myUid()));
                Log.e("Android-Zhang", "守护主线程");
                //jni.connectToMonitor();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Android-Zhang", e.getMessage());
            }
        }
    }

    /**
     * <p>initNotification</p>
     * @Description 初始化Notification
     */
    private void initNotification(){
        //获取一个Notification构造器
        mBuilder = new Notification.Builder(this.getApplicationContext());
        mBuilder.setContentTitle("服务已开启") // 设置下拉列表里的标题
                .setContentText("微信需运行于前台窗口，方可定时刷新账单") // 设置上下文内容
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.app_icon))// 设置下拉列表中的图标(大图标)
                .setSmallIcon(R.mipmap.app_icon) // 设置状态栏内的小图标
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        Notification notification = mBuilder.build(); // 获取构建好的Notification
        //notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
        //参数一：唯一的通知标识；参数二：通知消息。
        startForeground(NOTICE_ID, notification);// 开始前台服务
    }

    /**
     * <p>initOreaNotification</p>
     * @Description api 版本26及以上
     */
    @TargetApi(Build.VERSION_CODES.O)
    private void initOreaNotification(){
        NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        //获取一个Notification构造器
        mOBuilder = new NotificationCompat.Builder(this.getApplicationContext(),id);
        mOBuilder.setContentTitle("服务已开启") // 设置下拉列表里的标题
                .setContentText("微信需运行于前台窗口，方可定时刷新账单") // 设置上下文内容
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.app_icon))// 设置下拉列表中的图标(大图标)
                .setSmallIcon(R.mipmap.app_icon) // 设置状态栏内的小图标
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        Notification notification = mOBuilder.build(); // 获取构建好的Notification
        //notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
        //参数一：唯一的通知标识；参数二：通知消息。

        //manager.notify(NOTICE_ID, notification);
        startForeground(NOTICE_ID, notification);// 开始前台服务
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationManager mManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mManager.cancel(NOTICE_ID);

        if(Constants.DEBUG)
            Log.d(TAG,"DaemonService---->onDestroy，前台service被杀死");
        // 重启自己
        Intent intent = new Intent(getApplicationContext(),DaemonService.class);
        startService(intent);
    }
}
