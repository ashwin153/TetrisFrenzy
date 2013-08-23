package com.ashwin.tetris.android.services;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.widget.Toast;

import com.ashwin.tetris.command.Command;
import com.ashwin.tetris.command.Player;
import com.ashwin.tetris.command.PlayerData;

// This class manages Socket connections with the Server
public class ServerConnectionService extends Service {
	
	public static final int REGISTER_CLIENT = 0;	// Registers a client messenger
	public static final int UNREGISTER_CLIENT = 1;	// Unregisters a client messenger
	public static final int SEND_DATA = 2;			// Send commands to server
	public static final int RECEIVE_DATA = 3;		// Ask service if there are server responses
	public static final int SERVER_RESPONSE = 4;	// Responses originating in the server
	public static final int SERVICE_RESPONSE = 5;	// Response originating in the service
	
	private static final String HOST="192.168.1.112";
	private static final int PORT=5517;
	
	private Messenger _serviceMessenger;
	private Messenger _clientMessenger;
	private Player _player;
	private ServerConnectionServiceHandler _serverConnectionHandler;
	
	@Override
	public void onCreate() {
		super.onCreate();
		_serverConnectionHandler = new ServerConnectionServiceHandler();
		_serviceMessenger = new Messenger(_serverConnectionHandler);
	}
	
	public class ServerConnectionServiceHandler extends IncomingHandler {
		@Override
	    public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
					case REGISTER_CLIENT:
						_clientMessenger = msg.replyTo;
						break;
					case UNREGISTER_CLIENT:
						_clientMessenger = null;
						break;
					case SEND_DATA:
						Bundle data = msg.getData();
						for(String cmd : data.keySet())
							handleCommand(Command.valueOf(cmd), data.getString(cmd));
						break;
					case RECEIVE_DATA:
						if(_player.getInputStream().available() > 0)
							sendMessage(_serviceMessenger, _clientMessenger, SERVER_RESPONSE, _player.read());
						break;
				}
			} catch(Exception e) {
				Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
			}
		}
		
		// Handle input form client (binds to server on login and closes socket on logout)
		public void handleCommand(Command cmd, String value) throws Exception {
			switch (cmd) {
				case LOGOUT:
					_player.sendCommand(cmd, value);
					_player.close();
					break;
				case LOGIN:
					LoginTask loginTask = (LoginTask) new LoginTask().execute(value);
					sendMessage(_serviceMessenger, _clientMessenger, SERVER_RESPONSE, loginTask.get());
					break;
				default:
					_player.sendCommand(cmd, value);
					break;
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
//		Toast.makeText(getApplicationContext(), "Binding to Client", Toast.LENGTH_SHORT).show();
		return _serviceMessenger.getBinder();
	}
	
	public class ResponseTask extends AsyncTask<Void, Void, Void> {
		private boolean _isFinished;
		
		@Override
		public void onPreExecute() {
			super.onPreExecute();
			_isFinished = false;
		}
		
		@Override
		public Void doInBackground(Void... params) {
			while(!this.isCancelled() && !_isFinished) {
				try {
					if(_player != null && _player.getInputStream() != null && 
							_serviceMessenger != null && _clientMessenger != null &&
							_player.getInputStream().available() > 0)
						_serverConnectionHandler.sendMessage(_serviceMessenger, _clientMessenger, SERVER_RESPONSE, _player.read());
				} catch(Exception e) {
					
				}
			}
			
			return null;
		}
		
		public void finish() {
			_isFinished = true;
		}
		
		public boolean isFinished() {
			return _isFinished;
		}
	}
	
	public class LoginTask extends AsyncTask<String, Void, HashMap<Command, String>> {

		@Override
		protected HashMap<Command, String> doInBackground(String... params) {
			try {
				String tokens = params[0];
				
				if (_player != null && _player.getSocket() != null)
					_player.close();
	
				_player = new Player(new Socket(HOST, PORT));
				_player.waitForInput();

				if (_player.read().get(Command.AUTHENTICATE) != null)
					_player.sendCommand(Command.AUTHENTICATE, tokens);
				_player.waitForInput();
					
				HashMap<Command, String> result = _player.read();
				
				if(result.containsKey(Command.LOGIN_SUCCESSFUL)) {
					_player.setPlayerData(PlayerData.fromString(result.get(Command.LOGIN_SUCCESSFUL)));
					new ResponseTask().execute();
				}
				return result;
			} catch(IOException e) {
				return null;
			}
		}
	}
}
