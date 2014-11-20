package com.yeshen.hellopush;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Until {
	public final static int UserId=7;
	public final static String MQTTServiceAddress="192.168.1.76";
	
	public static void onError(Exception e,String message){
		if(BuildConfig.DEBUG){
			Log.e("error", message);
			if(e!=null){
				e.printStackTrace();
			}
		}
	}
	
	public static String getDeviceUuid(Context iContext){
	    TelephonyManager tm = (TelephonyManager)iContext.getSystemService(Context.TELEPHONY_SERVICE);
	    
	    String androidId =android.provider.Settings.Secure.getString(
	    		iContext.getContentResolver(),
	    		android.provider.Settings.Secure.ANDROID_ID);
	    StringBuilder builder=new StringBuilder();
	    builder.append(tm.getDeviceId())
			    .append(tm.getSimSerialNumber())
			    .append(androidId);
	    int hashCode=builder.toString().hashCode();
	    builder.delete(0, builder.length());
	    builder.append(UserId).append(hashCode);
	    tm=null;
	    return builder.toString();
	}
	
}
