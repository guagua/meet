package com.baidu.meet.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baidu.meet.MeetApplication;
import com.baidu.meet.R;
import com.baidu.meet.log.MeetLog;

import android.graphics.Color;

/**
 * 字符串辅助类
 * 
 * @author zhaolin02
 * 
 */
public class StringHelper {

	private static SimpleDateFormat FORMATE_DATE_ALL = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static SimpleDateFormat FORMATE_DATE_YEAR = new SimpleDateFormat("yyyy年");
	private static SimpleDateFormat FORMATE_DATE_TIME = new SimpleDateFormat("HH:mm");
	private static SimpleDateFormat FORMATE_DATE_MOUTH = new SimpleDateFormat("M月d日");
	private static SimpleDateFormat FORMATE_DATE_MOUTH_TIME = new SimpleDateFormat("M月d日 HH:mm");
	private static SimpleDateFormat FORMATE_DATE_DAY = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat FORMATE_DATE_DAY_1 = new SimpleDateFormat("yy-M-d");
	private static SimpleDateFormat FORMATE_DATE_MS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static SimpleDateFormat FORMATE_DATE_DAY_NO_YEAR = new SimpleDateFormat("yyyy-MM-dd");

	private static long MS_TO_DAY = 1000 * 60 * 60 *24;
	private static long MS_TO_HOUR = 1000 * 60 * 60;
	private static long MS_TO_MIN = 1000 * 60;
	private static long MS_TO_SEC = 1000;
	private static String HOUR_BEFORE = MeetApplication.getApp().getString(R.string.time_hour_before);
	private static String MIN_BEFORE = MeetApplication.getApp().getString(R.string.time_min_before);
	private static String SEC_BEFORE = MeetApplication.getApp().getString(R.string.time_sec_before);
	private static Date date = new Date();


	static{
		TimeZone CN_ZONE = TimeZone.getTimeZone("GMT+8");
		if (CN_ZONE != null) {
			FORMATE_DATE_ALL.setTimeZone(CN_ZONE);
			FORMATE_DATE_YEAR.setTimeZone(CN_ZONE);
			FORMATE_DATE_TIME.setTimeZone(CN_ZONE);
			FORMATE_DATE_MOUTH.setTimeZone(CN_ZONE);
			FORMATE_DATE_MOUTH_TIME.setTimeZone(CN_ZONE);
			FORMATE_DATE_DAY.setTimeZone(CN_ZONE);
			FORMATE_DATE_DAY_1.setTimeZone(CN_ZONE);
			FORMATE_DATE_MS.setTimeZone(CN_ZONE);
			FORMATE_DATE_DAY_NO_YEAR.setTimeZone(CN_ZONE);
		}
	}
	
	/**
	 * 返回时间字符串，格式HH:mm
	 */
	public static String getDateStringMdHm(Date date) {
		synchronized (FORMATE_DATE_MOUTH_TIME) {
			return FORMATE_DATE_MOUTH_TIME.format(date);
		}
	}

	/**
	 * 返回时间字符串，格式HH:mm
	 */
	public static String getDateStringHm(Date date) {
		synchronized (FORMATE_DATE_TIME) {
			return FORMATE_DATE_TIME.format(date);
		}
	}

	public static String getDateStringYear(Date date) {
		synchronized (FORMATE_DATE_YEAR) {
			return FORMATE_DATE_YEAR.format(date);
		}
	}

	public static String getDateStringMouth(Date date) {
		synchronized (FORMATE_DATE_MOUTH) {
			return FORMATE_DATE_MOUTH.format(date);
		}
	}

	public static String getDateStringDay(Date date) {
		synchronized (FORMATE_DATE_DAY) {
			return FORMATE_DATE_DAY.format(date);
		}
	}

	public static String getDateStringDay1(Date date) {
		synchronized (FORMATE_DATE_DAY_1) {
			return FORMATE_DATE_DAY_1.format(date);
		}
	}

