package com.baidu.meet.imageLoader;

import com.baidu.meet.config.Config;
import com.baidu.meet.log.MeetLog;
import com.baidu.meet.util.FileHelper;

import android.database.sqlite.SQLiteDatabase;

public class DatabaseSdHelper {
	private int mVersion = 1;
	private boolean mHaveCreate = false;
	private String mName;
	private String mPath;
	private onCreateCallback mOnCreateCallback = null;
	
	public DatabaseSdHelper(){
		mVersion = Config.DATABASE_VERSION;
		mName = Config.TMP_DATABASE_NAME;
		mPath = FileHelper.EXTERNAL_STORAGE_DIRECTORY + "/" + Config.TMPDIRNAME + "/" + mName;
	}
	
	private void ExecSQL(SQLiteDatabase db, String sql){
		try{
			db.execSQL(sql);
		}catch(Exception ex){
			MeetLog.log_e(MeetLog.ERROR_TYPE_DB, this.getClass().getName(), "ExecSQL", sql);
		};
	}
	
	public SQLiteDatabase getWritableDatabase(){
		SQLiteDatabase database = null;
		if(FileHelper.CheckTempDir() == true){
			mHaveCreate = FileHelper.CheckFile(mName);
			database = SQLiteDatabase.openOrCreateDatabase(mPath, null);
			if(database != null){
				if(mHaveCreate == false){
					onCreateDatabase(database);
					database.setVersion(mVersion);
				}else{
					int version = database.getVersion();
					if(version != mVersion){
						onUpdateDatabase(database, version, mVersion);
						database.setVersion(mVersion);
					}
				}
			}
		}
		return database;
	}
	
	private void onCreateDatabase(SQLiteDatabase database){
		if(database != null){
			ExecSQL(database, "CREATE TABLE if not exists pb_photo(key varchar(50) Primary Key,image blob,date Integer)");
			ExecSQL(database, "CREATE INDEX if not exists pb_photo_index ON pb_photo(date)");
			ExecSQL(database, "CREATE TABLE if not exists friend_photo(key varchar(50) Primary Key,image blob,date Integer)");
			ExecSQL(database, "CREATE INDEX if not exists friend_photo_index ON friend_photo(date)");
		}
		ExeCallback();
	}
	
	private void onUpdateDatabase(SQLiteDatabase database, int old_version, int new_version){
		ExeCallback();
	}
	
	private void ExeCallback(){
		if(mOnCreateCallback != null){
			try{
				mOnCreateCallback.onCreate();
			}catch(Exception ex){
				MeetLog.e(this.getClass().getName(), "onCreateDatabase", ex.getMessage());
			}
		}
	}
	public void setOnCreateCallback(onCreateCallback callback){
		mOnCreateCallback = callback;
	}
	
	public interface onCreateCallback {  
		public void onCreate();  
	}
	
}
