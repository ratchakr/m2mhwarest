package com.ericsson;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;



/**
 * The Class DigestAuthenticationClientWrapper.
 */
public class DigestAuthenticationClientWrapper {

	/** The Constant DEFAULT_SCHEME. */
	private static final String DEFAULT_SCHEME = "http";

	private final static Logger logger = Logger.getLogger(DigestAuthenticationClientWrapper.class.getName());	

	/** The http client. */
	private CloseableHttpClient httpClient;

	/** The sslsf. */
	private LayeredConnectionSocketFactory sslsf = null;

	/** The target. */
	private HttpHost target;

	/** The creds provider. */
	private CredentialsProvider credsProvider;

	/** The proxy. */
	private HttpHost proxy;

	/** The realm. */
	private String realm;

	/** The nonce. */
	private String nonce;

	/**
	 * Instantiates a new digest authentication client wrapper.
	 */
	public DigestAuthenticationClientWrapper() {
		logger.fine("Dflt");
	}

	/**
	 * Instantiates a new digest authentication client wrapper.
	 * 
	 * @param hostname
	 *            the hostname
	 * @param port
	 *            the port
	 * @param scheme
	 *            the scheme
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 */
	public DigestAuthenticationClientWrapper(String hostname, int port,
			String scheme, String username, String password) {
		//logger.trace("==== DigestAuthenticationHttpClientWrapper ENTRY ========");
		target = new HttpHost(hostname, port, scheme);
		credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
				new AuthScope(target.getHostName(), target.getPort()),
				new UsernamePasswordCredentials(username, password));
		logger.fine("==== DigestAuthenticationHttpClientWrapper EXIT ========");
	}

	/**
	 * Gets the http client.
	 * 
	 * @return the http client
	 * @throws KeyManagementException
	 *             the key management exception
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 */
	public CloseableHttpClient getHttpClient() throws KeyManagementException,
	NoSuchAlgorithmException {
		if (httpClient == null) {
			buildHttpClient();
		}

		return httpClient;
	}

	/**
	 * Builds the http client.
	 * 
	 * @throws KeyManagementException
	 *             the key management exception
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 */
	private void buildHttpClient() throws KeyManagementException,
	NoSuchAlgorithmException {
		if (this.sslsf == null) {
			this.sslsf = getDefaultSSLSocketFactory();
		}

		HttpClientBuilder httpClientBuilder = HttpClients.custom()
				.setDefaultCredentialsProvider(credsProvider)
				.setSSLSocketFactory(sslsf);

		if (proxy != null) {
			httpClientBuilder.setProxy(proxy);
		}

		httpClient = httpClientBuilder.build();
	}

	/**
	 * Sets the SSL socket factory.
	 * 
	 * @param sslSocketFactory
	 *            the new SSL socket factory
	 */
	public void setSSLSocketFactory(
			LayeredConnectionSocketFactory sslSocketFactory) {
		this.sslsf = sslSocketFactory;
	}

	/**
	 * Gets the default ssl socket factory.
	 * 
	 * @return the default ssl socket factory
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws KeyManagementException
	 *             the key management exception
	 */
	private LayeredConnectionSocketFactory getDefaultSSLSocketFactory()
			throws NoSuchAlgorithmException, KeyManagementException {
		TrustManager[] trustManagers = new TrustManager[1];
		trustManagers[0] = new DefaultTrustManager();

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(new KeyManager[0], trustManagers, new SecureRandom());
		SSLContext.setDefault(sslContext);

		sslContext.init(null, trustManagers, null);
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				sslContext,
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		return sslsf;

	}

	/**
	 * Execute.
	 * 
	 * @param request
	 *            the request
	 * @return the closeable http response
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ClientProtocolException
	 *             the client protocol exception
	 * @throws KeyManagementException
	 *             the key management exception
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 */
	public CloseableHttpResponse execute(HttpUriRequest request)
			throws IOException, ClientProtocolException,
			KeyManagementException, NoSuchAlgorithmException {
		CloseableHttpClient httpClient = getHttpClient();
		logger.fine("========== Request Line =============="
				+ request.getRequestLine());
		AuthCache authCache = new BasicAuthCache();
		DigestScheme digestAuth = new DigestScheme();
		if (realm != null) {
			digestAuth.overrideParamter("realm", realm);
			System.out.println(" THE NONCE VALUE === "+nonce);
			digestAuth.overrideParamter("nonce", nonce);
		}

		authCache.put(target, digestAuth);

		HttpClientContext localContext = HttpClientContext.create();
		localContext.setAuthCache(authCache);

		CloseableHttpResponse response = httpClient.execute(target, request,
				localContext);

		return response;
	}

	/**
	 * Sets the realm.
	 * 
	 * @param realm
	 *            the new realm
	 */
	public void setRealm(String realm) {
		this.realm = realm;
		SecureRandom sr = new SecureRandom();
		setNonce(sr.nextInt(32) + "");
	}

	/**
	 * Sets the nonce.
	 * 
	 * @param nonce
	 *            the new nonce
	 */
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	/**
	 * Sets the proxy.
	 * 
	 * @param proxy
	 *            the new proxy
	 */
	public void setProxy(HttpHost proxy) {
		this.proxy = proxy;
	}

	/**
	 * Sets the proxy.
	 * 
	 * @param hostname
	 *            the hostname
	 * @param port
	 *            the port
	 * @param scheme
	 *            the scheme
	 */
	public void setProxy(String hostname, int port, String scheme) {
		this.proxy = new HttpHost(hostname, port, scheme);
	}

	/**
	 * Sets the proxy.
	 * 
	 * @param hostname
	 *            the hostname
	 * @param port
	 *            the port
	 */
	public void setProxy(String hostname, int port) {
		setProxy(hostname, port, DEFAULT_SCHEME);
	}

}
