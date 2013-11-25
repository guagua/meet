package com.baidu.meet.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Hashtable;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
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
public class BdBitmapHelper {
	public static final int ROTATE_LEFT = 0;
	public static final int ROTATE_RIGHT = 1;
	public static final int ROTATE_LEFT_RIGHT = 2;
	public static final int ROTATE_UP_DOWN = 3;

	static private BdBitmapHelper sInstance = null;
	
	private volatile Hashtable<Integer, Bitmap> mBitmapHash = new Hashtable<Integer, Bitmap>();
	private Context mContext = null;
	private Config mBitmapConfig = Bitmap.Config.RGB_565;

	static synchronized public BdBitmapHelper getInstance(){
		if(sInstance == null){
			sInstance = new BdBitmapHelper();
		}
		return sInstance;
	}
	
	/**
	 * 
	 * {@hide}
	 */
	public void initial(Context context){
		mContext = context;
	}
	
	public void setBitmapConfig(Config config){
		mBitmapConfig = config;
	}
	
	private BdBitmapHelper(){}
	
	public Bitmap getCashBitmap(int id) {
		Bitmap bm = mBitmapHash.get(id);
		if (bm != null) {
			return bm;
		} else {
			bm = getResBitmap(mContext, id);
			if (bm != null) {
				mBitmapHash.put(id, bm);
			}
			return bm;
		}
	}

	public void removeCashBitmap(int id) {
		mBitmapHash.remove(id);
	}

	public void clearCashBitmap() {
		mBitmapHash.clear();
	}
	
	public Bitmap getImage(String appath, String filename){
		return BitmapFactory.decodeFile(BdFileHelper.getFilePath(appath, filename));
	}
	
	public Bitmap getImage(String file){
		return BitmapFactory.decodeFile(BdFileHelper.getFilePath(file));
	}
	
	public Bitmap getImageAbsolutePath(String filePath){
		return BitmapFactory.decodeFile(filePath) ;
	}
	
