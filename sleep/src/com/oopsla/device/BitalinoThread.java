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
	private int nFrames; //�� ������ �������� ������ �����͸� ���ϴ� �� ����.
	private int packNum;
//	private int bitalinoMode; 
	private Queue<BITalinoFrame> frame_queue; // ������ ���� ������ ������
	private SharedPreferences sharedPreferences; 
	private boolean downsample = false;
	
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
			//������ �ۼ����� ���� �������� open
			_bitalino_dev.open(_inStream, _outStream);		
			
			//String firmVersion = _bitalino_dev.version();
			//Log.v(TAG, "Firm Version: "+firmVersion);
			
			//_bitalino_dev.setBattThreshold(3.8); // Only Works in LIVE MODE
						
//			_bitalino_dev.start(this.bitalinoMode, channels);
			//������ �ޱ� ����. ���ǵ� ä�ηκ��� ������ ������ ������
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

	//����Ŭ�����κ��� run�� ��ӹ���. run���� loop�� ȣ�� �� 
	@Override
	public void loop() {
		try {		
			
			//Blocking task . nFrames�� BITlog���� frameRate�̴�. 
			BITalinoFrame frames = new BITalinoFrame;
			
			//��Ŷ ������ �ð� , ���� �ѹ� �� ������ ������ ������ �ϳ��� ����.
			Log.i(TAG, "Monitor: Read "+(packNum++)+" :"+ System.currentTimeMillis());
			//ApplicationClass.showTop();
			
			//���� �ѹ� �� ������ �޴� �������� ���� 1��
			frames = _bitalino_dev.read(nFrames);
			
			if(downsample){
				BITalinoFrame[] halfFrames = new BITalinoFrame[nFrames/2];
				for(int i = 0; i<halfFrames.length; i++){
					halfFrames[i]= frames[i*2]; 			
				}
				frames = halfFrames;
				//Log.v(TAG, "Downsample: enable");
			}

			//ť���ٰ� ������ ����
			for(int i = 0; i < nFrames; i++)
				frame_queue.add(frames[i]);
			
			int secsToDisplay = Integer.parseInt(sharedPreferences.getString(Constants.PREF_GRAPH_SECONDS,
					Constants.DEFAULT_GRAPH_RANGE));
			int numberExtraAudioSamples = frame_queue.size() - secsToDisplay * 1000 / TIMER_INTERVAL;
		//	Log.e("TAG","numberExtraSample:"+numberExtraAudioSamples);
			if (numberExtraAudioSamples > 0) {
				for (int i = 0; i < numberExtraAudioSamples; i++) {
					frame_queue.remove();
				//	Log.e("TAG", "����� ������ ����: " + audioQueue.size());
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

	//BITlog���� frameRate�� �ʱ�ȭ
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