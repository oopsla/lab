package com.oopsla.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.sleepap.R;
import com.oopsla.analysis.RecordingsList;

public class MainActivity extends Activity implements OnClickListener {
	private final String INITIALIZED = "initialized"; // SharedPreferences 초기화.
	private Button monitoring_btn;
	private Button analysis_btn;
	private Button setup_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		monitoring_btn = (Button) findViewById(R.id.MainActivity_monitoring_btn);
		analysis_btn = (Button) findViewById(R.id.MainActivity_analysis_btn);
		setup_btn = (Button) findViewById(R.id.MainActivity_changeInfo_btn);

		monitoring_btn.setOnClickListener(this);
		analysis_btn.setOnClickListener(this);
		setup_btn.setOnClickListener(this);
		Log.e("teste", "dasdasdasdas");
	
		// 첫 실행시에 환자 정보입력을 위해 preference 사용.
		SharedPreferences myPrefs = getSharedPreferences("practice",
				MODE_PRIVATE);
		boolean hasPreferences = myPrefs.getBoolean(INITIALIZED, false);

		if (!hasPreferences) {

			// 첫 실행일 경우 설문조사 액티비티 호출
			Log.e("tag", "호출순서 확인. 환자정보");
			Intent intent = new Intent(this, Activity_Question.class);

			startActivity(intent);
			startActivity(new Intent(this, Activity_Loading.class));

			// Preferences 현재 상태 저장.
			Editor editor = myPrefs.edit();
			editor.putBoolean(INITIALIZED, true);
			editor.commit();
		} else
			startActivity(new Intent(this, Activity_Loading.class));			
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.MainActivity_monitoring_btn:
			Intent intent1 = new Intent(MainActivity.this,
					Activity_Monitoring_Set.class);
			startActivity(intent1);
			break;
		case R.id.MainActivity_analysis_btn:
			Intent intent2 = new Intent(MainActivity.this,
					RecordingsList.class);
			startActivity(intent2);
			break;
		case R.id.MainActivity_changeInfo_btn:
			Intent intent3 = new Intent(MainActivity.this,
					Activity_Question.class);
			startActivity(intent3);
			break;
		}
	}

}
