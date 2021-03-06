package com.frlgrd.ten.core;

import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.frlgrd.ten.core.model.Tile;

import java.util.Random;

public class LevelGenerator {

	public static Tile[][] generateRandom(RectF[][] rects, @NonNull Level level) {
		Tile[][] tiles = new Tile[level.getColumn()][level.getRow()];
		Random random = new Random();
		for (int x = 0; x < level.getColumn(); x++) {
			for (int y = 0; y < level.getRow(); y++) {
				if (random.nextDouble() > level.getTilesProbability()) {
					tiles[x][y] = null;
				} else {
					tiles[x][y] = new Tile();
					tiles[x][y].setPosition(rects[x][y]);
				}
			}
		}
		int sum;
		int cpt;
		float average;
		do {
			sum = 0;
			cpt = 0;
			average = 0;
			for (int x = 0; x < level.getColumn(); x++) {
				for (int y = 0; y < level.getRow(); y++) {
					Tile tile = tiles[x][y];
					if (tile != null) {
						tile.setValue(Tile.TileValueGenerator.value(-1, 9));
						sum += tile.getValue();
						cpt++;
						average = sum / (float) cpt;
					}
				}
			}
		} while (sum % 10 != 0 || average > 4);
		return tiles;
	}

	public static Tile[][] generate(RectF[][] rects, @NonNull Level level) {
		Tile[][] tiles = new Tile[level.getColumn()][level.getRow()];
		for (int x = 0; x < level.getColumn(); x++) {
			for (int y = 0; y < level.getRow(); y++) {
				// TODO
			}
		}
		return tiles;
	}

	public static class RandomLevel implements Level {

		@Override
		public int getRow() {
			return 5;
		}

		@Override
		public int getColumn() {
			return 5;
		}

		@Override
		public float getTilesProbability() {
			return .4F;
		}
	}
}
