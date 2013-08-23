package com.ashwin.tetris.shape;

import java.awt.Color;
import java.util.ArrayList;

import com.ashwin.tetris.java.TetrisPanel;

public abstract class Shape {

	protected ArrayList<Block> _blocks;
	private int _rotate;
	private Color _color;
	
	public Shape(Color color, int startRow, int startCol) {
		_blocks = new ArrayList<Block>();
		_color = color;
		
		for(int i = 0 ; i < 4; i++)
			_blocks.add(new Block(_color, startRow, startCol));
		updateShape();
	}
	
	public boolean canMoveForward (ArrayList<Block> othBlocks) {
		for(Block block : _blocks) 
			if(!block.canMoveForward(othBlocks))
				return false;
		return true;
	}
	
	public boolean canMoveRight (ArrayList<Block> othBlocks) {
		for(Block block : _blocks) 
			if(!block.canMoveRight(othBlocks))
				return false;
		return true;
	}
	
	public boolean canMoveLeft (ArrayList<Block> othBlocks) {
		for(Block block : _blocks) 
			if(!block.canMoveLeft(othBlocks))
				return false;
		return true;
	}
	
	public boolean canRotateForward(ArrayList<Block> othBlocks) {
		rotateForward();
		boolean canRotate = true;
		
		for(Block block : _blocks) {
			if(block.getCol() < 0 || 
				block.getCol() >= TetrisPanel.MAX_COL ||
				block.getRow() >= TetrisPanel.MAX_ROW)
				canRotate = false;
				
			for(Block othBlock : othBlocks)
				if(block.getRow() == othBlock.getRow() &&
				   block.getCol() == othBlock.getCol())
					canRotate = false;
		}
		
		rotateBackward();
		return canRotate;
	}
	
	public boolean canRotateBackward(ArrayList<Block> othBlocks) {
		rotateBackward();
		boolean canRotate = true;
		
		for(Block block : _blocks) {
			if(block.getCol() < 0 || 
					block.getCol() >= TetrisPanel.MAX_COL ||
					block.getRow() >= TetrisPanel.MAX_ROW)
					canRotate = false;
			
			for(Block othBlock : othBlocks)
				if(block.getRow() == othBlock.getRow() &&
				   block.getCol() == othBlock.getCol())
					
					canRotate = false;
		}
		
		rotateForward();
		return canRotate;
	}
	
	private void updateShape() {
		switch(_rotate) {
			case 0:  updateShape1(); break;
			case 1:  updateShape2(); break;
			case 2:  updateShape3(); break;
			default: updateShape4(); break;
		}
	}
	
	public void rotateForward() {
		_rotate = (_rotate + 1) % 4;
		updateShape();
	}
	
	public void rotateBackward() {
		_rotate = (_rotate - 1 < 0) ? 3 : _rotate - 1;
		updateShape();
	}
	
	public void moveTo(int newRow, int newCol) {
		_blocks.get(0).moveTo(newRow, newCol);
		updateShape();
	}
	
	public void moveForward() {
		for(Block block : _blocks)
			block.moveForward();
	}
	
	public void moveLeft() {
		for(Block block : _blocks)
			block.moveLeft();
	}
	
	public void moveRight() {
		for(Block block : _blocks)
			block.moveRight();
	}

	public ArrayList<Block> getBlocks () {
		return _blocks;
	}
	
	abstract void updateShape1();
	abstract void updateShape2();
	abstract void updateShape3();
	abstract void updateShape4();
}