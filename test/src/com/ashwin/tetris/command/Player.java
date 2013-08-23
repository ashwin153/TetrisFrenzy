package com.ashwin.tetris.command;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class Player {

	private Socket _socket;
	private DataInputStream _socketIn;
	private DataOutputStream _socketOut;
	private PlayerData _data;
	private boolean _isPlaying;
	
	public Player() {
		_socket = null;
	}
	
	public Player(String host, int port) throws IOException {
		_socket = new Socket(host, port);
		_socketIn  = new DataInputStream(_socket.getInputStream());
		_socketOut = new DataOutputStream(_socket.getOutputStream());
	}
	
	public Player(Socket socket) throws IOException {
		_socket = socket;
		_socketIn  = new DataInputStream(_socket.getInputStream());
		_socketOut = new DataOutputStream(_socket.getOutputStream());
	}
	
	public void bind(String host, int port) throws IOException {
		_socket = new Socket(host, port);
		_socketIn  = new DataInputStream(_socket.getInputStream());
		_socketOut = new DataOutputStream(_socket.getOutputStream());	
	}
	
	public boolean isPlaying() {
		return _isPlaying;
	}
	
	public void setPlaying(boolean isPlaying) {
		_isPlaying = isPlaying;
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
	
	public DataInputStream getInputStream() {
		return _socketIn;
	}
	
	public DataOutputStream getOutputStream() {
		return _socketOut;
	}
	
	public void sendCommand(Command cmd, String msg) throws IOException {
		_socketOut.writeUTF(cmd.name() + ":" + msg + ".");
	}
	
	public HashMap<Command, String> read() throws IOException {
		String contents = "";
		while(_socketIn.available() > 0)
			contents += _socketIn.readUTF();
		
		HashMap<Command, String> cmds = new HashMap<Command, String>();
		
		for(String str : contents.split("\\.")) {
			String[] line = str.split(":");
			cmds.put(Command.valueOf(line[0]), line[1]);
		}
		
		return cmds;
	}
	
	public void close() throws IOException {
		_socket.close();
		_socketIn.close();
		_socketOut.close();
	}
	
	@Override
	public String toString() {
		return _socket.toString() + ", isPlaying" + _isPlaying + ", PlayerData: " + _data.toString();
	}
}
