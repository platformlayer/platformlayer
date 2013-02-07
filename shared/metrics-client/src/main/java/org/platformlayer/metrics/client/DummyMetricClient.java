package org.platformlayer.metrics.client;

import java.io.IOException;

import org.platformlayer.metrics.MetricTreeObject;
import org.platformlayer.metrics.model.MetricQuery;
import org.platformlayer.rest.RestClientException;

public class DummyMetricClient implements MetricClient {

	@Override
	public void close() throws IOException {

	}

	@Override
	public boolean sendMetrics(MetricTreeObject tree) {
		return true;
	}

	@Override
	public MetricServiceData getMetrics(MetricQuery query) throws RestClientException {
		throw new UnsupportedOperationException();
	}

}
