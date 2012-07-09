package org.platformlayer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.URI;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.codehaus.jettison.json.JSONException;
import org.openstack.utils.Utf8;
import org.platformlayer.crypto.AcceptAllHostnameVerifier;
import org.platformlayer.crypto.PublicKeyTrustManager;
import org.platformlayer.http.SimpleHttpRequest;
import org.platformlayer.http.SimpleHttpRequest.SimpleHttpResponse;
import org.platformlayer.xml.JaxbHelper;
import org.platformlayer.xml.JsonHelper;
import org.platformlayer.xml.UnmarshalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PlatformLayerHttpRequest implements Closeable {
	static final Logger log = LoggerFactory.getLogger(PlatformLayerHttpRequest.class);

	final PlatformLayerHttpTransport client;
	final SimpleHttpRequest httpRequest;
	SimpleHttpResponse response;

	PrintStream debug;

	public PlatformLayerHttpRequest(PlatformLayerHttpTransport client, String method, URI uri, List<String> trustKeys)
			throws PlatformLayerClientException {
		this.client = client;
		try {
			this.httpRequest = SimpleHttpRequest.build(method, uri);
		} catch (IOException e) {
			throw new PlatformLayerClientException("Error building http request " + method + " " + uri, e);
		}

		if (trustKeys != null) {
			TrustManager trustManager = new PublicKeyTrustManager(trustKeys);
			this.httpRequest.setTrustManager(trustManager);
			this.httpRequest.setHostnameVerifier(new AcceptAllHostnameVerifier());
		}
	}

	void populateHttpRequest(Format acceptFormat, Format contentFormat) throws PlatformLayerClientException {
		if (acceptFormat != null) {
			switch (acceptFormat) {
			case XML:
				httpRequest.setRequestHeader("Accept", "application/xml");
				break;
			case JSON:
				httpRequest.setRequestHeader("Accept", "application/json");
				break;
			case TEXT:
				httpRequest.setRequestHeader("Accept", "text/plain");
				break;

			default:
				throw new IllegalStateException();
			}
		}

		if (contentFormat != null) {
			switch (contentFormat) {
			case XML:
				httpRequest.setRequestHeader("Content-Type", "application/xml");
				break;
			case JSON:
				httpRequest.setRequestHeader("Content-Type", "application/json");
				break;
			case TEXT:
				httpRequest.setRequestHeader("Content-Type", "text/plain");
				break;

			default:
				throw new IllegalStateException();
			}
		}

		this.client.getAuthenticationToken().populateRequest(httpRequest);
	}

	SimpleHttpResponse getResponse() throws IOException {
		if (response == null) {
			response = httpRequest.doRequest();
		}
		return response;
	}

	InputStream getInputStream() throws IOException {
		return getResponse().getInputStream();
	}

	@Override
	public void close() throws IOException {
		if (response != null) {
			response.close();
			response = null;
		}
	}

	InputStream getErrorStream() throws IOException {
		return getResponse().getErrorStream();
	}

	OutputStream getOutputStream() throws IOException {
		return httpRequest.getOutputStream();
	}

	public <T> T doRequest(Class<T> retvalClass, Format acceptFormat, Object sendData, Format sendDataFormat)
			throws PlatformLayerClientException {
		try {
			populateHttpRequest(acceptFormat, sendDataFormat);

			if (debug != null) {
				debug.println("Request" + httpRequest);
			}

			if (sendData != null) {
				if (sendData instanceof String) {
					if (debug != null) {
						debug.println("Data: " + sendData);
					}

					String sendDataString = (String) sendData;
					OutputStreamWriter writer = Utf8.openWriter(getOutputStream());
					writer.write(sendDataString);
					writer.flush();
				} else {
					switch (sendDataFormat) {
					case XML:
						if (debug != null) {
							debug.println("Data: [XML Content]");
						}

						JaxbHelper jaxbHelper = JaxbHelper.get(sendData.getClass());
						jaxbHelper.marshal(sendData, false, getOutputStream());
						break;
					case JSON:
						if (debug != null) {
							debug.println("Data: [JSON Content]");
						}

						JsonHelper jsonHelper = JsonHelper.build(sendData.getClass());
						jsonHelper.marshal(sendData, false, getOutputStream());
						break;
					default:
						throw new IllegalStateException();
					}
				}
			}
		} catch (JAXBException e) {
			throw new PlatformLayerClientException("Error while building request", e);
		} catch (IOException e) {
			throw new PlatformLayerClientException("Error while building request", e);
		} catch (XMLStreamException e) {
			throw new PlatformLayerClientException("Error while building request", e);
		} catch (TransformerException e) {
			throw new PlatformLayerClientException("Error while building request", e);
		} catch (JSONException e) {
			throw new PlatformLayerClientException("Error while building request", e);
		}

		try {
			processHttpResponseCode(getResponse());

			if (retvalClass == null) {
				return null;
			} else if (String.class.equals(retvalClass)) {
				InputStream is = getInputStream();

				String text = null;
				if (is != null) {
					text = IoUtils.readAll(Utf8.openReader(is));
				}

				if (debug != null) {
					debug.println("Response: " + text);
				}

				return CastUtils.as(text, retvalClass);
			} else {
				if (debug != null) {
					debug.println("Response: XML/JSON content");
				}

				InputStream is = getInputStream();

				return JaxbHelper.deserializeXmlObject(is, retvalClass, true);
			}
		} catch (ConnectException e) {
			throw new PlatformLayerClientException("Error connecting to PlatformLayer service", e);
		} catch (UnmarshalException e) {
			throw new PlatformLayerClientException("Error while reading PlatformLayer response", e);
		} catch (IOException e) {
			throw new PlatformLayerClientException("Error communicating with PlatformLayer service", e);
		}
	}

	private void processHttpResponseCode(SimpleHttpResponse response) throws PlatformLayerClientException, IOException {
		// Send the HTTP request
		int httpResponseCode = response.getHttpResponseCode();

		if (debug != null) {
			debug.println("Response: " + response);
		}

		switch (httpResponseCode) {
		case 200: // OK
		case 201: // Created
		case 202: // Async accepted
		case 204: // No content
			break;
		case 203: // Cached
			String lastModified = getResponse().getResponseHeaderField("Last-Modified");
			// Map<String, List<String>> headerFields = httpConn.getHeaderFields();
			log.debug("Cached content at: " + httpRequest.getUrl() + " lastModified=" + lastModified);
			break;
		case 401:
			throw new PlatformLayerAuthenticationException("Not authorized (or authorization timed out)");
		case 404:
			throw new PlatformLayerClientNotFoundException("Not found", 404, httpRequest.getUrl());
		case 500:
			throw buildExceptionFromErrorStream(httpResponseCode);

		default:
			throw buildExceptionFromErrorStream(httpResponseCode);
		}
	}

	private PlatformLayerClientException buildExceptionFromErrorStream(int httpResponseCode) throws IOException {
		InputStream errorStream = null;
		String errorText = null;
		try {
			errorStream = getErrorStream();
			if (errorStream != null) {
				errorText = IoUtils.readAll(Utf8.openReader(errorStream));
			}
		} catch (IOException e) {
			log.warn("Could not read error response from request", e);
		} finally {
			IoUtils.safeClose(errorStream);
		}

		String faultReason = parseFaultReason(errorText);
		if (faultReason == null) {
			return new PlatformLayerClientException("Unexpected error from PlatformLayer.  ResponseCode="
					+ httpResponseCode + " Details=" + errorText, httpResponseCode);
		} else {
			return new PlatformLayerClientException(faultReason, httpResponseCode);
		}
	}

	private String parseFaultReason(String xml) {
		if (xml == null) {
			return null;
		}

		return xml;

		// try {
		// // The rather odd ByteArrayInputStream(xml.getBytes()) is so that we can print the response even if we have a
		// problem parsing it
		// ServiceFault cloudServersAPIFault = JaxbHelper.deserializeXmlObject(new ByteArrayInputStream(xml.getBytes()),
		// ServiceFault.class);
		// return cloudServersAPIFault;
		// } catch (OpenstackException e) {
		// log.warn("Could not parse error response: " + xml, e);
		// return null;
		// }
	}

}
