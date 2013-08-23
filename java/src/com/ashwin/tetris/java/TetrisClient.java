package com.ashwin.tetris.java;

import javax.swing.JFrame;

public class TetrisClient extends JFrame {
	
	public TetrisClient(String host, int port) {
		setContentPane(new TetrisPanel(host, port));
		
		this.setTitle("TetrisClient - Ashwin Madavan");
		this.setLocation(100, 100);
		this.setSize(750, 1250);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}
	
	public static void main(String[] args) {
		new TetrisClient("localhost", 2238);
	}
}
