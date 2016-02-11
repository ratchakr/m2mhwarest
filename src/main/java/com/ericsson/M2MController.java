package com.ericsson;

import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Controller
@RequestMapping(value = "/m2m")
public class M2MController {

	/** The logger. */
	private Logger logger = LoggerFactory.getLogger(M2MController.class);

	@Autowired
	CvcDispatcherHandler m2mDispatcher;

	/**
	 * Ping.
	 * 
	 * @return the response entity
	 */
	@RequestMapping(value = "/ping", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity ping() {
		logger.info("Health check... PING!");
		return new ResponseEntity(HttpStatus.OK);
	}

	@RequestMapping(value = "/post", method = RequestMethod.POST)
	public @ResponseBody void createEmployee(@RequestBody Employee emp) {
		logger.info("Start createEmployee.");
		emp.setCreatedDate(new Date());
		logger.info("Emp values = " + emp);
	}

	
	/**
	 * 
	 * @param msgId
	 * @return
	 */
	@RequestMapping(value = "/get/{messageid}", method = RequestMethod.GET)
	public @ResponseBody PayLoad getPayload(@PathVariable("messageid") int msgId) {
		logger.info("Start getData for ID="+msgId);
		
		
		
		PayLoad ret = null;
		try {
			ret = convertXmlToJson (retrieveM2MData(msgId));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}	
	
	private String retrieveM2MData( Integer messagId) throws Exception {
		String m2mData = null;
		String m2mAppId = null, m2mAppPwd = null;
		String enterpriseCustomerId = "MSDPUSERID001";
		M2MDataRetrieverImpl m2mDataRetriever = new M2MDataRetrieverImpl ();
		System.out.println("=========  enterpriseCustomerId in NB Call :::  ================        "+enterpriseCustomerId);
			m2mAppId = "";
			m2mAppPwd = "";
		
		try {
			String msgId = String.valueOf(messagId);
			m2mData = m2mDataRetriever.retrieveDataFromDataOutputInterface(m2mAppId, m2mAppPwd, enterpriseCustomerId, msgId);
			System.out.println("m2mData received from NB =====         " + m2mData);
			

		} catch (Exception e) {
			System.err.println("Exception in retrieveDiagnosticData:: " + e.getMessage());
		}
		return m2mData;
	}	
	
	/**
	 * 
	 * @param xml 
	 * @return
	 */
	private PayLoad convertXmlToJson(String xml) {
		// TODO Auto-generated method stub
		return null;
	}

	@RequestMapping(value = "/postpayload", method = RequestMethod.POST)
	public @ResponseBody void save(@RequestBody PayLoad payload) {
		logger.info("Start saving data to m2m..");

		logger.info("Payload values = " + payload);

		String xmlData = null;
		try {
			xmlData = prepareXMLData(payload);
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		logger.info("xmlData = " + xmlData);
		try {

			MqttAsyncClient client = m2mDispatcher.getAsyncClient("TestVehicle1");
			IMqttToken conToken = m2mDispatcher.connectToDispatcherBroker(client);
			IMqttDeliveryToken token = null;
			if (conToken.isComplete()) {
				logger.info("       ASDP Dispatcher MQTT Client is connected now...proceed with publishing ");
				logger.info("    Going to Publish to Request Topic   ");
				token = m2mDispatcher.publishToTopic(client, xmlData, "TestVehicle1");
				if (token.isComplete()) {
					logger.info(" !!!!!        Publishing to CvcDispatcher is DONE         !!!!!");
				}
			}
			if (m2mDispatcher.alreadyConnected(client)) {
				logger.info(
						"ASDP Dispatcher MQTT Client has published its message...needs to dretrieveM2MDataisconnect from MQTT broker now");
				m2mDispatcher.disconnectClient(client);
			}
		} catch (MqttException e) {
			logger.error("    Error in CvcDispatcher...MqttException:        " + e.getMessage());

		}

	}

	/**
	 * 
	 * @param payload
	 * @return
	 * @throws ParserConfigurationException
	 */
	private String prepareXMLData(PayLoad payload) throws ParserConfigurationException {

		logger.info(" *********    INSIDE prepareXMLData      *******");

		Writer out = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			Document doc = builder.newDocument();

			Element element = doc.createElement("m2m:input");
			element.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			element.setAttribute("xmlns:m2m", "urn:com:ericsson:schema:xml:m2m:protocols:vnd.ericsson.m2m.input");

			Element metadata = doc.createElement("m2m:metadata");
			Element msgId = doc.createElement("m2m:messageId");
			// TODO
			msgId.setTextContent("123456" + "");

			metadata.appendChild(msgId);
			element.appendChild(metadata);

			Element fuelUsageDataElem = createDataElement(doc, "frontLeftTirePressure", "integer", payload);
			element.appendChild(fuelUsageDataElem);
			doc.appendChild(element);

			Transformer tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			out = new StringWriter();
			tf.transform(new DOMSource(doc), new StreamResult(out));
			logger.info("***********     XML Data        **********         === " + out.toString());
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out.toString();
	}

	/**
	 * 
	 * @param doc
	 * @param statusAttribute
	 * @param type
	 * @param payload
	 * @return
	 */
	private Element createDataElement(Document doc, String statusAttribute, String type, PayLoad payload) {
		Element data = null;
		try {
			data = doc.createElement("m2m:data");
			Element sourceId = doc.createElement("m2m:sourceIdentifiers");
			data.appendChild(sourceId);

			Element operator = doc.createElement("m2m:operator");
			operator.setTextContent("bell");
			sourceId.appendChild(operator);

			Element da = doc.createElement("m2m:domainApplication");
			da.setTextContent("dispatcherApp");
			sourceId.appendChild(da);

			Element gateway = doc.createElement("m2m:gateway");
			gateway.setTextContent("bell".concat("$").concat("TestVehicle1"));
			sourceId.appendChild(gateway);

			Element resourceSpec = doc.createElement("m2m:resourceSpec");
			resourceSpec.setTextContent(statusAttribute);
			sourceId.appendChild(resourceSpec);

			Element timestamp = doc.createElement("m2m:timestamp");
			String ts = getFormattedResponseTime(System.currentTimeMillis());
			timestamp.setTextContent(ts);

			data.appendChild(sourceId);
			data.appendChild(timestamp);

			Element value = doc.createElement("m2m:value");
			value.setTextContent(payload.getFrontLeftTirePressure());
			value.setAttribute("type", type);
			data.appendChild(value);

			Element meta = doc.createElement("m2m:metadata");
			Element metaType = doc.createElement("m2m:type");
			metaType.setTextContent("data");
			meta.appendChild(metaType);

			data.appendChild(meta);

		} catch (DOMException e) {
			System.err.println(" ERROR in createDataElement ");
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * 
	 * @param requestTime
	 * @return
	 */
	private String getFormattedResponseTime(long requestTime) {
		String responseTimeFormatted = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSS");
		responseTimeFormatted = dateFormat.format(requestTime).concat("Z");
		return responseTimeFormatted;
	}

}
