package com.baidu.meet.talk;

import com.baidu.meet.R;
import com.baidu.meet.ListView.BdIListCommonPullView.ListPullRefreshListener;
import com.baidu.meet.ListView.BdListView;
import com.baidu.meet.ListView.TbListViewPullView;
import com.baidu.meet.config.RequestResponseCode;
import com.baidu.meet.imageLoader.AsyncImageLoader.ImageCallback;
import com.baidu.meet.log.MeetLog;
import com.baidu.meet.model.BaseLoadDataCallBack;
import com.baidu.meet.model.MeetListModel;
import com.baidu.meet.util.UtilHelper;
import com.baidu.meet.view.NavigationBar;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;

public class MeetListActivity extends Activity implements ImageCallback{
	private MeetListModel mModel = null;
	
	// 当前页码，从1开始
	private int mPn = 1;
	
	// 加载图片处理器
	private Handler mLoadImageHandler = null;
	// 加载图片
	private Runnable mLoadImageRunnable = null;
	
	private RecentChatFriendData mCurrentData = null;
	
	private static final int UPDATA_TYPE_NEXT = 1; // 下一页
	private static final int UPDATA_TYPE_PREVIOUS = 2; // 上一页
	private static final int UPDATA_TYPE_REFRESH = 3; // 刷新
	private static final int UPDATA_TYPE_DELETE = 4; // 删除聊天记录
	private int mType = UPDATA_TYPE_REFRESH;
	private RelativeLayout mLayout = null;

	private BdListView mChatList = null;
	private MeetListAdapter mChatListAdapter = null;
	private TbListViewPullView mPullView = null;
	
	// delete menu dialog
	AlertDialog mMenuChatList = null;
	private DialogInterface.OnClickListener mMenuListener;
	
	private boolean isHaveFooter = false;
	private boolean footerAdded = false;
	private static final String IS_HAVE_FOOTER = "is_have_footer";
	
	private NavigationBar mNavigationBar;
	private TextView mTitle;
	private ImageView mMore;
	
	public static void startAcitivity(Context context) {
		Intent intent = new Intent(context, MeetListActivity.class);
		context.startActivity(intent);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meet_list);
		initData();
		initUI();
		
