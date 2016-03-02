package com.frlgrd.ten.core;

public interface Level {

	/**
	 * Return row count of this level
	 */
	int getRow();

	/**
	 * Return column count of this level
	 */
	int getColumn();

	/**
	 * Return the tile presence probability
	 * 0 -> 0%
	 * 1 -> 100%
	 */
	float getTilesProbability();
}