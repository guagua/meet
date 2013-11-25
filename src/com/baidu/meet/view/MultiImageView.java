package com.baidu.meet.view;

import java.util.ArrayList;

import com.baidu.meet.R;
import com.baidu.meet.util.UtilHelper;
import com.baidu.meet.view.BaseViewPager.OnScrollOutListener;
import com.baidu.meet.view.DragImageView.OnGifSetListener;
import com.baidu.meet.view.DragImageView.OnSizeChangedListener;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * 多图片滑动切换控件
 *
 */
public class MultiImageView extends RelativeLayout {

	private Button mZoomIn = null;
	private Button mZoomOut = null;
	private LinearLayout mTools = null;
	private OnClickListener mOnClickListener = null;
	private GalleryViewPager mGalleryViewPager = null;
	private OnPageChangeListener mOnPageChangeListener = null;
	private OnPageChangeListener mUserOnPageChangeListener = null;
	private OnSizeChangedListener mOnSizeChangedListener = null;
	private ImagePagerAdapter mAdapter = null;
	private OnGifSetListener mOnGifSetListener = null;
	private int mGifMaxMemory = 0;
	private boolean mGifPlayRealseOther = true;
	private boolean mIsSupportGesture = false;
	
	private boolean allowLocalUrl = false ;
	public MultiImageView(Context context) {
		super(context);
		init();
	}

	public void setOnScrollOutListener(OnScrollOutListener listener){
		if(mGalleryViewPager!=null)
			mGalleryViewPager.setOnFlipOutListener(listener);
	};
	
	public MultiImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public MultiImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init(){
		mIsSupportGesture = true;
		initEvent();
		initUI();
	}
	
