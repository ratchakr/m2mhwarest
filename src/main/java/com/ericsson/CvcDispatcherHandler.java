package com.ericsson;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;

@Component
public class CvcDispatcherHandler {
	

	private final static Logger logger = Logger.getLogger(CvcDispatcherHandler.class.getName());			
	

	/** The max mqtt clientid length. */
	int MAX_MQTT_CLIENTID_LENGTH = 23;
	
	

	
	/**
	 * Already connected.
	 *
	 * @param aClient the a client
	 * @return true, if successful
	 */
	public boolean alreadyConnected(MqttAsyncClient aClient) {
		return aClient.isConnected();
	}

	/**
	 * Generate client id.
	 *
	 * @param serviceIdentifier the service identifier
	 * @return the string
	 */
	private String generateClientId(String serviceIdentifier) {
		String mqttClientId = null;
		// generate a unique client ID - I'm basing this on a combination of
		// ServiceId and the current timestamp
		String timestamp = "" + (new Date()).getTime();
		mqttClientId = serviceIdentifier + timestamp ;
		// truncate - MQTT spec doesn't allow client ids longer than 23 chars
		logger.fine("mqttClientId ::: "+mqttClientId);
		if (mqttClientId.length() > MAX_MQTT_CLIENTID_LENGTH) {
			mqttClientId = mqttClientId.substring(0, MAX_MQTT_CLIENTID_LENGTH);
		}
		return mqttClientId;
	}

	/**
	 * Gets the tcp address.
	 *
	 * @return the tcp address
	 */
	public String getTcpAddress() {
		return "tcp://10.111.133.82:1884";
	}	
	

	
	/**
	 * 
	 * @param aClientId
	 * @return
	 */
	public MqttAsyncClient getAsyncClient(String aClientId) {
		MqttAsyncClient asyncClient = null;
		try {
			String clientId = generateClientId(aClientId);
			logger.info("ASDP Client Id generated towards CVCDispatcher is === "+clientId);
			asyncClient = new MqttAsyncClient(getTcpAddress(), clientId, new MemoryPersistence());
		} catch (MqttException e) {
			e.printStackTrace();
			logger.fine(" Exception creating Async Client for ASDP Dispatcher::: "+e.getMessage());
		}
		return asyncClient;
	}	
	
	/**
	 * Connect to broker.
	 *
	 * @param client the client
	 * @return the i mqtt token
	 * @throws MqttException the mqtt exception
	 */
	public IMqttToken connectToDispatcherBroker(MqttAsyncClient client) throws MqttException {
		MqttConnectOptions connOptions = new MqttConnectOptions();
		connOptions.setCleanSession(true);
		connOptions.setConnectionTimeout(5000);
		IMqttToken conToken = null;
		logger.info("    ASDP client is connected ?? :::       " + client.isConnected());
		if (!alreadyConnected(client)) {
			conToken = client.connect(connOptions);
		}
		int retry = 0;
		int maxRetry = 5;
		while (conToken != null && !conToken.isComplete() && retry < maxRetry) {
			try {
				Thread.sleep(1000);
				retry++;
			} catch (InterruptedException e) {
				logger.fine("Thread InterruptedException");
			}
		}
		if ((conToken!= null) && (!conToken.isComplete())) {
			logger.fine(" *********  CvcDispatcher broker is down. Please try after some time  ************** ");
		}
		return conToken;
	}
	
	/**
	 * 
	 * @param aClient
	 * @throws MqttException
	 */
	public void disconnectClient(MqttAsyncClient aClient) throws MqttException {
		aClient.unsubscribe("remote/#");
		aClient.disconnect();
		logger.info(" ASDP Client is disconnected from MQTT broker...");
	}

	/**
	 * 
	 * @param aClient
	 * @param xmlData
	 * @param aVin
	 * @return
	 * @throws RemoteServicesException
	 */
	public IMqttDeliveryToken publishToTopic(MqttAsyncClient aClient, String xmlData, String aVin) {
		IMqttDeliveryToken token = null;
		try {
			String topicName = "m2mdm/devicedata";


			MqttMessage message = new MqttMessage();
			System.out.println("=== xml data in dispatcher === "+xmlData);
			byte[] b = xmlData.getBytes(Charset.forName("UTF-8"));
			System.out.println("=== bytes === "+b);
			message.setPayload(b);
			message.setQos(2);
			logger.info("message payload \" with QoS = " + message.getQos());
			token = aClient.publish(topicName, message);
			token.waitForCompletion(10000);
		} catch (MqttException e) {
			logger.fine("MqttException: " + e.getMessage());
			
		}
		logger.info("Message published Successfully to Mqtt");
		return token;

	}

	
}
