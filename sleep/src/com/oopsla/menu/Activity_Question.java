package com.oopsla.menu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sleepap.R;
import com.oopsla.common.Constants;
// sleep 앱에는 없음

public class Activity_Question extends Activity {

	/*
	 * // 키, 몸무게, 목사이즈를 여러 단위(kg, sp, p...)으로 받기 때문에 선언하는데 여기서는 필요없음
	 * 
	 * // 높이 단위 private enum HeightUnit { 
	 * // 미터, 피트인치 Metres, FeetInches } 
	 * // 무게단위 private enum WeightUnit { 
	 * // 킬로그램, 스톤 파운드, 파운드 Kilograms, StonePounds, Pounds } 
	 * // 목 크기 단위 private enum NeckSizeUnit { 
	 * // 인치, 센티미터 Inches, Centimetres }
	 */

	// 선택 (높이 | 무게 | 목 크기) 단위
	/*
	 * // 키, 몸무게, 목사이즈를 여러 단위(kg, sp, p...)으로 받기 때문에 선언하는데 여기서는 필요없음 private
	 * HeightUnit selectedHeightUnit; private WeightUnit selectedWeightUnit;
	 * private NeckSizeUnit selectedNeckSizeUnit;
	 */
	private EditText height;
	private EditText weight;
	private EditText age;
	private EditText neckSize;
	private RadioGroup stopBang1;
	private RadioGroup stopBang2;
	private RadioGroup stopBang3;
	private RadioGroup stopBang4;
	private RadioGroup stopBang8;

	/*
	 * // 키, 몸무게, 목사이즈를 여러 단위(kg, sp, p...)으로 받기 때문에 선언하는데 여기서는 필요없음 private
	 * Spinner heightUnitSpinner; private Spinner weightUnitSpinner; private
	 * Spinner neckSizeUnitSpinner; private Spinner ethnicitySpinner;
	 */

