package com.baidu.meet.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Hashtable;

import com.baidu.meet.MeetApplication;
import com.baidu.meet.log.MeetLog;
import com.baidu.meet.config.Config;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

/**
 * 图片辅助类
 * 
 * @author zhaolin02
 * 
 */
public class BitmapHelper {
	public static final int ROTATE_LEFT = 0;
	public static final int ROTATE_RIGHT = 1;
	public static final int ROTATE_LEFT_RIGHT = 2;
	public static final int ROTATE_UP_DOWN = 3;
	
	public static final int FILE_2_BITMAP_MUL = 10;

	/*
	 * 同步锁，用于让所有图片编解码操作串行执行，减少对内存的消耗，降低OOM异常。
	 */
	public static final Object lockForSyncImageDecoder = new Object();

	static private volatile Hashtable<Integer, Bitmap> mBitmapHash = new Hashtable<Integer, Bitmap>();

	public static Bitmap getCashBitmap(int id) {
		Bitmap bm = mBitmapHash.get(id);
		if (bm != null) {
			return bm;
		} else {
			bm = getResBitmap(MeetApplication.getApp(), id);
			if (bm != null) {
				mBitmapHash.put(id, bm);
			}
			return bm;
		}
	}
	
	/**
	 * 获得bitmap占用的内存大小
	 */
	public static int getBitmapSize(Bitmap bitmap) {
		if(bitmap == null) return 0 ;
		
		return bitmap.getRowBytes() * bitmap.getHeight() ;
	}

	public static void removeCashBitmap(int id) {
		mBitmapHash.remove(id);
	}

	public static void clearCashBitmap() {
		mBitmapHash.clear();
	}

	/**
	 * 获取Logo图片
	 * 
	 * @param context
	 *            上下文
	 * @param resId
	 *            资源id
	 * @return 图片
	 */
	public static Bitmap getLogoBitmap(Context context, int resId) {
		Bitmap bm = null;
		try {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = Config.BitmapConfig;

			bm = BitmapFactory.decodeResource(context.getResources(), resId, opt);

		} catch (Throwable ex) {
			MeetLog.e("BitmapHelper", "getResBitmap", "error = " + ex.getMessage());
		}
		return bm;
	}

	/**
	 * 获取图片资源
	 * 
	 * @param context
	 *            上下文
	 * @param resId
	 *            资源id
	 * @return 图片
	 */
	public static Bitmap getResBitmap(Context context, int resId) {
		Bitmap bm = null;
		try {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			//FIXME 在安卓4.1.1手机上，使用565，正方形的png图会解析失败，所以改回4444
//			if (android.os.Build.VERSION.SDK_INT >= 16) {
//				opt.inPreferredConfig = Bitmap.Config.ARGB_4444;
//			} else {
//				opt.inPreferredConfig = Config.BitmapConfig;
//			}
			
//			synchronized (lockForSyncImageDecoder) {
				bm = BitmapFactory.decodeResource(context.getResources(), resId, opt);
//			}
			
		} catch (Throwable ex) {
			MeetLog.e("BitmapHelper", "getResBitmap", "error = " + ex.getMessage());
		}
		return bm;
	}

