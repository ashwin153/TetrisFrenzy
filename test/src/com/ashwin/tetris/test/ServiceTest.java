package com.ashwin.tetris.test;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class ServiceTest extends Service {

	public static final int REGISTER_CLIENT = 0;
	public static final int UNREGISTER_CLIENT = 1;
	public static final int GET_DATA = 2;
	
	private final Messenger _serviceMessenger = new Messenger(new InputHandler());
	private Messenger _clientMessenger;
	
	private class InputHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case REGISTER_CLIENT:
					_clientMessenger = msg.replyTo;
					break;
				case UNREGISTER_CLIENT:
					_clientMessenger = null;
					break;
				case GET_DATA:
					try {
						if(_clientMessenger != null)
							_clientMessenger.send(Message.obtain(null, GET_DATA, msg.arg1, 0));
			
					} catch(RemoteException e) {
						Log.e("ServiceTest@handleMessage", e.toString());
					}
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Toast.makeText(getApplicationContext(), "ServiceTest@onBind: Client binding", Toast.LENGTH_SHORT).show();
		return _serviceMessenger.getBinder();
	}
}
