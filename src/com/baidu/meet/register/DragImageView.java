package com.baidu.meet.register;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import com.baidu.meet.util.BdUtilHelper;
import com.baidu.meet.util.BitmapHelper;
import com.baidu.tieba.compatible.CompatibleUtile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;

/**
 * 拖拽图片控件
 * @author zhaolin02
 *
 */
public class DragImageView extends ImageView {
	
	public static final int DEFAULT_MODE = 0;
	public static final int HEAD_MODE = 1;
	private static final float ZOOM_IN_MULTIPLE = 1.25f;
	private static final int MAX_IMAGE_SIZE = 1300;
	private static final float STRETCH_FULL_SCALE = 0.2f;
	private Matrix mMatrix = new Matrix();
	private int mViewWidth = 0, mViewHeight = 0;
	private float mResizedWidth,mResizedHeight;
	private ArrayList<Float> mScale;
	private float mInitScale;
	private float mMaxScale;
	private float mCurrentScale;
	private float mOldScale;
	private boolean mIsTouched = false;
	private byte[] mImageData = null;
	private int mGifMaxUseableMem = 0;
	private int mMaxZoomInSize = MAX_IMAGE_SIZE;
	private int mImageMode = 0;
	
	private OnSizeChangedListener mListener = null;
	private OnClickListener mClick = null;
	private OnGifSetListener mOnGifSetListener = null;
	private DecelerateAnimation mAnimation;
	private GestureDetector mGestureDetector; 
	private float mOldDist = 1f;
	static final int NORMAL = 0;
	static final int DRAG = 1;
    static final int ZOOM = 2;
    public static final int IMAGE_TYPE_STATIC = 0;
    public static final int IMAGE_TYPE_DYNAMIC = 1;
    public static final int IMAGE_TYPE_DEFAULT = 2;
    private int mImageType = IMAGE_TYPE_STATIC;

	private int mMode = NORMAL;
	private boolean mHaveMove = false;
	private boolean mHaveZoom = false;
	
	//Gif 相关
	public static final int DECODE_STATUS_UNDECODE = 0;
	public static final int DECODE_STATUS_DECODING = 1;
	public static final int DECODE_STATUS_DECODED = 2;
	public static final int GIF_STATIC = 0;
	public static final int GIF_DYNAMIC = 1;
	public volatile int mDecodeStatus = DECODE_STATUS_UNDECODE;
	private int mGifType = GIF_STATIC;
	private volatile GifDecoder mGifDecoder = null;
	private Bitmap mGifCache = null;
	private int mGifIndex = 0;
	private volatile long time = 0;
	private Paint mPaint = new Paint(Color.BLACK);
	private boolean mPlayFlag = false;
	private int mTop = 0;
	private int mBottom = 0;
	private int mTopOffset = 0;
	private int mBottomOffset = 0;
	
	private Interpolator mDecelerateInterpolater = AnimationUtils.loadInterpolator(this.getContext(), android.R.anim.decelerate_interpolator);
	
	public DragImageView(Context context) {
		super(context);
		initData();
	}
	
