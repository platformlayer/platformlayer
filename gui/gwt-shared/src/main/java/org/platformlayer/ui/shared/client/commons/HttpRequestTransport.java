package org.platformlayer.ui.shared.client.commons;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gwt.http.client.RequestBuilder;
import com.google.web.bindery.requestfactory.gwt.client.DefaultRequestTransport;

public class HttpRequestTransport extends DefaultRequestTransport {
	final Map<String, String> headers = Maps.newHashMap();

	@Override
	protected void configureRequestBuilder(RequestBuilder builder) {
		super.configureRequestBuilder(builder);

		for (Map.Entry<String, String> header : headers.entrySet()) {
			builder.setHeader(header.getKey(), header.getValue());
		}
	}

	public Map<String, String> getHeaders() {
		return headers;
	}
}