		refreshLastChat(1, UPDATA_TYPE_REFRESH);
	}
	
	private void initUI() {
		// ListView 相关
		mChatList = (BdListView) findViewById(R.id.meet_list_content);
		
		mNavigationBar = (NavigationBar) findViewById(R.id.navigation_bar);
		mTitle = mNavigationBar.setTitleText("心动列表");
		mMore = mNavigationBar.addSystemImageButton(
				NavigationBar.ControlAlign.HORIZONTAL_RIGHT,
				NavigationBar.ControlType.HOME_BUTTON, mCommonOnClickListener);

		mPullView = new TbListViewPullView(this);
		mPullView.setListPullRefreshListener(mOnPullRefreshLisner);

		mChatList.setPullRefresh(mPullView);
		mChatListAdapter = new MeetListAdapter(this);
		mChatList.setAdapter(mChatListAdapter);
		mChatListAdapter.setOnHeadListener(mCommonOnClickListener);

		mChatList.setOnItemClickListener(mItemClickListener);
		mChatList.setOnScrollListener(mScrollListener);
		mChatList.setOnItemLongClickListener(mItemLongClickListener);

	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case RequestResponseCode.REQUEST_CHAT_SELECT:
//                String id = data.getStringExtra(AtListActivity.ID);
//                String name = data.getStringExtra(AtListActivity.NAME);
//                String portrait = data.getStringExtra(AtListActivity.PORTRAIT);
//                if (name != null && id != null) {
//                    ChatActivity.startChatActivity(ChatListActivity.this, id,
//                            name, portrait, null, null);
//                }
                break;
            }
        }
    }
	
	private void initData() {
		mModel = new MeetListModel();
		mModel.setLoadDataCallBack(dataCb);
		// 加载头像
		initLoadImageHandler();
	}
	
	private ListPullRefreshListener mOnPullRefreshLisner = new ListPullRefreshListener() {

		@Override
		public void onListPullRefresh(boolean auto) {
			if (!auto) {
				refreshLastChat(1, UPDATA_TYPE_REFRESH);
			} else {
				// 自动刷新时手工激活的下拉状态
			}
		}
	};
	
	private OnClickListener mCommonOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.chat_head:
				ImageView head = (ImageView) v;
			}
		}
	};
	
	private OnItemClickListener mItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (arg2 < 0) {
				return;
			}
			long index = mChatListAdapter.getItemId(arg2);
			if (index == -1) { // header
				if (mPn > 1) {
					mPn--;
//					refreshLastChatWithOutSysMsg(mPn, UPDATA_TYPE_PREVIOUS);
				}
			} else if (index == -2) { // footer
				mPn++;
//				refreshLastChatWithOutSysMsg(mPn, UPDATA_TYPE_NEXT);
			} else {
				RecentChatFriendData data = (RecentChatFriendData) mChatListAdapter
						.getItem(arg2);
				if (data != null) {
					jumpToPrivateChat(data);
//					String chatType = data.getOwnerName();
//					if (TextUtils.isEmpty(chatType)) {
//						jumpToPrivateChat(data);
//					}
				} else {
//					BdLog.d(ChatListFragment.class.getName(), "onItemClick",
//							" RecentChatFriendData data is null");
				}
			}
		}
	};
	
	private void jumpToPrivateChat(RecentChatFriendData data) {
		// 更新tab上的未来数字显示
		if (data.getUnReadCount() > 0) {
			data.setUnReadCount(0);
		}
		// 进入具体聊天页
		TalkActivity.startChatActivity(this, data.getFriendId(),
				data.getFriendName(), data.getFriendPortrait(), null,
				"chat_list");
	}
	
	private OnScrollListener mScrollListener = new OnScrollListener() {
		@Override
		public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
			mLoadImageHandler.removeCallbacks(mLoadImageRunnable);
			mLoadImageHandler.postDelayed(mLoadImageRunnable,
					300);
			return;
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}
	};
	
	private OnItemLongClickListener mItemLongClickListener = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			if (arg2 < 0) {
				return false;
			}
			long index = mChatListAdapter.getItemId(arg2);
			if (index == -1) { // header
			} else if (index == -2) { // footer
			} else {
				mCurrentData = (RecentChatFriendData) mChatListAdapter
						.getItem(arg2);
				prepareMenuDialog(mCurrentData);
				if (mMenuChatList != null) {
					mMenuChatList.show();
				}
			}
			return false;
		}
	};

	private void prepareMenuDialog(final RecentChatFriendData data) {

		final DialogInterface.OnClickListener menuListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
				case 0:
					mType = UPDATA_TYPE_DELETE;
//					mModel.deleteChat(data,
//							new ISingleRunnableCallback<Void>() {
//
//								@Override
//								public void onReturnDataInUI(Void result) {
//									refreshGroupMsg();
//								}
//							});
					break;
				}
			}
		};
		mMenuListener = menuListener;
		final String menuView = getString(R.string.delete_user_chat);
		AlertDialog.Builder builderLike = new AlertDialog.Builder(this);
		builderLike.setTitle(R.string.operation);
		builderLike.setItems(new String[] { menuView }, mMenuListener);
		mMenuChatList = builderLike.create();
		mMenuChatList.setCanceledOnTouchOutside(true);
	}
	
	BaseLoadDataCallBack dataCb = new BaseLoadDataCallBack() {

		@Override
		public void callback(Object result) {
			mChatList.completePullRefresh();

			switch (mType) {
			case UPDATA_TYPE_REFRESH:
				break;
			case UPDATA_TYPE_NEXT:
				mChatListAdapter.setIsProcessNext(false);
				break;
			case UPDATA_TYPE_PREVIOUS:
				mChatListAdapter.setIsProcessPre(false);
				break;
			case UPDATA_TYPE_DELETE:
				if (Boolean.TRUE.equals(result)) {
					// 删除成功，需要更新当前页面
//					UtilHelper
//							.showToast(MeetListActivity.this, mActivity
//									.getString(R.string.delete_user_chat_done));
//					refreshLastChatWithOutSysMsg(mPn, UPDATA_TYPE_REFRESH);
					return;
				}
//				UtilHelper.showToast(mActivity, mModel.getErrorString());
				return;
			}
			if (Boolean.TRUE.equals(result)) {
				if (mModel.hasMore() == true) {
					mChatListAdapter.setHaveFooter(true);
				} else {
					mChatListAdapter.setHaveFooter(false);
				}
				if (mPn > 1) {
					mChatListAdapter.setHaveHeader(true);
				} else {
					// 通知底层消息更新
					int count = 0;
					for (RecentChatFriendData data : mModel.getRecentChatData()) {
						count += data.getUnReadCount();
					}
					mChatListAdapter.setHaveHeader(false);
				}
				mChatListAdapter.setData(mModel.getRecentChatData());

				if (mType == UPDATA_TYPE_NEXT) {
					mChatList.setSelection(2);
				}

//				if (mModel.getErrNo() == 0) {
//					mChatListAdapter.setData(mModel.getRecentChatData());
//
//					if (mType == UPDATA_TYPE_NEXT) {
//						mChatList.setSelection(2);
//					}
//
//				} else {
////					UtilHelper.showToast(mActivity, mModel.getErrMsg());
//				}

				mLoadImageRunnable.run();
			} else {
//				UtilHelper.showToast(mActivity, mModel.getErrorString());
			}
		}
	};
	
	public void initLoadImageHandler() {
		mLoadImageHandler = new Handler();

		mLoadImageRunnable = new Runnable() {

			@Override
			public void run() {

				try {
					loadHeadIcon();
				} catch (Exception ex) {
					MeetLog.e("ChatListFragment", "mLoadImageRunnable.run",
							"error = " + ex.getMessage());
				}

			}
		};
	}
	
	private void loadHeadIcon() {
		UtilHelper.NetworkStateInfo info = UtilHelper
				.getNetStatusInfo(this);
		mChatListAdapter.getImageLoader().clearHoldUrl();
		boolean supportHold = false;
		if (info == UtilHelper.NetworkStateInfo.WIFI
				|| info == UtilHelper.NetworkStateInfo.ThreeG) {
			supportHold = true;
		} else {
			supportHold = false;
		}
		mChatListAdapter.getImageLoader().setSupportHoldUrl(supportHold);

		int firstVisiblePos = mChatList.getFirstVisiblePosition();
		int lastVisiblePos = mChatList.getLastVisiblePosition();

		mChatListAdapter.getImageLoader().clearHoldUrl();
		int image_num = 0;
		for (int i = firstVisiblePos; i < mChatListAdapter.getCount(); i++) {
			if (supportHold == false && i > lastVisiblePos) {
				break;
			}
			RecentChatFriendData rcfd = (RecentChatFriendData) mChatListAdapter
					.getItem(i);
			if (mChatListAdapter.getItem(i) instanceof RecentChatFriendData) {
				if (image_num < 30) {

					String url = ((RecentChatFriendData) mChatListAdapter
							.getItem(i)).getFriendPortrait();
					if (url != null && url.equals("") == false) {
						String type = rcfd.getOwnerName();
//						if (!TextUtils.isEmpty(type)) {
//							if (type.equals(String
//									.valueOf(ChatListModel.TYPE_GROUP_MSG))) {
//								mChatListAdapter.getImageLoader().loadImage(
//										url, this);
//							} else {
//								mChatListAdapter.getImageLoader()
//										.loadFriendPhoto(url, this);
//							}
//						} else {
//							mChatListAdapter.getImageLoader().loadFriendPhoto(
//									url, this);
//						}
						mChatListAdapter.getImageLoader().loadImage(
								url, this);
						image_num++;
					}
				}
			}

			if (image_num >= 30) {
				break;
			}
		}
	}

	@Override
	public void imageLoaded(Bitmap bitmap, String imageUrl, boolean isCached) {
		if (bitmap == null) {
			return;
		}
		if (mChatList == null) {
			return;
		}
		ImageView view = (ImageView) mChatList.findViewWithTag(imageUrl);
		if (view != null) {
			view.setImageBitmap(bitmap);
			view.invalidate();
		} else {
		}
	}
	
	/**
	 * 刷新最新数据
	 */
	private void refreshLastChat(int page, int type) {
		if (page >= 1) {
			mPn = page;
			mType = type;
			if (mType == UPDATA_TYPE_NEXT) {
				mChatListAdapter.setIsProcessNext(true);
			} else if (mType == UPDATA_TYPE_PREVIOUS) {
				mChatListAdapter.setIsProcessPre(true);
			}

			if (mModel.startTask("123", String.valueOf(mPn))) {
				// 【消息】在聊天消息列表页，每隔30s页面刷新一次，其他当有新未读消息，切换到这个列表才会刷新
				// 【确认：逻辑不变，隐藏后台刷新时对用户显示的效果
				// if(page == 1){
				// mChatList.startPullRefresh() ;
				// }
			}
		}
	}
}
