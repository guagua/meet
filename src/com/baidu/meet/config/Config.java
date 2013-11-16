package com.baidu.meet.config;

import com.baidu.meet.MeetApplication;

import android.graphics.Bitmap;

/**
 * 
 * 通用配置文件
 * @author zhangdongning
 *
 */
public class Config {
	public static final String SERVER_ADDRESS = 	"http://lovefacedev.duapp.com/";
	public static final String REGISTER = 			"?r=user/register";
	public static final String UPLOAD_IMAGE = 		"?r=uploda/image";
	public static final String GET_PIC = 			"?r=picture/getpic";
	public static final String UPDATE_RELATION = 	"?r=relation/updateRelation";
	public static final String GET_LOVELIST = 		"?r=relation/getlovelist";
	public static final String GET_MSG = 			"?r=msg/getmsg";
	public static final String SEND_MSG = 			"?r=msg/sendmsg";
	
	public static final String TMPDIRNAME = "meetyou";
	private static int BIG_IMAGE_SIZE = 1024;
	
	public static int BitmapQuality = 80;
	public static final Bitmap.Config BitmapConfig = Bitmap.Config.RGB_565;
	
	public static final String LOG_ERROR_FILE = "log_error.log";
	public static final long FATAL_ERROR_FILE_MAX_SIZE = 200 * 1024L;
	
	public static final int MAX_SDRAM_PHOTO_NUM = 50;
	public static final int MAX_PRELOAD_PHOTO_NUM = 30;
	public static final int MAX_SDRAM_PIC_NUM = 13;
	public static final int MAX_PRELOAD_PIC_NUM = 13;
	public static final int MAX_ASYNC_IMAGE_LOADER_NUM = 5;
	
	private static int BIG_IMAGE_MAX_USED_MEMORY = 1024 * 1024;
	public static final String TMP_PIC_DIR_NAME = "image";
	
	public static final String PHONE_DATEBASE_NAME = "meet_you.db";
	public static final String TMP_DATABASE_NAME = "meet_you_temp.db";
	public static final int DATABASE_VERSION = 6;
	
	public static final int PIC_ALBUM_IMAGE = 1;
	public static final int PIC_PHOTO = 2;
	
	public static final String CONFIG_FILE_NAME = "meet_setting";
	
	public static final int HEAD_IMG_SIZE = 110;
	public static final String IMAGE_RESIZED_FILE = "tieba_resized_image";
	public static final String PERSON_HEAD_FILE = "tieba_head_image";
	public static final String GROUP_HEAD_FILE = "tieba_group_image";
	public static final int POST_IMAGE_QUALITY = 80;
	public static final int POST_IMAGE_DISPLAY = 100;
	public static final int POST_IMAGE_SMALL = 600;
	public static final int POST_IMAGE_MIDDLE = 750;
	public static final int POST_IMAGE_BIG = 900;
	
	public static final String BROADCAST_IMAGE_RESIZED = "com.baidu.tieba.broadcast.image.resized";
	
	public static final int CHUNK_UPLOAD_SIZE = 1024 * 10;
	
	public static final String IMAGE_RESIZED_FILE_DISPLAY = "tieba_resized_image_display";
	
	public static int getBigImageSize() {
		return BIG_IMAGE_SIZE;
	}

	public static void setBigImageSize(int bigImageSize) {
		BIG_IMAGE_SIZE = bigImageSize;
	}
	
	static public boolean getDebugSwitch() {
		return MeetApplication.getApplication().isDebugMode();
	}
	
	public static int getBigImageMaxUsedMemory(){
		return BIG_IMAGE_MAX_USED_MEMORY;
	}
	
	public static void setBigImageMaxUsedMemory(int bigImageMaxUsedMemory){
		BIG_IMAGE_MAX_USED_MEMORY = bigImageMaxUsedMemory;
	}
}
