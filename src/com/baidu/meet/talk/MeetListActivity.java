package com.baidu.meet.talk;

import com.baidu.meet.R;
import com.baidu.meet.config.RequestResponseCode;

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
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case RequestResponseCode.REQUEST_CHAT_SELECT:
//                String id = data.getStringExtra(AtListActivity.ID);
//                String name = data.getStringExtra(AtListActivity.NAME);
//                String portrait = data.getStringExtra(AtListActivity.PORTRAIT);
//                if (name != null && id != null) {
//                    ChatActivity.startChatActivity(ChatListActivity.this, id,
//                            name, portrait, null, null);
//                }
                break;
            }
        }
    }

}
