package com.example.km.ndk.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.km.ndk.R;
import com.example.km.ndk.data.Constants;

/**循环播放一段无声音频，以提升进程优先级
 *
 * Created by KM-ZhangYufei on 2018/3/30.
 */

public class PlayerMusicService extends Service {

    private final static String TAG = "PlayerMusicService";
    private MediaPlayer mMediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(Constants.DEBUG)
            Log.d(TAG,TAG+"---->onCreate,启动服务");
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.silent);
        mMediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(mMediaPlayer != null){
                    if(Constants.DEBUG)
                        Log.d(TAG,"启动后台播放音乐");
                    mMediaPlayer.start();
                }
            }
        }).start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMediaPlayer != null){
            if(Constants.DEBUG)
                Log.d(TAG,"关闭后台播放音乐");
            mMediaPlayer.stop();
        }
        if(Constants.DEBUG)
            Log.d(TAG,TAG+"---->onCreate,停止服务");
        // 重启自己
        Intent intent = new Intent(getApplicationContext(),PlayerMusicService.class);
        startService(intent);
    }
}
