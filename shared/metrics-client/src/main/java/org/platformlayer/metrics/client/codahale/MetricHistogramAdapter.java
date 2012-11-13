package org.platformlayer.metrics.client.codahale;

import org.platformlayer.metrics.MetricHistogram;

import com.yammer.metrics.core.Histogram;

public class MetricHistogramAdapter implements MetricHistogram {
	private final Histogram inner;

	public MetricHistogramAdapter(Histogram inner) {
		this.inner = inner;
	}

	@Override
	public void record(long value) {
		inner.update(value);
	}
}