	/**
	 * 返回时间字符串，格式yyyy-mm-dd hh:mm:ss
	 * 
	 * @return 当前的时间字符串
	 */
	public static String getTimeString(long time) {
		Date date = new Date(time);
		synchronized (FORMATE_DATE_ALL) {
			return FORMATE_DATE_ALL.format(date);
		}
	}

	public static String getCurrentString() {
		Date date = new Date();
		synchronized (FORMATE_DATE_MS) {
			return FORMATE_DATE_MS.format(date);
		}
	}

	public static int getyyyyMMddTimeForNow() {
		Calendar c = Calendar.getInstance();

		int result = 0;
		result += c.get(Calendar.YEAR) * 10000;
		result += (c.get(Calendar.MONTH) + 1) * 100;
		result += c.get(Calendar.DAY_OF_MONTH);

		return result;
	}

	/*
	 * @brief FRS页时间展现策略
	 * 
	 * @param tObj 主题的最后回复时间
	 * 
	 * 策略如下： 1小时内，XX分钟前 当日内，15:30 1年内，3月2日 超过一年，2006年
	 */
	public static String GetTimeString(Date tObj) {
		if (tObj == null) {
			return "";
		}

		Date tClient = new Date();

		// 再往后的，tClient > tObj
		// 当天的显示 15:30
		if (tClient.getYear() == tObj.getYear()) { // 当年
			if (tClient.getMonth() == tObj.getMonth()
					&& tClient.getDate() == tObj.getDate()) { // 当日内
				return getDateStringHm(tObj);
			} else {
				return getDateStringMouth(tObj);
			}

		}
		return getDateStringYear(tObj);
	}

	/*
	 * @brief FRS页时间展现策略
	 * 
	 * @param tObj 主题的最后回复时间
	 * 
	 * 当日内，15:30 超过一日，YYYY-MM-DD
	 */
	public static String GetTimeString2(Date tObj) {
		if (tObj == null) {
			return "";
		}
		Date tClient = new Date();
		if (tClient.getMonth() == tObj.getMonth()
				&& tClient.getDate() == tObj.getDate()) { // 当日内
			return getDateStringHm(tObj);
		} else {
			return getDateStringDay(tObj);
		}

	}

	/*
	 * @brief FRS、PB页 4.0帖子 时间展现策略
	 * 
	 * @param tObj 主题的最后回复时间
	 * 
	 * 如果发帖时间在未来： 120s之内 返回刚刚 120s以上 返回未来时间yyyy-mm-dd 30s内： 刚刚 30s-1min： 半分钟前
	 * 1min-60min n分钟前 1h-24h而且是当天 hh：mm 1d-31day n天前 31day-32day 1个月前 32day--
	 * yyyy-mm-dd
	 */
	public static String GetPostTimeString(Date tObj) {
		if (tObj == null) {
			return "";
		}

		return getPostTimeString(new Date(), tObj);
	}

	/*
	 * @brief FRS、PB页 4.0帖子 时间展现策略
	 * 
	 * @param tObj 主题的最后回复时间
	 * 
	 * 如果发帖时间在未来： 120s之内 返回刚刚 120s以上 返回未来时间yyyy-mm-dd 30s内： 刚刚 30s-1min： 半分钟前
	 * 1min-60min n分钟前 1h-24h而且是当天 hh：mm 1d-31day n天前 31day-32day 1个月前 32day--
	 * yyyy-mm-dd
	 */
	public static String getPostTimeString(Date timeNow, Date tObj) {
		if (tObj == null) {
			return "";
		}
		Date tClient = timeNow;
		int day = tClient.getDay() - tObj.getDay();
		long ts = tClient.getTime() - tObj.getTime();
		long base = 30 * 1000; // 半分钟，30s

		if (ts < 0) {
			if (ts > -120 * 1000) {
				return "刚刚";
			} else {
				return getDateStringDay(tObj);
			}
		}

		if (ts < base) {
			return "刚刚";
		}

		base *= 2;// 1分钟，60s

		if (ts < base) {
			return "半分钟前";
		}

		base *= 60;// 1小时

		if (ts < base) {
			return String.valueOf(ts * 60 / base) + "分钟前";
		}

		base *= 24; // 24小时

		if (ts < base) {
			if (day == 0) {
				return getDateStringHm(tObj);
			} else {
				return "1天前";
			}
		}

		base *= 31; // 31天

		if (ts < base) {
			return String.valueOf(ts * 31 / base) + "天前";
		}

		base += 24 * 60 * 60 * 1000; // 32天

		if (ts < base) {
			return "1个月前";
		}

		return getDateStringDay(tObj);
	}

