package com.baidu.meet.ListView;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.baidu.meet.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BdIListCommonPullView extends BdIListPullView {
	private View mHeaderView = null;
	private ImageView mArrowView = null;
	private ProgressBar mProgressBar = null;
	private TextView mPullText = null;
	private TextView mPullTime = null;
	private RotateAnimation mAnimRotate = null;
	private RotateAnimation mAnimReverseRotate = null;
	private ListPullRefreshListener mListPullRefreshListener = null;

	private String mPullMsg;
	private String mReleaseMsg;
	private String mLoadingMsg;

	public BdIListCommonPullView(Context context) {
		super(context);
	}

	@Override
	public View createView() {
		Context mContext = getContext();
		String pullMsg = mContext.getString(R.string.adp_pull_to_refresh);
		String releaseMsg = mContext.getString(R.string.adp_release_to_refresh);
		String loadingMsg = mContext.getString(R.string.adp_loading);
		return createView(pullMsg, releaseMsg, loadingMsg);
	}

	/**
	 * by dj for v5.1 PullView
	 * 
	 * @param pullMsg
	 * @param releaseMsg
	 * @return
	 */
	public View createView(String pullMsg, String releaseMsg, String loadingMsg) {
		Context mContext = getContext();
		this.mPullMsg = pullMsg != null ? pullMsg : mContext
				.getString(R.string.adp_pull_to_refresh);
		this.mReleaseMsg = releaseMsg != null ? releaseMsg : mContext
				.getString(R.string.adp_release_to_refresh);
		this.mLoadingMsg = loadingMsg != null ? loadingMsg : mContext
				.getString(R.string.adp_loading);
		LayoutInflater inflater = LayoutInflater.from(getContext());

		mHeaderView = inflater.inflate(R.layout.pull_view, null);

		mArrowView = (ImageView) mHeaderView.findViewById(R.id.pull_arrow);

		mProgressBar = (ProgressBar) mHeaderView
				.findViewById(R.id.pull_progress);
		mPullText = (TextView) mHeaderView.findViewById(R.id.pull_text);
		mPullTime = (TextView) mHeaderView.findViewById(R.id.pull_time);
		setPullTime(getDateString());
		mAnimRotate = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mAnimRotate.setInterpolator(new LinearInterpolator());
		mAnimRotate.setDuration(250);
		mAnimRotate.setFillAfter(true);

		mAnimReverseRotate = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mAnimReverseRotate.setInterpolator(new LinearInterpolator());
		mAnimReverseRotate.setDuration(200);
		mAnimReverseRotate.setFillAfter(true);
		
		
		return mHeaderView;
	}

	

	/**
	 * by dj for v5.1 PullView
	 * 
	 * @return
	 */
	public View getHeaderView() {
		if (mHeaderView == null) {
			mHeaderView = createView();
		}
		return mHeaderView;
	}

	public TextView getPullTime() {
		return mPullTime;
	}

	public TextView getPullText() {
		return mPullText;
	}

	@Override
	public void releaseToRefresh() {
		mArrowView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.INVISIBLE);
		mPullText.setVisibility(View.VISIBLE);
		mPullTime.setVisibility(View.VISIBLE);
		mArrowView.clearAnimation();

		mArrowView.startAnimation(mAnimRotate);

		mPullText.setText(mReleaseMsg);
	}

	@Override
	public void pullToRefresh(boolean isBack) {
		// TODO Auto-generated method stub
		mProgressBar.setVisibility(View.INVISIBLE);
		mPullText.setVisibility(View.VISIBLE);
		mPullTime.setVisibility(View.VISIBLE);
		mArrowView.setVisibility(View.VISIBLE);
		mArrowView.clearAnimation();
		// 是由RELEASE_To_REFRESH状态转变来的
		if (isBack) {
			mArrowView.startAnimation(mAnimReverseRotate);
		}
		mPullText.setText(mPullMsg);
	}

	@Override
	public void refreshing() {
		mProgressBar.setVisibility(View.VISIBLE);
		mArrowView.clearAnimation();
		mArrowView.setVisibility(View.INVISIBLE);
		mPullText.setText(mLoadingMsg);
		mPullTime.setVisibility(View.VISIBLE);
	}

	@Override
	public void done(boolean success) {
		mProgressBar.setVisibility(View.INVISIBLE);
		mArrowView.clearAnimation();
		mArrowView.setImageResource(R.drawable.pull_icon);
		mPullText.setText(mPullMsg);
		mPullTime.setVisibility(View.VISIBLE);
		if (success == true) {
			setPullTime(getDateString());
		}
	}

	@SuppressLint("SimpleDateFormat")
	private static SimpleDateFormat FORMATE_DATA = new SimpleDateFormat(
			"MM-dd HH:mm");

	public static String getDateString() {
		synchronized (FORMATE_DATA) {
			return FORMATE_DATA.format(new Date());
		}
	}

	public void setPullTime(String date) {
		String mDate = getContext().getString(R.string.adp_pull_view_date_tip)
				+ date;
		mPullTime.setText(mDate);
	}

	@Override
	public void onRefresh(boolean auto) {
		// TODO Auto-generated method stub
		if (mListPullRefreshListener != null) {
			mListPullRefreshListener.onListPullRefresh(auto);
		}
	}

	public void setListPullRefreshListener(ListPullRefreshListener listener) {
		mListPullRefreshListener = listener;
	}

	public interface ListPullRefreshListener {
		public void onListPullRefresh(boolean auto);
	}

}
