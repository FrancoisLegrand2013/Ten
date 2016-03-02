package com.frlgrd.ten.core.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;

import com.frlgrd.ten.R;

import java.util.Random;

public class Tile {

	private Rect position;
	private int value;

	public static int getTilesColor(Context context, int value) {
		if (value < 0) {
			return Color.WHITE;
		} else if (value == 0) {
			return Color.DKGRAY;
		} else {
			int[] colors = context.getResources().getIntArray(R.array.tiles_color);
			return colors[value % colors.length];
		}
	}

	public static int getValueColor(int value) {
		if (value < 0) {
			return Color.RED;
		} else if (value == 0) {
			return Color.WHITE;
		} else {
			return Color.WHITE;
		}
	}

	public Rect getPosition() {
		return position;
	}

	public void setPosition(Rect position) {
		this.position = position;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public static class TileValueGenerator {
		private static Random rand;

		static {
			rand = new Random();
		}

		public static int value(int min, int max) {
			int value = rand.nextInt((max - min) + 1) + min;
			return value == 0 ? value(min, max) : value;
		}
	}
}
