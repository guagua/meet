package com.baidu.meet.util;

import com.baidu.meet.R;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * 自定义顶部通用导航栏 <br/>
 * 支持在顶部通栏的左、中、右三个位置，自由地添加控件<br/>
 * <p>
 * 相关文档：<br />
 * 接口文档：<a target="_blank" href="http://zhaoxianlie.fe.baidu.com/javadoc/navigationbar/index.html">
 * http://zhaoxianlie.fe.baidu.com/javadoc/navigationbar/index.html</a> <br />
 * 使用方法：<a target="_blank" href="http://zhaoxianlie.fe.baidu.com/javadoc/navigationbar/demo.html">
 * http://zhaoxianlie.fe.baidu.com/javadoc/navigationbar/demo.html</a> <br />
 * </p>
 *
 * @author zhaoxianlie
 */
public class NavigationBar extends RelativeLayout {
	
	/**
	 * 内部的Click事件是否有效
	 */
	private boolean mClickIsVaild = true;
	
    /**
     * 控件类型，可认为是系统控件，目前仅两种，后续根据具体需求进行扩展
     */
    public static enum ControlType {
        /**
         * 返回按钮
         */
        BACK_BUTTON,

        /**
         * 主页按钮
         */
        HOME_BUTTON,

        /**
         * 添加聊天（比如消息页在用的）
         */
        ADD_CHAT,

        /**
         * 发帖入口（比如FRS顶部就有）
         */
        EDIT_BUTTON,

        /**
         * 【更多】按钮
         */
        MORE_BUTTON,

        /**
         * 拍照按钮
         */
        CAMERA_BUTTON

    }

    /**
     * 控件所在位置
     */
    public static enum ControlAlign {
        /**
         * 控件位于导航栏的：左侧
         */
        HORIZONTAL_LEFT,

        /**
         * 控件位于导航栏的：中间
         */
        HORIZONTAL_CENTER,

        /**
         * 控件位于导航栏的：右侧
         */
        HORIZONTAL_RIGHT

    }

    /**
     * 居左的Box
     */
    private LinearLayout mLeftBox;

    /**
     * 居中的Box
     */
    private LinearLayout mCenterBox;

    /**
     * 居右的Box
     */
    private LinearLayout mRightBox;

    /**
     * 底部的一条分割线
     */
    private TextView mNavBottomLine;

    /**
     * 当前导航栏相关联的Activity
     */
    private Activity mCurrentActivity;
    private LayoutInflater mInflater;
    private TextView mTextTitle;

    public NavigationBar(Context context) {
        super(context);
        init(context);
    }

