package com.yeshen.hellopush;

import com.ibm.mqtt.MqttSimpleCallback;
import com.yeshen.hellopush.CAlarmTimer.IRunable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String TAG="MainActivity";
	private final String MSG="MSG";
	private CAlarmTimer iTimer;
	private CMqttClient imqttclient;
	private TextView iMessageTextView;
	private Handler iHandlerMessage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		String ReadmeStr="<b>HelloPush</b> <br>This is an Android MQTT client using IBM's library for more see #<br><br><br>YeShen in Landow<br>2014.11";
		ReadmeStr=ReadmeStr.replace("#", "<a href='http://www.ibm.com/developerworks/cn/lotus/expeditor-mqtt/index.html'>IBM doc</a>");
		TextView tv=((TextView)findViewById(R.id.readme));
		tv.setText(Html.fromHtml(ReadmeStr));
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		iMessageTextView=(TextView)findViewById(R.id.message);
		
		iHandlerMessage=new UIHandler(Looper.myLooper());
		initializeTimer();
		initializeMQTT();
	}
	
	@Override
	protected void onDestroy(){
		iTimer.unredisterTimer();
		DestroyMQTT();
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void initializeTimer(){
		iTimer=new CAlarmTimer(this);
		iTimer.setOnRun(new IRunable() {
			@Override
			public void runOnThread() {
				StartConnection();
			}
		});
	}
	
	private void initializeMQTT(){
		imqttclient=new CMqttClient();
		String id=Until.getDeviceUuid(MainActivity.this);
		imqttclient.create(Until.MQTTServiceAddress, String.valueOf(Until.UserId), id);
		
		imqttclient.registerSimpleHandler(new MqttSimpleCallback() {
			@Override
			public void publishArrived(String TopicName, byte[] Payload, int QoS, boolean retained)
					throws Exception {
				Log.e("publishArrived current_Thread", Thread.currentThread().getName());
				Message msg = new Message();
				Bundle data = new Bundle();
				data.putString(MSG, new String(Payload,"GBK"));
				msg.setData(data);
				msg.what = 0;
				iHandlerMessage.sendMessage(msg);
			}
			
			@Override
			public void connectionLost() throws Exception {
				iTimer.registerTimer(30*1000);
			}
		});
		iTimer.registerTimer(1000);
	}
	
	private void StartConnection(){
		if(!imqttclient.start()){
			iTimer.registerTimer(30*1000);
		}else{
			iTimer.unredisterTimer();
		}
	}
	
	private void DestroyMQTT(){
		if(imqttclient.isConnected()){
			imqttclient.stop();
			Log.e(TAG, "STOP");
		}
	}
	
	private class UIHandler extends Handler{
		public UIHandler(Looper looper){
			super(looper);
		}
		public void handleMessage(Message msg){
			String meassgeStr="<b>Message:</b><br><br>#";
			String message=meassgeStr.replace("#", msg.getData().getString(MSG, ""));
			iMessageTextView.setText(Html.fromHtml(message));
		}
	}


}
