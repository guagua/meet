package com.baidu.meet.dbcache.safe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;


public class SafeService {

	public static boolean startActivity(Context context, Intent intent){
		try{
			context.startActivity(intent);
			return true;
		}catch(Exception e){
			return false;
		}
	}
	
	public static boolean startActivityForResult(Activity activity, Intent intent, int requestCode){
		try{
			activity.startActivityForResult(intent, requestCode);
			return true;
		}catch(Exception e){
			return false;
		}
	}
	
	public static boolean startService(Context context, Intent intent){
		try{
			context.startService(intent);
			return true;
		}catch(Exception e){
			return false;
		}
	}
	
	
}
