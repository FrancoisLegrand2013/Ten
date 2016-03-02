package com.frlgrd.ten.ui.component;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.frlgrd.ten.R;
import com.frlgrd.ten.core.Level;
import com.frlgrd.ten.core.LevelGenerator;
import com.frlgrd.ten.core.Logger;
import com.frlgrd.ten.core.model.Tile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class GameView extends FrameLayout implements GestureDetector.OnGestureListener {

	private static final int START = 0;
	private static final int GAME_PREPARED = 1;
	@GameState
	private int gameSate = START;

	private int row = 0;
	private int column = 0;
	private int tileColor1 = 0;
	private int tileColor2 = 0;

	private int tileSize;
	private Rect baseTileRect;
	private Paint paint;
	private Paint textPaint;

	private Tile[][] tiles;
	private Rect[][] tilesPositions;

	private boolean viewSizeInitialized = false;

	private GestureDetector gestureDetector;

	public GameView(Context context) {
		super(context);
		init(context, null);
	}

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public GameView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attributeSet) {

		TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.GameView);
		tileColor1 = array.getInteger(R.styleable.GameView_tileColor1, Color.BLACK);
		tileColor2 = array.getInteger(R.styleable.GameView_tileColor2, Color.DKGRAY);
		array.recycle();

		baseTileRect = new Rect();
		paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		textPaint = new Paint();
		textPaint.setAntiAlias(true);

		gestureDetector = new GestureDetector(getContext(), this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (gameSate == GAME_PREPARED) {
			int maxHeight = getMeasuredHeight() / row;
			int maxWidth = getMeasuredWidth() / column;
			if (maxHeight < maxWidth) {
				tileSize = maxHeight;
			} else {
				tileSize = maxWidth;
			}
			textPaint.setTextSize(tileSize * .5F);
			if (!viewSizeInitialized) {
				initViewSize();
			}
		}
	}

	private void initViewSize() {
		setLayoutParams(new RelativeLayout.LayoutParams(column * tileSize, row * tileSize));
		viewSizeInitialized = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		switch (gameSate) {
			case GAME_PREPARED:
				drawBoard(canvas);
				drawTilesAtLaunch(canvas);
				break;
		}
	}

	private void drawTilesAtLaunch(Canvas canvas) {
		for (int x = 0; x < column; x++) {
			for (int y = 0; y < row; y++) {
				Tile tile = tiles[x][y];
				if (tile != null) {
					tile.setPosition(tilesPositions[x][y]);
					paint.setColor(Tile.getTilesColor(getContext(), tile.getValue()));
					textPaint.setColor(Tile.getValueColor(tile.getValue()));
					canvas.drawRect(tilesPositions[x][y], paint);
					float horizontalMarginFactor = (tile.getValue() > 9 || tile.getValue() < 0) ? .2F : .33F;
					canvas.drawText(String.valueOf(tile.getValue()), tile.getPosition().left + (tileSize * horizontalMarginFactor), tile.getPosition().top + tileSize * .66F, textPaint);
				}
			}
		}
	}

	private void drawBoard(Canvas canvas) {
		int horizontalOffset = (getMeasuredWidth() - column * tileSize) / 2;
		int verticalOffset = (getMeasuredHeight() - row * tileSize) / 2;
		for (int x = 0; x < column; x++) {
			for (int y = 0; y < row; y++) {
				int left = x * tileSize + horizontalOffset;
				int top = y * tileSize + verticalOffset;
				int right = (x + 1) * tileSize + horizontalOffset;
				int bottom = (y + 1) * tileSize + verticalOffset;
				baseTileRect.set(left, top, right, bottom);
				paint.setColor((x + y) % 2 == 0 ? tileColor1 : tileColor2);
				canvas.drawRect(baseTileRect, paint);
				tilesPositions[x][y].set(left, top, right, bottom);
			}
		}
	}

	public void start(@NonNull Level level) {
		column = level.getColumn();
		row = level.getRow();
		tilesPositions = new Rect[column][row];
		for (int x = 0; x < column; x++) {
			for (int y = 0; y < row; y++) {
				tilesPositions[x][y] = new Rect();
			}
		}
		if (level instanceof LevelGenerator.RandomLevel) {
			tiles = LevelGenerator.generateRandom(tilesPositions, level);
		} else {
			tiles = LevelGenerator.generate(tilesPositions, level);
		}

		gameSate = GAME_PREPARED;
		invalidate();
	}

	@Override
	public boolean onDown(MotionEvent e) {
		Logger.info("onDown");
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		Logger.info("onScroll");
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		Logger.info("onFling");
		return false;
	}

	@IntDef({START, GAME_PREPARED})
	@Retention(RetentionPolicy.SOURCE)
	private @interface GameState {
	}
}
