package com.ashwin.tetris.command;

/**
 * Command format for all Tetris Commands
 * Command:message.
 * ex. LINE:3.
 * 
 * @author Avinash
 *
 */
public enum Command_old {

	CONNECT,
	DISCONNECT,
	BEGIN,
	LOSE,
	WIN,
	LINE,
	MSG,
	EMPTY,
	SHAPE,
	BLOCK,
	TIME,
	SCORE,
	AUTHENTICATE;
	
}
