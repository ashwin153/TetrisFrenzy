package com.ashwin.tetris.android.adapters;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TetrisGame {
	
	private static final String DATE_FORMAT = "MM/dd/yy";

	private String _opponent;
	private long _timeMillis;
	private boolean _isWon;
	
	private SimpleDateFormat _dateFormat;
	
	public TetrisGame(String opponent, long timeMillis, boolean isWon) {
		_opponent = opponent;
		_timeMillis = timeMillis;
		_isWon = isWon;
		
		_dateFormat = new SimpleDateFormat(DATE_FORMAT);
	}
	
	public String getOpponent() {
		return _opponent;
	}
	
	public long getTimeMillis() {
		return _timeMillis;
	}
	
	public boolean isWon() {
		return _isWon;
	}
	
	public String getTimeString() {
		return _dateFormat.format(new Date(_timeMillis));
	}
	
	public static TetrisGame fromString(String str) {
		String[] tokens = str.split(",");
		return new TetrisGame(tokens[0], Long.parseLong(tokens[1]), Boolean.parseBoolean(tokens[2]));
	}
	
	@Override
	public String toString() {
		return _opponent + "," + _timeMillis + "," + _isWon;
	}
}
