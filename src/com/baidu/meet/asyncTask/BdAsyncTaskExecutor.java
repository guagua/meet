package com.baidu.meet.asyncTask;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.baidu.meet.BaseApplication;
import com.baidu.meet.log.MeetLog;

public class BdAsyncTaskExecutor implements Executor {
	private static final boolean Debug = BaseApplication.getApplication()
			.isDebugMode();
	private static final int CORE_POOL_SIZE = 5;
	private static final int MAXIMUM_POOL_SIZE = 256;
	private static final int KEEP_ALIVE = 30;
	private static final int TASK_MAX_TIME = 2 * 60 * 1000;
	private static final int TASK_MAX_TIME_ID = 1;
	private static final int TASK_RUN_NEXT_ID = 2;

	private static BdAsyncTaskExecutor sInstance = null;
	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			String log = "BdAsyncTask #"
					+ String.valueOf(mCount.getAndIncrement());
			MeetLog.i(log);
			return new Thread(r, log);
		}
	};

	private static final BlockingQueue<Runnable> sPoolWorkQueue = new SynchronousQueue<Runnable>();
	public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
			CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
			sPoolWorkQueue, sThreadFactory,
			new ThreadPoolExecutor.DiscardPolicy());

	private final LinkedList<BdAsyncTaskRunnable> mTasks = new LinkedList<BdAsyncTaskRunnable>();
	private final LinkedList<BdAsyncTaskRunnable> mActives = new LinkedList<BdAsyncTaskRunnable>();
	private final LinkedList<BdAsyncTaskRunnable> mTimeOutActives = new LinkedList<BdAsyncTaskRunnable>();

	private Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == TASK_MAX_TIME_ID) {
				if (msg.obj != null && msg.obj instanceof BdAsyncTaskRunnable) {
					activeTaskTimeOut((BdAsyncTaskRunnable) (msg.obj));
				}
			} else if (msg.what == TASK_RUN_NEXT_ID) {
				if (msg.obj != null && msg.obj instanceof BdAsyncTaskRunnable) {
					scheduleNext((BdAsyncTaskRunnable) (msg.obj));
				}
			}
		}

	};

	private BdAsyncTaskExecutor() {
	}

	public String toString() {
		return "mTasks = " + mTasks.size() + " mActives = " + mActives.size()
				+ " mTimeOutActives = " + mTimeOutActives.size();
	}

	public static BdAsyncTaskExecutor getInstance() {
		if (sInstance == null) {
			sInstance = new BdAsyncTaskExecutor();
		}
		return sInstance;
	}

	public synchronized void execute(Runnable r) {
		if (!(r instanceof BdAsyncTaskFuture)) {
			return;
		}
		BdAsyncTaskRunnable runnable = new BdAsyncTaskRunnable(
				(BdAsyncTaskFuture<?>) r) {
			public void run() {
				try {
					try {
						if (getPriority() == BdAsyncTaskPriority.HIGH) {
							android.os.Process
									.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT - 1);
						} else if (getPriority() == BdAsyncTaskPriority.MIDDLE) {
							android.os.Process
									.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT);
						} else {
							android.os.Process
									.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
						}
					} catch (Exception ex) {
						MeetLog.e(ex.getMessage());
					}
					runTask();
				} finally {
					if (isSelfExecute() == false) {
						mHandler.sendMessageDelayed(
								mHandler.obtainMessage(TASK_RUN_NEXT_ID, this),
								1);
					}
				}
			}
		};
		if (runnable.isSelfExecute() == true) {
			new Thread(runnable).start();
			return;
		}
		/**
		 * by dj for v5.1 资源装载优先级设置
		 */
		if (runnable.isImmediatelyExecut()) {
			immediatelyExecut(runnable);
			return;
		}

		int num = mTasks.size();
		int index = 0;
		for (index = 0; index < num; index++) {
			if (mTasks.get(index).getPriority() < runnable.getPriority()) {
				break;
			}
		}
		mTasks.add(index, runnable);
		scheduleNext(null);
	}

	private synchronized void activeTaskTimeOut(BdAsyncTaskRunnable task) {
		mActives.remove(task);
		mTimeOutActives.add(task);
		// 终止任务需要时间，所以提前终止线程
		if (mTimeOutActives.size() > MAXIMUM_POOL_SIZE - CORE_POOL_SIZE * 2) {
			BdAsyncTaskRunnable runnable = mTimeOutActives.poll();
			if (runnable != null) {
				runnable.cancelTask();
			}
		}
		scheduleNext(null);
	}

	/**
	 * by dj for v5.1 资源装载优先级设置 立即执行任务
	 * 
	 * @param current
	 */
	protected synchronized void immediatelyExecut(BdAsyncTaskRunnable task) {
		mActives.add(task);
		THREAD_POOL_EXECUTOR.execute(task);
		mHandler.sendMessageDelayed(
				mHandler.obtainMessage(TASK_MAX_TIME_ID, task),
				TASK_MAX_TIME / 2);
	}

	protected synchronized void scheduleNext(BdAsyncTaskRunnable current) {
		if (current != null) {
			mActives.remove(current);
			mTimeOutActives.remove(current);
			mHandler.removeMessages(TASK_MAX_TIME_ID, current);
		}

		int activeNum = mActives.size();
		if (activeNum >= CORE_POOL_SIZE) {
			if (Debug)
				MeetLog.d(BdAsyncTaskExecutor.getInstance().toString());
			return;
		}

		BdAsyncTaskRunnable item = mTasks.peek();
		if (item == null) {
			if (Debug)
				MeetLog.d(BdAsyncTaskExecutor.getInstance().toString());
			return;
		}

		if (activeNum >= CORE_POOL_SIZE - 1
				&& item.getPriority() == BdAsyncTaskPriority.LOW) {
			if (Debug)
				MeetLog.d(BdAsyncTaskExecutor.getInstance().toString());
			return;
		}
		TypeNum typeNum = new TypeNum(mActives);
		for (int i = 0; i < mTasks.size(); i++) {
			BdAsyncTaskRunnable task = mTasks.get(i);
			if (typeNum.canExecute(task)) {
				mActives.add(task);
				mTasks.remove(task);
				THREAD_POOL_EXECUTOR.execute(task);
				mHandler.sendMessageDelayed(
						mHandler.obtainMessage(TASK_MAX_TIME_ID, task),
						TASK_MAX_TIME);
				break;
			}
		}
		if (Debug)
			MeetLog.d(BdAsyncTaskExecutor.getInstance().toString());
	}

	public synchronized void removeAllTask(String tag) {
		removeAllQueueTask(tag);
		removeTask(mActives, false, tag);
		removeTask(mTimeOutActives, false, tag);
		if (Debug)
			MeetLog.d(BdAsyncTaskExecutor.getInstance().toString());
	}

	public synchronized void removeAllQueueTask(String tag) {
		removeTask(mTasks, true, tag);
		if (Debug)
			MeetLog.d(BdAsyncTaskExecutor.getInstance().toString());
	}

	private void removeTask(LinkedList<BdAsyncTaskRunnable> tasks,
			boolean remove, String tag) {
		Iterator<BdAsyncTaskRunnable> iterator = tasks.iterator();
		while (iterator.hasNext()) {
			BdAsyncTaskRunnable next = iterator.next();
			final String tmp = next.getTag();
			if (tmp != null && tmp.equals(tag)) {
				if (remove == true) {
					iterator.remove();
				}
				next.cancelTask();
			}
		}
	}

	public synchronized void removeTask(BdAsyncTask<?, ?, ?> task) {
		Iterator<BdAsyncTaskRunnable> iterator = mTasks.iterator();
		while (iterator.hasNext()) {
			BdAsyncTaskRunnable next = iterator.next();
			if (next != null && next.getTask() == task) {
				iterator.remove();
				break;
			}
		}
		if (Debug)
			MeetLog.d(BdAsyncTaskExecutor.getInstance().toString());
	}

	public synchronized BdAsyncTask<?, ?, ?> searchTask(String key) {
		BdAsyncTask<?, ?, ?> tmp = null;
		tmp = searchTask(mTasks, key);
		if (tmp == null) {
			tmp = searchTask(mActives, key);
		}
		return tmp;
	}

	// by dj for v5.1 资源装载优先级设置
	// 搜索等待中的任务
	public synchronized BdAsyncTask<?, ?, ?> searchWaitingTask(String key) {
		return searchTask(mTasks, key);
	}

	// 搜索执行中的任务
	public synchronized BdAsyncTask<?, ?, ?> searchActivTask(String key) {
		return searchTask(mActives, key);
	}

	// end

	public BdAsyncTask<?, ?, ?> searchTask(
			LinkedList<BdAsyncTaskRunnable> list, String key) {
		if (list == null) {
			return null;
		}
		Iterator<BdAsyncTaskRunnable> iterator = list.iterator();
		while (iterator.hasNext()) {
			BdAsyncTaskRunnable next = iterator.next();
			final String tmp = next.getKey();
			if (tmp != null && tmp.equals(key)) {
				return next.getTask();
			}
		}
		return null;
	}

	private static abstract class BdAsyncTaskRunnable implements Runnable {
		private BdAsyncTaskFuture<?> mBdAsyncTaskFuture = null;

		public BdAsyncTaskRunnable(BdAsyncTaskFuture<?> task) {
			if (task == null) {
				throw new NullPointerException();
			}
			mBdAsyncTaskFuture = task;
		}

		public void runTask() {
			if (mBdAsyncTaskFuture != null) {
				try {
					mBdAsyncTaskFuture.run();
				} catch (OutOfMemoryError oom) {
					BaseApplication.getApplication().onAppMemoryLow();
					System.gc();
				}
			}
		}

		public void cancelTask() {
			if (mBdAsyncTaskFuture != null) {
				mBdAsyncTaskFuture.cancelTask();
			}
		}

		public BdAsyncTask<?, ?, ?> getTask() {
			if (mBdAsyncTaskFuture != null) {
				return mBdAsyncTaskFuture.getTask();
			} else {
				return null;
			}
		}

		public int getPriority() {
			try {
				return mBdAsyncTaskFuture.getTask().getPriority();
			} catch (Exception e) {
				return BdAsyncTaskPriority.LOW;
			}
		}

		public String getTag() {
			try {
				return mBdAsyncTaskFuture.getTask().getTag();
			} catch (Exception e) {
				return null;
			}
		}

		public String getKey() {
			try {
				return mBdAsyncTaskFuture.getTask().getKey();
			} catch (Exception e) {
				return null;
			}
		}

		public BdAsyncTaskType getType() {
			try {
				return mBdAsyncTaskFuture.getTask().getType();
			} catch (Exception e) {
				return BdAsyncTaskType.MAX_PARALLEL;
			}
		}

		public boolean isSelfExecute() {
			try {
				return mBdAsyncTaskFuture.getTask().isSelfExecute();
			} catch (Exception e) {
				return false;
			}
		}

		/**
		 * by dj for v5.1 资源装载优先级设置
		 */
		public boolean isImmediatelyExecut() {
			try {
				return mBdAsyncTaskFuture.getTask().isImmediatelyExecut();
			} catch (Exception e) {
				return false;
			}

		}
	}

	private class TypeNum {
		int mTotalNum = 0;
		int mTypeSerialNum = 0;
		int mTypeTwoNum = 0;
		int mTypeThreeNum = 0;
		int mTypeFourNum = 0;

		public TypeNum(LinkedList<BdAsyncTaskRunnable> list) {
			if (list == null) {
				return;
			}
			mTotalNum = list.size();
			for (int i = 0; i < mTotalNum; i++) {
				BdAsyncTaskRunnable tmp = list.get(i);
				if (tmp.getType() == BdAsyncTaskType.SERIAL) {
					mTypeSerialNum++;
				} else if (tmp.getType() == BdAsyncTaskType.TWO_PARALLEL) {
					mTypeTwoNum++;
				} else if (tmp.getType() == BdAsyncTaskType.THREE_PARALLEL) {
					mTypeThreeNum++;
				} else if (tmp.getType() == BdAsyncTaskType.FOUR_PARALLEL) {
					mTypeFourNum++;
				}
			}
		}

		public boolean canExecute(BdAsyncTaskRunnable task) {
			if (task == null) {
				return false;
			}
			if (task.getType() == BdAsyncTaskType.SERIAL) {
				if (mTypeSerialNum < 1) {
					return true;
				}
			} else if (task.getType() == BdAsyncTaskType.TWO_PARALLEL) {
				if (mTypeTwoNum < 2) {
					return true;
				}
			} else if (task.getType() == BdAsyncTaskType.THREE_PARALLEL) {
				if (mTypeThreeNum < 3) {
					return true;
				}
			} else if (task.getType() == BdAsyncTaskType.FOUR_PARALLEL) {
				if (mTypeFourNum < 4) {
					return true;
				}
			} else {
				return true;
			}
			return false;
		}
	}

}
