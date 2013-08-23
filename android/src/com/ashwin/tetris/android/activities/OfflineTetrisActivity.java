package com.ashwin.tetris.android.activities;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ashwin.tetris.android.R;
import com.ashwin.tetris.android.views.TetrisScoreView;
import com.ashwin.tetris.android.views.TetrisView;

public class OfflineTetrisActivity extends Activity {

	private TetrisScoreView _scoreView;
	private TetrisView _tetrisView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_offline_tetris);
		
		_tetrisView = (TetrisView) findViewById(R.id.OfflineTetrisActivity_TetrisView);
		_scoreView = (TetrisScoreView) findViewById(R.id.OfflineTetrisActivity_TetrisScoreView);
		_tetrisView.setTetrisScoreView(_scoreView);
		
		Typeface typeface = Typeface.createFromAsset(getAssets(), "Hobo.ttf");
		LinearLayout layout = (LinearLayout) findViewById(R.id.OfflineTetrisActivity_layout);
		
		setTypeface(layout, typeface);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		_tetrisView.pause();
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
}
