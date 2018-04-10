package com.example.km.ndk.jni;

import com.example.km.ndk.strategy.base.IDaemonStrategy;

/**
 * Created by KM-ZhangYufei on 2018/4/2.
 */

public class NativeJNI {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    /**
     * <p>createWatcher</p>
     *
     * @param userId     当前进程的用户ID,子进程重启当前进程时需要用到当前进程的用户ID.
     * @return 如果子进程创建成功返回true，否则返回false
     * @Descripiton  创建一个监视子进程.
     */
    public native boolean createWatcher(String userId);

    /**
     * <p>doDaemon20</p>
     * @param pkgName
     * @param svcName
     * @param daemonPath
     * @Description api版本21以下-->双管道进程守护
     */
    public native void doDaemon20(String pkgName, String svcName, String daemonPath);

    /**
     * <p>doDaemon21</p>
     * @param indicatorSelfPath
     * @param indicatorDaemonPath
     * @param observerSelfPath
     * @param observerDaemonPath
     * @Description  api版本21及以上-->双管道进程守护
     */
    public native void doDaemon21(String indicatorSelfPath, String indicatorDaemonPath, String observerSelfPath, String observerDaemonPath);

    /**
     * <p>onDaemonDead</p>
     * @Description 当守护进程挂掉后，native回调这个java方法
     */
    protected void onDaemonDead(){
        IDaemonStrategy.Fetcher.fetchStrategy().onDaemonDead();
    }
}
