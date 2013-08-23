package com.ashwin.tetris.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.ashwin.tetris.command.Command;
import com.ashwin.tetris.command.Player;
import com.ashwin.tetris.command.PlayerData;
import com.ashwin.tetris.database.DBConnect;

// ThreadManager manages client requests after connection and in between games. The ThreadManager
// does not send requests for information to a player - it merely responds to their requests to
// join games, logout, or get player data.
public class ThreadManager extends Thread {

	private Connection _connection;
	private PreparedStatement _updatePlayerStatement;
	private List<Player> _players;
	private List<Player> _matchmakingQuene;
	private MatchmakingThread _matchmakingThread;
	private boolean _isRunning;
	
	public ThreadManager(List<Player> players) throws SQLException, ClassNotFoundException {
		_players = players;
		_connection = DBConnect.openConnection();
		_updatePlayerStatement = _connection.prepareStatement("update user set wins=?, loses=?, rid=?, division=? where uid=?");
		_matchmakingQuene = Collections.synchronizedList(new ArrayList<Player>());
		_matchmakingThread = new MatchmakingThread(_matchmakingQuene);
		Log.write("ThreadManager@constructor: ThreadManager instantiated correctly");
	}
	
	@Override
	public void start() {
		_matchmakingThread.start();
		Log.write("ThreadManager@start: Starting ThreadManager");
		_isRunning = true;
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		super.start();
	}
	
	@Override
	public void run() {
		try {
			while(_isRunning) {
				for(int i = 0; i < _players.size(); i++) {
					try {
						if(_players.get(i).getInputStream().available() > 0) {
							System.out.println("ThreadManager@run: Data to be read");
							handleInput(_players.get(i));
						}
					} catch(IOException e) {
						e.printStackTrace();
					} catch(SQLException e) {
						e.printStackTrace();
					}
				}
			}
		} finally {
			close();
		}
	}
	
	private void close() {
		try {
			_connection.close();
		} catch(SQLException e) {
			Log.write("ThreadManager@close: " + e.toString());
		}
	}
	
	public void stopRunning() {
		_isRunning = false;
	}
	
	// Reads in player data unless the player is currently in a game and handles their
	// requests to logout, get player data or join a game.
	private void handleInput(Player player) throws IOException, SQLException {
		if(player.isPlaying())
			return;
		
		HashMap<Command, String> commands = player.read();
		for(Command cmd : commands.keySet()) {
			switch(cmd) {
				case LOGOUT:
					PlayerData data = player.getPlayerData();
					_updatePlayerStatement.setInt(1, data.getWins());
					_updatePlayerStatement.setInt(2, data.getLoses());
					_updatePlayerStatement.setInt(3, data.getRank());
					_updatePlayerStatement.setInt(4, data.getDivision());
					_updatePlayerStatement.setInt(5, data.getUID());
					_updatePlayerStatement.executeUpdate();
					
					player.close();
					_players.remove(player);
					break;
				case PLAYER_DATA:
					player.sendCommand(Command.PLAYER_DATA, player.getPlayerData().toString());
					break;
				case JOIN_GAME:
					player.sendCommand(Command.FINDING_MATCH, "Searching for a match");
					_matchmakingQuene.add(player);
					break;
			}
		}
	}
}
