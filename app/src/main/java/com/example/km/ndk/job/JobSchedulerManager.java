package com.example.km.ndk.job;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import com.example.km.ndk.util.VersionUtil;

import static com.example.km.ndk.util.VersionUtil.isBelowLOLLIPOP;

/**
 * Created by KM-ZhangYufei on 2018/3/30.
 */
public class JobSchedulerManager {

    private static final int JOB_ID = 1;
    private static JobSchedulerManager mJobManager;
    private JobScheduler mJobScheduler;
    private Context mContext;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private JobSchedulerManager(Context ctxt){
        this.mContext = ctxt;
        mJobScheduler = (JobScheduler)ctxt.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    public  static JobSchedulerManager getJobSchedulerInstance(Context ctxt){
        if(mJobManager == null){
            mJobManager = new JobSchedulerManager(ctxt);
        }
        return mJobManager;
    }

    /**
     * <p>startJobScheduler</p>
     * @Description  启动JobScheduler
     */
    public void startJobScheduler(){
        // 如果JobService已经启动或API<21，返回
        if(KeepJobService.isJobServiceAlive() || isBelowLOLLIPOP()){
            return;
        }
        // 构建JobInfo对象，传递给JobSchedulerService
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, new ComponentName(mContext, KeepJobService.class));
        // 设置每30秒执行一下任务(手机锁屏时间一般30秒)
        if(VersionUtil.isBelowN()) {
            builder.setPeriodic(1000 * 30);
        }else{
            builder.setPeriodic(JobInfo.getMinPeriodMillis(), JobInfo.getMinFlexMillis());
        }
        // 设置设备重启时，执行该任务
        builder.setPersisted(true);
        // 当插入充电器，执行该任务
        builder.setRequiresCharging(true);
        JobInfo info = builder.build();
        //开始定时执行该系统任务
        mJobScheduler.schedule(info);
    }

    /**
     * <p>stopJobScheduler</p>
     * @Description  结束JobScheduler
     */
    public void stopJobScheduler(){
        if(isBelowLOLLIPOP())
            return;
        mJobScheduler.cancelAll();
    }
}
