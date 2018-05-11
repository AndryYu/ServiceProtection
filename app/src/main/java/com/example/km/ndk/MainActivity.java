package com.example.km.ndk;

import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.km.ndk.account.AccountHelper;
import com.example.km.ndk.jni.NativeJNI;
import com.example.km.ndk.job.JobSchedulerManager;
import com.example.km.ndk.service.DaemonService;
import com.example.km.ndk.service.PlayerMusicService;
import com.example.km.ndk.service.UploadService;
import com.example.km.ndk.util.VersionUtil;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private NativeJNI nativeJNI;
    // JobService，执行系统任务
    private JobSchedulerManager mJobManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, UploadService.class);
        startService(intent);
        nativeJNI = new NativeJNI();
        TextView tv =  findViewById(R.id.sample_text);
        tv.setText(nativeJNI.stringFromJNI());
        Button btnClick = findViewById(R.id.btn_startservice);
        btnClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hasNLAuth();
                initData();
            }
        });

        //账号同步
        AccountHelper.addAccount(this);
        AccountHelper.autoSync();

        //后台播放一段
        Intent musicIntent = new Intent(this, PlayerMusicService.class);
        startService(musicIntent);
    }

    /**
     * <p>initData</p>
     * @Description  初始化数据
     */
    private void initData(){
        if( !VersionUtil.isBelowLOLLIPOP()){
            mJobManager = JobSchedulerManager.getJobSchedulerInstance(this);
            mJobManager.startJobScheduler();
        }
        Intent intent = new Intent(this, DaemonService.class);
        startService(intent);
    }

    /**
     * <p>hasNLAuth</p>
     * @Description  是否有监听通知栏权限
     */
    private void hasNLAuth(){
        Set<String> pckNames = NotificationManagerCompat.getEnabledListenerPackages(getApplicationContext());
        if (pckNames.contains(getPackageName())) {
            //Toast.makeText(this, "有权限", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "无权限", Toast.LENGTH_LONG).show();

            String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
            startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }
    }
}
