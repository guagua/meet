package com.baidu.meet.talk;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.baidu.meet.R;
import com.baidu.meet.imageLoader.AsyncImageLoader;
import com.baidu.meet.log.MeetLog;
import com.baidu.meet.util.BitmapHelper;
import com.baidu.meet.util.StringHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * @author zhangdongning 最新聊天列表页的数据适配
 */
public class MeetListAdapter extends BaseAdapter {
	private static final int MAX_VALIDATE_COUNT = 30;
	private Context mContext = null;
	private boolean mHaveHeader = false;
	private boolean mHaveFooter = false;
	private boolean mIsProcessNext = false;
	private boolean mIsProcessPre = false;
	private ArrayList<ProgressBar> mProgressbars;
	private AsyncImageLoader mAsyncImageLoader = null;
	private OnClickListener mHeadListener = null;
	private List<RecentChatFriendData> mRecentChatData = null;

	public MeetListAdapter(Context context) {
		mContext = context;
		mProgressbars = new ArrayList<ProgressBar>();
		mAsyncImageLoader = new AsyncImageLoader(mContext);
	}

	public void releaseProgressBar() {
		if (mProgressbars != null) {
			for (int i = 0; i < mProgressbars.size(); i++) {
				try {
					mProgressbars.get(i).setVisibility(View.GONE);
				} catch (Exception ex) {
					MeetLog.e(this.getClass().getName(), "releaseProgressBar",
							ex.getMessage());
				}
			}
			mProgressbars.clear();
		}
	}

