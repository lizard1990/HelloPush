package com.yeshen.hellopush;

import com.ibm.mqtt.IMqttClient;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttPersistence;
import com.ibm.mqtt.MqttSimpleCallback;

public class CMqttClient {
	//default setting
	private final String defaultURL="tcp://127.0.0.1@1883";
	private final boolean MQTT_CLEAN_START  = false;
	private final short MQTT_KEEP_ALIVE  = 20;
	/**
	 * see http://www.ibm.com/developerworks/cn/lotus/expeditor-mqtt/index.html
	 * <br><br>0:至多一次<br> 1:至少一次<br> 2:只有一次
	 * */
	private final int[] MQTT_QUALITIES_OF_SERVICE=new int[]{2};
	
	private IMqttClient innerClient;
	private MqttSimpleCallback iCallbackHandler;
	private String iTopic,iHardwareId;
	
	public CMqttClient(){}
	
	public boolean create(String IpAddress,String ATopic,String aClientID){
		String url=defaultURL.replace("127.0.0.1", IpAddress);
		return create(url,ATopic,aClientID, null);
	}
	
	public boolean create(String MqttURL,String aTopic,String aHardwareId,MqttPersistence APersistence){
		iTopic=aTopic;
		iHardwareId=aHardwareId;
		boolean isCreated=true;
		try {
			innerClient=MqttClient.createMqttClient(MqttURL,APersistence);
		} catch (MqttException e) {
			isCreated=false;
			Until.onError(e, "MqttClient creat error");
		}
		return isCreated;
	}
	
	public void registerSimpleHandler(MqttSimpleCallback aCallbackHandler){
		iCallbackHandler=aCallbackHandler;
	}
	
	
	public boolean start(){
		if(!connect()||!Subscribe()){
			return false;
		}
		return true;
	}
	
	public void stop(){
		try {
			innerClient.disconnect();
		} catch (MqttException e) {
			Until.onError(e, "MqttPersistenceException disconnect error");
		}
	}
	
	private boolean connect(){
		try{
			innerClient.connect(iHardwareId, MQTT_CLEAN_START, MQTT_KEEP_ALIVE);
		}catch(MqttException e){
			Until.onError(e, "CMqttClient connect error");
			return false;
		}
		if(iCallbackHandler!=null){
			innerClient.registerSimpleHandler(iCallbackHandler);
		}
		return innerClient.isConnected();
	}
	
	private boolean Subscribe(){
		String[] topics = { iTopic };
		try {
			innerClient.subscribe(topics, MQTT_QUALITIES_OF_SERVICE);
		}catch (MqttException e) {
			Until.onError(e, "CMqttClient Subscribe error");
			return false;
		}
		return true;
	}
	
	public boolean isConnected(){
		return innerClient.isConnected();
	}
	
	/**Sending of ping messages within the keepalive interval is automatically handled by the MQTT client library*/
	@Deprecated
	public boolean keepAlive(){
		try {
			innerClient.ping();
		} catch (MqttException e) {
			Until.onError(e, "Mqtt keepAlive error");
			return false;
		}
		return true;
	}
	
	
	
}
