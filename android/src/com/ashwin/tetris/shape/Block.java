package com.ashwin.tetris.shape;

import java.util.List;

import com.ashwin.tetris.android.views.OnlineTetrisView_withOnlineCapabilities;

public class Block {

	private int _row, _col;
	private int _color;
	
	public Block(int color, int row, int col) {
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
	
	public int getColor() {
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
	
	public void setColor(int color) {
		_color = color;
	}
	
	public boolean canMoveForward(List<Block> othBlocks) {
		if(this.getRow() + 1 >= OnlineTetrisView_withOnlineCapabilities.MAX_ROW)
			return false;
		
		for(Block oth : othBlocks)
			if(this.getRow() == oth.getRow() - 1 &&
				this.getCol() == oth.getCol())
				return false;
		return true;
	}
	
	public boolean canMoveLeft(List<Block> othBlocks) {
		if(this.getCol() - 1 < 0)
			return false;
		
		for(Block oth : othBlocks)
			if(this.getRow() == oth.getRow() &&
				this.getCol() == oth.getCol() + 1)
				return false;
		return true;
	}
	
	public boolean canMoveRight(List<Block> othBlocks) {
		if(this.getCol() + 1 >= OnlineTetrisView_withOnlineCapabilities.MAX_COL)
			return false;
				
		for(Block oth : othBlocks)
			if(this.getRow() == oth.getRow() &&
				this.getCol() == oth.getCol() - 1)
				return false;
		return true;
	}
	
	@Override
	public String toString() {
		return _color + "," + _row + "," + _col;
	}
	
	public Block fromString(String str) {
		String[] tokens = str.split(",");
		return new Block(Integer.parseInt(tokens[0]),
						 Integer.parseInt(tokens[1]),
						 Integer.parseInt(tokens[2]));
	}
}
