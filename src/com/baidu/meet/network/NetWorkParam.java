package com.baidu.meet.network;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.message.BasicNameValuePair;

public class NetWorkParam {

	protected String mUrl;
	protected int mNetErrorCode;
	protected int mServerErrorCode;
	protected String mErrorString;
	protected ArrayList<BasicNameValuePair> mPostData;
	protected HashMap<String, byte[]> mFileData;
	protected boolean mRequestGzip;
	protected boolean mIsBDImage;
	protected boolean mIsBaiduServer = false;
	protected boolean mIsJson = true;
	protected String charSet = "UTF-8";
	
	public NetWorkParam() {
		mUrl = null;
		mNetErrorCode = 0;
		mServerErrorCode = 0;
		mErrorString = null;
		mPostData = null;
		mRequestGzip = true;
		mIsBDImage = false;
		mFileData = null;
		mIsJson = true;
	}

}
