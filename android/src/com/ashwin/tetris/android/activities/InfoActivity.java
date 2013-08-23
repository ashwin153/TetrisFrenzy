package com.ashwin.tetris.android.activities;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ashwin.tetris.android.R;
import com.ashwin.tetris.android.adapters.RecentGamesAdapter;
import com.ashwin.tetris.android.adapters.TetrisGame;
import com.ashwin.tetris.android.services.IncomingHandler;
import com.ashwin.tetris.android.services.ServerConnectionService;
import com.ashwin.tetris.android.services.ServiceConnectionClient;
import com.ashwin.tetris.command.Command;
import com.ashwin.tetris.command.PlayerData;

public class InfoActivity extends Activity {
	
	private static final String RECENT_GAMES_FILE = "recent_games.txt";
	private ServiceConnectionClient _serviceConnectionClient;
	
	private ArrayList<TetrisGame> _recentGames;
	private RecentGamesAdapter _recentGamesAdapter;
	
	private TextView _username, _wins, _loses, _totalGames, _winPercentage;
	private RatingBar _division;
	private PlayerData _playerData;
	private ListView _recentGamesList;
	
	class InfoHandler extends IncomingHandler {
		@Override
		public void handleMessage(Message msg) {
			try {
				switch(msg.what) {
					case ServerConnectionService.SERVICE_RESPONSE:
					case ServerConnectionService.SERVER_RESPONSE:
						Bundle data = msg.getData();
						for(String cmd : data.keySet())
							handleCommand(Command.valueOf(cmd), data.getString(cmd));
						break;
				}
			} catch(Exception e) {
				Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
			}
		}
		
		public void handleCommand(Command cmd, String value) throws Exception {
			switch(cmd) {
				case PLAYER_DATA:  
					_playerData = PlayerData.fromString(value);
					updateFields();
					break;
				case FINDING_MATCH:
					Toast.makeText(InfoActivity.this, "Finding a match...", Toast.LENGTH_SHORT).show();
					break;
				case OPPONENT:
					Toast.makeText(InfoActivity.this, "Found a match " + value, Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(InfoActivity.this, OnlineTetrisActivity.class);
					intent.putExtra("player_data", _playerData.toString());
					intent.putExtra("opponent_data", value);
					break;
			}
		}
	}
	
	private void updateFields() {
		try {
			Scanner input = new Scanner(this.getAssets().open(RECENT_GAMES_FILE));
			while(input.hasNext())
				_recentGames.add(TetrisGame.fromString(input.nextLine()));
		} catch(IOException e) {
			Toast.makeText(this, "InfoActivity@updateFields: " + e.toString(), Toast.LENGTH_SHORT).show();
		}
		
		_username.setText(_playerData.getUsername() + "(" + _playerData.getRank() + ")");
		_division.setRating(_playerData.getDivision());
		
		int wins = _playerData.getWins();
		int loses = _playerData.getLoses();
		
		DecimalFormat df = new DecimalFormat("##.#%");
		
		_wins.setText(String.valueOf(wins));
		_loses.setText(String.valueOf(loses));
		_totalGames.setText(String.valueOf(wins + loses));
		_winPercentage.setText(df.format((double) wins / (wins + loses)));
		_recentGamesAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		
		Typeface typeface = Typeface.createFromAsset(getAssets(), "Hobo.ttf");
		_recentGames = new ArrayList<TetrisGame>();
		_serviceConnectionClient = new ServiceConnectionClient(getApplicationContext(), new InfoHandler());
		
		_username = (TextView) findViewById(R.id.InfoActivity_user_name);
		_division = (RatingBar) findViewById(R.id.InfoActivity_user_division);
		
		_wins = (TextView) findViewById(R.id.InfoActivity_wins);
		_loses = (TextView) findViewById(R.id.InfoActivity_loses);
		_totalGames = (TextView) findViewById(R.id.InfoActivity_totalGames);
		_winPercentage = (TextView) findViewById(R.id.InfoActivity_winPercentage);
		
		_recentGamesList = (ListView) findViewById(R.id.InfoActivity_recentGames);
		_recentGamesAdapter = new RecentGamesAdapter(this, typeface, _recentGames);
		_recentGamesList.setAdapter(_recentGamesAdapter);

//		// http://docs.millennialmedia.com/android-SDK/AndroidBannerAds.html
//		MMAdView adView = new MMAdView(this);
//		//Replace YOUR_APID with the APID provided to you by Millennial Media
//		adView.setApid("YOUR_APID");
//		//Set your metadata in the MMRequest object
//		MMRequest request = new MMRequest();
//		//Add metadata here.
//		//Add the MMRequest object to your MMAdView.
//		adView.setMMRequest(request);
//		//Sets the id to preserve your ad on configuration changes.
//		adView.setId(MMSDK.getDefaultAdId());

		
		_playerData = PlayerData.fromString(this.getIntent().getStringExtra("player_data"));

		Button findMatch = (Button) findViewById(R.id.InfoActivity_findMatch);
		findMatch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				HashMap<Command, String> commands = new HashMap<Command, String>();
				commands.put(Command.JOIN_GAME, "I want to join a game.");
				try {
					_serviceConnectionClient.sendMessage(ServerConnectionService.SEND_DATA, commands);
				} catch(RemoteException e) {
					Toast.makeText(InfoActivity.this, "Cannot access matchmaking server.", Toast.LENGTH_SHORT).show();
				}
			}
		});
	
		LinearLayout layout = (LinearLayout) findViewById(R.id.InfoActivity_layout);
		
		setTypeface(layout, typeface);
		updateFields();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		_serviceConnectionClient.doBindService();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		_serviceConnectionClient.doUnbindService();
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
