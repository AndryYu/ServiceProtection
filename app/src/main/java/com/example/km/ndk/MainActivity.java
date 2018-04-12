package com.example.km.ndk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.km.ndk.jni.NativeJNI;
import com.example.km.ndk.job.JobSchedulerManager;
import com.example.km.ndk.service.DaemonService;
import com.example.km.ndk.service.UploadService;
import com.example.km.ndk.util.VersionUtil;

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
                initData();
            }
        });
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
}
