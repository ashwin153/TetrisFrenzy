package com.ashwin.tetris.android.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.ashwin.tetris.android.R;
import com.ashwin.tetris.android.services.ServerConnectionService;
import com.ashwin.tetris.android.services.ServiceConnectionClient;
import com.ashwin.tetris.command.Command;
import com.ashwin.tetris.shape.Block;
import com.ashwin.tetris.shape.L1;
import com.ashwin.tetris.shape.L2;
import com.ashwin.tetris.shape.Line;
import com.ashwin.tetris.shape.S1;
import com.ashwin.tetris.shape.S2;
import com.ashwin.tetris.shape.Shape;
import com.ashwin.tetris.shape.Square;
import com.ashwin.tetris.shape.T;

public class TetrisView extends SurfaceView implements  OnTouchListener, SurfaceHolder.Callback {
	// TetrisView Graphical and Main UI contants
	private static final int MAX_ROW = 20;
	private static final int MAX_COL = 10;
	
	private int _blockSize, _blockInlaySize, _border, _clickRadius, _swipeLength, _swipeVelocity;
	private boolean _isActive, _isOnline;
	private float _screenDensity;

	// Tetris Game Variables
	private ServiceConnectionClient _serviceConnectionClient;
	private List<Block> _blocks;
	private GameTask _gameTask;
	private Shape _curShape;
	private TetrisScoreView _scoreView;
	private int _score;
	
	private GestureDetector _detector;
	private SimpleOnGestureListener _gestureListener = new SimpleOnGestureListener() {
		private float _startX, _startY;
		
		@Override
		public boolean onDown(MotionEvent e) {
			_startX = e.getX();
			_startY = e.getY();
			
			return true;
		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distX, float distY) {
			float dx = e2.getX() - _startX;
			float dy = e2.getY() - _startY;
			Log.i("TetrisView@onScroll", "distY: " + distY);
			if(dy > _blockSize && _curShape.canMoveForward(_blocks) && distY > -_swipeLength) {
				_curShape.moveForward();
				_startY = e2.getY();
	   		} 
			
			if (dx > _blockSize && _curShape.canMoveRight(_blocks)) {
				_curShape.moveRight();
				_startX = e2.getX();
			} else if(dx < -_blockSize && _curShape.canMoveLeft(_blocks)) {
				_curShape.moveLeft();
				_startX = e2.getX();
			}
			
			return true;
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velX, float velY) {
			float dy = e2.getY() - _startY;
			Log.i("TetrisView@onFling", "distY: " + dy);
			
			if(dy < -_swipeLength && velY < -_swipeVelocity) {
				if(_scoreView.getHoldShape() == null) {
					_scoreView.setHoldShape(_curShape.moveTo(0, 5));
					_curShape = _scoreView.getNextShape();
					_scoreView.setNextShape(getRandomShape());
				} else {
					Shape temp = _curShape;
					_curShape = _scoreView.getHoldShape();
					_scoreView.setHoldShape(temp.moveTo(0, 5));
				}
	   		}
			
			return true;
		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if(_curShape.canRotateForward(_blocks))
				_curShape.rotateForward();
			return true;
		}
		
//		@Override
//		public boolean onDoubleTap(MotionEvent e) {
//			if(_scoreView.getHoldShape() == null) {
//				_scoreView.setHoldShape(_curShape.moveTo(0, 5));
//				_curShape = _scoreView.getNextShape();
//				_scoreView.setNextShape(getRandomShape());
//			} else {
//				Shape temp = _curShape;
//				_curShape = _scoreView.getHoldShape();
//				_scoreView.setHoldShape(temp.moveTo(0, 5));
//			}
//			
//			return true;
//		}
	};
	
	public TetrisView(Context context, int blockSize, int border, int clickRadius, int swipeLength,
			int swipeVelocity, boolean isActive, boolean isOnline, TetrisScoreView scoreView) {
		
		super(context);
		setZOrderOnTop(true);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		getHolder().addCallback(this);
		
		_blockSize = getPixels(blockSize);
		_blockInlaySize = getPixels(blockSize / 3);
		_border = getPixels(border);
		_clickRadius = getPixels(clickRadius);
		_swipeLength = swipeLength;
		_isActive = isActive;
		_isOnline = isOnline;
		_scoreView = scoreView;
	}
	
