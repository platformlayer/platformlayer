package org.platformlayer.rest;

import javax.xml.bind.JAXBException;

import org.platformlayer.ByteSource;
import org.platformlayer.xml.JaxbHelper;

public class HttpPayload {
	final String contentType;
	final ByteSource content;

	public HttpPayload(String contentType, ByteSource content) {
		this.contentType = contentType;
		this.content = content;
	}

	public static HttpPayload asXml(Object object) throws RestClientException {
		try {
			boolean formatted = false;
			String content = JaxbHelper.toXml(object, formatted);

			return fromStringUtf8("application/xml", content);
		} catch (JAXBException e) {
			throw new RestClientException("Error serializing data", e);
		}
	}

	private static HttpPayload fromStringUtf8(String contentType, String content) {
		return new HttpPayload(contentType, new Utf8StringByteSource(content));
	}

	public String getContentType() {
		return contentType;
	}

	public ByteSource getContent() {
		return content;
	}

	// String data = null;
	//
	// if (postObject instanceof JSONObject) {
	// httpRequest.setRequestHeader("Content-Type", "application/json");
	// data = ((JSONObject) postObject).toString();
	// } else if (postObject instanceof byte[]) {
	// httpRequest.setRequestContent(new ArrayByteSource((byte[]) postObject));
	// } else if (postObject instanceof ByteSource) {
	// httpRequest.setRequestContent((ByteSource) postObject);
	// } else if (postObject instanceof File) {
	// httpRequest.setRequestContent(new FileByteSource((File) postObject));
	// } else {
	// httpRequest.setRequestHeader("Content-Type", "application/xml");
	// data = serializeXml(postObject);
	// }
	//
	// if (data != null) {
	// httpRequest.setRequestContent(new Utf8StringByteSource(data));
	// if (debug != null) {
	// debug.println(data);
	// }
	// }

}
