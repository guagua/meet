package com.baidu.meet.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;

import com.baidu.meet.log.MeetLog;
import com.baidu.meet.util.StringUtils;
import com.baidu.meet.MeetApplication;
import com.baidu.meet.R;
import com.baidu.meet.config.Config;

/**
 * 文件辅助类
 * 
 * @author zhaolin02
 * 
 */
public class FileHelper {
	public static final File EXTERNAL_STORAGE_DIRECTORY = Environment
			.getExternalStorageDirectory();
	public static final int FILE_TYPE_VOICE = 1;
	private static final File CACHE_DIR = MeetApplication.getApp()
			.getCacheDir();

	/**
	 * 检查SD卡是否存在
	 * 
	 * @return true：存在； false：不存在
	 */
	public static boolean checkSD() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	public static String getSdErrorString() {
		String status = Environment.getExternalStorageState();
		String error = null;
		if (status.equals(Environment.MEDIA_REMOVED)) {
			error = MeetApplication.getApp().getString(
					R.string.error_no_sdcard);
		} else if (status.equals(Environment.MEDIA_UNMOUNTED)
				|| status.equals(Environment.MEDIA_UNMOUNTABLE)) {
			error = MeetApplication.getApp().getString(
					R.string.error_sd_unmount);
		} else if (status.equals(Environment.MEDIA_SHARED)) {
			error = MeetApplication.getApp().getString(
					R.string.error_sd_shared);
		} else {
			error = MeetApplication.getApp()
					.getString(R.string.error_sd_error);
		}
		return error;
	}

	/**
	 * 检查贴吧客户端临时文件夹是否存在
	 * 
	 * @return true：存在； false：不存在
	 */
	public static boolean CheckTempDir(String path) {
		if (checkSD() == false) {
			return false;
		}
		File tf = new File(path);
		if (!tf.exists()) {
			try {
				return tf.mkdirs();
			} catch (Exception ex) {
				return false;
			}
		} else {
			return true;
		}
	}

	public static boolean CheckTempDir() {
		return CheckTempDir(EXTERNAL_STORAGE_DIRECTORY + "/"
				+ Config.TMPDIRNAME + "/");
	}

	/**
	 * 检查贴吧客户端临时文件夹中的文件是否存在
	 * 
	 * @return true：存在； false：不存在
	 */
	public static boolean CheckFile(String file) {
		if (checkSD() == false) {
			return false;
		}
		try {
			File tf = new File(EXTERNAL_STORAGE_DIRECTORY + "/"
					+ Config.TMPDIRNAME + "/" + file);
			if (tf.exists()) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			MeetLog.e("FileHelper", "CheckFile", "error = " + ex.getMessage());
			return false;
		}
	}

	/**
	 * 检查图片文件是否存在，如果存在返回图片内存占用尺寸；如果不存在返回-1
	 */
	public static long checkImageFileSize(String path, String file) {
		if (checkSD() == false) {
			return -1;
		}

		try {
			File tf = new File(EXTERNAL_STORAGE_DIRECTORY + "/"
					+ Config.TMPDIRNAME + "/" + path + "/" + file);
			if (tf.exists()) {
				// 为了性能，浪费点内存。
				return Config.getBigImageSize();

				// BitmapFactory.Options bounds = new BitmapFactory.Options();
				// bounds.inJustDecodeBounds = true;
				// BitmapFactory.decodeFile(tf.getAbsolutePath(), bounds);
				//
				// if(bounds.outWidth <= 0){
				// //error
				// ////图片的压缩比为 10：1～40：1，获取到文件大小后，需要在乘上1个到Bitmap对象的压缩比。
				// return tf.length() * 40 ;
				// }
				//
				// //一般图片为ARGB_8888占用内存为 长*宽*4
				// http://stackoverflow.com/questions/6681988/bitmap-image-out-of-memory
				// //不过我们的编码为RGB565，是乘上2的
				// return bounds.outWidth * bounds.outHeight * 2 ;
			} else {
				return -1;
			}
		} catch (Exception ex) {
			MeetLog.e("FileHelper", "CheckFile", "error = " + ex.getMessage());
			return -1;
		}
	}

