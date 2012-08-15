package org.platformlayer.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.openstack.utils.Utf8;
import org.platformlayer.CastUtils;
import org.platformlayer.IoUtils;
import org.platformlayer.http.SimpleHttpRequest;
import org.platformlayer.http.SimpleHttpRequest.SimpleHttpResponse;
import org.platformlayer.xml.JaxbHelper;
import org.platformlayer.xml.UnmarshalException;

public class RestfulClient {
	static final Logger log = Logger.getLogger(RestfulClient.class);

	final String baseUrl;

	final KeyManager keyManager;
	final TrustManager trustManager;
	final HostnameVerifier hostnameVerifier;

	PrintStream debug;

	public RestfulClient(String baseUrl, KeyManager keyManager, TrustManager trustManager,
			HostnameVerifier hostnameVerifier) {
		this.baseUrl = baseUrl;
		this.keyManager = keyManager;
		this.trustManager = trustManager;
		this.hostnameVerifier = hostnameVerifier;
	}

	public RestfulClient(String baseUrl) {
		this(baseUrl, null, null, null);
	}

	public class Request<T> {
		String method;
		String relativeUri;
		Object postObject;
		Class<T> responseClass;

		public Request(String method, String relativeUri, Object postObject, Class<T> responseClass) {
			super();
			this.method = method;
			this.relativeUri = relativeUri;
			this.postObject = postObject;
			this.responseClass = responseClass;
		}

		public T execute() throws RestClientException {
			try {
				URI uri = new URI(baseUrl + relativeUri);

				if (debug != null) {
					debug.println("HTTP Request: " + method + " " + uri);
				} else {
					log.debug("HTTP Request: " + method + " " + uri);
				}

				SimpleHttpRequest httpRequest = SimpleHttpRequest.build(method, uri);
				httpRequest.setRequestHeader("Accept", "application/xml");

				addHeaders(httpRequest);

				if (debug != null) {
					debug.println(httpRequest.toString());
				}

				if (postObject != null) {
					httpRequest.setRequestHeader("Content-Type", "application/xml");
					String xml = serializeXml(postObject);
					httpRequest.getOutputStream().write(Utf8.getBytes(xml));

					if (debug != null) {
						debug.println(xml);
					}
				}

				SimpleHttpResponse response = httpRequest.doRequest();

				int responseCode = response.getHttpResponseCode();
				switch (responseCode) {
				case 401:
					throw new RestClientException("Authentication failure (401)");

				case 200:
				case 203: {
					if (responseClass.equals(String.class)) {
						return CastUtils.as(IoUtils.readAll(response.getInputStream()), responseClass);
					} else {
						return deserializeXml(response.getInputStream(), responseClass);
					}
				}

				default:
					throw new RestClientException("Unexpected result code: " + responseCode, null, responseCode);
				}
			} catch (IOException e) {
				throw new RestClientException("Error communicating with service", e);
			} catch (URISyntaxException e) {
				throw new RestClientException("Error building URI", e);
			}
		}

		protected void addHeaders(SimpleHttpRequest httpRequest) {
			RestfulClient.this.addHeaders(httpRequest);
		}
	}

	protected <T> T doSimpleRequest(String method, String relativeUri, Object postObject, Class<T> responseClass)
			throws RestClientException {
		Request<T> request = new Request<T>(method, relativeUri, postObject, responseClass);
		return request.execute();
	}

	protected void addHeaders(SimpleHttpRequest httpRequest) {
		if (trustManager != null) {
			httpRequest.setTrustManager(trustManager);
		}

		if (keyManager != null) {
			httpRequest.setKeyManager(keyManager);
		}

		if (hostnameVerifier != null) {
			httpRequest.setHostnameVerifier(hostnameVerifier);
		}
	}

	<T> T deserializeXml(InputStream is, Class<T> clazz) throws RestClientException {
		try {
			return JaxbHelper.deserializeXmlObject(is, clazz, true);
		} catch (UnmarshalException e) {
			throw new RestClientException("Error reading authentication response data", e);
		}
	}

	String serializeXml(Object object) throws RestClientException {
		try {
			boolean formatted = false;
			return JaxbHelper.toXml(object, formatted);
		} catch (JAXBException e) {
			throw new RestClientException("Error serializing data", e);
		}
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setDebug(PrintStream debug) {
		this.debug = debug;
	}

	protected String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 not supported", e);
		}
	}

}
