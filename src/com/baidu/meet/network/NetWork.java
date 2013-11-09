package com.baidu.meet.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;

import com.baidu.meet.MeetApplication;
import com.baidu.meet.util.FileHelper;

import android.content.Context;
import android.os.Handler;

/**
 * 封装网络操作的类
 * 
 * @author guagua
 * 
 */
public class NetWork {

	private static final int NET_TYPE_GET = 1;
	private static final int NET_TYPE_POST = 2;
	private static final int NET_TYPE_POST_CHUNK = 3;

	public final static String CLIENT_TYPE = "_client_type";
	public final static String CLIENT_VERSION = "_client_version";
	public final static String CLIENT_ID = "_client_id";
	public final static String FROM = "from";
	public final static String NET_TYPE = "net_type";
	public final static String PHONE_IMEI = "_phone_imei";

	public final static String CLIENT_TYPE_ANDROID = "2";

	private INetWorkCore mNet = null;
	private INetWorkCore mNetLogin = null;

	final private NetWorkParam netWorkParam = new NetWorkParam();

	private void initNetWork() {
		mNet = NetWorkCoreFacotry.getInstance().createINetWorkCore(netWorkParam);
		mNetLogin = null;
	}

	/**
	 * 构造函数
	 * 
	 */
	public NetWork() {
		initNetWork();
	}

	public void setIsJson(boolean is_json) {
		mNet.setIsJson(is_json);
	}

	/**
	 * 构造函数
	 * 
	 * @param url
	 *            URL地址
	 */
	public NetWork(String url) {
		initNetWork();
		mNet.setUrl(url);
	}

	/**
	 * 构造函数
	 * 
	 * @param context
	 *            上下文，最好使用应用上下以免发生内存泄露
	 * @param url
	 *            URL地址
	 */
	public NetWork(Context context, String url) {
		initNetWork();
		mNet.setContext(context);
		mNet.setUrl(url);
	}

	/**
	 * 构造函数
	 * 
	 * @param context
	 *            上下文，最好使用应用上下以免发生内存泄露
	 * @param url
	 *            URL地址
	 * @param PostData
	 *            需要Post的数据数组
	 */
	public NetWork(Context context, String url, ArrayList<BasicNameValuePair> PostData) {
		initNetWork();
		mNet.setContext(context);
		mNet.setUrl(url);
		mNet.setPostData(PostData);
	}

	/**
	 * 设置是否请求压缩格式，默认请求压缩格式
	 * 
	 * @param requestGzip
	 *            是否请求压缩格式
	 */
	public void setRequestGzip(Boolean requestGzip) {
		mNet.setRequestGzip(requestGzip);
	}

	public boolean getRequestGzip() {
		return mNet.getRequestGzip();
	}

	/**
	 * 设置URL地址
	 * 
	 * @param url
	 */
	public void setUrl(String url) {
		mNet.setUrl(url);
	}

	/**
	 * 获得当前的URL地址
	 * 
	 * @return
	 */
	public String getUrl() {
		return mNet.getUrl();
	}

	/**
	 * 设置context
	 * 
	 * @param context
	 *            上下文
	 */
	public void setContext(Context context) {
		mNet.setContext(context);
	}

	/**
	 * 获得context
	 * 
	 * @return context
	 */
	public Context getContext() {
		return mNet.getContext();
	}

	/**
	 * 获得post的数据数组
	 * 
	 * @return 数据
	 */
	public ArrayList<BasicNameValuePair> getPostData() {
		return mNet.getPostData();
	}

	/**
	 * 设置需要post的数据
	 * 
	 * @param mPostData
	 *            需要post的数据数组
	 */
	public void setPostData(ArrayList<BasicNameValuePair> mPostData) {
		mNet.setPostData(mPostData);
	}

	/**
	 * 添加需要post的数据
	 * 
	 * @param k
	 *            key值
	 * @param v
	 *            value值
	 */
	public void addPostData(String k, String v) {
		mNet.addPostData(k, v);
	}

	/**
	 * 添加需要post的数据
	 * 
	 * @param data
	 *            NameValuePair格式数据
	 */
	public void addPostData(BasicNameValuePair data) {
		mNet.addPostData(data);
	}

	/**
	 * 设置需要Post的图片资源
	 * 
	 * @param bmp
	 */
	public void addPostData(String k, byte[] v) {
		mNet.addPostData(k, v);
	}

	public void setIsBDImage(boolean isBDImage) {
		mNet.setIsBDImage(isBDImage);
	}

	public boolean getIsBDImage() {
		return mNet.getIsBDImage();
	}

	public int getNetDataSize() {
		return mNet.getNetDataSize();
	}

