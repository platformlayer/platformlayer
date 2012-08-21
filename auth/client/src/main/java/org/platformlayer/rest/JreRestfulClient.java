package org.platformlayer.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.KeyManager;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.openstack.utils.Utf8;
import org.platformlayer.CastUtils;
import org.platformlayer.IoUtils;
import org.platformlayer.http.HttpRequest;
import org.platformlayer.http.HttpResponse;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.http.SslConfiguration;
import org.platformlayer.xml.JaxbHelper;
import org.platformlayer.xml.UnmarshalException;

public class JreRestfulClient implements RestfulClient {
	static final Logger log = Logger.getLogger(JreRestfulClient.class);

	final String baseUrl;
	final HttpStrategy httpStrategy;

	PrintStream debug;

	final SslConfiguration sslConfiguration;

	public JreRestfulClient(HttpStrategy httpStrategy, String baseUrl, SslConfiguration sslConfiguration) {
		this.baseUrl = baseUrl;
		this.httpStrategy = httpStrategy;
		this.sslConfiguration = sslConfiguration;
	}

	public JreRestfulClient(HttpStrategy httpStrategy, String baseUrl) {
		this(httpStrategy, baseUrl, null);
	}

	public class JreRequest<T> implements RestfulRequest<T> {
		String method;
		String relativeUri;
		Object postObject;
		Class<T> responseClass;

		SslConfiguration sslConfiguration = JreRestfulClient.this.sslConfiguration;

		JreRequest(String method, String relativeUri, Object postObject, Class<T> responseClass) {
			super();
			this.method = method;
			this.relativeUri = relativeUri;
			this.postObject = postObject;
			this.responseClass = responseClass;
		}

		@Override
		public T execute() throws RestClientException {
			try {
				URI uri = new URI(baseUrl + relativeUri);

				if (debug != null) {
					debug.println("HTTP Request: " + method + " " + uri);
				} else {
					log.debug("HTTP Request: " + method + " " + uri);
				}

				HttpRequest httpRequest = httpStrategy.buildConfiguration(sslConfiguration).buildRequest(method, uri);
				httpRequest.setRequestHeader("Accept", "application/xml");

				if (debug != null) {
					debug.println(httpRequest.toString());
				}

				if (postObject != null) {
					httpRequest.setRequestHeader("Content-Type", "application/xml");
					String xml = serializeXml(postObject);
					httpRequest.setRequestContent(Utf8.getBytes(xml));

					if (debug != null) {
						debug.println(xml);
					}
				}

				HttpResponse response = httpRequest.doRequest();

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

		@Override
		public SslConfiguration getSslConfiguration() {
			return sslConfiguration;
		}

		@Override
		public void setKeyManager(KeyManager keyManager) {
			this.sslConfiguration = sslConfiguration.copyWithNewKeyManager(keyManager);
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

	@Override
	public void setDebug(PrintStream debug) {
		this.debug = debug;
	}

	@Override
	public <T> RestfulRequest<T> buildRequest(String method, String relativeUri, Object postObject,
			Class<T> responseClass) {
		return new JreRequest<T>(method, relativeUri, postObject, responseClass);
	}

	@Override
	public URI getBaseUri() {
		return URI.create(this.baseUrl);
	}

}
