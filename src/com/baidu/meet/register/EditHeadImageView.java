package com.baidu.meet.register;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

import com.baidu.meet.R;
import com.baidu.meet.config.Config;
import com.baidu.tieba.compatible.CompatibleUtile;

public class EditHeadImageView extends DragImageView {

	private static final int MASK_ALPHA = 153;// 60%
	private Paint maskPaint = null;
	private Paint linePaint = null;
	private int maskTop = 0;
	private int maskBottom = 0;
	private float maskOffset = 0.428571429f;
	private int mBackColor = 0;

	public EditHeadImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public EditHeadImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public EditHeadImageView(Context context) {
		super(context);
		init();
	}

	private void init() {
		maskPaint = new Paint();
		maskPaint.setColor(Color.BLACK);
		maskPaint.setAlpha(MASK_ALPHA);
		linePaint = new Paint();
		linePaint.setStyle(Style.STROKE);
		linePaint.setColor(Color.WHITE);
		mBackColor = getResources().getColor(R.color.editimage_bg);
		setDrawingCacheEnabled(true);
		setImageMode(DragImageView.HEAD_MODE);
		CompatibleUtile.getInstance().noneViewGpu(this);
	}

	@Override
	public void setImageBitmap(Bitmap bitmap) {
		super.setImageBitmap(bitmap);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		maskTop = (int) (((bottom - top) - (right - left)) * maskOffset);
		maskBottom = (int) (((bottom - top) - (right - left)) * (1 - maskOffset));
		setOffset(0, maskTop, 0, maskBottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(mBackColor);
		super.onDraw(canvas);
		canvas.restore();
		canvas.drawRect(0, 0, getWidth(), maskTop, maskPaint);
		canvas.drawRect(0, getHeight() - maskBottom, getWidth(), getHeight(),
				maskPaint);
		canvas.drawRect(0, maskTop, getWidth() - 1, getHeight() - maskBottom,
				linePaint);
	}

	public Bitmap getHeadBitmap(int type) {
		Bitmap headBitmap = null;
		try {
			Bitmap cacheBitmap = getVisableBitmap();
			if (cacheBitmap != null) {
				cacheBitmap = Bitmap.createBitmap(cacheBitmap, 0, maskTop,
						getWidth(), getWidth());
				if (type == EditHeadActivity.PERSON_TYPE) {
					headBitmap = Bitmap.createScaledBitmap(cacheBitmap,
							Config.HEAD_IMG_SIZE, Config.HEAD_IMG_SIZE, false);
				}else {//群头像不需要压缩
					headBitmap = cacheBitmap;
				}
				if (headBitmap != cacheBitmap) {
					cacheBitmap.recycle();
				}
			}
		} catch (Exception e) {
		}
		return headBitmap;
	}

}
