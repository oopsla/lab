package com.oopsla.device;

import android.app.Application;
import android.content.Context;

/**
 * 
 * @author bgamecho
 *
 */
public class BITlog extends Application {

	public static String MAC="98:d3:31:b2:c4:23";
	public static int SamplingRate=100;
	public static int frameRate= 1;
	public static int[] channels = {2};

	public static boolean digitalOutputs = false;
	
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context mContext) {
        BITlog.mContext = mContext;
    }
}
