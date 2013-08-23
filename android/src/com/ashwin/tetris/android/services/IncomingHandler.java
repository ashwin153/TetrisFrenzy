package com.ashwin.tetris.android.services;

import java.util.HashMap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.ashwin.tetris.command.Command;

public abstract class IncomingHandler extends Handler {

	@Override
	public abstract void handleMessage(Message msg);
	
	public abstract void handleCommand(Command cmd, String value) throws Exception;
	
	public void sendMessage(Messenger sender, Messenger receiver, int what) throws RemoteException {
		Message msg = Message.obtain(null, what);
		msg.replyTo = sender;
		receiver.send(msg);
	}

	public void sendMessage(Messenger sender, Messenger receiver, int what, HashMap<Command, String> commands) throws RemoteException {
		Bundle data = new Bundle();
		for (Command cmd : commands.keySet())
			data.putString(cmd.toString(), commands.get(cmd));

		Message msg = Message.obtain(null, what);
		msg.replyTo = sender;
		msg.setData(data);
		receiver.send(msg);
	}
}
