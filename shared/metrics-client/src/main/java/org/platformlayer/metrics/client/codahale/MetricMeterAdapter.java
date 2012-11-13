package org.platformlayer.metrics.client.codahale;

import org.platformlayer.metrics.MetricMeter;

import com.yammer.metrics.core.Meter;

public class MetricMeterAdapter implements MetricMeter {
	private final Meter inner;

	public MetricMeterAdapter(Meter inner) {
		this.inner = inner;
	}

	@Override
	public void record() {
		inner.mark();
	}
}
