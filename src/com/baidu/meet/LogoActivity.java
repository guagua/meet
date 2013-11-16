package com.baidu.meet;

import com.baidu.meet.config.Config;
import com.baidu.meet.config.RequestResponseCode;
import com.baidu.meet.imageLoader.AsyncImageLoader;
import com.baidu.meet.imageLoader.AsyncImageLoader.ImageCallback;
import com.baidu.meet.model.BaseLoadDataCallBack;
import com.baidu.meet.model.RegisterModel;
import com.baidu.meet.register.EditHeadActivity;
import com.baidu.meet.register.RegisterActivity;
import com.baidu.meet.util.NavigationBar;
import com.baidu.meet.util.UtilHelper;
import com.baidu.meet.util.WriteUtil;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class LogoActivity extends Activity {
	private RegisterModel mRegisterModel = null;
	private Button register;
	private ImageView image;
	private AlertDialog mSelectImageDialog = null;
	private NavigationBar mNavi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logo_activity);
		initUI();
		initData();
	}

	private void initUI() {
		mNavi = (NavigationBar) this.findViewById(R.id.navigation_bar);
		mNavi.setTitleText(R.string.meet_you);
		register = (Button) this.findViewById(R.id.register);
		register.setOnClickListener(mCommonListener);
	}

	private void initData() {
		mRegisterModel = new RegisterModel();
		mRegisterModel.setLoadDataCallBack(mLoadDataCallBack);
		
		initializeSelectImageDialog();
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
//		AsyncImageLoader loader = new AsyncImageLoader(this);
//		String url = "http://img3.cache.netease.com/photo/0001/2013-11-08/900x600_9D6O343S19BR0001.jpg";
//
//		String urlTest = "http://d.pcs.baidu.com/thumbnail/860a7a71331725fb1bc93fb8e65d7f1d?fid=138269170-250528-3752294460&time=1383986317&rt=pr&sign=FDTAR-DCb740ccc5511e5e8fedcff06b081203-3rqtnKGHARQCuh%2BQuJlcMehVEvQ%3D&expires=8h&r=209599956&size=c850_u580&quality=100";
//		loader.loadImage(urlTest, mImageCallback);
		mSelectImageDialog.show();
//		RegisterActivity.startAcitivity(this);
	}
	
	protected void initializeSelectImageDialog() {
		String[] items = { getString(R.string.take_photo),
				getString(R.string.album) };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.operation));
		builder.setItems(items, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					WriteUtil.takePhoto(LogoActivity.this);
//					mModel.setPicType(Config.PIC_PHOTO);
				} else if (which == 1) {
					WriteUtil.getAlbumImage(LogoActivity.this);
//					mModel.setPicType(Config.PIC_ALBUM_IMAGE);
				}
			}

		});
		if (mSelectImageDialog == null) {
			mSelectImageDialog = builder.create();
			mSelectImageDialog.setCanceledOnTouchOutside(false);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case RequestResponseCode.REQUEST_ALBUM_IMAGE:
				if (data != null) {
					EditHeadActivity.startActivityForResult(this,
							RequestResponseCode.REQUEST_ALBUM_IMAGE,
							RequestResponseCode.REQUEST_ALBUM_IMAGE_VIEW,
							data.getData(),
							EditHeadActivity.PERSON_TYPE);
				}
				break;
			case RequestResponseCode.REQUEST_CAMERA:
				EditHeadActivity.startActivityForResult(this,
						RequestResponseCode.REQUEST_CAMERA,
						RequestResponseCode.REQUEST_CAMERA_VIEW, null,
						EditHeadActivity.PERSON_TYPE);
				break;
			case RequestResponseCode.REQUEST_CAMERA_VIEW:
			case RequestResponseCode.REQUEST_ALBUM_IMAGE_VIEW:
//				mView.setPortrait(data
//						.getStringExtra(EditHeadActivity.PHOTO_RESOURCE));
//				mView.refreshGroupPhoto();
//				mView.modifyText();
				break;
			case RequestResponseCode.REQUEST_ADDRESS_VIEW:
//				mView.setAddressName(data.getStringExtra(GroupAddressEditActivity.SELECTED_ADDRESS));
//				mView.setHiddenAddress(data.getBooleanExtra(GroupAddressEditActivity.HIDDEN_ADDRESS_FLAG, false));
//				selectedAddressIndex = addressInfoData.getAddressList().indexOf(mView.getAddressName());
				break;
			default:
				break;
			}
		} else if (resultCode == Activity.RESULT_CANCELED) {
			switch (requestCode) {
			case RequestResponseCode.REQUEST_ALBUM_IMAGE_VIEW:
				WriteUtil.getAlbumImage(LogoActivity.this);
				break;
			case RequestResponseCode.REQUEST_CAMERA_VIEW:
				WriteUtil.takePhoto(LogoActivity.this);
				break;
			}
		}
	}

}
