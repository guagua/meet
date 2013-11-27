package com.baidu.meet.talk;

import com.baidu.meet.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

public class TalkActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_talk);
	}

	public static void startChatActivity(MeetListActivity meetListActivity,
			String friendId, String friendName, String friendPortrait,
			Object object, String string) {
		Intent intent = new Intent(meetListActivity, TalkActivity.class);
		meetListActivity.startActivity(intent);
	}

}