	/**
	 * 获取文件目录路径
	 * 
	 * @param filename
	 * @return
	 */
	public static String getFileDireciory(String filename) {
		if (filename == null) {
			return null;
		}
		return EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME + "/"
				+ filename;
	}

	/**
	 * 获得贴吧临时文件夹中的文件
	 * 
	 * @param filename
	 *            文件名
	 * @return 成功：File； 失败：空
	 */
	public static File GetFile(String filename) {
		if (CheckTempDir() == false) {
			return null;
		}
		File file = new File(EXTERNAL_STORAGE_DIRECTORY + "/"
				+ Config.TMPDIRNAME + "/" + filename);

		try {
			if (file.exists()) {
				return file;
			} else {
				return null;
			}
		} catch (SecurityException ex) {
			MeetLog.e("FileHelper", "GetFile", "error = " + ex.getMessage());
			return null;
		}
	}

	/**
	 * 获得贴吧临时文件夹中的文件
	 * 
	 * @param filename
	 *            文件名
	 * @return 成功：File； 失败：空
	 */
	public static File FileObject(String filename) {
		if (CheckTempDir() == false) {
			return null;
		}
		File file = new File(EXTERNAL_STORAGE_DIRECTORY + "/"
				+ Config.TMPDIRNAME + "/" + filename);
		return file;
	}

	/**
	 * 在贴吧临时文件夹中创建新文件
	 * 
	 * @param filename
	 *            新文件名字
	 * @return 成功：File； 失败：空
	 */
	public static File CreateFile(String filename) {
		if (CheckTempDir() == false) {
			return null;
		}
		File file = new File(EXTERNAL_STORAGE_DIRECTORY + "/"
				+ Config.TMPDIRNAME + "/" + filename);
		try {
			if (file.exists()) {
				if (file.delete() == false) {
					return null;
				}
			}
			if (file.createNewFile() == true) {
				return file;
			} else {
				return null;
			}
		} catch (Exception ex) {
			MeetLog.e("FileHelper", "CreateFile", "error = " + ex.getMessage());
			return null;
		}
	}

	/**
	 * 在贴吧临时文件夹中创建新文件
	 * 
	 * @param filename
	 *            新文件名字
	 * @return 成功：File； 失败：空
	 */
	public static File CreateFileIfNotFound(String filename) {
		if (CheckTempDir() == false) {
			return null;
		}
		File file = new File(EXTERNAL_STORAGE_DIRECTORY + "/"
				+ Config.TMPDIRNAME + "/" + filename);
		try {
			if (file.exists()) {
				return file;
			} else {
				if (file.createNewFile() == true) {
					return file;
				} else {
					return null;
				}
			}
		} catch (Exception ex) {
			MeetLog.e("FileHelper", "CreateFile", "error = " + ex.getMessage());
			return null;
		}
	}

	/**
	 * 创建文件，并且打开输出流
	 * 
	 * @param filename
	 *            贴吧临时文件夹中的文件名字
	 * @return 成功：输出流； 失败：null
	 */
	public static FileOutputStream CreateFileOutputStream(String filename) {
		try {
			File file = CreateFile(filename);
			if (file != null) {
				return new FileOutputStream(file, true);
			} else {
				return null;
			}
		} catch (Exception ex) {
			MeetLog.e("FileHelper", "FileOutputStream",
					"error = " + ex.getMessage());
			return null;
		}
	}

	/**
	 * 在贴吧临时文件夹中保存图片文件
	 * 
	 * @param path
	 *            路径
	 * @param filename
	 *            临时文件名
	 * @param imageData
	 *            图片数据
	 * @return 成功：保存文件的全路径 ； 失败：空
	 */
	public static String SaveGifFile(String path, String filename,
			byte[] imageData) {
		String all_path = null;
		if (path != null) {
			all_path = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
					+ "/" + path + "/";
		} else {
			all_path = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
					+ "/";
		}
		if (CheckTempDir(all_path) == false || imageData == null) {
			return null;
		}
		File file = new File(all_path + filename);
		FileOutputStream fOut = null;
		try {
			if (file.exists()) {
				if (file.delete() == false) {
					return null;
				}
			}
			if (file.createNewFile() == false) {
				return null;
			}
			fOut = new FileOutputStream(file);
			fOut.write(imageData);
			fOut.flush();
			fOut.close();
			fOut = null;
			return file.getPath();
		} catch (Exception ex) {
			MeetLog.e("FileHelper", "SaveGifFile", ex.getMessage());
			return null;
		} finally {
			try {
				if (fOut != null) {
					fOut.close();
				}
			} catch (Exception ex2) {
				MeetLog.e("FileHelper", "SaveGifFile", ex2.getMessage());
			}
		}
	}

