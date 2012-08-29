package org.platformlayer.metrics.client.codahale;

import org.apache.log4j.Logger;
import org.platformlayer.metrics.MetricMeter;

import com.yammer.metrics.core.Meter;

public class MetricMeterAdapter implements MetricMeter {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(MetricMeterAdapter.class);
	private final Meter inner;

	public MetricMeterAdapter(Meter inner) {
		this.inner = inner;
	}

	@Override
	public void record() {
		inner.mark();
	}
}
