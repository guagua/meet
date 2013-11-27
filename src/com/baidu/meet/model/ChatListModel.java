package com.baidu.meet.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.baidu.meet.asyncTask.BdAsyncTaskPriority;
import com.baidu.meet.talk.RecentChatFriendData;

/**
 * @author zhangdongning 聊天列表页model，管理最近聊天人资料
 */

public class ChatListModel extends BaseModel {

	/** 群消息 */
	public static final int TYPE_GROUP_MSG = 1;
	/** 验证消息 */
	public static final int TYPE_GROUP_VALIDATE = 2;
	/** 群动态 */
	public static final int TYPE_GROUP_UPDATES = 3;
	// 最近聊天列表数据
	private List<RecentChatFriendData> mRecentChatData = new LinkedList<RecentChatFriendData>();
	private List<RecentChatFriendData> mRecentGroupChatData = new LinkedList<RecentChatFriendData>();
	private List<RecentChatFriendData> mRecentPrivateChatData = new LinkedList<RecentChatFriendData>();
	// 我的id
	private String mUserId = null;

	// 朋友的id
	private String mFriendId = null;

	// 当前页码，默认从1开始
	private String mPn = "1";

	// 默认获取条数
	private String mRn = "20";

	// server返回的服务器错误码
	private int errNo = -1;

	// server返回的错误提示
	private String errMsg;

	// 是否还有更多最近聊天记录
	private boolean hasMore = false;

	// 获取最近聊天记录
	private RecentChatAsyncTask mGetChatTask = null;

	// 删除某人聊天记录
	private DeleteChatAsyncTask mDelChatTask = null;

	@Override
	protected boolean LoadData() {
		return false;
	}

	@Override
	public boolean cancelLoadData() {
		return false;
	}

	public void cancelTask() {

		if (mGetChatTask != null) {
			mGetChatTask.cancel();
			mGetChatTask = null;
		}
	}

	public Boolean isNoData() {
		if (null != mRecentChatData && mRecentChatData.size() > 0) {
			return false;
		}
		if (null != mRecentGroupChatData && mRecentGroupChatData.size() > 0) {
			return false;
		}

		if (null != mRecentPrivateChatData && mRecentPrivateChatData.size() > 0) {
			return false;
		}

		return true;
	}

	public boolean startTask(String userId, String pn) {

		if ((userId == null || userId.length() <= 0)
				|| (pn == null || pn.length() <= 0)) {

			return false;
		}

		mUserId = userId;
		mPn = pn;

		if (mGetChatTask == null) {
			mGetChatTask = new RecentChatAsyncTask();
			mGetChatTask.setPriority(BdAsyncTaskPriority.MIDDLE);
			mGetChatTask.execute();
		}

		return true;
	}

