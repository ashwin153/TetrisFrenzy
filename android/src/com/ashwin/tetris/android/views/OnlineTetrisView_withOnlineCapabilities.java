package com.ashwin.tetris.android.views;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

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

public class OnlineTetrisView_withOnlineCapabilities extends SurfaceView implements OnTouchListener,
		SurfaceHolder.Callback {

	private static final float SWIPE_HORIZONTAL_DP = 23;
	private static final float SWIPE_VERTICAL_DP   = 23;
	private static final float CLICK_DP = 4;
	
	private static final int BLOCK_SIZE_DP = 21;
	private static final int BLOCK_INLAY_OFFSET_DP = 7;
	private static final int BORDER = 5;
	
	public static final int MAX_ROW = 20;
	public static final int MAX_COL = 10;

	private List<Block> _blocks;
	private Shape _curShape, _holdShape, _nextShape;
	private GameThread _gameThread;
	private ConnectThread _connectThread;
	private Player _player;
	
	private float _startX, _startY;
	private float _scale;

	private int _score;
	
	public OnlineTetrisView_withOnlineCapabilities(Context context) {
		super(context);
		setZOrderOnTop(true);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		getHolder().addCallback(this);
	}

	public OnlineTetrisView_withOnlineCapabilities(Context context, AttributeSet attrs) {
		super(context, attrs);
		setZOrderOnTop(true);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		getHolder().addCallback(this);
		
	}

	public OnlineTetrisView_withOnlineCapabilities(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setZOrderOnTop(true);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		getHolder().addCallback(this);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		float dx = event.getX() - _startX;
		float dy = event.getY() - _startY;

		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			_startX = event.getX();
			_startY = event.getY();

			break;

		case MotionEvent.ACTION_MOVE:
			if(dy > getPixels(SWIPE_VERTICAL_DP) &&_curShape.canMoveForward(_blocks)) {
					_curShape.moveForward();
					_startY = event.getY();
		    }if (dx > getPixels(SWIPE_HORIZONTAL_DP) && _curShape.canMoveRight(_blocks)) {
				_curShape.moveRight();
				_startX = event.getX();
			} if(dx < getPixels(-SWIPE_HORIZONTAL_DP) && _curShape.canMoveLeft(_blocks)) {
				_curShape.moveLeft();
				_startX = event.getX();
			}

			this.repaint(getHolder());
			break;

		case MotionEvent.ACTION_UP:
			if (Math.abs(dx) < getPixels(CLICK_DP) && Math.abs(dy) < getPixels(CLICK_DP) && _curShape.canRotateForward(_blocks))
				_curShape.rotateForward();

			this.repaint(getHolder());
			break;
		}
		return true;
	}
	
	public int getPixels(float dp) {
		return (int)(dp * _scale + 0.5f);
	}
	
	protected void repaint(SurfaceHolder holder) {
		Canvas c = holder.lockCanvas();
		synchronized(holder) {
			onDraw(c);
		}

		if(c != null)
			holder.unlockCanvasAndPost(c);
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


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		this.setOnTouchListener(this);
		_blocks = new ArrayList<Block>();
		_curShape = getRandomShape();
		_nextShape = getRandomShape();
		_curShape.moveTo(0,  5);
		_scale = getResources().getDisplayMetrics().density;
		
		this.repaint(holder);
		
		_gameThread = new GameThread(holder);
		_connectThread = new ConnectThread();
		_connectThread.execute();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);

		paintGrid(canvas, paint);
		paintBlockArrayList(canvas, paint, _curShape.getBlocks());
		paintBlockArrayList(canvas, paint, _nextShape.getBlocks());
		paintBlockArrayList(canvas, paint, _blocks);
		paintBorder(canvas);
		paintScore(canvas);
	}
	
	private void paintScore(Canvas canvas) {
		Paint paint = new Paint();
		paint.setTypeface(Typeface.createFromAsset(this.getContext().getAssets(), "Hobo.ttf"));
		paint.setColor(Color.WHITE);
		paint.setTextSize(80);
		canvas.drawText("" + _score, getPixels(MAX_COL * BLOCK_SIZE_DP + 10), getPixels(MAX_ROW * BLOCK_SIZE_DP), paint);
	}
	
	private void paintBorder(Canvas canvas) {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(BORDER);
		paint.setColor(Color.WHITE);
		
		Rect border = new Rect(0, 0, OnlineTetrisView_withOnlineCapabilities.MAX_COL* getPixels(BLOCK_SIZE_DP) + BORDER,
				OnlineTetrisView_withOnlineCapabilities.MAX_ROW * getPixels(BLOCK_SIZE_DP) + BORDER);
		canvas.drawRect(border, paint);
	}
	
	private void paintGrid(Canvas c, Paint p) {
		for (int row = 0; row < OnlineTetrisView_withOnlineCapabilities.MAX_ROW; row++) {
			for (int col = 0; col < OnlineTetrisView_withOnlineCapabilities.MAX_COL; col++) {
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
			paintBlock(block.getRow(), block.getCol(), getPixels(BLOCK_INLAY_OFFSET_DP), c, p);
		}
	}

	private void paintBlock(int row, int col, int offset, Canvas c, Paint p) {
		Rect rect = new Rect(BORDER + col * getPixels(BLOCK_SIZE_DP) + offset,BORDER + row
				* getPixels(BLOCK_SIZE_DP) + offset, BORDER +  col * getPixels(BLOCK_SIZE_DP)
				+ getPixels(BLOCK_SIZE_DP) - offset, BORDER +  row * getPixels(BLOCK_SIZE_DP)
				+ getPixels(BLOCK_SIZE_DP) - offset);
		c.drawRect(rect, p);
	}
	
	
	private void handleInput() throws IOException {
		HashMap<Command, String> commands = _player.read();
		for(Command cmd : commands.keySet()) {
			switch(cmd) {
				case BEGIN:		 _gameThread.execute();  break;
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
	
	private class ConnectThread extends AsyncTask<Void, Void, Void> {
		
		@Override
		public Void doInBackground(Void... params) {
			try {
				Socket socket = new Socket("192.168.1.128", 2238);
				_player = new Player(socket);
				Log.i("ConnectThread@doInBackground", "Socket connected");
				
				while(_player.getInputStream().available() <= 0);
				
				handleInput();
			} catch(IOException e) {
				Log.e("ConnectThread@doInBackground", e.toString());
			}
			
			return null;
		}
	}

	private class GameThread extends AsyncTask<Void, Void, Void> {

		private SurfaceHolder _holder;
		private boolean _isRunning;
		
		private int _numLines;

		public GameThread(SurfaceHolder holder) {
			_holder = holder;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			_isRunning = true;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				while (_isRunning) {
					if(_curShape.canMoveForward(_blocks))
						_curShape.moveForward();
					else {
						_blocks.addAll(_curShape.getBlocks());
						_curShape = _nextShape;
						_curShape.moveTo(0, 5);
						_nextShape = getRandomShape();
					}
					
					if(_player.getInputStream().available() > 0)
						handleInput();
					
					if(hasLost())
						_player.sendCommand(Command.LOSE, "I lose!");
					
					int numLinesRemoved = removeRows();
					_score += (int) (100 * numLinesRemoved);
					
					if(numLinesRemoved > 1)
						_player.sendCommand(Command.LINE, "" + numLinesRemoved);
					
					publishProgress();
					
				    Thread.sleep(800);
				}
				
			} catch(IOException e) {
				Log.e("GameThread@doInBackground", e.toString());
			} catch (InterruptedException e) {
		    	Log.e("GameThread@doInBackground", e.toString());
		    }
			
			return null;
		}
		
		private boolean hasLost() {
			for(Block block : _blocks)
				if(block.getRow() < 0)
					return true;
			return false;
		}

		
		public void addLine() {
			for(Block block : _blocks)
				block.setRow(block.getRow() - 1);
			
			int row = OnlineTetrisView_withOnlineCapabilities.MAX_ROW - _numLines;
			for(int i = 0; i < OnlineTetrisView_withOnlineCapabilities.MAX_COL; i++)
				_blocks.add(new Block(Color.DKGRAY, row, i));
			
			_numLines++;
		}

		@Override
		public void onProgressUpdate(Void... params) {
			repaint(_holder);
		}

		public void stopRunning() {
			_isRunning = false;
		}
		
		private int removeRows() {
			ArrayList<ArrayList<Block>> grid = new ArrayList<ArrayList<Block>>();
			int numLinesRemoved = 0;
			
			for(int row = 0; row < OnlineTetrisView_withOnlineCapabilities.MAX_ROW - _numLines; row++) {
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
	}
}