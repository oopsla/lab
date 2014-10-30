/**
 * Copyright (c) 2013, J. Behar, A. Roebuck, M. Shahid, J. Daly, A. Hallack, 
 * N. Palmius, K. Niehaus, G. Clifford (University of Oxford). All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * 	1. 	Redistributions of source code must retain the above copyright notice, this 
 * 		list of conditions and the following disclaimer.
 * 	2.	Redistributions in binary form must reproduce the above copyright notice, 
 * 		this list of conditions and the following disclaimer in the documentation
 * 		and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * NOT MEDICAL SOFTWARE.
 * 
 * This software is provided for informational or research purposes only, and is not
 * for professional medical use, diagnosis, treatment or care, nor is it intended to
 * be a substitute therefor. Always seek the advice of a physician or other qualified
 * health provider properly licensed to practice medicine or general healthcare in
 * your jurisdiction concerning any questions you may have regarding any health
 * problem. Never disregard professional medical advice or delay in seeking it
 * because of something you have observed through the use of this software. Always
 * consult with your physician or other qualified health care provider before
 * embarking on a new treatment, diet or fitness programme.
 * 
 * Graphical charts copyright (c) AndroidPlot (http://androidplot.com/), SVM 
 * component copyright (c) LIBSVM (http://www.csie.ntu.edu.tw/~cjlin/libsvm/) - all 
 * rights reserved.
 * */
/*
 * onCreate에서 csv 파일 생성 및 기록을 시작(오디오는 스레드를 시작하지만 기록은 하지 않음, 자세랑 가속도는 센서 값이 들어올 때마다 기록함)
 * 그리고 나서 UserInterface 스레드를 돌림. UserInterface는 onCreate에서 설정한 flag 값에 따라서 해당 그래프를 그려줌
 * UserInterface에 doInBackground에서 delaytime이 지나면 오디오 녹음을 시작함. 
 */

package com.oopsla.monitoring;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidplot.Plot;
import com.androidplot.series.XYSeries;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.SimpleXYSeries.ArrayFormat;
import com.androidplot.xy.XYPlot;
import com.example.sleepap.R;
import com.oopsla.common.Constants;
import com.oopsla.common.CustomExceptionHandler;
import com.oopsla.common.Utils;
import com.oopsla.device.BITlog;
import com.oopsla.device.BitalinoThread;
import com.oopsla.menu.MainActivity;

