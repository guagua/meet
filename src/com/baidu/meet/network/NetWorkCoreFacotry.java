package com.baidu.meet.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.baidu.meet.MeetApplication;
import com.baidu.meet.log.MeetLog;
import com.baidu.meet.MeetApplication;

/**
 * 网络实现器的工厂方法, 用于切换BDHttp的网络库还是使用原来的网络库
 * 
 * @author tony
 * 
 */
public class NetWorkCoreFacotry {
	private static NetWorkCoreFacotry instance;

	public final static int NetWorkCore_Type_Old = 0;
	public final static int NetWorkCore_Type_BdHttp = 1;

	private NetWorkCoreFacotry() {
	}

	public synchronized static NetWorkCoreFacotry getInstance() {
		if (instance == null) {
			instance = new NetWorkCoreFacotry();
		}

		return instance;
	}

	public INetWorkCore createINetWorkCore(NetWorkParam networkParam) {
		return new NetWorkCore(networkParam);
	}

	public static String getNetType() {
		try {
			ConnectivityManager cwjManager = (ConnectivityManager) MeetApplication.getApp().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkinfo = cwjManager.getActiveNetworkInfo();
			boolean netSataus = networkinfo.isAvailable();

			if (!netSataus) {
				return null;
			} else {
				if (networkinfo.getTypeName().equalsIgnoreCase("WIFI")) {
					return "wifi";
				} else {
					String proxyHost = android.net.Proxy.getDefaultHost();
					if (proxyHost != null && proxyHost.length() > 0) {
						return "wap";
					} else {
						return "net";
					}
				}
			}
		} catch (Exception ex) {
			return null;
		}
	}
}
