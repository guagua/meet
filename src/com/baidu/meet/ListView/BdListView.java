package com.baidu.meet.ListView;

import java.security.InvalidParameterException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BdListView extends ListView {

	private BdListAdpter mBdListAdpter = null;
	private OnItemClickListener mOnItemClickListener = null;
	private OnItemLongClickListener mOnItemLongClickListener = null;
	private OnItemSelectedListener mOnItemSelectedListener = null;
	private OnHeaderClickListener mOnHeaderClickListener = null;
	private OnFooterClickListener mOnFooterClickListener = null;
	private OnScrollListener mOnScrollListener = null;
	private OnScrollStopDelayedListener mOnScrollStopDelayedListener = null;
	private long mOnScrollStopDelayedMillis = 100;
	private OnScrollToTopListener mOnScrollToTopListener = null;
	private int mScrollToTopNum = 0;
	private OnScrollToBottomListener mOnScrollToBottomListener = null;
	private BdIListPage mPrePage = null;
	private BdIListPage mNextPage = null;
	private View NoDataView = null;
	private int mFirstVisibleItemIndex = 0;

	private Runnable mDelayedRunnable = new Runnable() {

		@Override
		public void run() {

			if (mOnScrollStopDelayedListener != null) {
				int firstVisiblePos = getFirstVisiblePosition();
				int lastVisiblePos = getLastVisiblePosition();
				if (mBdListAdpter != null
						&& mBdListAdpter.getWrappedAdapter() != null
						&& mBdListAdpter.getWrappedCount() > 0) {
					firstVisiblePos -= mBdListAdpter.getHeadersCount();
					if (firstVisiblePos < 0) {
						firstVisiblePos = 0;
					}
					lastVisiblePos -= mBdListAdpter.getHeadersCount();
					if (lastVisiblePos >= mBdListAdpter.getWrappedCount()) {
						lastVisiblePos = mBdListAdpter.getWrappedCount() - 1;
					}
					if (lastVisiblePos < 0) {
						lastVisiblePos = 0;
					}
				} else {
					firstVisiblePos = -1;
					lastVisiblePos = -1;
				}
				mOnScrollStopDelayedListener.onScrollStop(firstVisiblePos,
						lastVisiblePos);
			}

		}
	};

	public BdListView(Context context) {
		super(context);
		initial();
	}

	public BdListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initial();
	}

	public BdListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initial();
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		try {
			super.dispatchDraw(canvas);
		} catch (NullPointerException ex) {
			if (getContext() instanceof Activity) {
				((Activity) getContext()).finish();
			}

		}
	}

	private void initial() {
		mBdListAdpter = new BdListAdpter(getContext());
		super.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int numHeaders = mBdListAdpter.getHeadersCount();
				if (position < numHeaders) {
					if (mPrePage != null && view == mPrePage.getView()) {
						mPrePage.onClick();
					} else if (mOnHeaderClickListener != null) {
						mOnHeaderClickListener.onClick(view);
					}
					return;
				}
				// Adapter
				final int adjPosition = position - numHeaders;
				int adapterCount = 0;
				ListAdapter adapter = mBdListAdpter.getWrappedAdapter();
				if (adapter != null) {
					adapterCount = adapter.getCount();
					if (adjPosition < adapterCount) {
						if (mOnItemClickListener != null) {
							mOnItemClickListener.onItemClick(parent, view,
									adjPosition, id);
						}
						return;
					}
				}

				// Footer (off-limits positions will throw an
				// ArrayIndexOutOfBoundsException)
				if (mNextPage != null && view == mNextPage.getView()) {
					mNextPage.onClick();
				} else if (mOnFooterClickListener != null) {
					mOnFooterClickListener.onClick(view);
				}
			}
		});
		super.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (mOnScrollListener != null) {
					mOnScrollListener.onScrollStateChanged(view, scrollState);
				}

				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					if (mOnScrollToBottomListener != null
							&& view.getLastVisiblePosition() == view.getCount() - 1
							&& view.getFirstVisiblePosition() != 0) {
						mOnScrollToBottomListener.onScrollToBottom();
					}
					if (mOnScrollToTopListener != null
							&& view.getFirstVisiblePosition() <= mScrollToTopNum) {
						mOnScrollToTopListener.onScrollToTop();
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				mFirstVisibleItemIndex = firstVisibleItem;

				if (mOnScrollListener != null) {
					mOnScrollListener.onScroll(view, firstVisibleItem,
							visibleItemCount, totalItemCount);
				}
				if (mOnScrollStopDelayedListener != null
						&& mOnScrollStopDelayedMillis > 0) {
					getHandler().removeCallbacks(mDelayedRunnable);
					getHandler().postDelayed(mDelayedRunnable,
							mOnScrollStopDelayedMillis);
				}
			}
		});
	}

	public void setOnSrollToTopListener(OnScrollToTopListener l) {
		mOnScrollToTopListener = l;
	}

	public void setOnSrollToTopListener(OnScrollToTopListener l, int num) {
		mOnScrollToTopListener = l;
		num--;
		if (num < 0) {
			num = 0;
		}
		mScrollToTopNum = num;
	}

	public void setOnSrollToBottomListener(OnScrollToBottomListener l) {
		mOnScrollToBottomListener = l;
	}

	public void setOnScrollStopDelayedListener(OnScrollStopDelayedListener l,
			long delayMillis) {
		mOnScrollStopDelayedListener = l;
		mOnScrollStopDelayedMillis = delayMillis;
	}

	@Override
	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		if (listener == null) {
			super.setOnItemLongClickListener(null);
		} else {
			mOnItemLongClickListener = listener;
			super.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
						View view, int position, long id) {
					int numHeaders = mBdListAdpter.getHeadersCount();
					if (position < numHeaders) {
						return true;
					}
					// Adapter
					final int adjPosition = position - numHeaders;
					int adapterCount = 0;
					ListAdapter adapter = mBdListAdpter.getWrappedAdapter();
					if (adapter != null) {
						adapterCount = adapter.getCount();
						if (adjPosition < adapterCount) {
							if (mOnItemLongClickListener != null) {
								return mOnItemLongClickListener
										.onItemLongClick(parent, view,
												adjPosition, id);
							} else {
								return false;
							}
						}
					}
					return true;
				}
			});
		}
	}

	@Override
	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		if (listener == null) {
			super.setOnItemSelectedListener(null);
		} else {
			mOnItemSelectedListener = listener;
			super.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					int numHeaders = mBdListAdpter.getHeadersCount();
					if (position < numHeaders) {
						return;
					}
					// Adapter
					final int adjPosition = position - numHeaders;
					int adapterCount = 0;
					ListAdapter adapter = mBdListAdpter.getWrappedAdapter();
					if (adapter != null) {
						adapterCount = adapter.getCount();
						if (adjPosition < adapterCount) {
							if (mOnItemSelectedListener != null) {
								mOnItemSelectedListener.onItemSelected(parent,
										view, adjPosition, id);
							}
						}
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					if (mOnItemSelectedListener != null) {
						mOnItemSelectedListener.onNothingSelected(parent);
					}
				}
			});
		}
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mOnScrollListener = l;
	}

	@Override
	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}

	@Override
	public ListAdapter getAdapter() {
		return mBdListAdpter;
	}

	public ListAdapter getWrappedAdapter() {
		if (mBdListAdpter instanceof BdListAdpter) {
			return ((BdListAdpter) mBdListAdpter).getWrappedAdapter();
		}

		return null;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(null);
		mBdListAdpter.setAdaper(adapter);
		super.setAdapter(mBdListAdpter);
	}

	public void setOnHeaderClickListener(OnHeaderClickListener listener) {
		mOnHeaderClickListener = listener;
	}

	public void setOnFooterClickListener(OnFooterClickListener listener) {
		mOnFooterClickListener = listener;
	}

	@Override
	public void addHeaderView(View v, Object data, boolean isSelectable) {
		mBdListAdpter.addHeaderView(v, data, isSelectable, getHeaderIndex());
	}

	@Override
	public void addHeaderView(View v) {
		mBdListAdpter.addHeaderView(v, getHeaderIndex());
	}

	public void addPullRefreshView(View v) {
		mBdListAdpter.addHeaderView(v, null, false, 0);
	}

	@Override
	public void addFooterView(View v, Object data, boolean isSelectable) {
		mBdListAdpter.addFooterView(v, data, isSelectable, -1);
	}

	@Override
	public void addFooterView(View v) {
		mBdListAdpter.addFooterView(v);
	}

	@Override
	public boolean removeHeaderView(View v) {
		return mBdListAdpter.removeHeader(v);
	}

	@Override
	public boolean removeFooterView(View v) {
		return mBdListAdpter.removeFooter(v);
	}

	public void setNoData(String text) {
		if (NoDataView != null) {
			removeHeaderView(NoDataView);
			NoDataView = null;
		}
		if (text != null) {
			TextView v = new TextView(getContext());
			v.setText(text);
			v.setTextSize(16);
			v.setGravity(Gravity.CENTER);
			setNoData(v);
		}
	}

	public void setNoData(View v) {
		addHeaderView(v, null, false);
	}

	public void setPrePage(BdIListPage page) {
		if (mPrePage != null) {
			removeHeaderView(mPrePage.getView());
			mPrePage = null;
		}
		if (page != null) {
			addHeaderView(page.getView());
			mPrePage = page;
		}
	}

	@Override
	public void setSelectionFromTop(int position, int y) {
		super.setSelectionFromTop(position, y);
	}

	public void setNextPage(BdIListPage page) {
		if (mNextPage != null) {
			removeFooterView(mNextPage.getView());
			mNextPage = null;
		}
		if (page != null) {
			mBdListAdpter.addFooterView(page.getView(), null, true, 0);
			mNextPage = page;
		}
	}

	private int getHeaderIndex() {
		if (mPrePage != null) {
			return mBdListAdpter.getHeadersCount() - 1;
		} else {
			return -1;
		}
	}

	public interface OnHeaderClickListener {
		public void onClick(View v);
	}

	public interface OnFooterClickListener {
		public void onClick(View v);
	}

	@Override
	protected void onDetachedFromWindow() {
		try {
			super.onDetachedFromWindow();
			getHandler().removeCallbacks(mDelayedRunnable);
			getHandler().removeCallbacks(mSelectRunnable);
		} catch (Exception e) {
		} 
	}

	public interface OnScrollToTopListener {
		public void onScrollToTop();
	}

	public interface OnScrollToBottomListener {
		public void onScrollToBottom();
	}

	public interface OnScrollStopDelayedListener {
		public void onScrollStop(int firstVisiblePos, int lastVisiblePos);
	}

	// /////////////////////////////////////////////////////////////////////////
	// 键盘相关
	public static final byte KEYBOARD_STATE_SHOW = -3;
	public static final byte KEYBOARD_STATE_HIDE = -2;
	public static final byte KEYBOARD_STATE_INIT = -1;
	private boolean mLayoutHasInit = false;
	private boolean mHasKeybord = false;
	private boolean mKeybordScrollBottom = false;
	private int mMaxHeight = 0;
	private int mPreHeight = 0;
	private OnKybdsChangeListener mOnKybdsChangeListener = null;

	private Runnable mSelectRunnable = new Runnable() {

		@Override
		public void run() {
			setSelection(getCount() - 1);
		}
	};

	public void setKybdsScrollBottom(boolean scroll) {
		mKeybordScrollBottom = scroll;
	}

	public interface OnKybdsChangeListener {
		public void onKeyBoardStateChange(int state);
	}

	public void setOnkbdStateListener(OnKybdsChangeListener listener) {
		mOnKybdsChangeListener = listener;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int last = getLastVisiblePosition();
		try {
			super.onLayout(changed, l, t, r, b);
		} catch (Throwable ex) {
			if (getContext() instanceof Activity) {
				((Activity) getContext()).finish();
			}
			return;
		}
		if (!mLayoutHasInit) {
			mLayoutHasInit = true;
			mMaxHeight = b;
			if (mOnKybdsChangeListener != null) {
				mOnKybdsChangeListener
						.onKeyBoardStateChange(KEYBOARD_STATE_INIT);
			}
		} else {
			mMaxHeight = mMaxHeight < b ? b : mMaxHeight;
		}
		if (mLayoutHasInit && mMaxHeight > b && b != mPreHeight) {
			mHasKeybord = true;
			if (mOnKybdsChangeListener != null) {
				mOnKybdsChangeListener
						.onKeyBoardStateChange(KEYBOARD_STATE_SHOW);
			}
			if (mKeybordScrollBottom == true && last >= getCount() - 1) {
				getHandler().postDelayed(mSelectRunnable, 1);
			}
		}
		if (mLayoutHasInit && mHasKeybord && mMaxHeight == b) {
			mHasKeybord = false;
			if (mOnKybdsChangeListener != null) {
				mOnKybdsChangeListener
						.onKeyBoardStateChange(KEYBOARD_STATE_HIDE);
			}
		}
		mPreHeight = b;
	}

	// //////////////////////////////////////////////////////////////////////////////
	// 下拉刷新相关
	private PullRefresh mPullRefresh = null;

	public void setPullRefresh(BdIListPullView view) {
		if (mPullRefresh != null) {
			removeHeaderView(mPullRefresh.getBdIListPullView().getView());
		}
		mPullRefresh = null;
		if (view != null) {
			mPullRefresh = new PullRefresh(this, view);
		}
	}

	public void completePullRefresh() {
		// if (mPullRefresh != null) {
		// mPullRefresh.done();
		// }
		if (mPullRefresh != null) {
			mPullRefresh.animatePullView();
		}
	}

	public void startPullRefresh() {
		if (mPullRefresh != null) {
			setSelection(0);
			mPullRefresh.startPullRefresh(true);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mPullRefresh != null) {
			mPullRefresh.onInterceptTouchEvent(ev, mFirstVisibleItemIndex);
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mPullRefresh != null) {
			mPullRefresh.onTouchEvent(event, mFirstVisibleItemIndex);
		}
		boolean ret = false;
		try {
			ret = super.onTouchEvent(event);
		} catch (Exception e) {
		}
		return ret;
	}

	public static boolean setPullRefreshRatio(float ratio) {
		return PullRefresh.setRatio(ratio);
	}



	/**
	 * 下拉是否是结束状态。
	 */
	public boolean isRefreshDone(){
		if(mPullRefresh != null){
			return mPullRefresh.mState == PullRefresh.DONE ;
		}
		
		return  true ;
	}
	
	private static class PullRefresh{

		private final static int RELEASE_TO_REFRESH = 0;
		private final static int PULL_TO_REFRESH = 1;
		private final static int REFRESHING = 2;
		private final static int DONE = 3;

		// 实际的padding的距离与界面上偏移距离的比例
		private static float sRatio = 3;

		private BdIListPullView mBdIListPullView = null;
		private boolean mIsRecored = false;
		private int mStartY = 0;
		protected int mState = DONE;
		private BdListView mListView = null;
		private Boolean mIsBack = false;

		
		
		public static boolean setRatio(float ratio){
			if(ratio > 0){
				sRatio = ratio;
				return true;
			} else {
				return false;
			}
		}

		public PullRefresh(BdListView listView, BdIListPullView view) {
			if (view == null) {
				throw new InvalidParameterException("PullRefresh view is null");
			}
			if (listView == null) {
				throw new InvalidParameterException(
						"PullRefresh listView is null");
			}
			mBdIListPullView = view;
			mListView = listView;
			View headView = mBdIListPullView.getView();
			headView.setPadding(0, -mBdIListPullView.getHeadContentHeight(), 0,
					0);
			headView.invalidate();
			mListView.addPullRefreshView(headView);
		}

		public BdIListPullView getBdIListPullView() {
			return mBdIListPullView;
		}

		public void done() {
			mState = DONE;
			mBdIListPullView.setPadding(0,
					-mBdIListPullView.getHeadContentHeight(), 0, 0);
			mBdIListPullView.done(true);
		}

		public void startPullRefresh(boolean auto) {
			mState = REFRESHING;
			mBdIListPullView.setPadding(0, 0, 0, 0);
			mBdIListPullView.refreshing();
			mBdIListPullView.onRefresh(auto);
		}

		public void onInterceptTouchEvent(MotionEvent ev, int firstItemIndex) {
			if (ev.getAction() == MotionEvent.ACTION_DOWN
					&& getBdIListPullView().isEnable()) {
				mIsRecored = false;
				mIsBack = false;
				if (firstItemIndex == 0 && !mIsRecored) {
					mIsRecored = true;
					mStartY = (int) ev.getY();
				}
			}
			return;
		}

		public void onTouchEvent(MotionEvent event, int firstItemIndex) {
			if (getBdIListPullView().isEnable()) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					if (mState != REFRESHING) {
						if (mState == PULL_TO_REFRESH) {
							mState = DONE;
							mBdIListPullView.setPadding(0,
									-mBdIListPullView.getHeadContentHeight(),
									0, 0);
							mBdIListPullView.done(false);
						} else if (mState == RELEASE_TO_REFRESH) {
							startPullRefresh(false);
							// animatePullView();
						}
					}

					break;

				case MotionEvent.ACTION_MOVE:
					int tempY = (int) event.getY();

					if (!mIsRecored && firstItemIndex == 0) {
						mIsRecored = true;
						mStartY = tempY;
					}

					if (mState != REFRESHING && mIsRecored) {

						// 保证在设置padding的过程中，当前的位置一直是在head，否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动

						// 可以松手去刷新了
						if (mState == RELEASE_TO_REFRESH) {
							mListView.setSelection(0);
							// 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
							if (((int) ((tempY - mStartY) / sRatio) < mBdIListPullView
									.getHeadContentHeight())
									&& (tempY - mStartY) > 0) {
								mState = PULL_TO_REFRESH;
								mBdIListPullView.pullToRefresh(mIsBack);
								mIsBack = false;
							} else if (tempY - mStartY <= 0) { // 一下子推到顶了
								mState = DONE;
								mBdIListPullView.setPadding(0,
										-mBdIListPullView
												.getHeadContentHeight(), 0, 0);
								mBdIListPullView.done(false);
							}
						} else if (mState == PULL_TO_REFRESH) {
							mListView.setSelection(0);
							// 下拉到可以进入RELEASE_TO_REFRESH的状态
							if ((int) ((tempY - mStartY) / sRatio) >= mBdIListPullView
									.getHeadContentHeight()) {
								mState = RELEASE_TO_REFRESH;
								mIsBack = true;
								mBdIListPullView.releaseToRefresh();
							} else if (tempY - mStartY <= 0) { // 上推到顶了
								mState = DONE;
								mBdIListPullView.setPadding(0,
										-mBdIListPullView
												.getHeadContentHeight(), 0, 0);
								mBdIListPullView.done(false);
							}
						} else if (mState == DONE) {
							if (tempY - mStartY > 0) {
								mState = PULL_TO_REFRESH;
								mBdIListPullView.pullToRefresh(mIsBack);
								mIsBack = false;
							}
						}

						// 更新headView的paddingTop
						if (mState == PULL_TO_REFRESH
								|| mState == RELEASE_TO_REFRESH) {
							mBdIListPullView.setPadding(
									0,
									(int) ((tempY - mStartY) / sRatio)
											- mBdIListPullView
													.getHeadContentHeight(), 0,
									0);

						}

					}

					break;
				}
			}
		}

		public static final int DEFAULT_REFRESH_DURATION_TIME = 800;
		private int mAnimDurationTime = DEFAULT_REFRESH_DURATION_TIME;

		private void animatePullView() {

			BdIListPullView mBdIListPullView = getBdIListPullView();
			if (mBdIListPullView == null)
				return;

			View mView = mBdIListPullView.getView();
			if (mView == null)
				return;
			int from = 0;
			int to = -mBdIListPullView.getHeadContentHeight();
			BdPaddingAnimation4ListView mBdPaddingAnimation = new BdPaddingAnimation4ListView(
					mView.getContext(), from, to, mAnimDurationTime);
			// BdPaddingAnimation mBdPaddingAnimation = new BdPaddingAnimation(
			// from, to, mAnimDurationTime);
			mBdPaddingAnimation
					.setOnAnimationOverListener(new BdOnAnimationOverListener() {
						@Override
						public void onOver() {
							done();
						}
					});
			mBdPaddingAnimation.startAnimation(mView);

		}
	}

}
