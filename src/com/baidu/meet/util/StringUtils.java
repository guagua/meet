package com.baidu.meet.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {
	private static final String TIMEFORMAT = "yyyy-MM-dd HH:mm";
	private static final String[] TM = new String[48];
	public static final String lineSeparator = System
			.getProperty("line.separator");

	public static String SimpleDateFormat(Date date) {
		if (date == null)
			return null;
		SimpleDateFormat todayDateFormatter = new SimpleDateFormat(TIMEFORMAT);
		return todayDateFormatter.format(date);
	}

	public static String SimpleDateFormat(Date date, String dateformat) {
		if (date == null)
			return null;
		if (dateformat == null) {
			dateformat = TIMEFORMAT;
		}
		SimpleDateFormat todayDateFormatter = new SimpleDateFormat(dateformat);
		return todayDateFormatter.format(date);
	}

	public static Date handleDate(String data) {
		return handleDate(data, TIMEFORMAT);

	}

	public static Date handleDate(String data, String dateformat) {
		if (dateformat == null) {
			dateformat = TIMEFORMAT;
		}
		if (data == null || data.length() == 0)
			return null;
		try {
			DateFormat df = new SimpleDateFormat(dateformat);
			Date d_date = df.parse(data);
			return d_date;
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean isNull(String str) {
		boolean b = false;
		if (str == null || str.trim().length() == 0)
			b = true;

		return b;
	}

	public static boolean isNULL(String str) {
		boolean b = false;
		if (str == null)
			b = true;

		return b;
	}

	public static boolean isNull(String str, boolean bValidNullString) {
		boolean b = false;
		if (str == null || str.trim().length() == 0)
			b = true;
		if (!b && bValidNullString) {
			if (str != null && str.equalsIgnoreCase("null"))
				b = true;
		}
		return b;
	}
}
