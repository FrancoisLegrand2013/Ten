package com.frlgrd.ten.ui.component;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.frlgrd.ten.R;

public class GameView extends FrameLayout {

	private int row = 0;
	private int column = 0;
	private int tileColor1 = 0;
	private int tileColor2 = 0;

	private int tileSize;
	private Rect baseTileRect;
	private Paint basePaint;
	private boolean baseDrawn = false;

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

		int defaultSize = 5;
		TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.GameView);
		column = array.getInteger(R.styleable.GameView_column, defaultSize);
		row = array.getInteger(R.styleable.GameView_row, defaultSize);
		tileColor1 = array.getInteger(R.styleable.GameView_tileColor1, Color.BLACK);
		tileColor2 = array.getInteger(R.styleable.GameView_tileColor2, Color.DKGRAY);
		array.recycle();

		int maxSize = 20;
		int minSize = 2;
		if (column < minSize) {
			column = minSize;
		} else if (column > maxSize) {
			column = maxSize;
		}
		if (row < minSize) {
			row = minSize;
		} else if (row > maxSize) {
			row = maxSize;
		}

		baseTileRect = new Rect();
		basePaint = new Paint();
		basePaint.setStyle(Paint.Style.FILL);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (!baseDrawn) {
			int maxHeight = getMeasuredHeight() / row;
			int maxWidth = getMeasuredWidth() / column;
			if (maxHeight < maxWidth) {
				tileSize = maxHeight;
			} else {
				tileSize = maxWidth;
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!baseDrawn) {
			int horizontalOffset = (getMeasuredWidth() - column * tileSize) / 2;
			int verticalOffset = (getMeasuredHeight() - row * tileSize) / 2;
			for (int x = 0; x < column; x++) {
				for (int y = 0; y < row; y++) {
					baseTileRect.set(
							x * tileSize + horizontalOffset,
							y * tileSize + verticalOffset,
							(x + 1) * tileSize + horizontalOffset,
							(y + 1) * tileSize + verticalOffset
					);
					basePaint.setColor((x + y) % 2 == 0 ? tileColor1 : tileColor2);
					canvas.drawRect(baseTileRect, basePaint);
				}
			}
			baseDrawn = true;
		}
	}
}
