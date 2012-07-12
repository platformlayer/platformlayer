package org.platformlayer.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;

public class SimpleHttpRequest {
	static final Logger log = Logger.getLogger(SimpleHttpRequest.class);

	final HttpURLConnection httpConn;
	final URL url;
	final String method;

	public static SimpleHttpRequest build(String method, URI uri) throws IOException {
		return new SimpleHttpRequest(method, uri);
	}

	protected SimpleHttpRequest(String method, URI uri) throws IOException {
		this.method = method;

		this.url = uri.toURL();

		httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setDoInput(true);
		httpConn.setDoOutput(true);
		httpConn.setUseCaches(false);
		httpConn.setDefaultUseCaches(false);
		httpConn.setAllowUserInteraction(false);
		httpConn.setRequestMethod(method);
	}

	public void setRequestHeader(String key, String value) {
		httpConn.setRequestProperty(key, value);
	}

	SimpleHttpResponse response;

	private KeyManager keyManager;
	private TrustManager trustManager;

	public SimpleHttpResponse doRequest() throws IOException {
		if (response == null) {
			response = doRequest0();
		}
		return response;
	}

	protected SimpleHttpResponse doRequest0() throws IOException {
		return new SimpleHttpResponse();
	}

	SSLSocketFactory buildSslSocketFactory() throws NoSuchAlgorithmException, UnrecoverableKeyException,
			KeyStoreException, KeyManagementException {
		SSLContext context = SSLContext.getInstance("TLS");

		// context.getClientSessionContext().setEndpointIdentificationAlgorithm();
		// KeyStore keyStore = KeyStore.getInstance("PKCS12");
		// keyStore.load(new FileInputStream(privateKeyFile), privateKeyPassword.toCharArray());

		// InputStream keyInput = new FileInputStream(pKeyFile);
		// keyStore.load(keyInput, pKeyPassword.toCharArray());
		// keyInput.close();

		// We need to pass a keystore password, though I don't think it's used
		// String keystorePassword = "password";
		KeyManager[] keyManagers = null;
		if (keyManager != null) {
			keyManagers = new KeyManager[] { keyManager };
		}

		TrustManager[] trustManagers = null;

		if (trustManager != null) {
			trustManagers = new TrustManager[] { trustManager };
		}

		context.init(keyManagers, trustManagers, new SecureRandom());

		return context.getSocketFactory();

	}

	public class SimpleHttpResponse {
		private final int responseCode;

		public SimpleHttpResponse() throws IOException {
			this(httpConn.getResponseCode());
		}

		public SimpleHttpResponse(int responseCode) {
			this.responseCode = responseCode;
		}

		public int getHttpResponseCode() throws IOException {
			return responseCode;
		}

		public Map<String, List<String>> getHeaderFields() {
			return httpConn.getHeaderFields();
		}

		public InputStream getErrorStream() {
			return httpConn.getErrorStream();
		}

		public String getResponseMessage() throws IOException {
			return httpConn.getResponseMessage();
		}

		InputStream is = null;

		public InputStream getInputStream() throws IOException {
			if (is == null) {
				is = httpConn.getInputStream();
			}
			return is;
		}

		public void close() throws IOException {
			if (is != null) {
				is.close();
				is = null;
			}
		}

		public String getResponseHeaderField(String name) {
			return httpConn.getHeaderField(name);
		}

		public Map<String, String> getHeadersRemoveDuplicates() {
			Map<String, String> allHeaders = Maps.newHashMap();
			Map<String, List<String>> headerFields = httpConn.getHeaderFields();
			for (Entry<String, List<String>> headerEntry : headerFields.entrySet()) {
				// Collapse multiple values (we don't expect duplicates)
				for (String headerValue : headerEntry.getValue()) {
					allHeaders.put(headerEntry.getKey(), headerValue);
				}
			}
			return allHeaders;
		}

		@Override
		public String toString() {
			try {
				StringBuilder sb = new StringBuilder();
				sb.append(httpConn.getResponseCode() + " " + httpConn.getResponseMessage());
				return sb.toString();
			} catch (IOException e) {
				log.warn("Error in toString", e);
				return "Exception while calling toString";
			}

		}

	}

	public OutputStream getOutputStream() throws IOException {
		return httpConn.getOutputStream();
	}

	public URL getUrl() {
		return url;
	}

	public String getMethod() {
		return method;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getMethod() + " " + getUrl() + "\n");
		for (Entry<String, List<String>> entry : httpConn.getRequestProperties().entrySet()) {
			sb.append(entry.getKey() + ": " + entry.getValue() + "\n");
		}
		sb.append("\n");
		return sb.toString();
	}

	public void setKeyManager(KeyManager keyManager) {
		this.keyManager = keyManager;

		updateSslParameters();
	}

	private void updateSslParameters() {
		HttpsURLConnection https = (HttpsURLConnection) httpConn;
		try {
			https.setSSLSocketFactory(buildSslSocketFactory());
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("Error loading certificate", e);
		}
	}

	public TrustManager getTrustManager() {
		return trustManager;
	}

	public void setTrustManager(TrustManager trustManager) {
		this.trustManager = trustManager;

		updateSslParameters();
	}

	public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
		HttpsURLConnection https = (HttpsURLConnection) httpConn;
		https.setHostnameVerifier(hostnameVerifier);
	}

}