	public static Bitmap resizeBitmap(Bitmap bitmap, int max_widht, int max_height) {
		if (max_widht <= 0 || max_height < 0 || bitmap == null || bitmap.isRecycled()) {
			return null;
		}
		if (bitmap.getWidth() <= max_widht && bitmap.getHeight() <= max_height) {
			return bitmap;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float temp = 0;
		if (((float) max_height / (float) height) > (((float) max_widht) / (float) width)) {
			temp = (((float) max_widht) / (float) width);
		} else {
			temp = ((float) max_height / (float) height);
		}
		
		synchronized (lockForSyncImageDecoder) {
			Matrix matrix = new Matrix();
			// resize the bit map
			matrix.postScale(temp, temp);
			// matrix.postRotate(45);
			Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
			if (resizedBitmap != bitmap) { //createBitmap中有可能产生同一个对象， 如果不是同一个对象， 就将原对象recycle。
				bitmap.recycle();
			}
			return resizedBitmap;
		}
	}

	public static Bitmap getResizedBitmap(Bitmap bitmap, int max_width, int max_height) {
		if (max_width <= 0 || max_height < 0 || bitmap == null || bitmap.isRecycled()) {
			return null;
		}
		if (bitmap.getWidth() <= max_width && bitmap.getHeight() <= max_height) {
			return bitmap;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float temp = 0;
		if (((float) max_height / (float) height) < (((float) max_width) / (float) width)) {
			temp = (((float) max_width) / (float) width);
		} else {
			temp = ((float) max_height / (float) height);
		}
		
		synchronized (lockForSyncImageDecoder) {
			Matrix matrix = new Matrix();
			// resize the bit map
			float x = (max_width - width * temp) / 2;
			float y = (max_height - height * temp) / 2;
			matrix.postScale(temp, temp);
			matrix.postTranslate(x, y);
			Bitmap resizedBitmap = Bitmap.createBitmap(max_width, max_height, bitmap.getConfig());
			Canvas canvas = new Canvas(resizedBitmap);
			canvas.drawBitmap(bitmap, matrix, null);
			return resizedBitmap;
		}
	}
	
	public static Bitmap getResizedBitmapFillCenter(Bitmap bitmap, int max_width, int max_height) {
		if (max_width <= 0 || max_height < 0 || bitmap == null || bitmap.isRecycled()) {
			return null;
		}
		if (bitmap.getWidth() <= max_width && bitmap.getHeight() <= max_height) {
			return bitmap;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float temp = 0;
		if (((float) max_height / (float) height) > (((float) max_width) / (float) width)) {
			temp = (((float) max_width) / (float) width);
		} else {
			temp = ((float) max_height / (float) height);
		}
		
		synchronized (lockForSyncImageDecoder) {
			Matrix matrix = new Matrix();
			matrix.postScale(temp, temp);
			Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
			return resizedBitmap;
		}
	}

	/**
	 * 等比缩小给定的图片，不保留原图片
	 * 
	 * @param bitmap
	 *            需要缩小的图片
	 * @param maxsize
	 *            缩小后的最大值
	 * @return 缩小后的图片
	 */
	public static Bitmap resizeBitmap(Bitmap bitmap, int maxsize) {
		return resizeBitmap(bitmap, maxsize, maxsize);
	}

	/**
	 * 等比缩小给定的图片,保留原图片,截取正方形图片
	 * 
	 * @param bitmap
	 *            需要缩小的图片
	 * @param maxsize
	 *            缩小后的最大值
	 * @return 缩小后的图片
	 */
	public static Bitmap getResizedBitmap(Bitmap bitmap, int maxsize) {
		return getResizedBitmap(bitmap, maxsize, maxsize);
	}

	/**
	 * 等比缩小给定的图片文件
	 * 
	 * @param file_name
	 *            需要缩小的图片全路径
	 * @param maxsize
	 *            缩小后的最大值
	 * @return 缩小后的图片
	 */
	public static Bitmap resizeBitmap(String file_name, int maxsize) {
		Bitmap b = subSampleBitmap(file_name, maxsize);
		return resizeBitmap(b, maxsize);
	}

	public static Bitmap subSampleBitmap(String file_name, int maxsize) {
		int s = 1;
		if (file_name == null || file_name.length() <= 0 || maxsize <= 0) {
			return null;
		}
		try {
			synchronized (lockForSyncImageDecoder) {
				InputStream in = null;
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inJustDecodeBounds = true;
				in = FileHelper.GetStreamFromFile(file_name);
				BitmapFactory.decodeStream(in, null, opt);
				opt.inPreferredConfig = Config.BitmapConfig;
				CloseUtil.close(in) ;
				
				while ((opt.outWidth / (s * 2) > maxsize) || (opt.outHeight / (s * 2) > maxsize)) {
					s *= 2;
				}
				opt.inJustDecodeBounds = false;
				opt.inSampleSize = s;
				in = FileHelper.GetStreamFromFile(file_name);
				Bitmap b = BitmapFactory.decodeStream(in, null, opt);
				CloseUtil.close(in) ;
				
				return b;
			}
		} catch (Throwable ex) {
			return null;
		}
	}

	/**
	 * 等比缩小给定的图片文件
	 * 
	 * @param context
	 *            上下文
	 * @param file_name
	 *            需要缩小的图片的URI
	 * @param maxsize
	 *            缩小后的最大值
	 * @return 缩小后的图片
	 */
	public static Bitmap resizeBitmap(Context context, Uri uri, int maxsize) {
		Bitmap b = subSampleBitmap(context, uri, maxsize);
		return resizeBitmap(b, maxsize);
	}

	public static Bitmap subSampleBitmap(Context context, Uri uri, int maxsize) {
		ContentResolver res = context.getContentResolver();
		ParcelFileDescriptor fd = null;
		int s = 1;

		try {
			fd = res.openFileDescriptor(uri, "r");
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = Config.BitmapConfig;
			opt.inDither = false;
			opt.inJustDecodeBounds = true;
			
			synchronized (lockForSyncImageDecoder) {
				BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, opt);
				while ((opt.outWidth / (s + 1) > maxsize) || (opt.outHeight / (s + 1) > maxsize)) {
					s++;
				}
				opt.inJustDecodeBounds = false;
				opt.inSampleSize = s;
				return BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, opt);
			}
		} catch (Throwable e) {
			try {
				if (fd != null) {
					fd.close();
				}
			} catch (Throwable ex) {
			}
			return null;
		}
	}

	/**
	 * 获得圆角图片
	 * 
	 * @param bitmap
	 *            原始图片
	 * @param roundPx
	 *            圆角
	 * @return 圆角后的图片
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		Bitmap output = null;
		try {
			synchronized (lockForSyncImageDecoder) {
				output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), android.graphics.Bitmap.Config.ARGB_4444);
				Canvas canvas = new Canvas(output);
				final int color = 0xff424242;
				final Paint paint = new Paint();
				final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
				final RectF rectF = new RectF(rect);
				paint.setAntiAlias(true);
				canvas.drawARGB(0, 0, 0, 0);
				paint.setColor(color);
	
				canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
				paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
				canvas.drawBitmap(bitmap, rect, rect, paint);
			}
			
			if(output != bitmap){
				bitmap.recycle() ;
				bitmap = null ;
			}
			
		} catch (Throwable e) {
			
		}
		
		return output;
	}

	/**
	 * 获得圆角图片
	 * 
	 * @param bitmap
	 *            原始图片
	 * @param roundPx
	 *            圆角
	 * @return 截取正方形
	 */
	public static Bitmap getSquareBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		
		synchronized (lockForSyncImageDecoder) {
			Bitmap old = bitmap ;
			
			if (bitmap.getHeight() < bitmap.getWidth()) {
				int offset = (bitmap.getWidth() - bitmap.getHeight()) >> 1;
				bitmap = Bitmap.createBitmap(bitmap, offset, 0, bitmap.getHeight(), bitmap.getHeight());
			} else if (bitmap.getHeight() > bitmap.getWidth()) {
				int offset = (bitmap.getHeight() - bitmap.getWidth()) >> 1;
				bitmap = Bitmap.createBitmap(bitmap, 0, offset, bitmap.getWidth(), bitmap.getWidth());
			}
			
			if(old != bitmap){
				old.recycle() ;
				old = null ;
			}
			
			return bitmap;
		}
	}

