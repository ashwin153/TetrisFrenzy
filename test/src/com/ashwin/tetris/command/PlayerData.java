package com.ashwin.tetris.command;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerData {

	private static final int MAX_RANK = 20;
	private static final int MIN_RANK = 1;
	
	private int _uid, _wins, _loses, _division, _rank;
	private String _username;
	
	public PlayerData(ResultSet resultSet) throws SQLException {
		_username = resultSet.getString(1);
		_uid = resultSet.getInt(2);
		_wins = resultSet.getInt(3);
		_loses = resultSet.getInt(4);
		_rank = resultSet.getInt(5);
		_division = resultSet.getInt(6);
	}
	
	public PlayerData(String serverResponse) {
		String[] tokens = serverResponse.split(",");
		_username = tokens[0];
		_uid = Integer.parseInt(tokens[1]);
		_wins = Integer.parseInt(tokens[2]);
		_loses = Integer.parseInt(tokens[3]);
		_rank = Integer.parseInt(tokens[4]);
		_division = Integer.parseInt(tokens[5]);
	}
	
	public PlayerData(String username, int uid, int wins, int loses, int rank, int division) {
		_username = username;
		_uid = uid;
		_wins = wins;
		_loses = loses;
		_rank = rank;
		_division = division;
	}
	
	public String getUsername() {
		return _username;
	}
	
	public int getUID() {
		return _uid;
	}
	
	public int getWins() {
		return _wins;
	}
	
	public int getLoses() {
		return _loses;
	}
	
	public int getDivision() {
		return _division;
	}
	
	public int getRank() {
		return _rank;
	}
	
	public void setUsername(String username) {
		_username = username;
	}
	
	public void setUID(int uid) {
		_uid = uid;
	}
	
	public void setWins(int wins) {
		_wins = wins;
	}
	
	public void setLoses(int loses) {
		_loses = loses;
	}
	
	public void setDivision(int division) {
		_division = division;
		
		// If the division is less than zero, rank down. If the
		// division is greater than five, rank up.
		if(division < 0 && this.getRank() > MIN_RANK) {
			_rank--;
			_division = 2;
		} else if(division > 5 && this.getRank() < MAX_RANK) {
			_rank++;
			_division = 2;
		} else if(division < 0) {
			_division = 0;
		} else if(division > 5) {
			division = 5;
		}
	}
	
	public void setRank(int rank) {
		_rank = rank;
	}
	
	@Override
	public String toString() {
		return _username+","+_uid+","+_wins+","+_loses+","+_rank+","+_division;
	}
	
	public static PlayerData fromString(String str) {
		String[] values = str.split(",");
		return new PlayerData(values[0], Integer.valueOf(values[1]), Integer.valueOf(values[2]), Integer.valueOf(values[3]), 
				Integer.valueOf(values[4]), Integer.valueOf(values[5]));
	}
}
