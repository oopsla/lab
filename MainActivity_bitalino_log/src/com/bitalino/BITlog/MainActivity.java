package com.bitalino.BITlog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.bitalino.BITlog.device.BitalinoThread;
import com.bitalino.comm.BITalinoDevice;
import com.bitalino.comm.BITalinoFrame;

/**
 * 
 * @author bgamecho
 * 
 */
public class MainActivity extends Activity {

	static final String TAG = "Main Activity";

	public BitalinoThread bitalinoThread; // java���� �ʱ�ȭ���� ������ �η� ����ǳ�

	public boolean sd_write = true;

	public String filename = null;
	public OutputStreamWriter fout = null;

	public boolean testInitiated;

	public StoreLooperThread looperThread;

	public TextView tvFrames;
	public TextView tvFile;
	public Button buttonStart;
	public Button buttonStop;
	public Button buttonLaunchStoreFiles;
	public Button buttonBluetoothConf;
	public Chronometer chronometer; //Ÿ�̸�

	public Spinner spinnerBluetooth; 
	public CheckBox checkboxSD; //ecg sdī�忡 �����ϰڳĴ� ���� ����� üũ�ڽ� 

	public boolean checkValueSD = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		BITlog.setContext(this);

		sd_write = this.checkSD();

		checkboxSD = (CheckBox) findViewById(R.id.checkBoxSD);

		
		//������ �����ϸ� true �ƴϸ� false
		//������ ������ �ǵ���.
		if (checkSD())
			checkboxSD.setChecked(true);
		else
			checkboxSD.setChecked(false);
		
