package com.example.km.ndk.data;

/**
 * Created by KM-ZhangYufei on 2018/4/4.
 */

public class Constants {
    //是否开启调试
    public static final boolean DEBUG = true;
    //当前应用的包名
    public static final String PACKAGE_NAME = "com.example.km.ndk";

    public final static String INDICATOR_DIR_NAME 					= "indicators";
    public final static String INDICATOR_PERSISTENT_FILENAME 		= "indicator_p";
    public final static String INDICATOR_DAEMON_ASSISTANT_FILENAME = "indicator_d";
    public final static String OBSERVER_PERSISTENT_FILENAME		= "observer_p";
    public final static String OBSERVER_DAEMON_ASSISTANT_FILENAME	= "observer_d";


    public final static String BINARY_DEST_DIR_NAME 	= "bin";
    public final static String BINARY_FILE_NAME		= "daemon";

    //广播Transaction标识
    public final static int BROADCAST_INTENT_TRANSACTION = 14;
    //启动Service Transaction标识
    public final static int START_SERVICE_TRANSACTION = 34;
}
