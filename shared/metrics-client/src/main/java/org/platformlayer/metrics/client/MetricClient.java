package org.platformlayer.metrics.client;

import java.io.Closeable;

import org.platformlayer.metrics.MetricTreeObject;
import org.platformlayer.metrics.model.MetricQuery;
import org.platformlayer.rest.RestClientException;

import com.google.inject.ProvidedBy;

@ProvidedBy(MetricClientImpl.Provider.class)
public interface MetricClient extends Closeable {
	public boolean sendMetrics(MetricTreeObject tree);

	public MetricServiceData getMetrics(MetricQuery query) throws RestClientException;
}
