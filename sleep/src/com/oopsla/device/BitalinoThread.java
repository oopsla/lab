package com.oopsla.device;

import java.util.Queue;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bitalino.comm.BITalinoDevice;
import com.bitalino.comm.BITalinoException;
import com.bitalino.comm.BITalinoFrame;
import com.oopsla.common.Constants;

/**
 * 
 * @author bgamecho
 *
 */
public class BitalinoThread extends BTDeviceThread{

	public final static String TAG ="BITalinoThread";
	private int TIMER_INTERVAL = 50;
	private int sample_rate;
	private int nFrames; //내 생각에 프레임은 각각의 데이터를 말하는 것 같다.
	private int packNum;
//	private int bitalinoMode; 
	private Queue<BITalinoFrame> frame_queue; // 데이터 값을 저장할 프레임
	private SharedPreferences sharedPreferences; 
	private boolean downsample = false;
	
	private BITalinoDevice _bitalino_dev = null;
	
	/**
	 * BITalinoThreadConstructor
	 * @param myHandler : looperThread의 public 핸들러임  , 하나는 수신기의 맥주소
	 * context는 시스템에서 제공하는 Api에 접근하기 위해서 사용 됨
	 */
	public BitalinoThread(String remoteDevice, Context context) throws Exception{

		this.setName("BITalinoThread");

		//블루투스 페어링하는 부분. 해당하는 하드웨어(수신기)의 객체를 가져옴
		super.setupBT(remoteDevice);

		//디바이스를 설정함
		super.initComm();
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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
			BITalinoFrame frames = new BITalinoFrame;
			
			//패킷 갯수당 시간 , 루프 한번 돌 때마다 데이터 프레임 하나씩 증가.
			Log.i(TAG, "Monitor: Read "+(packNum++)+" :"+ System.currentTimeMillis());
			//ApplicationClass.showTop();
			
			//루프 한번 돌 때마다 받는 데이터의 수를 1개
			frames = _bitalino_dev.read(nFrames);
			
			if(downsample){
				BITalinoFrame[] halfFrames = new BITalinoFrame[nFrames/2];
				for(int i = 0; i<halfFrames.length; i++){
					halfFrames[i]= frames[i*2]; 			
				}
				frames = halfFrames;
				//Log.v(TAG, "Downsample: enable");
			}

			//큐에다가 데이터 저장
			for(int i = 0; i < nFrames; i++)
				frame_queue.add(frames[i]);
			
			int secsToDisplay = Integer.parseInt(sharedPreferences.getString(Constants.PREF_GRAPH_SECONDS,
					Constants.DEFAULT_GRAPH_RANGE));
			int numberExtraAudioSamples = frame_queue.size() - secsToDisplay * 1000 / TIMER_INTERVAL;
		//	Log.e("TAG","numberExtraSample:"+numberExtraAudioSamples);
			if (numberExtraAudioSamples > 0) {
				for (int i = 0; i < numberExtraAudioSamples; i++) {
					frame_queue.remove();
				//	Log.e("TAG", "오디오 사이즈 제거: " + audioQueue.size());
				}
			}

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
	
	public Queue<BITalinoFrame> getQueue(){
		return frame_queue;
	}

//	public void setMode(int Mode){
//			this.bitalinoMode = Mode;
//	}
	
	public void setDownsamplingOn(boolean downsample){
		this.downsample = downsample;
		
	}
}