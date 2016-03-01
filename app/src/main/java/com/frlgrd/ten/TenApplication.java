package com.frlgrd.ten;

import android.app.Application;

import org.androidannotations.annotations.EApplication;

import timber.log.Timber;

@EApplication
public class TenApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Timber.plant(new Timber.DebugTree());
	}
}
