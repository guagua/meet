package com.baidu.meet.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.baidu.meet.R;
import com.baidu.meet.asyncTask.BdAsyncTask;
import com.baidu.meet.config.Config;
import com.baidu.meet.log.MeetLog;
import com.baidu.meet.network.NetWork;
import com.baidu.meet.util.BdBitmapHelper;
import com.baidu.meet.util.BitmapHelper;
import com.baidu.meet.util.FileHelper;
import com.baidu.meet.util.StringHelper;
import com.baidu.meet.util.UtilHelper;
import com.baidu.meet.view.DragImageView.OnGifSetListener;
import com.baidu.meet.view.DragImageView.OnSizeChangedListener;

/**
 * comments here.
 */
public class UrlDragImageView extends RelativeLayout {
	private static final String NCDN_PER = "width=";
	protected ProgressBar mProgressBar = null;
	protected DragImageView mImageView = null;
	private ImageAsyncTask mTask = null;
	protected Context mContext = null;
	private GetDataCallback mCallback = null;

	private boolean mIsCdn = false;

	/**
	 * 设置数据获取后的回调
	 * 
	 * @param callback
	 */
	public void setCallback(GetDataCallback callback) {
		mCallback = callback;
	}

	public UrlDragImageView(Context ctx) {
		super(ctx);
		mContext = ctx;
		init();
	}

