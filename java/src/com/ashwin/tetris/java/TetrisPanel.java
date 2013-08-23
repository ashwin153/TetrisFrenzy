package com.ashwin.tetris.java;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;

import com.ashwin.tetris.command.Command;
import com.ashwin.tetris.command.Player;
import com.ashwin.tetris.shape.Block;
import com.ashwin.tetris.shape.L1;
import com.ashwin.tetris.shape.L2;
import com.ashwin.tetris.shape.Line;
import com.ashwin.tetris.shape.S1;
import com.ashwin.tetris.shape.S2;
import com.ashwin.tetris.shape.Shape;
import com.ashwin.tetris.shape.Square;
import com.ashwin.tetris.shape.T;

public class TetrisPanel extends JPanel implements KeyListener {

	public static final int MAX_ROW = 20;
	public static final int MAX_COL = 10;
	private static final int BORDER = 5;
	private static final int BLOCK_SIZE = 50;
	
	private ArrayList<Block> _blocks;
	private Shape _curShape, _nextShape;
	private Player _player;
	private GameThread _gameThread;
	
	public TetrisPanel(String host, int port) {
		_blocks = new ArrayList<Block>();
		_curShape = getRandomShape();
		_curShape.moveTo(0, 5);
		_nextShape = getRandomShape();
		_gameThread = new GameThread();
		
		try {
			Socket socket = new Socket(host, port);
			_player = new Player(socket);
			
			while(_player.getInputStream().available() <= 0);
			
			handleInput();
		} catch(IOException e) {
			e.printStackTrace();
		}
		this.requestFocus();
		this.addKeyListener(this);
		this.setFocusable(true);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_DOWN:
				if(_curShape.canMoveForward(_blocks))
					_curShape.moveForward();
				repaint();
				break;
			case KeyEvent.VK_UP:
				if(_curShape.canRotateForward(_blocks))
					_curShape.rotateForward();
				repaint();
				break;
			case KeyEvent.VK_SPACE:
				while(_curShape.canMoveForward(_blocks))
					_curShape.moveForward();
				repaint();
				break;
			case KeyEvent.VK_RIGHT:
				if(_curShape.canMoveRight(_blocks))
					_curShape.moveRight();
				repaint();
				break;
			case KeyEvent.VK_LEFT:
				if(_curShape.canMoveLeft(_blocks))
					_curShape.moveLeft();
				repaint();
				break;
			}
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		paintGrid(g);
		paintBlockArrayList(g, _curShape.getBlocks());
		paintBlockArrayList(g, _blocks);
	}

	private void paintBlockArrayList(Graphics g, ArrayList<Block> blocks) {
		Block[] blockArray = new Block[blocks.size()];
		blocks.toArray(blockArray);
		
		for (Block block : blockArray) {
			// Paints block
			Color bColor = block.getColor();
			g.setColor(bColor);
			paintBlock(g, block.getRow(), block.getCol(), 1);

			// Paints inlay
			float[] hsb = new float[3];
			Color.RGBtoHSB(bColor.getRed(), bColor.getGreen(), 
					bColor.getBlue(), hsb);
			hsb[2] *= 0.8f;
			Color color = Color.decode("" + Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
			g.setColor(color);
			paintBlock(g,block.getRow(), block.getCol(), 8);
		}
	}

	private void paintBlock(Graphics g, int row, int col, int offset) {
		g.fillRect(BORDER + col * TetrisPanel.BLOCK_SIZE + offset,BORDER + row
				* TetrisPanel.BLOCK_SIZE + offset, BLOCK_SIZE - 2 * offset, BLOCK_SIZE - 2 * offset);
	}
	
	private void paintGrid(Graphics g) {
		for (int row = 0; row < TetrisPanel.MAX_ROW; row++) {
			for (int col = 0; col < TetrisPanel.MAX_COL; col++) {
				// Paints the grid
				Color gridColor = ((col - row) % 2 != 0) ? Color.BLACK : Color.GRAY;
				g.setColor(gridColor);
				paintBlock(g, row, col, 0);
			}
		}
	}
	
	protected Shape getRandomShape() {
		switch((int) (Math.random() * 7)) {
			case 0:  return new L1(3, 12);
			case 1:  return new L2(3, 12);
			case 2:  return new Line(3, 11);
			case 3:  return new S1(3, 12);
			case 4:  return new S2(3, 12);
			case 5:  return new Square(3, 11);
			default:  return new T(3, 11);
		}
	}
	
	protected void handleInput() throws IOException {
		HashMap<Command, String> commands = _player.read();
		for(Command cmd : commands.keySet()) {
			switch(cmd) {
				case BEGIN:		 _gameThread.start();  break;
				case DISCONNECT: break;
				case WIN:		 _gameThread.stopRunning(); break;
				case LINE:		 
					for(int i = 0; i < Integer.parseInt(commands.get(cmd)); i++) 
						_gameThread.addLine(); 
					break;
				case MSG:		 break;
			}
		}
	}
	
	private class GameThread extends Thread {
		
		private int _numLines;
		private boolean _isRunning;

		@Override
		public void start() {
			super.start();
			_isRunning = true;
		}
		
		@Override
		public void run() {
			try {
				while(_isRunning) {
					if(_curShape.canMoveForward(_blocks))
						_curShape.moveForward();
					else {
						_blocks.addAll(_curShape.getBlocks());
						_curShape = _nextShape;
						_curShape.moveTo(0, 5);
						_nextShape = getRandomShape();
					}
					
					if(hasLost())
						_player.sendCommand(Command.LOSE, "I lose!");
					
					if(_player.getInputStream().available() > 0)
						handleInput();
					
					int numLinesRemoved = removeRows();
					if(numLinesRemoved > 1)
						_player.sendCommand(Command.LINE, "" + numLinesRemoved);
					
					repaint();
					Thread.sleep(600);
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		private boolean hasLost() {
			for(Block block : _blocks)
				if(block.getRow() < 0)
					return true;
			return false;
		}

		private void addLine() {
			for(Block block : _blocks)
				block.setRow(block.getRow() - 1);
			
			int row = TetrisPanel.MAX_ROW - _numLines;
			for(int i = 0; i < TetrisPanel.MAX_COL; i++)
				_blocks.add(new Block(Color.LIGHT_GRAY, row, i));
			
			_numLines++;
		}
		
		public void stopRunning() {
			_isRunning = false;
		}
		
		private int removeRows() {
			ArrayList<ArrayList<Block>> grid = new ArrayList<ArrayList<Block>>();
			int numLinesRemoved = 0;
			
			for(int row = 0; row < TetrisPanel.MAX_ROW - _numLines; row++) {
				grid.add(row, new ArrayList<Block>());
				for(Block block : _blocks)
					if(block.getRow() == row)
						grid.get(row).add(block);
			}
			
			for(int row = 0; row < grid.size(); row++) {
				ArrayList<Block> line = grid.get(row);
				if(line.size() == TetrisPanel.MAX_COL) {
					for(int i = 0; i < row; i++)
						for(int j = 0; j < grid.get(i).size(); j++)
							grid.get(i).get(j).moveForward();
						_blocks.removeAll(line);
						
					numLinesRemoved++;
				}
			}
			
			return numLinesRemoved;
		}
	}
}