	/**
	 * bitmap 转换图片为 byte[]
	 * 
	 * @param bm
	 * @return
	 */
	public static byte[] Bitmap2Bytes(Bitmap bm, int quality) {
		synchronized (lockForSyncImageDecoder) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bm.compress(Bitmap.CompressFormat.PNG, quality, baos);
			return baos.toByteArray();
		}
	}

	/**
	 * 字节转换成图片
	 * 
	 * @param b
	 * @return
	 */
	public static Bitmap Bytes2Bitmap(byte[] b) {
		Bitmap bm = null;
		if (b != null && b.length != 0) {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = Config.BitmapConfig;
			try {
				synchronized (lockForSyncImageDecoder) {
					bm = BitmapFactory.decodeByteArray(b, 0, b.length, opt);
				}
			} catch (OutOfMemoryError e) {
			}
		}
		return bm;
	}

	/**
	 * 旋转图片
	 * 
	 * @param bm
	 *            需要旋转的图片
	 * @param degrees
	 *            度数
	 * @return 旋转后的图片
	 */
	public static Bitmap rotateBitmap(Bitmap bm, int direction) {
		Bitmap returnBm = null;
		int w = bm.getWidth();
		int h = bm.getHeight();

		synchronized (lockForSyncImageDecoder) {
			Matrix matrix = new Matrix();
			if (direction == ROTATE_LEFT) {
				matrix.postRotate(-90);
			} else if (direction == ROTATE_RIGHT) {
				matrix.postRotate(90);
			}
			try {
				returnBm = Bitmap.createBitmap(bm, 0, 0, w, h, matrix, true);
			} catch (OutOfMemoryError e) {
			}
			if (returnBm == null) {
				returnBm = bm;
			}
			if (bm != returnBm) {
				bm.recycle();
			}
		}

		return returnBm;
	}

	public static Bitmap rotateBitmapBydegree(Bitmap bm, int degree) {
		Bitmap returnBm = null;
		int w = bm.getWidth();
		int h = bm.getHeight();

		synchronized (lockForSyncImageDecoder) {
			Matrix matrix = new Matrix();
			matrix.postRotate(degree);
			try {
				returnBm = Bitmap.createBitmap(bm, 0, 0, w, h, matrix, true);
			} catch (OutOfMemoryError e) {
			}
			if (returnBm == null) {
				returnBm = bm;
			}
			if (bm != returnBm) {
				bm.recycle();
			}
		}
		return returnBm;
	}

	/**
	 * 翻转图片
	 * 
	 * @param bm
	 *            需要旋转的图片
	 * @param degrees
	 *            度数
	 * @return 翻转后的图片
	 */
	public static Bitmap reversalBitmap(Bitmap bm, int direction) {
		Bitmap returnBm;
		Bitmap btt;
		Matrix mx = new Matrix();
		int w = bm.getWidth();
		int h = bm.getHeight();

		// 产生镜像
		if (direction == ROTATE_LEFT_RIGHT) {
			mx.setScale(1, -1);
		} else if (direction == ROTATE_UP_DOWN) {
			mx.setScale(-1, 1);
		}

		synchronized (lockForSyncImageDecoder) {
			btt = Bitmap.createBitmap(bm, 0, 0, w, h, mx, true);
			mx.setRotate(180);
			returnBm = Bitmap.createBitmap(btt, 0, 0, btt.getWidth(), btt.getHeight(), mx, true);
	
			if (btt != returnBm) {
				btt.recycle();
			}
			if (bm != returnBm) {
				bm.recycle();
			}
		}

		return returnBm;
	}


}
