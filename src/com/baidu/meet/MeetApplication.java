package com.baidu.meet;

import java.util.ArrayList;
import java.util.List;

import com.baidu.meet.cache.BdCacheService;
import com.baidu.meet.cache.BdCacheService.CacheEvictPolicy;
import com.baidu.meet.cache.BdCacheService.CacheStorage;
import com.baidu.meet.cache.BdKVCache;
import com.baidu.meet.config.Config;
import com.baidu.meet.imageLoader.SDRamImage;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Process;
import android.telephony.TelephonyManager;


public class MeetApplication extends BaseApplication {
	private static MeetApplication app;
	private String mImei;
	private SDRamImage mSdramImage = null;
	private BdKVCache<String> kv_cache = null; // cache
	
	private Boolean _isMainProcess;
	
	public ArrayList<Activity> mRemoteActivity = null;
	/**
	 * 创建应用的时候调用
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		app = this;
		mSdramImage = new SDRamImage();
		
		if (isMainProcess()) {
			
		} else {
			mRemoteActivity = new ArrayList<Activity>();
		}
	}
	
	public static MeetApplication getApp() {
		return app;
	}
	
	public boolean isMainProcess() {
		if (_isMainProcess != null) {
			return _isMainProcess.booleanValue();
		}

		boolean ret = true;
		ActivityManager am = (ActivityManager) this
				.getSystemService(ACTIVITY_SERVICE);
		if (am == null) {
			return ret;
		}
		List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
		int pid = Process.myPid();
		if (list != null) {
			_isMainProcess = Boolean.TRUE;

			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).pid == pid) {
					String name = list.get(i).processName;
					if (name != null
							&& (name.equalsIgnoreCase("com.baidu.meet:pushservice_v1")
									|| name.equalsIgnoreCase("com.baidu.meet:remote") || name
										.equalsIgnoreCase("com.baidu.meet:bdservice_v1"))) {
						ret = false;

						_isMainProcess = Boolean.FALSE;
					}
					break;
				}
			}
		}

		// 如果失败的话，下次重新检测。

		return ret;
	}
	
	public void addRemoteActivity(Activity activity) {
		if (mRemoteActivity != null) {
			int size = mRemoteActivity.size();
			for (int i = 0; i < size; i++) {
				try {
//					mRemoteActivity.get(i).releaseResouce();
				} catch (Exception ex) {
				}
			}
			if (activity != null) {
				mRemoteActivity.add(activity);
			}
		}
	}

	public void delRemoteActivity(Activity activity) {
		if (mRemoteActivity != null) {
			mRemoteActivity.remove(activity);
		}
	}
	
	public static synchronized SharedPreferences getSharedPreferences() {
		return MeetApplication.getApp().getSharedPreferences(Config.CONFIG_FILE_NAME,
				Context.MODE_PRIVATE);
	}
	
	private void initImei() {
		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		if (mTelephonyMgr != null) {
			mImei = mTelephonyMgr.getDeviceId();
		}
	}
	
	public String getImei() {
		return mImei;
	}
	
	public void setSdramImage(SDRamImage sdramImage) {
		this.mSdramImage = sdramImage;
	}

	public SDRamImage getSdramImage() {
		return mSdramImage;
	}
	
	/**
	 * 获取Cache
	 * 
	 * @return
	 */
	public BdKVCache<String> getCache() {
		if (kv_cache == null) {
			BdCacheService service = BdCacheService.sharedInstance();
			kv_cache = service.getAndStartTextCache("tb.global",
					CacheStorage.SQLite_CACHE_PER_TABLE,
					CacheEvictPolicy.NO_EVICT, 1);
		}
		return kv_cache;
	}
	
}
