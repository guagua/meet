package com.baidu.meet.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.apache.http.message.BasicNameValuePair;

import com.baidu.meet.MeetApplication;
import com.baidu.meet.R;
import com.baidu.meet.config.Config;
import com.baidu.meet.data.ErrorData;
import com.baidu.meet.log.MeetLog;
import com.baidu.meet.util.BitmapHelper;
import com.baidu.meet.util.FileHelper;
import com.baidu.meet.util.GzipHelper;
import com.baidu.meet.util.StringHelper;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;

/**
 * 封装网络操作的类
 * 趋势是已经废弃的， 启用NetWorkCoreByBdHttp
 *
 */
public class NetWorkCore implements INetWorkCore {
	static public enum NetworkState{UNAVAIL,WIFI,MOBILE};
	static public enum NetworkStateInfo{UNAVAIL,WIFI,TwoG,ThreeG};
	public final static String NET_TYPE_NET = "1";
	public final static String NET_TYPE_WAP = "2";
	public final static String NET_TYPE_WIFI = "3";
	
	static private String end = "\r\n";
	static private String twoHypens = "--";
	static private String boundary = "--------7da3d81520810*";
	static private int MAX_DATA_LENG = 2097152; //2*1024*1024
	static private final int BUFFERSIZE = 1024;
	static private final int MAX_RETRY_COUNT = 10;
	static private final int CONNECTTIMEOUT = 5*1000;
	static private final int GETDATATIMEOUT = 30*1000;
	static private final int POSTDATATIMEOUT = 15*1000;
	static private Handler mHandler = null;
	
	static private volatile String mProxyUser = null;
	static private volatile boolean mHaveInitProxyUser = false;
	static private Pattern mPattern = Pattern.compile("^[0]{0,1}10\\.[0]{1,3}\\.[0]{1,3}\\.172$", Pattern.MULTILINE);

	private Context mContext;
	private HttpURLConnection mConn;
	private int mWapRetryConnt;
	private boolean mIsLimited;
	private volatile boolean mIsInterrupte;
	private int mDataSize = 0;
	private NetWorkParam netWorkParam;

	
	/**
	 * 构造函数  
	 * 
	 */
	public NetWorkCore(NetWorkParam netWorkParam){
		initNetWork();
		this.netWorkParam = netWorkParam;
		mContext = MeetApplication.getApp();
	}
	//统计添加项
	private int mUpDataSize = 0;
	private long mDNSTime = 0;
	private long mConnectTime = 0;
	private long mRspTime = 0;
	private int mRetry = 0;
	private long mAllCostTime = 0;
	
	public long getUpDataSize(){
		return mUpDataSize;
	}
	
	public long getDownDataSize(){
		return mDataSize;
	}
	
	public long getConnectTime(){
		return mConnectTime;
	}
	
	public long getDNSTime(){
		return mDNSTime;
	}
	
	public long getRspTime(){
		return mRspTime;
	}
	
	public int getRetry(){
		return mRetry;
	}
	
	public String getApiName() {
		if (netWorkParam.mUrl == null) {
			return null;
		}
		String prefix = Config.SERVER_ADDRESS;
		if (netWorkParam.mUrl.startsWith(prefix)) {
			int end = netWorkParam.mUrl.indexOf('?');
			end = end < 0 ? netWorkParam.mUrl.length() : end;
			return netWorkParam.mUrl.substring(prefix.length(), end);
		} else {
			return netWorkParam.mUrl;
		}
	}
	
	public long getCostTime(){
		return mAllCostTime;
	}
	
	

	private int getMode(NetworkStateInfo info){
		int mode = 0;
		switch (info) {
		case WIFI:
			mode = 1;
			break;
		case TwoG:
			mode = 2;
			break;
		case ThreeG:
			mode = 3;
			break;
		}
		return mode;
	}
	
	private void initNetWork(){
		mConn = null;
		mContext = null;
		mWapRetryConnt = 0;
		mIsInterrupte = false;
		mIsLimited = false;
		initPorxyUser();
	}
	
	public void setIsJson(boolean is_json){
		netWorkParam.mIsJson = is_json;
	}
	
