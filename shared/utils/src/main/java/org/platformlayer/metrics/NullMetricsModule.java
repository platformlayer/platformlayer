package org.platformlayer.metrics;

import com.google.inject.AbstractModule;

public class NullMetricsModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(MetricsSystem.class).to(NullMetricsSystem.class);
	}

}