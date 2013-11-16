package com.baidu.meet.data;

import org.json.JSONObject;

import com.baidu.meet.log.MeetLog;

public class RegisterData {
	
	private String uid;
	private String ckid;
	private int quxiang;
	private String imei;
	private String phone;	
	private int mPicType;
	
	public RegisterData(){
	}
	
	/**
	 * 解析JSON数据
	 * @param data  需要解析的字符串
	 */
	public void parserJson(String data){
		try{
			JSONObject json = new JSONObject(data);
			parserJson(json);
		}catch(Exception ex){
			MeetLog.e("RegisterData", "parserJson", "error = " + ex.getMessage());
		}
	}

	/**
	 * 解析JSON数据
	 * @param data  需要解析的JSON
	 */
	public void parserJson(JSONObject json){
		try{
			if(json == null){
				return;
			}
			
			
		}catch(Exception ex){
			MeetLog.e("RegisterData", "parserJson", "error = " + ex.getMessage());
		}
	}
	
	/**
	 * 输出解析后的数据
	 */
	public void logPrint(){
	}
	
	public void setPicType(int picType) {
		this.mPicType = picType;
	}

	public int getPicType() {
		return mPicType;
	}

}