	/**
	 * 初始化事件监听
	 */
	private void initEvent(){
		mOnClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DragImageView view = null;
				if(v == mZoomIn){
					view = getCurrentImageView();
					if(view != null){
						view.zoomInBitmap();
					}
				}else if(v == mZoomOut){
					view = getCurrentImageView();
					if(view != null){
						view.zoomOutBitmap();
					}
				}
			}
		};
		
		mOnPageChangeListener = new OnPageChangeListener(){

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				if(mUserOnPageChangeListener != null){
					mUserOnPageChangeListener.onPageScrollStateChanged(arg0);
				}
			}
 
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				if(mUserOnPageChangeListener != null){
					mUserOnPageChangeListener.onPageScrolled(arg0, arg1, arg2);
				}
			}

			@Override
			public void onPageSelected(int postion) {
				View view = mGalleryViewPager.findViewWithTag(String.valueOf(postion));
				if(view != null && view instanceof UrlDragImageView){
					DragImageView drag = ((UrlDragImageView)view).getImageView();
					if(drag != null){
						mGalleryViewPager.setSelectedView(drag);
						drag.restoreSize();
					}
				}
				
				int num = mGalleryViewPager.getChildCount();
				for(int i = 0; i < num; i++){
					View child = mGalleryViewPager.getChildAt(i);
					if(child != null && child instanceof UrlDragImageView){
						((UrlDragImageView)child).stopGif();
					}
				}
				UtilHelper.NetworkStateInfo info = UtilHelper.getNetStatusInfo(getContext());
				if(mGifPlayRealseOther == true && (info == UtilHelper.NetworkStateInfo.WIFI || info == UtilHelper.NetworkStateInfo.ThreeG)){
					for(int i = 0; i < num; i++){
						View child = mGalleryViewPager.getChildAt(i);
						if(child != null && child instanceof UrlDragImageView){
							((UrlDragImageView) child).checkImage(allowLocalUrl);
						}
					}
				}
				if(mUserOnPageChangeListener != null){
					mUserOnPageChangeListener.onPageSelected(postion);
				}
			}
			
		};
		mOnSizeChangedListener = new OnSizeChangedListener(){

			@Override
			public void sizeChenged(DragImageView view, boolean canZoomIn,
					boolean canZoomOut) {
				// TODO Auto-generated method stub
				if(mGalleryViewPager.getSelectedView() == view){
					setZoomButton(view);
				}
			}
			
		};
		
		mOnGifSetListener = new OnGifSetListener(){

			@Override
			public void gifSet(DragImageView view) {
				// TODO Auto-generated method stub
				if(view == mGalleryViewPager.getCurrentView()){
					if(mGifPlayRealseOther == true){
						int num = mGalleryViewPager.getChildCount();
						for(int i = 0; i < num; i++){
							View child = mGalleryViewPager.getChildAt(i);
							if(child != null && child instanceof UrlDragImageView){
								if(((UrlDragImageView)child).getImageView() != view){
									((UrlDragImageView)child).release();
								}
							}
						}
					}
					view.play();
				}
			}
			
		};
	}
	
	/**
	 * 页面切换至前台回调
	 */
	public void onResume(){
		if(mGalleryViewPager.getCurrentView() == null){
			return;
		}
		if(mGifPlayRealseOther == true){
			int num = mGalleryViewPager.getChildCount();
			for(int i = 0; i < num; i++){
				View child = mGalleryViewPager.getChildAt(i);
				if(child != null && child instanceof UrlDragImageView){
					if(((UrlDragImageView)child).getImageView() != mGalleryViewPager.getCurrentView()){
						((UrlDragImageView)child).release();
					}
				}
			}
		}
		int index = mGalleryViewPager.getCurrentItem();
		View view = mGalleryViewPager.findViewWithTag(String.valueOf(index));
		if(view != null && view instanceof UrlDragImageView){
			((UrlDragImageView) view).checkImage( allowLocalUrl);
		}
		mGalleryViewPager.getCurrentView().play();
	}
	
	/**
	 * 页面切换至后台回调
	 */
	public void onPause(){
		if(mGalleryViewPager.getCurrentView() != null){
			mGalleryViewPager.getCurrentView().pause();
		}
	}
	
	/**
	 * 控件销毁回调
	 */
	public void onDestroy(){
		if(mGalleryViewPager != null){
			int num = mGalleryViewPager.getChildCount();
			for(int i = 0; i < num; i++){
				View child = mGalleryViewPager.getChildAt(i);
				if(child != null && child instanceof UrlDragImageView){
					((UrlDragImageView)child).onDestroy();
				}
			}
		}
	}
	
	/**
	 * 初始化ui
	 */
	private void initUI() {
		LayoutParams params = null;
		
		mGalleryViewPager = new GalleryViewPager(getContext());
		params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mGalleryViewPager.setLayoutParams(params);
		mGalleryViewPager.setOnPageChangeListener(mOnPageChangeListener);
		this.addView(mGalleryViewPager);
        
		mTools = new LinearLayout(getContext());
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = UtilHelper.dip2px(getContext(), 10);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mTools.setOrientation(LinearLayout.HORIZONTAL);
        mTools.setLayoutParams(params);
        this.addView(mTools);
        LinearLayout.LayoutParams l_param = 
        	new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
        			LinearLayout.LayoutParams.WRAP_CONTENT);
        mZoomOut = new Button(getContext());
        mZoomOut.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.image_zoomout));
        mZoomOut.setLayoutParams(l_param);
        mZoomOut.setOnClickListener(mOnClickListener);
		mZoomOut.setEnabled(false);
		mTools.addView(mZoomOut);
        mZoomIn = new Button(getContext());
        mZoomIn.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.image_zoomin));
        mZoomIn.setLayoutParams(l_param);
        mZoomIn.setOnClickListener(mOnClickListener);
        mZoomIn.setEnabled(false);
        mTools.addView(mZoomIn);
        if(mIsSupportGesture == true){
        	mTools.setVisibility(View.GONE);
        }
        mAdapter = new ImagePagerAdapter(getContext(), null, mOnGifSetListener);
        setAdapter(mAdapter);
	}

	public void setOnPageChangeListener(OnPageChangeListener listener){
		mUserOnPageChangeListener = listener;
	}
	
	public int getItemNum(){
		return mAdapter.getCount();
	}
	
	public int getCurrentItem(){
		return mGalleryViewPager.getCurrentItem();
	}
	
	public void setZoomButton(DragImageView view){
		if(view != null){
			if(view.canZoomIn()){
				mZoomIn.setEnabled(true);
			}else{
				mZoomIn.setEnabled(false);
			}
			if(view.canZoomOut()){
				mZoomOut.setEnabled(true);
			}else{
				mZoomOut.setEnabled(false);
			}
		}else{
			mZoomOut.setEnabled(false);
			mZoomIn.setEnabled(false);
		}
	}
	
	/**
	 * 切换按钮的状态
	 */
	public void switchTools(){
		if(mIsSupportGesture == true){
			return;
		}
		if(mTools.getVisibility() != View.VISIBLE){
			mTools.setVisibility(View.VISIBLE);
		}else{
			mTools.setVisibility(View.GONE);
		}
	}
	
	public void showTools(){
		if(mIsSupportGesture == true){
			return;
		}
		mTools.setVisibility(View.VISIBLE);
	}
	
	public void hideTools(){
		if(mIsSupportGesture == true){
			return;
		}
		mTools.setVisibility(View.GONE);
	}

	private DragImageView getCurrentImageView(){
		return mGalleryViewPager.getCurrentView();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return super.onTouchEvent(event);
	}
	
	public void setPageMargin(int px){
		mGalleryViewPager.setPageMargin(px);
	}
	
	public void setOffscreenPageLimit(int limit, int image_size){
		mGalleryViewPager.setOffscreenPageLimit(limit);
//		mGifMaxMemory = UtilHelper.getBitmapMaxMemory(getContext()) - (limit * 2 + 1) * image_size * 2;
//		mGifMaxMemory *= 0.8;
//		if(mGifMaxMemory < Config.THREAD_GIF_MIN_USE_MEMORY){
//			mGifPlayRealseOther = true;
//			mGifMaxMemory = (int)(UtilHelper.getBitmapMaxMemory(getContext()) * 0.7);
//		}else{
//			mGifPlayRealseOther = false;
//		}
		mGifPlayRealseOther = true;
		PagerAdapter adapter = mGalleryViewPager.getAdapter();
		if(adapter != null && adapter instanceof ImagePagerAdapter){
			((ImagePagerAdapter)adapter).setGifMaxUseableMem(mGifMaxMemory);
		}
	}
	
	private void setAdapter(ImagePagerAdapter adapter){
		adapter.setmOnSizeChangedListener(mOnSizeChangedListener);
		mGalleryViewPager.setAdapter(adapter);
	}
	
	public void setCurrentItem(int item, boolean smoothScroll){
		mGalleryViewPager.setCurrentItem(item, smoothScroll);
	}
	
	public void setTempSize(int size){
		mAdapter.setTempSize(size);
        mAdapter.notifyDataSetChanged();
	}
	
	public void setItemOnclickListener(OnClickListener listener){
		mAdapter.setOnClickListener(listener);
	}
	public void setUrlData(ArrayList<String> data){
		mAdapter.setData(data);
        mAdapter.notifyDataSetChanged();
	}
	
	public void setHasNext(boolean hasNext){
		mAdapter.setHasNext(hasNext);
        mAdapter.notifyDataSetChanged();
	}
	
	public boolean getHasNext(){
		return mAdapter.getHasNext();
	}
	
	public void setNextTitle(String title){
		mAdapter.setNextTitle(title);
	}
	
	/**
	 * 获取当前图片的数据
	 * @return 图片二进制流
	 */
	public byte[] getCurrentImageData(){
		byte[] data = null;
		DragImageView view = mGalleryViewPager.getSelectedView();
		if(view != null){
			data = view.getImageData();
		}
		return data;
	}
	
	public String getCurrentImageUrl(){
		String url = null;
		DragImageView view = mGalleryViewPager.getSelectedView();
		if(view.getTag() instanceof String){
			url = (String)view.getTag();
		}
		return url;
	}
	public boolean isAllowLocalUrl() {
		return allowLocalUrl;
	}

	public void setAllowLocalUrl(boolean allowLocalUrl) {
		this.allowLocalUrl = allowLocalUrl;
		
		if(this.mAdapter != null){
			this.mAdapter.setAllowLocalUrl(allowLocalUrl) ;
		}
	}
}