    public NavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NavigationBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mCurrentActivity = (Activity) context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.widget_navigation_bar, this, true);
        mLeftBox = (LinearLayout) view.findViewById(R.id.leftBox);
        mCenterBox = (LinearLayout) view.findViewById(R.id.centerBox);
        mRightBox = (LinearLayout) view.findViewById(R.id.rightBox);
        mNavBottomLine = (TextView)view.findViewById(R.id.navBottomLine);

        // 设置样式
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        setGravity(Gravity.TOP);
        initPadding();
    }

    /**
     * 初始化Padding
     */
    private void initPadding() {
        setPadding(BdUtilHelper.dip2px(mCurrentActivity, getResources().getDimension(R.dimen.navi_padding_left)),
        		BdUtilHelper.dip2px(mCurrentActivity, getResources().getDimension(R.dimen.navi_padding_top)),
        		BdUtilHelper.dip2px(mCurrentActivity, getResources().getDimension(R.dimen.navi_padding_right)),
        		BdUtilHelper.dip2px(mCurrentActivity, getResources().getDimension(R.dimen.navi_padding_bottom)));
    }

    private int maxWidth = 0;
    private int containerWidth = 0;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 控件的实际宽度
        containerWidth = MeasureSpec.getSize(widthMeasureSpec);
        // 左边box的宽度
        int lw = mLeftBox.getMeasuredWidth() + getPaddingLeft();
        // 右边box的宽度
        int rw = mRightBox.getMeasuredWidth() + getPaddingRight();
        // 确定中间box的宽度，必须保证居中
        maxWidth = Math.max(lw, rw);
        mCenterBox.measure(containerWidth - maxWidth * 2 + MeasureSpec.EXACTLY,
                mCenterBox.getMeasuredHeight() + MeasureSpec.EXACTLY);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mCenterBox.layout(maxWidth, mCenterBox.getTop(), containerWidth - maxWidth, mCenterBox.getBottom());
    }

    /**
     * 设置标题：文字
     *
     * @param title 标题文字
     * @return 文字标题对应的TextView对象
     */
    public TextView setTitleText(String title) {
        if (mTextTitle == null) {
            mTextTitle = (TextView) getViewFromLayoutFile(R.layout.widget_nb_item_title);
            mCenterBox.addView(mTextTitle);
        }
        mTextTitle.setText(title);
        return mTextTitle;
    }
    
    /**
     * 设置标题：文字
     *
     * @param resId 标题文字
     * @return 文字标题对应的TextView对象
     */
    public TextView setTitleText(int resId) {
    	String text = this.mCurrentActivity.getString(resId) ;
    	
    	return this.setTitleText(text) ;
    }

    /**
     * 添加一个TitleView
     *
     * @param view     titleView对应的View对象
     * @param listener 点击事件
     * @return
     */
    public View setTitleView(View view, OnClickListener listener) {
        return addCustomView(ControlAlign.HORIZONTAL_CENTER, view, listener);
    }

    /**
     * 添加一个TitleView
     *
     * @param layoutResource titleView对应的layout文件
     * @param listener       点击事件
     * @return
     */
    public View setTitleView(int layoutResource, OnClickListener listener) {
        return addCustomView(ControlAlign.HORIZONTAL_CENTER, getViewFromLayoutFile(layoutResource), listener);
    }

    /**
     * 添加一个图片按钮，按钮具有默认的点击行为，如：返回按钮、主页按钮
     *
     * @param align 按钮在导航条中的位置：左、中、右
     * @param type  按钮类型
     * @return 按钮对象
     */
    public ImageView addSystemImageButton(ControlAlign align, ControlType type) {
        return addSystemImageButton(align, type, mOnClickListener);
    }

    /**
     * 添加一个图片按钮，并指定按钮的点击事件
     *
     * @param align    按钮在导航条中的位置：左、中、右
     * @param type     按钮类型
     * @param listener 按钮点击事件
     * @return
     */
    public ImageView addSystemImageButton(ControlAlign align, ControlType type, OnClickListener listener) {
        ImageView imageView = null;

        // 返回按钮
        if (type == ControlType.BACK_BUTTON) {
            imageView = (ImageView) getViewFromLayoutFile(R.layout.widget_nb_item_back);
        }
        // 主页按钮
        else if (type == ControlType.HOME_BUTTON) {
            imageView = (ImageView) getViewFromLayoutFile(R.layout.widget_nb_item_home);
        }

        if (imageView != null) {
            getViewGroup(align).addView(imageView);
            if (listener != null) {
                imageView.setOnClickListener(listener);
            }
        }

        return imageView;
    }

    /**
     * 添加一个文字类型的按钮
     *
     * @param align      按钮在导航条中的位置：左、中、右
     * @param buttonText 按钮文字
     * @return 按钮对象
     */
    public TextView addTextButton(ControlAlign align, String buttonText) {
        return addTextButton(align, buttonText, null);
    }
    
    public Button addRightButton(ControlAlign position, String buttonText) {
		Button btn = (Button) getViewFromLayoutFile(R.layout.widget_nb_item_stepbtn);
		btn.setText(buttonText);
		getViewGroup(position).addView(btn);
		return btn;
	}
    
	/**
	 * 设置系统的Click事件有效性 author:chenrensong
	 * 
	 * @param isVaild
	 */
	public void setSystemClickable(boolean isVaild) {
		this.mClickIsVaild = isVaild;
	}

    /**
     * 添加一个文字类型的按钮，并设置点击事件
     *
     * @param align      按钮在导航条中的位置：左、中、右
     * @param buttonText 按钮文字
     * @return 按钮对象
     */
    public TextView addTextButton(ControlAlign align, String buttonText, OnClickListener listener) {
        TextView btn = (TextView) getViewFromLayoutFile(R.layout.widget_nb_item_textbtn);
        btn.setText(buttonText);
        if (ControlAlign.HORIZONTAL_RIGHT == align) {
        	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT); 
        	int margin = (int)this.getResources().getDimension(R.dimen.navi_btn_margin_right);
            lp.setMargins(0, margin, margin, margin);
            //view要设置margin的view
            btn.setLayoutParams(lp);  
        }
        getViewGroup(align).addView(btn);
        if (listener != null) {
            btn.setOnClickListener(listener);
        }
        return btn;
    }

    /**
     * 在导航条的任意位置添加一个自定义的View
     *
     * @param align      按钮在导航条中的位置：左、中、右
     * @param customView 自定义View
     * @return View本身
     */
    public View addCustomView(ControlAlign align, View customView, OnClickListener listener) {
        getViewGroup(align).addView(customView);
        if (listener != null) {
            customView.setOnClickListener(listener);
        }
        return customView;
    }

    /**
     * 根据一个layout文件，在导航条的任意位置添加一个自定义View
     *
     * @param align            按钮在导航条的位置：左、中、右
     * @param layoutResourceId 自定义View所对应的layoutResouce文件
     * @param listener         View的点击事件
     * @return View本身
     */
    public View addCustomView(ControlAlign align, int layoutResourceId, OnClickListener listener) {
        View customView = getViewFromLayoutFile(layoutResourceId);
        return addCustomView(align, customView, listener);
    }

    /**
     * 根据align获取相应的ViewGroup，左中右
     *
     * @param align
     * @return 对应的ViewGroup
     */
    private ViewGroup getViewGroup(ControlAlign align) {
        return align == ControlAlign.HORIZONTAL_LEFT ? mLeftBox : align == ControlAlign.HORIZONTAL_CENTER ? mCenterBox : mRightBox;
    }

    /**
     * 导航栏中相关控件的点击事件处理
     */
    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
        	// 点击事件无效
        	if (!mClickIsVaild) {
        		return;
        	}
        }
    };

    /**
     * 从一个资源文件获得View
     *
     * @param layoutResource 资源文件id
     * @return 资源文件对应的View
     */
    public View getViewFromLayoutFile(int layoutResource) {
        return mInflater.inflate(layoutResource, this, false);
    }
}
