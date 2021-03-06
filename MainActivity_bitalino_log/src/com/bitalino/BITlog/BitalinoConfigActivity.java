package com.bitalino.BITlog;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

/**
 * 
 * @author bgamecho
 *
 */
public class BitalinoConfigActivity extends Activity {

	public final String TAG ="Bitalino Config Activity";
	
	public Spinner spinnerSR;
	public boolean beforeStart;
	public CheckBox dOut;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bitalino_config);
		
		beforeStart = true;
		
		dOut = (CheckBox) findViewById(R.id.checkBoxDout);
		if(BITlog.digitalOutputs){
			dOut.setChecked(true);
		}else{
			dOut.setChecked(false);
		}
		dOut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
		       @Override
		       public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		    	   if(isChecked){
		    		    BITlog.digitalOutputs = true;
		    	   }else{
		    		   BITlog.digitalOutputs  = false;
		    	   }			    		   
		       }
		});     
		
		spinnerSR = (Spinner) findViewById(R.id.spinnerSamplingRate);
		ArrayAdapter<CharSequence> adapterSR = ArrayAdapter.createFromResource(this,
		        R.array.sampling_rate, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapterSR.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinnerSR.setAdapter(adapterSR);
		spinnerSR.setSelection(2);
		
		spinnerSR.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				String mySamplingRate = spinnerSR.getSelectedItem().toString();
				BITlog.SamplingRate = Integer.valueOf(mySamplingRate);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				spinnerSR.setSelection(2);
			}

		});
		
		//6개의 체크박스 ,ecg는 3번
		CheckBox cbArray[] = new CheckBox[6];
		
		cbArray[0] = (CheckBox) findViewById(R.id.checkBoxCh1);
		cbArray[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

		       @Override
		       public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		    	   if(isChecked){
		   			Log.e("체크박스",": "+1);
		    		   addChecked(0);	     
		    	   }else{
		    		   removeChecked(0);
		    	   }
			    		   
		       }
		});     
		Log.e("환경설정", "asdsadasdas "+beforeStart);
		cbArray[3] = (CheckBox) findViewById(R.id.checkBoxCh4);
		cbArray[3].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

		       @Override
		       public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		    	   if(isChecked){
		    		   Log.e("체크박스",": "+4);
		    		   addChecked(3);	     
		    	   }else{
		    		   removeChecked(3);
		    	   }
			    		   
		       }
		});     
		
		
		cbArray[1] = (CheckBox) findViewById(R.id.checkBoxCh2);
		cbArray[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

		       @Override
		       public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		    	   if(isChecked){
		    		   Log.e("체크박스",": "+2);
		    		   addChecked(1);	     
		    	   }else{
		    		   removeChecked(1);
		    	   }
			    		   
		       }
		});     
		
		
		cbArray[4] = (CheckBox) findViewById(R.id.checkBoxCh5);
		cbArray[4].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

		       @Override
		       public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		    	   if(isChecked){
		    		   Log.e("체크박스",": "+5);
		    		   addChecked(4);	     
		    	   }else{
		    		   removeChecked(4);
		    	   }
			    		   
		       }
		});     
	
		cbArray[2] = (CheckBox) findViewById(R.id.checkBoxCh3);
		cbArray[2].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

		       @Override
		       public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		    	   if(isChecked){
		    		   Log.e("체크박스",": "+3);
		    		   addChecked(2);	     
		    	   }else{
		    		   removeChecked(2);
		    	   }
			    		   
		       }
		});     
		
		
		cbArray[5] = (CheckBox) findViewById(R.id.checkBoxCh6);
		cbArray[5].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

		       @Override
		       public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		    	   if(isChecked){
		    		   Log.e("체크박스",": "+6);
		    		   addChecked(5);	     
		    	   }else{
		    		   removeChecked(5);
		    	   }
			    		   
		       }
		});     
		
		for(int aux : BITlog.channels){
			cbArray[aux].setChecked(true);
		}
		
		Log.e("환경설정", "beforeStart _onCreate1: "+beforeStart);
		beforeStart = false;
		Log.e("환경설정", "beforeStart _onCreate2: "+beforeStart);
	}
	
	public void addChecked(int channel){
		Log.e("환경설정", "beforeStart_addChecked : "+beforeStart);
		if(!beforeStart){
			ArrayList<Integer> aux = new ArrayList<Integer>();
			//처음에 디폴트 값 aux를 0,1,2로 초기화
			for(int i = 0; i<6; i++){
				boolean find = false;
				for(int j=0; j<BITlog.channels.length; j++){
					if(i == BITlog.channels[j]){
						find = true;
						Log.e("환경설정__",i+", "+BITlog.channels[j]);
						aux.add(i, Integer.valueOf(i));
					}
				}
				//
				if(!find) aux.add(i, null);
			}
			//무조건 찍힘
			if(aux.get(channel) == null){
				Log.e("채널",": "+channel);
				aux.add(channel, channel);			
			}
	
		    int[] ret = new int[BITlog.channels.length+1];
		    int ret_index = 0;
		    for (int i=0; i < 6; i++)
		    {
		    	if(aux.get(i)!=null){
		    	    ret[ret_index] = aux.get(i).intValue();
		    	    ret_index++;
		    	}
		    }
		
		    BITlog.channels = ret;
		    for(int i = 0 ; i < BITlog.channels.length; i++){
		    	Log.e("Bitlog", ": "+ BITlog.channels.length);
		    	Log.e("최종적으로", ": "+BITlog.channels[i]);
		    }
		}
	}
	
	
	public void removeChecked(int channel){
		if(!beforeStart){
		ArrayList<Integer> aux = new ArrayList<Integer>();
		for(int i = 0; i<6; i++){
			boolean find = false;
			for(int j=0; j<BITlog.channels.length; j++){
				if(i == BITlog.channels[j]){
					find = true;
					aux.add(i, Integer.valueOf(i));
				}
			}
			if(!find) aux.add(i, null);
		}
		
		if(aux.get(channel) == channel){
			aux.set(channel, null);
		}

	    int[] ret = new int[BITlog.channels.length-1];
	    int ret_index = 0;
	    for (int i=0; i < 6; i++)
	    {
	    	if(aux.get(i)!=null){
	    	    ret[ret_index] = aux.get(i).intValue();
	    	    ret_index++;
	    	}
	    }
	
	    BITlog.channels = ret;
	    
		}
	}
	
}
