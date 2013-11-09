package com.baidu.meet;

import android.app.Application;
import android.content.pm.ApplicationInfo;

public class BaseApplication extends Application {
private static BaseApplication sApp = null;
	
	private boolean mIsDebugMode = false;

	@Override
	public void onCreate() {
		sApp = this;
		super.onCreate();
		
		initWorkMode();
	}


	public static BaseApplication getApplication() {
		return sApp;
	}

	private void initWorkMode() {
		if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
			mIsDebugMode = false;
		} else {
			mIsDebugMode = true;
		}
	}

	public boolean isDebugMode() {
		return mIsDebugMode;
	}

	/**
	 * 此方法需要在 {@link #onCreate()} 方法执行完毕后调用才能生效。
	 */
	public void setDebugMode(boolean mIsDebugMode) {
		this.mIsDebugMode = mIsDebugMode;
	}

	/**
	 * 应用内存不足时回调方法。
	 * 
	 * <p>在检测到OOM时回调，回调来自于background线程。</p>
	 */
	public void onAppMemoryLow(){
		
	}
	
	
}