public class Activity_Monitoring extends Activity implements
		SensorEventListener {

	private File orientationFile, accelerationFile, actigraphyFile,
			audioProcessedFile, audioRawFile, bodyPositionFile, apneaFile;
	private Position position = Position.Supine;
	int oldPositionValue = Constants.CODE_POSITION_SUPINE;
	private SensorManager sensorManager;
	private Sensor accelerometer, magnetometer;
	long accelerometerCurrentTime = 0;
	long lastAccelerometerReadTime = 0;
	long[] totalPositionTime = new long[5];
	long lastPositionChangeTime;
	long lastAccelerometerRecordedTime;
	private float[] latestAccelerometerEventValues = new float[3];
	private float[] runningGravityComponents = new float[3];
	private float[] previousXAccels = new float[4];
	private float[] previousYAccels = new float[4];
	private float[] previousZAccels = new float[4];
	private double gravitySum = 0;
	private double gravitySquaredSum = 0;
	private int varianceCounter = 0;
	private float[] mGeoMags = new float[3];
	private float[] mOrientation = new float[3];
	private float[] mRotationM = new float[9];
	private String dateTimeString;
	private String filesDirPath;
	private NotificationManager notificationManager;
	private Queue<Double> actigraphyQueue;
	private UserInterfaceUpdater graphUpdateTask;
	private SharedPreferences sharedPreferences;
	private TextView positionDisplay;
	private ExtAudioRecorder extAudioRecorder;
	private WakeLock wakeLock;
	private AlertDialog delayAlertDialog;
	private Calendar startTime;
	private boolean startRecordingFlag;
	private boolean finishRecordingFlag;
	private ImageView recordingSign;
	private boolean screenLocked;
	private boolean actigraphyEnabled, audioEnabled, ecgEnabled;
	private long recordingStartDelayMs, recordingDurationMs;
	
	private int apnea_Count = 0;
	private TextView apneaText;

	//ecg
	BitalinoThread bitalinoThread;
	private final String ACTIVITY_MONITORING = "ACTIVITY_MONITORING";
	
	private enum Position {
		Supine, Prone, Left, Right, Sitting
	}

	private BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Stop recording if battery level is less than 5%.
			int batteryLevel = intent
					.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,
					100);
			float percentage = (float) batteryLevel / (float) batteryScale;
			if (percentage < Constants.PARAM_BATTERY_NOTIFICATION_THRESHOLD) {
				stopRecording();
			}
		}
	};

	// 일반 호흡보다 호흡세기가 반이 적으면서 10초이상 지속되면 저호출
	private ExtAudioRecorder.Apnea a_Callback = new ExtAudioRecorder.Apnea() {

		@Override
		public void apena_breath() {
			// TODO Auto-generated method stub
			apnea_Count++;
			Log.e("Apnea count ++", "콜백메소드 호출 호출");

			// 기록
			writeApneaRecord();
		}
	};

	public void writeApneaRecord() {
		// 시간 측정
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Log.e("무호흡 발생 ", "현재 시간 : " + dataFormat.format(calendar.getTime()));

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(apneaFile,
					true));
			out.append(String.valueOf(dataFormat.format(calendar.getTime()))
					+ ",");
			out.append(String.valueOf(apnea_Count + "\n"));
			out.flush();
			out.close();

			Log.e("레코딩안하냐?", "ㅇㄴㅁㅇㅁㄴㅇㅁㅇㄴㅁㅇㄴㅁㅇㅁㄴ");
		} catch (IOException e) {
			Log.e(Constants.CODE_APP_TAG,
					"Error writing raw actigraphy data to file", e);
		}
	}

	private class UserInterfaceUpdater extends AsyncTask<Void, Void, String> {
		private XYPlot _activityPlot;
		private XYPlot _audioPlot;
		private XYPlot _ecgPlot;
		
		// private XYPlot _breathPlot;
		private boolean _stopRunningFlag;
		private int _counter;
		private PointLabelFormatter _plf;
		private LineAndPointFormatter _activityFormatter;
		private LineAndPointFormatter _audioFormatter;
		private LineAndPointFormatter _ecgFormatter;	
		
		// private LineAndPointFormatter _breathFormatter;
		// private XYSeries _breathSeries;
		private XYSeries _activitySeries;
		private XYSeries _audioSeries;
		private XYSeries _ecgSeries;

		@Override
		protected String doInBackground(Void... params) {
			// Graph module
			_activityPlot = (XYPlot) findViewById(R.id.activityPlot);
			_audioPlot = (XYPlot) findViewById(R.id.audioPlot);
			_ecgPlot = (XYPlot) findViewById(R.id.ecgPlot);
			
			// _breathPlot = (XYPlot) findViewById(R.id.breath_count);

			_plf = new PointLabelFormatter(getResources().getColor(
					R.color.transparent));
			_activityFormatter = new LineAndPointFormatter(getResources()
					.getColor(R.color.darkgreen), null, getResources()
					.getColor(R.color.transparent), _plf);
			_audioFormatter = new LineAndPointFormatter(Color.BLUE, null,
					getResources().getColor(R.color.transparent), _plf);
			_ecgFormatter = new LineAndPointFormatter(Color.RED, null, 
					getResources().getColor(R.color.transparent), _plf);
			
			//활동사항
			if(ecgEnabled){
				initialisePlot(_ecgPlot);
			}
			else{
				((LinearLayout) findViewById(R.id.ecgDisplay))
				.setVisibility(View.GONE);
			}
			if (actigraphyEnabled) {
				Log.e("test", "actigraphy");
				initialisePlot(_activityPlot);
				Log.e("test", "actigraphy end");
			} else {
				// 그래프 보이도ㅗ록
				((LinearLayout) findViewById(R.id.activityDisplay))
						.setVisibility(View.GONE);
			}
			if (audioEnabled) {
				initialisePlot(_audioPlot);
			} else {
				((LinearLayout) findViewById(R.id.audioDisplay))
						.setVisibility(View.GONE);
			}
			//ecg 출력하는 그래프
			

			_stopRunningFlag = false;
			// UI가 갱신되게끔하는 반복문
			while (!_stopRunningFlag) {
				try {
					Thread.sleep(Constants.PARAM_UI_UPDATE_PERIOD);
				} catch (InterruptedException e) {
					Log.e(Constants.CODE_APP_TAG,
							"InterruptedException during UI thread sleep", e);
					continue;
				}

				// 딜레이시간 가져와서 딜레이 시간 만큼 지나고 나면 기록을 시작함
				if (_counter == Constants.PARAM_FLAGS_CHECK_PERIOD) {
					Calendar now = Calendar.getInstance(Locale.getDefault());
					// 경과시간
					long timeSinceStartMillis = now.getTimeInMillis()
							- startTime.getTimeInMillis();
					Log.e("경과시간 : ", timeSinceStartMillis + ""); // /////////////////
					if (!startRecordingFlag) {
						Log.e("경과시간 : ", timeSinceStartMillis + ""); // /////////////////

						// 수면 상태에서 레코딩을 시작함 그러기위해 기록할 수 있도록 true전달
						if (timeSinceStartMillis > recordingStartDelayMs) {
							startRecordingFlag = true;
							if (audioEnabled) {
								// 오디오 레코드 기록하도록 플래그 값을 true로 만들어줌
							}
							if (delayAlertDialog != null) {
								delayAlertDialog.cancel();
							}
							// 레코딩을 시작하면 이미지 변경
							if (sharedPreferences.getBoolean(
									Constants.PREF_NOTIFICATIONS,
									Constants.DEFAULT_NOTIFICATIONS)) {
								NotificationCompat.Builder builder = new NotificationCompat.Builder(
										getApplicationContext())
										.setSmallIcon(
												R.drawable.notification_icon)
										.setLargeIcon(
												BitmapFactory
														.decodeResource(
																getResources(),
																R.drawable.deviceaccessmic))
										.setContentTitle("SleepAp")
										.setContentText(
												getString(R.string.startedRecordingNotification))
										.setAutoCancel(true);
								notificationManager.notify(
										Constants.CODE_APP_NOTIFICATION_ID,
										builder.build());
							}
						}
					} else {
						// 경과시간이 지나면 종료함
						if (timeSinceStartMillis > recordingStartDelayMs
								+ recordingDurationMs) {
							finishRecordingFlag = true;
						}
					}
					_counter = 0;
				}
				_counter++;
				publishProgress(); // onProgressUpdate 메소드 호출
			}
			return "Stopped";
		}

		// 배경 및 세팅
		private void initialisePlot(XYPlot plot) {
			Paint whitePaint = new Paint();
			whitePaint.setColor(Color.WHITE);
			whitePaint.setAlpha(255);
			plot.getLayoutManager().remove(plot.getLegendWidget());
			plot.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
			plot.setBorderPaint(null);
			plot.getGraphWidget().getRangeLabelPaint().setColor(Color.WHITE);
			plot.getGraphWidget().getDomainLabelPaint().setColor(Color.WHITE);
			plot.getGraphWidget().getRangeOriginLabelPaint()
					.setColor(Color.WHITE);
			plot.getGraphWidget().getDomainOriginLabelPaint()
					.setColor(Color.WHITE);
			plot.getGraphWidget().getBackgroundPaint().setAlpha(0);
			plot.getGraphWidget().setGridBackgroundPaint(whitePaint);
			plot.setDomainLabel("");
			plot.setRangeLabel("");
			plot.setTitle("");
			plot.getGraphWidget().getRangeLabelPaint().setAlpha(0);
			plot.getGraphWidget().getDomainLabelPaint().setAlpha(0);
			plot.getGraphWidget().getDomainOriginLabelPaint().setAlpha(0);
			plot.getGraphWidget().getRangeOriginLabelPaint().setAlpha(0);
			plot.setPlotPadding(-60, -22, -5, -35); // L,T,R,B
			plot.setMarkupEnabled(false);
			plot.getGraphWidget().getGridLinePaint().setAlpha(0);
		}

		// UI업데이트
		@Override
		protected void onProgressUpdate(Void... progress) {

			if (finishRecordingFlag) {
				stopRecording();	
			} else {
				if (startRecordingFlag
						&& recordingSign.getVisibility() == View.GONE) {
					recordingSign.setVisibility(View.VISIBLE);
				}
			}

			if (!screenLocked) {
				// Only bother updating the UI if the screen is currently
				// unlocked.
				try {
					if(ecgEnabled){
						Queue<Integer> ecgQueue = bitalinoThread.getQueue();				
						List<Number> ecgVals = integerQueueToNumberList(ecgQueue);
						_ecgSeries = new SimpleXYSeries(ecgVals,
								ArrayFormat.Y_VALS_ONLY, "");
						_ecgPlot.removeSeries(_ecgSeries);
						_ecgPlot.clear();
						_ecgPlot.addSeries(_ecgSeries, _ecgFormatter);
						_ecgPlot.redraw();
					}
					// Activity.
					if (actigraphyEnabled) {
						List<Number> activityVals = doubleQueueToNumberList(actigraphyQueue);
						_activitySeries = new SimpleXYSeries(activityVals,
								ArrayFormat.Y_VALS_ONLY, "");
						_activityPlot.removeSeries(_activitySeries);
						_activityPlot.clear();
						_activityPlot.addSeries(_activitySeries,
								_activityFormatter);
						_activityPlot.redraw();
						_activityPlot.setRangeBoundaries(
								Constants.PARAM_ACTIVITY_GRAPH_MIN_Y,
								BoundaryMode.FIXED,
								Constants.PARAM_ACTIVITY_GRAPH_MAX_Y,
								BoundaryMode.FIXED);
					}

					// Position.
					String positionText;
					if (position == Position.Prone) {
						positionText = "엎드림";
					} else if (position == Position.Supine) {
						positionText = "누움";
					} else if (position == Position.Left) {
						positionText = "왼쪽으로 누음";
					} else if (position == Position.Right) {
						positionText = "오른쪽으로 누움";
					} else if (position == Position.Sitting) {
						positionText = "앉아있음";
					} else {
						positionText = position.toString();
					}

					positionDisplay
							.setText(getString(R.string.estimatedPosition)
									+ " " + positionText);

					// Audio.
					if (audioEnabled) {
						Queue<Double> audioQueue = extAudioRecorder
								.getAudioQueue();
						
						//Log.e(ACTIVITY_MONITORING, "오디오 사이즈 : " + extAudioRecorder.getAudioQueue().size());

						List<Number> audioVals = doubleQueueToNumberList(audioQueue);
						_audioSeries = new SimpleXYSeries(audioVals,
								ArrayFormat.Y_VALS_ONLY, "");
						_audioPlot.removeSeries(_audioSeries);
						_audioPlot.clear();
						_audioPlot.addSeries(_audioSeries, _audioFormatter);
						_audioPlot.redraw();
						_audioPlot.setRangeBoundaries(
								Constants.PARAM_AUDIO_GRAPH_MIN_Y,
								BoundaryMode.FIXED,
								Constants.PARAM_AUDIO_GRAPH_MAX_Y,
								BoundaryMode.FIXED);
					}
					// 무호흡을 출력함
					if (true) {
						apneaText.setText(getString(R.string.apneaLabel)
								+ apnea_Count);
					}
				} catch (OutOfMemoryError e) {
					Log.e(Constants.CODE_APP_TAG,
							"OutOfMemoryError during UI update", e);
					resetUi();
				}
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result.equals("Stopped")) {
				// Thread was cancelled.
			}
		}

		public void stopUiUpdates() {
			_stopRunningFlag = true;
		}

		public boolean isRunning() {
			return !_stopRunningFlag;
		}
	}

	/**
	 * Called when the activity is first created. onStart() is called
	 * immediately afterwards.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitoring);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		// 처리가 안되면 로그 데이터를 저장함 기본 경로에 log파일 저장
		if (sharedPreferences.getBoolean(Constants.PREF_WRITE_LOG,
				Constants.DEFAULT_WRITE_LOG)) {
			String bugDirPath = Environment.getExternalStorageDirectory()
					.toString()
					+ "/"
					+ getString(R.string.app_name)
					+ "/"
					+ Constants.FILENAME_LOG_DIRECTORY;
			File bugDir = new File(bugDirPath);
			if (!bugDir.exists()) {
				bugDir.mkdirs();
			}

			// sql역 분석하는 trace파일
			String handledFileName = bugDirPath + "/logcat"
					+ System.currentTimeMillis() + ".trace";
			String unhandledFileName = bugDirPath + "/unhandled"
					+ System.currentTimeMillis() + ".trace";
			// Log any warning or higher, and write it to handledFileName.
			String[] cmd = new String[] { "logcat", "-f", handledFileName,
					"*:W" };
			try {
				Runtime.getRuntime().exec(cmd);
			} catch (IOException e1) {
				Log.e(Constants.CODE_APP_TAG, "Error creating bug files", e1);
			}
			// 로그 스레드를 돌림
			Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
					unhandledFileName));
		}

		// 자세정보, 오디오 그래프 출력을 위한 정보들
		actigraphyEnabled = true;
		audioEnabled = true;
		ecgEnabled = true;

		// 현재 시간을 해당 데이터 형식으로 가져옴
		dateTimeString = DateFormat.format(Constants.PARAM_DATE_FORMAT,
				System.currentTimeMillis()).toString();
//
		// 센서 사용을 위해 서비스 가져와서 센서 가져옴
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		// appDIR경로
		String appDirPath = Environment.getExternalStorageDirectory()
				.toString() + "/" + getString(R.string.app_name);
		filesDirPath = appDirPath + "/" + dateTimeString + "/";
		Log.e("경로", filesDirPath + "");

		lastAccelerometerRecordedTime = 0;
		lastPositionChangeTime = System.currentTimeMillis();

		// PowerManager 디바이스의 전원상태를 제어함
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// PARTIAL_WAKE_LOCK 사용자가 단말을 슬립시켜도 계속 cpu가 돌아감
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				Constants.CODE_APP_TAG);

		// 위에 상태알람을 위한 노티피케이션
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// 자세 그래프 큐 초기화
		actigraphyQueue = new LinkedList<Double>();

		// TEXTVIOEW
		positionDisplay = (TextView) findViewById(R.id.position);
		apneaText = (TextView) findViewById(R.id.apnea);

		// 레코딩하는 빨간불 이미지
		recordingSign = (ImageView) findViewById(R.id.recordingSign);

		for (int i = 0; i < 5; ++i) {
			totalPositionTime[i] = 0;
		}

		// Battery check receiver.
		registerReceiver(this.batteryLevelReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));

		// Button to stop the recording.
		Button stopButton = (Button) findViewById(R.id.buttonStop);
		stopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View viewNext) {
				stopRecording();
			}
		});

		// 레코딩을 위해 파일 생성하고 여분의 기록을 삭제하는 모듈  
		File dir = new File(filesDirPath);
		if (!dir.exists()) {
			dir.mkdirs();
			File appDir = new File(appDirPath);
			Log.e("path", appDirPath);
			// Create a list of recordings in the app directory. These
			// are named by the date on which they were formed and so can be in
			// date order (earliest first).

			String[] recordingDirs = appDir.list();
			Arrays.sort(recordingDirs);

			// How many more recordings do we have in the app directory than are
			// specified in the settings? Should account for questionnaires
			// file,
			// which must exist for the user to have gotten to this stage
			// (checklist).
			// Log.e("ttt","dsadadsadas141");
			int numberRecordings = 0;
			for (String folderOrFileName : recordingDirs) {
				if (!folderOrFileName.equals(Constants.FILENAME_QUESTIONNAIRE)
						&& !folderOrFileName
								.equals(Constants.FILENAME_LOG_DIRECTORY)
						&& !folderOrFileName
								.equals(Constants.FILENAME_FEEDBACK_DIRECTORY)) {
					numberRecordings++;
				}
			}

			int extraFiles = numberRecordings
					- Integer.parseInt(sharedPreferences.getString(
							Constants.PREF_NUMBER_RECORDINGS,
							Constants.DEFAULT_NUMBER_RECORDINGS));

			if (extraFiles > 0) {
				// Too many recordings. Delete the earliest n, where n is the
				// number of extra files.
				boolean success;
				int nDeleted = 0;
				for (String candidateFolderName : recordingDirs) {
					if (nDeleted >= extraFiles) {
						// We've deleted enough already.
						break;
					}
					if (candidateFolderName
							.equals(Constants.FILENAME_QUESTIONNAIRE)
							|| candidateFolderName
									.equals(Constants.FILENAME_LOG_DIRECTORY)
							|| candidateFolderName
									.equals(Constants.FILENAME_FEEDBACK_DIRECTORY)) {
						// Don't delete questionnaire file or log/feedback
						// directory.
						continue;
					}
					// See if the path is a directory, and skip it if it isn't.
					File candidateFolder = new File(appDir, candidateFolderName);
					if (!candidateFolder.isDirectory()) {
						continue;
					}
					// If we've got to this stage, the file is the earliest
					// recording and should be deleted. Delete files in
					// recording first.
					success = Utils.deleteDirectory(candidateFolder);
					if (success) {
						nDeleted++;
					}
				}
			}
		}

		/*
		 * 컴퓨터\Youcanfly (SM-G906K)\Phone\SleepAp 경로에 Questionnaire.dat 파일이있음
		 * old 버전. 최신의 Questionnaire 데이터로 변경하기 위해 old 파일을 불러와서 최신의 것을 저장함.
		 */
		try {
			File latestQuestionnaireFile = new File(appDirPath,
					Constants.FILENAME_QUESTIONNAIRE);
			InputStream in = new FileInputStream(latestQuestionnaireFile);
			OutputStream out = new FileOutputStream(new File(filesDirPath,
					Constants.FILENAME_QUESTIONNAIRE));
			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			Log.e(Constants.CODE_APP_TAG,
					"FileNotFoundException copying Questionnaire file.");
		} catch (IOException e) {
			Log.e(Constants.CODE_APP_TAG,
					"IOException copying Questionnaire file.");
		}

		/*
		 * appDirPath경로 내부에 생성한 폴더에 넣을 파일들 생성함 자세 정보는 센서에 데이터가 들어올 때마다 저장함
		 */
		orientationFile = new File(filesDirPath, Constants.FILENAME_ORIENTATION);
		accelerationFile = new File(filesDirPath,
				Constants.FILENAME_ACCELERATION_RAW);
		actigraphyFile = new File(filesDirPath,
				Constants.FILENAME_ACCELERATION_PROCESSED);
		audioProcessedFile = new File(filesDirPath,
				Constants.FILENAME_AUDIO_PROCESSED);
		bodyPositionFile = new File(filesDirPath, Constants.FILENAME_POSITION);
		audioRawFile = new File(filesDirPath, Constants.FILENAME_AUDIO_RAW);
		apneaFile = new File(filesDirPath, Constants.FILENAME_APENA);

		/** Recording starts here. */
		// Log start time so recording can begin in 30 minutes.
		startTime = Calendar.getInstance(Locale.getDefault());
		finishRecordingFlag = false;

		// 설정메뉴 저장을 위해 preference에 저장함
		// 기록하기 전 시간 30분을 의미하는 recordingStartDelayMs
		Log.e("test", "액티비티로부터 데이터 가져오는 부분전");
		Intent intent = getIntent();
		if (intent == null) {
			Log.e("test", "인텐트가 널이다");
		}

		int tmpDelayTime = intent.getIntExtra("delayTime", 0);
		int tmpDuringTime = intent.getIntExtra("duringTime", 0);

		// 레코딩 딜레이 설정 밀리세컨드로 저장
		recordingStartDelayMs = tmpDelayTime * 1000;

		// 레코딩하는 시간을 설정함 밀리세컨드로 저장
		recordingDurationMs = tmpDuringTime * 60 * 1000;

		// 위에 대한 정보를 액티비티 다이얼로그박스로 띄워줌(딜레이시간)
		if (recordingStartDelayMs > 0) {
			startRecordingFlag = false;
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
			dialogBuilder
					.setTitle(getString(R.string.delayAlertTitle))
					.setMessage(
							getString(R.string.delayAlertMessage1)
									+ " "
									+ sharedPreferences
											.getString(
													Constants.PREF_RECORDING_START_DELAY,
													tmpDelayTime + "") + " "
									+ getString(R.string.delayAlertMessage2))
					.setPositiveButton(getString(R.string.ok), null);
			delayAlertDialog = dialogBuilder.create();
			delayAlertDialog.show(); // 보여줌
		} else {
			/*
			 * 레코딩 시작 시에 레코딩 빨간불 이미지 띄워주고 노티피케이션에 레코딩정보를 띄움
			 */
			startRecordingFlag = true;
			// Notify user
			Intent notificationIntent = new Intent(Activity_Monitoring.this,
					Activity_Monitoring.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);
			if (sharedPreferences.getBoolean(Constants.PREF_NOTIFICATIONS,
					Constants.DEFAULT_NOTIFICATIONS)) {
				NotificationCompat.Builder builder = new NotificationCompat.Builder(
						getApplicationContext())
						.setSmallIcon(R.drawable.notification_icon)
						.setLargeIcon(
								BitmapFactory.decodeResource(getResources(),
										R.drawable.deviceaccessmic))
						.setContentTitle("SleepAp")
						.setContentText(
								getString(R.string.startedRecordingNotification))
						.setAutoCancel(false).setOngoing(true)
						.setContentIntent(pendingIntent);
				notificationManager.notify(Constants.CODE_APP_NOTIFICATION_ID,
						builder.build());
				recordingSign.setVisibility(View.VISIBLE);
			}
		}

		/*
		 * 오디오, ecg 설정 및 스레드 시작. 이전 액티비티에서 가져온 가져온 오디오 플래그 값. 오디오는 따로 스레드 돌려서 녹음함, 지금 당장
		 * 녹음하지 않고 UserInterface스레드에서 녹음을 시작하는 플래그를 전달함 -> 이유는 수면상태에서의 호흡을
		 * 기록해야하므로 인터페이스덥댓 스레드에서 루프 돌리는데 시간을 체크함 수면중에만 측정
		 */

		if(ecgEnabled){
			configureBitalino();
			bitalinoThread.start();
		}
		
		if (audioEnabled) {			
			configureAudio();
			extAudioRecorder.start(); // 스레드 시작
		}

		/*
		 * Start actigraphy recording. 현재 스레드에서 레코딩함 (자세와, 가속도 정보)
		 */

		if (actigraphyEnabled) {
			// 센서 이벤트를 등록 현재 객체에 등록함. onSensorChanged메소드가 됨
			// 파라미터 : 첫번째: 어떤 객체에 등록할건지, 두번째: 어떤센서인지, 센서 반응 속도
			sensorManager
					.registerListener(
							this,
							accelerometer,
							1000000 / (Constants.PARAM_SAMPLERATE_ACCELEROMETER * Constants.PARAM_UPSAMPLERATE_ACCELEROMETER));
			sensorManager.registerListener(this, magnetometer,
					1000000 / Constants.PARAM_SAMPLERATE_ACCELEROMETER);
		}

		// 웨이크락을 얻음.
		wakeLock.acquire();

		// 그래프 쓰레드 돌림
		graphUpdateTask = new UserInterfaceUpdater();
		graphUpdateTask.execute();
	}
	
	private void configureAudio(){
		// 오디오 레코더 객체 획득.
		extAudioRecorder = new ExtAudioRecorder(this);

		// 저호흡증과 무호흡증 콜백메소드 등록.
		extAudioRecorder.setApnea(a_Callback);
		// 기본 경로에 생성한 audioRawFile 파일에 레코드함.
		extAudioRecorder.setOutputFile(audioRawFile);
		extAudioRecorder.setShouldWrite(startRecordingFlag); // 시작함을 의미하는
		extAudioRecorder.setAudioProcessedFile(audioProcessedFile);
		extAudioRecorder.prepare();
	}

	private void configureBitalino() {
		try {
			bitalinoThread = new BitalinoThread(BITlog.MAC, this);
			// Configure the devicesddd

			bitalinoThread.setChannels(BITlog.channels);

			//BITlog의 samplingrate값이 저장 됨 200
			bitalinoThread.setSampleRate(BITlog.SamplingRate);
			Log.e("bitaLINO_configureBitalino",  ""+ BITlog.SamplingRate);
		
			// 화면에 보여지는 데이터 갯수.
			BITlog.frameRate = 1; 
			if (BITlog.SamplingRate > 1) {			
				BITlog.frameRate =  3 * BITlog.SamplingRate / 10;
				Log.e("Activity_Monitoring", "frame rate"+ BITlog.frameRate);
			}

			//sampling rate가 200개이면 frame rate 60개가 저장 됨
			bitalinoThread.setNumFrames(BITlog.frameRate);
			bitalinoThread.setDownsamplingOn(false);
			// bitalinoThread.setMode(BITalinoDevice.LIVE_MODE);

		} catch (Exception e) {
			Log.v(ACTIVITY_MONITORING, "Error creating the Bitalino Thread");
			e.printStackTrace();
		}
	}
	
	// 액티비티 시작
	@Override
	protected void onStart() {
		screenLocked = false;
		super.onStart();
	}

	// 액티비티 비활성
	@Override
	protected void onStop() {
		screenLocked = true;		
		super.onStop();
	}

	// 리셋될 때 ui 스레드 멈춤
	public void resetUi() {
		// 스레드 멈춤
		graphUpdateTask.stopUiUpdates();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			Log.e(Constants.CODE_APP_TAG, "Error while thread sleeping", e);
		}
		System.gc(); // 가비지 컬렉터를 실행하는 좋은시간되럯이라고 vm에 알려줌
		graphUpdateTask = new UserInterfaceUpdater();
		graphUpdateTask.execute(); // 인터페이스 스레드 다시 시작
	}

	protected void stopRecording() {

		// Cancel dialogs.
		if (delayAlertDialog != null) {
			delayAlertDialog.cancel();
		}
		try {
			unregisterReceiver(batteryLevelReceiver);
		} catch (Exception e) {
			Log.e(Constants.CODE_APP_TAG,
					"Error unregistering bluetooth disconnect BroadcastReceiver",
					e);
		}
		// Stop graphs update.
		if (graphUpdateTask.isRunning()) {
			graphUpdateTask.stopUiUpdates();
		}

		// Audio
		if (audioEnabled) {
			extAudioRecorder.stop();
			extAudioRecorder.release();
		}

		// Actigraphy
		if (actigraphyEnabled) {
			// 리스너 객체 등록해제
			sensorManager.unregisterListener(this);
		}
		
		if (ecgEnabled){
			bitalinoThread.finalizeThread();
			bitalinoThread.close();
		}

		try {
			wakeLock.release();
		} catch (Throwable t) {
			Log.e(Constants.CODE_APP_TAG, "Wakelock has already been released",
					t);
		}

		// 정지를 하게되면 파이링 분석이 안됨. 데이터 확인해야함
		// Check if we have both stopped and started recording properly. If we
		// have, everything has worked properly. If we haven't, user probably
		// interrupted recordings and we should delete the files they made.
		// 설정에서 가져오는 데이터들 확ㄷ인해야함
		// 기본 저장되는 키랑 불값을 던져서 값이 있으며 ㄴtrue 아니면 예외
		/*
		 * sharedpreference가 데이터를 저장하고
		 */
		boolean shouldDelete = sharedPreferences.getBoolean(
				Constants.PREF_EARLY_EXIT_DELETION,
				Constants.DEFAULT_EARLY_EXIT_DELETION);
		boolean recordingSuccessful = startRecordingFlag && finishRecordingFlag; // 레코딩
																					// 정지도
																					// 안되고
																					// 시간
																					// 다
																					// 채웠을
																					// 경우.
		if (shouldDelete && !recordingSuccessful) {
			// User interrupted recording. Recordings are useless - get rid of
			// them.
			if (filesDirPath != null) {
				File filesDir = new File(filesDirPath);
				Utils.deleteDirectory(filesDir);
			}
		} else {
			// Otherwise save the position data - woudn't need to do this if
			// files were to be deleted.
			// 바디 자세 저장

			saveBodyPositionData();
		}

		// 노티피케이션 제거
		if (sharedPreferences.getBoolean(Constants.PREF_NOTIFICATIONS,
				Constants.DEFAULT_NOTIFICATIONS)) {
			notificationManager.cancel(Constants.CODE_APP_NOTIFICATION_ID);
		}

		// 레코딩 이미지 제거
		recordingSign.setVisibility(View.GONE);

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(getString(R.string.finishedAlertTitle));
		if (recordingSuccessful) {
			dialogBuilder.setMessage(getString(R.string.finishedAlertSuccess));
		} else {
			dialogBuilder.setMessage(getString(R.string.finishedAlertFailure));
		}

		dialogBuilder.setPositiveButton(getString(R.string.mainMenuButtonText),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Activity_Monitoring.this,
								MainActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
				});

		dialogBuilder.setCancelable(false);
		dialogBuilder.create().show();

		// Change logcat back if necessary.
		if (sharedPreferences.getBoolean(Constants.PREF_WRITE_LOG,
				Constants.DEFAULT_WRITE_LOG)) {
			String[] cmd = new String[] { "logcat", "-f", "stdout", "*:V" };
			try {
				Runtime.getRuntime().exec(cmd);
			} catch (IOException e) {
				Log.e(Constants.CODE_APP_TAG,
						"Error changing logcat back to default", e);
			}
		}
	}

	/*
	 * 센서에 데이터가 들어오면 호출 됨
	 * 
	 * 자기센서(마그네틱) : 자세정보를 저장함 계산해서 데이터를 저장핮는 메소드 호출 가속도센서: 가속도 값을 저장함 (가속도를
	 * actigraphyQueue에 넣고 그래프로 출력함) 값이 들어올 때마다 데이터를 저장함.
	 */

	@Override
	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			// Checking which type of sensor called this listener
			// In this case it is the Accelerometer (the next is the Magnetomer)
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				accelerometerCurrentTime = System.currentTimeMillis(); // 현재 시간
																		// 가져옴

				if (accelerometerCurrentTime - lastAccelerometerReadTime > (1000 / Constants.PARAM_SAMPLERATE_ACCELEROMETER - 1000 / (Constants.PARAM_SAMPLERATE_ACCELEROMETER * Constants.PARAM_UPSAMPLERATE_ACCELEROMETER))) {
					lastAccelerometerReadTime = accelerometerCurrentTime;
					float xRaw = event.values[0];
					float yRaw = event.values[1];
					float zRaw = event.values[2];

					// Extracts unwanted gravity component from the
					// accelerometer signal.
					float alpha = Constants.PARAM_GRAVITY_FILTER_COEFFICIENT;
					runningGravityComponents[0] = runningGravityComponents[0]
							* alpha + (1 - alpha) * xRaw;
					runningGravityComponents[1] = runningGravityComponents[1]
							* alpha + (1 - alpha) * yRaw;
					runningGravityComponents[2] = runningGravityComponents[2]
							* alpha + (1 - alpha) * zRaw;

					float xAccel = xRaw - runningGravityComponents[0];
					float yAccel = yRaw - runningGravityComponents[1];
					float zAccel = zRaw - runningGravityComponents[2];

					double magnitudeSquare = xAccel * xAccel + yAccel * yAccel
							+ zAccel * zAccel;
					double magnitude = Math.sqrt(magnitudeSquare);

					actigraphyQueue.add(magnitude); // 가속도 
					
					int secsToDisplay = Integer.parseInt(sharedPreferences
							.getString(Constants.PREF_GRAPH_SECONDS,
									Constants.DEFAULT_GRAPH_RANGE));
					int numberExtraSamples = actigraphyQueue.size()
							- (secsToDisplay * Constants.PARAM_SAMPLERATE_ACCELEROMETER);
					if (numberExtraSamples > 0) {
						for (int i = 0; i < numberExtraSamples; i++) {
							actigraphyQueue.remove();
						}
					}

					// Saves accelerometer data, necessary for the orientation
					// computation
					System.arraycopy(event.values, 0,
							latestAccelerometerEventValues, 0, 3);
					pushBackAccelerometerValues(xRaw, yRaw, zRaw);

					if (accelerometerCurrentTime
							- lastAccelerometerRecordedTime > Constants.PARAM_ACCELEROMETER_RECORDING_PERIOD
							+ 1000 / Constants.PARAM_SAMPLERATE_ACCELEROMETER) {
						if (startRecordingFlag) {
							// Saves accelerometer data
							writeActigraphyLogVariance();
						}
						gravitySum = gravitySquaredSum = 0;
						varianceCounter = 0;
						lastAccelerometerRecordedTime = accelerometerCurrentTime;
					}
					if (startRecordingFlag) {
						// 가속도 센서 데이터 저장
						writeRawActigraphy();
					}
				}
			}

			// 마그네틱 센서로 자세를 정보를 계산해서 저장
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

				// Copying magnetometer measures.
				System.arraycopy(event.values, 0, mGeoMags, 0, 3);

				if (SensorManager.getRotationMatrix(mRotationM, null,
						latestAccelerometerEventValues, mGeoMags)) {
					SensorManager.getOrientation(mRotationM, mOrientation);

					// Finding current orientation requires both Accelerometer
					// (using the previous measure) and Magnetometer data.
					// Converting radians to degrees (yaw, pitch, roll)
					mOrientation[0] = mOrientation[0]
							* Constants.CONST_DEGREES_PER_RADIAN;
					mOrientation[1] = mOrientation[1]
							* Constants.CONST_DEGREES_PER_RADIAN;
					mOrientation[2] = mOrientation[2]
							* Constants.CONST_DEGREES_PER_RADIAN;

					// The values (1,2,3,4) attributed for
					// supine/prone/left/right match the
					// ones attributed in VISI text files.
					int positionValue = 0;
					// Supine (4).
					if (-45 < mOrientation[1] && mOrientation[1] < 45
							&& -45 < mOrientation[2] && mOrientation[2] < 45) {
						positionValue = Constants.CODE_POSITION_SUPINE;
						position = Position.Supine;
					}

					// Prone (1).
					if ((((-180 < mOrientation[2] && mOrientation[2] < -135) || (135 < mOrientation[2] && mOrientation[2] < 180))
							&& -45 < mOrientation[1] && mOrientation[1] < 45)) {
						positionValue = Constants.CODE_POSITION_PRONE;
						position = Position.Prone;
					}

					// Right (2).
					if (-90 < mOrientation[2] && mOrientation[2] < -45) {
						positionValue = Constants.CODE_POSITION_RIGHT;
						position = Position.Right;
					}

					// Left (3).
					if (45 < mOrientation[2] && mOrientation[2] < 90) {
						positionValue = Constants.CODE_POSITION_LEFT;
						position = Position.Left;
					}

					// Sitting up (5).
					if ((((-135 < mOrientation[1] && mOrientation[1] < -45) || (45 < mOrientation[1] && mOrientation[1] < 135))
							&& -45 < mOrientation[2] && mOrientation[2] < 45)) {
						positionValue = Constants.CODE_POSITION_SITTING;
						position = Position.Sitting;
					}

					if ((oldPositionValue != positionValue)
							&& (positionValue != 0) && startRecordingFlag) {
						updatePositionChangeTime(oldPositionValue);
						oldPositionValue = positionValue;
						try {
							// Write raw body position data
							BufferedWriter orientationBufferedWriter = new BufferedWriter(
									new FileWriter(orientationFile, true));
							orientationBufferedWriter.append(String
									.valueOf(System.currentTimeMillis()) + ",");
							orientationBufferedWriter.append(String
									.valueOf(positionValue) + "\n");
							orientationBufferedWriter.flush();
							orientationBufferedWriter.close();
						} catch (IOException e) {
							Log.e(Constants.CODE_APP_TAG,
									"Error writing orientation data to file", e);
						}
					}
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void pushBackAccelerometerValues(float xAccel, float yAccel,
			float zAccel) {
		for (int i = 0; i < 3; ++i) {
			previousXAccels[i] = previousXAccels[i + 1];
			previousYAccels[i] = previousYAccels[i + 1];
			previousZAccels[i] = previousZAccels[i + 1];
		}

		previousXAccels[3] = xAccel;
		previousYAccels[3] = yAccel;
		previousZAccels[3] = zAccel;

		float meanXAccel = (xAccel + previousXAccels[2] - previousXAccels[1] - previousXAccels[0]) / 2;
		float meanYAccel = (yAccel + previousYAccels[2] - previousYAccels[1] - previousYAccels[0]) / 2;
		float meanZAccel = (zAccel + previousZAccels[2] - previousZAccels[1] - previousZAccels[0]) / 2;

		double magnitudeSquare = meanXAccel * meanXAccel + meanYAccel
				* meanYAccel + meanZAccel * meanZAccel;
		double magnitude = Math.sqrt(magnitudeSquare);

		gravitySum += magnitude;
		gravitySquaredSum += magnitudeSquare;
		varianceCounter += 1;
	}

	/*
	 * 가속도 센서로부터 받아온 정보를 저장함 accelerationFile : 시간, 값, 값, 개행
	 */
	void writeRawActigraphy() {
		// Writes Accelerometer data
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(
					accelerationFile, true));
			out.append(String.valueOf(accelerometerCurrentTime) + ",");
			out.append(String.valueOf(latestAccelerometerEventValues[0]) + ",");
			out.append(String.valueOf(latestAccelerometerEventValues[1]) + ",");
			out.append(String.valueOf(latestAccelerometerEventValues[2]) + "\n");
			out.flush();
			out.close();
		} catch (IOException e) {
			Log.e(Constants.CODE_APP_TAG,
					"Error writing raw actigraphy data to file", e);
		}
	}

	/*
	 * 자세 수치를(게산된 값) 저장함
	 */
	void writeActigraphyLogVariance() {
		double mean = gravitySum / varianceCounter;
		double logVariance = Math.log(1 + gravitySquaredSum / varianceCounter
				- mean * mean);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(
					actigraphyFile, true));
			// out.append(String.valueOf(accelerometerCurrentTime) + ",");
			// out.flush();
			out.write(String.valueOf(logVariance) + ",");
			out.write(String.valueOf(varianceCounter) + "\n");
			out.flush();
			out.close();
		} catch (IOException e) {
			Log.e(Constants.CODE_APP_TAG,
					"Error writing processed actigraphy data to file", e);
		}
	}

	// 자세 정보를 업데이트 해줌
	void updatePositionChangeTime(int positionValue) {
		// Updates the time spent on a body position
		long currentTime = System.currentTimeMillis();
		totalPositionTime[positionValue - 1] += currentTime
				- lastPositionChangeTime;
		lastPositionChangeTime = currentTime;
	}

	// 자세 정보를 저장함
	void saveBodyPositionData() {
		// Computing full time spent during the session (supine, prone, right,
		// left, sitting)
		int totalTime = (int) (totalPositionTime[0] + totalPositionTime[1]
				+ totalPositionTime[2] + totalPositionTime[3] + totalPositionTime[4]);
		totalTime = Math.max(totalTime, 1);

		// Computing % of time spent in each position
		float supineProportion = ((float) totalPositionTime[Constants.CODE_POSITION_SUPINE - 1] * 100)
				/ totalTime;
		float proneProportion = ((float) totalPositionTime[Constants.CODE_POSITION_PRONE - 1] * 100)
				/ totalTime;
		float rightProportion = ((float) totalPositionTime[Constants.CODE_POSITION_RIGHT - 1] * 100)
				/ totalTime;
		float leftProportion = ((float) totalPositionTime[Constants.CODE_POSITION_LEFT - 1] * 100)
				/ totalTime;
		float sittingProportion = ((float) totalPositionTime[Constants.CODE_POSITION_SITTING - 1] * 100)
				/ totalTime;

		Log.e("ActivityMonitoring", "position : " + supineProportion + ","
				+ proneProportion + "," + rightProportion + ","
				+ leftProportion + "," + sittingProportion);
		try {
			// Writing data into file
			// 각각의 자세를 %로 저장함
			BufferedWriter out = new BufferedWriter(new FileWriter(
					bodyPositionFile, true));
			out.write(String.valueOf(supineProportion) + ','
					+ String.valueOf(proneProportion) + ','
					+ String.valueOf(leftProportion) + ','
					+ String.valueOf(rightProportion) + ','
					+ String.valueOf(sittingProportion) + " \n");
			out.flush();
			out.close();
		} catch (IOException e) {
			Log.e(Constants.CODE_APP_TAG,
					"Error writing position data to file", e);
		}
	}

	private List<Number> integerQueueToNumberList(Queue<Integer> queue) {
		List<Number> list;
		// If the Queue is modified while the loop is running (which is more
		// than possible), a ConcurrentModificationException will be thrown.
		// If one is, it is caught and we try again.
		int attempts = 0;
		while (attempts < 5) {
			try {
				list = new ArrayList<Number>();
				for (Iterator<Integer> iter = queue.iterator(); iter.hasNext();) {
					Integer obj = iter.next();
					list.add(obj);
				}
				return list;
			} catch (ConcurrentModificationException e) {
				// Don't return anything so we go through the while loop again.
				attempts++;
			}
		}
		list = new ArrayList<Number>();
		list.add(0);
		list.add(0);
		return list;
	}
	
	private List<Number> doubleQueueToNumberList(Queue<Double> queue) {
		List<Number> list;
		// If the Queue is modified while the loop is running (which is more
		// than possible), a ConcurrentModificationException will be thrown.
		// If one is, it is caught and we try again.
		int attempts = 0;
		while (attempts < 5) {
			try {
				list = new ArrayList<Number>();
				for (Iterator<Double> iter = queue.iterator(); iter.hasNext();) {
					Double obj = iter.next();
					list.add(obj);
				}
				return list;
			} catch (ConcurrentModificationException e) {
				// Don't return anything so we go through the while loop again.
				attempts++;
			}
		}
		list = new ArrayList<Number>();
		list.add(0);
		list.add(0);
		return list;
	}
}