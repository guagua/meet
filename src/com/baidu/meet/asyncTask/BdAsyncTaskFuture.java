package com.baidu.meet.asyncTask;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public abstract class BdAsyncTaskFuture<V> extends FutureTask<V> {
	private BdAsyncTask<?, ?, ?> mTask = null;
		
	public BdAsyncTask<?, ?, ?> getTask(){
		return mTask;
	}
	public BdAsyncTaskFuture(Callable<V> callable, BdAsyncTask<?, ?, ?> task) {
		super(callable);
		mTask = task;
	}
	
	public BdAsyncTaskFuture(Runnable runnable, V result, BdAsyncTask<?, ?, ?> task) {
		super(runnable, result);
		mTask = task;
	}
	
	protected abstract void cancelTask();

}
