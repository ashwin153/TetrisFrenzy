package com.ashwin.tetris.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.ashwin.tetris.command.Player;
import com.ashwin.tetris.database.DBConnect;

public class TetrisServer {
	private ConnectThread _connectThread;
	private List<Player> _players;
	private ServerSocket _serverSocket;
	private ThreadManager _threadManager;
	
	public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
		new TetrisServer();
	}
	 
	public TetrisServer() throws IOException, SQLException, ClassNotFoundException {
		HashMap<String, String> properties = loadProperties();
		DBConnect.setup(properties.get("db_host"), properties.get("db_name"), 
				properties.get("db_user"), properties.get("db_pswd"));
		Log.setup(properties.get("log_name"), properties.get("log_path"));
		
		int port = Integer.valueOf(properties.get("port"));
		_serverSocket = new ServerSocket(port);
		Log.write("TetrisServer@constructor: Server listening at " + _serverSocket.getInetAddress() + " on " + port);
		
		_players = Collections.synchronizedList(new ArrayList<Player>());
		_connectThread = new ConnectThread(_serverSocket, _players);
		_threadManager = new ThreadManager(_players);
		
		_connectThread.start();
		_threadManager.start();
	}

	private HashMap<String, String> loadProperties() throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream("./config.properties"));
		HashMap<String, String> map = new HashMap<String, String>();
		
		for(String name : properties.stringPropertyNames())
			map.put(name, properties.getProperty(name));
		
		return map;
	}
}
