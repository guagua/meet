package com.baidu.meet.register;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.baidu.meet.R;
import com.baidu.meet.asyncTask.BdAsyncTask;
import com.baidu.meet.config.Config;
import com.baidu.meet.config.RequestResponseCode;
import com.baidu.meet.network.NetWork;
import com.baidu.meet.util.BdUtilHelper;
import com.baidu.meet.util.BitmapHelper;
import com.baidu.meet.util.FileHelper;
import com.baidu.meet.util.NavigationBar;
import com.baidu.meet.util.TiebaPrepareImageService;
import com.baidu.meet.util.UtilHelper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import cn.jingling.lib.filters.FilterFactory;

public class EditHeadActivity extends Activity {

	public final static int PERSON_TYPE = 0;// 個人
	public final static int GROUP_TYPE = 1;// 群
	public final static int GROUP_PHOTO_WALL = 2;// 照片墙照片

	public static String PHOTO_RESOURCE = "resourceid";
	public static String PIC_INFO = "pic_info";
	static public String DELET_FLAG = "delete";
	static public String CHANGE_FLAG = "change";
	static public String FILE_NAME = "file_name";
	public final static String FROMCODE = "request";
	public final static String ACCOUNTDATA = "account_data";
	public final static String EDITTYPE = "edit_type";
	public final static String FROM = "from";
	public final static String FORUMID = "forumid";
	public final static String FORUMNAME = "foruimname";
	public final static String THREADID = "threadid";
	public final static String FILTER_NAME_NORMAL = "normal";
	public final static int PREVIEW_IMAGE_WIDTH = 300;
	private static String[] filterNameArray = null;

	private EditHeadImageView mImage = null;
	private Bitmap mBitmap = null;
	private int mEditType = EditHeadActivity.PERSON_TYPE;
	private TextView mDone = null;
	private Button mHide = null;
	private Button mShow = null;
	private HorizontalScrollView mBeautifyView = null;
	private ProgressBar mProgress = null;
	private GetImageTask mTask = null;
	private FilterTask mFilterTask = null;
	private RadioButton mBtnBeautify;
	private RadioButton mBtnRotate;
	private LinearLayout mRotateView = null;
	private LinearLayout mBeautifyRotateView = null;
	private ProHeadModifyTask mHeadModifyTask = null;
	private TextView mTitle = null;

	private Bitmap mPreparedBitmap = null;
	private ImageResizedReceiver receiver = null;
	private int BOUND = 0;
	private int motuID = 0;

	private boolean isMotuOn = true;

	private String lastFilter = null;

	private LinearLayout mFilterView;
	private boolean isEdited = false;
	private int requestCode;
	private HashMap<String, Bitmap> previewBitmaps;
	private HashMap<String, ImageView> previewImageView;
	
	private NavigationBar mNavigationBar;
	private ImageView mBack;

    static public void startActivityForResult(Activity activity, int fromCode,
			int requestCode, Uri uri, int editType) {
		Intent intent = new Intent(activity, EditHeadActivity.class);
		intent.putExtra(FROMCODE, fromCode);
		intent.putExtra(EDITTYPE, editType);
		intent.setData(uri);
		activity.startActivityForResult(intent, requestCode);
	}

	static public void startActivityForResult(Activity activity, int fromCode,
			int requestCode, Uri uri) {
		startActivityForResult(activity, fromCode, requestCode, uri,
			EditHeadActivity.PERSON_TYPE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_head_activity);
		Intent intent = getIntent();

		mEditType = intent.getIntExtra(EDITTYPE, PERSON_TYPE);
		requestCode = intent.getIntExtra(FROMCODE, 0);
		if (requestCode == RequestResponseCode.REQUEST_ALBUM_IMAGE || requestCode == RequestResponseCode.REQUEST_CAMERA) {
			initUI();
			if (intent.getData() != null) {
				TiebaPrepareImageService.StartService(requestCode, intent.getData(), 900);
			} else {
				TiebaPrepareImageService.StartService(requestCode, null, 900);
			}
			regReceiver();
		} else {
			initUI();
			initData();
		}
		filterNameArray = getResources().getStringArray(R.array.fiter_name);
		isMotuOn = true;

	}

