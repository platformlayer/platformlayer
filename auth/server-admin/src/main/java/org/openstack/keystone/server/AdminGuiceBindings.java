package org.openstack.keystone.server;

import org.platformlayer.metrics.MetricsSystem;
import org.platformlayer.metrics.client.CodahaleMetricsSystem;
import org.platformlayer.web.InstrumentedJettyWebServerBuilder;
import org.platformlayer.web.WebServerBuilder;

import com.google.inject.AbstractModule;

public class AdminGuiceBindings extends AbstractModule {
	@Override
	protected void configure() {
		bind(MetricsSystem.class).to(CodahaleMetricsSystem.class);

		bind(WebServerBuilder.class).to(InstrumentedJettyWebServerBuilder.class);
	}
}