		//üũ�ڽ� ������ ���
		checkboxSD
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {

						if (!checkSD())
							checkboxSD.setChecked(false);

						if (isChecked) {
							checkValueSD = true;
							disableButtonStore(true);

						} else {
							checkValueSD = false;
							disableButtonStore(false);
						}
					}
				});

		//���������� ����� ���� �ܺη� ������ �κ�
		buttonLaunchStoreFiles = (Button) findViewById(R.id.buttonLaunchStoreFiles);
		buttonLaunchStoreFiles.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				launchStore();
			}
		});

		//start ��ư
		buttonStart = (Button) findViewById(R.id.buttonStart);
		buttonStart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (bitalinoThread == null) {
					configureBitalino();
				}
				looperThread.resetPackNum(); //looperThread�� �����ϴ� �κ��� Ȯ���� ����, packNum�� 0���� �ʱ�ȭ ��, packNum�� ������ ��
				createFile(); //������ �ؽ�Ʈ ���� ����.
				bitalinoThread.start();
			}
		});

		//���ߴ� ��ư
		buttonStop = (Button) findViewById(R.id.buttonStop);
		buttonStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (bitalinoThread != null) {
					bitalinoThread.finalizeThread();
					chronometer.stop();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					closeFile();
					bitalinoThread = null;
				}

			}
		});

		buttonBluetoothConf = (Button) findViewById(R.id.buttonBitalinoConf);
		buttonBluetoothConf.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				launchConfigure();
			}
		});

		spinnerBluetooth = (Spinner) findViewById(R.id.spinnerBluetooth);
		String[] myDeviceList = this.getBluetoothDevices();

		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, myDeviceList);
		spinnerArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The
																							// drop
																							// down
																							// view
		spinnerBluetooth.setAdapter(spinnerArrayAdapter);
		spinnerBluetooth.setSelection(getIndex(spinnerBluetooth, "bitalino"));

		spinnerBluetooth
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {

						String myMAC = spinnerBluetooth.getSelectedItem()
								.toString();
						myMAC = myMAC.substring(myMAC.length() - 17);
						BITlog.MAC = myMAC;

					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}

				});

		chronometer = (Chronometer) findViewById(R.id.chronometer1);

		tvFrames = (TextView) findViewById(R.id.textViewFrames);

		tvFile = (TextView) findViewById(R.id.TextViewFileName);

		looperThread = new StoreLooperThread();
		looperThread.start();
		Log.v(TAG, "MobileBit Activity --OnCreate()--");
	}

	@Override
	public void onResume() {
		super.onResume();
		// Log.v(TAG, "MobileBit Activity --OnResume()--");

		if (checkboxSD.isChecked()) {
			disableButtonStore(true);
		} else {
			disableButtonStore(false);
		}
	}

	private int getIndex(Spinner spinner, String myString) {

		int index = 0;

		for (int i = 0; i < spinner.getCount(); i++) {
			String aux = (String) spinner.getItemAtPosition(i);
			if (aux.contains(myString)) {
				index = i;
				continue;
			}
		}
		return index;
	}

	private static final int REQUEST_ENABLE_BT = 12;

	private String[] getBluetoothDevices() {
		String[] result = null;
		ArrayList<String> devices = new ArrayList<String>();
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (!mBluetoothAdapter.isEnabled()) {
			Log.e("Dialog", "Couldn't find enabled the mBluetoothAdapter");
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			Set<BluetoothDevice> devList = mBluetoothAdapter.getBondedDevices();

			for (BluetoothDevice device : devList)
				devices.add(device.getName() + "-" + device.getAddress());

			String[] aux_items = new String[devices.size()];
			final String[] items = devices.toArray(aux_items);
			result = items;
		}
		return result;

	}

	/** ������� ����� �ϱ� ���ؼ� ������ �ʱ�ȭ.(���ø� �Ǵ� �������� ���� ��)
	 * frameNumber Sampling rate 1 --> 1 Sampling rate 10 --> 3 Sampling rate
	 * 100 --> 30 Sampling rate 1000 --> 300
	 */
	private void configureBitalino() {
		try {

			bitalinoThread = new BitalinoThread(looperThread.mHandler,
					BITlog.MAC);
			// Configure the devices

			bitalinoThread.setChannels(BITlog.channels);

			//���⼭ ���ϴ� ���ø��� ������ �ǹ��ϴ��� �� �𸣰ڴ�.
			bitalinoThread.setSampleRate(BITlog.SamplingRate);
			Log.e("bitaLINO_configureBitalino",  ""+ BITlog.SamplingRate);
		
			// Calculate frame number: ?? �����ӳѹ��� ���??
			BITlog.frameRate = 1;
			if (BITlog.SamplingRate > 1) {
				BITlog.frameRate = 3 * BITlog.SamplingRate / 10;
			}

			bitalinoThread.setNumFrames(BITlog.frameRate);
			bitalinoThread.setDownsamplingOn(false);
			// bitalinoThread.setMode(BITalinoDevice.LIVE_MODE);

		} catch (Exception e) {
			Log.v(TAG, "Error creating the Bitalino Thread");
			e.printStackTrace();
		}
	}

	//������ �� �ִ� sdī�尡 �����ϴ����� ���ٰ������� Ȯ�� �� ��ȯ ���ɿ��θ� ��ȯ
	private boolean checkSD() {

		boolean sdAvailable = false;
		boolean sdWriteAccess = false;

		// Check external memory status
		String sdStatus = Environment.getExternalStorageState();

		if (sdStatus.equals(Environment.MEDIA_MOUNTED)) {
			sdAvailable = true;
			sdWriteAccess = true;
			sd_write = true;
		} else if (sdStatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			sdAvailable = true;
			sdWriteAccess = false;
		} else {
			sdAvailable = false;
			sdWriteAccess = false;
		}

		Log.v(TAG, (sdAvailable ? "Media mounted" : "Media unmounted"));
		Log.v(TAG, "SD Write " + (sdWriteAccess ? "true" : "false"));

		return sdAvailable & sdWriteAccess;
	}

	private void createFile() {
		Log.v(TAG, "UI BUtton pressed: Store Block - createFile()");
		if (fout == null) {

			Date cDate = new Date();
			String fDate = new SimpleDateFormat("yyyyMMddHHmmss").format(cDate);
			filename = fDate + ".txt";

			ArrayList<Integer> channelList = new ArrayList<Integer>();
			for (int i = 0; i < 6; i++) {
				boolean find = false;
				for (int j = 0; j < BITlog.channels.length; j++) {
					if (i == BITlog.channels[j]) {
						find = true;
						channelList.add(i, Integer.valueOf(i));
					}
				}
				if (!find)
					channelList.add(i, null);
			}

			String line = "#{"
					+ "\"date\": \""
					+ new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
							.format(cDate) + "\", " + "\"MAC\": \""
					+ BITlog.MAC + "\", " + "\"ChannelsOrder\": [\"SeqN\"";

			if (BITlog.digitalOutputs) {
				line += ", " + "\"Digital0\", " + "\"Digital1\", "
						+ "\"Digital2\", " + "\"Digital3\"";
			}

			for (int i = 0; i < 6; i++) {
				if (channelList.get(i) != null) {
					line += ", " + "\t" + "\"Analog" + i + "\"";
				}
			}

			line += " ]}\n";

			// Select between external or internal memory
			if (checkValueSD & sd_write) {
				try {
					File sdPath = Environment.getExternalStorageDirectory(); // getExternalStorageDirectory();
					String directory = "/BITlog";
					File dir = new File(sdPath.getAbsolutePath() + directory);
					dir.mkdirs();

					File f = new File(dir, filename);
					fout = new OutputStreamWriter(new FileOutputStream(f));

					Log.v(TAG, "Log file: " + filename
							+ " created in the SD card");
					fout.write(line);

				} catch (Exception ex) {
					Log.e("Ficheros", "Error writing the file in SD card");
				}
			} else {
				try {
					fout = new OutputStreamWriter(openFileOutput(filename,
							MODE_WORLD_WRITEABLE));
					Log.v(TAG, "Log file: " + filename
							+ " created in the internal memory");
					fout.write(line);
					// Log.v(TAG, "Write: \n\t"+line);
				} catch (Exception e) {
					Log.e(TAG, "Error at writing in internal memory");
					e.printStackTrace();

				}
			}

			tvFile.setText(filename);

		}

	}

	private void closeFile() {
		Log.v(TAG, "UI BUtton pressed: Store Block - closeFile()");

		if (fout != null) {
			try {
				fout.flush();
				fout.close();
				fout = null;
				Log.v(TAG, "File closed " + filename);
			} catch (IOException e) {
				Log.e(TAG, "Error at writing in internal memory");
				e.printStackTrace();
			}

		}

	}

	private void disableButtonStore(boolean disable) {
		if (disable) {
			buttonLaunchStoreFiles.setEnabled(false);
			buttonLaunchStoreFiles.setClickable(false);
		} else {
			// buttonLaunchStoreFiles.setEnabled(true);
			// buttonLaunchStoreFiles.setClickable(true);
		}

	}

	private void launchStore() {
		Intent intent = new Intent(this, LogStoreActivity.class);
		startActivity(intent);
	}

	private void launchConfigure() {
		Intent intent = new Intent(this, BitalinoConfigActivity.class);
		startActivity(intent);
	}

	public final int MSG_CHRONO = 1;
	public final int MSG_BITALINO = 2;
	
	//
	public Handler myHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message inputMessage) {

			switch (inputMessage.what) {
			// The decoding is done
			case MSG_CHRONO:
				chronometer.setBase(SystemClock.elapsedRealtime());
				chronometer.start();
				break;
			case MSG_BITALINO:

				int pkg = inputMessage.arg1;
				Log.v(TAG, "Number of packages received: " + pkg);

				tvFrames.setText(Integer.valueOf(pkg).toString());

				break;
			default:
				super.handleMessage(inputMessage);

			}// Switch
		}// handle
	};

	/**
     * �ش� �����忡�� �޽����� �޽��� ť�� ������(sendMessage()) looper�� ���� �ڵ鷯�� ���� ��. �׸��� ���� �ڵ鷯���� �ڵ鷯 �޽����� ó��.
     * �����ϴ� �κ�
     */
	public class StoreLooperThread extends Thread {
		public Handler mHandler;
		private int packNum = 0;

		public void resetPackNum() {
			packNum = 0;
		}

		public void run() {

			//���� �������� ���� �ʱ�ȭ
			Looper.prepare();

			//BitalinoThread���� ������ �޽����� ó����
			mHandler = new Handler() {

				public void handleMessage(Message msg_in) {
					// process incoming messages here

					Bundle myBundle = msg_in.getData(); //���޵� �޽���

					//bitalino�� �������� ������ �ش� ���ڿ����� ���ο� ���ԵǾ��ִ��� Ȯ��
					if (myBundle.containsKey("bitalino_data")) {
						Message msg_out = new Message();

						if (packNum == 0) {
							myHandler.sendMessage(myHandler
									.obtainMessage(MSG_CHRONO));
						}

						//?
						BITalinoFrame[] frames = new BITalinoFrame[BITlog.frameRate];

						//���޵� ������
						frames = (BITalinoFrame[]) myBundle
								.getSerializable("bitalino_data");

						//ä�� ����Ʈ �߰�
						ArrayList<Integer> channelList = new ArrayList<Integer>();
						for (int i = 0; i < 6; i++) {
							boolean find = false;
							for (int j = 0; j < BITlog.channels.length; j++) {
								if (i == BITlog.channels[j]) {
									find = true;
									channelList.add(i, Integer.valueOf(i));
								}
							}
							if (!find)
								channelList.add(i, null);
						}

						
						for (BITalinoFrame myBitFrame : frames) {

							packNum++;

							if (fout != null) {
								String line = Integer.valueOf(
										myBitFrame.getSequence()).toString();
								if (BITlog.digitalOutputs) {
									line += "\t" + myBitFrame.getDigital(0)
											+ "\t" + myBitFrame.getDigital(1)
											+ "\t" + myBitFrame.getDigital(2)
											+ "\t" + myBitFrame.getDigital(3);

								}
								for (int i = 0; i < 6; i++) {
									if (channelList.get(i) != null) {
										line += "\t" + myBitFrame.getAnalog(i);
									}

								}
								line += "\n";
								;
								try {
									fout.write(line);

									// Log.v(TAG, "Write: \n\t"+line);
								} catch (IOException e) {
									Log.v(TAG, "Error writing to the file "
											+ line);
									// e.printStackTrace();
								}
							}

						}

						msg_out.what = MSG_BITALINO;
						msg_out.arg1 = packNum; // ��Ŷ �ѹ�����: �������̶� ��Ŷ�̶� ���� ���� �� ����. �ƴ� ���ɼ��� �� ����
						myHandler.sendMessage(msg_out); //��ü������ ó��

						//biatlinoThread���� ���� OKó�� -> bluetooth ����ƴٴ� �޽���
					} else if (myBundle.containsKey("OK")) {
						String str = myBundle.getString("OK");
						Log.v(TAG, str);
					} else if (myBundle.containsKey("OFF")) {
						String str = myBundle.getString("OFF");
						Log.v(TAG, str);
					} else {
						Log.i(TAG, " message :  " + myBundle.keySet() + " + "
								+ myBundle.get("what"));
					}

				}

			};

			Looper.loop();
		}
	}

}
