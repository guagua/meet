package com.baidu.meet.model;

import com.baidu.meet.asyncTask.BdAsyncTask;
import com.baidu.meet.asyncTask.BdAsyncTaskPriority;
import com.baidu.meet.config.Config;
import com.baidu.meet.data.*;
import com.baidu.meet.log.MeetLog;
import com.baidu.meet.network.NetWork;

public class RegisterModel extends BaseModel {
	private static final String TAG = "RegisterModel";
	private RegisterAsyncTask mTask = null;
	
	public void register() {
		if (mTask == null) {
			mTask = new RegisterAsyncTask();
			mTask.setPriority(BdAsyncTaskPriority.MIDDLE);
			mTask.execute();
		}
	}
	
	public void cancelTask() {

		if (mTask != null) {
			mTask.cancel();
			mTask = null;
		}
	}

	private class RegisterAsyncTask extends
			BdAsyncTask<Object, Integer, RegisterData> {
		private volatile NetWork mNetwork = null;

		@Override
		protected RegisterData doInBackground(Object... params) {
			try {

				mNetwork = new NetWork(Config.SERVER_ADDRESS
						+ Config.REGISTER);

				mNetwork.addPostData("pic", "test.jpg");
				mNetwork.addPostData("sex", "0");
				mNetwork.addPostData("quxiang", "1");
				mNetwork.addPostData("imei", "12345");
				mNetwork.addPostData("phone", "15867118666");

				String data = null;
				data = mNetwork.postNetData();

				MeetLog.d(TAG, "doInBackground", "data:" + data);

				if (mNetwork.isRequestSuccess() && data != null) {
					RegisterData registerData = new RegisterData();
//					registerData.parserJson(data);

					return registerData;
				}

			} catch (Exception ex) {
				MeetLog.e(this.getClass().getName(), "doInBackground",
						ex.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(RegisterData data) {
			mTask = null;

			if (data == null) {
				if (mNetwork != null) {
					mErrorCode = mNetwork.getErrorCode();
					mErrorString = mNetwork.getErrorString();
					MeetLog.d(TAG, "onPostExecute", mErrorString);
				}
			}

			if (mLoadDataCallBack != null) {
				mLoadDataCallBack.callback(data);
			}
		}
		
		public void cancel() {
			super.cancel(true);
			if (mNetwork != null) {
				mNetwork.cancelNetConnect();
				mNetwork = null;
			}

			mTask = null;
			mLoadDataCallBack.callback(null);
		}
	}

	@Override
	protected boolean LoadData() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cancelLoadData() {
		// TODO Auto-generated method stub
		return false;
	}

}
