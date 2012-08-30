package org.platformlayer.metrics;

import com.google.inject.AbstractModule;

public class NullMetricsModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(MetricRegistry.class).to(NullMetricRegistry.class).asEagerSingleton();
		bind(MetricReporter.class).to(NullMetricReporter.class).asEagerSingleton();
	}

}