	public TetrisView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(attrs);
	}
	
	public TetrisView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup(attrs);
	}
	
	// Setup retrieves values from the AttributeSet, sets up the background and holder
	// and converts values to density pixels
	private void setup(AttributeSet attrs) {
		setZOrderOnTop(true);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		getHolder().addCallback(this);
		
		TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TetrisView);
		_screenDensity = this.getResources().getDisplayMetrics().density;
		
		// Default Values
		_isActive = array.getBoolean(R.styleable.TetrisView_isActive, true);
		_isOnline = array.getBoolean(R.styleable.TetrisView_isOnline, true);
		_clickRadius = getPixels(array.getFloat(R.styleable.TetrisView_clickRadius, 4));
		_swipeLength = getPixels(array.getFloat(R.styleable.TetrisView_swipeLength, 75));
		_swipeVelocity = getPixels(array.getFloat(R.styleable.TetrisView_swipeVelocity, 300));
		_border = getPixels(array.getFloat(R.styleable.TetrisView_border, 1));
		_blockSize = getPixels(array.getFloat(R.styleable.TetrisView_blockSize, 21));
		_blockInlaySize = _blockSize / 3;
		
		array.recycle();
	}
	
	private int getPixels(float dp) {
		return (int)(dp * _screenDensity + 0.5f);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if(_detector != null && _detector.onTouchEvent(event))
			repaint(getHolder());
		
		return true;
	}
	
	// SurfaceHolder.Callback
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		this.setOnTouchListener(this);
		if(_blocks == null)
			_blocks = Collections.synchronizedList(new ArrayList<Block>());
			
		if(_isActive) {
			_gameTask = (GameTask) new GameTask(holder).execute();
			_detector = new GestureDetector(getContext(), _gestureListener);
		}
		
		this.repaint(holder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {}

	
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
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		
		paintGrid(canvas, paint);
		paintBlockArrayList(canvas, paint, _curShape.getBlocks());
		paintBlockArrayList(canvas, paint, _blocks);
		paintBorder(canvas);
	}
	
	private void paintBorder(Canvas canvas) {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(_border);
		paint.setColor(Color.GRAY);
		
		Rect border = new Rect(0, 0, TetrisView.MAX_COL* _blockSize + 2 * _border,
							  TetrisView.MAX_ROW * _blockSize + 2 * _border);
		canvas.drawRect(border, paint);
	}
	
	private void paintGrid(Canvas c, Paint p) {
		for (int row = 0; row < TetrisView.MAX_ROW; row++) {
			for (int col = 0; col < TetrisView.MAX_COL; col++) {
				// Paints the grid
				int gridColor = ((col - row) % 2 != 0) ? Color.argb(225, 0, 0, 0) : Color.argb(225, 33, 33, 33);
				p.setColor(gridColor);
				paintBlock(row, col, 0, c, p);
			}
		}
	}

	private void paintBlockArrayList(Canvas c, Paint p, List<Block> blocks) {
		Block[] blockArray = new Block[blocks.size()];
		blocks.toArray(blockArray);
		
		for (Block block : blockArray) {
			// Paints block
			int blockColor = block.getColor();
			p.setColor(blockColor);
			paintBlock(block.getRow(), block.getCol(), 1, c, p);

			// Paints inlay
			float[] hsv = new float[3];
			Color.colorToHSV(blockColor, hsv);
			hsv[2] *= 0.8f;
			p.setColor(Color.HSVToColor(hsv));
			paintBlock(block.getRow(), block.getCol(), _blockInlaySize, c, p);
		}
	}

	private void paintBlock(int row, int col, int offset, Canvas c, Paint p) {
		Rect rect = new Rect(_border + col * _blockSize + offset,
							 _border + row * _blockSize + offset, 
							 _border + col * _blockSize + _blockSize - offset, 
							 _border + row * _blockSize + _blockSize - offset);
		c.drawRect(rect, p);
	}
	
	protected Shape getRandomShape() {
		switch((int) (Math.random() * 7)) {
			case 0:  return new L1(0, 5);
			case 1:  return new L2(0, 5);
			case 2:  return new Line(0, 5);
			case 3:  return new S1(0, 5);
			case 4:  return new S2(0, 5);
			case 5:  return new Square(0, 5);
			default:  return new T(0, 5);
		}
	}

	
	// Getter and Setter Methods for Blocks
	public List<Block> getBlocks() {
		return _blocks;
	}
	
	public void setBlocks(ArrayList<Block> blocks) {
		_blocks = blocks;
		this.repaint(getHolder());
	}
	
	public void setTetrisScoreView(TetrisScoreView scoreView) {
		_scoreView = scoreView;
		_scoreView.setNextShape(getRandomShape());
	}
	
	public void pause() {
		if(_gameTask != null)
			_gameTask.cancel(true);
	}
	
	// GameTask
	private class GameTask extends AsyncTask<Void, Void, Void> {
		
		private static final long REFRESH_RATE_MILLIS = 225;
		
		private SurfaceHolder _holder;
		private boolean _isRunning;
		
		public GameTask(SurfaceHolder holder) {
			_holder = holder;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			_isRunning = true;
			
			if(_curShape == null)
				_curShape = getRandomShape();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				while(_isRunning) {
						if(_curShape.canMoveForward(_blocks))
							_curShape.moveForward();
						else
							onCurrentShapeBlocked();
						
						_score += 5;
						publishProgress();
						Thread.sleep(GameTask.REFRESH_RATE_MILLIS);
				}
			} catch(Exception e) {
				for(StackTraceElement str : e.getStackTrace())
					Log.e("GameThread@doInBackground", str.getMethodName() + ", " + str.getLineNumber());
			}
			return null;
		}
		
		public void stopRunning() {
			_isRunning = false;
		}
		
		private void onCurrentShapeBlocked() throws RemoteException {
			_blocks.addAll(_curShape.getBlocks());
			int lines = removeRows();
			_score += Math.pow(2,  lines) * 30;
			if(_scoreView != null && _scoreView.getNextShape() != null) {
				_curShape = _scoreView.getNextShape();
				_scoreView.setNextShape(getRandomShape());
			} else {
				_curShape = getRandomShape();
				if(_scoreView != null)
					_scoreView.setNextShape(getRandomShape());
			}
			
			
			if(!_isOnline)
				return;
			
			StringBuilder builder = new StringBuilder();
			for(Block b : _blocks)
				builder.append(b.toString() + ".");
			
			HashMap<Command, String> commands = new HashMap<Command, String>();
			commands.put(Command.LINE, String.valueOf(lines));
			commands.put(Command.BLOCKS, builder.toString());
			_serviceConnectionClient.sendMessage(ServerConnectionService.SEND_DATA, commands);
		}
		
		private int removeRows() {
			ArrayList<ArrayList<Block>> grid = new ArrayList<ArrayList<Block>>();
			int numLinesRemoved = 0;
			
			for(int row = 0; row < OnlineTetrisView_withOnlineCapabilities.MAX_ROW - numLinesRemoved; row++) {
				grid.add(row, new ArrayList<Block>());
				for(Block block : _blocks)
					if(block.getRow() == row)
						grid.get(row).add(block);
			}
			
			for(int row = 0; row < grid.size(); row++) {
				ArrayList<Block> line = grid.get(row);
				if(line.size() == OnlineTetrisView_withOnlineCapabilities.MAX_COL) {
					for(int i = 0; i < row; i++)
						for(int j = 0; j < grid.get(i).size(); j++)
							grid.get(i).get(j).moveForward();
					_blocks.removeAll(line);
					numLinesRemoved++;
				}
			}
			
			return numLinesRemoved;
		}
		
		@Override
		public void onProgressUpdate(Void... params) {
			repaint(_holder);
			
			if(_scoreView != null)
				_scoreView.setScore(_score);
		}
	}
}
