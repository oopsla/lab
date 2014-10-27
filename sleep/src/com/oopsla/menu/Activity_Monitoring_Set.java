package com.oopsla.menu;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.example.sleepap.R;
import com.oopsla.device.BITlog;
import com.oopsla.monitoring.Activity_Monitoring;

public class Activity_Monitoring_Set extends Activity {
	private EditText delayTime;
	private EditText duringTime;
	private Button ok_btn;
	private Spinner spinnerBluetooth;

	// 블루투스
	private BluetoothAdapter mBluetoothAdapter;
	private final String ACTIVITY_MONITORING_BLUETOOTH = "ACTIVITY_MONITORING_BLUETOOTH";
	private final int REQUEST_ENABLE_BT = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitoring_set);

		delayTime = (EditText) findViewById(R.id.activity_monitoring_set_timeDelay);
		duringTime = (EditText) findViewById(R.id.activity_monitoring_set_timeDuring);
		ok_btn = (Button) findViewById(R.id.activity_monitoring_set_ok);
		spinnerBluetooth = (Spinner) findViewById(R.id.spinnerBluetooth);

		// 블루투스
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		}
		else
			onActivityResult(REQUEST_ENABLE_BT, Activity.RESULT_OK, null);
		
		ok_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Activity_Monitoring_Set.this,
						Activity_Monitoring.class);

				int delayInt, duringInt;

				try {
					delayInt = Integer.parseInt(delayTime.getText().toString());
					duringInt = Integer.parseInt(duringTime.getText()
							.toString());

					if (delayInt < 0 || duringInt <= 0) {
						Toast.makeText(getApplicationContext(),
								"잘못된 값을 입력하셨습니다. 다시 입력하세요", Toast.LENGTH_SHORT)
								.show();
					} else {
						intent.putExtra("delayTime", delayInt);
						intent.putExtra("duringTiem", duringInt);
						startActivity(intent);
					}
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "값들을 입력하세요",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private String[] getBluetoothDevices() {
		String[] result = null;
		ArrayList<String> devices = new ArrayList<String>();
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

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				Log.e("MainActivity", "블루투스 사용가능");
				String[] deviceList = this.getBluetoothDevices();
				ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
						this, android.R.layout.simple_spinner_item, deviceList);
				spinnerArrayAdapter
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The
																									// drop
																									// down
																									// view
				spinnerBluetooth.setAdapter(spinnerArrayAdapter);
				spinnerBluetooth.setSelection(getIndex(spinnerBluetooth,
						"bitalino"));

				spinnerBluetooth
						.setOnItemSelectedListener(new OnItemSelectedListener() {
							@Override
							public void onItemSelected(AdapterView<?> parent,
									View view, int position, long id) {

								String myMAC = spinnerBluetooth
										.getSelectedItem().toString();
								myMAC = myMAC.substring(myMAC.length() - 17);
								BITlog.MAC = myMAC; //선택한 맥주소를 BITlog에 저장함
							}

							@Override
							public void onNothingSelected(AdapterView<?> parent) {
							}

						});
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, "블루투스 사용불가능", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
}
