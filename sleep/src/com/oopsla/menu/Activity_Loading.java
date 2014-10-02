package com.oopsla.menu;

import com.example.sleepap.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class Activity_Loading extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);

		Handler hd = new Handler();
		hd.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				finish();
			}
		}, 3000);
	}
}