	@Override
	protected void onResume() {
		super.onResume();
		Drawable dr = mImage.getDrawable();
		if (dr != null && dr instanceof BitmapDrawable) {
			if (((BitmapDrawable) dr).getBitmap() == null && mTask == null) {
				mTask = new GetImageTask();
				mTask.execute();
			}
		}
	}

    public void releaseResouce() {
		if (mTask != null) {
			mTask.cancel();
		}
		mImage.setImageBitmap(null);
		releasePreviewResouce();
	}

	private void releasePreviewResouce() {
		if (previewImageView != null) {
			Iterator<Entry<String, ImageView>> iter = previewImageView.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, ImageView> tempEntry = iter.next();
				ImageView tempBitmap = tempEntry.getValue();
				if (tempBitmap != null) {
					tempBitmap.setImageBitmap(null);
				}
			}
			previewImageView.clear();
			previewImageView = null;
		}
		if (previewBitmaps != null) {
			Iterator<Entry<String, Bitmap>> iter = previewBitmaps.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Bitmap> tempEntry = iter.next();
				Bitmap tempBitmap = tempEntry.getValue();
				if (tempBitmap != null && !tempBitmap.isRecycled()) {
					tempBitmap.recycle();
				}
			}
			previewBitmaps.clear();
			previewBitmaps = null;
		}
	}

	private void initData() {
		if (mTask != null) {
			mTask.cancel();
		}
		mTask = new GetImageTask();
		mTask.execute();
	}

	@Override
	protected void onDestroy() {
		releaseResouce();
		super.onDestroy();
		mImage.onDestroy();
		if (mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
			mBitmap = null;
		}
		if (mHeadModifyTask != null) {
			mHeadModifyTask.cancel();
		}

		if (mPreparedBitmap != null && !mPreparedBitmap.isRecycled()) {
			mPreparedBitmap.recycle();
			mPreparedBitmap = null;
		}
		if (mTask != null) {
			mTask.cancel();
			mTask = null;
		}
		mProgress.setVisibility(View.GONE);
		if (requestCode == RequestResponseCode.REQUEST_ALBUM_IMAGE || requestCode == RequestResponseCode.REQUEST_CAMERA)
			unregisterReceiver(receiver);
	}

	private void modifyHead() {
		if (mHeadModifyTask != null) {
			mHeadModifyTask.cancel();
		}
		mHeadModifyTask = new ProHeadModifyTask();
		mHeadModifyTask.execute();
	}

	private void initUI() {
		mProgress = (ProgressBar) findViewById(R.id.progress);
		mProgress.setVisibility(View.GONE);
		mNavigationBar = (NavigationBar) findViewById(R.id.navigation_bar);
		mImage = (EditHeadImageView) findViewById(R.id.image);
		mImage.setImageBitmap(mBitmap);
		mBeautifyView = (HorizontalScrollView) findViewById(R.id.filters_layout);
		mBack = mNavigationBar.addSystemImageButton(NavigationBar.ControlAlign.HORIZONTAL_LEFT,
                NavigationBar.ControlType.BACK_BUTTON, new OnClickListener() {

            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }

        });
		mShow = (Button) findViewById(R.id.show_button);
		mShow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mBeautifyRotateView.setVisibility(View.VISIBLE);
				mShow.setVisibility(View.GONE);
			}
		});

		mHide = (Button) findViewById(R.id.hide_button);
		mHide.setVisibility(View.VISIBLE);
		mHide.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mBeautifyRotateView.setVisibility(View.GONE);
				mShow.setVisibility(View.VISIBLE);
			}
		});

		mDone = mNavigationBar.addTextButton(NavigationBar.ControlAlign.HORIZONTAL_RIGHT,
                getString(R.string.done), new OnClickListener() {

			@Override
			public void onClick(View v) {
//				RegisterActivity.startAcitivity(EditHeadActivity.this);
				
				String filename = Config.PERSON_HEAD_FILE;

				if (mEditType != PERSON_TYPE) {
					filename = Config.GROUP_HEAD_FILE;
				}
				Bitmap headBitmap = mImage.getHeadBitmap(mEditType);
				if (headBitmap != null) {
					if (saveFile(filename, headBitmap) == false) {
						return;
					}
				}

//				if (mEditType == PERSON_TYPE) {
//					modifyHead();
//				} else {
//					PicSendModel model = new PicSendModel(FileHelper
//							.getFileDireciory(Config.GROUP_HEAD_FILE),
//							0, 0, 0, 0);
//					model.setUploadPicCallback(new UploadPicCallback() {
//
//						@Override
//						public void callBack(String pach, UploadPicData2 info) {
//							closeLoadingDialog();
//							Intent intent = getIntent();
//
//							if (info != null) {
//								PhotoUrlData photoUrlData = new PhotoUrlData();
//								photoUrlData.setPicId(String
//										.valueOf(info.picId));
//								if (info.picInfo != null) {
//									if (info.picInfo.bigPic != null) {
//										photoUrlData
//												.setBigurl(info.picInfo.bigPic.picUrl);
//									}
//									if (info.picInfo.smallPic != null) {
//										photoUrlData
//												.setSmallurl(info.picInfo.smallPic.picUrl);
//									}
//								}
//								intent.putExtra(PHOTO_RESOURCE, String
//										.valueOf(info.picId));
//								intent.putExtra(PIC_INFO, photoUrlData);
//							}
//							setResult(Activity.RESULT_OK, intent);
//							finish();
//						}
//					});
//					model.loadPic();
//					showLoadingDialog(getString(R.string.uploading));
//				}
			}

		});
		mDone.setEnabled(false);

		mTitle = mNavigationBar.setTitleText(getString(R.string.beautify));
		if (mEditType == GROUP_PHOTO_WALL) {// 照片墙特殊处理title和button文案
			mTitle.setText(getString(R.string.beautify));
			mDone.setText(getString(R.string.done));
		}
		// 完成
		mFilterView = (LinearLayout) findViewById(R.id.filters);
		BOUND = BdUtilHelper.dip2px(this, 2f);

		mBeautifyRotateView = (LinearLayout) findViewById(R.id.beautify_rotate);
		mRotateView = (LinearLayout) findViewById(R.id.rotate);
		mBtnBeautify = (RadioButton) findViewById(R.id.beautify_btn);
		mBtnRotate = (RadioButton) findViewById(R.id.rotate_btn);
		OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				if (isChecked == true) {
					buttonView.setTextColor(getResources().getColor(R.color.white));
					if (buttonView == mBtnBeautify) {
						mBeautifyView.setVisibility(View.VISIBLE);
						mRotateView.setVisibility(View.INVISIBLE);
					} else if (buttonView == mBtnRotate) {
						mBeautifyView.setVisibility(View.INVISIBLE);
						mRotateView.setVisibility(View.VISIBLE);
					}
				} else {
					buttonView.setTextColor(getResources().getColor(R.color.beautify_rotate_tab_unchecked_color));
				}
			}
		};
		mBtnBeautify.setOnCheckedChangeListener(onCheckedChangeListener);
		mBtnRotate.setOnCheckedChangeListener(onCheckedChangeListener);

		mBtnBeautify.setChecked(true);

		Button rotateLeft = (Button) findViewById(R.id.rotate_left);
		Button rotateRight = (Button) findViewById(R.id.rotate_right);
		Button rotateLeftRight = (Button) findViewById(R.id.rotate_left_right);
		Button rotateUpDown = (Button) findViewById(R.id.rotate_up_down);

		rotateLeft.setTag(BitmapHelper.ROTATE_LEFT);
		rotateRight.setTag(BitmapHelper.ROTATE_RIGHT);
		rotateLeftRight.setTag(BitmapHelper.ROTATE_LEFT_RIGHT);
		rotateUpDown.setTag(BitmapHelper.ROTATE_UP_DOWN);

		OnClickListener rotateClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mProgress.getVisibility() == View.VISIBLE)
					return;

				if (mBitmap == null && mPreparedBitmap == null) {
					return;
				}

				if (v.getTag() != null) {
					isEdited = false;
					startFilterTask(v.getTag().toString());
				}
			}
		};

		rotateLeft.setOnClickListener(rotateClickListener);
		rotateRight.setOnClickListener(rotateClickListener);
		rotateLeftRight.setOnClickListener(rotateClickListener);
		rotateUpDown.setOnClickListener(rotateClickListener);

		if (android.os.Build.VERSION.SDK_INT < 7 || isMotuOn == false) {
			mBeautifyRotateView.setVisibility(View.GONE);
			mShow.setVisibility(View.GONE);
		}

	}

	private void startFilterTask(String label) {
		if (mFilterTask != null)
			mFilterTask.cancel();
		mFilterTask = new FilterTask();
		mFilterTask.execute(label);
	}

	private class GetImageTask extends BdAsyncTask<Object, Integer, Bitmap> {

		@Override
		protected Bitmap doInBackground(Object... params) {
			Bitmap bitmap = null;
			try {
				bitmap = FileHelper.getImage(null, Config.IMAGE_RESIZED_FILE);
				if (bitmap.getWidth() > Config.POST_IMAGE_MIDDLE || bitmap.getHeight() > Config.POST_IMAGE_MIDDLE) {
					Bitmap temp = bitmap;
					bitmap = BitmapHelper.resizeBitmap(bitmap, Config.POST_IMAGE_MIDDLE);
					temp.recycle();
				}
				if (isCancelled() && bitmap != null && !bitmap.isRecycled()) {
					bitmap.recycle();
					bitmap = null;
				} else {
					int bitmapWidth = BdUtilHelper.dip2px(EditHeadActivity.this, 63.5f);
					if (android.os.Build.VERSION.SDK_INT >= 7 && isMotuOn) {
						Bitmap smallBitmap = BitmapHelper.getResizedBitmap(bitmap, bitmapWidth);
						smallBitmap = BitmapHelper.getRoundedCornerBitmap(smallBitmap, BdUtilHelper.dip2px(EditHeadActivity.this, 5f));
						previewBitmaps = new HashMap<String, Bitmap>();
						previewImageView = new HashMap<String, ImageView>();
						previewBitmaps.put(FILTER_NAME_NORMAL, smallBitmap);

						for (final String label : filterNameArray) {
							String filter = label.substring(0, label.indexOf("|"));
							if (filter.equals(FILTER_NAME_NORMAL))
								continue;

							Bitmap preview = FilterFactory.createOneKeyFilter(
									EditHeadActivity.this, filter).apply(
									EditHeadActivity.this,
									smallBitmap.copy(smallBitmap.getConfig() == null ? Config.BitmapConfig
											: smallBitmap.getConfig(),
											true));
							previewBitmaps.put(filter, preview);
						}
					}
				}
			} catch (Exception e) {
			}
			return bitmap;
		}

		@Override
		protected void onPreExecute() {
			mProgress.setVisibility(View.VISIBLE);
			mDone.setEnabled(false);
			super.onPreExecute();
		}

		public void cancel() {
			mTask = null;
			mProgress.setVisibility(View.GONE);
			mDone.setEnabled(true);
			super.cancel(true);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			mTask = null;
			mBitmap = result;
			mProgress.setVisibility(View.GONE);
			mDone.setEnabled(true);
			if (result == null || result.isRecycled())
				return;
			if (result != null) {
				mImage.setImageBitmap(result);
				if (android.os.Build.VERSION.SDK_INT >= 7 && isMotuOn == true)
					makeFilters(filterNameArray);
			}
		}
	}

	private boolean saveFile(String filename, Bitmap bitmap) {
		try {
			FileHelper.SaveFile(null, filename, bitmap, 90);
//			if (isEdited) {
//				(new PvThread("motu_pic", String.valueOf(motuID))).start();
//			}
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	private void makeFilters(String[] labels) {
		if (previewBitmaps == null || labels == null)
			return;
		mFilterView.removeAllViews();

		View filterItem = getLayoutInflater().inflate(R.layout.filter_item, null);
		ImageView previewView = (ImageView) filterItem.findViewById(R.id.filter_immage);
		TextView previewText = (TextView) filterItem.findViewById(R.id.filter_text);
		int count = 0;
		for (final String label : labels) {
			final String filter = label.substring(0, label.indexOf("|"));
			final String name = label.substring(label.indexOf("|") + 1);

			filterItem = getLayoutInflater().inflate(R.layout.filter_item, null);
			previewView = (ImageView) filterItem.findViewById(R.id.filter_immage);
			previewText = (TextView) filterItem.findViewById(R.id.filter_text);
			previewText.setText(name);
			previewView.setImageBitmap(previewBitmaps.get(filter));
			final int id = count++;
			if (filter.equals(FILTER_NAME_NORMAL)) {

				previewView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mProgress.getVisibility() == View.VISIBLE)
							return;
						mImage.replaceImageBitmap(mBitmap);
						isEdited = false;
						setChecked(filter);
						motuID = id;
					}
				});

			} else {
				previewView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mProgress.getVisibility() == View.VISIBLE || filter.equals(lastFilter))
							return;
						startFilterTask(filter);
						setChecked(filter);
						motuID = id;
					}
				});

			}
			mFilterView.addView(filterItem);
			previewImageView.put(filter, previewView);
		}
		setChecked(FILTER_NAME_NORMAL);
		previewView = null;
	}

	private void setChecked(String filtername) {
		if (filtername == null)
			return;
		if (lastFilter != null) {
			ImageView lastView = previewImageView.get(lastFilter);
			if (lastView != null) {
				lastView.setBackgroundDrawable(null);
				lastView.setPadding(BOUND, BOUND, BOUND, BOUND);

			}
		}
		ImageView newView = previewImageView.get(filtername);
		if (newView != null) {
			newView.setBackgroundResource(R.drawable.round_corner);
			newView.setPadding(BOUND, BOUND, BOUND, BOUND);
		}
		lastFilter = filtername;
	}

	private class FilterTask extends BdAsyncTask<String, Void, Bitmap> {

		private String mLabel;
		private Bitmap bitmap;
		private Boolean isRotate = false;
		private Boolean isRecyle = false;

		@Override
		protected void onPreExecute() {
			mProgress.setVisibility(View.VISIBLE);
			mDone.setClickable(false);
		}

		@Override
		protected Bitmap doInBackground(String... arg0) {
			mLabel = arg0[0];

			if (mBitmap == null && mPreparedBitmap == null) {
				return null;
			}

			if (mLabel.equals(BitmapHelper.ROTATE_LEFT + "") || mLabel.equals(BitmapHelper.ROTATE_RIGHT + "")) {
				isRotate = true;
			} else if (mLabel.equals(BitmapHelper.ROTATE_LEFT_RIGHT + "") || mLabel.equals(BitmapHelper.ROTATE_UP_DOWN + "")) {
				isRecyle = true;
			}

			if (isRotate || isRecyle) {
				if (mPreparedBitmap != null) {
					bitmap = mPreparedBitmap.copy(mPreparedBitmap.getConfig(), true);
				} else {
					bitmap = mBitmap.copy(mBitmap.getConfig(), true);
				}
			} else {
				bitmap = mBitmap.copy(mBitmap.getConfig(), true);
			}

			if (isRotate) {
				bitmap = BitmapHelper.rotateBitmap(bitmap, Integer.parseInt(mLabel));
			} else if (isRecyle) {
				bitmap = BitmapHelper.reversalBitmap(bitmap, Integer.parseInt(mLabel));
			} else {
				bitmap = FilterFactory.createOneKeyFilter(EditHeadActivity.this, mLabel).apply(EditHeadActivity.this, bitmap);
			}

			return bitmap;
		}

		public void cancel() {
			if (bitmap != null && !bitmap.isRecycled() && mPreparedBitmap != bitmap)
				bitmap.recycle();
			bitmap = null;
			mProgress.setVisibility(View.GONE);
			mDone.setClickable(true);
			super.cancel(true);
		}

		@Override
		protected void onPostExecute(final Bitmap bm) {
			mProgress.setVisibility(View.GONE);
			mDone.setClickable(true);
			mDone.setEnabled(true);
			if (bm != null && bm.isRecycled() == false) {
				isEdited = true;

				if (isRotate || isRecyle) {
					mImage.setImageBitmap(bm);
					if (mBitmap.getWidth() > Config.POST_IMAGE_MIDDLE || mBitmap.getHeight() > Config.POST_IMAGE_MIDDLE)
						mBitmap = BitmapHelper.resizeBitmap(mBitmap, Config.POST_IMAGE_MIDDLE);

					if (isRotate) {
						mBitmap = BitmapHelper.rotateBitmap(mBitmap, Integer.parseInt(mLabel));
					} else if (isRecyle) {
						mBitmap = BitmapHelper.reversalBitmap(mBitmap, Integer.parseInt(mLabel));
					}
				} else {
					mImage.replaceImageBitmap(bm);
				}

				if (mPreparedBitmap != null && !(mPreparedBitmap.isRecycled())) {
					mPreparedBitmap.recycle();
				}

				mPreparedBitmap = bm;
			}
		}
	}

	private void regReceiver() {
		receiver = new ImageResizedReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Config.BROADCAST_IMAGE_RESIZED);
		registerReceiver(receiver, filter);
	}

	private class ImageResizedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			releaseResouce();
			if (intent.getBooleanExtra("result", false)) {
				initData();
			} else {
//				showToast(intent.getStringExtra("error"));
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			closeLoadingDialog();// 必须关闭对话框 不然无法退出
			setResult(Activity.RESULT_CANCELED);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private class ProHeadModifyTask extends BdAsyncTask<String, Integer, String> {
		private NetWork mNetwork = null;
//		private ChunkUploadResult mChunkUploadResult = null;
//		private ChunkUploadHelper mChunkUploadHelper = null;

		@Override
		protected void onPreExecute() {
//			showLoadingDialog(getString(R.string.upload_head));
		}

		@Override
		protected String doInBackground(String... arg0) {
			String ret = null;
			mNetwork = new NetWork(Config.SERVER_ADDRESS + Config.UPLOAD_IMAGE);
			try {
				final File image = FileHelper.GetFile(Config.PERSON_HEAD_FILE);
//				if (image.length() <= Config.CHUNK_UPLOAD_SIZE
//
//						|| (Config.getImgChunkUploadEnable() == 0 && mNetwork.getNetType() != null && !mNetwork.getNetType().equals(
//								NetWorkCoreByBdHttp.NET_TYPE_WAP))) {
//
//					ret = mNetwork.uploadImage(Config.PERSON_HEAD_FILE);
//					if (!mNetwork.isRequestSuccess()) {
//						return null;
//					}
//				} else {
//					TiebaLog.d("PostThreadTask", "doInBackground", "image size is more than 100K");
//					final String md5 = StringHelper.ToMd5(FileHelper.GetStreamFromFile(image));
//					ChunkUploadData uploadData = DatabaseService.getChunkUploadDataByMd5(md5);
//					if (uploadData == null) {
//						TiebaLog.d("PostThreadTask", "doInBackground", "upload data is null");
//						uploadData = new ChunkUploadData();
//						uploadData.setMd5(md5);
//						uploadData.setChunkNo(0);
//						uploadData.setTotalLength(image.length());
//					}
//					mChunkUploadHelper = new ChunkUploadHelper(Config.PERSON_HEAD_FILE, uploadData, Config.SERVER_ADDRESS + Config.UPLOAD_CHUNK_IMAGE_ADDRESS);
//					mChunkUploadResult = mChunkUploadHelper.uploadChunkFile();
//					if (mChunkUploadResult.isSuccess()) {
//						mNetwork = new NetWork(Config.SERVER_ADDRESS + Config.FINISH_UPLOAD_CHUNK_IMAGE_ADDRESS);
//						mNetwork.addPostData("md5", uploadData.getMd5());
//
//						ret = mNetwork.postNetData();
//						if (ret == null || !mNetwork.isRequestSuccess()) {
//							final long totalLength = uploadData.getTotalLength();
//							final long chunkNo = (totalLength % Config.CHUNK_UPLOAD_SIZE == 0) ? (totalLength / Config.CHUNK_UPLOAD_SIZE) : (totalLength
//									/ Config.CHUNK_UPLOAD_SIZE + 1);
//							uploadData.setChunkNo((int) chunkNo);
//							DatabaseService.saveChunkUploadData(uploadData);
//							return null;
//						}
//						DatabaseService.delChunkUploadData(md5);
//					} else {
//						return null;
//					}
//				}
			} catch (Exception e) {
			}
			return ret;
		}

		public void cancel() {
//			closeLoadingDialog();
			mHeadModifyTask = null;
			if (mNetwork != null) {
				mNetwork.cancelNetConnect();
			}
			super.cancel(true);
		}

		@Override
		protected void onPostExecute(String result) {
//			closeLoadingDialog();
			if (mNetwork != null) {
				if (mNetwork.isRequestSuccess()) {
					setResult(Activity.RESULT_OK);
					finish();
//					UtilHelper.showToast(getString(R.string.upload_head_ok));
				} else {
//					UtilHelper.showToast(mNetwork.getErrorString());
				}
			}
		}
	}

}
