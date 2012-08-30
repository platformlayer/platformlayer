package org.platformlayer.metrics;

import org.apache.log4j.Logger;

public class NullMetricReporter implements MetricReporter {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(NullMetricReporter.class);

	@Override
	public void start() {
	}
}
