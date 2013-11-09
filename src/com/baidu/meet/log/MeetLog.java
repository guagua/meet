package com.baidu.meet.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import android.util.Log;

import com.baidu.meet.config.Config;
import com.baidu.meet.util.FileHelper;
import com.baidu.meet.util.StringHelper;
/**
 * TiebaLog 类，主要是控制Log信息
 * @author guagua
 *
 */
public class MeetLog {
	/**
	 * 打印Info信息
	 * @param className 类名
	 * @param method 方法名
	 * @param msg 消息
	 * @return
	 */
	static public int i(String className, String method, String msg){
		if(Config.getDebugSwitch()){
			StringBuilder fullMsg = new StringBuilder(100);
			fullMsg.append(className);
			fullMsg.append(":");
			fullMsg.append(method);
			fullMsg.append(":");
			fullMsg.append(msg);
			return Log.i("TiebaLog", fullMsg.toString());
		}else{
			return 0;
		}
	}
	
	/**
	 * log写入到sd卡
	 * @param info
	 */
	static private void logToSDcard(String info) {
		long curTime = System.currentTimeMillis();
		File file = FileHelper.CreateFileIfNotFound("log_"+StringHelper.getDateStringMouth(new Date()));
		
		FileWriter filerWriter = null;
		BufferedWriter bufWriter = null;
        try { 
        	if(file != null) {
        		filerWriter = new FileWriter(file, true);//后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖  
        		bufWriter = new BufferedWriter(filerWriter);  
        		bufWriter.write(StringHelper.getTimeString(curTime) +"		"+info);  
        		bufWriter.newLine();  
        		bufWriter.flush();
        	}
        }catch (Exception e){
        	Log.d("tieba", e.getMessage());
        }finally {
        	try{
        		if(filerWriter != null) {
        			filerWriter.close();
        		}
        		if(bufWriter != null) {
        			bufWriter.close();
        		}
        	}catch (Exception e){
        		Log.d("tieba", e.getMessage());
            }
        }
	}
	
	
	/**
	 * 打印error信息
	 * @param className 类名
	 * @param method 方法名
	 * @param msg 消息
	 * @return
	 */
	static public int e(String className, String method, String msg){
		if(Config.getDebugSwitch()){
			StringBuilder fullMsg = new StringBuilder(100);
			fullMsg.append(className);
			fullMsg.append(":");
			fullMsg.append(method);
			fullMsg.append(":");
			fullMsg.append(msg);
			logToSDcard(fullMsg.toString());//error信息存入sd卡。 按照log_x月x日的方式存储
			return Log.e("TiebaLog", fullMsg.toString());
		}else{
			return 0;
		}
	}
	

	/**
	 * 打印Warning信息
	 * @param className 类名
	 * @param method 方法名
	 * @param msg 消息
	 * @return
	 */
	static public int w(String className, String method, String msg){
		if(Config.getDebugSwitch()){
			StringBuilder fullMsg = new StringBuilder(100);
			fullMsg.append(className);
			fullMsg.append(":");
			fullMsg.append(method);
			fullMsg.append(":");
			fullMsg.append(msg);
			return Log.w("TiebaLog", fullMsg.toString());
		}else{
			return 0;
		}
	}
	
	
	/**
	 * 打印verbose信息
	 * @param className 类名
	 * @param method 方法名
	 * @param msg 消息
	 * @return
	 */
	static public int v(String className, String method, String msg){
		if(Config.getDebugSwitch()){
			StringBuilder fullMsg = new StringBuilder(100);
			fullMsg.append(className);
			fullMsg.append(":");
			fullMsg.append(method);
			fullMsg.append(":");
			fullMsg.append(msg);
			return Log.v("TiebaLog", fullMsg.toString());
		}else{
			return 0;
		}
	}
	
	
	/**
	 * 打印debug信息
	 * @param className 类名
	 * @param method 方法名
	 * @param msg 消息
	 * @return
	 */
	static public int d(String className, String method, String msg){
		if(Config.getDebugSwitch()){
			StringBuilder fullMsg = new StringBuilder(100);
			fullMsg.append(className);
			fullMsg.append(":");
			fullMsg.append(method);
			fullMsg.append(":");
			fullMsg.append(msg);
			return Log.d("TiebaLog", fullMsg.toString());
		}else{
			return 0;
		}
	}
	
