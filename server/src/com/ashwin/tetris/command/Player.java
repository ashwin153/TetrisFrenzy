package com.ashwin.tetris.command;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import com.ashwin.tetris.server.Log;
                      
public class Player {
                 
	private static final String SEPARATOR = ":";		// Separates a Command from its Message Body
	private static final String DELIMETER = "|";		// Separates multiple Commands
	        
	private Socket _socket;
	private DataInputStream _socketIn;
	private DataOutputStream _socketOut;
	private PlayerData _data;
	private boolean _isPlaying;
	       
	public Player(Socket socket) throws IOException {
		_socket = socket;
		_socketIn  = new DataInputStream(_socket.getInputStream());
		_socketOut = new DataOutputStream(_socket.getOutputStream());
	}     
	         
	public boolean isPlaying() {
		return _isPlaying;
	}        
	                    
	public void setPlaying(boolean isPlaying) {
		_isPlaying =  isPlaying;
	}
	
	public Player(Socket socket, PlayerData data) throws IOException {
		this(socket);
		_data = data;
	}
	
	public PlayerData getPlayerData() {
		return _data; 
	}
	
	public void setPlayerData(PlayerData data) {
		_data = data;     
	}
	
	public Socket getSocket() {
		return _socket;
	}
	
	public DataInputStream  getInputStream() {
		return _socketIn;
	}
	
	public DataOutputStream getOutputStream() {
		return  _socketOut;
	}
	
	public void sendCommand(Command cmd, String msg) throws IOException {
		_socketOut.writeUTF( cmd.name() + Player.SEPARATOR + msg + Player.DELIMETER);
	}
	
	public void waitForInput() throws IOException {
		while(_socketIn.available() == 0);
	}
	
	public HashMap<Command, String> read() throws IOException {
		String contents = "";
		while(_socketIn.available() > 0) 
			contents += _socketIn.readUTF();
		
		HashMap<Command,  String> cmds = new HashMap<Command, String>();
		
		for(String str : contents.split("\\" + Player.DELIMETER)) {
			String[]  line = str.split(Player.SEPARATOR );
			cmds.put(Command.valueOf(line[0]), line[1]);
		}
		
		return cmds;
	}
	
	public void close() throws IOException {
		_socket.close( );
		_socketIn.close( );
		_socketOut.close( );
	}
	
	@Override
	public String toString() {
		return _socket.toString() +  ", isPlaying" + _isPlaying + ", PlayerData: " + _data.toString();
	}
}
