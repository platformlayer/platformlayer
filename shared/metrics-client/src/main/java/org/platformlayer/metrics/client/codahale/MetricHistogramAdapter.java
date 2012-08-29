package org.platformlayer.metrics.client.codahale;

import org.apache.log4j.Logger;
import org.platformlayer.metrics.MetricHistogram;

import com.yammer.metrics.core.Histogram;

public class MetricHistogramAdapter implements MetricHistogram {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(MetricHistogramAdapter.class);
	private final Histogram inner;

	public MetricHistogramAdapter(Histogram inner) {
		this.inner = inner;
	}

	@Override
	public void record(long value) {
		inner.update(value);
	}
}
