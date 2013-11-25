package com.baidu.meet.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;
import android.os.StatFs;

/**
 * 文件辅助类
 * 
 * @author zhaolin02
 * 
 * @see 1、约定一级目录结构：SDCarDir/APP_DIR/ 2、所有接口都支持传入二级目录+文件名，path+file
 * 
 * @Note 所有接口的传参约定： appath: 表示应用内部的目录结构（比如，有聊某群组的语音存储/data/group/{gid}/voice
 *       filename: 表示文件名（包含扩展名）
 * 
 */
public class BdFileHelper {
	private static String APP_DIR = "baidu";
	public static final File EXTERNAL_STORAGE_DIRECTORY = Environment.getExternalStorageDirectory();
	public static final int SD_MIN_AVAILAALE_SIZE = 2;

	/**
	 * @brief 错误号定义
	 */
	public static final int ERR_FILE_OK = 0;
	// SD卡相关错误
	public static final int ERR_FILE_NO_SD = 1; // 无法找到存储卡
	public static final int ERR_FILE_SHARED_SD = 2; // 你的存储卡被USB占用，请更改数据线连接方式
	public static final int ERR_FILE_IO_SD = 3; // 存储卡读写失败

	/**
	 * @brief 设置应用根目录 比如："tieba", "local", "baidu/youliao"
	 */
	public static void setTmpDir(String dir) {
		APP_DIR = dir;
	}

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

	/**
	 * @brief 返回错误号
	 */
	public static int getSdError() {
		String status = Environment.getExternalStorageState();
		int error = ERR_FILE_OK;

		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return error;
		}