	public void startDeleteTask(String friendId) {
		if (friendId == null || friendId.length() <= 0) {
			return;
		}
		mFriendId = friendId;

		if (mDelChatTask == null) {
			mDelChatTask = new DeleteChatAsyncTask();
			mDelChatTask.setPriority(BdAsyncTaskPriority.MIDDLE);
			mDelChatTask.execute();
		}
	}

//	public void deleteChat(final RecentChatFriendData data,
//			ISingleRunnableCallback<Void> callback) {
//		if (null == data) {
//			return;
//		}
//		if (null == data.getOwnerName()) {
//			// 删除私聊
//			startDeleteTask(data.getFriendId());
//		} else if (data.getOwnerName().equals(
//				String.valueOf(ChatListModel.TYPE_GROUP_VALIDATE))) {
//			SharedPrefHelper.getInstance().putBoolean(
//					SharedPrefConfig.IS_SHOW_VALIDATE, false);
//			ChatNotifyManager.getInstance().refreshIsShowValidate();
//			delGroupMsgData(TiebaApplication.getCurrentAccount(),
//					String.valueOf(ChatListModel.TYPE_GROUP_VALIDATE), callback);
//		} else if (data.getOwnerName().equals(
//				String.valueOf(ChatListModel.TYPE_GROUP_UPDATES))) {
//			SharedPrefHelper.getInstance().putBoolean(
//					SharedPrefConfig.IS_SHOW_UPDATES, false);
//			ChatNotifyManager.getInstance().refreshUpdates();
//			delGroupMsgData(TiebaApplication.getCurrentAccount(),
//					String.valueOf(ChatListModel.TYPE_GROUP_UPDATES), callback);
//		} else if (data.getOwnerName().equals(
//				String.valueOf(ChatListModel.TYPE_GROUP_MSG))) {
//			// 删除群聊
//			delGroupMsgData(TiebaApplication.getCurrentAccount(),
//					data.getFriendId(), callback);
//		} else {
//			// 删除私聊
//			startDeleteTask(data.getFriendId());
//		}
//
//	}

//	private void delGroupMsgData(final String uid, final String fid,
//			final ISingleRunnableCallback<Void> callback) {
//		ChatStorageService.getService().clearRecentMessage(uid, fid);
//		ImMessageCenterMemoryCache
//				.getInstance()
//				.getCache(
//						new ISingleRunnableCallback<ConcurrentHashMap<String, ImMessageCenterPojo>>() {
//
//							@Override
//							public void onReturnDataInUI(
//									ConcurrentHashMap<String, ImMessageCenterPojo> result) {
//								ImMessageCenterPojo pojo = result.get(fid);
//								if (pojo != null) {
//									pojo.setIs_hidden(ImMessageCenterPojo.HIDDEN);
//								}
//
//								TiebaIMSingleExecutor.execute(
//										new SingleRunnable<Void>() {
//
//											@Override
//											public Void doInBackground() {
//												ImMessageCenterDao
//														.getInstance()
//														.updateGroupMsgVisiblity(
//																fid, true);
//												return null;
//											}
//										}, new ISingleRunnableCallback<Void>() {
//
//											@Override
//											public void onReturnDataInUI(
//													Void result) {
//
//												RecentChatFriendData rcd = null;
//												for (RecentChatFriendData r : mRecentGroupChatData) {
//													if (fid.equals(r
//															.getFriendId())) {
//														rcd = r;
//														break;
//													}
//												}
//
//												if (null != rcd) {
//													mRecentGroupChatData
//															.remove(rcd);
//												}
//												ChatNotifyManager
//														.getInstance()
//														.delRecentChatFriendData(
//																fid);
//												ChatNotifyManager
//														.getInstance()
//														.refreshGroupMsgList(
//																false, callback);
//											}
//										});
//							}
//						});
//	}

	public void setRecentChatData(List<RecentChatFriendData> mRecentChatData) {
		this.mRecentChatData = mRecentChatData;
	}

	public List<RecentChatFriendData> getRecentChatData() {
		return mRecentChatData;
	}

	public void clearData() {
		mRecentChatData.clear();
		mRecentGroupChatData.clear();
		mRecentPrivateChatData.clear();
	}

	/**
	 * 解析返回数据
	 * 
	 * @param data
	 *            server返回的数据
	 */
	public void parserJson(String data) {
		try {
			JSONObject json = new JSONObject(data);
			parserJson(json);
		} catch (Exception ex) {
		}
	}
	
