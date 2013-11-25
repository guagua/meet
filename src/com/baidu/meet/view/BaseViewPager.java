package com.baidu.meet.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class BaseViewPager extends ViewPager implements GestureDetector.OnGestureListener  {

	public static final int SCROLL_NEXT = 0;
	public static final int SCROLL_PRE = 1;
	private GestureDetector mGestureDetector = null;
	private OnScrollOutListener mOnFlipOutListener = null;
	private OnScrollOutListener mOnScrollOutListener = null;
	
	public BaseViewPager(Context context) {
		super(context);
		init();
	}

	public BaseViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public void setOnFlipOutListener(OnScrollOutListener listener){
		mOnFlipOutListener=listener;
	}
	
	public void setOnScrollOutListener(OnScrollOutListener listener){
		mOnScrollOutListener=listener;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(mGestureDetector==null||getAdapter()==null||getAdapter().getCount()==0||(getCurrentItem()!=0&&getAdapter().getCount()!=getCurrentItem()+1)){
			return super.onTouchEvent(event);
		}else{
			mGestureDetector.onTouchEvent(event);
			return super.onTouchEvent(event);
		}
	}
	
	private void init(){
		mGestureDetector=new GestureDetector(this);
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		if(mOnFlipOutListener!=null){
			if (arg2 < 0&&getAdapter().getCount()==getCurrentItem()+1) {
				mOnFlipOutListener.onScrollOut(SCROLL_NEXT);
				return true;
			} else if(arg2 > 0&&0==getCurrentItem())  {
				mOnFlipOutListener.onScrollOut(SCROLL_PRE);
				return true;
			}
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		if(mOnScrollOutListener!=null){
			if (arg2 > 0&&getAdapter().getCount()==getCurrentItem()+1) {
				mOnScrollOutListener.onScrollOut(SCROLL_NEXT);
				return true;
			} else if(arg2 < 0&&0==getCurrentItem())  {
				mOnScrollOutListener.onScrollOut(SCROLL_PRE);
				return true;
			}
		}
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		
		return false;
	}
	
	public interface OnScrollOutListener{
		public void onScrollOut(int state);
	}
	
}
