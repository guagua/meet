package com.baidu.meet.util;

import com.baidu.meet.MeetApplication;
import com.baidu.meet.R;
import com.baidu.meet.asyncTask.BdAsyncTask;
import com.baidu.meet.config.Config;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;

public class TiebaPrepareImageService extends Service {
	static private final String REQUESTCODE = "request_code";
	static private final String MAX_SIZE = "max_size";
	static private final String DISPLAY_SIZE = "display_size";
	static public volatile boolean IS_DECODING = false;
	private int mRequestCode = 0;
	private Uri mUri = null;
	private PrepareImageTask mTask = null;
	private final Handler mHandler = new Handler();
	private int mMaxSize;
	private int mDisplaySize;
	private final Runnable mStartRun = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (IS_DECODING == false) {
				mTask = new PrepareImageTask(mRequestCode, mUri);
				mTask.execute();
			} else {
				mHandler.postDelayed(mStartRun, 1000);
			}
		}

	};

	static public void StartService(int requestCode, Uri uri, int max_size,
			int display_size) {
		Intent intent = new Intent(MeetApplication.getApp(),
				TiebaPrepareImageService.class);
		intent.putExtra(REQUESTCODE, requestCode);
		intent.putExtra(MAX_SIZE, max_size);
		intent.putExtra(DISPLAY_SIZE, display_size);
		intent.setData(uri);
		MeetApplication.getApp().startService(intent);
	}

	static public void StartService(int requestCode, Uri uri, int max_size) {
		StartService(requestCode, uri, max_size, 500);
	}

	static public void StopService() {
		Intent intent = new Intent(MeetApplication.getApp(),
				TiebaPrepareImageService.class);
		MeetApplication.getApp().stopService(intent);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mTask != null) {
			mTask.cancel();
		}
		mHandler.removeCallbacks(mStartRun);
		mTask = null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		if (intent != null) {
			startPrepareImage(intent);
		}
	}

	private void startPrepareImage(Intent intent) {
		if (mTask != null) {
			mTask.cancel();
		}
		mUri = intent.getData();
		mRequestCode = intent.getIntExtra(REQUESTCODE, 0);
		mMaxSize = intent.getIntExtra(MAX_SIZE, Config.POST_IMAGE_MIDDLE);
		mDisplaySize = intent.getIntExtra(DISPLAY_SIZE, 0);
		if (IS_DECODING == false) {
			mTask = new PrepareImageTask(mRequestCode, mUri);
			mTask.execute();
		} else {
			mHandler.postDelayed(mStartRun, 1000);
		}
	}

	private class PrepareImageTask extends
			BdAsyncTask<Object, Integer, Boolean> {
		int mRequestCode = 0;
		Uri mUri = null;
		String mError = null;

		public PrepareImageTask(int requestCode, Uri uri) {
			mRequestCode = requestCode;
			mUri = uri;
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			// TODO Auto-generated method stub
			boolean ret = false;
			IS_DECODING = true;
			try {
				Bitmap bm = WriteUtil.ImageResult(mRequestCode,
						TiebaPrepareImageService.this, mUri, mMaxSize);
				if (bm != null) {
					if (FileHelper.SaveFile(null, Config.IMAGE_RESIZED_FILE,
							bm, Config.POST_IMAGE_QUALITY) != null) {
						int size = Config.POST_IMAGE_DISPLAY;
						if (mDisplaySize > 0) {
							size = mDisplaySize;
						}
						Bitmap display = BitmapHelper.resizeBitmap(bm, size);
						if (display != null
								&& FileHelper.SaveFile(null,
										Config.IMAGE_RESIZED_FILE_DISPLAY,
										display, Config.POST_IMAGE_QUALITY) != null) {
							ret = true;
						} else {
							mError = getString(R.string.error_sd_error);
						}
					} else {
						mError = getString(R.string.error_sd_error);
					}
				} else {
					mError = getString(R.string.pic_parser_error);
				}

			} catch (Exception ex) {

			} finally {
				IS_DECODING = false;
			}
			return ret;
		}

		@Override
		public void cancel() {
			mTask = null;
			super.cancel(true);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Intent intent = new Intent(Config.BROADCAST_IMAGE_RESIZED);
			intent.putExtra("result", result);
			if (mError != null) {
				intent.putExtra("error", mError);
			}
			sendBroadcast(intent);
		}

	}
}
