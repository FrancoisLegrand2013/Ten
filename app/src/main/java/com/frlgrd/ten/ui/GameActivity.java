package com.frlgrd.ten.ui;

import android.support.v7.app.AppCompatActivity;

import com.frlgrd.ten.R;
import com.frlgrd.ten.core.LevelGenerator;
import com.frlgrd.ten.ui.component.GameView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(value = R.layout.game_layout)
public class GameActivity extends AppCompatActivity {

	@ViewById
	GameView gameView;

	@AfterViews
	void afterViews() {
		gameView.start(new LevelGenerator.RandomLevel());
	}
}
