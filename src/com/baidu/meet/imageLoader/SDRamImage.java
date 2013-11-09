package com.baidu.meet.imageLoader;

import java.util.HashMap;
import java.util.Map.Entry;

import android.graphics.Bitmap;

import com.baidu.meet.config.Config;
import com.baidu.meet.log.MeetLog;

public class SDRamImage {
	private volatile int weight;
	private volatile HashMap<String ,Image> photo;
	private volatile HashMap<String ,Image> pic;
	private volatile int pic_mem;
	
	public SDRamImage(){
		photo = new HashMap<String ,Image>();
		pic = new HashMap<String ,Image>();
		weight = 0;
		pic_mem = 0;
	}

	public void addPhoto(String name, Bitmap bitmap){
		synchronized(this){
			try{
				weight++;
				if(photo.size() >= Config.MAX_SDRAM_PHOTO_NUM){
					deletePhoto();
				}
				Image image = new Image();
				image.image = bitmap;
				image.weight = weight;
				photo.put(name, image);
			}catch(Exception ex){
				MeetLog.e(this.getClass().getName(), "addPhoto", ex.getMessage());
			}
		}
	}
	
	public void addPic(String name,Bitmap bitmap,boolean isGif)
	{
		synchronized(this){
			try{
				if(bitmap == null){
					return;
				}
				weight++;
				int pic_size = bitmap.getWidth() * bitmap.getHeight() * 2;
				if(pic_mem + pic_size > Config.getBigImageMaxUsedMemory()){
					deletePic(pic_mem + pic_size - Config.getBigImageMaxUsedMemory());
				}
				Image image = new Image();
				image.image = bitmap;
				image.weight = weight;
				image.isGif=isGif;
				pic.put(name, image);
				pic_mem += pic_size;
			}catch(Exception ex){
				MeetLog.e(this.getClass().getName(), "addPic", ex.getMessage());
			}
		}
	}
	
	public void addPic(String name, Bitmap bitmap){
		synchronized(this){
			try{
				if(bitmap == null){
					return;
				}
				weight++;
				int pic_size = bitmap.getWidth() * bitmap.getHeight() * 2;
				if(pic_mem + pic_size > Config.getBigImageMaxUsedMemory()){
					deletePic(pic_mem + pic_size - Config.getBigImageMaxUsedMemory());
				}
				Image image = new Image();
				image.image = bitmap;
				image.weight = weight;
				pic_mem += pic_size;
				pic.put(name, image);
			}catch(Exception ex){
				MeetLog.e(this.getClass().getName(), "addPic", ex.getMessage());
			}
		}
	}
	
	public void deletePhoto(){
		synchronized(this){
			String key = null;
			int tmp = 0x7FFFFFF;
			for(Entry<String, Image> entry:photo.entrySet()){
				if(entry.getValue().weight < tmp){
					tmp = entry.getValue().weight;
					key = entry.getKey();
				}
			}
			if(key != null){
				photo.remove(key);
			}else{
				photo.clear();
			}
		}
	}
	
	public void deletePhoto(String key){
		synchronized(this){
			photo.remove(key);
		}
	}
	
	public void deletePic(int size){
		synchronized(this){
			if(pic_mem + size <= Config.getBigImageMaxUsedMemory()){
				return;
			}
			while(size > 0){
				int image_size = 0;
				String key = null;
				int tmp = 0x7FFFFFF;
				for(Entry<String, Image> entry:pic.entrySet()){
					if(entry.getValue().weight < tmp){
						tmp = entry.getValue().weight;
						key = entry.getKey();
					}
				}
				if(key != null){
					Image image = pic.remove(key);
					if(pic != null && image.image != null){
						image_size = image.image.getWidth() * image.image.getHeight() * 2;
						pic_mem -= image_size;
						size -= image_size;
					}
				}else{
					pic.clear();
					pic_mem = 0;
					size = 0;
				}
				size -= image_size;
			}
		}
	}
	
	public Bitmap getPhoto(String name){
		synchronized(this){
			Bitmap bitmap = null;
			Image image = photo.get(name);
			if(image != null){
				weight++;
				bitmap = image.image;
				image.weight = weight;
			}
			return bitmap;
		}
	}

	public Bitmap getPic(String name){
		synchronized(this){
			Bitmap bitmap = null;
			Image image = pic.get(name);
			if(image != null){
				weight++;
				bitmap = image.image;
				image.weight = weight;
			}
			return bitmap;
		}
	}
	
	public boolean isGif(String name)
	{
		synchronized(this){
			boolean isGif=false;
			Image image = pic.get(name);
			if(image != null){
				isGif=image.isGif;
			}
			return isGif;
		}
	}
	
	public void clearPicAndPhoto(){
		synchronized(this){
			photo.clear();
			pic.clear();
			pic_mem = 0;
		}
	}
	
	public void LogCount(){
		int photo_size = photo.size();
		int i = 0;
		MeetLog.log_e(0,this.getClass().getName(), "logPrint", "photo.size = " + String.valueOf(photo_size));
		for(Entry<String, Image> entry:photo.entrySet()){
			StringBuffer log = new StringBuffer(50);
			log.append("photo[");
			log.append(i);
			log.append("].width = ");
			log.append(entry.getValue().image.getWidth());
			log.append("	photo[");
			log.append(i);
			log.append("].height = ");
			log.append(entry.getValue().image.getHeight());
			MeetLog.log_e(0,this.getClass().getName(), "logPrint", log.toString());
			i++;
		}
		int pic_size = pic.size();
		i = 0;
		MeetLog.log_e(0,this.getClass().getName(), "logPrint", "pic.size = " + String.valueOf(pic_size));
		for(Entry<String, Image> entry:pic.entrySet()){
			StringBuffer log = new StringBuffer(50);
			log.append("pic[");
			log.append(i);
			log.append("].width = ");
			log.append(entry.getValue().image.getWidth());
			log.append("	pic[");
			log.append(i);
			log.append("].height = ");
			log.append(entry.getValue().image.getHeight());
			MeetLog.log_e(0,this.getClass().getName(), "logPrint", log.toString());
			i++;
		}
	}
	
	private class Image
	{
		Bitmap image;
		Integer weight;
		boolean isGif;
	}
}