	/**
	 * @see #getChatTimeString(java.util.Date, java.util.Date)
	 * @param date
	 *            目标时间
	 * @return 时间字符串
	 */
	public static String getChatTimeString(Date date) {
		final Date now = new Date();
		return getChatTimeString(now, date);
	}

	/**
	 * 聊天消息时间显示策略 一天以内显示 HH:MM 一天以前显示 “昨天” 两天以前显示 ”前天“ 三天以前显示 YY:MM:DD
	 * 
	 * @since 4.4
	 * @param now
	 *            当前时间
	 * @param date
	 *            目标时间
	 * @return 时间字符串
	 */
	public static String getChatTimeString(Date timeNow, Date tObj) {
		// if (date == null) {
		// return "";
		// }
		// long miliSecondDelta = now.getTime() - date.getTime();
		//
		// final int oneDay = 3600 * 24 * 1000;
		// if (miliSecondDelta <= oneDay) {
		// return getDateStringHm(date);
		// } else if (miliSecondDelta <= 2 * oneDay) {
		// return "昨天";
		// } else if (miliSecondDelta <= 3 * oneDay) {
		// return "前天";
		// } else if (miliSecondDelta > 3 * oneDay) {
		//
		// if (now.getYear() == date.getYear()) {
		// synchronized (FORMATE_DATE_DAY_NO_YEAR) {
		// return FORMATE_DATE_DAY_NO_YEAR.format(date);
		// }
		// } else {
		// synchronized (FORMATE_DATE_DAY_1) {
		// return FORMATE_DATE_DAY_1.format(date);
		// }
		// }
		// }
		// return "";
		if (tObj == null) {
			return "";
		}
		Date tClient = timeNow;
		int day = tClient.getDay() - tObj.getDay();
		long ts = tClient.getTime() - tObj.getTime();
		long base = 30 * 1000; // 半分钟，30s

		if (ts < 0) {
			if (ts > -120 * 1000) {
				return "刚刚";
			} else {
				return getDateStringDay(tObj);
			}
		}

		if (ts < base) {
			return "刚刚";
		}

		base *= 2;// 1分钟，60s

		if (ts < base) {
			return "半分钟前";
		}

		base *= 60;// 1小时

		if (ts < base) {
			return String.valueOf(ts * 60 / base) + "分钟前";
		}

		base *= 24; // 24小时

		if (ts < base) {
			if (day == 0) {
				return getDateStringHm(tObj);
			} else {
				return "1天前";
			}
		}

		base *= 31; // 31天

		if (ts < base) {
			return String.valueOf(ts * 31 / base) + "天前";
		}

		base += 24 * 60 * 60 * 1000; // 32天

		if (ts < base) {
			return "1个月前";
		}

		if (tClient.getYear() == tObj.getYear()) {
			synchronized (FORMATE_DATE_DAY_NO_YEAR) {
				return FORMATE_DATE_DAY_NO_YEAR.format(tObj);
			}
		} else {
			synchronized (FORMATE_DATE_DAY) {
				return FORMATE_DATE_DAY.format(tObj);
			}
		}
	}

