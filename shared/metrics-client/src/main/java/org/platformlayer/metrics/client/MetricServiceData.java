package org.platformlayer.metrics.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.platformlayer.metrics.model.MetricDataSource;

import com.fathomdb.io.IoUtils;

public class MetricServiceData implements MetricDataSource {
	final HttpGet request;
	final HttpResponse response;

	public MetricServiceData(HttpGet request, HttpResponse response) {
		this.request = request;
		this.response = response;
	}

	public String readAll() throws IOException {
		HttpEntity responseEntity = response.getEntity();

		InputStream contentStream = responseEntity.getContent();

		try {
			return IoUtils.readAll(contentStream);
		} finally {
			IoUtils.safeClose(contentStream);
		}
	}

	@Override
	public void close() throws IOException {
		if (response != null) {
			EntityUtils.consume(response.getEntity());
		}
	}

	@Override
	public void serializeAsJson(OutputStream out) throws IOException {
		HttpEntity responseEntity = response.getEntity();

		InputStream contentStream = responseEntity.getContent();

		try {
			IoUtils.copyStream(contentStream, out);
		} finally {
			IoUtils.safeClose(contentStream);
		}
	}

	@Override
	public void serializeAsXml(OutputStream out) {
		throw new UnsupportedOperationException();
	}
}