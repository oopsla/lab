package com.example.header;

import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;

import com.example.data_process.MainActivity;

import android.R;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class CManagerDeviceReceiver {

	// �۹̼��� ���� ������
	private final Context mApplicationContext;
	private final UsbManager mUsbManager;
	protected static final String ACTION_USB_PERMISSION = "ch.serverbox.android.USB";

	private final int ID_SIZE = 5;
	private final int RECEIVE_MODE = 1;
	private final int COMMAND_SIZE = 3;
	private final int PACKET_SIZE = 18;
	private final char ID_ADDRESS = 0x00; //�۽ű� ���̵�
	private final char ID_ADDRESS1 = 0x01;
	private final char ID_ADDRESS2 = 0x02;
	private final char ID_ADDRESS3 = 0x03;
	
	private final int VID = 0x10c4;
	private final int PID = 0xea61;
	// private final int ID_ADDRESS_1 = 1158001;
	private UsbDevice device;
	private char[] ucWriteBuffer;

	private HCDATA m_sHC; // ���� ������ ����

	// ��Ŷ ������ ���� �ʿ��� ������
	private UsbDeviceConnection conn;
	private UsbEndpoint epIN;
	private UsbEndpoint epOUT;
	
	private SendMassgeHandler mMainHandler = null;

	protected CUCareReceiveBufferList m_cReceiveBufferList;

	// ���κ��� ����̽� ������ ������ �� �ֵ��� �޼ҵ� ����.

	// �溸 ������

	// ������ �� �ֽ�����

	// ������ �Ǵ� ������

	/*
	 * ����� usb ��ġ �ʱ�ȭ.
	 */

	public CManagerDeviceReceiver(Activity parentActivity) {
		mApplicationContext = parentActivity.getApplicationContext();
		mUsbManager = (UsbManager) mApplicationContext
				.getSystemService(Context.USB_SERVICE);
		ucWriteBuffer = new char[80];
		mMainHandler = new SendMassgeHandler();
		
		this.epIN = null;
		this.epOUT = null;

		init();
	}

	private void init() {
		enumerate(new IPermissionListener() {
			@Override
			public void onPermissionDenied(UsbDevice d) {
				// ���� ���
				UsbManager usbman = (UsbManager) mApplicationContext
						.getSystemService(Context.USB_SERVICE);
				PendingIntent pi = PendingIntent.getBroadcast(
						mApplicationContext, 0, new Intent(
								ACTION_USB_PERMISSION), 0);
				mApplicationContext.registerReceiver(mPermissionReceiver,
						new IntentFilter(ACTION_USB_PERMISSION));
				usbman.requestPermission(d, pi);
			}
		});
	}

	private void enumerate(IPermissionListener listener) {
		Log.e("enumerating", "enumerating : ����");
		HashMap<String, UsbDevice> devlist = mUsbManager.getDeviceList();
		Iterator<UsbDevice> deviter = devlist.values().iterator();
		while (deviter.hasNext()) {
			device = deviter.next();
			if (device.getVendorId() == VID && device.getProductId() == PID) {
				// ��� ���� �ϰ� ������.
				Log.e("enumerating", "usb deviceID : " + device.getDeviceId());
				if (!mUsbManager.hasPermission(device)) {
					// ������ ���� ��� ȣ��
					listener.onPermissionDenied(device);
				} else {
					// ������ �־��� ��� ������ ���� , ���� ���� ���¿��� ����۽ÿ� �������� ��
					Log.e("enumerating", "usb ���� ���� ȹ��2");
					startHandler();
				}
			}
		}
	}

	private BroadcastReceiver mPermissionReceiver = new PermissionReceiver(
			new IPermissionListener() {
				@Override
				public void onPermissionDenied(UsbDevice d) {
					Log.e("BroadcastReceiver Permission denied on",
							"usb deviceID : " + d.getDeviceId());
				}
			});

	private static interface IPermissionListener {
		void onPermissionDenied(UsbDevice d);
	}

	private class PermissionReceiver extends BroadcastReceiver {
		private final IPermissionListener mPermissionListener;

		public PermissionReceiver(IPermissionListener permissionListener) {
			mPermissionListener = permissionListener;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			mApplicationContext.unregisterReceiver(this);
			if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
				if (!intent.getBooleanExtra(
						UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
					mPermissionListener.onPermissionDenied((UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE));
				} else {
					Log.e("PermissionReceiver", "Permission granted");
					UsbDevice dev = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (dev != null) {
						if (dev.getVendorId() == VID
								&& dev.getProductId() == PID) {
							// ������.
							Log.e("PermissionReceiver", "��ε� ĳ��Ʈ ���ù� ���⼭ ����");
							startHandler();// ������ �䱸�ϴ� ��ε� ĳ��Ʈ ���ù� ȣ�� �Ǹ鼭 ������
											// ������
						}
					} else {
						Log.e("PermissionReceiver", "device not present!");
					}
				}
			}
		}
	}
	// Handler Ŭ����
		class SendMassgeHandler extends Handler {

			@Override
	        public void handleMessage(Message msg) {
	            super.handleMessage(msg);
	             
	            switch (msg.what) {
	            case 0:
	            	MainActivity.t1.setText("WRITE: "+msg.arg1);	
	            	MainActivity.t2.setText(("WRITE: "+msg.arg2));
	            	MainActivity.t3.setText((CharSequence) msg.obj);
	            default:
	                break;
	            }
	        }
		};

	// ����̽� ���� ���� ��������
	// ������ ��û ��Ŷ ����
	// ������ ��û ��Ŷ ����
	// ���� ��Ŷ ����
	// ���� �м�
	// ������ ���� ������ ����. -> ���ۿ� ���� ������ ������ �����ؼ� ���ۿ� ����
	// �׷����� �׸�.//thread ���� �� ����.

	private UsbRunnable write_run;
	private Thread mUsbThread;
	private ObjectOutputStream os;

	private void startHandler() {
		// ��������Ʈ ����� ����Ʈ�� �ʱ�ȭ��
		endpoint_in_out();

		write_run = new UsbRunnable();
		mUsbThread = new Thread(write_run);
		mUsbThread.start();
	}

	/*
	 * usb�κ��� endpoint_in_out
	 */
	public void endpoint_in_out() {
		try {
			// TODO Auto-generated method stub
			conn = mUsbManager.openDevice(device);
			if (!conn.claimInterface(device.getInterface(0), true)) {
				Log.e("endpoint_in_out", "claimInterface error");
			}

			/*
			 * conn.controlTransfer(0x21, 34, 0, 0, null, 0, 0);
			 * conn.controlTransfer(0x21, 32, 0, 0, new byte[] { (byte) 0x80,
			 * 0x25, 0x00, 0x00, 0x00, 0x00, 0x08 }, 7, 0);
			 */

			UsbInterface usbIf = device.getInterface(0);
			Log.e("endpoint_in_out",
					"EndpointCount = " + usbIf.getEndpointCount());
			for (int i = 0; i < usbIf.getEndpointCount(); i++) {
				if (usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
					if (usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
						epIN = usbIf.getEndpoint(i);
						Log.e("endpoint_in_out","epIN address : "+ epIN.getAddress());
						
						Log.e("endpoint_in_out", "epIN index : " + i);
					} else {
						epOUT = usbIf.getEndpoint(i);
						Log.e("endpoint_in_out","epOUT address : "+ epOUT.getAddress());
						Log.e("endpoint_in_out", "epIN index : " + i);
					}
				}
			}
		} catch (Exception e) {
			Log.e("endpoint_in_out", "endpoint_in_out: " + e.getMessage());
		}
	}

	/*
	 * ������ ��û�� ���� ��Ŷ ����
	 */
	class UsbRunnable implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			byte[] byteBuffer;
			int write1 = 0, write2 = 0, write3 = 0;
			
			// ������ ������ ���� ����.
			ucWriteBuffer[0] = 'y';
			ucWriteBuffer[1] = 0xff; // ?
			ucWriteBuffer[2] = 0xff; // ?

			try{
				byteBuffer = new String(ucWriteBuffer).getBytes();
	
				int writec = conn.bulkTransfer(epOUT, byteBuffer,
						COMMAND_SIZE, 1000);
				Log.e("Run ???????????", "Write Data Bytes : " + writec);
			}
			catch(Exception e){
				Log.e("1. Runnable Set ID Size",
						" exception error : " + e.getMessage());
			}
			
			
			// 1. Set ID Size
			ucWriteBuffer[0] = 'I';
			ucWriteBuffer[1] = RECEIVE_MODE;
			ucWriteBuffer[2] = ID_SIZE;
				
			Log.e("1. Runnable Set ID Size", "����");
			Log.e("1. Runnable Set ID Size", "ucWriteBuffer[0] : "
					+ ucWriteBuffer[0]);
			Log.e("1. Runnable Set ID Size", "ucWriteBuffer[1] : "
					+ ucWriteBuffer[1]);
			Log.e("1. Runnable Set ID Size", "ucWriteBuffer[2] : "
					+ ucWriteBuffer[2]);

			try {
				byteBuffer = new String(ucWriteBuffer).getBytes();
				write1 = conn.bulkTransfer(epOUT, byteBuffer,
					COMMAND_SIZE, 1000);

				//Log.e("1. Runnable Set ID Size", "Write Data Bytes : " + writec);
			} catch (Exception e) {
				Log.e("1. Runnable Set ID Size",
						" exception error : " + e.getMessage());
			}

			Log.e("1. Runnable Set ID Size", "��");

			// 2. Set ID Address
			ucWriteBuffer[0] = 'I';
			ucWriteBuffer[1] = ID_ADDRESS; //
			ucWriteBuffer[2] = ID_ADDRESS1;
			ucWriteBuffer[3] = ID_ADDRESS2;
			ucWriteBuffer[4] = ID_ADDRESS3;
			
			Log.e("2. Runnable Set ID Address", "����");
			Log.e("2. Runnable Set ID Address", "ucWriteBuffer[0] : "
					+ ucWriteBuffer[0]);
			Log.e("2. Runnable Set ID Address", "ucWriteBuffer[1] : "
					+ ucWriteBuffer[1]);
			Log.e("2. Runnable Set ID Address", "ucWriteBuffer[2] : "
					+ ucWriteBuffer[2]);						

			try {
				byteBuffer = new String(ucWriteBuffer).getBytes();

				write2 = conn.bulkTransfer(epOUT, byteBuffer,
						COMMAND_SIZE, 1000);
	
				
			//	Log.e("2. Runnable Set ID Address", "Write Data Bytes : "
			//			+ write1);
			} catch (Exception e) {
				Log.e("2. Runnable Set ID Address",
						"exception error : " + e.getMessage());
			}
			Log.e("2. Runnable Set ID Address", "��");

			// 3. Run with Channel
			ucWriteBuffer[0] = 'R';
			ucWriteBuffer[1] = 20; // 1ch
			ucWriteBuffer[2] = PACKET_SIZE;
			
			Log.e("3. Runnable Run with Channel", "����");
			Log.e("3. Runnable Run with Channel", "ucWriteBuffer[0] : "
					+ ucWriteBuffer[0]);
			Log.e("3. Runnable Run with Channel", "ucWriteBuffer[1] : "
					+ ucWriteBuffer[1]);
			Log.e("3. Runnable Run with Channel", "ucWriteBuffer[2] : "
					+ ucWriteBuffer[2]);

			try {
				byteBuffer = new String(ucWriteBuffer).getBytes();

				write3 = conn.bulkTransfer(epOUT, byteBuffer,
						COMMAND_SIZE, 500);
			//	Log.e("3. Runnable Run with Channel", "Write Data Bytes : "
			//			+ writec);
			} catch (Exception e) {
				Log.e("3. Runnable Run with Channel",
						"exception error : " + e.getMessage());
			}
			Log.e("3. Runnable Run with Channel", "��");

			// �����͸� ������

			/*
			 * //�����͸� ������ UsbRead reader = new UsbRead(); Thread tr = new
			 * Thread(reader); tr.start();
			 */
			
			Message msg = mMainHandler.obtainMessage();
			msg.what = 0;
			msg.arg1 = write1;
			msg.arg2 = write2;
			
			msg.obj =write1+","+write2+","+write3;
			
			mMainHandler.sendMessage(msg);
		}
	}

	public void mByteOrder(HCDATA m_sHC) {

		// tByteOrder;
	}
}

class HCDATA {
	int ltime;
	byte mode;
	byte stat;
	short ecg0; // ECG0 12Bit Snap Button electrode
	short ecg1; // ECG1 12Bit External electrode
	short adx; // 10Bit 333mV = 1g REFV = 3.0V
	short ady; // 10Bit 333mV = 1g REFV = 3.0V
	short adz; // 10Bit 333mV = 1g REFV = 3.0V
	short tmp; // �����µ�*10 25.2 = 252
}