	public UrlDragImageView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		mContext = ctx;
		init();
	}

	public DragImageView getImageView() {
		return mImageView;
	}

	/**
	 * 设置gif播放的监听事件
	 * 
	 * @param listener
	 */
	public void setGifSetListener(OnGifSetListener listener) {
		mImageView.setGifSetListener(listener);
	}

	/**
	 * 设置点击事件监听
	 * 
	 * @param click
	 */
	public void setImageOnClickListener(OnClickListener click) {
		mImageView.setImageOnClickListener(click);
	}

	/**
	 * 设置resize事件监听
	 * 
	 * @param listener
	 */
	public void setOnSizeChangedListener(OnSizeChangedListener listener) {
		mImageView.setOnSizeChangedListener(listener);
	}

	/**
	 * 放大图片
	 */
	public void zoomInBitmap() {
		mImageView.zoomInBitmap();
	}

	/**
	 * 缩小图片
	 */
	public void zoomOutBitmap() {
		mImageView.zoomOutBitmap();
	}

	protected void init() {
		mImageView = new DragImageView(mContext);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		mImageView.setLayoutParams(params);
		this.addView(mImageView);

		mProgressBar = new ProgressBar(mContext, null,
				android.R.attr.progressBarStyleInverse);
		mProgressBar.setIndeterminateDrawable(mContext.getResources()
				.getDrawable(R.drawable.progressbar));
		params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mProgressBar.setLayoutParams(params);
		mProgressBar.setIndeterminate(true);
		this.addView(mProgressBar);
	}

	/**
	 * 设置图片url
	 * 
	 * @param imageUrl
	 */
	public void setUrl(String imageUrl, boolean isAllowLocal) {
		mImageView.setTag(imageUrl);
		UtilHelper.NetworkStateInfo info = UtilHelper
				.getNetStatusInfo(getContext());
		if (info == UtilHelper.NetworkStateInfo.WIFI
				|| info == UtilHelper.NetworkStateInfo.ThreeG) {
			if (mTask != null) {
				mTask.cancel();
			}
			if (imageUrl != null) {
				mTask = new ImageAsyncTask(imageUrl, isAllowLocal);
				mTask.execute();
			}
		}
	}

	public void setGifMaxUseableMem(int mem) {
		mImageView.setGifMaxUseableMem(mem);
	}

	public void destroyTask() {
		if (mTask != null) {
			mTask.cancel();
			mTask = null;
		}
	}

	public void onDestroy() {
		destroyTask();
		if (mImageView != null) {
			mImageView.onDestroy();
		}
		mProgressBar.setVisibility(View.GONE);
	}

	public void release() {
		destroyTask();
		if (mImageView != null) {
			mImageView.release();
		}
		mProgressBar.setVisibility(View.GONE);
	}

	/**
	 * 停止gif播放
	 */
	public void stopGif() {
		if (mImageView != null
				&& mImageView.getImageType() == DragImageView.IMAGE_TYPE_DYNAMIC) {
			mImageView.stop();
		}
	}

	/**
	 * 检查图片是否下载完成，如果没有下载，则下载
	 */
	public void checkImage(boolean isAllowLocal) {
		if (mImageView == null) {
			return;
		}
		String url = (String) (mImageView.getTag());
		if (url == null) {
			return;
		}
		if (mImageView != null && mTask == null) {
			if (mImageView.getImageType() == DragImageView.IMAGE_TYPE_DYNAMIC) {
				if (mImageView.getGifCache() == null) {
					mTask = new ImageAsyncTask(url, isAllowLocal);
					mTask.execute();
				}
			} else if (mImageView.getImageType() == DragImageView.IMAGE_TYPE_DEFAULT) {
				if (UtilHelper.getNetStatusInfo(getContext()) == UtilHelper.NetworkStateInfo.UNAVAIL) {
					return;
				}
				mTask = new ImageAsyncTask(url, isAllowLocal);
				mTask.execute();
			} else {
				if (mImageView.getImageBitmap() == null) {
					mTask = new ImageAsyncTask(url, isAllowLocal);
					mTask.execute();
				}
			}
		}
	}

	public int getImageType() {
		if (mImageView != null) {
			return mImageView.getImageType();
		} else {
			return DragImageView.IMAGE_TYPE_STATIC;
		}
	}

	public boolean isIsCdn() {
		return mIsCdn;
	}

	public void setIsCdn(boolean mIsCdn) {
		this.mIsCdn = mIsCdn;
	}

	/**
	 * 获取图片的异步任务
	 * 
	 * @author zhaolin02
	 * 
	 */
	private class ImageAsyncTask extends
			BdAsyncTask<String, Integer, ImageData> {
		private NetWork mNetwork = null;
		private String mUrl = null;
		private String mFileName = null;
		private boolean allowLocalUrl;

		ImageAsyncTask(String url, boolean isAllowLocal) {
			mUrl = url;
			mFileName = StringHelper.getNameMd5FromUrl(url);
			allowLocalUrl = isAllowLocal;
		}

		@Override
		protected ImageData doInBackground(String... params) {
			ImageData im = null;
			if (mUrl == null || mFileName == null) {
				return null;
			}

			String fullUrl = mUrl;

			Bitmap bm = null;
			try {
				byte[] data = mImageView.getImageData();
				;
				if (data != null) {
					bm = BitmapHelper.Bytes2Bitmap(data);
				}
				if (bm == null) {
					if (this.allowLocalUrl && mUrl.startsWith("/")) {
						// local file
						bm = BdBitmapHelper.getInstance().getImageAbsolutePath(
								mUrl);
					} else {
						data = FileHelper.GetFileData(Config.TMP_PIC_DIR_NAME,
								mFileName);
						if (data != null) {
							bm = BitmapHelper.Bytes2Bitmap(data);
						}
					}
				}
				if (bm == null) {
					mNetwork = new NetWork(fullUrl);

					mNetwork.setIsBDImage(true);
					data = mNetwork.getNetData();
					if (mNetwork.isRequestSuccess()) {
						bm = BitmapHelper.Bytes2Bitmap(data);
					}
					FileHelper.SaveFile(Config.TMP_PIC_DIR_NAME, mFileName,
							data);
				}
				im = new ImageData();
				im.url = mUrl;
				im.data = data;
				im.bitmap = bm;
			} catch (Exception ex) {
				MeetLog.e(this.getClass().getName(), "doInBackground",
						ex.getMessage());
			}
			return im;
		}

		protected void onPostExecute(ImageData data) {
			mProgressBar.setVisibility(GONE);
			mTask = null;
			if (data == null) {
				return;
			}
			if (mCallback != null) {
				mCallback.callback(data.url, data.data);
			}
			Bitmap bm = data.bitmap;
			if (bm == null) {
				mImageView.setDefaultBitmap();
			} else {
				boolean isGif = UtilHelper.isGif(data.data);
				if (isGif == true) {
					mImageView.setGifData(data.data, bm);
				} else {
					mImageView.setImageBitmap(bm);
					mImageView.setImageData(data.data);
				}
			}

		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			mImageView.setImageBitmap(null);
			mProgressBar.setVisibility(VISIBLE);
			super.onPreExecute();
		}

		public void cancel() {
			if (mNetwork != null) {
				mNetwork.cancelNetConnect();
			}
			mImageView.setVisibility(VISIBLE);
			mProgressBar.setVisibility(GONE);
			mTask = null;
			super.cancel(true);
		}
	}

	public interface GetDataCallback {
		public void callback(String url, byte[] data);
	}

	private class ImageData {
		public String url;
		public byte[] data;
		public Bitmap bitmap;
	}
}
