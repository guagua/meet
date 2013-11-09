package com.baidu.meet.network;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.Handler;

/**
 * 接口， BDHttp和原有网络内核的接口类
 */
public interface INetWorkCore {

	long getCostTime();

	String getApiName();

	long getUpDataSize();

	long getDownDataSize();

	long getConnectTime();
	
	long getDNSTime();

	int getRetry();

	long getRspTime();

	int getErrorCode();

	void setIsJson(boolean is_json);

	void setUrl(String url);

	void setContext(Context context);

	void setPostData(ArrayList<BasicNameValuePair> postData);

	void setRequestGzip(Boolean requestGzip);

	Boolean getRequestGzip();

	String getUrl();

	Context getContext();

	ArrayList<BasicNameValuePair> getPostData();

	void addPostData(String k, String v);

	void addPostData(BasicNameValuePair data);

	void addPostData(String k, byte[] v);

	void setIsBDImage(boolean isBDImage);

	boolean getIsBDImage();

	int getNetDataSize();

	String getNetType();

	boolean isRequestSuccess();

	boolean isNetSuccess();

	String getErrorString();

	void cancelNetConnect();

	String postNetData();

	void setErrorString(String string);

	void cleanErrorString();

	byte[] getNetData();

	String getNetString();

	String postMultiNetData();

	boolean isFileSegSuccess();

	Boolean downloadFile(String name, Handler handler, int what);

	void setIsBaiduServer(boolean b);

}
