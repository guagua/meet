package com.baidu.meet.imageLoader;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.baidu.meet.MeetApplication;
import com.baidu.meet.config.Config;
import com.baidu.meet.log.MeetLog;
import com.baidu.meet.network.NetWork;
import com.baidu.meet.util.BitmapHelper;
import com.baidu.meet.util.FileHelper;
import com.baidu.meet.util.StringHelper;
import com.baidu.meet.util.UtilHelper;

public class AsyncImageLoader {
	private Context mContext;
	private LinkedList<ImageAsyncTask> mTasks;
	private LinkedList<HoldData> mHoldData;
	private boolean mSupportHoldUrl = false;
	private ArrayList<BasicNameValuePair> infos = null;

	public AsyncImageLoader(Context context) {
		mContext = context;
		mTasks = new LinkedList<ImageAsyncTask>();
		mHoldData = new LinkedList<HoldData>();
		mSupportHoldUrl = true;
	}

	public void setSupportHoldUrl(boolean support) {
		return;
	}

	public void setExtraInfos(ArrayList<BasicNameValuePair> infos) {
		this.infos = infos;
	}

	public void clearHoldUrl() {
		mHoldData.clear();
	}

	public Bitmap getPhoto(String imageUrl) {
		Bitmap bitmap = null;
		SDRamImage sdramImage = MeetApplication.getApp().getSdramImage();
		if (sdramImage != null) {
			bitmap = sdramImage.getPhoto(imageUrl);
		}
		return bitmap;
	}

	public Bitmap getPic(String imageUrl) {
		Bitmap bitmap = null;
		SDRamImage sdramImage = MeetApplication.getApp().getSdramImage();
		if (sdramImage != null) {
			bitmap = sdramImage.getPic(imageUrl);
		}
		return bitmap;
	}

	public void removePhoto(String key) {
		SDRamImage sdramImage = MeetApplication.getApp().getSdramImage();
		if (sdramImage != null) {
			sdramImage.deletePhoto(key);
		}
	}

	public Bitmap loadImage(String imageUrl, ImageCallback mImageCallback) {
		return loadBitmap(imageUrl, mImageCallback, 0, true);
	}

	/**
	 * 
	 * @param imageUrl
	 * @param mImageCallback
	 * @param type
	 *            0:ͼƬ��� 1:��ע��ͷ�� 2:PBҳͷ��
	 * @return
	 */
	private Bitmap loadBitmap(String imageUrl, ImageCallback mImageCallback,
			Integer type, boolean from_db) {
		if (imageUrl == null)
			return null;
		String cacheImageUrl = imageUrl;

		SDRamImage sdramImage = MeetApplication.getApp().getSdramImage();
		if (sdramImage != null) {
			Bitmap bitmap = null;
			if (type == 0) {
				bitmap = sdramImage.getPic(cacheImageUrl);
			}
			if (bitmap != null) {
				return bitmap;
			}
		}
		try {
			int size = mTasks.size();
			for (int i = 0; i < size; i++) {
				if (mTasks.get(i).getUrl().equals(imageUrl)) {
					return null;
				}
			}
			if (mSupportHoldUrl == true && mHoldData != null) {
				size = mHoldData.size();
				for (int i = 0; i < size; i++) {
					if (mHoldData.get(i).url != null
							&& mHoldData.get(i).url.equals(imageUrl)) {
						return null;
					}
				}
			}
			if (mTasks.size() >= Config.MAX_ASYNC_IMAGE_LOADER_NUM) {
				if (mSupportHoldUrl == false) {
					ImageAsyncTask tmp = mTasks.get(0);
					tmp.cancel();
					mTasks.remove(0);
					ImageAsyncTask task = new ImageAsyncTask(imageUrl, type,
							mImageCallback, from_db);
					mTasks.add(task);
					task.execute();
				} else if (mHoldData != null) {
					HoldData data = new HoldData();
					data.callback = mImageCallback;
					data.url = imageUrl;
					data.type = type;
					data.from_db = from_db;
					mHoldData.add(data);
				}
			} else {
				ImageAsyncTask task = new ImageAsyncTask(imageUrl, type,
						mImageCallback, from_db);
				mTasks.add(task);
				task.execute();
			}

		} catch (Exception ex) {
			MeetLog.e("AsyncImageLoader", "loadBitmap",
					"error = " + ex.getMessage());
		}
		return null;
	}

	private class ImageAsyncTask extends AsyncTask<String, Integer, Bitmap> {
		private volatile NetWork mNetWork = null;
		private ImageCallback mImageCallback = null;
		private String mUrl = null;
		private volatile int mType = 0;
		private volatile Bitmap mBitmap = null;
		private boolean iscached = true;
		private boolean from_db = false;
		private String cacheImageUrl = null;
		private volatile boolean mIsGif = false;