	/**
	 * 在文件夹中保存图片文件
	 * @param path  路径
	 * @param filename  临时文件名
	 * @param bm	图片
	 * @param quality	图片质量
	 * @return	成功：保存文件的全路径 ；  失败：空
	 */	
	public String saveFile(String appath, String filename,Bitmap bm, int quality){
		if(BdFileHelper.checkDir(appath) == false || bm == null){
			return null;
		}
		
		if (BdFileHelper.checkAndMkdirs(appath, filename) != true) {
			return null;
		}
		
		File file = BdFileHelper.getFile(appath, filename);
        try {
        	if(file.exists()){
        		if(file.delete() == false){
        			return null;
        		}
        	}
        	if(file.createNewFile() == false){
        		return null;
        	}
			FileOutputStream fOut = new FileOutputStream(file);
			bm.compress(Bitmap.CompressFormat.JPEG, quality, fOut);
			fOut.flush();
			fOut.close();
			return file.getPath();
		} catch (Exception ex) {
			return null;
		}
	}
	public String saveFile(String file,Bitmap bm, int quality){
		return saveFile(null, file, bm, quality);
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
	public Bitmap getResBitmap(Context context, int resId) {
		Bitmap bm = null;
		try {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			// 由于在16以上用ARGB_4444解码，导致偏色
//			if(android.os.Build.VERSION.SDK_INT >= 16 || mBitmapConfig == null){
//				opt.inPreferredConfig = Bitmap.Config.ARGB_4444;
//			}else{
//				opt.inPreferredConfig = mBitmapConfig;
//			}
			bm = BitmapFactory.decodeResource(context.getResources(), resId, opt);
		} catch (Exception ex) {
		}
		return bm;
	}

	public Bitmap resizeBitmap(Bitmap bitmap, int max_widht, int max_height) {
		if (max_widht <= 0 || max_height < 0 || bitmap == null
				|| bitmap.isRecycled()) {
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
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(temp, temp);
		// matrix.postRotate(45);
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		if (resizedBitmap != bitmap) {
			bitmap.recycle();
		}
		return resizedBitmap;
	}

	public Bitmap getResizedBitmap(Bitmap bitmap, int max_width,
			int max_height) {
		if (max_width <= 0 || max_height < 0 || bitmap == null
				|| bitmap.isRecycled()) {
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
		Matrix matrix = new Matrix();
		// resize the bit map
		float x = (max_width - width * temp) / 2;
		float y = (max_height - height * temp) / 2;
		matrix.postScale(temp, temp);
		matrix.postTranslate(x, y);
		Bitmap resizedBitmap = Bitmap.createBitmap(max_width, max_height,
				bitmap.getConfig());
		Canvas canvas = new Canvas(resizedBitmap);
		canvas.drawBitmap(bitmap, matrix, null);
		return resizedBitmap;
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
	public Bitmap resizeBitmap(Bitmap bitmap, int maxsize) {
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
	public Bitmap getResizedBitmap(Bitmap bitmap, int maxsize) {
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
	public Bitmap resizeBitmap(String appath, String filename, int maxsize) {
		Bitmap b = subSampleBitmap(appath, filename, maxsize);
		return resizeBitmap(b, maxsize);
	}
	public Bitmap resizeBitmap(String file, int maxsize) {
		Bitmap b = subSampleBitmap(null, file, maxsize);
		return resizeBitmap(b, maxsize);
	}

	public Bitmap subSampleBitmap(String appath, String filename, int maxsize) {
		int s = 1;
		if (maxsize <= 0) {
			return null;
		}
		try {
			InputStream in = null;
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inJustDecodeBounds = true;
			in = BdFileHelper.getInStreamFromFile(appath, filename);
			BitmapFactory.decodeStream(in, null, opt);
			opt.inPreferredConfig = mBitmapConfig;
			try {
				in.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			while ((opt.outWidth / (s * 2) > maxsize)
					|| (opt.outHeight / (s * 2) > maxsize)) {
				s *= 2;
			}
			opt.inJustDecodeBounds = false;
			opt.inSampleSize = s;
			in = BdFileHelper.getInStreamFromFile(appath, filename);
			Bitmap b = BitmapFactory.decodeStream(in, null, opt);
			try {
				in.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return b;
		} catch (Exception ex) {
			return null;
		}
	}
	public Bitmap subSampleBitmap(String file, int maxsize) {
		return subSampleBitmap(null, file, maxsize);
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
	public Bitmap resizeBitmap(Context context, Uri uri, int maxsize) {
		Bitmap b = subSampleBitmap(context, uri, maxsize);
		return resizeBitmap(b, maxsize);
	}

	public Bitmap subSampleBitmap(Context context, Uri uri, int maxsize) {
		ContentResolver res = context.getContentResolver();
		ParcelFileDescriptor fd = null;
		int s = 1;

		try {
			fd = res.openFileDescriptor(uri, "r");
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = mBitmapConfig;
			opt.inDither = false;
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null,
					opt);
			while ((opt.outWidth / (s + 1) > maxsize)
					|| (opt.outHeight / (s + 1) > maxsize)) {
				s++;
			}
			opt.inJustDecodeBounds = false;
			opt.inSampleSize = s;
			return BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),
					null, opt);
		} catch (Exception e) {
			try {
				if (fd != null) {
					fd.close();
				}
			} catch (Exception ex) {
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
	public Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		if (bitmap == null) {
			return null;
		}
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), android.graphics.Bitmap.Config.ARGB_4444);
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
		return output;
	}
	
	/**
	 * 获得圆角图片，不带处理ARGB_4444
	 * 
	 * @param bitmap
	 *            原始图片
	 * @param roundPx
	 *            圆角
	 * @return 圆角后的图片
	 */
	public Bitmap getRoundedCornerBitmap2(Bitmap bitmap, float roundPx) {
		if (bitmap == null) {
			return null;
		}
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
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
		return output;
	}

	/**
	 * bitmap 转换图片为 byte[]，存为jpg格式
	 * 
	 * @param bm
	 * @return
	 */
	public byte[] Bitmap2Bytes(Bitmap bm, int quality) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, quality, baos);
		return baos.toByteArray();
	}

	/**
	 * bitmap 转换图片为 byte[]，存为png格式
	 * 
	 * @param bm
	 * @return
	 */
	public byte[] Bitmap2BytesPng(Bitmap bm, int quality) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, quality, baos);
		return baos.toByteArray();
	}

	/**
	 * 字节转换成图片
	 * 
	 * @param b
	 * @return
	 */
	public Bitmap Bytes2Bitmap(byte[] b) {
		if (b != null && b.length != 0) {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = mBitmapConfig;
			return BitmapFactory.decodeByteArray(b, 0, b.length, opt);
		} else {
			return null;
		}
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
		Bitmap returnBm;
		int w = bm.getWidth();
		int h = bm.getHeight();

		Matrix matrix = new Matrix();
		if (direction == ROTATE_LEFT) {
			matrix.postRotate(-90);
		} else if (direction == ROTATE_RIGHT) {
			matrix.postRotate(90);
		}

		returnBm = Bitmap.createBitmap(bm, 0, 0, w, h, matrix, true);

		if (bm != returnBm) {
			bm.recycle();
		}
		
		return returnBm;
	}
	
	public static Bitmap rotateBitmapDegree(Bitmap bm, int degree) {
		Bitmap returnBm;
		int w = bm.getWidth();
		int h = bm.getHeight();

		Matrix matrix = new Matrix();
		matrix.postRotate(degree);

		returnBm = Bitmap.createBitmap(bm, 0, 0, w, h, matrix, true);

		if (bm != returnBm) {
			bm.recycle();
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

		btt = Bitmap.createBitmap(bm, 0, 0, w, h, mx, true);
		mx.setRotate(180);
		returnBm = Bitmap.createBitmap(btt, 0, 0, btt.getWidth(),
				btt.getHeight(), mx, true);

		if (btt != returnBm) {
			btt.recycle();
		}
		if (bm != returnBm) {
			bm.recycle();
		}

		return returnBm;
	}

}
