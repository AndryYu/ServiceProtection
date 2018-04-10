package com.example.km.ndk.strategy.base;

import android.content.Context;

/**
 * the configurations of Daemon SDK, contains two process configuration.
 *
 * Created by KM-ZhangYufei on 2018/4/9.
 */
public class DaemonConfigurations {
	
	public final DaemonConfiguration 	PERSISTENT_CONFIG;
	public final DaemonConfiguration 	DAEMON_ASSISTANT_CONFIG;
	public final DaemonListener		LISTENER;
	
	public DaemonConfigurations(DaemonConfiguration persistentConfig, DaemonConfiguration daemonAssistantConfig){
		this.PERSISTENT_CONFIG = persistentConfig;
		this.DAEMON_ASSISTANT_CONFIG = daemonAssistantConfig;
		this.LISTENER = null;
	}
	
	public DaemonConfigurations(DaemonConfiguration persistentConfig, DaemonConfiguration daemonAssistantConfig, DaemonListener listener){
		this.PERSISTENT_CONFIG = persistentConfig;
		this.DAEMON_ASSISTANT_CONFIG = daemonAssistantConfig;
		this.LISTENER = listener;
	}


	/**
	 * 单个守护进程的配置
	 */
	public static class DaemonConfiguration{
		
		public final String PROCESS_NAME;
		public final String SERVICE_NAME;
		public final String RECEIVER_NAME;
		
		public DaemonConfiguration(String processName, String serviceName, String receiverName){
			this.PROCESS_NAME = processName;
			this.SERVICE_NAME = serviceName;
			this.RECEIVER_NAME = receiverName;
		}
	}
	
	/**
	 * listener of daemon for external
	 * 
	 * @author Mars
	 */
	public interface DaemonListener {
		void onPersistentStart(Context context);
		void onDaemonAssistantStart(Context context);
		void onWatchDaemonDaed();
	}
}
