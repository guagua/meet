package com.baidu.meet.data;

import java.io.Serializable;

import org.json.JSONObject;

import com.baidu.meet.log.MeetLog;

public class ErrorData implements Serializable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2582050549890612990L;
	public int error_code;
	public String error_msg;
	public String error_data;
	
	public ErrorData(){
		error_code = -1;
		error_msg = null;
		error_data = null;
	}
	
	/**
	 * 获得错误码
	 * @return	错误码
	 */
	public int getError_code() {
		return error_code;
	}
	
	public void setError_code(int error_code) {
		this.error_code = error_code;
	}
	
	/**
	 * 获得错误信息
	 * @return	错误信息
	 */
	public String getError_msg() {
		return error_msg;
	}
	
	public void setError_msg(String error_msg) {
		this.error_msg = error_msg;
	}
	
	/**
	 * 获得错误数据
	 * @return	错误数据
	 */
	public String getError_data() {
		return error_data;
	}
	
	public void setError_data(String error_data) {
		this.error_data = error_data;
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
			MeetLog.e(this.getClass().getName(), "parserJson", ex.getMessage());
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
			error_code = json.optInt("error_code", 0);
			error_msg = json.optString("error_msg");
			error_data = json.optString("error_data");
		}catch(Exception ex){
			MeetLog.e(this.getClass().getName(), "parserJson", ex.getMessage());
		}
	}
	
	/**
	 * 输出解析后的数据
	 */
	public void logPrint(){
		MeetLog.v(this.getClass().getName(), "logPrint", "error_code = " + String.valueOf(error_code));
		MeetLog.v(this.getClass().getName(), "logPrint", "error_msg = " + error_msg);
		MeetLog.v(this.getClass().getName(), "logPrint", "error_data = " + error_data);
	}
}
