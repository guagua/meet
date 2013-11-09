package com.baidu.meet.network;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 网络多套统计逻辑之一
 */
public class NetWorkState {

	public static class StatisticsData {
		public int mMethod;
		public long mTime;
		public int mSize;
		public int mTimesNum;
		public int mMode;
	}

	// 统计用
	private static ArrayList<StatisticsData> mStatisticsDatas = new ArrayList<StatisticsData>();
	public static AtomicInteger mErrorNums = new AtomicInteger(0);

	public static int getErrorNumsAndSet(int num) {
		return mErrorNums.getAndSet(num);
	}

	public static int addErrorNumsAndGet(int num) {
		return mErrorNums.addAndGet(num);
	}
	
	public static synchronized void addStatisticsData(StatisticsData data){
		if(data == null){
			return;
		}
		int num = mStatisticsDatas.size();
		if(num > 20){
			return;
		}
		mStatisticsDatas.add(data);
	}
	
	public static synchronized StatisticsData delStatisticsData(){
		int num = mStatisticsDatas.size();
		if(num > 0){
			return mStatisticsDatas.remove(num - 1);
		}else{
			return null;
		}
	}
}