	/**
	 * 查看路径下图片是否是Gif
	 * 
	 * @param path
	 *            路径
	 * @param filename
	 *            临时文件名
	 * @return 是：true 否：false
	 */
	public static boolean isGif(String path, String filename) {
		boolean result = false;
		String all_path = null;
		if (path != null) {
			all_path = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
					+ "/" + path + "/";
		} else {
			all_path = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
					+ "/";
		}

		InputStream fStream = null;
		File file = new File(all_path + filename);

		try {
			fStream = new FileInputStream(file);

			byte[] temp = new byte[7];
			int num = fStream.read(temp, 0, 6);
			if (num == 6) {
				result = UtilHelper.isGif(temp);
			}
			if (fStream != null) {
				fStream.close();
				fStream = null;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			try {
				if (fStream != null) {
					fStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;

	}

	/**
	 * 在贴吧临时文件夹中保存图片文件
	 * 
	 * @param path
	 *            路径
	 * @param filename
	 *            临时文件名
	 * @param bm
	 *            图片
	 * @param quality
	 *            图片质量
	 * @return 成功：保存文件的全路径 ； 失败：空
	 */
	public static String SaveFile(String path, String filename, Bitmap bm,
			int quality) {
		if (bm == null)
			return null;

		String all_path = null;
		if (path != null) {
			all_path = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
					+ "/" + path + "/";
		} else {
			all_path = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
					+ "/";
		}
		if (CheckTempDir(all_path) == false || bm == null) {
			return null;
		}
		File file = new File(all_path + filename);
		try {
			if (file.exists()) {
				if (file.delete() == false) {
					return null;
				}
			}
			if (file.createNewFile() == false) {
				return null;
			}
			FileOutputStream fOut = new FileOutputStream(file);
			bm.compress(Bitmap.CompressFormat.JPEG, quality, fOut);
			fOut.flush();
			fOut.close();
			return file.getPath();
		} catch (Exception ex) {
			MeetLog.e("FileHelper", "SaveFile", ex.getMessage());
			return null;
		}
	}

	public static String renameTo(String sPath, String sName, String dPath,
			String dName) {
		String sAllPath = null;
		String dAllPath = null;
		if (sPath != null) {
			sAllPath = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
					+ "/" + sPath + "/";
		} else {
			sAllPath = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
					+ "/";
		}
		if (dPath != null) {
			dAllPath = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
					+ "/" + dPath + "/";
		} else {
			dAllPath = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
					+ "/";
		}
		if (CheckTempDir(sAllPath) == false || CheckTempDir(dAllPath) == false) {
			return null;
		}
		File sFile = new File(sAllPath + sName);
		File dFile = new File(dAllPath + dName);
		if (sFile.renameTo(dFile) == false) {
			return null;
		}
		return dFile.getAbsolutePath();
	}

	public static Bitmap getImage(String path, String filename) {
		Bitmap bm = null;
		String all_path = null;
		if (path != null) {
			all_path = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
					+ "/" + path + "/";
		} else {
			all_path = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
					+ "/";
		}
		try {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = Config.BitmapConfig;
			bm = BitmapFactory.decodeFile(all_path + filename, opt);
		} catch (OutOfMemoryError e) {

			// GC & retry.
			// http://stackoverflow.com/questions/7138645/catching-outofmemoryerror-in-decoding-bitmap
			System.gc();
			try {
				bm = BitmapFactory.decodeFile(all_path + filename);
			} catch (OutOfMemoryError e2) {
				// handle gracefully.
			}
		}

		return bm;
	}

	public static String SaveFile(String filename, byte[] data) {
		return SaveFile(null, filename, data);
	}

	public static String SaveFile(String filename, byte[] data, int file_type) {
		if (checkSD()) {
			return SaveFile(null, getFilePath(filename, file_type, false), data);
		} else {
			return SaveTempFile(file_type, filename, data);
		}
	}

	public static String SaveFile(String path, String filename, byte[] data) {
		String all_path = null;
		if (path != null) {
			all_path = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
					+ "/" + path + "/";
		} else {
			all_path = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
					+ "/";
		}
		if (CheckTempDir(all_path) == false || data == null || filename == null) {
			return null;
		}
		File file = new File(all_path + filename);
		String s_parent = file.getParent();
		if (!StringUtils.isNull(s_parent)) {
			File parent = new File(s_parent);
			if (!parent.exists()) {
				parent.mkdirs();
			}
		}
		FileOutputStream fOut = null;
		try {
			if (file.exists()) {
				if (file.delete() == false) {
					return null;
				}
			}
			if (file.createNewFile() == false) {
				return null;
			}
			fOut = new FileOutputStream(file);
			fOut.write(data, 0, data.length);
			fOut.flush();
			fOut.close();
			fOut = null;
			return file.getPath();
		} catch (IOException ex) {
			MeetLog.e("FileHelper", "SaveFile", "error = " + ex.getMessage());
			return null;
		} finally {
			try {
				if (fOut != null) {
					fOut.close();
				}
			} catch (Throwable ex) {
				MeetLog.e("FileHelper", "SaveFile",
						"error = " + ex.getMessage());
			}
		}
	}

	public static byte[] GetFileData(String path, String filename) {
		if (CheckTempDir() == false || filename == null) {
			return null;
		}
		String all_path = null;
		if (path != null) {
			all_path = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
					+ "/" + path + "/";
		} else {
			all_path = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
					+ "/";
		}
		File file = new File(all_path + filename);
		try {
			if (!file.exists()) {
				return null;
			}
			FileInputStream fStream = new FileInputStream(file);
			ByteArrayOutputStream outputstream = new ByteArrayOutputStream(1024);
			byte[] temp = new byte[1024];
			int num = -1;
			while ((num = fStream.read(temp, 0, 1024)) != -1) {
				outputstream.write(temp, 0, num);
			}
			if (fStream != null)
				fStream.close();
			return outputstream.toByteArray();
		} catch (IOException ex) {
			MeetLog.e("FileHelper", "GetFileData",
					"error = " + ex.getMessage());
			return null;
		}
	}

	/**
	 * 获取完整路径
	 * 
	 * @param path
	 * @return
	 */
	private static String getPrefixPath(String path, boolean isAutoPrefix) {
		String reslut = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
				+ "/" + path;
		if (isAutoPrefix) {
			if (path.startsWith(EXTERNAL_STORAGE_DIRECTORY.toString())) {
				reslut = path;
			}
		}
		return reslut;
	}
	public static boolean CopyFile(String srcPath, String dstPath,
			boolean isAutoPrefix) {
		boolean result = false;
		InputStream fosfrom = null;
		OutputStream fosto = null;
		String fromPath = null;
		String toPath = null;
		fromPath = getPrefixPath(srcPath, isAutoPrefix);
		toPath = getPrefixPath(dstPath, isAutoPrefix);
		try {
			File srcFile = new File(fromPath);
			File dstFile = new File(toPath);
			if (!srcFile.exists())
				return result;
			fosfrom = new FileInputStream(srcFile);
			fosto = new FileOutputStream(dstFile);
			byte bt[] = new byte[1024];
			int c;
			while ((c = fosfrom.read(bt)) > 0) {
				fosto.write(bt, 0, c);
			}
			fosfrom.close();
			fosfrom = null;
			fosto.close();
			fosto = null;
		} catch (Throwable e) {
			MeetLog.e("FileHelper", "CopyFile", e.toString());
		} finally {
			try {
				if (fosfrom != null) {
					fosfrom.close();
				}
			} catch (Throwable e) {
				MeetLog.e("FileHelper", "CopyFile", e.toString());
			}
			try {
				if (fosto != null) {
					fosto.close();
				}
			} catch (Throwable e) {
				MeetLog.e("FileHelper", "CopyFile", e.toString());
			}
		}
		return result;
	}

	public static boolean CopyFile(String srcPath, String dstPath) {
		return CopyFile(srcPath, dstPath, false);
	}

	/**
	 * 获得贴吧临时文件夹中文件的输入流
	 * 
	 * @param filename
	 *            文件名
	 * @return 成功：输入流； 失败：空
	 */
	public static InputStream GetStreamFromFile(String filename) {
		return GetStreamFromFile(GetFile(filename));
	}

	@SuppressWarnings("finally")
	public static InputStream GetStreamFromTmpFile(String tmpfilename) {
		File file = new File(tmpfilename);
		try {
			if (!file.exists()) {
				file = null;
			}
		} catch (SecurityException ex) {
			MeetLog.e("FileHelper", "GetFile", "error = " + ex.getMessage());
			file = null;
		} finally {
			return GetStreamFromFile(file);
		}
	}

	/**
	 * 获得贴吧临时文件夹中文件的输入流
	 * 
	 * @param file
	 *            文件
	 * @return 成功：输入流； 失败：空
	 */
	public static InputStream GetStreamFromFile(File file) {
		if (file != null) {
			try {
				return new FileInputStream(file);
			} catch (Throwable ex) {
				MeetLog.e("FileHelper", "GetStreamFromFile",
						"error = " + ex.getMessage());
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * 获得贴吧临时文件夹中的图片
	 * 
	 * @param filename
	 *            文件名
	 * @param maxsize
	 *            图片的最大尺寸
	 * @return 成功：图片； 失败：空
	 */
	public static Bitmap GetBitmapFromFile(String filename, int maxsize) {
		String filepath = EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME
				+ "/" + filename;
		return BitmapHelper.resizeBitmap(filepath, maxsize);
	}

	/**
	 * 删除贴吧临时文件夹中的文件
	 * 
	 * @param filename
	 *            文件名
	 * @return 成功：true； 失败：false
	 */
	public static boolean DelFile(String filename) {
		if (CheckTempDir() == false) {
			return false;
		}
		File file = new File(EXTERNAL_STORAGE_DIRECTORY + "/"
				+ Config.TMPDIRNAME + "/" + filename);
		try {
			if (file.exists()) {
				return file.delete();
			} else {
				return false;
			}
		} catch (Throwable ex) {
			MeetLog.e("FileHelper", "DelFile", "error = " + ex.getMessage());
			return false;
		}
	}

	/**
	 * 返回临时文件夹公共部分的路径
	 * 
	 * @return cache文件夹路径
	 */
	public static String getCacheDir() {
		return EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME + "/";
	}

	/**
	 * 文件重命名
	 * 
	 * @param srcPath
	 *            文件原路径
	 * @param dstPath
	 *            文件新路径
	 * @return 成功：true； 失败：false
	 */
	public static boolean renameTo(String srcPath, String dstPath) {
		File src = new File(srcPath);
		File dst = new File(dstPath);
		String s_parent = dst.getParent();
		if (!StringUtils.isNull(s_parent)) {
			File parent = new File(s_parent);
			if (!parent.exists()) {
				parent.mkdirs();
			}
		}
		if (!src.exists()) {
			return false;
		}
		return src.renameTo(dst);
	}

	/**
	 * 获得剩余空间(有SD卡时返回SD卡剩余空间，否则返回机身剩余空间。)
	 * 
	 * @return 剩余空间(字节)
	 */
	public static long getAvailableSize() {
		String path = null;
		if (checkSD()) {
			path = Environment.getExternalStorageDirectory().getAbsolutePath();
		} else {
			path = Environment.getRootDirectory().getAbsolutePath();
		}
		if (path == null)
			return 0;
		StatFs stat = new StatFs(path);
		return stat.getAvailableBlocks() * (long) stat.getBlockSize();
	}

	/**
	 * 获取内置存储中临时文件全路径
	 * 
	 * @param file_type
	 *            文件类别
	 * @param suffix
	 *            后缀（key）
	 * @return 文件路径，如果不存在则返回null。
	 */
	public static String getTempFilePath(int file_type, String key) {
		return getTempFilePath(file_type, key, false);
	}

	private static String getTempFilePath(int file_type, String key,
			boolean isClean) {

		if (CACHE_DIR == null)
			return null;
		File[] files = CACHE_DIR.listFiles();
		String prefix = getPrefixByType(file_type);

		for (int i = 0; i < files.length; i++) {
			if (files[i] != null && files[i].getName().startsWith(prefix)) {
				if (files[i].getName().endsWith(key)) {
					return files[i].getAbsolutePath();
				} else {
					if (isClean) {
						files[i].delete();
					}
				}

			}
		}
		return null;
	}

	private static String getPrefixByType(int file_type) {
		return "";
	}

	public static String SaveTempFile(int file_type, String key, byte[] data) {
		if (key == null || data == null || data.length == 0) {
			return null;
		}
		String path = getTempFilePath(file_type, key, true);
		if (path != null) {
			return path;
		}
		String prefix = getPrefixByType(file_type);
		File file = null;
		FileOutputStream fOut = null;
		try {
			file = File.createTempFile(prefix, key, CACHE_DIR);
			if (file == null) {
				return null;
			}
			fOut = new FileOutputStream(file);
			fOut.write(data, 0, data.length);
			fOut.flush();
			fOut.close();
			fOut = null;
			return file.getPath();
		} catch (IOException ex) {
			MeetLog.e("FileHelper", "SaveFile", "error = " + ex.getMessage());
			return null;
		} finally {
			try {
				if (fOut != null) {
					fOut.close();
				}
			} catch (Throwable ex) {
				MeetLog.e("FileHelper", "SaveFile",
						"error = " + ex.getMessage());
			}
		}
	}

	/**
	 * 获取缓存文件绝对路径（文件必须存在,否则返回null）
	 * 
	 * @param key
	 *            关键字
	 * @param file_type
	 *            文件类型
	 * @return file path. if file doesn't exist, return null.
	 */
	public static String getStoreFile(String key, int file_type) {
		if (key == null)
			return null;
		if (FileHelper.checkSD()) {
			if (!CheckFile(getFilePath(key, file_type, false))) {
				return null;
			} else
				return getFilePath(key, file_type, true);
		} else {
			return getTempFilePath(FileHelper.FILE_TYPE_VOICE, key);
		}
	}

	/**
	 * 获取外置缓存文件相对路径（文件可以不存在）
	 * 
	 * @return cache dir path
	 */
	public static String getFilePath(String key, int file_type, boolean fullpath) {
		// Check external available first
		String path = null;
		if (FileHelper.checkSD()) {
			StringBuilder builder = new StringBuilder();
			if (fullpath) {
				builder.append(getCacheDir());
			}
			builder.append(getPrefixByType(file_type));
			builder.append(File.separator);
			builder.append(key);
			path = builder.toString();
		}
		return path;
	}
	/**
	 * 获得SD剩余空间（内部接口）
	 * 
	 * @return 剩余空间(字节)
	 */
	public static long SDAvailableSize() {
		String path = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			path = Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		if (path == null)
			return 0;
		StatFs stat = new StatFs(path);
		return stat.getAvailableBlocks() * (long) stat.getBlockSize();
	}

	/**
	 * 获取文件大小
	 * 
	 * @author chenrensong
	 * @param f
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	public static long getFileSize(File f) {
		long s = 0;
		try {
			if (f.exists()) {
				FileInputStream fis = new FileInputStream(f);
				s = fis.available();
			}
		} catch (Exception ex) {
			s = 0;
		}
		return s;
	}

	public static boolean deleteFile(File f) {
		boolean isSuccess = false;
		try {
			if (f.delete()) {
				isSuccess = true;
			}
		} catch (Exception ex) {
			isSuccess = false;
		}
		return isSuccess;
	}

	public static long getDirectorySize(String path, boolean isIgnoreDirectory) {
		return getDirectorySize(new File(path), isIgnoreDirectory);
	}

	/**
	 * 获取文件夹大小
	 * 
	 * @author chenrensong
	 * @param f
	 * @param isIgnoreDirectory
	 *            忽略字文件夹
	 * @return
	 */
	public static long getDirectorySize(File f, boolean isIgnoreDirectory) {
		long size = 0;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory() && !isIgnoreDirectory) {
				size = size + getDirectorySize(flist[i], false);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}

	/**
	 * 确保目录存在
	 * 
	 * @author chenrensong
	 * @param filePath
	 */
	public static void makeRootDirectory(String filePath) {
		File file = null;
		try {
			file = new File(filePath);
			if (!file.exists()) {
				file.mkdir();
			}
		} catch (Exception e) {

		}
	}
}
