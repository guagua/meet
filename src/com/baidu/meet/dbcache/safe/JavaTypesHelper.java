/**
 * 
 */
package com.baidu.meet.dbcache.safe;

/**
 * 
 * 安全的类型转换
 * 
 * @author liukaixuan
 */
public abstract class JavaTypesHelper {

	public static int toInt(String val, int defaultValue){
		if(val == null){
			return defaultValue ;
		}
		
		try{
			return Integer.parseInt(val) ;
		}catch(Exception e){
			
		}
		
		return defaultValue ;
	}
	
	public static long toLong(String val, long defaultValue){
		if(val == null){
			return defaultValue ;
		}
		
		try{
			return Long.parseLong(val) ;
		}catch(Exception e){
			
		}
		
		return defaultValue ;
	}

	public static float toFloat(String val, float defaultValue){
		if(val == null){
			return defaultValue ;
		}
		
		try{
			return Float.parseFloat(val) ;
		}catch(Exception e){
			
		}
		
		return defaultValue ;
	}

	public static double toDouble(String val, double defaultValue){
		if(val == null){
			return defaultValue ;
		}
		
		try{
			return Double.parseDouble(val) ;
		}catch(Exception e){
			
		}
		
		return defaultValue ;
	}
	
}
