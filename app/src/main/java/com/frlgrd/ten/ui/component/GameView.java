package com.frlgrd.ten.ui.component;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
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
import com.frlgrd.ten.core.model.Tile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class GameView extends FrameLayout implements GestureDetector.OnGestureListener {

	private static final int START = 0;
	private static final int GAME_PREPARED = 1;
	private static final int WAITING = 2;
	private static final int DRAGGING = 3;

	private static final int NONE = -1;
	private static final int HORIZONTAL = 0;
	private static final int VERTICAL = 1;

	@OrientationMode
	private int draggingOrientation;
	@GameState
	private int gameSate = START;

	private int row = 0;
	private int column = 0;
	private int tileColor1 = 0;
	private int tileColor2 = 0;

	private int tileSize;
	private RectF baseTileRect;
	private Paint paint;
	private Paint textPaint;

	private Tile[][] tiles;
	private RectF[][] tilesPositions;
	private Tile draggingTile;

	private boolean viewSizeInitialized = false;

	private GestureDetector gestureDetector;
	private RectF pointer;
	private float dragX = 0, dragY = 0;

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

		baseTileRect = new RectF();
		paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		textPaint = new Paint();
		textPaint.setAntiAlias(true);

		gestureDetector = new GestureDetector(getContext(), this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (draggingTile != null) {
				Tile tile = getTargetTile(new PointF(event.getX(), event.getY()));
				if (tile != null && draggingTile.canMergeWith(tile)) {
					mergeDraggingTileWith(tile);
				} else {
					invalidate();
				}
			}
			gameSate = WAITING;
			draggingOrientation = NONE;
			pointer = null;
			draggingTile = null;
		}
		gestureDetector.onTouchEvent(event);
		return true;
	}

	private void mergeDraggingTileWith(Tile target) {
		if (draggingTile.canMergeWith(target)) {
			for (int x = 0; x < column; x++) {
				for (int y = 0; y < row; y++) {
					Tile tile = tiles[x][y];
					if (tile != null) {
						if (tile == draggingTile) {
							tiles[x][y] = null;
						} else if (tile == target) {
							tiles[x][y].setValue(target.getValue() + draggingTile.getValue());
						}
					}
				}
			}

			invalidate();
		}
	}

	private Tile getTargetTile(@NonNull PointF point) {
		for (int x = 0; x < column; x++) {
			for (int y = 0; y < row; y++) {
				Tile tile = tiles[x][y];
				if (tile != null) {
					RectF rect = tile.getPosition();
					if (point.x > rect.left && point.x < rect.right && point.y > rect.top && point.y < rect.bottom) {
						if (draggingTile != null && tile != draggingTile) {
							return tile;
						}
					}
				}
			}
		}
		return null;
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
		if (gameSate >= GAME_PREPARED) {
			drawBoard(canvas);
			drawTiles(canvas);
		}
	}

	private void drawTiles(Canvas canvas) {
		for (int x = 0; x < column; x++) {
			for (int y = 0; y < row; y++) {
				Tile tile = tiles[x][y];
				if (tile != null) {
					if (pointer != null) {
						if (pointer.intersect(tile.getPosition()) && gameSate == WAITING && tile.canBeDragged()) {
							draggingTile = tile;
							gameSate = DRAGGING;
						}
					}
					if (gameSate >= GAME_PREPARED && tile != draggingTile) {
						tile.setPosition(tilesPositions[x][y]);
						drawTile(canvas, tile);
					}
				}
			}
		}
		if (gameSate == DRAGGING && pointer != null) {
			if (draggingOrientation == HORIZONTAL) {
				draggingTile.getPosition().left = dragX - tileSize / 2;
				draggingTile.getPosition().right = dragX + tileSize / 2;
			} else {
				draggingTile.getPosition().top = dragY - tileSize / 2;
				draggingTile.getPosition().bottom = dragY + tileSize / 2;
			}
			drawTile(canvas, draggingTile);
		}
	}

	private void drawTile(Canvas canvas, Tile tile) {
		paint.setColor(Tile.getTilesColor(getContext(), tile.getValue()));
		textPaint.setColor(Tile.getValueColor(tile.getValue()));
		canvas.drawRect(tile.getPosition(), paint);
		float horizontalMarginFactor = (tile.getValue() > 9 || tile.getValue() < 0) ? .2F : .33F;
		canvas.drawText(
				String.valueOf(tile.getValue()),
				tile.getPosition().left + (tileSize * horizontalMarginFactor),
				tile.getPosition().top + tileSize * .66F,
				textPaint);
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
		tilesPositions = new RectF[column][row];
		for (int x = 0; x < column; x++) {
			for (int y = 0; y < row; y++) {
				tilesPositions[x][y] = new RectF();
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
		if (draggingOrientation == NONE) {
			draggingOrientation = Math.abs(distanceX) > Math.abs(distanceY) ? HORIZONTAL : VERTICAL;
		}
		if (pointer == null) {
			pointer = new RectF();
		}
		pointer.set(e2.getX(), e2.getY(), e2.getX(), e2.getY());
		dragX = e2.getX();
		dragY = e2.getY();
		invalidate();
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}

	@IntDef({START, GAME_PREPARED, WAITING, DRAGGING})
	@Retention(RetentionPolicy.SOURCE)
	private @interface GameState {
	}

	@IntDef({NONE, HORIZONTAL, VERTICAL})
	@Retention(RetentionPolicy.SOURCE)
	public @interface OrientationMode {
	}
}