		if (status.equals(Environment.MEDIA_UNMOUNTED) || status.equals(Environment.MEDIA_UNMOUNTABLE) || status.equals(Environment.MEDIA_REMOVED)) {
			error = ERR_FILE_NO_SD;
		} else if (status.equals(Environment.MEDIA_SHARED)) {
			error = ERR_FILE_SHARED_SD;
		} else {
			error = ERR_FILE_IO_SD;
		}
		return error;
	}

	/**
	 * @brief 获取全路径
	 * @param appath
	 *            : 应用内路径，为null表示使用应用根目录
	 */
	public static String getPath(String appath) {
		String path = null;
		if (appath != null) {
			path = EXTERNAL_STORAGE_DIRECTORY + "/" + APP_DIR + "/" + appath + "/";
		} else {
			path = EXTERNAL_STORAGE_DIRECTORY + "/" + APP_DIR + "/";
		}
		return path;
	}

	/**
	 * @brief 获取文件全路径
	 * @param appath
	 *            : 应用内路径，为null表示使用应用根目录
	 * @param filename
	 *            : 文件名
	 */
	public static String getFilePath(String appath, String filename) {
		String file = null;
		if (appath != null) {
			file = EXTERNAL_STORAGE_DIRECTORY + "/" + APP_DIR + "/" + appath + "/" + filename;
		} else {
			file = EXTERNAL_STORAGE_DIRECTORY + "/" + APP_DIR + "/" + filename;
		}
		return file;
	}

	public static boolean checkSDHasSpace() {
		try {
			StatFs statfs = new StatFs(EXTERNAL_STORAGE_DIRECTORY.getPath());
			long availaBlock = statfs.getAvailableBlocks();
			long blocSize = statfs.getBlockSize();
			long sdFreeSize = availaBlock * blocSize / 1024 / 1024;

			return sdFreeSize > SD_MIN_AVAILAALE_SIZE;
		} catch (Exception ex) {
			return false;
		}
	}

	public static String getFilePath(String file) {
		return getFilePath(null, file);
	}

	/**
	 * @brief 检查文件夹是否存在
	 * @return true：存在； false：不存在
	 */
	public static boolean checkDir(String appath) {
		String dir = getPath(appath);

		if (checkSD() == false) {
			return false;
		}
		File tf = new File(dir);

		if (tf.exists()) {
			return true;
		}

		boolean ret = tf.mkdirs();
		if (ret == false) {
			return false;
		}

		return true;
	}

	private static String getDir(String fullfile) {
		int index = fullfile.lastIndexOf("/");
		if (index > 0 && index < fullfile.length()) {
			return fullfile.substring(0, index);
		}
		return null;
	}

	public static boolean checkAndMkdirs(String appath, String filename) {
		String fullfile = getFilePath(appath, filename);
		String fulldir = getDir(fullfile);
		File fulldirObj = new File(fulldir);
		boolean ret = false;
		if (!fulldirObj.exists()) {
			try {
				ret = fulldirObj.mkdirs();
				if (ret == false) {
					return false;
				}
			} catch (Exception ex) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 检查文件夹中的文件是否存在
	 * 
	 * @return true：存在； false：不存在
	 */
	public static boolean checkFile(String appath, String filename) {
		if (checkSD() == false) {
			return false;
		}
		try {
			File tf = getFile(appath, filename);

			if (tf.exists()) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			return false;
		}
	}

	public static boolean checkFile(String file) {
		return checkFile(null, file);
	}

	/**
	 * 获得文件夹中的文件对象（不区分文件是否存在）
	 * 
	 * @param filename
	 *            文件名
	 * @return 成功：File； 失败：空
	 */
	public static File getFile(String appath, String filename) {
		if (checkDir(appath) == false) {
			return null;
		}

		try {
			String file = getFilePath(appath, filename);
			File fileObj = new File(file);
			return fileObj;
		} catch (SecurityException ex) {
			return null;
		}
	}

	public static File getFile(String file) {
		return getFile(null, file);
	}

	/**
	 * 在文件夹中创建新文件，如果存在先删除
	 * 
	 * @param filename
	 *            新文件名字
	 * @return 成功：File； 失败：空
	 */
	public static File createFile(String appath, String filename) {
		if (checkDir(appath) == false) {
			return null;
		}
		try {
			// 先解决目录问题
			boolean ret = checkAndMkdirs(appath, filename);
			if (ret == false) {
				return null;
			}

			// 再创建文件
			File file = getFile(appath, filename);
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
			return null;
		}
	}

	public static File createFile(String file) {
		return createFile(null, file);
	}

	/**
	 * 在文件夹中创建新文件，如果文件存在直接返回
	 * 
	 * @param filename
	 *            新文件名字
	 * @return 成功：File； 失败：空
	 */
	public static File createFileIfNotFound(String appath, String filename) {
		if (checkDir(appath) == false) {
			return null;
		}
		try {
			File file = getFile(appath, filename);
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
			return null;
		}
	}

	public static File createFileIfNotFound(String file) {
		return createFileIfNotFound(null, file);
	}

	/**
	 * 在文件夹中保存图片文件
	 * 
	 * @param path
	 *            路径
	 * @param filename
	 *            临时文件名
	 * @param imageData
	 *            图片数据
	 * @return 成功：保存文件的全路径 ； 失败：空
	 */
	public static boolean saveGifFile(String appath, String filename, byte[] imageData) {
		if (checkDir(appath) == false) {
			return false;
		}
		if (imageData == null) {
			return false;
		}

		FileOutputStream fOut = null;
		try {

			File file = createFile(appath, filename);
			if (file != null) {
				fOut = new FileOutputStream(file, true);
			} else {
				return false;
			}

			fOut.write(imageData);
			fOut.flush();
			fOut.close();
			fOut = null;
			return true;
		} catch (Exception ex) {
			return false;
		} finally {
			try {
				if (fOut != null) {
					fOut.close();
				}
			} catch (Exception ex2) {
			}
		}
	}

	public static boolean saveGifFile(String file, byte[] imageData) {
		return saveGifFile(null, file, imageData);
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
	public static boolean isGif(String appath, String filename) {
		boolean result = false;
		InputStream fStream = null;
		File file = getFile(appath, filename);
		try {
			fStream = new FileInputStream(file);

			byte[] temp = new byte[7];
			int num = fStream.read(temp, 0, 6);
			if (num == 6) {
				result = BdUtilHelper.isGif(temp);
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

	public static boolean isGif(String file) {
		return isGif(null, file);
	}

	public static boolean saveFile(String appath, String filename, byte[] data) {
		if (checkDir(appath) == false) {
			return false;
		}

		// 先处理创建目录
		if (BdFileHelper.checkAndMkdirs(appath, filename) != true) {
			return false;
		}

		File file = getFile(appath, filename);
		FileOutputStream fOut = null;
		try {
			if (file.exists()) {
				if (file.delete() == false) {
					return false;
				}
			}
			if (file.createNewFile() == false) {
				return false;
			}
			fOut = new FileOutputStream(file);
			fOut.write(data, 0, data.length);
			fOut.flush();
			fOut.close();
			fOut = null;
			return true;
		} catch (IOException ex) {
			return false;
		} finally {
			try {
				if (fOut != null) {
					fOut.close();
				}
			} catch (Exception ex) {
			}
		}
	}

	public static boolean saveFile(String file, byte[] data) {
		return saveFile(null, file, data);
	}

	public static byte[] getFileData(String appath, String filename) {
		if (checkDir(appath) == false) {
			return null;
		}

		File file = getFile(appath, filename);
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
			return null;
		}
	}

	public static byte[] getFileData(String file) {
		return getFileData(null, file);
	}

	public static boolean copyFile(String srcAppath, String srcFilename, String dstAppath, String dstFilename) {
		boolean result = false;
		InputStream fosfrom = null;
		OutputStream fosto = null;
		try {
			File srcFile = getFile(srcAppath, srcFilename);
			File dstFile = getFile(dstAppath, dstFilename);
			if (!srcFile.exists()) {
				return result;
			}
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
		} catch (Exception e) {
		} finally {
			try {
				if (fosfrom != null) {
					fosfrom.close();
				}
			} catch (Exception e) {
			}
			try {
				if (fosto != null) {
					fosto.close();
				}
			} catch (Exception e) {
			}
		}
		return result;
	}

	public static boolean copyFile(String srcFile, String dstFile) {
		return copyFile(null, srcFile, null, dstFile);
	}

	public static boolean renameFile(String srcAppath, String srcFilename, String dstAppath, String dstFilename) {
		boolean result = false;
		try {
			// 确保目录存在，否则逐级创建
			boolean ret = checkAndMkdirs(dstAppath, dstFilename);
			if (ret == false) {
				return result;
			}

			File srcFile = getFile(srcAppath, srcFilename);
			File dstFile = getFile(dstAppath, dstFilename);
			if (!srcFile.exists()) {
				return result;
			}
			if (dstFile.exists()) {
				return result;
			}
			result = srcFile.renameTo(dstFile);
			return result;
		} catch (Exception e) {
		}
		return result;
	}

	public static boolean renameFile(String srcFile, String dstFile) {
		return renameFile(null, srcFile, null, dstFile);
	}

	/**
	 * 获得文件夹中文件的输入流
	 * 
	 * @param filename
	 *            文件名
	 * @return 成功：输入流； 失败：空
	 */
	public static InputStream getInStreamFromFile(String appath, String filename) {
		File f = getFile(appath, filename);
		return getInStreamFromFile(f);
	}

	/**
	 * 获得文件夹中文件的输入流
	 * 
	 * @param file
	 *            文件
	 * @return 成功：输入流； 失败：空
	 */
	public static InputStream getInStreamFromFile(File file) {
		if (file != null) {
			try {
				return new FileInputStream(file);
			} catch (Exception ex) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * 获得文件夹中文件的输出流
	 * 
	 * @param filename
	 *            文件名
	 * @return 成功：输入流； 失败：空
	 */
	public static OutputStream getOutStreamFromFile(String appath, String filename) {
		File f = getFile(appath, filename);
		return getOutStreamFromFile(f);
	}

	/**
	 * 获得文件夹中文件的输出流
	 * 
	 * @param file
	 *            文件
	 * @return 成功：输入流； 失败：空
	 */
	public static OutputStream getOutStreamFromFile(File file) {
		if (file != null) {
			try {
				return new FileOutputStream(file);
			} catch (Exception ex) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * 删除贴吧临时文件夹中的文件
	 * 
	 * @param filename
	 *            文件名
	 * @return 成功：true； 失败：false
	 */
	public static boolean delFile(String appath, String filename) {
		if (checkDir(appath) == false) {
			return false;
		}
		File file = getFile(appath, filename);
		try {
			if (file.exists()) {
				return file.delete();
			} else {
				return false;
			}
		} catch (Exception ex) {
			return false;
		}
	}

	public static boolean delFile(String file) {
		return delFile(null, file);
	}

	public static boolean deleteDir(String appath, String filename) {
		File file = getFile(appath, filename);
		return deleteDir(file);
	}

	private static boolean deleteDir(File dir) {
	    if(null==dir) return false;
		if (dir.isDirectory()) {
			String[] children = dir.list();
			// 递归删除目录中的子目录下
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	public static void writeAmrFileHeader(OutputStream out) throws IOException {
		byte[] header = new byte[6];
		header[0] = '#'; // AMR header
		header[1] = '!';
		header[2] = 'A';
		header[3] = 'M';
		header[4] = 'R';
		header[5] = '\n';
		out.write(header, 0, 6);
	}

	public static void writeWaveFileHeader(DataOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate)
			throws IOException {
		byte[] header = new byte[44];
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = 16; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		out.write(header, 0, 44);
	}
}
