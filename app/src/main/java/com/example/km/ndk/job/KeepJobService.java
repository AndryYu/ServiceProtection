package com.example.km.ndk.job;

import android.annotation.TargetApi;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.km.ndk.MainActivity;
import com.example.km.ndk.data.Constants;
import com.example.km.ndk.service.DaemonService;
import com.example.km.ndk.util.SystemUtil;

/**
 * Created by KM-ZhangYufei on 2018/3/30.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class KeepJobService extends JobService {

    private static final int MESSAGE_ID_TASK = 0x01;
    // 告知编译器，这个变量不能被优化
    private volatile static Service mService = null;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            Log.i("Android-Zhang", "JobService定时检查");
            if(!SystemUtil.isAppAlive(KeepJobService.this, Constants.PACKAGE_NAME)){
                Intent intent = new Intent(getApplicationContext(), DaemonService.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            jobFinished((JobParameters) message.obj, false);
            return true;
        }
    });

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        mService = this;
        Message msg = Message.obtain(mHandler, MESSAGE_ID_TASK, jobParameters);
        mHandler.sendMessage(msg);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        mHandler.removeMessages(MESSAGE_ID_TASK);
        return false;
    }

    /**
     * <p>isJobServiceAlive</p>
     * @return
     * @Description  判断JobService服务是否存活
     */
    public static boolean isJobServiceAlive(){
        return mService != null;
    }
}
