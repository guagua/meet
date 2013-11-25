package com.baidu.meet.show;

import java.util.ArrayList;

import com.baidu.meet.R;
import com.baidu.meet.talk.MeetListActivity;
import com.baidu.meet.util.UtilHelper;
import com.baidu.meet.view.BaseViewPager.OnScrollOutListener;
import com.baidu.meet.view.MultiImageView;
import com.baidu.meet.view.NavigationBar;

import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class ShowActivity extends Activity {
	private NavigationBar mNavigationBar;
	private TextView mTitle;
	private ImageView mMore;
	
	private MultiImageView mMultiImageView;
	private OnClickListener mOnClickListener;
	private OnPageChangeListener mOnPageChangeListener;
	private OnScrollOutListener mOnscOnScrollOutListener;
	private ArrayList<String> mUrls;
	private int mIndex;
	
	public static void startAcitivity(Context context) {
		Intent intent = new Intent(context, ShowActivity.class);
		context.startActivity(intent);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show);
		initData();
		initUI();
	}

	private void initData() {
		mUrls = new ArrayList<String>();
		mUrls.add("http://img3.cache.netease.com/photo/0001/2013-11-08/900x600_9D6O343S19BR0001.jpg");
		mUrls.add("http://a.hiphotos.bdimg.com/album/s%3D900%3Bq%3D90/sign=c82872ebbd315c60479567efbd8aba2e/8ad4b31c8701a18baf00ccb19f2f07082938fe61.jpg");
		mUrls.add("http://c.hiphotos.bdimg.com/album/s%3D680%3Bq%3D90/sign=24fcc2e0b8389b503cffe35ab50e94e0/b64543a98226cffc0c3a674cb8014a90f603ea38.jpg");
		mUrls.add("http://img3.cache.netease.com/photo/0001/2013-11-08/900x600_9D6O343S19BR0001.jpg");
		mUrls.add("http://a.hiphotos.bdimg.com/album/s%3D900%3Bq%3D90/sign=c82872ebbd315c60479567efbd8aba2e/8ad4b31c8701a18baf00ccb19f2f07082938fe61.jpg");
		mUrls.add("http://c.hiphotos.bdimg.com/album/s%3D680%3Bq%3D90/sign=24fcc2e0b8389b503cffe35ab50e94e0/b64543a98226cffc0c3a674cb8014a90f603ea38.jpg");
		mUrls.add("http://img3.cache.netease.com/photo/0001/2013-11-08/900x600_9D6O343S19BR0001.jpg");
		mUrls.add("http://a.hiphotos.bdimg.com/album/s%3D900%3Bq%3D90/sign=c82872ebbd315c60479567efbd8aba2e/8ad4b31c8701a18baf00ccb19f2f07082938fe61.jpg");
		mUrls.add("http://c.hiphotos.bdimg.com/album/s%3D680%3Bq%3D90/sign=24fcc2e0b8389b503cffe35ab50e94e0/b64543a98226cffc0c3a674cb8014a90f603ea38.jpg");
	}

	private void initUI() {
		mOnClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (v == mMore) {
					MeetListActivity.startAcitivity(ShowActivity.this);
				} 
			}
		};
		mOnPageChangeListener = new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
//				imageChange(mIndex, arg0);
//				mIndex = arg0;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		};
		mOnscOnScrollOutListener = new OnScrollOutListener() {

			@Override
			public void onScrollOut(int state) {
//				if (BaseViewPager.SCROLL_NEXT == state) {
//					mGetImageHelper.switchImageAlbum();
//				}
			}
		};
		mNavigationBar = (NavigationBar) findViewById(R.id.navigation_bar);
		mTitle = mNavigationBar.setTitleText("遇见");
		mMore = mNavigationBar.addSystemImageButton(
				NavigationBar.ControlAlign.HORIZONTAL_RIGHT,
				NavigationBar.ControlType.HOME_BUTTON, mOnClickListener);
		
		mMultiImageView = (MultiImageView) findViewById(R.id.viewpager);
		mMultiImageView.setPageMargin(UtilHelper.dip2px(this, 8));
		mMultiImageView.setOffscreenPageLimit(
				2,
				800 * 800);
		mMultiImageView.setOnPageChangeListener(mOnPageChangeListener);
		mMultiImageView.setUrlData(mUrls);
		mMultiImageView.setItemOnclickListener(mOnClickListener);
		mMultiImageView.setCurrentItem(calCurrentIndex(), false);
		mMultiImageView.setOnScrollOutListener(mOnscOnScrollOutListener);
		mMultiImageView.setHasNext(true);
	}
	
	private int calCurrentIndex() {
		if (mUrls != null && mUrls.size() > 0) {
			int num = mUrls.size();
			if (mIndex >= num) {
				mIndex = num - 1;
			}
			if (mIndex < 0) {
				mIndex = 0;
			}
		} else {
			mIndex = 0;
		}
		return mIndex;
	}

}
