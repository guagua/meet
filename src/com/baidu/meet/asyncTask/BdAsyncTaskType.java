package com.baidu.meet.asyncTask;

public enum BdAsyncTaskType {
	SERIAL, // 串行
	TWO_PARALLEL, // 两个任务并行
	THREE_PARALLEL, // 三个任务并行
	FOUR_PARALLEL, // 四个任务并行
	MAX_PARALLEL;// 多个任务并行
}
