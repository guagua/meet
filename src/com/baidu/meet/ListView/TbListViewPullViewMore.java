package com.baidu.meet.ListView;

import com.baidu.meet.R;

import android.content.Context;
import android.view.View;

/**
 * by dj for v5.1 PullView
 */
public class TbListViewPullViewMore extends TbListViewPullView{

	public TbListViewPullViewMore(Context context) {
		super(context);
	}

	@Override
	public View createView() {
		String pullMsg = getContext().getString(R.string.pull_view_pull_more);
		String releaseMsg = getContext().getString(
				R.string.pull_view_release_more);
		return createView(pullMsg, releaseMsg, null);
	}

}