	// 활동이 처음 만들어질 때 호출됨
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question);

		// 코를 크게 고십니까?
		stopBang1 = (RadioGroup) findViewById(R.id.radioGroup1);
		// 가끔씩 하루동안 피곤하십니까?
		stopBang2 = (RadioGroup) findViewById(R.id.radioGroup2);
		// 다른 사람이 당신의 수면 중 호흡이 멈춘 것을 본 적 있습니까?
		stopBang3 = (RadioGroup) findViewById(R.id.radioGroup3);
		// 고혈압이 있으십니까?
		stopBang4 = (RadioGroup) findViewById(R.id.radioGroup4);
		// 키
		height = (EditText) findViewById(R.id.Activity_Question_height);
		// 몸무게
		weight = (EditText) findViewById(R.id.Activity_Question_weight);
		// 키
		age = (EditText) findViewById(R.id.Activity_Question_age);
		// 목 둘레
		neckSize = (EditText) findViewById(R.id.Activity_Question_neck);
		// 성별
		stopBang8 = (RadioGroup) findViewById(R.id.radioGroup5);

		// 시작 버튼
		Button next = (Button) findViewById(R.id.Activity_Question_start_btn);
		next.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 질문에 대한 답변이 되있다 | 되 있지 않다.
				boolean question1Answered = stopBang1.getCheckedRadioButtonId() > -1;
				boolean question2Answered = stopBang2.getCheckedRadioButtonId() > -1;
				boolean question3Answered = stopBang3.getCheckedRadioButtonId() > -1;
				boolean question4Answered = stopBang4.getCheckedRadioButtonId() > -1;
				boolean question5Answered = !(height.getText().toString()
						.equals("") || weight.getText().toString().equals(""));
				boolean question6Answered = !(age.getText().toString()
						.equals(""));
				boolean question7Answered = !(neckSize.getText().toString()
						.equals(""));
				boolean question8Answered = stopBang8.getCheckedRadioButtonId() > -1;

				// 모든 답변이 체크 되었는지 확인하는 변수
				boolean allAnswered = true;

				/* 각 질문이 답변되지 않은 경우 빨간색 폰트로 경고 */
				// 빨간색으로 바꾸어 줄 변수
				int lightRed = getResources().getColor(R.color.darkred);
				if (!question1Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang1question))
							.setTextColor(lightRed);
				}
				if (!question2Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang2question))
							.setTextColor(lightRed);
				}
				if (!question3Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang3question))
							.setTextColor(lightRed);
				}
				if (!question4Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang4question))
							.setTextColor(lightRed);
				}
				if (!question5Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang5question))
							.setTextColor(lightRed);
				}
				if (!question6Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang6question))
							.setTextColor(lightRed);
				}
				if (!question7Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang7question))
							.setTextColor(lightRed);
				}
				if (!question8Answered) {
					allAnswered = false;
					((TextView) findViewById(R.id.stopbang8question))
							.setTextColor(lightRed);
				}

				if (!allAnswered) {
					Toast.makeText(getApplicationContext(),
							"모든 질문에 답변해주세요. 답변되지 않은 질문이 빨간색으로 나타납니다.",
							Toast.LENGTH_LONG).show();
					return;
				}

				/*
				 * responses[11]의 인덱스에 담긴 뜻 0 코를 고는가 1 가끔씩 하루동안 피곤하십니까? 
				 * 2 다른 사람이 니 호흡 멈춘거 봄? 
				 * 3 고혈압 있냐?
				 * 
				 * folat bmi = weightKg / (heightM * heightM)
				 * 
				 * 4 String.valueOf(bmi)
				 * 5 String.valueof(ageInt) 
				 * 6 String.valueof(neckSizeInches) 
				 * 7 성별이 무엇입니까?
				 * 8 키 
				 * 9 몸무게 
				 * 10 String.valueOf(score)
				 */

				// 점수 계산
				int score = 0;

				// ~ 이따가
				String[] responses = new String[11];

				// 계산
				float heightM;
				float weightKg;

				/*
				 * switch문이 아니라서 에러가 날 수도 있음
				 */

				// cm로 받은 edittext를 m로 바꾸어주면서 float형으로 변환한다.

				heightM = floatFromEditText(height) / 100;
				responses[8] = Float.toString(heightM);

				// 무게를 계산한다. (Kilograms)
				weightKg = floatFromEditText(weight);
				responses[9] = Float.toString(weightKg);

				// 키와 무게를 이용하여 bmi 계산
				float bmi = weightKg / (heightM * heightM);

				// 나이 계산
				int ageInt = (int) floatFromEditText(age);

				// 목 사이즈 계산
				float neckSizeInches;

				// 센치미터를 인치로 변환
				neckSizeInches = 0.394f*floatFromEditText(neckSize);

				// Add relevant score for each answer. Radio button IDs are the
				// order on which they appear in the XML layout file, so if they
				// are changed the 'if' statements in the scoring below must be
				// adjusted.
				// 해... 해석 좀 부탁드립니다.

				// 큰 소리로 코를 고는가?
				if (stopBang1.getCheckedRadioButtonId() == R.id.radioGroup1) {
					score++;
					responses[0] = "1"; // yes
				} else {
					responses[0] = "0"; // no
				}

				// 낮에 피곤?
				if (stopBang2.getCheckedRadioButtonId() == R.id.radioGroup2) {
					score++;
					responses[1] = "1";
				} else {
					responses[1] = "0";
				}

				// 수면 중 호흡을 하지 않는걸 관찰
				if (stopBang3.getCheckedRadioButtonId() == R.id.radioGroup3) {
					score++;
					responses[2] = "1";
				} else {
					responses[2] = "0";
				}

				// 고혈압 치료를 받았음?
				if (stopBang4.getCheckedRadioButtonId() == R.id.radioGroup4) {
					score++;
					responses[3] = "1";
				} else {
					responses[3] = "0";
				}

				// bmi > 35이면 score 올림
				if (bmi > 35) {
					score++;
				}
				// [4] bmi
				responses[4] = String.valueOf(bmi);

				// 나이가 50 이상이면 score 올림
				if (ageInt > 50) {
					score++;
				}
				// [5] 나이
				responses[5] = String.valueOf(ageInt);

				// 목사이즈가 16과 같거나 크면 score 올림
				if (neckSizeInches >= 16) {
					score++;
				}
				// [6] 목 사이즈
				responses[6] = String.valueOf(neckSizeInches);

				// 성별
				if (stopBang8.getCheckedRadioButtonId() == R.id.radioGroup5) {
					score++;
					responses[7] = "1";
				} else {
					responses[7] = "0";
				}

				// [10] score
				responses[10] = String.valueOf(score);

				// 기록에 대한 답변
				try {
					// csv 파일을 만듬, 변경 놉
					// 변경하면 충돌남
					File appDir = new File(Environment
							.getExternalStorageDirectory().toString()
							+ "/"
							+ getResources().getString(R.string.app_name) + "/");

					// 디렉토리를 만듬
					/* 이 부분 다시 보기 */
					if (!appDir.exists()) {
						appDir.mkdirs();
					}
					
					File outputFile = new File(appDir, Constants.FILENAME_QUESTIONNAIRE);
					FileWriter fileWriter = new FileWriter(outputFile);
					BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
					for (int i = 0; i < responses.length; i++) {
						if (!responses[i].isEmpty()) {
							bufferedWriter.write(responses[i] + "\n");
						} else {
							bufferedWriter.write("NaN \n");
						}
					}

					bufferedWriter.flush();
					bufferedWriter.close();
					fileWriter.close();
				} catch (IOException e) {
					Log.e("StopBang", "IOException when writing to file");
				}
				
				/*
				Intent intent = new Intent(Activity_Question.this, StopBangResults.class);
				intent.putExtra(Constants.EXTRA_SCORE, score);
				startActivity(intent);
				overridePendingTransition(R.anim.enteringfromright, R.anim.exitingtoleft);				
				*/
				
	            Intent intent = new Intent(Activity_Question.this, MainActivity.class);
	            //   intent.putExtra(Constants.EXTRA_SCORE, score);
	            startActivity(intent);
	            //   overridePendingTransition(R.anim.enteringfromright, R.anim.exitingtoleft);
			}
		});
	}

	private float floatFromEditText(EditText view) {
		String text = view.getText().toString();
		if (text.equals("")) {
			return 0;
		}
		return Float.parseFloat(text);
	}
}