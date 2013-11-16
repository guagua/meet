package com.baidu.meet.register;

import com.baidu.meet.R;
import com.baidu.meet.R.layout;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;

public class RegisterActivity extends Activity {
	
	public static void startAcitivity(Context context) {
		Intent intent = new Intent(context, RegisterActivity.class);
		context.startActivity(intent);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_activity);
	}

}
