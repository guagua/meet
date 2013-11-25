package com.baidu.meet.view;

import java.util.ArrayList;

import com.baidu.meet.R;
import com.baidu.meet.view.DragImageView.OnGifSetListener;
import com.baidu.meet.view.DragImageView.OnSizeChangedListener;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Gallery;
import android.widget.TextView;

public class ImagePagerAdapter extends PagerAdapter {
	private Context mContext = null;
	private ArrayList<String> mUrl = null;
	private OnClickListener mOnClickListener = null;
	private OnSizeChangedListener mOnSizeChangedListener = null;
	private OnGifSetListener mOnGifSetListener = null;
	private int mGifMaxUseableMem = 0;
	private boolean mHasNext = false;
	private String mNextTitle = null;
	private int mTempSize = 0;
	
	private boolean mIsCdn = false;
	private boolean allowLocalUrl = false ;
	
	public ImagePagerAdapter(Context context, ArrayList<String> url, OnGifSetListener onGifSetListener){
		mContext = context;
		mUrl = url;
		mOnGifSetListener = onGifSetListener;
	}
	
	public void setData(ArrayList<String> data){
		mUrl = data;
		this.notifyDataSetChanged();
	}
	
	public void setNextTitle(String nextTitle){
		mNextTitle = nextTitle;
	}
	
	public void setHasNext(boolean hasNext){
		mHasNext = hasNext;
        notifyDataSetChanged();
	}
	
	public boolean getHasNext(){
		return mHasNext;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		int num = 0;
		if(mUrl != null){
			num = mUrl.size();
			if(mHasNext == true){
				num++;
			}
		}
		num += mTempSize;
		return num;
	}

	public void setTempSize(int size){
		mTempSize = size;
        notifyDataSetChanged();
	}
	
	public void setOnClickListener(OnClickListener listener){
		mOnClickListener = listener;
	}
	
	public void setGifMaxUseableMem(int mem){
		mGifMaxUseableMem = mem;
	}
	
	public void setmOnSizeChangedListener(OnSizeChangedListener listener){
		mOnSizeChangedListener = listener;
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object) {
		// TODO Auto-generated method stub
		return view.equals(object);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		((ViewPager) container).removeView((View) object);
		if(object instanceof UrlDragImageView){
			UrlDragImageView urlImage = (UrlDragImageView)object;
			urlImage.onDestroy();
		}
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		if(position==mUrl.size()){
			LayoutInflater mInflater = LayoutInflater.from(mContext);
			View nextView=mInflater.inflate(R.layout.big_image_next,null);
			TextView threadText=(TextView)nextView.findViewById(R.id.thread_name);
			threadText.setText(mNextTitle);
			container.addView(nextView);
			nextView.setOnClickListener(mOnClickListener);
			return nextView;
		}
		UrlDragImageView iv = new UrlDragImageView(mContext);
		String url=null;
		if(position<mUrl.size()){
			url = mUrl.get(position);
		}
        iv.setLayoutParams(new Gallery.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        iv.setImageOnClickListener(mOnClickListener);
        iv.setIsCdn(mIsCdn);
        iv.setOnSizeChangedListener(mOnSizeChangedListener);
        ((ViewPager) container).addView(iv, 0);
        iv.setUrl(url, allowLocalUrl);
        iv.setGifMaxUseableMem(mGifMaxUseableMem);
        iv.setTag(String.valueOf(position));
        iv.setGifSetListener(mOnGifSetListener);
        return iv;
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		super.setPrimaryItem(container, position, object);
		if(!(object instanceof UrlDragImageView)){
			return;
		}
		GalleryViewPager pager = (GalleryViewPager)container;
		DragImageView drag = ((UrlDragImageView)object).getImageView();
		if(pager.getSelectedView() == null){
			pager.setSelectedView(drag);
			ViewParent Parent = pager.getParent();
			if(Parent != null && Parent instanceof MultiImageView){
				((MultiImageView)Parent).setZoomButton(drag);
			}
		}
		DragImageView tmp = pager.getCurrentView();
		if(drag != tmp){
			if(tmp != null){
				tmp.restoreSize();
			}
			((UrlDragImageView)object).checkImage(allowLocalUrl);
			pager.setCurrentView(drag);
			if(((UrlDragImageView)object).getImageType() == DragImageView.IMAGE_TYPE_DYNAMIC){
				mOnGifSetListener.gifSet(drag);
			}
		}
	}

	public boolean isAllowLocalUrl() {
		return allowLocalUrl;
	}

	public void setAllowLocalUrl(boolean allowLocalUrl) {
		this.allowLocalUrl = allowLocalUrl;
	}
	public boolean isIsCdn() {
		return mIsCdn;
	}

	public void setIsCdn(boolean mIsCdn) {
		this.mIsCdn = mIsCdn;
	}

	public interface ImageLoadCallBack{
		abstract public void callback();
	}
}
