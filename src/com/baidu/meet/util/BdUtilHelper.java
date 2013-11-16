package com.baidu.meet.util;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import com.baidu.meet.BaseApplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Looper;
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

public class BdUtilHelper {
	
	private static boolean deviceDataInited = false;

	private static float displayMetricsDensity;
	private static int displayMetricsWidthPixels;
	private static int displayMetricsHeightPixels;

	public static void initDeviceData(Context context) {
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
			Toast toast = Toast.makeText(BaseApplication.getApplication(), str,
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

	public static void showLongToast(Context context, String str) {
		if (str != null && str.length() > 0) {
			Toast toast = Toast.makeText(BaseApplication.getApplication(), str,
					Toast.LENGTH_LONG);
			int y_offset = dip2px(context, 100);
			toast.setGravity(Gravity.CENTER, 0, y_offset);
			toast.show();
		}
	}

	public static void showLongToast(Context context, int stringId) {
		String str = context.getResources().getString(stringId);
		showLongToast(context, str);
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
		}
	}

	public static void showSoftKeyPad(Context context, View view) {
		try {
			InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.showSoftInput(view, InputMethodManager.RESULT_UNCHANGED_SHOWN);
		} catch (Throwable ex) {
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

	public static DisplayMetrics getScreenSize(Activity activity) {
		DisplayMetrics result = null;
		try {
			result = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(result);
		} catch (Exception e) {
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

	public static int getScreenBrightness(Activity activity) {
		int screenBrightness = 0;
		try {
			screenBrightness = android.provider.Settings.System.getInt(activity.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
		} catch (Exception ex) {
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

	public static void share(Context context, String st_type, String content, File image) {
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
			if (image != null && image.exists()) {
				Uri u = Uri.fromFile(image);
				intent.putExtra(Intent.EXTRA_STREAM, u);
				intent.setType("image/*");
			}
			context.startActivity(Intent.createChooser(intent, "分享到"));
		} catch (Exception e) {
		}
	}

	public static int[] getImageResize(int width, int height, int maxWidth, int maxHeight) {
		if (width <= 0 || height <= 0 || maxWidth <= 0 || maxHeight <= 0) {
			return null;
		}
		int[] size = new int[2];

		if (height > maxHeight) {
			width = width * maxHeight / height;
			height = maxHeight;
		}
		if (width > maxWidth) {
			height = height * maxWidth / width;
			width = maxWidth;
		}
		size[0] = width;
		size[1] = height;
		return size;
	}

	/* begin 获取两个经纬度间的距离，单位公里 */
	private final static double EARTH_RADIUS = 6378.137;// 地球半径

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	// 获取两个经纬度间的距离，单位公里
	public static double GetDistance(double lat1, double lng1, double lat2, double lng2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);

		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}

	/* end 获取两个经纬度间的距离，单位公里 */

	// 检测客户端是否安装某个app
	public static boolean hasInstallApp(Context context, String packageName) {
		if (packageName == null || packageName.length() == 0) {
			return false;
		}

		final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
		// 从pinfo中将包名字逐一取出，压入pName list中
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				if (pn.equals(packageName)) {
					return true;
				}
			}
		}
		return false;
	}

	// 检查是否有快捷方式
	public static boolean checkShortCut(Context context, String appName) {
		boolean hasShortCut = false;
		try {
			ContentResolver cr = context.getContentResolver();
			final String AUTHORITY1 = "com.android.launcher.settings";
			final String AUTHORITY2 = "com.android.launcher2.settings";
			String contentUri = "";
			if (android.os.Build.VERSION.SDK_INT < 8) {
				contentUri = "content://" + AUTHORITY1 + "/favorites?notify=true";
			} else {
				contentUri = "content://" + AUTHORITY2 + "/favorites?notify=true";
			}
			final Uri CONTENT_URI = Uri.parse(contentUri);
			Cursor c = cr.query(CONTENT_URI, new String[] { "title", "iconResource" }, "title=?", new String[] { appName }, null);
			if (c != null && c.getCount() > 0) {
				hasShortCut = true;
			}
		} catch (Exception e) {
		}
		return hasShortCut;
	}

	public static void addShortcut(Context context, String appName, String packageName, String className, int iconResId) {
		Intent target = new Intent();
		target.addCategory(Intent.CATEGORY_LAUNCHER);
		target.setAction(Intent.ACTION_MAIN);
		ComponentName comp = new ComponentName(packageName, className);
		target.setComponent(comp);

		Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		// 快捷方式的名称
		shortcut.putExtra("duplicate", false);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, target);

		// 快捷方式的图标
		ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(context, iconResId);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
		context.sendBroadcast(shortcut);
	}

	/**
	 * 检查是否为主线程
	 * 
	 * @throws Throwable
	 */
	public static void checkMainThread() {
		if (BaseApplication.getApplication().isDebugMode()) {
			boolean error = false;
			if (Looper.myLooper() == null
					|| Looper.getMainLooper() != Looper.myLooper()) {
				error = true;
			}

			if (error == true) {
				StringBuilder buidler = new StringBuilder(100);
				StackTraceElement[] elements = Thread.currentThread().getStackTrace();
				for (int i = 1; i < elements.length; i++) {
					buidler.append(elements[i].getClassName());
					buidler.append(".");
					buidler.append(elements[i].getMethodName());
					buidler.append("<-");
				}
				throw new Error("can not be call not thread! trace = " + buidler.toString());
			}
		}
	}
	
}
