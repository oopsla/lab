package com.bitalino.BITlog.device;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bitalino.comm.BITalinoDevice;
import com.bitalino.comm.BITalinoException;
import com.bitalino.comm.BITalinoFrame;

/**
 * 
 * @author bgamecho
 *
 */
public class BitalinoThread extends BTDeviceThread{

	public final static String TAG ="BITalinoThread";

	private int sample_rate;
	private int nFrames; //내 생각에 프레임은 각각의 데이터를 말하는 것 같다.
	private int packNum;
//	private int bitalinoMode; 
	private boolean downsample = false;
	
	private BITalinoDevice _bitalino_dev = null;
	
	/**
	 * BITalinoThreadConstructor
	 * @param myHandler : looperThread의 public 핸들러임  , 하나는 수신기의 맥주소
	 */
	public BitalinoThread(Handler myHandler, String remoteDevice) throws Exception{
		super(myHandler);

		this.setName("BITalinoThread");

		//블루투스 페어링하는 부분. 해당하는 하드웨어(수신기)의 객체를 가져옴
		super.setupBT(remoteDevice);

		//디바이스를 설정함
		super.initComm();

		//맥주소
		sendMessage("OK", "Connected to BITalino device at: "+_bluetoothDev.getAddress());
	}
	
//	public void setLed(boolean val){
//		int led = (val) ? 1 : 0;
//		int[] digOutputs = {0, 0, led, 0};
//		try {
//			_bitalino_dev.setDigitalOutput(digOutputs);
//		} catch (BITalinoException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	int[] channels; 
	public void setChannels(int[] channels){
		this.channels = channels;
	}
	
	@Override
	public void initialize() {
		super.initialize();
		 
		packNum = 0;
		_bitalino_dev = null;

		try {
			_bitalino_dev = new BITalinoDevice(sample_rate, channels);
			//데이터 송수신을 위한 프로토콜 open
			_bitalino_dev.open(_inStream, _outStream);		
			
			//String firmVersion = _bitalino_dev.version();
			//Log.v(TAG, "Firm Version: "+firmVersion);
			
			//_bitalino_dev.setBattThreshold(3.8); // Only Works in LIVE MODE
						
//			_bitalino_dev.start(this.bitalinoMode, channels);
			//데이터 받기 시작. 정의된 채널로부터 데이터 수집을 시작함
			_bitalino_dev.start();
		
			// Led in the Bitalino Boards, blinks twice
//			if(_bitalino_dev.getMode()==BITalinoDevice.LIVE_MODE){
//				setLed(true);
//				Thread.sleep(100);
//				setLed(false);
//				Thread.sleep(50);
//				setLed(true);
//				Thread.sleep(100);
//				setLed(false);
//			}
					
			
			
		} catch (BITalinoException e2) {
			e2.printStackTrace();
		}
//		catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}   		
	}

	//상위클래스로부터 run을 상속받음. run에서 loop를 호출 함 
	@Override
	public void loop() {
		try {		
			
			//Blocking task . nFrames는 BITlog에서 frameRate이다. 
			BITalinoFrame[] frames = new BITalinoFrame[nFrames];
			
			//패킷 갯수당 시간 , 루프 한번 돌 때마다 데이터 프레임 하나씩 증가.
			Log.i(TAG, "Monitor: Read "+(packNum++)+" :"+ System.currentTimeMillis());
			//ApplicationClass.showTop();
			
			//내 생각에 루프 한번 돌 때마다 받는 데이터의 수가 30개인듯.
			frames = _bitalino_dev.read(nFrames);

			if(downsample){
				BITalinoFrame[] halfFrames = new BITalinoFrame[nFrames/2];
				for(int i = 0; i<halfFrames.length; i++){
					halfFrames[i]= frames[i*2]; 			
				}
				frames = halfFrames;
				//Log.v(TAG, "Downsample: enable");
			}

			
			//Send the data
			Message msg = new Message();
		
			//프레임을 전달.
			Bundle myDataBundle = new Bundle();
			myDataBundle.putSerializable("bitalino_data", frames);
			msg.setData(myDataBundle);
			myHandler.sendMessage(msg);  //해당 데이터는 looperThread의 핸들러에서 처리 됨

		} catch (BITalinoException e1) {
			Log.e(TAG, "Error with Bitalino");
			e1.printStackTrace();
		}

	}

	@Override
	public void close() {
		
		if(_bitalino_dev!=null){
			// Stop the data acquisition
			try {
//				if(_bitalino_dev.getMode()==BITalinoDevice.LIVE_MODE){
//					setLed(true);				
//					Thread.sleep(500);
//					setLed(false); // Only works in LIVE mode
//				}
				_bitalino_dev.stop();
			} catch (BITalinoException e) {
				Log.e(TAG, "Problems closing the BITalino device");
				e.printStackTrace();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
		}
		super.close();
	}

	public int getSampleRate() {
		return sample_rate;
	}

	public void setSampleRate(int sample_rate) {
		this.sample_rate = sample_rate;
	}

	public int getNumFrames() {
		return nFrames;
	}

	//BITlog에서 frameRate로 초기화
	public void setNumFrames(int nFrames) {
		this.nFrames = nFrames;
	}	

//	public void setMode(int Mode){
//			this.bitalinoMode = Mode;
//	}
	
	public void setDownsamplingOn(boolean downsample){
		this.downsample = downsample;
		
	}
}