	private void addCommonParam() {
//		mNet.addPostData(CLIENT_TYPE, CLIENT_TYPE_ANDROID);
//		if (MeetApplication.getApp().getImei() != null) {
//			mNet.addPostData(PHONE_IMEI, MeetApplication.getApp().getImei());
//		}
//		String net_type = mNet.getNetType();
//		if (net_type != null) {
//			mNet.addPostData(NET_TYPE, net_type);
//		}
	}

	private int mErrorNums = 0;

	/**
	 * 判断是否请求成功
	 * 
	 * @return true：成功； false：失败
	 */
	public boolean isRequestSuccess() {
		return mNet.isRequestSuccess();
	}

	public boolean isNetSuccess() {
		return mNet.isNetSuccess();
	}

	public int getErrorCode() {
		return (int) mNet.getErrorCode();
	}

	public String getNetType() {
		return mNet.getNetType();
	}

	/**
	 * 获得失败的错误提示
	 * 
	 * @return
	 */
	public String getErrorString() {
		return mNet.getErrorString();
	}

	/**
	 * 取消当前的网络请求
	 */
	public void cancelNetConnect() {
		if (mNet != null) {
			mNet.cancelNetConnect();
		}
		if (mNetLogin != null) {
			mNetLogin.cancelNetConnect();
		}
	}


	/**
	 * 使用get方法请求网络数据
	 * 
	 * @return 服务器返回的字符串，应该先用isRequestSuccess判断是否请求成功，再使用返回的数据
	 */
	public byte[] getNetData() {
		addCommonParam();
		return mNet.getNetData();
	}

	private String process(int type) {
		String data = null;

		switch (type) {
		case NET_TYPE_GET:
			addCommonParam();
			data = mNet.getNetString();
			break;
		case NET_TYPE_POST:
			addCommonParam();
			data = mNet.postNetData();
			break;
		case NET_TYPE_POST_CHUNK:
			addCommonParam();
			data = mNet.postMultiNetData();
			break;
		default:
			return null;
		}
		mNet = new NetWorkCore(netWorkParam);
		switch (type) {
		case NET_TYPE_GET:
			addCommonParam();
			data = mNet.getNetString();
			break;
		case NET_TYPE_POST:
			addCommonParam();
			data = mNet.postNetData();
			break;
		case NET_TYPE_POST_CHUNK:
			addCommonParam();
			data = mNet.postMultiNetData();
			break;
		default:
			return null;
		}
		
		/////////////END 网络迁移结束后，可以删除以上代码
		

		if (!mNet.isNetSuccess()) {
			NetWorkState.addErrorNumsAndGet(mErrorNums);
			return data;
		}
		if (mNet.isRequestSuccess()) {
			return data;
		}

		return data;
	}

	private String process_second(int type) {
		String data = null;

		switch (type) {
		case NET_TYPE_GET:
			data = mNet.getNetString();
			break;
		case NET_TYPE_POST:
			data = mNet.postNetData();
			break;
		case NET_TYPE_POST_CHUNK:
			data = mNet.postMultiNetData();
			break;
		default:
			return null;
		}
		return data;
	}

	/**
	 * 以application/x-www-form-urlencoded格式post网络数据
	 * 
	 * @return 服务器返回的字符串
	 */
	public String postNetData() {
		return process(NET_TYPE_POST);
	}

	public String getNetString() {
		return process(NET_TYPE_GET);
	}

	/**
	 * 以multipart/form-data格式post网络数据
	 * 
	 * @return 服务器返回的字符串
	 */
	public String postMultiNetData() {
		return process(NET_TYPE_POST_CHUNK);
	}

	public String uploadImage(final String filename) throws IOException {
		byte[] data = null;
		try {
			InputStream in = FileHelper.GetStreamFromFile(filename);
			byte[] buf = new byte[1024 * 5];
			int num = -1;
			ByteArrayOutputStream outputstream = new ByteArrayOutputStream(1024 * 5);
			while ((num = in.read(buf)) != -1) {
				outputstream.write(buf, 0, num);
			}
			data = outputstream.toByteArray();
		} catch (Exception ex) {
		}
		if (data == null || data.length <= 0) {
			return null;
		}
		addPostData("pic", data);
		return postMultiNetData();
	}

	public boolean isFileSegSuccess() {
		return mNet.isFileSegSuccess();
	}

	/**
	 * 下载文件
	 * 
	 * @param path
	 *            保存文件的全路径
	 * @return true：成功； false：失败
	 */
	public Boolean downloadFile(String name, Handler handler, int what) {
		return mNet.downloadFile(name, handler, what);
	}
}