	/**
	 * 解析并保存
	 * 
	 * @param data
	 *            server返回的数据解析的JSONObject
	 */
	public void parserJson(JSONObject json) {
		try {
			if (json != null) {
				JSONObject error = json.optJSONObject("error");
				if (error != null) {
					setErrNo(error.optInt("errno"));
                    setErrMsg(error.optString("usermsg"));
				}
				int num = json.optInt("has_more");
				hasMore = (num == 0) ? false : true;
				JSONArray list = null;
				list = json.optJSONArray("record");
				long localTime = System.currentTimeMillis();
				if (list != null) {
					mRecentPrivateChatData = new ArrayList<RecentChatFriendData>();
					for (int i = 0; i < list.length(); i++) {
						JSONObject item = list.optJSONObject(i);
						RecentChatFriendData temp = new RecentChatFriendData();
						temp.setOwnerId(mUserId);
						temp.setFriendId(item.optString("user_id"));
						temp.setFriendName(item.optString("user_name"));
						temp.setStatus(ChatMessageData.STATUS_RECEIVED);
						temp.setServerTime(item.optLong("time") * 1000);
						temp.setUnReadCount(item.optInt("unread_count"));
						temp.setFriendPortrait(item.optString("portrait"));
						temp.setLocalTime(localTime);
						// 考虑到以后摘要信息会增加，目前把整个内容存储，使用时再解析，可以防止后期不动底层
						String msg = item.optString("abstract");
						if (msg != null && msg.length() >= 1) {
							temp.setMsgContent(msg);
						}
						mRecentPrivateChatData.add(temp);
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	// 从本地服务获取缓存数据
	public List<RecentChatFriendData> getChatStorageData(String userId) {
		mRecentChatData = ChatStorageService.getService()
				.listRecentChatFriends(userId);
		for (RecentChatFriendData data : mRecentChatData) {
			data.setUnReadCount(0);
		}
		return mRecentChatData;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}

	public boolean hasMore() {
		return hasMore;
	}

	public void setErrNo(int errNo) {
		this.errNo = errNo;
	}

	public int getErrNo() {
		return errNo;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public List<RecentChatFriendData> getmRecentGroupChatData() {
		return mRecentGroupChatData;
	}

	public void setmRecentGroupChatData(
			List<RecentChatFriendData> mRecentGroupChatData) {
		this.mRecentGroupChatData = mRecentGroupChatData;
	}

	private class RecentChatAsyncTask extends
			BdAsyncTask<Object, Integer, Boolean> {
		private volatile NetWork mNetwork = null;

		@Override
		protected Boolean doInBackground(Object... params) {
			return requestPrivateChat();
		}

		private Boolean requestPrivateChat() {
			try {

				mNetwork = new NetWork(Config.SERVER_ADDRESS
						+ Config.RECENT_CHAT_LIST);

				mNetwork.setIsNeedTbs(true);
				mNetwork.addPostData("user_id", mUserId);
				mNetwork.addPostData("pn", mPn);
				mNetwork.addPostData("rn", mRn);

				String data = null;

				data = mNetwork.postNetData();
				if (mNetwork.isRequestSuccess() && data != null) {
					parserJson(data);
					// 返回数据成功时才缓存 并且只存第一页数据
					if ((getErrNo() == 0 && mPn.equals("1"))
							&& mRecentPrivateChatData != null) {
						processGroupMsgAndrPrivateChat();
					}
					return true;
				}

			} catch (Exception ex) {
				TiebaLog.e(this.getClass().getName(), "doInBackground",
						ex.getMessage());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean isSuccess) {
			mGetChatTask = null;
			resetData();
			if (false == isSuccess) {
				mErrorCode = mNetwork.getErrorCode();
				mErrorString = mNetwork.getErrorString();
				mLoadDataCallBack.callback(false);
				return;
			} else {
				mLoadDataCallBack.callback(true);
			}
		}

		public void cancel() {
			super.cancel(true);
			if (mNetwork != null) {
				mNetwork.cancelNetConnect();
				mNetwork = null;
			}
			mLoadDataCallBack.callback(false);
		}
	}

	public List<RecentChatFriendData> getmRecentPrivateChatData() {
		return mRecentPrivateChatData;
	}

	public void setmRecentPrivateChatData(
			List<RecentChatFriendData> mRecentPrivateChatData) {
		this.mRecentPrivateChatData = mRecentPrivateChatData;
	}

	public void processGroupMsgOnly(final ISingleRunnableCallback<Void> callback) {
		ChatNotifyManager.getInstance().refreshGroupMsgList(false,
				new ISingleRunnableCallback<Void>() {

					@Override
					public void onReturnDataInUI(Void result) {
						LinkedList<RecentChatFriendData> datas = ChatNotifyManager
								.getInstance().getRecentGroupChat();
						if (null == datas) {
							return;
						}

						mRecentGroupChatData.clear();
						mRecentGroupChatData.addAll(datas);
						resetData();
						if (null != callback) {
							callback.onReturnDataInUI(null);
						}
					}
				});
	}

	private synchronized void resetData() {
		mRecentChatData.clear();
		if (null != mRecentGroupChatData) {
			mRecentChatData.addAll(mRecentGroupChatData);
		}
		if (null != mRecentPrivateChatData) {
			mRecentChatData.addAll(mRecentPrivateChatData);
		}

		if (ChatNotifyManager.getInstance().isShowValidate()) {
			RecentChatFriendData validate = ChatNotifyManager.getInstance()
					.getValidateData();
			if (null != validate) {
				mRecentChatData.add(validate);
			} else {
				ChatNotifyManager.processValidate();
				validate = ChatNotifyManager.getInstance().getValidateData();
				if (null != validate) {
					mRecentChatData.add(validate);
				}
			}
		}
		if (ChatNotifyManager.getInstance().isShowUpdate()) {
			RecentChatFriendData updates = ChatNotifyManager.getInstance()
					.getUpdatesData();
			if (null != updates) {
				mRecentChatData.add(updates);
			} else {
				ChatNotifyManager.processGroupUpdateEvent(false);
				updates = ChatNotifyManager.getInstance().getUpdatesData();
				if (null != updates) {
					mRecentChatData.add(updates);
				}
			}
		}

		Collections.sort(mRecentChatData,
				new Comparator<RecentChatFriendData>() {

					@Override
					public int compare(RecentChatFriendData lhs,
							RecentChatFriendData rhs) {
						if (null == lhs || null == rhs) {
							return 0;
						}
						if (lhs.getServerTime() < rhs.getServerTime()) {
							return 1;
						} else if (lhs.getServerTime() > rhs.getServerTime()) {
							return -1;
						} else {
							return 0;
						}
					}
				});
	}

	private void processGroupMsgAndrPrivateChat() {
		BdLog.d(" get recent list private");

		if (null == mRecentPrivateChatData) {
			return;
		}
		long unReadCount = 0;
		for (RecentChatFriendData rcd : mRecentPrivateChatData) {
			unReadCount += rcd.getUnReadCount();
		}
		MessageCenterHelper.sharedInstance().setMsgChat(unReadCount);
		ChatStorageService.getService().saveRecentChatFriends(
				TiebaApplication.getCurrentAccount(), mRecentPrivateChatData);
		// 存服务器返回的私聊数据
		// 把合并后的数据取出
		mRecentPrivateChatData = ChatStorageService.getService()
				.listRecentChatFriends(TiebaApplication.getCurrentAccount());
	}

	private class DeleteChatAsyncTask extends
			BdAsyncTask<Object, Integer, Boolean> {
		private volatile NetWork mNetwork = null;

		@Override
		protected Boolean doInBackground(Object... params) {
			try {

				mNetwork = new NetWork(Config.SERVER_ADDRESS
						+ Config.RECENT_CHAT_DELETE);
				// sign=123456tbclient654321&user_id=800018547&com_id=50020462

				mNetwork.setIsNeedTbs(true);
				mNetwork.addPostData("user_id", mUserId);
				mNetwork.addPostData("com_id", mFriendId);

				String data = null;
				data = mNetwork.postNetData();
				if (mNetwork.isRequestSuccess() && data != null) {
					JSONObject json = new JSONObject(data);
					if (json != null) {
						JSONObject temp = json.optJSONObject("error");
						if (temp.optInt("errno") == 0) {
							// 删除成功，清空本地缓存
							ChatStorageService service = ChatStorageService
									.getService();
							ChatStorageService.getService().clearRecentMessage(
									mUserId, mFriendId);
							service.clearMessages(mUserId, mFriendId);
							return true;
						}
					}
				}

			} catch (Exception ex) {
				TiebaLog.e(this.getClass().getName(), "doInBackground",
						ex.getMessage());
				return false;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean isSuccess) {
			mDelChatTask = null;
			if (isSuccess == false) {
				mErrorCode = mNetwork.getErrorCode();
				mErrorString = mNetwork.getErrorString();
				mLoadDataCallBack.callback(false);
				return;
			}
			ChatNotifyManager.getInstance().refreshGroupMsgList(false,
					new ISingleRunnableCallback<Void>() {

						@Override
						public void onReturnDataInUI(Void result) {
							mLoadDataCallBack.callback(true);
						}
					});
		}

		public void cancel() {
			super.cancel(true);
			if (mNetwork != null) {
				mNetwork.cancelNetConnect();
				mNetwork = null;
			}

			mLoadDataCallBack.callback(false);
		}

	}
}
