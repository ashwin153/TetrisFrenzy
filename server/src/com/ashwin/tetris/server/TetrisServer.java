package com.ashwin.tetris.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ashwin.tetris.command.Player;

public class TetrisServer {
	
	private static final int PORT=5517;
	
	private ConnectThread _connectThread;
	private List<Player> _players;
	private ServerSocket _serverSocket;
	private ThreadManager _threadManager;
	
	public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
		new TetrisServer();
	}
	 
	public TetrisServer() throws IOException, SQLException, ClassNotFoundException {
		Log.setup();
		
		_serverSocket = new ServerSocket(PORT);
		Log.write("TetrisServer@constructor: Server listening at " + _serverSocket.getInetAddress() + " on " + PORT);
		
		_players = Collections.synchronizedList(new ArrayList<Player>());
		_connectThread = new ConnectThread(_serverSocket, _players);
		_threadManager = new ThreadManager(_players);
		
		_connectThread.start();
		_threadManager.start();
	}

}
