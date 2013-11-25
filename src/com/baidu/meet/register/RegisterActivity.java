package com.baidu.meet.register;

import com.baidu.meet.R;
import com.baidu.meet.R.layout;
import com.baidu.meet.config.Config;
import com.baidu.meet.show.ShowActivity;
import com.baidu.meet.view.NavigationBar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class RegisterActivity extends Activity {
	
	private TextView mDone = null;
	private NavigationBar mNavigationBar;
	private ImageView mBack;
	
	public static void startAcitivity(Context context) {
		Intent intent = new Intent(context, RegisterActivity.class);
		context.startActivity(intent);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_activity);
		initUI();
	}

	private void initUI() {
		mNavigationBar = (NavigationBar) findViewById(R.id.navigation_bar);
		mBack = mNavigationBar.addSystemImageButton(NavigationBar.ControlAlign.HORIZONTAL_LEFT,
                NavigationBar.ControlType.BACK_BUTTON, new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }

        });
		
		mDone = mNavigationBar.addTextButton(NavigationBar.ControlAlign.HORIZONTAL_RIGHT,
                getString(R.string.register), new OnClickListener() {

			@Override
			public void onClick(View v) {
				ShowActivity.startAcitivity(RegisterActivity.this);
			}
		});
	}

}
