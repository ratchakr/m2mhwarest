package com.ericsson;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.logging.Logger;

import org.apache.http.client.utils.URIBuilder;

public class M2MDataRetrieverImpl {


	private final static Logger logger = Logger.getLogger(M2MDataRetrieverImpl.class.getName());		
	
	
	public String retrieveDataFromDataOutputInterface(String applicationId, String appPasswor,
			String enterpriseCustomerId, String messageId) throws Exception {
		
		logger.info(" =============     retrieveDataFromDataOutputInterface  ENTRY    ================  ");

		String host = "10.111.133.90";
		
		Integer port = 8181;
		
		logger.info(" ========      host := "+ host + "and  port := "+ port);
		// Change this if required
		String daName = "ASDPTCUDispatcher";
		String daPassword = "Password1";
		
		M2MConnector connector = null;
		try {
			connector = new M2MConnector(host, port, daName, daPassword);
			String URL = "http://" + host + ":" + port  +  "/m2m/data";
			
			URIBuilder builder = new URIBuilder(URL).addParameter("messageId", messageId);
			
			connector.connect(URL, "Get", builder);
			
			logger.info(" ========  GET Method executed successfully        ============= ");
			
			
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String retData = connector.getResult();
		//logger.info(" ========       respopnse from GET        ============= "+retData);
		logger.info(" =============     retrieveDataFromDataOutputInterface  EXIT    ================  ");
		return retData;
	}

}
