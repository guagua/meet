package com.baidu.meet;

import com.baidu.meet.imageLoader.SDRamImage;

import android.content.Context;
import android.telephony.TelephonyManager;


public class MeetApplication extends BaseApplication {
	private static MeetApplication app;
	private String mImei;
	private SDRamImage mSdramImage = null;
	/**
	 * 创建应用的时候调用
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		app = this;
		mSdramImage = new SDRamImage();
	}
	
	public static MeetApplication getApp() {
		return app;
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
	
}
