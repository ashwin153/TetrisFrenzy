package com.ashwin.tetris.server;

import java.io.IOException;
import java.util.List;

import com.ashwin.tetris.command.Command;
import com.ashwin.tetris.command.Player;

// Responsible for creating matches between players. Finds matches based on player rank
// and spawns TetrisThreads to play the actual games.
public class MatchmakingThread extends Thread {

	private List<Player> _matchmakingQuene;
	private boolean _isRunning;
	
	public MatchmakingThread(List<Player> matchmakingQuene) {
		_matchmakingQuene = matchmakingQuene;
	}
	
	@Override
	public void start() {
		super.start();
		_isRunning = true;
	}
	
	@Override
	public void run() {
		try {
			int lastSize = _matchmakingQuene.size();
			
			while(_isRunning) {
				int curSize = _matchmakingQuene.size();
				
				// If there is more than 1 player in the matchmakingQuene and the matchmakingQuene
				// has changed since the last iteration then iterate over it
				if(lastSize != curSize && _matchmakingQuene.size() > 1) {
					for(Player p1 : _matchmakingQuene) {
						for(Player p2 : _matchmakingQuene) {
							if(p1.getPlayerData().getRank() == p2.getPlayerData().getRank()) {
								_matchmakingQuene.remove(p1);
								_matchmakingQuene.remove(p2);
								
								p1.setPlaying(true);
								p2.setPlaying(true);
								
								p1.sendCommand(Command.OPPONENT, p2.getPlayerData().toString());
								p2.sendCommand(Command.OPPONENT, p1.getPlayerData().toString());
								
								Log.write("MatchmakingThread@run: Match found between " + p1.toString() + " and " + p2.toString());
								new TetrisThread(p1, p2);
							}
						}
					}
					
					// After the matchmakingQuene iterates and finds matches, don't iterate until
					// new players are added the quene
					curSize = lastSize = _matchmakingQuene.size();
				}
			}
		} catch(IOException e) {
			System.out.println("MatchmakingThread@run: " + e.toString());
		}
	}
	
	public void stopRunning() {
		_isRunning = false;
	}
}