		public ImageAsyncTask(String url, int type, ImageCallback callback,
				boolean from_db) {
			mUrl = url;
			mType = type;
			mImageCallback = callback;
			mBitmap = null;
			this.from_db = from_db;
			cacheImageUrl = url;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			try {
				boolean need_cash = false;
				byte[] tmp = null;
				String name = StringHelper.getNameMd5FromUrl(mUrl);
				if (from_db == true) {
					if (name != null) {
						if (FileHelper.CheckFile(name)) {
							SDRamImage sdramImage = MeetApplication.getApp()
									.getSdramImage();
							if (sdramImage != null) {
								sdramImage.deletePic(Config.getBigImageSize());
							}
						}
						mBitmap = FileHelper.getImage(Config.TMP_PIC_DIR_NAME,
								name);
						if (mBitmap != null) {
							mIsGif = FileHelper.isGif(Config.TMP_PIC_DIR_NAME,
									name);
						}
					}
				}
				
				if (mBitmap == null) {
					iscached = false;
					need_cash = true;
					String fullUrl = null;
					int pb_image_width = 800;
					int pb_image_height = 800;
					if (mType == 0) {
//						fullUrl = StringHelper.getUrlEncode(mUrl);
						fullUrl = mUrl;
					}

					mNetWork = new NetWork(mContext, fullUrl);
					if (infos != null) {
						for (int i = 0; i < infos.size(); i++) {
							mNetWork.addPostData(infos.get(i));
						}
					}

					if (mType == 0) {
						mNetWork.setIsBDImage(true);
					}
					tmp = mNetWork.getNetData();
					if (mNetWork.isRequestSuccess()) {
						if (mType == 0) {
							SDRamImage sdramImage = MeetApplication.getApp()
									.getSdramImage();
							if (sdramImage != null) {
								sdramImage.deletePic(Config.getBigImageSize());
							}
						}
						mBitmap = BitmapHelper.Bytes2Bitmap(tmp);

						mIsGif = UtilHelper.isGif(tmp);
						
						if (mBitmap != null) {

							if (mType == 0) {
								if (mBitmap.getWidth() > pb_image_width
										|| mBitmap.getHeight() > pb_image_height) {
									MeetLog.log_e(
											MeetLog.ERROR_TYPE_NET,
											this.getClass().getName(),
											"doInBackground",
											"Pb_image_too_big:"
													+ String.valueOf(mBitmap
															.getWidth()
															+ "*"
															+ String.valueOf(mBitmap
																	.getHeight())));
									mBitmap = BitmapHelper.resizeBitmap(
											mBitmap, pb_image_width,
											pb_image_height);
								}
							}

						}
					}
				}

				this.publishProgress();
				if (need_cash == true && mBitmap != null) {
					if (name != null && mBitmap != null && tmp != null) {
						FileHelper.SaveFile(Config.TMP_PIC_DIR_NAME, name,
								tmp);
					}
				}
			} catch (Exception ex) {
				MeetLog.e("ImageAsyncTask", "doInBackground",
						"error = " + ex.getMessage());
			}
			return mBitmap;
		}

		public String getUrl() {
			return mUrl;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			SDRamImage sdramImage = MeetApplication.getApp().getSdramImage();
			if (sdramImage != null && mBitmap != null) {
				if (mType == 0) {
					sdramImage.addPic(cacheImageUrl, mBitmap, mIsGif);
				} else if (mType == 1 || mType == 2) {
					sdramImage.addPhoto(cacheImageUrl, mBitmap);
				}
			}
			if (mImageCallback != null) {
				mImageCallback.imageLoaded(mBitmap, mUrl, iscached);
			}
			super.onProgressUpdate(values);
		}

		protected void onPostExecute(Bitmap bitmap) {
			mTasks.remove(this);
			if (mSupportHoldUrl == true) {
				if (mHoldData.size() > 0) {
					HoldData data = mHoldData.remove(0);
					loadBitmap(data.url, data.callback, data.type, data.from_db);
				}
			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}

		public void cancel() {
			if (mNetWork != null) {
				mNetWork.cancelNetConnect();
			}
			if (mSupportHoldUrl == true && mHoldData.size() > 0) {
				HoldData data = mHoldData.remove(0);
				if (data != null) {
					loadBitmap(data.url, data.callback, data.type, data.from_db);
				}
			}
			super.cancel(true);
		}

	}

	public void cancelAllAsyncTask() {
		mHoldData.clear();
		for (int i = 0; i < mTasks.size(); i++) {
			ImageAsyncTask task = mTasks.get(i);
			if (task != null) {
				task.cancel();
			}
		}
		mTasks.clear();
	}

	public int getAsyncTaskNum() {
		return mTasks.size();
	}

	public interface ImageCallback {
		public void imageLoaded(Bitmap bitmap, String imageUrl, boolean isCached);
	}

	private class HoldData {
		String url;
		int type;
		ImageCallback callback;
		boolean from_db;
	}
}