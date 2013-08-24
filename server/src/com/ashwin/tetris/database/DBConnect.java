package com.ashwin.tetris.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {
	
	// Default values for Database variables
	private static String _host = "localhost";
	private static String _name = "TetrisFrenzy";
	private static String _user = "TetrisFrenzy_usr";
	private static String _pswd = "TetrisFrenzy123!";

	public static void setup(String host, String name,
							 String user, String pswd) {
		_host = host;
		_name = name;
		_user = user;
		_pswd = pswd;
	}
	
	public static Connection openConnection() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection("jdbc:mysql://"+_host+"/"+_name+
				"?user="+_user+"&password="+_pswd);
	}
	
}
