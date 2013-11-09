package com.baidu.meet.model;

public abstract class BaseModel{
	
	public static final int MODE_INVALID = 0;
	
	/*
	 * @brief 操作标记
	 */
	protected int mLoadDataMode = MODE_INVALID;
	public int getLoadDataMode(){
		return mLoadDataMode;
	}
	
	/*
	 * @brief UI线程中用于更新页面的回调，从网络加载数据
	 */
	protected BaseLoadDataCallBack mLoadDataCallBack = null;
	public void setLoadDataCallBack(BaseLoadDataCallBack callBack){
		mLoadDataCallBack = callBack;
	}
	
	/*
	 * @brief 出错处理相关
	 */
	protected int mErrorCode = 0;
	protected String mErrorString = null;
	public int getErrorCode() {
		return mErrorCode;
	}
	public void setErrorCode(int errorCode) {
		this.mErrorCode = errorCode;
	}
	public String getErrorString() {
		return mErrorString;
	}
	public void setErrorString(String errorString) {
		this.mErrorString = errorString;
	}
	

//	protected abstract void callBackBefore(Object ob);
//	protected abstract Object AsyncTaskRun();
	
	protected abstract boolean LoadData();
	
	public abstract boolean cancelLoadData();
	
	
}
