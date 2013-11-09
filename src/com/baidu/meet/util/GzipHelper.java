package com.baidu.meet.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 压缩辅助类
 * @author zhaolin02
 *
 */
public class GzipHelper{
	static final int BUFFERSIZE = 1024;
	
	/**
	 * 数据压缩
	 * 
	 * @param is
	 * @param os
	 * @throws Exception
	 */
	public static void compress(InputStream is, OutputStream os) throws Exception {

		GZIPOutputStream gos = null;
		try {
			gos = new GZIPOutputStream(os);
			int count;
			byte data[] = new byte[BUFFERSIZE];
			while ((count = is.read(data, 0, BUFFERSIZE)) != -1) {
				gos.write(data, 0, count);
			}
			gos.flush();
		} finally{
			CloseUtil.close(gos);
		}
	}

	/**
	 * 数据解压缩
	 * 
	 * @param is
	 * @param os
	 * @throws Exception
	 */
	public static void decompress(InputStream is, OutputStream os) throws Exception {

		GZIPInputStream gis = null;
		try {
			gis = new GZIPInputStream(is);
			int count;
			byte data[] = new byte[BUFFERSIZE];
			while ((count = gis.read(data, 0, BUFFERSIZE)) != -1) {
				os.write(data, 0, count);
			}
		}  finally{
			CloseUtil.close(gis);
		}
	} 
}