	// public static String GetTimeString(long date, long base_date){
	// SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm");
	// Date tmp_date = new Date();
	// String ret = null;
	//
	// long ts = date - base_date;
	// if(ts<0){
	// return time.format(tmp_date);
	// }
	// TimeZone zone=TimeZone.getDefault();
	// long offset=zone.getRawOffset();
	// long nowday = (date + offset)/(24*3600*1000);
	// long tday = (base_date + offset)/(24*3600*1000);
	//
	// if(ts < (1000 * 60 * 60)){
	// if(ts<1000*60) //1分钟内
	// ret = String.valueOf(ts/1000) + "秒前";
	// else
	// ret = String.valueOf(ts/1000/60)+"分钟前";
	// }else if(tday==nowday){
	// SimpleDateFormat time2=new SimpleDateFormat("HH:mm");
	// ret = time2.format(date);
	// ret = "今天 "+ret;
	// }else if(tday==nowday-1){
	// SimpleDateFormat time3=new SimpleDateFormat("HH:mm");
	// ret = time3.format(date);
	// ret = "昨天 "+ret;
	// }else{
	// ret = time.format(tmp_date);
	// }
	// return ret;
	// }

	private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String toHexString(byte[] b) {
		if (b == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
			sb.append(HEX_DIGITS[b[i] & 0x0f]);
		}
		return sb.toString();
	}

	public static String ToMd5(InputStream in) {
		String ret = null;
		if (in == null) {
			return null;
		}
		try {
			byte[] buffer = new byte[1024];
			int numRead = 0;
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			while ((numRead = in.read(buffer)) > 0) {
				md5.update(buffer, 0, numRead);
			}
			ret = toHexString(md5.digest());
		} catch (Exception ex) {
			MeetLog.i("StringHelper", "ToMd5", ex.toString());
		} finally {
			CloseUtil.close(in);
		}
		return ret;
	}

	/**
	 * MD5加密字符串
	 * 
	 * @param str
	 * @return
	 */
	public static String ToMd5(String str) {
		String ret = null;
		try {
			byte[] bMsg = str.getBytes("UTF-8");
			InputStream in = new ByteArrayInputStream(bMsg);
			ret = ToMd5(in);
		} catch (Exception e) {
		}
		return ret;
	}

	/**
	 * 字符串中是否包含汉字
	 * 
	 * @param str
	 *            字符串
	 * @return
	 */
	public static boolean ContentChinese(String str) {
		boolean ret = false;
		if (str == null || str.length() < 1) {
			return ret;
		}
		for (int i = 0; i < str.length(); i++) {
			if (isChinese(str.charAt(i))) {
				return true;
			}
		}
		return ret;
	}

	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	/**
	 * @brief 判断是有效的帐号名
	 */
	public static boolean isAccount(String account) {
		// 先判断只能是：汉字、字母、数字、下划线
		String all = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w]+$";
		Pattern pattern = Pattern.compile(all);
		boolean tf = pattern.matcher(account).matches();
		if (tf == false) {
			return false;
		}