	public String getNetType(){
		try{
			ConnectivityManager cwjManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);    
			NetworkInfo networkinfo = cwjManager.getActiveNetworkInfo();
			boolean netSataus = networkinfo.isAvailable();    
	
			if (!netSataus) {    
				return null;
			}else{
				if(networkinfo.getTypeName().equalsIgnoreCase("WIFI")){
					return NET_TYPE_WIFI;
				}else{
					String proxyHost = android.net.Proxy.getDefaultHost();    
					if (proxyHost != null && proxyHost.length() > 0) {
						return NET_TYPE_WAP;
					}else{
						return NET_TYPE_NET;
					}
				}
			}
		}catch(Exception ex){
			return null;
		}
	}
	
	static public void initPorxyUser(){
		synchronized(NetWorkCore.class){
			if(mHaveInitProxyUser == false){
				mHaveInitProxyUser = true;
				setProxyUser();
			}
		}
	}
	
	static public synchronized void setProxyUser() {
		try{
			Uri uri = Uri.parse("content://telephony/carriers/preferapn");
			Cursor apn = MeetApplication.getApp().getContentResolver().query(uri, null, null, null, null);
			if (apn != null && apn.moveToNext()){
				String name = apn.getString(apn.getColumnIndex("user"));
				String pwd = apn.getString(apn.getColumnIndex("password"));
				apn.close();
				String login = name + ":" + pwd;
				String encodedLogin = StringHelper.base64Encode(login.getBytes());
				mProxyUser = "Basic " + encodedLogin;
			}
		}catch(Exception ex){}
	}
	
	static public void initNetWorkCore(){
		mHandler = new Handler(){
	
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				try{
					NetWorkCore network = (NetWorkCore)msg.obj;
					if(network != null){
						network.cancelNetConnect();
					}
				}catch(Exception ex){
					MeetLog.e(this.getClass().getName(), "initNetWorkCore", ex.getMessage());
				}
			}
			
		};
	}
	
	
	
	
	/**
	 * 设置是否请求压缩格式，默认请求压缩格式
	 * @param requestGzip  是否请求压缩格式
	 */
	public void setRequestGzip(Boolean requestGzip){
		netWorkParam.mRequestGzip = requestGzip;
	}
	
	/**
	 * 获得当前是否请求压缩格式
	 * @return  true：请求；   false：不请求
	 */
	public Boolean getRequestGzip(){
		return netWorkParam.mRequestGzip;
	}
	
	/**
	 * 静态方法，获取当前网络的状态信息
	 * @param context 上下文
	 * @return	NetworkState 状态
	 */
	static public NetworkState getNetworkState(Context context){
		boolean netSataus = false;    
		NetworkInfo networkinfo = null;
		NetworkState ret = NetworkState.UNAVAIL;
		try{
			ConnectivityManager cwjManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);    
			networkinfo = cwjManager.getActiveNetworkInfo();
			netSataus = networkinfo.isAvailable(); 

			if (!netSataus) {    
				ret = NetworkState.UNAVAIL;
			}else{
				if(networkinfo.getTypeName().equalsIgnoreCase("WIFI")){
					ret = NetworkState.WIFI;
				}else{
					ret = NetworkState.MOBILE;
				}
			}
		}catch(Exception ex){
			MeetLog.e(ex.getMessage());
		}
		return ret;
	}
	
	/**
	 * 设置URL地址
	 * @param url
	 */
	public void setUrl(String url){
		netWorkParam.mUrl = url;
	}
	/**
	 * 获得当前的URL地址
	 * @return
	 */
	public String getUrl(){
		return netWorkParam.mUrl;
	}
	
	/**
	 * 判断是否请求成功
	 * @return  true：成功；   false：失败
	 */
	public boolean isRequestSuccess(){
		if(netWorkParam.mNetErrorCode == HttpStatus.SC_OK && netWorkParam.mServerErrorCode == 0){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isNetSuccess() {
		if(netWorkParam.mNetErrorCode == HttpStatus.SC_OK) {
			return true;
		} else {
			return false;
		}
	}
	
	public int getErrorCode() {
		return netWorkParam.mServerErrorCode;
	}
	
	public int getNetErrorCode() {
		return netWorkParam.mNetErrorCode;
	}
	
	/**
	 * 获得失败的错误提示
	 * @return
	 */
	public String getErrorString(){
		return netWorkParam.mErrorString;
	}
	
	public void cleanErrorString() {
		netWorkParam.mErrorString = "";
	}
	
	public void setErrorString(String err) {
		netWorkParam.mErrorString = err;
	}
	
	public void setErrorCode(int code) {
		netWorkParam.mServerErrorCode = code;
	}
	
	/**
	 * 取消当前的网络请求
	 */
	public void cancelNetConnect(){
		mIsInterrupte = true;
		try{
			if(mConn != null){
				mConn.disconnect();
			}
		}catch(Exception ex){
			
		}
	}
	
	/**
	 * 连接服务器
	 * @param url 地址
	 * @return HttpURLConnection  null表示失败
	 */
	private HttpURLConnection getConnect(URL url){
		HttpURLConnection conn = null;
		NetworkState state = getNetworkState(mContext);
		mIsLimited = false;
		
		try{
			if(state == NetworkState.UNAVAIL){
				return null;
			}else if(state == NetworkState.MOBILE){
				String proxyHost = android.net.Proxy.getDefaultHost();    
				if (proxyHost != null && proxyHost.length() > 0) {
					if(isCMCCServer(proxyHost)){
						mIsLimited = true;
						StringBuilder new_address = new StringBuilder(80);
						new_address.append("http://");
						new_address.append(android.net.Proxy.getDefaultHost());
						String file = url.getFile();
						if(file != null && file.startsWith("?")){
							new_address.append("/");
						}
						new_address.append(file);
						URL new_url = new URL(new_address.toString());   
						conn = (HttpURLConnection) new_url.openConnection();   
						conn.setRequestProperty("X-Online-Host", url.getHost());
					}else{
						java.net.Proxy p = null;
						p = new java.net.Proxy(java.net.Proxy.Type.HTTP,    
								new InetSocketAddress(android.net.Proxy.getDefaultHost(),android.net.Proxy.getDefaultPort()));  
						conn = (HttpURLConnection)url.openConnection(p);
						if(mProxyUser != null){
							conn.setRequestProperty("Proxy-Authorization", mProxyUser); 
						}
					}
				}
			}
			if(conn == null){
				conn = (HttpURLConnection)url.openConnection();
			}
		}catch(Exception ex){
			MeetLog.e(getClass().getName(), "getConnect", "error = " + ex.getMessage());
		}
		return conn;
	}
	
	private boolean isCMCCServer(String ip){
		boolean ret = false;
		Matcher m = mPattern.matcher(ip);
		if(m.find()){
			ret = true;
		}else{
			ret = false;
		}
		return ret;
	}
	
	/**
	 * 使用get方法请求网络数据
	 * 
	 * @return  服务器返回的字符串，应该先用isRequestSuccess判断是否请求成功，再使用返回的数据
	 */
	public byte[] getNetData(){
		byte[]output = null;
		boolean is_net_error = true;
		long time = 0;
		URL url = null;
		try{
			String address = null;
			address = netWorkParam.mUrl;
			url = new URL(address);
			mUpDataSize = address.length();
			if(Config.getDebugSwitch()){
		    	MeetLog.d(this.getClass().getName(), "getNetData", address);
		    }
		}catch(Exception ex){
			MeetLog.e(this.getClass().getName(), "getNetData", ex.getMessage());
			return output;
		}
		
		for(int retry = 0; mIsInterrupte == false && is_net_error == true && retry < MAX_RETRY_COUNT; retry++){
			ByteArrayOutputStream outputstream = null;
			InputStream in = null;
			try{
				mConn = getConnect(url);
				if(mConn == null){
					throw new java.net.SocketException();
				}
				mConn.setConnectTimeout(CONNECTTIMEOUT);
				mConn.setReadTimeout(GETDATATIMEOUT);
				if ((netWorkParam.mRequestGzip == true && netWorkParam.mIsBDImage == false)) {
					mConn.setRequestProperty("Accept-Encoding", "gzip");
				}
				if(mIsInterrupte == true){
					break;
				}
				time = new Date().getTime();
				checkDNS(url);
				mDNSTime = new Date().getTime() - time;
				mConn.connect();
				mConnectTime = new Date().getTime()-time - mDNSTime;
				netWorkParam.mNetErrorCode = mConn.getResponseCode();
				if(netWorkParam.mNetErrorCode != HttpStatus.SC_OK){
					throw new java.net.SocketException();
				}
	
				/**
				 * 判断是否是移动的提示信息
				 */
				if(mConn.getContentType().contains("text/vnd.wap.wml") == true){
					if(mWapRetryConnt < 1){
						mConn.disconnect();
						mWapRetryConnt++;
						netWorkParam.mNetErrorCode = 0;
						retry--;
						continue;
					}else{
						break;
					}
				}
				
				String encodeing = mConn.getContentEncoding();
	
				in = mConn.getInputStream();
				
				String contentLengthHeader = mConn.getHeaderField("Content-Length") ;
					
				if(contentLengthHeader != null){
					try{
						int sizeOfImageFile = Integer.parseInt(contentLengthHeader) ;

						//超过最大允许长度的文件，直接拒绝下载。
						if(sizeOfImageFile > MAX_DATA_LENG){
							return null ;
						}

						//按照10倍压缩提前预留出缓存，避免OOM。
						sizeOfImageFile = sizeOfImageFile * BitmapHelper.FILE_2_BITMAP_MUL ;

					}catch(Throwable t){

					}
				}
				
				byte[] buf = new byte[BUFFERSIZE];
				int num = -1;
				outputstream = new ByteArrayOutputStream(BUFFERSIZE);
				int size = 0;
				
				while (mIsInterrupte == false && size < MAX_DATA_LENG && (num = in.read(buf)) != -1) {  
					//TODO: 这个调用引起了大量OOM
	    			outputstream.write(buf, 0, num);
					size += num;
				}
				if(mIsInterrupte == true){
					break;
				}
				mDataSize = size;
				time = new Date().getTime() - time;
				mRspTime = time - mConnectTime - mDNSTime;
				MeetLog.i(getClass().getName(), "getNetData", "time = " + String.valueOf(time) + "ms");
				if(size < MAX_DATA_LENG){
					output = outputstream.toByteArray();
					outputstream.close();
					MeetLog.i(getClass().getName(), "getNetData", "data.zise = " + String.valueOf(size));
					if(encodeing != null && encodeing.contains("gzip")){
						ByteArrayInputStream tmpInput = new ByteArrayInputStream(output);
						ByteArrayOutputStream tmpOutput = new ByteArrayOutputStream(BUFFERSIZE);
						GzipHelper.decompress(tmpInput, tmpOutput);
						output = tmpOutput.toByteArray();
					}
				}else{
					netWorkParam.mNetErrorCode = -1;
					netWorkParam.mErrorString = mContext.getResources().getString(R.string.data_too_big);
				}
				mRetry = retry + 1;
				mAllCostTime = time;
				break;
			}catch(java.net.SocketException ex){
				netWorkParam.mNetErrorCode = 0;
				NetWorkState.mErrorNums.incrementAndGet();
				is_net_error = true;
				netWorkParam.mErrorString = mContext.getResources().getString(R.string.neterror);
			}catch(java.net.SocketTimeoutException ex){
				NetWorkState.mErrorNums.incrementAndGet();
				netWorkParam.mNetErrorCode = 0;
				is_net_error = true;
				netWorkParam.mErrorString = mContext.getResources().getString(R.string.neterror);
			}catch(Exception ex){
				netWorkParam.mNetErrorCode = 0;
				is_net_error = false;
				netWorkParam.mErrorString = mContext.getResources().getString(R.string.neterror);;
				MeetLog.e(getClass().getName(), "getNetData", "error = " + ex.getMessage());
			}finally{
				try{
					if(in != null){
						in.close();
					}
				}catch(Exception ex){}			
				try{
					if(mConn != null){
						mConn.disconnect();
					}
				}catch(Exception ex){}
			}
		}
		mWapRetryConnt = 0;
		return output;
	}
	
	/**
	 * 读取服务器返回的数据编码格式
	 * @return
	 * @throws Exception
	 */
	private String getCharset() throws Exception{
		String type = null;
		if(mConn != null){
			type = mConn.getContentType();
		}
		String charset = "utf-8";
		if(type != null){
			 int index = type.indexOf("charset");
			 if(index != -1){
				 int end = type.indexOf(' ', index);
				 if(end == -1){
					 charset = type.substring(index + 8);
				 }else{
					 charset = type.substring(index + 8, end);
				 }
			 }
		}
		return charset;
	}
	
	/**
	 * 解析服务器错误码
	 */
	public void parseServerCode(String data){
		netWorkParam.mServerErrorCode = -1;
		try{
			if(data != null){
				ErrorData error = new ErrorData();
				error.parserJson(data);
				netWorkParam.mServerErrorCode = error.getError_code();
				if(netWorkParam.mServerErrorCode == -1){
					netWorkParam.mErrorString = mContext.getString(R.string.error_unkown_try_again);
				}else if(netWorkParam.mServerErrorCode != 0){
					netWorkParam.mErrorString = error.getError_msg();
				}
			}
		}catch(Exception ex){
			MeetLog.e("NetWork", "parseServerCode", "error = " + ex.getMessage());
			netWorkParam.mErrorString = mContext.getString(R.string.error_unkown_try_again);
		}
		return;
	}
	
	/**
	 * 联网获得服务器返回的字符串
	 * @return 字符串，应该先用isRequestSuccess判断是否请求成功，再使用返回的字符串
	 */
	public String getNetString(){
		byte[] data = getNetData();
		String retData = null;
		if(netWorkParam.mNetErrorCode == HttpStatus.SC_OK){
			try{
				String charset = getCharset();
				retData=new String(data, 0, data.length, charset);
				parseServerCode(retData);
			}catch(Exception ex){
				MeetLog.e(getClass().getName(), "getNetString", "error = " + ex.getMessage());
			}
		}
		return retData;
	}
	
	/**
	 * 以application/x-www-form-urlencoded格式post网络数据 
	 * @return	服务器返回的字符串
	 */
	public String postNetData(){
		long time = 0;
		byte[]output = null;
		String retData = null;
		boolean is_net_error = true;
		String postdata = null;
		int i = 0;
		BasicNameValuePair kv = null;
		StringBuilder build = new StringBuilder(BUFFERSIZE);
        for(i = 0; netWorkParam.mPostData != null && i < netWorkParam.mPostData.size(); i++){
        	kv = netWorkParam.mPostData.get(i);
        	if(kv == null){
        		continue;
        	}
        	String k = kv.getName();
        	String v = kv.getValue();
//        	if(i != 0){
        		build.append("&");
//        	}
        	build.append(k + "=");
        	build.append(StringHelper.getUrlEncode(v));
        }
        
        postdata = build.toString();
        mUpDataSize = postdata.length();
        if(Config.getDebugSwitch()){
        	MeetLog.d(this.getClass().getName(), "postNetData", netWorkParam.mUrl + postdata);
        }
		for(int retry = 0; mIsInterrupte == false && is_net_error == true && retry < MAX_RETRY_COUNT; retry++){
			InputStream in = null;
			ByteArrayOutputStream outputstream = null;
			try{
				URL url = new URL(netWorkParam.mUrl);
				mConn = getConnect(url);
				if(mConn == null){
					netWorkParam.mErrorString = mContext.getResources().getString(R.string.neterror);
					break;
				}
				mConn.setConnectTimeout(CONNECTTIMEOUT);
				mConn.setReadTimeout(POSTDATATIMEOUT);
				mConn.setDoOutput(true);
				mConn.setDoInput(true);
				mConn.setRequestMethod("POST");
				mConn.setRequestProperty("Charset","UTF-8");
				if(netWorkParam.mRequestGzip == true){
					mConn.setRequestProperty("Accept-Encoding", "gzip");
				}
				if(mIsInterrupte == true){
					break;
				}
				time = new Date().getTime();
				checkDNS(url);
				mDNSTime = new Date().getTime()-time;
				mConn.connect();
				mConnectTime = new Date().getTime()-time - mDNSTime;
				DataOutputStream ds = new DataOutputStream(mConn.getOutputStream());
		        if(mIsInterrupte == false){
		        	ds.writeBytes(postdata);
		        }
		        ds.flush();
		        MeetLog.i("NetWork", "postNetData", "Post data.zise = " + String.valueOf(ds.size()));
		        
		        ds.close();
		        netWorkParam.mNetErrorCode = mConn.getResponseCode();
		    	if (netWorkParam.mNetErrorCode != HttpStatus.SC_OK) {
					if (netWorkParam.mNetErrorCode == java.net.HttpURLConnection.HTTP_ACCEPTED
							|| netWorkParam.mNetErrorCode == java.net.HttpURLConnection.HTTP_CREATED
							|| netWorkParam.mNetErrorCode == java.net.HttpURLConnection.HTTP_RESET
							|| netWorkParam.mNetErrorCode == java.net.HttpURLConnection.HTTP_NOT_MODIFIED
							|| netWorkParam.mNetErrorCode == java.net.HttpURLConnection.HTTP_USE_PROXY
							|| netWorkParam.mNetErrorCode == java.net.HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
						throw new java.net.SocketException("retry");
					} else if (netWorkParam.mNetErrorCode == java.net.HttpURLConnection.HTTP_BAD_GATEWAY
							|| netWorkParam.mNetErrorCode == java.net.HttpURLConnection.HTTP_UNAVAILABLE
							|| netWorkParam.mNetErrorCode == java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT) {
						//遇到这几种错误号， 不进行重连
						return null;
					}
				}
				
				String encodeing = mConn.getContentEncoding();
				in = mConn.getInputStream();
				byte[] buf = new byte[BUFFERSIZE];
				int num = -1;
				outputstream = new ByteArrayOutputStream(BUFFERSIZE);
				int size = 0;
				while (mIsInterrupte == false && (num = in.read(buf)) != -1) {  
					outputstream.write(buf, 0, num);
					size += num;
				}
				in.close();
				mConn.disconnect();
				if(mIsInterrupte == true){
					break;
				}
				mDataSize = size;
				time = new Date().getTime() - time;
				mRspTime = time - mConnectTime - mDNSTime;
				MeetLog.i(getClass().getName(), "postNetData", "time = " + String.valueOf(time) + "ms");
				output = outputstream.toByteArray();
		        MeetLog.i(getClass().getName(), "postNetData", "Get data.zise = " + String.valueOf(output.length));
		
				if(encodeing != null && encodeing.contains("gzip")){
					ByteArrayInputStream tmpInput = new ByteArrayInputStream(output);
					ByteArrayOutputStream tmpOutput = new ByteArrayOutputStream(BUFFERSIZE);
					GzipHelper.decompress(tmpInput, tmpOutput);
					output = tmpOutput.toByteArray();
					MeetLog.i(getClass().getName(), "postNetData", "After ungzip data.zise = " + String.valueOf(output.length));
				}
				String charset = getCharset();
				retData = new String(output, 0, output.length, charset);
				if(netWorkParam.mIsBaiduServer == true && netWorkParam.mIsJson == true){
					parseServerCode(retData);
				}
				mRetry = retry + 1;
				mAllCostTime = time;
				break;
			}catch(java.net.SocketException ex){
				NetWorkState.mErrorNums.incrementAndGet();
				netWorkParam.mNetErrorCode = 0;
				is_net_error = true;
				netWorkParam.mErrorString = mContext.getResources().getString(R.string.neterror);
				MeetLog.e(getClass().getName(), "postNetData", "SocketException " + ex.getMessage());
			}catch(java.net.SocketTimeoutException ex){
				NetWorkState.mErrorNums.incrementAndGet();
				netWorkParam.mNetErrorCode = 0;
				is_net_error = true;
				netWorkParam.mErrorString = mContext.getResources().getString(R.string.neterror);
				MeetLog.e(getClass().getName(), "postNetData", "SocketTimeoutException " + ex.getMessage());
			}catch(Exception ex){
				netWorkParam.mNetErrorCode = 0;
				is_net_error = false;
				netWorkParam.mErrorString = mContext.getResources().getString(R.string.neterror);
				MeetLog.e(getClass().getName(), "postNetData", ex.getMessage());
			}finally{
				try{
					if(in != null){
						in.close();
					}
				}catch(Exception ex){}			
				try{
					if(mConn != null){
						mConn.disconnect();
					}
				}catch(Exception ex){}
			}
		}
		mWapRetryConnt = 0;
		return retData;
	}
	
	public int getNetDataSize(){
		return mDataSize;
	}
	/**
	 * 以multipart/form-data格式post网络数据 
	 * @return	服务器返回的字符串
	 */
	public String postMultiNetData(){
		byte[]output = null;
		String retData = null;
		long time = 0;
		boolean is_net_error = true;
		for(int retry = 0; mIsInterrupte == false && is_net_error == true && retry < MAX_RETRY_COUNT; retry++){
			ByteArrayOutputStream outputstream = null;
			InputStream in = null;
			DataOutputStream ds = null;
			try{
				URL url = new URL(netWorkParam.mUrl);
				mConn = getConnect(url);
				if(mConn == null){
					netWorkParam.mErrorString = mContext.getResources().getString(R.string.neterror);
					break;
				}
				mConn.setConnectTimeout(CONNECTTIMEOUT);
				mConn.setReadTimeout(POSTDATATIMEOUT);
				mConn.setDoOutput(true);
				mConn.setDoInput(true);
				mConn.setRequestMethod("POST");
				mConn.setRequestProperty("Charset","UTF-8");
				mConn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
				
				if(netWorkParam.mRequestGzip == true){
					mConn.setRequestProperty("Accept-Encoding", "gzip");
				}
				if(mIsInterrupte == true){
					break;
				}
				time = new Date().getTime();
				mConn.connect();
				ds = new DataOutputStream(mConn.getOutputStream());
				BasicNameValuePair kv = null;
				
		        for(int i = 0; netWorkParam.mPostData != null && i < netWorkParam.mPostData.size() && mIsInterrupte == false; i++){
		        	kv = netWorkParam.mPostData.get(i);
		        	if(kv==null){
		        		continue;
		        	}
		        	String k = kv.getName();
		        	String v = kv.getValue();
		        	ds.writeBytes(twoHypens+boundary+end);
		 	        byte[] vbuffer = v.getBytes("UTF-8");
		 	        ds.writeBytes("Content-Disposition: form-data; name=\""+k+"\""+end);
		 	        ds.writeBytes(end);
		 	        ds.write(vbuffer);
		 	        ds.writeBytes(end);
		        }
	
		        if(mIsInterrupte == false && netWorkParam.mFileData != null){
			        for(Entry<String, byte[]> entry : netWorkParam.mFileData.entrySet()){
			        	String k = entry.getKey();
			        	byte[] v = entry.getValue();
			        	if(mIsInterrupte == true){
			        		break;
			        	}
			        	if(v == null){
			        		continue;
			        	}
			        	ds.writeBytes(twoHypens+boundary+end);
			 	        ds.writeBytes("Content-Disposition: form-data; name=\""+k+"\"; filename=\"file\""+end);
			 	        ds.writeBytes(end);
			 	        ds.write(v);
			 	        ds.writeBytes(end);
			        }
		        }
		        
		        ds.writeBytes(twoHypens+boundary+twoHypens+end);
		        ds.flush();
		        MeetLog.i("NetWork", "postMultiNetData", "Post data.zise = " + String.valueOf(ds.size()));
		        ds.close();
		        
		        //发送大文件，使用wap模式会引起getResponseCode无法返回
		        if(mHandler != null){
		        	mHandler.sendMessageDelayed(mHandler.obtainMessage(0, this), POSTDATATIMEOUT * 3);
		        }
		        netWorkParam.mNetErrorCode = mConn.getResponseCode();
				if(mHandler != null){
					mHandler.removeMessages(0, this);
				}
		        
				if(netWorkParam.mNetErrorCode != HttpStatus.SC_OK){
					throw new java.net.SocketException();
				}
				
				if(mConn.getContentType().contains("text/vnd.wap.wml") == true){
					if(mWapRetryConnt < 1){
						mConn.disconnect();
						mWapRetryConnt++;
						retry--;
						netWorkParam.mNetErrorCode = 0;
						continue;
					}else{
						break;
					}
				}
				
				String encodeing = mConn.getContentEncoding();
	
				in = mConn.getInputStream();
				
				byte[] buf = new byte[BUFFERSIZE];
				int num = -1;
				outputstream = new ByteArrayOutputStream(BUFFERSIZE);
				while (mIsInterrupte == false && (num = in.read(buf)) != -1) {  
					outputstream.write(buf, 0, num);
				}
				if(mIsInterrupte == true){
					break;
				}
				in.close();
				mConn.disconnect();
				time = new Date().getTime() - time;
				MeetLog.i("NetWork", "postMultiNetData", "time = " + String.valueOf(time) + "ms");
				output = outputstream.toByteArray();
		        MeetLog.i("NetWork", "postMultiNetData", "Get data.zise = " + String.valueOf(output.length));

				if(encodeing != null && encodeing.contains("gzip")){
					ByteArrayInputStream tmpInput = new ByteArrayInputStream(output);
					ByteArrayOutputStream tmpOutput = new ByteArrayOutputStream(BUFFERSIZE);
					GzipHelper.decompress(tmpInput, tmpOutput);
					output = tmpOutput.toByteArray();
				}
				String charset = getCharset();
				retData = new String(output, 0, output.length, charset);
				parseServerCode(retData);
				break;
			}catch(java.net.SocketException ex){
				is_net_error = true;
				netWorkParam.mNetErrorCode = 0;
				netWorkParam.mErrorString = mContext.getResources().getString(R.string.neterror);
			}catch(java.net.SocketTimeoutException ex){
				netWorkParam.mNetErrorCode = 0;
				is_net_error = true;
				netWorkParam.mErrorString = mContext.getResources().getString(R.string.neterror);
			}catch(Exception ex){
				netWorkParam.mNetErrorCode = 0;
				is_net_error = false;
				netWorkParam.mErrorString = mContext.getResources().getString(R.string.neterror);
				MeetLog.e("NetWork", "postMultiNetData", "error = " + ex.getMessage());
			}finally{
				try{
					if(in != null){
						in.close();
					}
				}catch(Exception ex){}			
				try{
					if(mConn != null){
						mConn.disconnect();
					}
				}catch(Exception ex){}
				try {
					if(ds != null){
						ds.close();
					}
				}catch(Exception ex){}
				if(mHandler != null){
					mHandler.removeMessages(0, this);
				}
			}
		}
		mWapRetryConnt = 0;
		return retData;
	}
	
	public boolean isFileSegSuccess(){
		if(netWorkParam.mNetErrorCode != HttpStatus.SC_OK && netWorkParam.mNetErrorCode != HttpStatus.SC_PARTIAL_CONTENT){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 下载文件
	 * @param path 保存文件的全路径
	 * @return true：成功；   false：失败
	 */
	public Boolean downloadFile(String name, Handler handler, int what){
		InputStream in = null;
		Boolean ret = false;
		long time = 0;
		FileOutputStream fileStream = null;
		try{
			URL url = new URL(netWorkParam.mUrl);
			mConn = getConnect(url);
			if(mConn == null){
				throw new java.net.SocketException();
			}
			mConn.setConnectTimeout(CONNECTTIMEOUT);
			mConn.setReadTimeout(GETDATATIMEOUT);
			mConn.setInstanceFollowRedirects(false);
			if(mIsInterrupte == true){
				return false;
			}
			time = new Date().getTime();
			
			File file = FileHelper.CreateFileIfNotFound(name);
			if(file == null){
				throw new FileNotFoundException();
			}
			long file_length = file.length();
			fileStream = new FileOutputStream(file, true);
			if(mIsLimited == true){
				mConn.addRequestProperty("Range", "bytes=" + String.valueOf(file_length) + "-" + String.valueOf(file_length + 200000));
			}else{
				mConn.addRequestProperty("Range", "bytes=" + String.valueOf(file_length) + "-");
			}
			
			mConn.connect();
			netWorkParam.mNetErrorCode = mConn.getResponseCode();
			if(!isFileSegSuccess()){
				throw new java.net.SocketException();
			}

			/**
			 * 判断是否是移动的提示信息
			 */
			if(mConn.getContentType().contains("text/vnd.wap.wml") == true
					&& mWapRetryConnt < 1){
				mConn.disconnect();
				mWapRetryConnt++;
				netWorkParam.mNetErrorCode = 0;
				return downloadFile(name, handler, what);
			}else{
				mWapRetryConnt = 0;
			}
			int contentLen = 0;	
			String range = mConn.getHeaderField("Content-Range");
			if(range != null){
				int index = range.indexOf("/");
				if(index != -1){
					contentLen = Integer.valueOf(range.substring(index + 1));
				}
			}
			if(contentLen == 0 && netWorkParam.mNetErrorCode == HttpStatus.SC_OK){
				String length = mConn.getHeaderField("Content-Length");
				if(length != null){
					contentLen = Integer.valueOf(length);
				}
			}
			
			if(file_length >= contentLen){
				return true;
			}
			in = mConn.getInputStream();
			byte[] buf = new byte[BUFFERSIZE];
			int num = -1;
			int datalenth = 0;
			int notify_num = 0;
			if(contentLen > 0){
				notify_num = contentLen / 50;
			}
			int notify_tmp = 0;
			if(handler != null && file_length > 0){
				handler.sendMessage(handler.obtainMessage(what, (int)file_length, contentLen));
			}
			while (mIsInterrupte == false && (num = in.read(buf)) != -1) {  
				try{
					fileStream.write(buf, 0, num);
				}catch(Exception ex){
					throw new FileNotFoundException();
				}
				datalenth += num;
				notify_tmp += num;
				if(handler != null && (notify_tmp > notify_num || datalenth == contentLen)){
					notify_tmp = 0;
					handler.sendMessage(handler.obtainMessage(what, (int)(datalenth + file_length), contentLen));
				}
			}
			try{
				fileStream.flush();
			}catch(Exception ex){
				throw new FileNotFoundException();
			}
			time = new Date().getTime() - time;
			MeetLog.i("NetWork", "downloadFile", "time = " + String.valueOf(time) + "ms");
			if(contentLen != -1){
				MeetLog.i("NetWork", "downloadFile", "data.zise = " + String.valueOf(contentLen));				
			}
			if(datalenth + file_length >= contentLen){
				ret = true;
			}
		}catch(FileNotFoundException ex){
			netWorkParam.mNetErrorCode = -2;
			netWorkParam.mErrorString = mContext.getResources().getString(R.string.FileWriteError);
		}catch(Exception ex){
			netWorkParam.mNetErrorCode = 0;
			netWorkParam.mErrorString = mContext.getResources().getString(R.string.neterror);
			MeetLog.e("NetWork", "downloadFile", "error = " + ex.getMessage());
		}finally{
			mWapRetryConnt = 0;
			try{
				if(in != null){
					in.close();
				}
			}catch(Exception ex){}			
			try{
				if(mConn != null){
					mConn.disconnect();
				}
			}catch(Exception ex){}
			try{
				if(fileStream != null){
					fileStream.close();
				}
			}catch(Exception ex){}
		}
		return ret;
	}

	
	/**
	 * 设置context
	 * @param context 上下文
	 */
	public void setContext(Context context) {
		this.mContext = context;
	}

	/**
	 * 获得context
	 * @return context
	 */
	public Context getContext() {
		return mContext;
	}
	
	/**
	 * 获得post的数据数组
	 * @return 数据
	 */
	public ArrayList<BasicNameValuePair> getPostData() {
		return netWorkParam.mPostData;
	}

	/**
	 * 设置需要post的数据
	 * @param mPostData  需要post的数据数组
	 */
	public void setPostData(ArrayList<BasicNameValuePair> postData) {
		if(netWorkParam.mPostData != null){
			netWorkParam.mPostData.clear();
		}
		for(int i = 0; i < postData.size(); i++){
			addPostData(postData.get(i));
		}
	}

	/**
	 * 添加需要post的数据
	 * @param k  key值
	 * @param v	 value值
	 */
	public void addPostData(String k, String v){
		BasicNameValuePair data = new BasicNameValuePair(k, v);
		addPostData(data);
	}
	
	/**
	 * 添加需要post的数据
	 * @param k  key值
	 * @param v	 value值
	 */
	public void addPostData(String k, byte[] v){
		if(netWorkParam.mFileData == null){
			netWorkParam.mFileData = new HashMap<String, byte[]>();
		}
		netWorkParam.mFileData.put(k, v);
	}
	
	/**
	 * 添加需要post的数据
	 * @param data NameValuePair格式数据
	 */
	public void addPostData(BasicNameValuePair data){
		if(data == null || data.getName() == null){
			return;
		}
		if(netWorkParam.mPostData == null){
			netWorkParam.mPostData = new ArrayList<BasicNameValuePair>();
		}
		int index = getAddPostIndex(netWorkParam.mPostData, data.getName());
		int size = netWorkParam.mPostData.size();
		if(index >=0 && index < size){
			BasicNameValuePair tmp = netWorkParam.mPostData.get(index);
			if(data.getName().equals(tmp.getName())){
				netWorkParam.mPostData.set(index, data);
			}else{
				netWorkParam.mPostData.add(index, data);
			}
		}else if(index == size){
			netWorkParam.mPostData.add(index, data);
		}
	}
	
	private int getAddPostIndex(ArrayList<BasicNameValuePair> data, String key){
		int index = 0;
		if(data == null || key == null){
			return -1;
		}
		int size = data.size();
		int i = 0;
		for(i = 0; i < size; i++){
			index = i;
			int compare_ret = key.compareTo(data.get(i).getName());
			if(compare_ret < 0){
				break;
			}else if(compare_ret == 0){
				return -1;
			}
		}
		if(i >= size){
			index = size;
		}
		return index;
	}

	public void setIsBDImage(boolean isBDImage) {
		netWorkParam.mIsBDImage = isBDImage;
	}

	public boolean getIsBDImage() {
		return netWorkParam.mIsBDImage;
	}
	
	public void setIsBaiduServer(boolean isBaidu)
	{
		netWorkParam.mIsBaiduServer=isBaidu;
	}
	
	public static NetworkStateInfo getStatusInfo(Context context)
	{
		boolean netSataus = false;    
		NetworkInfo networkinfo = null;
		NetworkStateInfo ret = NetworkStateInfo.UNAVAIL;
		try{
			ConnectivityManager cwjManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);    
			networkinfo = cwjManager.getActiveNetworkInfo();
			netSataus = networkinfo.isAvailable(); 

			if (!netSataus) {    
				ret = NetworkStateInfo.UNAVAIL;
				MeetLog.i("NetWorkCore", "NetworkStateInfo", "UNAVAIL");
			}else{
				if(networkinfo.getType() == ConnectivityManager.TYPE_WIFI){
					MeetLog.i("NetWorkCore", "NetworkStateInfo", "WIFI");
					ret = NetworkStateInfo.WIFI;
				}else{
					TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
					int subType = tm.getNetworkType();
					switch(subType){  
			            case TelephonyManager.NETWORK_TYPE_1xRTT:  // ~ 50-100 kbps  
			            case TelephonyManager.NETWORK_TYPE_CDMA: // ~ 14-64 kbps 
			            case TelephonyManager.NETWORK_TYPE_EDGE: // ~ 50-100 kbps 
			            case TelephonyManager.NETWORK_TYPE_GPRS:  // ~ 100 kbps  
			            case /*Connectivity.NETWORK_TYPE_IDEN*/11:  // ~25 kbps   
			            case TelephonyManager.NETWORK_TYPE_UNKNOWN:  
			            	MeetLog.i("NetWorkCore", "NetworkStateInfo", "TwoG");
			                return NetworkStateInfo.TwoG; 
			            case TelephonyManager.NETWORK_TYPE_EVDO_0:  // ~ 400-1000 kbps  
			            case TelephonyManager.NETWORK_TYPE_EVDO_A:  // ~ 600-1400 kbps 
			            case TelephonyManager.NETWORK_TYPE_UMTS:  // ~ 400-7000 kbps 
			            case /*Connectivity.NETWORK_TYPE_EHRPD*/14:  // ~ 1-2 Mbps  
			            case /*Connectivity.NETWORK_TYPE_EVDO_B*/12:  // ~ 5 Mbps  
			            case /*Connectivity.NETWORK_TYPE_HSPAP*/15:  // ~ 10-20 Mbps  
			            case /*Connectivity.NETWORK_TYPE_LTE*/13:  // ~ 10+ Mbps  
			            case /*TelephonyManager.NETWORK_TYPE_HSDPA*/8:   //  ~ 4-8 Mbps
			            case /*TelephonyManager.NETWORK_TYPE_HSUPA*/9:  // ~ 1.4-5.8Mbps  
			            case /*TelephonyManager.NETWORK_TYPE_HSPA*/10:  //  ~20Mbps
			            	MeetLog.i("NetWorkCore", "NetworkStateInfo", "ThreeG");
			                return NetworkStateInfo.ThreeG;     
			            default:  
			            	MeetLog.i("NetWorkCore", "NetworkStateInfo-default", "TwoG");
			                return NetworkStateInfo.TwoG;
			        }
				}
			}
		}catch(Exception ex){}
		return ret;
	}
	
	private void checkDNS(URL url) {
		try {
			InetAddress address = InetAddress.getByName(url.getHost());
			address.getHostAddress();
		} catch (Exception e) {
			MeetLog.e(getClass().getName(), "checkDNS", e.toString());
		}
	}

}
