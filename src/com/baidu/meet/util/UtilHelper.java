package com.baidu.meet.util;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.baidu.meet.MeetApplication;
import com.baidu.meet.R;
import com.baidu.meet.log.MeetLog;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class UtilHelper {
	
	static public enum NetworkStateInfo {
		UNAVAIL, WIFI, TwoG, ThreeG
	}

	private static boolean deviceDataInited = false;

	private static float displayMetricsDensity;
	private static int displayMetricsWidthPixels;
	private static int displayMetricsHeightPixels;

	private static void initDeviceData(Context context) {
		displayMetricsDensity = context.getResources().getDisplayMetrics().density;
		displayMetricsWidthPixels = context.getResources().getDisplayMetrics().widthPixels;
		displayMetricsHeightPixels = context.getResources().getDisplayMetrics().heightPixels;

		deviceDataInited = true;
	}

	public static int dip2px(Context context, float dipValue) {
		if (!deviceDataInited) {
			initDeviceData(context);
		}

		return (int) (dipValue * displayMetricsDensity + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		if (!deviceDataInited) {
			initDeviceData(context);
		}

		return (int) (pxValue / displayMetricsDensity + 0.5f);
	}

	public static int getEquipmentWidth(Context context) {
		if (!deviceDataInited) {
			initDeviceData(context);
		}

		return displayMetricsWidthPixels;
	}

	public static int getEquipmentHeight(Context context) {
		if (!deviceDataInited) {
			initDeviceData(context);
		}

		return displayMetricsHeightPixels;
	}

	public static void showToast(Context context, String str) {
		if (str != null && str.length() > 0) {
			Toast toast = Toast.makeText(context, str,
					Toast.LENGTH_SHORT);
			int y_offset = dip2px(context, 100);
			toast.setGravity(Gravity.CENTER, 0, y_offset);
			toast.show();
		}
	}

	public static void showToast(Context context, int stringId) {
		String str = context.getResources().getString(stringId);
		showToast(context, str);
	}

	public static void hideSoftKeyPad(Context context, View view) {
		try {
			if (view == null) {
				return;
			} else if (view.getWindowToken() == null) {
				return;
			}

			InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		} catch (Throwable ex) {
			MeetLog.e("UtilHelper", "hideSoftKeyPad", "error = " + ex.getMessage());
		}
	}

	public static void showSoftKeyPad(Context context, View view) {
		try {
			InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.showSoftInput(view, InputMethodManager.RESULT_UNCHANGED_SHOWN);
		} catch (Throwable ex) {
			MeetLog.e("UtilHelper", "showSoftKeyPad", "error = " + ex.getMessage());
		}
	}

	public static int getStatusBarHeight(Activity activity) {
		Rect rect = new Rect();
		Window window = activity.getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rect);
		return rect.top;
	}

	public static int[] getScreenDimensions(Context context) {
		int[] dimensions = new int[2];
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		dimensions[0] = display.getWidth();
		dimensions[1] = display.getHeight();
		return dimensions;
	}

	public static void install_apk(Context context, String file_name) {
		if (file_name == null || file_name.length() <= 0) {
			return;
		}
		File file = FileHelper.GetFile(file_name);
		if (file != null) {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);
			String type = "application/vnd.android.package-archive";
			intent.setDataAndType(Uri.fromFile(file), type);
			context.startActivity(intent);
		}
	}

	/**
	 * 获取一个对象隐藏的属性，并设置属性为public属性允许直接访问�?
	 * 
	 * @return {@link Field} 如果无法读取，返回null；返回的Field需要使用者自己缓存，本方法不做缓存�?
	 */
	public static Field getDeclaredField(Object object, String field_name) {
		Class<?> cla = object.getClass();
		Field field = null;
		for (; cla != Object.class; cla = cla.getSuperclass()) {
			try {
				field = cla.getDeclaredField(field_name);
				field.setAccessible(true);
				return field;
			} catch (Exception e) {

			}
		}
		return null;
	}


	public static boolean isGif(byte[] data) {
		boolean isGif = false;
		try {
			if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F') {
				isGif = true;
			}
		} catch (Exception ex) {
			isGif = false;
		}
		return isGif;
	}

	public static void startWebActivity(Context context, String url) {
		try {
			startExternWebActivity(context, url);
		} catch (Exception e) {
			// TODO: handle exception
			MeetLog.e("UtilHelper", "startWebActivity", e.getMessage());
		}
	}

	public static void startExternWebActivity(Context context, String url) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(url));
			if ((context instanceof Activity) == false) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			context.startActivity(intent);
		} catch (Exception e) {
			// TODO: handle exception
			MeetLog.e("UtilHelper", "startExternWebActivity", e.getMessage());
		}
	}

	public static DisplayMetrics getScreenSize(Activity activity) {
		DisplayMetrics result = null;
		try {
			result = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(result);
		} catch (Exception e) {
			MeetLog.i("UtilHelper", "getScreenSize", e.toString());
		}
		return result;
	}

	// 1. 粗略计算文字宽度
	public static float measureTextWidth(Paint paint, String str) {
		if (paint == null || str == null) {
			return 0;
		}
		return paint.measureText(str);
	}

	// 2. 计算文字所在矩形，可以得到宽高
	public static Rect measureText(Paint paint, String str) {
		Rect rect = new Rect();
		paint.getTextBounds(str, 0, str.length(), rect);
		return rect;
	}

	// 3. 精确计算文字宽度
	public static int getTextWidth(Paint paint, String str) {
		int iRet = 0;
		if (str != null && str.length() > 0) {
			int len = str.length();
			float[] widths = new float[len];
			paint.getTextWidths(str, widths);
			for (int j = 0; j < len; j++) {
				iRet += (int) Math.ceil(widths[j]);
			}
		}
		return iRet;
	}

	public static String getTextOmit(TextPaint paint, String str, int width) {
		String des = null;
		CharSequence sequence = TextUtils.ellipsize(str, paint, width, TruncateAt.END);
		if (sequence != null) {
			des = sequence.toString();
		}
		return des;
	}

	public static TextPaint setTextSize(Context c, TextPaint paint, float size) {
		Resources r;
		if (c == null) {
			r = Resources.getSystem();
		} else {
			r = c.getResources();
		}
		if (r != null) {
			paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, r.getDisplayMetrics()));
		}
		return paint;
	}

	public static int getFontHeight(Context context, float fontSize) {
		TextPaint paint = new TextPaint();
		setTextSize(context, paint, fontSize);
		FontMetrics fm = paint.getFontMetrics();
		return (int) Math.ceil(fm.descent - fm.ascent);
	}

	public static void setWindowAlpha(Activity activity, float alpha) {
		LayoutParams params = activity.getWindow().getAttributes();
		// params.alpha = alpha;
		params.screenBrightness = alpha;
		activity.getWindow().setAttributes(params);
	}

	// public static void setEyeShieldMode(Activity activity) {
	// Activity temp = null;
	//
	// if (activity == null) {
	// return;
	// }
	//
	// temp = activity;
	// if (temp.getParent() != null) {
	// temp = temp.getParent();
	// }
	// if (MeetApplication.getApp().getEyeShieldMode() ==
	// Config.EYESHIELD_NORMAL) {
	// setWindowAlpha(temp, getScreenBrightness(activity));
	// } else {
	// setWindowAlpha(temp, MeetApplication.getApp().getEyeShieldMode());
	// }
	// }

	public static int getScreenBrightness(Activity activity) {
		int screenBrightness = 0;
		try {
			screenBrightness = android.provider.Settings.System.getInt(activity.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
		} catch (Exception ex) {
			MeetLog.e("UtilHelper", "getScreenBrightness", "error = " + ex.getMessage());
		}
		return screenBrightness;
	}

	public static String urlAddParam(String url, String param) {
		if (url == null || param == null) {
			return url;
		}
		if (url.indexOf("?") < 0) {
			url += "?";
		} else if (!url.endsWith("?") && !url.endsWith("&")) {
			url += "&";
		}
		url += param;
		return url;
	}

	public static void share(Context context, String st_type, String content, String image) {
		try {
			Intent intent = new Intent(Intent.ACTION_SEND, null);
			intent.addCategory(Intent.CATEGORY_DEFAULT);

			if (content.length() > 140) {
				content = content.substring(0, 140);
			}

			intent.putExtra(Intent.EXTRA_TEXT, content);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setType("text/plain");

			// 添加图片
			// if(image != null){
			// String name = StringHelper.getNameMd5FromUrl(image);
			// File pic = FileHelper.GetFile(Config.TMP_PIC_DIR_NAME + "/" +
			// name);
			// if(pic != null){
			// Uri u = Uri.fromFile(pic);
			// intent.putExtra(Intent.EXTRA_STREAM, u);
			// intent.setType("image/*");
			// }
			// }

			context.startActivity(Intent.createChooser(intent, MeetApplication.getApp().getResources().getString(R.string.share_to)));
		} catch (Exception e) {
			MeetLog.e("UtilHelper", "share", e.toString());
		}
	}


	/**
	 * 判断是否已经安装了此�?
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean isInstalledPackage(Context context, String packageName) {
		final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
		List<String> pName = new ArrayList<String>();// 用于存储所有已安装程序的包�?
		// 从pinfo中将包名字逐一取出，压入pName list�?
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				pName.add(pn);
			}
		}
		return pName.contains(packageName);// 判断pName中是否有目标程序的包名，有TRUE，没有FALSE
	}


	public static void callPhone(Context context, String mPhoneNumber) {
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mPhoneNumber));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			MeetLog.e(e.getMessage());
		} catch (SecurityException e) {
			MeetLog.e(e.getMessage());
		}
	}

	public static void smsPhone(Context context, String mPhoneNumber) {
		Uri smsToUri = Uri.parse("smsto:" + mPhoneNumber);
		Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
		intent.putExtra("sms_body", "");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			MeetLog.e(e.getMessage());
		} catch (SecurityException e) {
			MeetLog.e(e.getMessage());
		}
	}


	public static String getMetaValue(Context context, String metaKey) {
		Bundle metaData = null;
		String apiKey = null;
		if (context == null || metaKey == null) {
			return null;
		}
		try {
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			if (null != ai) {
				metaData = ai.metaData;
			}
			if (null != metaData) {
				apiKey = metaData.getString(metaKey);
			}
		} catch (NameNotFoundException e) {

		}
		return apiKey;
	}

	/**
	 * 判断当前网络是否可用
	 * 
	 * @return
	 */
	public static boolean isNetOk() {
		return getNetStatusInfo(MeetApplication.getApp().getApplicationContext()) != UtilHelper.NetworkStateInfo.UNAVAIL;
	}

	/**
	 * 获取网络状�?
	 * 
	 * @param context
	 * @return
	 */
	public static UtilHelper.NetworkStateInfo getNetStatusInfo(Context context) {
		boolean netSataus = false;
		NetworkInfo networkinfo = null;
		UtilHelper.NetworkStateInfo ret = UtilHelper.NetworkStateInfo.UNAVAIL;
		try {
			ConnectivityManager cwjManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			networkinfo = cwjManager.getActiveNetworkInfo();
			netSataus = networkinfo.isAvailable();

			if (!netSataus) {
				ret = UtilHelper.NetworkStateInfo.UNAVAIL;
				MeetLog.i("NetWorkCore", "NetworkStateInfo", "UNAVAIL");
			} else {
				if (networkinfo.getType() == ConnectivityManager.TYPE_WIFI) {
					MeetLog.i("NetWorkCore", "NetworkStateInfo", "WIFI");
					ret = UtilHelper.NetworkStateInfo.WIFI;
				} else {
					TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
					int subType = tm.getNetworkType();
					switch (subType) {
					case TelephonyManager.NETWORK_TYPE_1xRTT: // ~ 50-100 kbps
					case TelephonyManager.NETWORK_TYPE_CDMA: // ~ 14-64 kbps
					case TelephonyManager.NETWORK_TYPE_EDGE: // ~ 50-100 kbps
					case TelephonyManager.NETWORK_TYPE_GPRS: // ~ 100 kbps
					case /* Connectivity.NETWORK_TYPE_IDEN */11: // ~25 kbps
					case TelephonyManager.NETWORK_TYPE_UNKNOWN:
						MeetLog.i("NetWorkCore", "NetworkStateInfo", "TwoG");
						return UtilHelper.NetworkStateInfo.TwoG;
					case TelephonyManager.NETWORK_TYPE_EVDO_0: // ~ 400-1000
																// kbps
					case TelephonyManager.NETWORK_TYPE_EVDO_A: // ~ 600-1400
																// kbps
					case TelephonyManager.NETWORK_TYPE_UMTS: // ~ 400-7000 kbps
					case /* Connectivity.NETWORK_TYPE_EHRPD */14: // ~ 1-2 Mbps
					case /* Connectivity.NETWORK_TYPE_EVDO_B */12: // ~ 5 Mbps
					case /* Connectivity.NETWORK_TYPE_HSPAP */15: // ~ 10-20 Mbps
					case /* Connectivity.NETWORK_TYPE_LTE */13: // ~ 10+ Mbps
					case /* TelephonyManager.NETWORK_TYPE_HSDPA */8: // ~ 4-8
																	// Mbps
					case /* TelephonyManager.NETWORK_TYPE_HSUPA */9: // ~
																	// 1.4-5.8Mbps
					case /* TelephonyManager.NETWORK_TYPE_HSPA */10: // ~20Mbps
						MeetLog.i("NetWorkCore", "NetworkStateInfo", "ThreeG");
						return UtilHelper.NetworkStateInfo.ThreeG;
					default:
						MeetLog.i("NetWorkCore", "NetworkStateInfo-default", "TwoG");
						return UtilHelper.NetworkStateInfo.TwoG;
					}
				}
			}
		} catch (Exception ex) {
		}
		return ret;
	}

	/**
	 * 根据长度截断字符串，并在文字后补上�?..�?
	 * 
	 * @param str 文字
	 * @param len 要获得的文字长度
	 * @return
	 */
	public static String getFixedText(String str, int len) {
		String textFixed = "";
		double sizeCount = 0;
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
					|| (ch >= '0' && ch <= '9')) {
				sizeCount += 0.5;
			} else {
				sizeCount += 1;
			}
			if (sizeCount <= len) {
				textFixed += ch;
			} else {
				textFixed += "...";
				break;
			}
		}
		return textFixed;
	}
}
