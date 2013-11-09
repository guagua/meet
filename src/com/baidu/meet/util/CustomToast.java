package com.baidu.meet.util;

import com.baidu.meet.MeetApplication;

import android.os.Handler;
import android.view.Gravity;
import android.widget.Toast;

public class CustomToast {

	private static Toast mToast;
	private static final int TOAST_DURATION = 2000;

	private static Handler mHandler = new Handler();

	private static Runnable r = new Runnable() {
		public void run() {
			if (mToast != null) {
				mToast.cancel();
			}
		}
	};

	public static CustomToast newInstance() {
		return new CustomToast();
	}

	private boolean dontShowToast;

	private CustomToast() {

	}

	public void showToast(String text, int duration, int y_offset) {
		if (dontShowToast) {
			return;
		}

		if (text == null) {
			return;
		}

		text = text.trim();
		if (text.length() == 0) {
			return;
		}

		mHandler.removeCallbacks(r);

		if (mToast != null) {
			mToast.setText(text);
		} else {
			mToast = Toast.makeText(MeetApplication.getApp(), text,
					Toast.LENGTH_SHORT);

			mToast.setGravity(Gravity.CENTER, 0, y_offset);
		}

		mHandler.postDelayed(r, duration);
		mToast.show();
	}
	
	public void showToast(String text) {
		showToast( text, TOAST_DURATION);
	}

	public void showToast(int resId) {
		showToast(resId, TOAST_DURATION);
	}

	public void showToast(String text, int duration) {
		int y_offset = UtilHelper.dip2px(MeetApplication.getApp(), 100);
		showToast(text, duration, y_offset);
	}

	public void showToast(int resId, int duration) {
		showToast(MeetApplication.getApp().getResources().getString(resId),
				duration);
	}
	
	public void showToast(int resId, int duration,int y_offset) {
		showToast(MeetApplication.getApp().getResources().getString(resId),
				duration,y_offset);
	}

	public void onPause() {
		dontShowToast = true;

		cancel();
	}

	public void onResume() {
		dontShowToast = false;
	}

	public static void cancel() {
		if (mToast != null) {
			mHandler.removeCallbacks(r);
			mToast.cancel();
			mToast = null;
		}
	}
}
