package com.baidu.meet;

import com.baidu.meet.imageLoader.AsyncImageLoader;
import com.baidu.meet.imageLoader.AsyncImageLoader.ImageCallback;
import com.baidu.meet.model.BaseLoadDataCallBack;
import com.baidu.meet.model.RegisterModel;
import com.baidu.meet.util.UtilHelper;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class LogoActivity extends Activity {
	private RegisterModel mRegisterModel = null;
	private TextView title;
	private TextView register;
	private ImageView image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logo_activity);
		initUI();
		initData();
	}

	private void initUI() {
		title = (TextView) this.findViewById(R.id.title);
		register = (TextView) this.findViewById(R.id.register);
		register.setOnClickListener(mCommonListener);
		
		image = (ImageView) this.findViewById(R.id.image_view);
	}

	private void initData() {
		mRegisterModel = new RegisterModel();
		mRegisterModel.setLoadDataCallBack(mLoadDataCallBack);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.logo, menu);
		return true;
	}

	private BaseLoadDataCallBack mLoadDataCallBack = new BaseLoadDataCallBack() {

		@Override
		public void callback(Object result) {
			UtilHelper.showToast(LogoActivity.this, "done");
		}

	};

	private OnClickListener mCommonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			testResgister();
		}
	};
	private ImageCallback mImageCallback = new ImageCallback() {

		@Override
		public void imageLoaded(Bitmap bitmap, String imageUrl, boolean isCached) {
			image.setImageBitmap(bitmap);
			image.invalidate();
		}
	};

	private void testResgister() {
		// mRegisterModel.register();
		AsyncImageLoader loader = new AsyncImageLoader(this);
		String url = "http://img3.cache.netease.com/photo/0001/2013-11-08/900x600_9D6O343S19BR0001.jpg";

		String urlTest = "http://d.pcs.baidu.com/thumbnail/860a7a71331725fb1bc93fb8e65d7f1d?fid=138269170-250528-3752294460&time=1383986317&rt=pr&sign=FDTAR-DCb740ccc5511e5e8fedcff06b081203-3rqtnKGHARQCuh%2BQuJlcMehVEvQ%3D&expires=8h&r=209599956&size=c850_u580&quality=100";
		loader.loadImage(urlTest, mImageCallback);
	}

}
