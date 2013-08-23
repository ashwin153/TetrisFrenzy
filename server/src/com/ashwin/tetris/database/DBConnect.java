package com.ashwin.tetris.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {
	
	private static final String DB_HOST = "localhost";
	private static final String DB_NAME = "tetris_frenzy";
	private static final String DB_USERNAME = "tetris";
	private static final String DB_PASSWORD = "tetrisfrenzy";

	public static Connection openConnection() throws SQLException, ClassNotFoundException {
		
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection("jdbc:mysql://"+DB_HOST+"/"+DB_NAME+
				"?user="+DB_USERNAME+"&password="+DB_PASSWORD);
	}
	
}
