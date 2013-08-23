package com.ashwin.tetris.shape;

import java.awt.Color;

public class S1 extends Shape {

	public S1 (int _x, int _y) {
		super (Color.GREEN, _x, _y);	
	}
	
	protected void updateShape1 () {
		_blocks.get(1).moveTo(_blocks.get(0).getRow() + 0, _blocks.get(0).getCol() - 1);
		_blocks.get(2).moveTo(_blocks.get(0).getRow() - 1, _blocks.get(0).getCol() + 0);
		_blocks.get(3).moveTo(_blocks.get(0).getRow() - 1, _blocks.get(0).getCol() + 1);
	}

	protected void updateShape2 () {
		_blocks.get(1).moveTo(_blocks.get(0).getRow() + 1, _blocks.get(0).getCol() + 0);
		_blocks.get(2).moveTo(_blocks.get(0).getRow() + 0, _blocks.get(0).getCol() - 1);
		_blocks.get(3).moveTo(_blocks.get(0).getRow() - 1, _blocks.get(0).getCol() - 1);
	}

	protected void updateShape3 () {
		_blocks.get(1).moveTo(_blocks.get(0).getRow() + 0, _blocks.get(0).getCol() + 1);
		_blocks.get(2).moveTo(_blocks.get(0).getRow() + 1, _blocks.get(0).getCol() + 0);
		_blocks.get(3).moveTo(_blocks.get(0).getRow() + 1, _blocks.get(0).getCol() - 1);
	}

	protected void updateShape4 () {
		_blocks.get(1).moveTo(_blocks.get(0).getRow() - 1, _blocks.get(0).getCol() + 0);
		_blocks.get(2).moveTo(_blocks.get(0).getRow() + 0, _blocks.get(0).getCol() + 1);
		_blocks.get(3).moveTo(_blocks.get(0).getRow() + 1, _blocks.get(0).getCol() + 1);
	}
}
