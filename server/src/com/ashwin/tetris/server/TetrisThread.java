package com.ashwin.tetris.server;

import java.io.IOException;
import java.util.HashMap;

import com.ashwin.tetris.command.Command;
import com.ashwin.tetris.command.Player;
import com.ashwin.tetris.command.PlayerData;

// TetrisTheads are the game threads that ferry information from one player to the
// other player. It starts and stops the game and automatically updates player data
// once the game has finished.
public class TetrisThread extends Thread {
	
	private Player _p1, _p2;
	private boolean _isRunning;
	
	public TetrisThread(Player p1, Player p2) {
		_p1 = p1;
		_p2 = p2;
	}
	
	@Override
	public void start() {
		super.start();
		_isRunning = true;
	}
	
	@Override
	public void run() {
		try {
			_p1.sendCommand(Command.BEGIN, "Begin playing");
			_p2.sendCommand(Command.BEGIN, "Begin playing");
			
			while(_isRunning) {
				if(_p1.getInputStream().available() > 0)
					handleInput(_p1, _p2);
				if(_p2.getInputStream().available() > 0)
					handleInput(_p2, _p1);
			}	
		} catch(IOException e) {
			System.out.println("TetrisThread@run: " + e.toString());
			e.printStackTrace();
		}
	}
	
	// Sender is the player who sent the command, receiver is the
	// player who will be receiving output.
	private void handleInput(Player sender, Player receiver) throws IOException {
		HashMap<Command, String> commands = sender.read();
		for(Command cmd : commands.keySet()) {
			switch(cmd) {
				// Program treats disconnects and loses/wins the same. Tell the receiver that
				// it was a disconnect and then increment their various statistics.
				case DISCONNECT: receiver.sendCommand(Command.DISCONNECT, commands.get(cmd));
				case LOSE:		 
					PlayerData senderData = sender.getPlayerData();
					PlayerData receiverData = receiver.getPlayerData();
					
					// Rank is auto incremented in the setDivision method of the PlayerData class
					senderData.setLoses(senderData.getLoses() + 1);
					senderData.setDivision(senderData.getDivision() - 1);
					receiverData.setWins(receiverData.getWins() + 1);
					receiverData.setDivision(receiverData.getDivision() + 1);
					
					// Tell the other player that they won
					receiver.sendCommand(Command.WIN, commands.get(cmd)); break;
				case LINE:		 receiver.sendCommand(Command.LINE, commands.get(cmd)); break;
				case MSG:		 receiver.sendCommand(Command.MSG, commands.get(cmd)); break;
			}
		}
	}

	public void stopRunning() {
		_isRunning = false;
	}
}
