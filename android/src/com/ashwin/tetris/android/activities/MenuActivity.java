package com.ashwin.tetris.android.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ashwin.tetris.android.R;
import com.ashwin.tetris.android.R.id;
import com.ashwin.tetris.android.R.layout;
import com.ashwin.tetris.android.services.ServerConnectionService;
import com.millennialmedia.android.MMSDK;

public class MenuActivity extends Activity {

	private ServiceConnection _serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {}

		@Override
		public void onServiceDisconnected(ComponentName className) {}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		
		//MMSDK.initialize(this);
		this.bindService(new Intent(this, ServerConnectionService.class), _serviceConnection, Context.BIND_AUTO_CREATE);
		
		Typeface typeface = Typeface.createFromAsset(getAssets(), "Hobo.ttf");
		LinearLayout layout = (LinearLayout) findViewById(R.id.MenuActivity_layout);
		
		setTypeface(layout, typeface);
	}

	// Sets the typeface for a ViewGroup and all child ViewGroups
	public void setTypeface(View view, Typeface tf) {
		if(!(view instanceof ViewGroup)) {
			if(view instanceof TextView)
				((TextView) view).setTypeface(tf);
			return;
		}
		
		for(int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
			setTypeface(((ViewGroup) view).getChildAt(i), tf);
	}
	
	// Called when the login button is pressed
	public void onLogin(View view) {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}
	
	// Called when the play offline button is pressed
	public void onPlayOffline(View view) {
		Intent intent = new Intent(this, OfflineTetrisActivity.class);
		startActivity(intent);
	}
}
