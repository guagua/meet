package com.baidu.meet.view;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.baidu.meet.log.MeetLog;
import com.baidu.tieba.compatible.CompatibleUtile;

public class GalleryViewPager extends BaseViewPager {
	private PointF last;
	private DragImageView mCurrentView;
	private DragImageView mSelectedView;
	
    public GalleryViewPager(Context context) {
        super(context);
    }
    public GalleryViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void setCurrentView(DragImageView view){
    	mCurrentView = view;
    }
    
    public DragImageView getCurrentView(){
    	return mCurrentView;
    }
    
    private float[] handleMotionEvent(MotionEvent event)
    {
        switch (event.getAction() & CompatibleUtile.getActionMask()) {
            case MotionEvent.ACTION_DOWN:
                last = new PointF(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                PointF curr = new PointF(event.getX(), event.getY());
                return new float[]{curr.x - last.x, curr.y - last.y};

        }
        return null;
    }
    

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if ((event.getAction() & CompatibleUtile.getActionMask()) == MotionEvent.ACTION_UP){
            super.onTouchEvent(event);
            if(mCurrentView != null){
            	mCurrentView.actionUp();
            }
        }
        if(mCurrentView == null){
        	return super.onTouchEvent(event);
        }
        float [] difference = handleMotionEvent(event);

        if (mCurrentView.pagerCantScroll()) {
            return super.onTouchEvent(event);
        }
        else {
            if (difference != null && mCurrentView.onRightSide() && difference[0] < 0) //move right
            {
            	MeetLog.i(getClass().getName(),"onTouchEvent","right");
                return super.onTouchEvent(event);
            }
            if (difference != null && mCurrentView.onLeftSide() && difference[0] > 0) //move left
            {
            	MeetLog.i(getClass().getName(),"onTouchEvent","left");
                return super.onTouchEvent(event);
            }
            if (difference == null && ( mCurrentView.onLeftSide() || mCurrentView.onRightSide()))
            {
                return super.onTouchEvent(event);
            }
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if ((event.getAction() & CompatibleUtile.getActionMask()) == MotionEvent.ACTION_UP){
            super.onInterceptTouchEvent(event);
        }
        float [] difference = handleMotionEvent(event);
        if(mCurrentView == null){
        	return super.onInterceptTouchEvent(event);
        }
        if (mCurrentView.pagerCantScroll()) {
            return super.onInterceptTouchEvent(event);
        }else {
            if (difference != null && mCurrentView.onRightSide() && difference[0] < 0) //move right
            {
                return super.onInterceptTouchEvent(event);
            }
            if (difference != null && mCurrentView.onLeftSide() && difference[0] > 0) //move left
            {
                return super.onInterceptTouchEvent(event);
            }
            if (difference == null && ( mCurrentView.onLeftSide() || mCurrentView.onRightSide()))
            {
                return super.onInterceptTouchEvent(event);
            }
        }
        return false;
    }

	public void setSelectedView(DragImageView mSelectedView) {
		this.mSelectedView = mSelectedView;
	}
	public DragImageView getSelectedView() {
		return mSelectedView;
	}

}
