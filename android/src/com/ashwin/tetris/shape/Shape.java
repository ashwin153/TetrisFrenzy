package com.ashwin.tetris.shape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ashwin.tetris.android.views.OnlineTetrisView_withOnlineCapabilities;

public abstract class Shape {

	protected List<Block> _blocks;
	private int _rotate;
	private int _color;
	
	public Shape(int color, int startRow, int startCol) {
		_blocks = Collections.synchronizedList(new ArrayList<Block>());
		_color = color;
		
		for(int i = 0 ; i < 4; i++)
			_blocks.add(new Block(_color, startRow, startCol));
		updateShape();
	}
	
	public boolean canMoveForward (List<Block> othBlocks) {
		for(Block block : _blocks) 
			if(!block.canMoveForward(othBlocks))
				return false;
		return true;
	}
	
	public boolean canMoveRight (List<Block> othBlocks) {
		for(Block block : _blocks) 
			if(!block.canMoveRight(othBlocks))
				return false;
		return true;
	}
	
	public boolean canMoveLeft (List<Block> othBlocks) {
		for(Block block : _blocks) 
			if(!block.canMoveLeft(othBlocks))
				return false;
		return true;
	}
	
	public boolean canRotateForward(List<Block> othBlocks) {
		rotateForward();
		boolean canRotate = true;
		
		for(Block block : _blocks) {
			if(block.getCol() < 0 || 
				block.getCol() >= OnlineTetrisView_withOnlineCapabilities.MAX_COL ||
				block.getRow() >= OnlineTetrisView_withOnlineCapabilities.MAX_ROW)
				canRotate = false;
				
			for(Block othBlock : othBlocks)
				if(block.getRow() == othBlock.getRow() &&
				   block.getCol() == othBlock.getCol())
					canRotate = false;
		}
		
		rotateBackward();
		return canRotate;
	}
	
	public boolean canRotateBackward(List<Block> othBlocks) {
		rotateBackward();
		boolean canRotate = true;
		
		for(Block block : _blocks) {
			if(block.getCol() < 0 || 
					block.getCol() >= OnlineTetrisView_withOnlineCapabilities.MAX_COL ||
					block.getRow() >= OnlineTetrisView_withOnlineCapabilities.MAX_ROW)
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
	
	public Shape rotateForward() {
		_rotate = (_rotate + 1) % 4;
		updateShape();
		return this;
	}
	
	public Shape rotateBackward() {
		_rotate = (_rotate - 1 < 0) ? 3 : _rotate - 1;
		updateShape();
		return this;
	}
	
	public Shape moveTo(int newRow, int newCol) {
		_blocks.get(0).moveTo(newRow, newCol);
		updateShape();
		return this;
	}
	
	public Shape moveForward() {
		for(Block block : _blocks)
			block.moveForward();
		return this;
	}
	
	public Shape moveLeft() {
		for(Block block : _blocks)
			block.moveLeft();
		return this;
	}
	
	public Shape moveRight() {
		for(Block block : _blocks)
			block.moveRight();
		return this;
	}

	public List<Block> getBlocks () {
		return _blocks;
	}
	
	abstract void updateShape1();
	abstract void updateShape2();
	abstract void updateShape3();
	abstract void updateShape4();
}