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
import java.util.Arrays;

public class GameView extends FrameLayout implements GestureDetector.OnGestureListener {

	private static final int NONE = -1;

	private static final int WAITING = 0;
	private static final int DRAGGING = 1;

	private static final int HORIZONTAL = 0;
	private static final int VERTICAL = 1;

	private static final int DIRECTION_LEFT = 0;
	private static final int DIRECTION_TOP = 1;
	private static final int DIRECTION_RIGHT = 2;
	private static final int DIRECTION_BOTTOM = 3;

	@GameState
	private int gameSate = WAITING;
	@Orientation
	private int draggingOrientation;
	@Direction
	private int draggingDirection;
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

	private float leftBound = 0, topBound = 0, rightBound = 0, bottomBound = 0;
	private boolean boundsCalculated = false;

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
			boundsCalculated = false;
			pointer = null;
			draggingTile = null;
		}
		gestureDetector.onTouchEvent(event);
		return true;
	}

	private void mergeDraggingTileWith(Tile target) {
		if (draggingTile.canMergeWith(target)) {
			loop((x, y) -> {
				Tile tile = tiles[x][y];
				if (tile != null) {
					if (tile == draggingTile) {
						tiles[x][y] = null;
					} else if (tile == target) {
						tiles[x][y].setValue(target.getValue() + draggingTile.getValue());
					}
				}
			});
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
		if (gameSate == WAITING) {
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
		if (gameSate >= WAITING) {
			drawBoard(canvas);
			drawTiles(canvas);
		}
	}

	private void drawTiles(Canvas canvas) {
		loop((x, y) -> {
			Tile tile = tiles[x][y];
			if (tile != null) {
				if (pointer != null) {
					if (pointer.intersect(tile.getPosition())) {
						if (gameSate == WAITING && tile.canBeDragged()) {
							draggingTile = tile;
							calculateDraggingTileBounds(x, y);
							gameSate = DRAGGING;
						}
					}
				}
				if (gameSate >= WAITING && tile != draggingTile) {
					tile.setPosition(tilesPositions[x][y]);
					drawTile(canvas, tile);
				}
			}
		});
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

	private void calculateDraggingTileBounds(int tileXIndex, int tileYIndex) {
		Tile[] horizontalTile = new Tile[column];
		loop((x, y) -> {
			if (tileYIndex == y) {
				horizontalTile[x] = tiles[x][y];
			}
		});

		leftBound = getAvailablePositionsCount(Arrays.copyOfRange(horizontalTile, 0, tileXIndex), false) * tileSize;
		topBound = getAvailablePositionsCount(Arrays.copyOfRange(tiles[tileXIndex], 0, tileYIndex), false) * tileSize;
		rightBound = getAvailablePositionsCount(Arrays.copyOfRange(horizontalTile, tileXIndex + 1, column), true) * tileSize;
		bottomBound = getAvailablePositionsCount(Arrays.copyOfRange(tiles[tileXIndex], tileYIndex + 1, row), true) * tileSize;
		boundsCalculated = true;
	}

	private int getAvailablePositionsCount(Tile[] neighbour, boolean ascendant) {
		int distanceFactor = neighbour.length;
		if (ascendant) {
			for (int i = 0; i < neighbour.length; i++) {
				if (neighbour[i] != null && neighbour[i].canBeDragged()) {
					distanceFactor = i;
					break;
				}
			}
		} else {
			for (int i = neighbour.length - 1; i >= 0; i--) {
				if (neighbour[i] != null && neighbour[i].canBeDragged()) {
					distanceFactor = i - neighbour.length + 1;
					break;
				}
			}
		}
		return Math.abs(distanceFactor);
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
		loop((x, y) -> {
			int left = x * tileSize + horizontalOffset;
			int top = y * tileSize + verticalOffset;
			int right = (x + 1) * tileSize + horizontalOffset;
			int bottom = (y + 1) * tileSize + verticalOffset;
			baseTileRect.set(left, top, right, bottom);
			paint.setColor((x + y) % 2 == 0 ? tileColor1 : tileColor2);
			canvas.drawRect(baseTileRect, paint);
			tilesPositions[x][y].set(left, top, right, bottom);
		});
	}

	public void start(@NonNull Level level) {
		column = level.getColumn();
		row = level.getRow();
		tilesPositions = new RectF[column][row];
		loop((x, y) -> tilesPositions[x][y] = new RectF());
		if (level instanceof LevelGenerator.RandomLevel) {
			tiles = LevelGenerator.generateRandom(tilesPositions, level);
		} else {
			tiles = LevelGenerator.generate(tilesPositions, level);
		}

		gameSate = WAITING;
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
		if (gameSate != DRAGGING) {
			draggingOrientation = Math.abs(distanceX) > Math.abs(distanceY) ? HORIZONTAL : VERTICAL;
		}
		if (draggingOrientation == HORIZONTAL) {
			if (distanceX > 0) {
				draggingDirection = DIRECTION_RIGHT;
			} else {
				draggingDirection = DIRECTION_LEFT;
			}
		} else {
			if (distanceY > 0) {
				draggingDirection = DIRECTION_BOTTOM;
			} else {
				draggingDirection = DIRECTION_TOP;
			}
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
		return true;
	}

	private void loop(@NonNull Looper looper) {
		for (int x = 0; x < column; x++) {
			for (int y = 0; y < row; y++) {
				looper.iteration(x, y);
			}
		}
	}

	@IntDef({WAITING, DRAGGING})
	@Retention(RetentionPolicy.SOURCE)
	private @interface GameState {
	}

	@IntDef({NONE, HORIZONTAL, VERTICAL})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Orientation {
	}

	@IntDef({NONE, DIRECTION_LEFT, DIRECTION_TOP, DIRECTION_RIGHT, DIRECTION_BOTTOM})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Direction {
	}

	private interface Looper {
		void iteration(int x, int y);
	}
}
