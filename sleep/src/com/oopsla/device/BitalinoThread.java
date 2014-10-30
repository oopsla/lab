package com.oopsla.device;

import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bitalino.comm.BITalinoDevice;
import com.bitalino.comm.BITalinoException;
import com.bitalino.comm.BITalinoFrame;
import com.ooplsa.health.DataQueue;
import com.ooplsa.health.ECG;
import com.ooplsa.health.HR;
import com.oopsla.common.Constants;

/**
 * 
 * @author bgamecho
 *
 */
public class BitalinoThread extends BTDeviceThread{

	private final String BTDeviceThread ="BTDeviceThread";
	private final String BTDeviceThread_HR = "BTDeviceThread_HR";
	
	private int sample_rate;
	private int nFrames; //내 생각에 프레임은 각각의 데이터를 말하는 것 같다.	
	private Queue<Integer> ecg_queue; // 데이터 값을 저장할 프레임
	private SharedPreferences sharedPreferences; 
	private boolean downsample = false;
	private HR hR;
	private int heart_rate = 0; //맥박
	
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
		
		ecg_queue = new LinkedList<Integer>();
		hR = new HR(); //맥박 클래스
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	int[] channels; 
	public void setChannels(int[] channels){
		this.channels = channels;
	}
	
	
	@Override
	public void initialize() {
		super.initialize();
		 		
		_bitalino_dev = null;

		try {
			_bitalino_dev = new BITalinoDevice(sample_rate, channels);
			//데이터 송수신을 위한 프로토콜 open
			_bitalino_dev.open(_inStream, _outStream);		
			
			//데이터 받기 시작. 정의된 채널로부터 데이터 수집을 시작함
			_bitalino_dev.start();		
			
		} catch (BITalinoException e2) {
			e2.printStackTrace();
		}	
	}

	//상위클래스로부터 run을 상속받음. run에서 loop를 호출 함 
	@Override
	public void loop() {
		try {		
			
			//Blocking task . nFrames는 BITlog에서 frameRate이다.			
			BITalinoFrame[] frames = new BITalinoFrame[BITlog.SamplingRate];
			DataQueue[] dataQueues = new DataQueue[4];
			
			//1초동안 받는 샘플 데이터 수를 읽어옴
			frames = _bitalino_dev.read(nFrames);
			Log.e(BTDeviceThread, "ddddddddd1");
			  //read 2 seconds
            for (int i=0; i <4; i++){
                dataQueues[i] = new DataQueue(BITlog.SamplingRate / 2);
                for (BITalinoFrame frame : frames) {
                    dataQueues[i].push(frame.getAnalog(2));
                }
            }
    		Log.e(BTDeviceThread, "ddddddddd12");
    		
            processInThread(dataQueues);
    		Log.e(BTDeviceThread, "ddddddddd13");
			//false이므로 동작하지 않음
			if(downsample){
				BITalinoFrame[] halfFrames = new BITalinoFrame[nFrames/2];
				for(int i = 0; i<halfFrames.length; i++){
					halfFrames[i]= frames[i*2]; 			
				}
				frames = halfFrames;
				//Log.v(TAG, "Downsample: enable");
			}
			
			for(int i = 0; i < nFrames; i++){
				ecg_queue.add(frames[i].getAnalog(2));
			}
			
			int secsToDisplay = Integer.parseInt(sharedPreferences.getString(Constants.PREF_GRAPH_SECONDS,
					Constants.DEFAULT_GRAPH_RANGE));
			int numberExtraEcgSamples = ecg_queue.size() - secsToDisplay * Constants.PARAM_ECG_SAMPLERATE;
			if (numberExtraEcgSamples > 0) {
				for (int i = 1; i < numberExtraEcgSamples; i++) {
					ecg_queue.remove();
				}
			}
		} catch (BITalinoException e1) {
			Log.e(BTDeviceThread, "Error with Bitalino");
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
	        Log.e(BTDeviceThread, "There was an error.", e);
		}
	}

	 public void processInThread(final DataQueue [] dataQueues) throws Exception{
	        Runnable processor = (new Runnable(){
	            public void run(){
	                synchronized (hR) {
	                    ECG ecg= new ECG();
	                    for (int i = 0; i < 4; i++) {
	                        ecg.pushbulk(dataQueues[i].queue);
	                        ecg.DetectPeak();
	                        dataQueues[i].Clear();
	                        hR.CalcHR_ECG(ecg);
	                        ecg.ResetNewPeaks();
	                    }
	                    if (hR.HROK){
	                    	heart_rate = hR.HRV; //추출되는 맥박 값	      
	                    	Log.e(BTDeviceThread_HR,"맥박 : "+ heart_rate);
	                    }else{
	                       Log.e(BTDeviceThread_HR,"Processing ecg. Please wait... ");
	                    }
	                }
	            }
	        });
	        processor.run();
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
				Log.e(BTDeviceThread, "Problems closing the BITalino device");
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

	//BITlog에서 frameRate로 초기화 샘플링 레이트가 200일 때 60개 저장 됨 
	public void setNumFrames(int nFrames) {
		this.nFrames = nFrames;
	}	
	
	public Queue<Integer> getQueue(){
		return ecg_queue;
	}

//	public void setMode(int Mode){
//			this.bitalinoMode = Mode;
//	}
	//false가 저장됨.
	public void setDownsamplingOn(boolean downsample){
		this.downsample = downsample;
		
	}
}