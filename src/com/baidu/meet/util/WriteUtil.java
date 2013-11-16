package com.baidu.meet.util;

import java.io.File;

import com.baidu.meet.R;
import com.baidu.meet.config.RequestResponseCode;
import com.baidu.meet.log.MeetLog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

public class WriteUtil {
	static private final String TMP_IMAGE_NAME = "camera.jpg";

	/**
	 * 拍照
	 * 
	 * @param activity
	 */
	static public void takePhoto(Activity activity) {
		try {
			if (FileHelper.checkSD() == false) {
				UtilHelper.showToast(activity, FileHelper.getSdErrorString());
				return;
			}
			File out = FileHelper.CreateFile(TMP_IMAGE_NAME);
			if (out != null) {
				Uri uri = Uri.fromFile(out);
				Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
				activity.startActivityForResult(intent,
						RequestResponseCode.REQUEST_CAMERA);
			} else {
				UtilHelper.showToast(activity, activity.getString(R.string.error_sd_error));
			}
		} catch (Exception ex) {
			MeetLog.e("WriteUtil", "takePhoto", "error = " + ex.getMessage());
		}
	}

	/**
	 * 选择相册图片
	 * 根据是否启用了多图的开关，来判断是调用自定义相册视图还是系统相册视图
	 * added by songhaitao
	 * @param activity
	 */
	static public void getAlbumImage(Activity activity) {
		getSystemAlbumImage(activity);
	}

	/**
	 * 选择魔图自定义相册图片
	 * 
	 * @param activity
	 */
//	static public void getCustomAlbumImage(Activity activity) {
//		try {
//			MotuInterface.startGalleryActivityForResule(activity,
//					RequestResponseCode.REQUEST_ALBUM_IMAGE, null);
//		} catch (Exception ex) {
//			MeetLog.e("WriteUtil", "getAlbumImage",
//					"error = " + ex.getMessage());
//		}
//	}

	static public void getSystemAlbumImage(Activity activity) {
		try {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			activity.startActivityForResult(intent,
					RequestResponseCode.REQUEST_ALBUM_IMAGE);
		} catch (Exception ex) {
			MeetLog.e("WriteUtil", "getAlbumImage",
					"error = " + ex.getMessage());
		}
	}
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (Exception e) {
			MeetLog.e(e.getMessage());
		}
		return degree;
	}

	static private Bitmap photoResult(int max_size) {
		Bitmap bm = null;
		try {
			int degree = readPictureDegree(FileHelper.getFileDireciory(TMP_IMAGE_NAME));
			bm = BitmapHelper.subSampleBitmap(TMP_IMAGE_NAME, max_size);
			if (degree != 0 && bm != null) {
				bm = BitmapHelper.rotateBitmapBydegree(bm, degree);
			}
		} catch (Exception ex) {
			MeetLog.e("WriteUtil", "photoResult", "error = " + ex.getMessage());
		}
		return bm;
	}

	static private Bitmap AlbumImageResult(Context context, Uri uri,
			int max_size) {
		Bitmap bm = null;
		try {
			bm = BitmapHelper.subSampleBitmap(context, uri, max_size);
		} catch (Exception ex) {
			MeetLog.e("WriteUtil", "AlbumImageResult",
					"error = " + ex.getMessage());
		}
		return bm;
	}

	static public Bitmap ImageResult(int requestCode, Context context, Uri uri,
			int max_size) {
		Bitmap bm = null;
		if (requestCode == RequestResponseCode.REQUEST_CAMERA) {
			bm = photoResult(max_size);
		} else {
			bm = AlbumImageResult(context, uri, max_size);
		}
		return bm;
	}

}
