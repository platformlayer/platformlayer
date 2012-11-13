package org.platformlayer.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullMetricReporter implements MetricReporter {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(NullMetricReporter.class);

	@Override
	public void start() {
	}
}
