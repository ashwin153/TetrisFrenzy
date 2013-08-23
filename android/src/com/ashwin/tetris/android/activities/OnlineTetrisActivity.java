package com.ashwin.tetris.android.activities;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ashwin.tetris.android.R;
import com.ashwin.tetris.android.views.TetrisScoreView;
import com.ashwin.tetris.android.views.TetrisView;
import com.ashwin.tetris.command.PlayerData;

public class OnlineTetrisActivity extends Activity {

	private TetrisView _tetrisView, _opponentTetrisView;
	private TetrisScoreView _tetrisScoreView;
	
	private PlayerData _playerData, _opponentData;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_online_tetris);
		
		_playerData = PlayerData.fromString(this.getIntent().getStringExtra("player_data"));
		_opponentData = PlayerData.fromString(this.getIntent().getStringExtra("opponent_data"));
		
		TextView uName = (TextView) findViewById(R.id.OnlineTetrisActivity_user_name);
		RatingBar uDivision = (RatingBar) findViewById(R.id.OnlineTetrisActivity_user_division);
		TextView oName = (TextView) findViewById(R.id.OnlineTetrisActivity_opponent_name);
		RatingBar oDivision = (RatingBar) findViewById(R.id.OnlineTetrisActivity_opponent_division);
		
		uName.setText(_playerData.getUsername() + "(" + _playerData.getRank() + ")");
		uDivision.setRating(_playerData.getDivision());
		oName.setText(_opponentData.getUsername() + "(" + _opponentData.getRank() + ")");
		oDivision.setRating(_opponentData.getDivision());
		
		_tetrisScoreView = (TetrisScoreView) findViewById(R.id.OnlineTetrisActivity_tetrisScoreView);
		_tetrisView = (TetrisView) findViewById(R.id.OnlineTetrisActivity_tetrisView);
		_opponentTetrisView = (TetrisView) findViewById(R.id.OnlineTetrisActivity_opponentTetrisView);
		_tetrisView.setTetrisScoreView(_tetrisScoreView);
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.OnlineTetrisActivity_layout);
		Typeface typeface = Typeface.createFromAsset(getAssets(), "Hobo.ttf");
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
	
	@Override
	public void onPause() {
		super.onPause();
		_tetrisView.pause();
		_opponentTetrisView.pause();
	}
}
