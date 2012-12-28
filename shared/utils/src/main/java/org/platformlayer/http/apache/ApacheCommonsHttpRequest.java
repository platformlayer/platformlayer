package org.platformlayer.http.apache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.util.EntityUtils;
import org.platformlayer.http.HttpRequest;
import org.platformlayer.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.io.ByteSource;
import com.fathomdb.io.IoUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ApacheCommonsHttpRequest implements HttpRequest {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ApacheCommonsHttpRequest.class);

	final HttpClient httpClient;
	final String method;
	final URI uri;

	final HttpRequestBase request;

	ApacheCommonsHttpRequest(HttpClient httpClient, String method, URI uri) {
		this.httpClient = httpClient;
		this.method = method;
		this.uri = uri;

		if (method.equals(HttpGet.METHOD_NAME)) {
			request = new HttpGet(uri);
		} else if (method.equals(HttpPost.METHOD_NAME)) {
			request = new HttpPost(uri);
		} else if (method.equals(HttpPut.METHOD_NAME)) {
			request = new HttpPut(uri);
		} else {
			throw new IllegalArgumentException("Unhandled method: " + method);
		}
	}

	@Override
	public void setRequestHeader(String name, String value) {
		request.setHeader(name, value);
	}

	@Override
	public HttpResponse doRequest() throws IOException {
		org.apache.http.HttpResponse apacheResponse = httpClient.execute(request);
		return new ApacheCommonsHttpResponse(apacheResponse);
	}

	@Override
	public URI getUrl() {
		return uri;
	}

	public class ApacheCommonsHttpResponse implements HttpResponse {
		private final org.apache.http.HttpResponse response;

		public ApacheCommonsHttpResponse(org.apache.http.HttpResponse response) {
			this.response = response;
		}

		@Override
		public void close() throws IOException {
			HttpEntity httpEntity = response.getEntity();
			EntityUtils.consume(httpEntity);
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return response.getEntity().getContent();
		}

		@Override
		public int getHttpResponseCode() throws IOException {
			return response.getStatusLine().getStatusCode();
		}

		@Override
		public InputStream getErrorStream() throws IOException {
			return getInputStream();
		}

		@Override
		public String getResponseHeaderField(String key) {
			Header[] headers = response.getHeaders(key);
			if (headers == null || headers.length == 0) {
				return null;
			}

			return headers[0].getValue();
		}

		@Override
		public Map<String, List<String>> getHeaderFields() {
			Map<String, List<String>> ret = Maps.newHashMap();
			for (Header header : response.getAllHeaders()) {
				List<String> values = ret.get(header.getName());
				if (values == null) {
					values = Lists.newArrayList();
					ret.put(header.getName(), values);
				}

				values.add(header.getValue());
			}
			return ret;
		}
	}

	@Override
	public void setRequestContent(final ByteSource data) throws IOException {
		HttpEntityEnclosingRequestBase post = (HttpEntityEnclosingRequestBase) request;
		post.setEntity(new AbstractHttpEntity() {

			@Override
			public boolean isRepeatable() {
				return true;
			}

			@Override
			public long getContentLength() {
				try {
					return data.getContentLength();
				} catch (IOException e) {
					throw new IllegalStateException("Error getting content length", e);
				}
			}

			@Override
			public InputStream getContent() throws IOException, IllegalStateException {
				return data.open();
			}

			@Override
			public void writeTo(OutputStream os) throws IOException {
				InputStream is = data.open();
				try {
					IoUtils.copyToOutputStream(is, os);
				} finally {
					is.close();
				}
			}

			@Override
			public boolean isStreaming() {
				return false;
			}

		});
	}

}
