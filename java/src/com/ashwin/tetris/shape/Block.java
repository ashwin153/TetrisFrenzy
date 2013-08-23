package com.ashwin.tetris.shape;

import java.awt.Color;
import java.util.ArrayList;

import com.ashwin.tetris.java.TetrisPanel;

public class Block {

	private int _row, _col;
	private Color _color;
	
	public Block(Color color, int row, int col) {
		_color = color;
		_row = row;
		_col = col;
	}
	
	public void setRow(int row) {
		_row = row;
	}
	
	public void setCol(int col) {
		_col = col;
	}
	
	public int getRow() {
		return _row;
	}
	
	public int getCol() {
		return _col;
	}
	
	public Color getColor() {
		return _color;
	}
	
	public void moveTo(int row, int col) {
		_row = row;
		_col = col;
	}
	
	public void moveForward() {
		_row++;
	}
	
	public void moveLeft() {
		_col--;
	}
	
	public void moveRight() {
		_col++;
	}
	
	public void setColor(Color color) {
		_color = color;
	}
	
	public boolean canMoveForward(ArrayList<Block> othBlocks) {
		if(this.getRow() + 1 >= TetrisPanel.MAX_ROW)
			return false;
		
		for(Block oth : othBlocks)
			if(this.getRow() == oth.getRow() - 1 &&
				this.getCol() == oth.getCol())
				return false;
		return true;
	}
	
	public boolean canMoveLeft(ArrayList<Block> othBlocks) {
		if(this.getCol() - 1 < 0)
			return false;
		
		for(Block oth : othBlocks)
			if(this.getRow() == oth.getRow() &&
				this.getCol() == oth.getCol() + 1)
				return false;
		return true;
	}
	
	public boolean canMoveRight(ArrayList<Block> othBlocks) {
		if(this.getCol() + 1 >= TetrisPanel.MAX_COL)
			return false;
				
		for(Block oth : othBlocks)
			if(this.getRow() == oth.getRow() &&
				this.getCol() == oth.getCol() - 1)
				return false;
		return true;
	}
}
