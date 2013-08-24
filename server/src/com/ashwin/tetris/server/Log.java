package com.ashwin.tetris.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
	private static SimpleDateFormat _dateFormat = new SimpleDateFormat("MMM dd hh:mm - ");
	private static PrintWriter _writer;
	
	public static boolean isNull() {
		return _writer == null;
	}
	
	public static void setup(String fileName, String filePath) {
		try {
			_writer = new PrintWriter(new FileOutputStream(new File(fileName + filePath), true));
		} catch(Exception e) {
			System.out.println("Log@setup: Unable to create log file - " + e.toString());
		}
	}
	
	public static void write(String str) {
		if(_writer != null) {
			_writer.println(_dateFormat.format(new Date(System.currentTimeMillis())) + str);
			_writer.flush();
		}
	}
	
	public static void close() {
		if(_writer != null)
			_writer.close();
	}
}