	static public int printLog(int type, String msg){
		if(Config.getDebugSwitch()){
			StackTraceElement[] elements = Thread.currentThread().getStackTrace();
			if(elements.length < 5){
				return -1;
			}
			StackTraceElement element = elements[4];
			String methodName = element.getMethodName();
			String className = element.getClassName();
			if(type == 0){
				e(className, methodName, msg);
			}else if(type == 1){
				w(className, methodName, msg);
			}else if(type == 2){
				i(className, methodName, msg);
			}else if(type == 3){
				d(className, methodName, msg);
			}else{
				v(className, methodName, msg);
			}
			return 0;
		}else{
			return -1;
		}
	}
	
	static public int e(String msg){
		return printLog(0, msg);
	}
	
	static public int w(String msg){
		return printLog(1, msg);
	}
	
	static public int i(String msg){
		return printLog(2, msg);
	}
	
	static public int d(String msg){
		return printLog(3, msg);
	}
	
	static public int v(String msg){
		return printLog(4, msg);
	}
	
	static public final int ERROR_TYPE_NET = 1;
	static public final int ERROR_TYPE_IO = 2;
	static public final int ERROR_TYPE_DB = 3;
	static public final int ERROR_TYPE_LOGIC = 4;
	static public final int ERROR_MAX_NUM = 10;
	
	
	private static int error_net_num = 0;
	private static int error_io_num = 0;
	private static int error_db_num = 0;
	private static int error_logic_num = 0;
	
	static public void log_e(int type, String className, String method, String msg){
		StringBuilder fullMsg = null;
		String error = null;
		FileWriter writer = null;
		boolean should_write_file = false;
		switch(type){
		case ERROR_TYPE_NET:
			if(error_net_num < ERROR_MAX_NUM){
				should_write_file = true;
				error_net_num++;
			}
			break;
		case ERROR_TYPE_IO:
			if(error_io_num < ERROR_MAX_NUM){
				should_write_file = true;
				error_io_num++;
			}
			break;
		case ERROR_TYPE_DB:
			if(error_db_num < ERROR_MAX_NUM){
				should_write_file = true;
				error_db_num++;
			}
			break;
		case ERROR_TYPE_LOGIC:
			if(error_logic_num < ERROR_MAX_NUM){
				should_write_file = true;
				error_logic_num++;
			}
			break;
		}
		try {
			if(Config.getDebugSwitch() || should_write_file == true){
				fullMsg = new StringBuilder(100);
				fullMsg.append(new Date().getTime()/1000);
				fullMsg.append("\t");
				fullMsg.append(type);
				fullMsg.append("\t");
				fullMsg.append(method);
				if(msg != null){
					fullMsg.append(":");
					fullMsg.append(msg.replace("\n", " ").replace("\t", " "));
				}
				fullMsg.append("\t");
            	fullMsg.append(className);
            	fullMsg.append("\t");
            	fullMsg.append(0);
            	fullMsg.append("\n");
				error = fullMsg.toString();
				
				if(Config.getDebugSwitch()){
					Log.e("TiebaLog", error);
				}
				
				if(should_write_file == true){
		        	File file = FileHelper.CreateFileIfNotFound(Config.LOG_ERROR_FILE);
		        	if(error != null && file != null && file.length() < Config.FATAL_ERROR_FILE_MAX_SIZE){
		            	writer = new FileWriter(file, true);
		            	writer.append(error);
		            	writer.flush();
		        	}
				}
			}
        }catch (Exception e){
        	
        } 
        finally {
            try {
                if(writer != null){
                	writer.close();
                }
            } catch (Exception e){
            	e.printStackTrace();
            }
        }
	}
}
