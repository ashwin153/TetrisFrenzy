package com.ashwin.tetris.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.ashwin.tetris.command.Command;
import com.ashwin.tetris.command.Player;
import com.ashwin.tetris.command.PlayerData;
import com.ashwin.tetris.database.DBConnect;

// ConnectThread is responsible for accepting and authenticating clients when
// they connect to the server. ConnectThread creates Player objects that populate
// the concurrent ArrayList _players used by all threads to manage active players.
// In addition to being the entrance to the program, ConnectThread also acts as the
// exit. It closes the ServerSocket and Log on termination.

public class ConnectThread extends Thread {

	// Time that clients are given between connecting and authenticating. If it takes
	// longer than TIMEOUT_AUTHENTICATE for clients to return login information, the
	// server rejects the client.
	private static final int TIMEOUT_AUTHENTICATE = 3000;
	
	private PreparedStatement _authStatement, _createAuthStatement, _createUserStatement;
	private boolean _isRunning;
	private ServerSocket _serverSocket;
	private List<Player> _players;
	private Connection _connection;
	
	public ConnectThread(ServerSocket serverSocket, List<Player> players) throws SQLException, ClassNotFoundException {
		_players = players;
		_serverSocket = serverSocket;
		
		Log.write("ConnectThread@run: Opening database connection");
		_connection = DBConnect.openConnection();
		_authStatement = _connection.prepareStatement("select auth.username, auth.uid, user.wins, user.loses, user.rid, " +
				"user.division from auth join user on auth.uid=user.uid where auth.username=? and auth.password=?");
		_createAuthStatement = _connection.prepareStatement("insert into auth (username, password, email) values (?, ?, ?)");
		_createUserStatement = _connection.prepareStatement("insert into user(wins, loses, rid, division) values (0, 0, 1, 2)");
		Log.write("ConnectThread@run: Database connection opening successfully, listening for connections");
	}
	
	@Override
	public void start() {
		super.start();
		_isRunning = true;
		
		Log.write("ConnectThread@start: Server listening for connections");
	}
	
	@Override
	public void run() {
		try {
			while (_isRunning) {
				try {
					// Block until a socket connects to the server and query the new client for authentication info
					Player player = new Player(_serverSocket.accept());
					InputStream playerInput = player.getInputStream();
					player.sendCommand(Command.AUTHENTICATE, "Who are you?");
					Log.write("ConnectThread@run: Client accepted, authenticating");
	
					// Wait until the client responds with authentication tokens or until TIMEOUT_AUTHENTICATION is reached
					boolean auth = false, waiting = true;
					long startTime = System.currentTimeMillis();
					long currentTime = startTime;
	
					while (waiting && currentTime < startTime + TIMEOUT_AUTHENTICATE) {
						currentTime = System.currentTimeMillis();
						// If the player has returned authentication tokens, read them in
						if (playerInput.available() > 0) {
							// Gets the first command in the keySet
							HashMap<Command, String> commands = player.read();
							Command cmd = commands.keySet().iterator().next();
							String[] tokens = commands.get(cmd).split(",");
	
							// If the command is CREATE_USER, then create the user and then authenticate
							switch (cmd) {
								case CREATE_USER: 
									try {
										createUser(_createAuthStatement, _createUserStatement, tokens);
										player.sendCommand(Command.CREATE_USER_SUCCESSFUL, "Successfully created user, proceeding with login");
									} catch(SQLException e) {
										player.sendCommand(Command.CREATE_USER_FAILED, e.getMessage());
										Log.write("ConnectThread@run: " + e.toString());
									}
									break;
								case AUTHENTICATE: 
									try {
										auth = login(player, _authStatement, tokens);
										System.out.println("ConnectThread@run: " + tokens[0] + ", " + tokens[1]);
										
										if(auth) {
											player.sendCommand(Command.LOGIN_SUCCESSFUL, player.getPlayerData().toString());
											_players.add(player);
											Log.write("ConnectThread@run: Player authentication successful");
										} else {
											player.sendCommand(Command.LOGIN_FAILED, "Username and password do not match records. Try again!");
											Log.write("ConnectThread@run: Username and password do not match records.");
										}
									} catch(SQLException e) {
										player.sendCommand(Command.LOGIN_FAILED, e.getMessage());
									}
									break;
							}
							
							waiting = false;
						}
					}
						
					if (waiting) {
						player.sendCommand(Command.LOGIN_FAILED,"Request timed out. Try again!");
						Log.write("ConnectThread@run: Client authentication timeout");
					}
				} catch(IOException e) {
					Log.write("ConnectThread@run: " + e.toString());
				}
			}
		} finally {
			close();
		}
	}
	
	// Create a new user in the auth and the user table. If the insert statements are successful, then the
	// server will continue with authentication. However if they are not, clients will be notified.
	private void createUser(PreparedStatement createAuthStatement, PreparedStatement createUserStatement, String[] tokens) throws SQLException {
		createAuthStatement.setString(1, tokens[0]);
		createAuthStatement.setString(2, tokens[1]);
		createAuthStatement.setString(3, tokens[2]);
		createAuthStatement.executeUpdate();
		createUserStatement.executeUpdate();
	}
	
	// Logs in by making a query on the MySQL Database asking for the data for the player with the supplied
	// authentication tokens. If the query is successful and data is returned it is inferred  that the player
	// supplied valid authentication tokens. The method also puts the resulting data into memory.
	private boolean login(Player player, PreparedStatement authStatement, String[] tokens) throws SQLException {
		authStatement.setString(1, tokens[0]);
		authStatement.setString(2, tokens[1]);
		ResultSet resultSet = authStatement.executeQuery();
		boolean auth = resultSet.next();
		
		if(auth)
			player.setPlayerData(new PlayerData(resultSet));
		
		return auth;
	}
	
	// Closes the ServerSocket, Log, and Connection so that they may be used again.
	private void close() {
		try {
			_serverSocket.close();
			Log.close();
			_connection.close();
		} catch(Exception e) {
			Log.write("ConnectThread@close: " + e.toString());
		}
	}
	
	// Stops the ConnectThread from running
	public void stopRunning() {
		_isRunning = false;
	}
}