		String tempStr = null;
		int totalLen = 0;
		for (int i = 0; i < account.length(); i++) {
			tempStr = String.valueOf(account.charAt(i));
			if (tempStr.getBytes().length == 1) {
				// 字母、数字、下划线
				totalLen += 1;
			} else {
				// 中文
				// 注意，在java中中文用的utf-8，是3字节；后端的限制是总长度14且中文用的gbk，是2字节；
				totalLen += 2;
			}
		}
		if (totalLen <= 0 || totalLen > 14) {
			return false;
		}
		return true;
	}

	/**
	 * @brief 判断是有效的密码
	 */
	public static boolean isPassword(String password) {
		// 先判断长度
		int len = password.length();
		if (len < 6 || len > 14) {
			return false;
		}
		int byteLen = password.getBytes().length;
		if (byteLen > len) {
			return false;
		}
		return true;
	}

	/**
	 * @brief 判断是否是有效的吧名
	 */
	public static boolean isForumName(String name) {
		if (name != null && name.length() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是电话号码
	 * 
	 * @param phone
	 * @return
	 */
	public static boolean isMobileNo(String phone) {
		Pattern p = Pattern.compile("1\\d{10}");
		Matcher m = p.matcher(phone);
		return m.matches();
	}

	/**
	 * 字符是否为空
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isEmpty(String s) {
		if ((s == null) || (s.length() == 0) || s.equals("null")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取URLencode编码
	 * 
	 * @param s
	 * @return
	 */
	public static String getUrlEncode(String s) {
		if (s == null) {
			return null;
		}
		String result = "";
		try {
			result = URLEncoder.encode(s, "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 解码URLencode
	 * 
	 * @param s
	 * @return 解码后的字符串
	 */
	public static String getUrlDecode(String s) {
		String result = null;
		try {
			result = URLDecoder.decode(s, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * 计算字符串长度
	 */
	public static int byteLength(String string) {
		int count = 0;
		for (int i = 0; i < string.length(); i++) {
			if (Integer.toHexString(string.charAt(i)).length() == 4) {
				count += 2;
			} else {
				count++;
			}
		}
		return count;
	}

	public static String cutString(String string, int length) {
		if (string == null || length <= 0) {
			return String.valueOf("");
		}
		int len = string.length();
		int count = 0;
		int i = 0;
		for (i = 0; i < len; i++) {
			char c = string.charAt(i);
			if (isChinese(c)) {
				count += 2;
			} else {
				count++;
			}
			if (count >= length) {
				break;
			}
		}
		if (i < len) {
			return string.substring(0, i + 1) + "...";
		} else {
			return string;
		}
	}

	private static final char[] base64EncodeChars = new char[] { 'A', 'B', 'C',
			'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
			'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c',
			'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
			'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2',
			'3', '4', '5', '6', '7', '8', '9', '+', '/' };

	private static byte[] base64DecodeChars = new byte[] { -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59,
			60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
			10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1,
			-1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37,
			38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1,
			-1, -1 };

	public static String base64Encode(byte[] data) {
		int len = data.length;
		int i = 0;
		int b1, b2, b3;

		StringBuilder sb = new StringBuilder(len / 2);

		while (i < len) {
			b1 = data[i++] & 0xff;
			if (i == len) {
				sb.append(base64EncodeChars[b1 >>> 2]);
				sb.append(base64EncodeChars[(b1 & 0x3) << 4]);
				sb.append("==");
				break;
			}
			b2 = data[i++] & 0xff;
			if (i == len) {
				sb.append(base64EncodeChars[b1 >>> 2]);
				sb.append(base64EncodeChars[((b1 & 0x03) << 4)
						| ((b2 & 0xf0) >>> 4)]);
				sb.append(base64EncodeChars[(b2 & 0x0f) << 2]);
				sb.append("=");
				break;
			}
			b3 = data[i++] & 0xff;
			sb.append(base64EncodeChars[b1 >>> 2]);
			sb.append(base64EncodeChars[((b1 & 0x03) << 4)
					| ((b2 & 0xf0) >>> 4)]);
			sb.append(base64EncodeChars[((b2 & 0x0f) << 2)
					| ((b3 & 0xc0) >>> 6)]);
			sb.append(base64EncodeChars[b3 & 0x3f]);
		}
		return sb.toString();
	}

	public static byte[] base64Decode(String str) {
		byte[] data = str.getBytes();
		int len = data.length;
		ByteArrayOutputStream buf = new ByteArrayOutputStream(len);
		int i = 0;
		int b1, b2, b3, b4;

		while (i < len) {

			/* b1 */
			do {
				b1 = base64DecodeChars[data[i++]];
			} while (i < len && b1 == -1);
			if (b1 == -1) {
				break;
			}

			/* b2 */
			do {
				b2 = base64DecodeChars[data[i++]];
			} while (i < len && b2 == -1);
			if (b2 == -1) {
				break;
			}
			buf.write((int) ((b1 << 2) | ((b2 & 0x30) >>> 4)));

			/* b3 */
			do {
				b3 = data[i++];
				if (b3 == 61) {
					return buf.toByteArray();
				}
				b3 = base64DecodeChars[b3];
			} while (i < len && b3 == -1);
			if (b3 == -1) {
				break;
			}
			buf.write((int) (((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2)));

			/* b4 */
			do {
				b4 = data[i++];
				if (b4 == 61) {
					return buf.toByteArray();
				}
				b4 = base64DecodeChars[b4];
			} while (i < len && b4 == -1);
			if (b4 == -1) {
				break;
			}
			buf.write((int) (((b3 & 0x03) << 6) | b4));
		}
		return buf.toByteArray();
	}

	static public String getNameFromUrl(String url) {
		String name = null;
		try {
			int start = url.lastIndexOf("/");
			int end = url.lastIndexOf(".");
			if (start == -1) {
				name = url;
			} else if (start < end) {
				name = url.substring(start, end);
			} else {
				name = url.substring(start);
			}
		} catch (Exception ex) {
			MeetLog.e("StringHelper", "getNameFromUrl", ex.getMessage());
		}
		return name;
	}

	static public String getNameMd5FromUrl(String url) {
		return ToMd5(url);
	}

	static public String getHighLightString(String src, Color color) {
		if (src == null) {
			return "";
		}
		String highLightString = null;
		try {
			highLightString = src
					.replaceAll("<em>", "<font color=\'#007bd1\'>");
			highLightString = highLightString.replaceAll("</em>", "</font>");
		} catch (Exception e) {
			MeetLog.i("StringHelper", "getHighLightString", e.toString());
		}
		return highLightString;
	}

	private static long[] parseVersion(String ver) {
		long result[] = new long[3];
		if (ver != null) {
			ver = ver.replace(".", "#");
			String[] strarr = ver.split("#");
			result[0] = Long.parseLong(strarr[0]);
			result[1] = Long.parseLong(strarr[1]);
			result[2] = Long.parseLong(strarr[2]);
		}
		return result;
	}

    
	public static String getFormatTime(long time) {
		synchronized (date) {
			date.setTime(time);
			return getFormatTime(date);
		}
	}
    
    /**
     * 获得格式化的时间字符串（5.1版本加入）
     * 应用：frs、pb、首页、消息、我的贴子/ta的贴子、搜贴
     * 格式：一天内的显示为x秒前、x分钟前、x小时前，一天以后的显示为具体的日期。
     * @return
     */
	private static String getFormatTime(Date date) {
		if(date == null)
			return "";
		Date now = new Date();
		long between = now.getTime() - date.getTime();
		if (between < MS_TO_DAY && between > 0) // 同一天
		{
			if (between < MS_TO_HOUR)// 同一小时：
			{
				if (between < MS_TO_MIN)// 同一分钟
				{
					return String.valueOf(between / MS_TO_SEC)
							+ SEC_BEFORE;
				} else {
					return String.valueOf(between / MS_TO_MIN)
							+ MIN_BEFORE;
				}
			} else {
				return String.valueOf(between / MS_TO_HOUR)
						+ HOUR_BEFORE;
			}
		} else {
			return getDateStringDay(date);
		}
	}

	public static int compareVersion(String ver1, String ver2) {
		if (ver1 == null) {
			return -1;
		}
		if (ver2 == null) {
			return 1;
		}

		long[] ver1arr = parseVersion(ver1);
		long[] ver2arr = parseVersion(ver2);
		long ver1Result = 0, ver2Result = 0;
		for (int i = 0; i < 3; i++) {
			ver1Result += (ver1arr[i] << (24 - i * 8));
		}
		for (int i = 0; i < 3; i++) {
			ver2Result += (ver2arr[i] << (24 - i * 8));
		}
		if (ver1Result > ver2Result) {
			return 1;
		} else if (ver1Result == ver2Result) {
			return 0;
		} else {
			return -1;
		}
	}

}
