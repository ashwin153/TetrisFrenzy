package com.ashwin.tetris.android.services;

import java.util.HashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import com.ashwin.tetris.command.Command;

public class ServiceConnectionClient {

	private Messenger _clientMessenger;
	private Messenger _serviceMessenger;
	private IncomingHandler _handler;

	private Context _context;
	private boolean _isBound;
	
	private ServiceConnection _serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			_serviceMessenger = new Messenger(service);
			try {
				Message msg = Message.obtain(null, ServerConnectionService.REGISTER_CLIENT);
				msg.replyTo = _clientMessenger;
				_serviceMessenger.send(msg);
				
			} catch (RemoteException e) {
				Toast.makeText(_context,
						"ServerConnectionServiceClient@onServiceConnected: " + e.toString(),
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			_serviceMessenger = null;
			Toast.makeText(_context,
					"ServerConnectionServiceClient@onServiceConnected: Service Disconnected",
					Toast.LENGTH_SHORT).show();
		}
	};

	public ServiceConnectionClient(Context context, IncomingHandler handler) {
		_context = context;
		_handler = handler;
		
		_clientMessenger = new Messenger(_handler);
		_serviceMessenger = null;
	}
	
	public void sendMessage(int what, HashMap<Command, String> commands) throws RemoteException {
		_handler.sendMessage(_clientMessenger, _serviceMessenger, what, commands);
	}

	public void doBindService() {
		_context.bindService(
				new Intent(_context, ServerConnectionService.class),
				_serviceConnection, Context.BIND_AUTO_CREATE);
		_isBound = true;
	}

	public void doUnbindService() {
		if (_isBound) {
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (_serviceMessenger != null) {
				try {
					Message msg = Message.obtain(null,
							ServerConnectionService.UNREGISTER_CLIENT);
					msg.replyTo = _clientMessenger;
					_serviceMessenger.send(msg);
				} catch (RemoteException e) {
					Toast.makeText(_context,
							"ServerConnectionServiceClient@onServiceConnected: Service Disconnected",
							Toast.LENGTH_SHORT).show();
				}
			}

			// Detach our existing connection.
			_context.unbindService(_serviceConnection);
			_isBound = false;
		}
	}
	
	public boolean isBound() {
		return _isBound;
	}
}
