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
	private int nFrames; //�� ������ �������� ������ �����͸� ���ϴ� �� ����.	
	private Queue<Integer> ecg_queue; // ������ ���� ������ ������
	private SharedPreferences sharedPreferences; 
	private boolean downsample = false;
	private HR hR;
	private int heart_rate = 0; //�ƹ�
	
	private BITalinoDevice _bitalino_dev = null;
	
	/**
	 * BITalinoThreadConstructor
	 * @param myHandler : looperThread�� public �ڵ鷯��  , �ϳ��� ���ű��� ���ּ�
	 * context�� �ý��ۿ��� �����ϴ� Api�� �����ϱ� ���ؼ� ��� ��
	 */
	public BitalinoThread(String remoteDevice, Context context) throws Exception{

		this.setName("BITalinoThread");

		//������� ���ϴ� �κ�. �ش��ϴ� �ϵ����(���ű�)�� ��ü�� ������
		super.setupBT(remoteDevice);

		//����̽��� ������
		super.initComm();
		
		ecg_queue = new LinkedList<Integer>();
		hR = new HR(); //�ƹ� Ŭ����
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
			//������ �ۼ����� ���� �������� open
			_bitalino_dev.open(_inStream, _outStream);		
			
			//������ �ޱ� ����. ���ǵ� ä�ηκ��� ������ ������ ������
			_bitalino_dev.start();		
			
		} catch (BITalinoException e2) {
			e2.printStackTrace();
		}	
	}

	//����Ŭ�����κ��� run�� ��ӹ���. run���� loop�� ȣ�� �� 
	@Override
	public void loop() {
		try {		
			
			//Blocking task . nFrames�� BITlog���� frameRate�̴�.			
			BITalinoFrame[] frames = new BITalinoFrame[BITlog.SamplingRate];
			DataQueue[] dataQueues = new DataQueue[4];
			
			//1�ʵ��� �޴� ���� ������ ���� �о��
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
			//false�̹Ƿ� �������� ����
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
	                    	heart_rate = hR.HRV; //����Ǵ� �ƹ� ��	      
	                    	Log.e(BTDeviceThread_HR,"�ƹ� : "+ heart_rate);
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

	//BITlog���� frameRate�� �ʱ�ȭ ���ø� ����Ʈ�� 200�� �� 60�� ���� �� 
	public void setNumFrames(int nFrames) {
		this.nFrames = nFrames;
	}	
	
	public Queue<Integer> getQueue(){
		return ecg_queue;
	}

//	public void setMode(int Mode){
//			this.bitalinoMode = Mode;
//	}
	//false�� �����.
	public void setDownsamplingOn(boolean downsample){
		this.downsample = downsample;
		
	}
}