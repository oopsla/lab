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
// sleep �ۿ��� ����

public class Activity_Question extends Activity {

	/*
	 * // Ű, ������, ������ ���� ����(kg, sp, p...)���� �ޱ� ������ �����ϴµ� ���⼭�� �ʿ����
	 * 
	 * // ���� ���� private enum HeightUnit { 
	 * // ����, ��Ʈ��ġ Metres, FeetInches } 
	 * // ���Դ��� private enum WeightUnit { 
	 * // ų�α׷�, ���� �Ŀ��, �Ŀ�� Kilograms, StonePounds, Pounds } 
	 * // �� ũ�� ���� private enum NeckSizeUnit { 
	 * // ��ġ, ��Ƽ���� Inches, Centimetres }
	 */

	// ���� (���� | ���� | �� ũ��) ����
	/*
	 * // Ű, ������, ������ ���� ����(kg, sp, p...)���� �ޱ� ������ �����ϴµ� ���⼭�� �ʿ���� private
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
	 * // Ű, ������, ������ ���� ����(kg, sp, p...)���� �ޱ� ������ �����ϴµ� ���⼭�� �ʿ���� private
	 * Spinner heightUnitSpinner; private Spinner weightUnitSpinner; private
	 * Spinner neckSizeUnitSpinner; private Spinner ethnicitySpinner;
	 */

	// Ȱ���� ó�� ������� �� ȣ���
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question);

		// �ڸ� ũ�� ��ʴϱ�?
		stopBang1 = (RadioGroup) findViewById(R.id.radioGroup1);
		// ������ �Ϸ絿�� �ǰ��Ͻʴϱ�?
		stopBang2 = (RadioGroup) findViewById(R.id.radioGroup2);
		// �ٸ� ����� ����� ���� �� ȣ���� ���� ���� �� �� �ֽ��ϱ�?
		stopBang3 = (RadioGroup) findViewById(R.id.radioGroup3);
		// �������� �����ʴϱ�?
		stopBang4 = (RadioGroup) findViewById(R.id.radioGroup4);
		// Ű
		height = (EditText) findViewById(R.id.Activity_Question_height);
		// ������
		weight = (EditText) findViewById(R.id.Activity_Question_weight);
		// Ű
		age = (EditText) findViewById(R.id.Activity_Question_age);
		// �� �ѷ�
		neckSize = (EditText) findViewById(R.id.Activity_Question_neck);
		// ����
		stopBang8 = (RadioGroup) findViewById(R.id.radioGroup5);

		// ���� ��ư
		Button next = (Button) findViewById(R.id.Activity_Question_start_btn);
		next.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// ������ ���� �亯�� ���ִ� | �� ���� �ʴ�.
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

				// ��� �亯�� üũ �Ǿ����� Ȯ���ϴ� ����
				boolean allAnswered = true;

				/* �� ������ �亯���� ���� ��� ������ ��Ʈ�� ��� */
				// ���������� �ٲپ� �� ����
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
							"��� ������ �亯���ּ���. �亯���� ���� ������ ���������� ��Ÿ���ϴ�.",
							Toast.LENGTH_LONG).show();
					return;
				}

				/*
				 * responses[11]�� �ε����� ��� �� 0 �ڸ� ��°� 1 ������ �Ϸ絿�� �ǰ��Ͻʴϱ�? 
				 * 2 �ٸ� ����� �� ȣ�� ����� ��? 
				 * 3 ������ �ֳ�?
				 * 
				 * folat bmi = weightKg / (heightM * heightM)
				 * 
				 * 4 String.valueOf(bmi)
				 * 5 String.valueof(ageInt) 
				 * 6 String.valueof(neckSizeInches) 
				 * 7 ������ �����Դϱ�?
				 * 8 Ű 
				 * 9 ������ 
				 * 10 String.valueOf(score)
				 */

				// ���� ���
				int score = 0;

				// ~ �̵���
				String[] responses = new String[11];

				// ���
				float heightM;
				float weightKg;

				/*
				 * switch���� �ƴ϶� ������ �� ���� ����
				 */

				// cm�� ���� edittext�� m�� �ٲپ��ָ鼭 float������ ��ȯ�Ѵ�.

				heightM = floatFromEditText(height) / 100;
				responses[8] = Float.toString(heightM);

				// ���Ը� ����Ѵ�. (Kilograms)
				weightKg = floatFromEditText(weight);
				responses[9] = Float.toString(weightKg);

				// Ű�� ���Ը� �̿��Ͽ� bmi ���
				float bmi = weightKg / (heightM * heightM);

				// ���� ���
				int ageInt = (int) floatFromEditText(age);

				// �� ������ ���
				float neckSizeInches;

				// ��ġ���͸� ��ġ�� ��ȯ
				neckSizeInches = 0.394f*floatFromEditText(neckSize);

				// Add relevant score for each answer. Radio button IDs are the
				// order on which they appear in the XML layout file, so if they
				// are changed the 'if' statements in the scoring below must be
				// adjusted.
				// ��... �ؼ� �� ��Ź�帳�ϴ�.

				// ū �Ҹ��� �ڸ� ��°�?
				if (stopBang1.getCheckedRadioButtonId() == R.id.radioGroup1) {
					score++;
					responses[0] = "1"; // yes
				} else {
					responses[0] = "0"; // no
				}

				// ���� �ǰ�?
				if (stopBang2.getCheckedRadioButtonId() == R.id.radioGroup2) {
					score++;
					responses[1] = "1";
				} else {
					responses[1] = "0";
				}

				// ���� �� ȣ���� ���� �ʴ°� ����
				if (stopBang3.getCheckedRadioButtonId() == R.id.radioGroup3) {
					score++;
					responses[2] = "1";
				} else {
					responses[2] = "0";
				}

				// ������ ġ�Ḧ �޾���?
				if (stopBang4.getCheckedRadioButtonId() == R.id.radioGroup4) {
					score++;
					responses[3] = "1";
				} else {
					responses[3] = "0";
				}

				// bmi > 35�̸� score �ø�
				if (bmi > 35) {
					score++;
				}
				// [4] bmi
				responses[4] = String.valueOf(bmi);

				// ���̰� 50 �̻��̸� score �ø�
				if (ageInt > 50) {
					score++;
				}
				// [5] ����
				responses[5] = String.valueOf(ageInt);

				// ������ 16�� ���ų� ũ�� score �ø�
				if (neckSizeInches >= 16) {
					score++;
				}
				// [6] �� ������
				responses[6] = String.valueOf(neckSizeInches);

				// ����
				if (stopBang8.getCheckedRadioButtonId() == R.id.radioGroup5) {
					score++;
					responses[7] = "1";
				} else {
					responses[7] = "0";
				}

				// [10] score
				responses[10] = String.valueOf(score);

				// ��Ͽ� ���� �亯
				try {
					// csv ������ ����, ���� ��
					// �����ϸ� �浹��
					File appDir = new File(Environment
							.getExternalStorageDirectory().toString()
							+ "/"
							+ getResources().getString(R.string.app_name) + "/");

					// ���丮�� ����
					/* �� �κ� �ٽ� ���� */
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