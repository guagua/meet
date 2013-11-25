package com.baidu.meet.talk;

import com.baidu.meet.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class MeetListActivity extends Activity {
	public static void startAcitivity(Context context) {
		Intent intent = new Intent(context, MeetListActivity.class);
		context.startActivity(intent);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meet_list);
	}

}
