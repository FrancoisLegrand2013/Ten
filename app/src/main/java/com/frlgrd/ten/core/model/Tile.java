package com.frlgrd.ten.core.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;

import com.frlgrd.ten.R;

import java.util.Random;

public class Tile {

	private RectF position;
	private int value;

	public static int getTilesColor(Context context, int value) {
		if (value < 0) {
			return Color.WHITE;
		} else if (value == 0) {
			return Color.RED;
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

	public boolean canMergeWith(Tile tile) {
		if (tile == null) {
			return false;
		}
		if (tile == this) {
			return false;
		}
		if (getPosition().left != tile.getPosition().left && getPosition().top != tile.getPosition().top) {
			return false;
		}
		if (getValue() == 0 || tile.getValue() == 0) {
			return false;
		}
		if (getValue() + tile.getValue() <= 10) {
			return true;
		}
		return false;
	}

	public boolean canBeDragged() {
		return getValue() != 10 && getValue() != 0;
	}

	public RectF getPosition() {
		return position;
	}

	public void setPosition(RectF position) {
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
