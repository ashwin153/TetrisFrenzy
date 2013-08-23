package com.ashwin.tetris.android.views;

import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ashwin.tetris.android.R;
import com.ashwin.tetris.shape.Block;
import com.ashwin.tetris.shape.Shape;

public class TetrisScoreView extends SurfaceView implements SurfaceHolder.Callback {
	
	private Shape _nextShape, _holdShape;
	private int _blockSize, _blockInlaySize, _score;
	private float _screenDensity, _textSize;
	
	private boolean _isSurfaceCreated;
	
	public TetrisScoreView(Context context, int blockSize, float textSize) {
		super(context);
		setZOrderOnTop(true);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		this.getHolder().addCallback(this);
		
		_screenDensity = this.getResources().getDisplayMetrics().density;
		_textSize = textSize;
		_blockSize = getPixels(blockSize);
		_blockInlaySize = _blockSize / 3;
	}
	
	public TetrisScoreView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(attrs);
	}
	
	public TetrisScoreView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup(attrs);
	}
	
	private void setup(AttributeSet attrs) {
		setZOrderOnTop(true);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		this.getHolder().addCallback(this);
		
		TypedArray array = this.getContext().obtainStyledAttributes(attrs, R.styleable.TetrisScoreView);
		_screenDensity = this.getResources().getDisplayMetrics().density;
		
		_textSize = getPixels(20);
		_blockSize = getPixels(21);
		_blockInlaySize = 21 / 3;
		
		final int N = array.getIndexCount();
		for (int i = 0; i < N; ++i)
		{
		    int attr = array.getIndex(i);
		    switch (attr)
		    {
		        case R.styleable.TetrisScoreView_blockSize:
		            _blockSize = getPixels(array.getFloat(attr, 21));
		            _blockInlaySize = _blockSize / 3;
		            break;
		        case R.styleable.TetrisScoreView_textSize:
		        	_textSize = getPixels(array.getFloat(attr, 20));
		        	break;
		    }
		}
		
		array.recycle();
	}

	private int getPixels(float dp) {
		return (int)(dp * _screenDensity + 0.5f);
	}
	
	
	// Getter and Setter methods
	public void setNextShape(Shape nextShape) {
		_nextShape = nextShape;
		
		if(_isSurfaceCreated)
			this.repaint(getHolder());
	}
	
	public void setHoldShape(Shape holdShape) {
		_holdShape = holdShape;
		
		if(_isSurfaceCreated)
			this.repaint(getHolder());
	}
	
	public void setScore(int score) {
		_score = score;
		
		if(_isSurfaceCreated)
			this.repaint(getHolder());
	}
	
	public Shape getNextShape() {
		return _nextShape;
	}
	
	public Shape getHoldShape() {
		return _holdShape;
	}
	
	public int getScore() {
		return _score;
	}

	
	// SurfaceHolder.Callback
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		_isSurfaceCreated = true;
		this.repaint(holder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
	}

	
	// onDraw methods
	private void repaint(SurfaceHolder holder) {
		Canvas canvas = holder.lockCanvas();
		
		synchronized(holder) {
			onDraw(canvas);
		}
		
		if(canvas != null)
			holder.unlockCanvasAndPost(canvas);
	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
		int marginTop = getPixels(15);
		int textHeight  = (int) _textSize;
		int blockHeight = 4 * _blockSize;
		int marginBetween = getPixels(10);
		
		int nextShapeTextLoc = marginTop;
		int nextShapeLoc     = nextShapeTextLoc + textHeight + marginBetween;
		int holdShapeTextLoc = nextShapeLoc + blockHeight + marginBetween;
		int holdShapeLoc     = holdShapeTextLoc + textHeight + marginBetween;
		int scoreLoc         = holdShapeLoc + blockHeight + marginBetween;
		
		
		paintText(canvas, "Next Shape", nextShapeTextLoc);
		paintText(canvas, "Hold Shape", holdShapeTextLoc);
		paintText(canvas, String.valueOf(_score), scoreLoc);
		
		if(_nextShape != null)
			paintShape(canvas, _nextShape.getBlocks(), nextShapeLoc);
		
		if(_holdShape != null)
			paintShape(canvas, _holdShape.getBlocks(), holdShapeLoc);
	}
	
	private void paintText(Canvas canvas, String text, int startHeight) {
		Paint paint = new Paint();
		paint.setTypeface(Typeface.createFromAsset(this.getContext().getAssets(), "Hobo.ttf"));
		paint.setColor(Color.WHITE);
		paint.setTextSize(_textSize);
		paint.setAntiAlias(true);
		
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		
		float textLength = paint.measureText(text);
		int margin = (int) (this.getWidth() - textLength) / 2;
		canvas.drawText(text, margin, startHeight + bounds.height(), paint);
	}
	
	private void paintShape(Canvas canvas, List<Block> blocks, int startHeight) {
		int firstCol = Integer.MAX_VALUE, lastCol = Integer.MIN_VALUE, _firstRow = Integer.MAX_VALUE;
		for(Block block : blocks) {
			if(block.getRow() < _firstRow)
				_firstRow = block.getRow();
			
			if(block.getCol() < firstCol)
				firstCol = block.getCol();
			else if(block.getCol() > lastCol)
				lastCol = block.getCol();
		}
		
		int blockWidth = (lastCol - firstCol + 1) * _blockSize;
		int margin     = (this.getWidth() - blockWidth) / 2;
		Paint p = new Paint();
		p.setStyle(Paint.Style.FILL);
		for(Block block : blocks) {
			int startX = margin + (block.getCol() - firstCol) * _blockSize;
			int startY = startHeight + (block.getRow() - _firstRow) * _blockSize;
			
			p.setColor(block.getColor());
			canvas.drawRect(new Rect(startX + 1, startY + 1, startX + _blockSize - 1, startY + _blockSize - 1), p);
			
			float[] hsv = new float[3];
			Color.colorToHSV(block.getColor(), hsv);
			hsv[2] *= 0.8f;
			p.setColor(Color.HSVToColor(hsv));
			canvas.drawRect(new Rect(startX + _blockInlaySize, startY + _blockInlaySize,
									 startX + _blockSize - _blockInlaySize, startY + _blockSize - _blockInlaySize), p);
		}
	}
}
