package com.yeshen.hellopush;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class CAlarmTimer{
	
	private final String defaultId="CAlarmTimer-0";
	private AlarmTheards iTheards;
	private AlarmManager alarmMgr;
	private Context iContext;
	private IRunable iRunable;
	
	
	public interface IRunable{
		public abstract void runOnThread();
	}
	
	public CAlarmTimer(Context context){
		iContext=context;
		alarmMgr = (AlarmManager)iContext.getSystemService(Context.ALARM_SERVICE);
	}
	
	public void registerTimer(long interval){
		if(iTheards==null){
			registerTimer(defaultId,interval);
		}else{
			iTheards.interval=interval;
			resetAlarm(defaultId);
		}
	}
	
	public void registerTimer(String id,long interval){
		Intent i = new Intent(id);
		PendingIntent pi = PendingIntent.getBroadcast(iContext, 0, i, 0);
		registerTimer(id,pi,interval);
	}
	
	public void registerTimer(String id,PendingIntent Intent,long interval){
		alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, Intent);
		BroadcastReceiver receiver=new AlarmReceive();
		iContext.registerReceiver(receiver, new IntentFilter(id));
		iTheards=new AlarmTheards(id,Intent,interval,receiver);
	}
	
	public void setOnRun(IRunable runable){
		iRunable=runable;
	}
	
	public void resetAlarm(String aId){
		alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + iTheards.interval, iTheards.getPendingIntent());				
	}
	
	public void unredisterTimer(){
		if(iTheards!=null){
			iContext.unregisterReceiver(iTheards.getReceiver());
			alarmMgr.cancel(iTheards.getPendingIntent());
			iTheards=null;			
		}
		return;
	}
	

	private class AlarmTheards extends Object{
		private String iName;
		private PendingIntent iIntent;
		private BroadcastReceiver iBroadcastReceiver;
		public long interval;
		private Thread iTheards;
		public AlarmTheards(String name,PendingIntent intent,long aInterval,BroadcastReceiver receiver){
			iName=name;
			iIntent=intent;
			iBroadcastReceiver=receiver;
			interval=aInterval;
		}
		
		@Override
		public boolean equals(Object a){
			if(a==null)return false;
			return iName.equals(a);
		}
		public PendingIntent getPendingIntent(){
			return this.iIntent;
		}
		public BroadcastReceiver getReceiver(){
			return iBroadcastReceiver;
		}
		public void setThread(Thread a){
			iTheards=a;
			if(a!=null){
				iTheards.start();				
			}
		}
	}
	
	private class AlarmReceive extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			iTheards.setThread(getThread());
		}
	}
	
	private Thread getThread(){
		return new Thread(new Runnable() {
			@Override
			public void run() {
				if(iRunable!=null){
					iRunable.runOnThread();
				}
			}
		});
	}
}