	public DragImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initData();
	}
	public DragImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initData();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return super.dispatchTouchEvent(event);
	}
		
	public int getImageType() {
		return mImageType;
	}
	
	public void setImageMode(int mode){
		this.mImageMode = mode;
	}
	
	public void setOffset(int left, int top, int right, int bottom) {
		this.mTop = top;
		this.mBottom = bottom;
	}
	
	public void setGifMaxUseableMem(int mem){
		mGifMaxUseableMem = mem;
	}
	
	public void actionUp(){
		mHaveZoom = false;
		mIsTouched = false;
		mMode = NORMAL;
		adjustPos();
		if(mCurrentScale < mInitScale){
			mCurrentScale = mInitScale;
			resizeBitmap();
		}
	}
	
	/**
	 * 处理触屏事件
	 */
	@Override    
	public boolean onTouchEvent(MotionEvent event){  
		int action = event.getAction() & CompatibleUtile.getActionMask()/*MotionEvent.ACTION_MASK*/;    
		
		switch (action){ 
		case MotionEvent.ACTION_DOWN:
			mMode = NORMAL;
	    	mIsTouched = true;
	    	mHaveMove = false;
	    	mHaveZoom = false;
			break;
		case MotionEvent.ACTION_MOVE: 
			if(mMode == NORMAL){
				mMode = DRAG;
			}
			break;  
		case MotionEvent.ACTION_UP:
			actionUp();
			break;
		} 
		if(action == CompatibleUtile.getInstance().getActionPointerUp()){/*MotionEvent.ACTION_POINTER_UP*/
			mMode = DRAG;
		}else if(action == CompatibleUtile.getInstance().getActionPointerDown()){/*MotionEvent.ACTION_POINTER_DOWN*/
			mOldDist = spacing(event);
			if (mOldDist > 10f) {
                mMode = ZOOM;
            }
		}
		
		if (mMode != ZOOM && mGestureDetector.onTouchEvent(event)){  
			return true;
		}    
		switch (action){ 
		case MotionEvent.ACTION_MOVE: 
			if(mMode == ZOOM){
				mHaveMove = true;
				mHaveZoom = true;
				if(mImageType == IMAGE_TYPE_DYNAMIC || mImageType == IMAGE_TYPE_DEFAULT){
					break;
				}
				float newDist = spacing(event);
				if(newDist < 0){
					break;
				}
				if (Math.abs(mOldDist - newDist) < 10){
					break;
				}
				if(Math.abs(mOldDist - newDist) > 100){
					mOldDist = newDist;
					break;
				}
                float mScaleFactor = newDist / mOldDist;
                mOldDist = newDist;
                mOldScale = mCurrentScale;
                mCurrentScale *= mScaleFactor;
                if(mCurrentScale > mMaxScale){
                	mCurrentScale = mMaxScale;
                }
                if(mCurrentScale < mInitScale/4){
                	mCurrentScale = mInitScale/4;
                }
                resizeBitmap();
			}
			break;  
		} 
		return super.onTouchEvent(event);
	} 

	public float spacing(MotionEvent event) {
		return CompatibleUtile.getInstance().getSpacing(event);
		/*
		int pointer = event.getPointerCount();
		if(pointer < 2){
			return -1;
		}
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
        */
    }
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		if(changed == true){
			mViewWidth = right - left;
			mViewHeight = bottom - top;
			reInitBitmap();
		}
		super.onLayout(changed, left, top, right, bottom);
	}
	
	public boolean pagerCantScroll(){
		return mInitScale == mCurrentScale;
	}

	/**
	 * 重新初始化图片
	 */
	private void reInitBitmap(){
		Bitmap bitmap = getImageBitmap();
		if(bitmap != null && !bitmap.isRecycled()
				&& bitmap.getWidth() > 0 && bitmap.getHeight() > 0){
			if(mImageMode == DEFAULT_MODE){
				float sx = (float)(mViewWidth)/bitmap.getWidth();
				float sy = (float)(mViewHeight)/bitmap.getHeight();
				if((bitmap.getWidth() <= mViewWidth * STRETCH_FULL_SCALE 
						&& bitmap.getHeight() <= mViewHeight * STRETCH_FULL_SCALE)
						|| mImageType == IMAGE_TYPE_DEFAULT){
					mInitScale = 1f;
				}else if(bitmap.getWidth() <= mViewWidth * 0.4
						&& bitmap.getHeight() <= mViewHeight * 0.4){
					mInitScale = Math.min(sx, sy) * 0.6f;
				}else{
					mInitScale = Math.min(sx, sy);
				}
			}else{
				float sx = (float)(mViewWidth)/bitmap.getWidth();
				float sy = (float)(mViewHeight - mTop - mBottom)/bitmap.getHeight();
				mInitScale = Math.max(sx, sy);
			}
			mMaxScale =(float)mMaxZoomInSize /(bitmap.getWidth() * bitmap.getHeight());
			mMaxScale = FloatMath.sqrt(mMaxScale);
			if(mMaxScale > 10){
				mMaxScale = 10;
			}
			mScale.clear();
			mScale.add(mInitScale);
			mCurrentScale = mInitScale;
			mOldScale = mCurrentScale;
			resizeBitmap();	
			callChangeListener();
		}else{
			mResizedWidth = 0;
			mResizedHeight = 0;
			mInitScale = 1;
			mMaxScale = 1;
			mCurrentScale = 1;
			mOldScale = mCurrentScale;
			mScale.clear();
		}
	}
	
	public Bitmap getImageBitmap(){
		Drawable dr = getDrawable();
		if(dr == null || !(dr instanceof BitmapDrawable)){
			return null;
		}
		return ((BitmapDrawable)dr).getBitmap();
	}
	
	private void callChangeListener(){
		if(mListener != null){
			mListener.sizeChenged(this, canZoomIn(), canZoomOut());
		}
	}
	
	public void setOnSizeChangedListener(OnSizeChangedListener listener){
		mListener = listener;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	/**
	 * 解码gif
	 */
	public void stopDecode(){
		if(mGifDecoder != null){
			mGifDecoder.stopDecode();
			mGifDecoder = null;
		}
		mPlayFlag = false;
	}
	
	/**
	 * 停止解码
	 */
	public void decode() {
		stopDecode();
		mGifIndex = 0;
		if(mImageData == null){
			return;
		}
		mDecodeStatus = DECODE_STATUS_DECODING;
		invalidate();
		new Thread() {
			@Override
			public void run() {
				try{
					mGifDecoder = new GifDecoder(mGifMaxUseableMem);
					try{
						int type = mGifDecoder.read(new ByteArrayInputStream(mImageData));
						if(type != 0){
							return ;
						}
					}catch (Exception e) {
						stopDecode();
						return;
					}catch (OutOfMemoryError e) {
						stopDecode();
						return;
					}
					if (mGifDecoder.width == 0 || mGifDecoder.height == 0) {
						mGifType = GIF_STATIC;
					} else {
						mGifType = GIF_DYNAMIC;
					}
					if (mGifDecoder.err() == false){
						postInvalidate();
					}
					time = System.currentTimeMillis();
					mDecodeStatus = DECODE_STATUS_DECODED;
					if(mPlayFlag == true && mGifDecoder.isInterrupted() == false){
						postInvalidate();
					}
				}catch(Exception ex){
					mDecodeStatus = DECODE_STATUS_UNDECODE;
				}
			}
		}.start();
	}
	
	public byte[] getImageData(){
		return mImageData;
	}
	
	public void setImageData(byte[] data){
		mImageData = data;
	}
	
	public Bitmap getGifCache(){
		return mGifCache;
	}	
	
	public void setGifCache(Bitmap cache){
		mGifCache = cache;
	}	
	
	@Override
	protected void onDraw(Canvas canvas) {  
		super.onDraw(canvas);
		if(mImageType == IMAGE_TYPE_DYNAMIC){
			if (mGifCache == null || mGifCache.isRecycled())
				return;

			int bw = mGifCache.getWidth();
			int bh = mGifCache.getHeight();
			int wi = getWidth();
			int he = getHeight();
			int x = (wi - bw) >> 1;
			int y = (he - bh) >> 1;
			int sw = bw;
			int sh = bh;
			boolean flag = false;
			if (x >= 0 && y >= 0){
				flag = true;
			}else {
				float scale = Math.min((float) wi / bw, (float)he /bh );
				mMatrix.setScale(scale, scale);
				x = (int) ((wi - bw * scale) / 2);
				y = (int) ((he - bh * scale) / 2);
				mMatrix.postTranslate(x,y);
				sw = (int) (bw * scale);
				sh = (int) (bh * scale);
			}

			canvas.clipRect(x, y, x + sw, y + sh);
			canvas.drawColor(Color.WHITE);
			if (mDecodeStatus == DECODE_STATUS_DECODED && mGifType == GIF_DYNAMIC && mPlayFlag == true
					&& mGifDecoder != null){
				long now = System.currentTimeMillis();
				if (time + mGifDecoder.getDelay(mGifIndex) < now) {
					time += mGifDecoder.getDelay(mGifIndex);
					incrementFrameIndex();
				}
				Bitmap bitmap = mGifDecoder.getFrame(mGifIndex);
				if (bitmap != null) {
					if (flag){
						canvas.drawBitmap(bitmap, x, y, null);
					}else{
						canvas.drawBitmap(bitmap, mMatrix, mPaint);
					}
				}
				invalidate();
			}else{
				if (flag){
					canvas.drawBitmap(mGifCache, x, y, null);
				}else{
					canvas.drawBitmap(mGifCache, mMatrix, mPaint);
				}
			}
		}
	}

	private void incrementFrameIndex() {
		if(mGifDecoder == null){
			return;
		}
		mGifIndex++;
		if (mGifIndex >= mGifDecoder.getFrameCount()) {
			mGifIndex = 0;
		}
	}

	private void decrementFrameIndex() {
		if(mGifDecoder == null){
			return;
		}
		mGifIndex--;
		if (mGifIndex < 0) {
			mGifIndex = mGifDecoder.getFrameCount() - 1;
		}
	}

	public void play() {
		if(mImageType != IMAGE_TYPE_DYNAMIC){
			return;
		}
		if(mDecodeStatus == DECODE_STATUS_UNDECODE){
			decode();
		}else{
			time = System.currentTimeMillis();
			invalidate();
		}
		mPlayFlag = true;
	}

	public void pause() {
		if(mImageType != IMAGE_TYPE_DYNAMIC){
			return;
		}
		mPlayFlag = false;
		invalidate();
	}

	public void stop() {
		if(mImageType != IMAGE_TYPE_DYNAMIC){
			return;
		}
		super.setImageBitmap(null);
		mPlayFlag = false;
		stopDecode();
		mDecodeStatus = DECODE_STATUS_UNDECODE;
		mGifIndex = 0;
		invalidate();
	}
	
	public void nextFrame() {
		if (mDecodeStatus == DECODE_STATUS_DECODED) {
			incrementFrameIndex();
			invalidate();
		}
	}

	public void prevFrame() {
		if (mDecodeStatus == DECODE_STATUS_DECODED) {
			decrementFrameIndex();
			invalidate();
		}
	}
	
	/**
	 * 初始化数据
	 */
    private void initData(){
    	mMaxZoomInSize = BdUtilHelper.getEquipmentHeight(getContext())
    			* BdUtilHelper.getEquipmentWidth(getContext()) * 2;
    	if(mMaxZoomInSize < MAX_IMAGE_SIZE * MAX_IMAGE_SIZE){
    		mMaxZoomInSize = MAX_IMAGE_SIZE * MAX_IMAGE_SIZE;
    	}
    	mResizedWidth = 0;
    	mResizedHeight = 0;
    	mScale = new ArrayList<Float>();
    	mInitScale = 1;
    	mMaxScale = 1;
    	mCurrentScale = 1;
    	mOldScale = mCurrentScale;
    	setClickable(true);
    	setScaleType(ScaleType.MATRIX);
    	mAnimation = new DecelerateAnimation();
    	setHorizontalFadingEdgeEnabled(false); 
    	setVerticalFadingEdgeEnabled(false); 
    	setHorizontalScrollBarEnabled(false);
	    setVerticalScrollBarEnabled(false); 
    	setWillNotDraw(false);
	  	this.scrollTo(0, 0);
		mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener(){  
			/**
			 * 处理滑屏
			 */
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {
				// TODO Auto-generated method stub
				if(Math.abs(velocityX) > 200 || Math.abs(velocityY) > 200){
					mAnimation.prepareAnimation(velocityX, velocityY);
					DragImageView.this.startAnimation(mAnimation);
				}
				return super.onFling(e1, e2, velocityX, velocityY);
			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				// TODO Auto-generated method stub
				if(mClick != null && mHaveMove == false){
					mClick.onClick(DragImageView.this);
				}
				return super.onSingleTapUp(e);
			}

			@Override 
			public boolean onScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY) 
			{
				mHaveMove = true;
				if(mImageMode==DEFAULT_MODE&&mHaveZoom == true){
					return false;
				}
				int sx = getScrollX();
				if(mResizedWidth >= DragImageView.this.getWidth()) {
					sx += distanceX;
					if(mImageMode==DEFAULT_MODE){
						if(sx < 0){  
							sx = 0;  
						}  
						if(sx + getWidth() > mResizedWidth) {                      
							sx = (int)(mResizedWidth - getWidth());  
						} 
					}
				}
				int sy = getScrollY(); 
				if(DragImageView.this.mResizedHeight + mTop + mBottom >= DragImageView.this.getHeight()){
					sy += distanceY;
					if(mImageMode==DEFAULT_MODE){
						if (sy < -mTopOffset) {
							sy = -mTopOffset;
						}
						if (sy + getHeight() > mResizedHeight + mBottom + mBottomOffset) {
							sy = (int) (mResizedHeight - getHeight()  + mBottom + mBottomOffset);
						}
					}
				}
				if(sx != getScrollX() || sy != getScrollY()){
					scrollTo(sx, sy);
					invalidate();
				}
				return true;
			}
		});
	}
    
	private void adjustPos() {
		int sx = getScrollX();
		if (mResizedWidth >= DragImageView.this.getWidth()) {
			if (sx < 0) {
				sx = 0;
			}
			if (sx + getWidth() > mResizedWidth) {
				sx = (int) (mResizedWidth - getWidth());
			}
		}
		int sy = getScrollY();
		if (DragImageView.this.mResizedHeight + mTop + mBottom >= DragImageView.this
				.getHeight()) {
			if (sy < -mTopOffset) {
				sy = -mTopOffset;
			}
			if (sy + getHeight() > mResizedHeight + mBottom + mBottomOffset) {
				sy = (int) (mResizedHeight - getHeight() + mBottom + mBottomOffset);
			}
		}else{
			sy = 0;
		}
		if (sx != getScrollX() || sy != getScrollY()) {
			scrollTo(sx, sy);
			invalidate();
		}
	}
    
	@Override
	protected int computeHorizontalScrollRange() {
		// TODO Auto-generated method stub
		return (int)mResizedWidth;
	}

	@Override
	protected int computeVerticalScrollRange() {
		// TODO Auto-generated method stub
		return (int)mResizedHeight;
	}
	
	public boolean onRightSide(){
		if(mImageType == IMAGE_TYPE_DYNAMIC || mImageType == IMAGE_TYPE_DEFAULT){
			return true;
		}
		if(mHaveZoom == true){
			return false;
		}
		int sx = getScrollX();
		if(sx >= (int)(mResizedWidth - getWidth()) - 1){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean onLeftSide(){
		if(mImageType == IMAGE_TYPE_DYNAMIC || mImageType == IMAGE_TYPE_DEFAULT){
			return true;
		}
		if(mHaveZoom == true){
			return false;
		}
		int sx = getScrollX();
		if(sx <= 0){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 设置新图片
	 * @param bitmap
	 */
	public void setImageBitmap(Bitmap bitmap) {
		if(mAnimation.getIsAnimationInProgre()){
			mAnimation.stopAnimation();
		}
		super.setImageBitmap(bitmap);
		reInitBitmap();
		mImageType = IMAGE_TYPE_STATIC;
    	return;
    }
	
	/**
	 * 仅替换图片
	 * @param bitmap
	 */
	public void replaceImageBitmap(Bitmap bitmap) {
		Bitmap oldBitmap = getImageBitmap();
		
		if(bitmap == null || bitmap.isRecycled()){
			return;
		}
		if(oldBitmap!=null&&(oldBitmap.getWidth() !=bitmap.getWidth()||oldBitmap.getHeight()!=bitmap.getHeight())){
			setImageBitmap(bitmap);
			return;
		}
		
		if(mAnimation.getIsAnimationInProgre()){
			mAnimation.stopAnimation();
		}
		int x = getScrollX();
		int y = getScrollY();
		super.setImageBitmap(bitmap);
		super.setImageMatrix(mMatrix);
		scrollTo(x, y);
		mImageType = IMAGE_TYPE_STATIC;
    	return;
    }

	public void setGifData(byte[] data, Bitmap cache) {
		if(mAnimation.getIsAnimationInProgre()){
			mAnimation.stopAnimation();
		}
		super.setImageBitmap(null);
		stopDecode();
		reInitBitmap();
		mImageType = IMAGE_TYPE_DYNAMIC;
		mDecodeStatus = DECODE_STATUS_UNDECODE;
		mGifCache = cache;
		mImageData = data;
		if(mOnGifSetListener != null){
			mOnGifSetListener.gifSet(this);
		}
    	return;
    }
	
	public void onDestroy(){
		if(mAnimation.getIsAnimationInProgre()){
			mAnimation.stopAnimation();
		}
		super.setImageBitmap(null);
		stopDecode();
		mImageData = null;
		mDecodeStatus = DECODE_STATUS_UNDECODE;
		mGifCache = null;
		mPlayFlag = false;
	}
	
	public void release(){
		if(mAnimation.getIsAnimationInProgre()){
			mAnimation.stopAnimation();
		}
		super.setImageBitmap(null);
		stopDecode();
		mDecodeStatus = DECODE_STATUS_UNDECODE;
		mGifCache = null;
		mPlayFlag = false;
	}
	
	/**
	 * 设置新图片
	 * @param bitmap
	 */
	public void setDefaultBitmap() {
		if(mAnimation.getIsAnimationInProgre()){
			mAnimation.stopAnimation();
		}

		mImageType = IMAGE_TYPE_DEFAULT;
		reInitBitmap();
    	return;
    }
	
	/**
	 * 放大图片
	 */
	public void zoomInBitmap(){
		if(mImageType == IMAGE_TYPE_DYNAMIC || mImageType == IMAGE_TYPE_DEFAULT){
			return;
		}
		int size = mScale.size();
		if(size > 0){
			float scale = mScale.get(size - 1) * ZOOM_IN_MULTIPLE;
			mScale.add(scale);
		}else{
			mScale.add(mInitScale);
		}
		resizeBitmap();
		callChangeListener();
	}
	
	/**
	 * 缩小图片
	 */
	public void zoomOutBitmap(){
		if(mImageType == IMAGE_TYPE_DYNAMIC || mImageType == IMAGE_TYPE_DEFAULT){
			return;
		}
		int size = mScale.size();
		if(size > 1){
			mScale.remove(size - 1);
		}
		resizeBitmap();
		callChangeListener();
	}
	
	public void restoreSize(){
		callChangeListener();
		if(mImageType == IMAGE_TYPE_DYNAMIC || mImageType == IMAGE_TYPE_DEFAULT){
			return;
		}
		if(mCurrentScale != mInitScale){
			mScale.clear();
			mScale.add(mInitScale);
			mCurrentScale = mInitScale;
			mOldScale = mCurrentScale;
			resizeBitmap();
		}
	}
	
	/**
	 * 判断是否能放大
	 * @return
	 */
	public boolean canZoomIn(){
		if(mImageType == IMAGE_TYPE_DYNAMIC || mImageType == IMAGE_TYPE_DEFAULT){
			return false;
		}
		int size = mScale.size();
		Bitmap bitmap = getImageBitmap();
		if(bitmap != null && !bitmap.isRecycled() && size > 0){
			float scale = mScale.get(size - 1);
			int current_size = (int) (bitmap.getWidth() * bitmap.getHeight() * scale * scale);
			int max_size = mMaxZoomInSize;
			if(current_size * ZOOM_IN_MULTIPLE * ZOOM_IN_MULTIPLE <= max_size && scale <= 5){
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
	
	/**
	 * 判断是否能缩小
	 * @return
	 */
	public boolean canZoomOut(){
		if(mImageType == IMAGE_TYPE_DYNAMIC || mImageType == IMAGE_TYPE_DEFAULT){
			return false;
		}
		int size = mScale.size();
		Bitmap bitmap = getImageBitmap();
		if(bitmap != null && !bitmap.isRecycled()){
			if(size > 1){
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
	
	private void resizeBitmap() {
		Bitmap bitmap = getImageBitmap();
		if(bitmap == null || bitmap.isRecycled() == true){
			return;
		}
		try {
			float scale = 1f;
			if(mMode != ZOOM){
				int size = mScale.size();
				if(size > 0){
					scale = mScale.get(size - 1);
				}else{
					scale = mInitScale;
				}
				mCurrentScale = scale;
			}
			mMatrix.setScale(mCurrentScale, mCurrentScale);
			mResizedWidth = bitmap.getWidth() * mCurrentScale;
	    	mResizedHeight = bitmap.getHeight() * mCurrentScale;
	    	float oldWidth = bitmap.getWidth() * mOldScale;
	    	float oldHeight = bitmap.getHeight() * mOldScale;
	    	
	    	float x = 0, y = 0;
	    	mTopOffset = mTop;
	    	mBottomOffset = 0;
	    	if(mResizedWidth < mViewWidth){
	    		x = (int)((mViewWidth - mResizedWidth)/2);
	    	}
	    	if(mResizedHeight < mViewHeight){
	    		y = (int)((mViewHeight - mResizedHeight)/2);
	    		if(mImageMode == HEAD_MODE){
					y = y > mTop ? mTop : y;
	    		}
	    		mTopOffset = (int) (mTop - y);
	    		mBottomOffset =mTop - mTopOffset;
	    	}
	    	mMatrix.postTranslate(x, y);
			int sx = getScrollX();
			int sy = getScrollY(); 
			if(mMode == ZOOM){
				sx += (int)((mResizedWidth - oldWidth)/2);
			}
			if(mResizedWidth > getWidth()){                      
				if(sx + getWidth() > mResizedWidth){
					sx = (int)(mResizedWidth - getWidth());  
				}
			}else{
				sx = 0;
			}
			if(mMode == ZOOM){
				sy += (int)((mResizedHeight - oldHeight)/2);
			}
			if(mResizedHeight > getHeight()) {   
				if(sy + getHeight() > mResizedHeight){
					sy = (int)(mResizedHeight - getHeight());
				}
			}else{
				sy = 0;
			}
			setHorizontalScrollBarEnabled(false);
		    setVerticalScrollBarEnabled(false); 
		    if(sx < 0){
		    	sx = 0;
		    }
		    if(sy < 0){
		    	sy = 0;
		    }
			scrollTo(sx, sy);
			setImageMatrix(mMatrix);
		}
		catch(Exception ex){
		}
		return;
	}
	
	/**
	 * 实现减速度特效
	 * @author zhaolin02
	 *
	 */
    private class DecelerateAnimation extends Animation{
    	private boolean mIsAnimationInProgres;
    	private boolean mStop;
    	private long velocityX, velocityY;
    	private int mStartX, mStartY;
    	private long mTimeX, mTimeY;
    	static final long Decelerate = 2500;

    	public DecelerateAnimation(){
    		mIsAnimationInProgres = false;
    		mStop = false;
    	}
    	
    	public void prepareAnimation(float velocityX, float velocityY){
			// Configure base animation properties
    		if(velocityX > 1500){
    			velocityX = 1500;
    		}else if(velocityX < -1500){
    			velocityX = -1500;
    		}
    		if(velocityY > 1500){
    			velocityY = 1500;
    		}else if(velocityY < -1500){
    			velocityY = -1500;
    		}
    		this.velocityX = (long)velocityX;
    		this.velocityY = (long)velocityY;
    		mTimeX = (long)Math.abs(velocityX*1000/Decelerate);
    		mTimeY = (long)Math.abs(velocityY*1000/Decelerate);
    		long max_time = Math.max(mTimeX, mTimeY);
			this.setDuration(max_time);
			this.setInterpolator(mDecelerateInterpolater);
			mStartX = DragImageView.this.getScrollX();
			mStartY = DragImageView.this.getScrollY();
			mIsAnimationInProgres = true;
		}

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation transformation) {
        	// Ensure interpolatedTime does not over-shoot then calculate new offset
        	interpolatedTime = (interpolatedTime > 1.0f) ? 1.0f : interpolatedTime;
        	long time = 0;
        	float tmp = 0;
        	if(mTimeX > mTimeY){
        		time = (long)(interpolatedTime * mTimeX);
        	}else{
        		time = (long)(interpolatedTime * mTimeY);
        	}
        	int distanceX = 0;
        	tmp = ((time > mTimeX)?mTimeX:time)/(float)1000;
			if(velocityX > 0){
				distanceX = mStartX - (int) (tmp * (velocityX - Decelerate * tmp / 2));					
			}else{
				distanceX = mStartX - (int) (tmp * (velocityX + Decelerate * tmp / 2));		
			}
			
			
        	int distanceY = 0;
        	tmp = ((time > mTimeY)?mTimeY:time)/(float)1000;
        	if(velocityY > 0){
    			distanceY = mStartY - (int) (tmp * (velocityY - Decelerate * tmp / 2));
        	}else{
    			distanceY = mStartY - (int) (tmp * (velocityY + Decelerate * tmp / 2));  		
        	}
			
			if(mResizedHeight + mTop + mBottom> getHeight()) {   
				if(distanceY < -mTopOffset){
					distanceY = -mTopOffset;
				}
				if(distanceY + getHeight() > mResizedHeight + mBottom + mBottomOffset){
					distanceY = (int) (mResizedHeight - getHeight() + mBottom + mBottomOffset);
				}
			}else{
				distanceY = 0;
			}
			if(mResizedWidth > getWidth()) {   
				if(distanceX + getWidth() > mResizedWidth){
					distanceX = (int)(mResizedWidth - getWidth());
				}
				if(distanceX < 0){
					distanceX= 0;
				}
			}else{
				distanceX = 0;
			}
			DragImageView.this.scrollTo(distanceX, distanceY);
			DragImageView.this.invalidate();
        }

        @Override
        public boolean getTransformation(long currentTime, Transformation outTransformation)
        {
        	if(mStop == true){
        		mStop = false;
        		mIsAnimationInProgres = false;
        		return false;
        	}
        	// Cancel if the screen touched
        	if (mIsTouched){
        		mIsAnimationInProgres = false;
        		return false;
        	}
        	try{
	        	if (super.getTransformation(currentTime, outTransformation) == false){
	        		mIsAnimationInProgres = false;
					return false;
	        	}
        	}catch (Exception e) {
        		mIsAnimationInProgres = false;
				return false;
			}
        	return true;
        }
        
        public boolean getIsAnimationInProgre(){
        	return mIsAnimationInProgres;
        }
        
        public void stopAnimation(){
        	mStop = true;
        }
    }
    
    public void setImageOnClickListener(OnClickListener click){
    	mClick = click;
    }

    public void setGifSetListener(OnGifSetListener listener){
    	mOnGifSetListener = listener;
    }
    
	public interface OnSizeChangedListener {  
		public void sizeChenged(DragImageView view, boolean canZoomIn, boolean canZoomOut);  
	}
	
	public interface OnGifSetListener {  
		public void gifSet(DragImageView view);  
	}
	
	public Bitmap getVisableBitmap() {
		Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
				Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		int x = getScrollX();
		int y = getScrollY();
		Matrix matrix = new Matrix(mMatrix);
		matrix.postTranslate(-x, -y);
		canvas.drawBitmap(getImageBitmap(), matrix, null);
		return bitmap;
}
	
}
