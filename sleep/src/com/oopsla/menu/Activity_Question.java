package com.oopsla.menu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.sleepap.R;

public class Activity_Question extends Activity {
	
	private Button start_btn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question);
		
		start_btn = (Button)findViewById(R.id.Activity_Question_start_btn);
		
		start_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Activity_Question.this, MainActivity.class);
				startActivity(intent);				
			}
		});

	}
}