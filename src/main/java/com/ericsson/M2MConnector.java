package com.ericsson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;




public class M2MConnector {

	private final static Logger logger = Logger.getLogger(M2MConnector.class.getName());		

	private DigestAuthenticationClientWrapper httpclientWrapper;
	
	/**
	 * @return the httpclientWrapper
	 */
	public DigestAuthenticationClientWrapper getHttpclientWrapper() {
		return httpclientWrapper;
	}


	/**
	 * @param httpclientWrapper the httpclientWrapper to set
	 */
	public void setHttpclientWrapper(DigestAuthenticationClientWrapper httpclientWrapper) {
		this.httpclientWrapper = httpclientWrapper;
	}

	private String result;
	
	private Integer responseCode;
	
	private InputStream is;
	
	public InputStream getIs() {
		return is;
	}


	public void setIs(InputStream is) {
		this.is = is;
	}


	public Integer getResponseCode() {
		return responseCode;
	}


	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
	}


	public String getResult() {
		return result;
	}


	public void setResult(String result) {
		this.result = result;
	}


	/**
	 * 
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 * @param URL
	 * @param requestType
	 * @param jsonData
	 * @throws KeyManagementException
	 * @throws ClientProtocolException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws Exception 
	 * @throws ParseException 
	 */
	public M2MConnector(String host, Integer port, String userName, String password) throws Exception {
        //String result = null; 
      	logger.fine("=========== host:= "+host+" port:= "+port );	
        DigestAuthenticationClientWrapper httpclientWrapper = new DigestAuthenticationClientWrapper(host, port, "http", userName, password);
        //httpclientWrapper.setRealm("M2M-DM");
        
        
        
    	String proxyEnabled = "false";
    	/*if ("true".equalsIgnoreCase(proxyEnabled)) {
    		logger.debug("========== Setting Proxy ============");
            httpclientWrapper.setProxy("cngzip01.mgmt.ericsson.se", 8080);
    	}*/
    	setHttpclientWrapper(httpclientWrapper);

	}
	
	/**
	 * 
	 * @param URL
	 * @param requestType
	 * @param builder
	 */
	public void connect(final String URL, String requestType, URIBuilder builder) {
		
        
        HttpGet requestGet = null;
        CloseableHttpResponse response = null;
        logger.info("------------ Encoded URL is ============= "+URL);
        
        try {
			if ("Get".equalsIgnoreCase(requestType)) {
				logger.info("======= It's a GET Request ==========");
				requestGet = new HttpGet(builder.build());
				requestGet.addHeader("Accept", "application/vnd.ericsson.m2m.output+xml;version=1.2");
				
				//logger.debug("===================                 In M2MConnector executing GET Request:::         " + requestGet.toString());
				logger.fine(" ===================                The GET request :::       "+requestGet.getRequestLine().toString());
				response = getHttpclientWrapper().execute(requestGet);
			}
			
			
			responseCode = response.getStatusLine().getStatusCode();
			setResponseCode(response.getStatusLine().getStatusCode());
			logger.info("========== The responseCode for the Current Operation is ============== "+responseCode);
			System.out.println("========== The responseCode for the Current Operation is ============== "+responseCode);
			
		        HttpEntity entity = response.getEntity();

		        if (null != entity) {
		    		is = entity.getContent();
		    		result = readLine(is);
		    		//logger.info(" The result is  ::: "+result);
		    		//System.out.println( " The result is  ::: "+result);
		    		setIs(is);
		    		setResult(result);
		        }
			if (responseCode != 200 && responseCode != 201 & responseCode != 204) {
				//logger.error("========== Error in M2M COnnector ============"+response.getStatusLine().getReasonPhrase());
				System.err.println("========== Error in M2M COnnector ============"+response.getStatusLine().getReasonPhrase());
			}			
			
		} catch (Exception e) {
			//logger.error("            Error !!!!!!!!!!!!!!");
			//System.err.println("            Error !!!!!!!!!!!!!!");
			e.printStackTrace();
		}  finally 
        { 
        	try {
        		getHttpclientWrapper().getHttpClient().close();
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
        }
		
	}

	/**
	 * Read line.
	 * 
	 * @param inputStream
	 *            the input stream
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private String readLine(InputStream inputStream) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader bufferReader = null;
		try {
			bufferReader = new BufferedReader(new InputStreamReader(inputStream));
			String readline;
			while ((readline = bufferReader.readLine()) != null) {
				sb.append(readline);
			}
		} finally {
			if (bufferReader != null) {
				bufferReader.close();
			}
		}
		return sb.toString();

	}	

	/**
	 * 
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public static URI encodeURL(String url) throws MalformedURLException, URISyntaxException  
	{
	    URI uriFormatted = null; 

	    URL urlLink = new URL(url);
	    uriFormatted = new URI("https", urlLink.getHost(), urlLink.getPath(), urlLink.getQuery(), urlLink.getRef());

	    return uriFormatted;
	}	
	
	
	}


