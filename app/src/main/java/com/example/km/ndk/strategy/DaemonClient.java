package com.example.km.ndk.strategy;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.km.ndk.strategy.base.DaemonConfigurations;
import com.example.km.ndk.strategy.base.IDaemonStrategy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by KM-ZhangYufei on 2018/4/9.
 */

public class DaemonClient {

    private final String DAEMON_PERMITTING_SP_FILENAME 	= "d_permit";
    private final String DAEMON_PERMITTING_SP_KEY 		= "permitted";
    private DaemonConfigurations mConfigurations;
    private BufferedReader mReader;

    public DaemonClient(DaemonConfigurations configurations){
        mConfigurations = configurations;
    }

    /**
     * <p>onAttachBaseContext</p>
     * @param context
     */
    public void onAttachBaseContext(Context context){
        if(!isDaemonPermitting(context) || mConfigurations == null){
            return ;
        }
        String processName = getProcessName();
        String packageName = context.getPackageName();
        if(processName.startsWith(mConfigurations.PERSISTENT_CONFIG.PROCESS_NAME)){
            IDaemonStrategy.Fetcher.fetchStrategy().onPersistentCreate(context, mConfigurations);
        }else if(processName.startsWith(mConfigurations.DAEMON_ASSISTANT_CONFIG.PROCESS_NAME)){
            IDaemonStrategy.Fetcher.fetchStrategy().onDaemonAssistantCreate(context, mConfigurations);
        }else if(processName.startsWith(packageName)){
            IDaemonStrategy.Fetcher.fetchStrategy().onInitialization(context);
        }
        if(mReader != null){
            try {
                mReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mReader = null;
        }
    }

    /**
     * <p>getProcessName</p>
     * @return
     * @Description  获取进程名字
     */
    private String getProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            mReader = new BufferedReader(new FileReader(file));
            return mReader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * <p>isDaemonPermitting</p>
     * @param context
     * @return
     * @Description 是否允许进程守护
     */
    private boolean isDaemonPermitting(Context context){
        SharedPreferences sp = context.getSharedPreferences(DAEMON_PERMITTING_SP_FILENAME, Context.MODE_PRIVATE);
        return sp.getBoolean(DAEMON_PERMITTING_SP_KEY, true);
    }

    /**
     * <p>setDaemonPermiiting</p>
     * @param context
     * @param isPermitting
     * @return
     * @Description  设置进程守护状态
     */
    protected boolean setDaemonPermiiting(Context context, boolean isPermitting) {
        SharedPreferences sp = context.getSharedPreferences(DAEMON_PERMITTING_SP_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(DAEMON_PERMITTING_SP_KEY, isPermitting);
        return editor.commit();
    }
}