	public void setData(List<RecentChatFriendData> data) {
		if (mRecentChatData == null) {
			mRecentChatData = new LinkedList<RecentChatFriendData>();
		}
		mRecentChatData.clear();
		mRecentChatData.addAll(data);
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		int count = 0;
		if (mRecentChatData != null) {
			count = mRecentChatData.size();
			if (mHaveHeader == true) {
				count++;
			}
			if (mHaveFooter == true) {
				count++;
			}
			return count;
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		Object item = null;
		int index = (int) getItemId(position);
		if (index >= 0 && index < mRecentChatData.size()) {
			item = mRecentChatData.get(index);
		}
		return item;
	}

	@Override
	public long getItemId(int position) {
		// -1 : header
		// -2 : footer
		// 0 ~ n-1 : content
		int index = position;

		if (mHaveHeader == true) {
			index--;
		}
		if (mHaveFooter == true && position == getCount() - 1) {
			index = -2;
		}
		return index;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView != null) {
			holder = (ViewHolder) convertView.getTag();
		}
		if (null == holder) {
			LayoutInflater mInflater = LayoutInflater.from(mContext);
			convertView = mInflater.inflate(R.layout.meet_list_item, parent,
					false);
			holder = iniHolderView(convertView);
			convertView.setTag(holder);
			mProgressbars.add(holder.mCtlProg);
		}

		long data_index = getItemId(position);
		if (data_index == -1) { // header
			((ViewGroup) convertView).setBackgroundDrawable(null);

			holder.mContent.setVisibility(View.GONE);
			holder.mControl.setVisibility(View.VISIBLE);
			if (mIsProcessPre) {
				holder.mCtlText.setText(R.string.loading);
				holder.mCtlProg.setVisibility(View.VISIBLE);
			} else {
				holder.mCtlText.setText(R.string.frs_pre);
				holder.mCtlProg.setVisibility(View.GONE);
			}
			holder.mCtlText.setBackgroundResource(R.drawable.btn_w_square);
			holder.mCtlText.setTextColor(0xFF262626);
		} else if (data_index == -2) { // footer
			((ViewGroup) convertView).setBackgroundDrawable(null);

			holder.mContent.setVisibility(View.GONE);
			holder.mControl.setVisibility(View.VISIBLE);
			if (mIsProcessNext) {
				holder.mCtlText.setText(R.string.loading);
				holder.mCtlProg.setVisibility(View.VISIBLE);
			} else {
				holder.mCtlText.setText(R.string.frs_next);
				holder.mCtlProg.setVisibility(View.GONE);
			}
			holder.mCtlText.setBackgroundResource(R.drawable.btn_w_square);
			holder.mCtlText.setTextColor(0xFF262626);
		} else { // 正常贴子
			
			convertView.setBackgroundResource(R.drawable.list_selector_divider1);

			holder.mContent.setVisibility(View.VISIBLE);
			holder.mControl.setVisibility(View.GONE);
			RecentChatFriendData data = (RecentChatFriendData) getItem(position);
			if (data != null) {
				try {
					// 朋友名字
					holder.mNameText.setText(data.getFriendName());
					// 最新聊天内容
					refreshMsgContent(holder, data);

					// 聊天时间，注意服务器返回ms还是s
					Date tmpDate = new Date();
					tmpDate.setTime(data.getServerTime());
					String d = "";
					if (data.getServerTime() != 0) {
						d = StringHelper.getChatTimeString(tmpDate);
					}
					holder.mChatTime.setText(d);

					// 处理头像
					refreshHeadImage(holder, data);
					// 未读
					refreshUnReadCount(holder, data);

//					if (String.valueOf(
//							String.valueOf(MeetListModel.TYPE_GROUP_VALIDATE))
//							.equals(data.getOwnerName())) {
//						if (data.getUnReadCount() >= MAX_VALIDATE_COUNT) {
//							if (null != fragment) {
//								fragment.setShutDownValidateVisiblity(true);
//							}
//						} else {
//							if (null != fragment) {
//								fragment.setShutDownValidateVisiblity(false);
//							}
//						}
//					}

					refreshBellVisiblity(holder, data);

				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		}
		return convertView;
	}

	private void refreshBellVisiblity(ViewHolder holder,
			RecentChatFriendData data) {
		if (null == holder) {
			return;
		}
		if (null == data) {
			holder.iv_bell.setVisibility(View.GONE);
			return;
		}

//		if (String.valueOf(MeetListModel.TYPE_GROUP_MSG).equals(
//				data.getOwnerName())) {
//			GroupSettingItemData setting = data.getGroupSetting();
//			if (null == setting) {
//				holder.iv_bell.setVisibility(View.GONE);
//				return;
//			}
//			boolean isAccept = setting.isAcceptNotify();
//			if (!isAccept) {
//				holder.iv_bell.setVisibility(View.VISIBLE);
//				int skinType = TiebaApplication.getApp().getSkinType();
//				if (skinType == SkinManager.SKIN_TYPE_NIGHT) {
//					holder.iv_bell.setImageResource(R.drawable.icon_news_stop_1);
//				}else{
//					holder.iv_bell.setImageResource(R.drawable.icon_news_stop);
//				}
//			} else {
//				holder.iv_bell.setVisibility(View.GONE);
//			}
//		} else {
			holder.iv_bell.setVisibility(View.GONE);
//		}
	}

	private void refreshMsgContent(ViewHolder holder, RecentChatFriendData data)
			throws JSONException {
		JSONArray msgTemp = null;
//		if (String.valueOf(MeetListModel.TYPE_GROUP_VALIDATE).equals(
//				data.getOwnerName())) {
//			holder.mChatContent.setText(data.getMsgContent());
//
//		} else if (String.valueOf(MeetListModel.TYPE_GROUP_UPDATES).equals(
//				data.getOwnerName())) {
//			holder.mNameText.setText(mContext
//					.getString(R.string.updates_activity_title));
//			holder.mChatContent.setText(data.getMsgContent());
//		} else if (String.valueOf(MeetListModel.TYPE_GROUP_MSG).equals(
//				data.getOwnerName())) {
//			holder.mChatContent.setText(data.getMsgContent());
//
//		} else {
//			BdLog.d(data.getMsgContent());
			if (TextUtils.isEmpty(data.getMsgContent())) {
				holder.mChatContent.setText(null);
			} else {
				msgTemp = new JSONArray(data.getMsgContent());
				if (msgTemp != null && msgTemp.length() >= 1) {
					String content = msgTemp.optJSONObject(0).optString("text");
					holder.mChatContent.setText(content);
				} else {
					holder.mChatContent.setText(null);
				}
			}
//		}
	}

	/**
	 * 更新未读数
	 * 
	 * @param holder
	 * @param skinType
	 * @param data
	 */
	private void refreshUnReadCount(ViewHolder holder,
			RecentChatFriendData data) {
		int count = data.getUnReadCount();
		if (count > 0) {
			holder.mNewMessage.setVisibility(View.VISIBLE);
			String countString = count > 99 ? "..." : String.valueOf(count);
//			if (!TiebaApplication.getApp().isGroupMsgOn()) {
//				countString = "";
//				count = 0;
//			}
//			if (TiebaApplication.getApp().getMsgFrequency() == 0) {
//				countString = "";
//				count = 0;
//			}
//			if (String.valueOf(MeetListModel.TYPE_GROUP_VALIDATE).equals(
//					data.getOwnerName())) {
//				countString = "";
//				count = 0;
//			} else if (String.valueOf(MeetListModel.TYPE_GROUP_UPDATES).equals(
//					data.getOwnerName())) {
//				countString = "";
//				count = 0;
//			} else if (String.valueOf(MeetListModel.TYPE_GROUP_MSG).equals(
//					data.getOwnerName())) {
//				if (null != data.getGroupSetting()
//						&& !data.getGroupSetting().isAcceptNotify()) {
//					countString = "";
//					count = 0;
//				}
//			}

			holder.mNewMessage.setText(countString);
		} else {
			holder.mNewMessage.setVisibility(View.GONE);
		}
		holder.mNameText.setTextColor(0xff262626);
		holder.mChatContent.setTextColor(0xff888888);
		holder.mChatTime.setTextColor(0xff888888);
		if (count < 10) {
			holder.mNewMessage
			.setBackgroundResource(R.drawable.icon_news_head_prompt_one);
		} else if (count < 100) {
			holder.mNewMessage
			.setBackgroundResource(R.drawable.icon_news_head_prompt_two);
		} else {
			holder.mNewMessage
			.setBackgroundResource(R.drawable.icon_news_head_prompt_more);
			holder.mNewMessage.setText("");
		}
		holder.mNewMessage.setTextColor(mContext.getResources().getColor(R.color.top_msg_num_day));
	}

	/**
	 * 更新头像
	 * 
	 * @param holder
	 * @param data
	 */
	private void refreshHeadImage(ViewHolder holder, RecentChatFriendData data) {
		// 私聊
		String portrait = data.getFriendPortrait();
		String cachePortrait = (String) holder.mHeadImage.getTag();
		if ((!TextUtils.isEmpty(portrait)
				&& !TextUtils.isEmpty(cachePortrait) && !cachePortrait
				.equals(portrait)) || TextUtils.isEmpty(cachePortrait)) {
			Bitmap bm = mAsyncImageLoader.getPhoto(portrait);
			if (bm != null) {
				holder.mHeadImage.setImageBitmap(bm);
//				bm.drawImageTo(holder.mHeadImage);
			} else {
				holder.mHeadImage.setImageBitmap(BitmapHelper
						.getCashBitmap(R.drawable.person_photo));
			}
		} else if (TextUtils.isEmpty(portrait)) {
			holder.mHeadImage.setImageBitmap(BitmapHelper
					.getCashBitmap(R.drawable.person_photo));
		}

		holder.mHeadImage.setOnClickListener(mHeadListener);
		holder.mHeadImage.invalidate();
	}

	private ViewHolder iniHolderView(View convertView) {
		ViewHolder holder;
		holder = new ViewHolder();
		holder.mLayout = (ViewGroup) convertView.findViewById(R.id.chat_item);
		holder.mContent = (ViewGroup) convertView
				.findViewById(R.id.list_content);
		holder.mHeadImage = (ImageView) convertView
				.findViewById(R.id.chat_head);
		holder.mNameText = (TextView) convertView.findViewById(R.id.chat_name);
		holder.mChatContent = (TextView) convertView
				.findViewById(R.id.last_chat_content);
		holder.mChatTime = (TextView) convertView.findViewById(R.id.chat_time);
		holder.mNewMessage = (TextView) convertView
				.findViewById(R.id.new_message);

		holder.mControl = (ViewGroup) convertView
				.findViewById(R.id.list_control);
		holder.mCtlText = (TextView) convertView
				.findViewById(R.id.list_control_tv);
		holder.mCtlProg = (ProgressBar) convertView
				.findViewById(R.id.list_control_progress);
		holder.iv_bell = (ImageView) convertView.findViewById(R.id.iv_bell);
		return holder;
	}

	private class ViewHolder {
		ViewGroup mLayout = null;
		ViewGroup mContent = null;
		ImageView mHeadImage = null;
		TextView mNameText = null;
		TextView mChatContent = null;
		TextView mChatTime = null;
		TextView mNewMessage = null;

		ViewGroup mControl = null;
		TextView mCtlText = null;
		ProgressBar mCtlProg = null;
		ImageView iv_bell = null;
	}

	public void setOnHeadListener(OnClickListener l) {
		mHeadListener = l;
	}

	public AsyncImageLoader getImageLoader() {
		return mAsyncImageLoader;
	}

	public void setIsProcessNext(boolean b) {
		mIsProcessNext = b;
	}

	public boolean getIsProcessNext() {
		return mIsProcessNext;
	}

	public void setIsProcessPre(boolean b) {
		mIsProcessPre = b;
	}

	public boolean getIsProcessPre() {
		return mIsProcessPre;
	}

	public void setHaveHeader(boolean b) {
		mHaveHeader = b;
	}

	public boolean getHaveHeader() {
		return mHaveHeader;
	}

	public void setHaveFooter(boolean b) {
		mHaveFooter = b;
	}

	public boolean getHaveFooter() {
		return mHaveFooter;
	}
}
