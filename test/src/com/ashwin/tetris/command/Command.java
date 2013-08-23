package com.ashwin.tetris.command;

public enum Command {

	// Authentication Commands
	AUTHENTICATE,					// Client should return: AUTHENTICATE:email,password.
	LOGIN_SUCCESSFUL,				// Server returns: LOGIN_SUCCESSFUL:PlayerData.
	LOGIN_FAILED,					// Server returns: LOGIN_FAILED:reason_of_failure
	LOGIN,							// Sever should return: AUTHENTICATE
	LOGOUT,							// Server should update database and remove socket connection
	CREATE_USER,					// Server should create a user and return the login data for the new user
	
	// Matchmaking Commands
	JOIN_GAME,						// Server should put player into matchmaking quene
	FINDING_MATCH,					// Client should update with new info
	
	// Tetris Game Commands
	OPPONENT,						// Server returns: OPPONENT:PlayerData once a match has been found
	BEGIN,							// Client should begin playing game
	DISCONNECT,						// Server should send command to other player
	LOSE,							// Server should send win command to other player
	LINE,							// Server should send lines to other player
	WIN,							// Client should display win procedure
	
	// Global Commands
	MSG,							// Server should send message to other player
	PLAYER_DATA;					// Server should return: PLAYER_DATA:PlayerData
}
