package org.platformlayer.metrics.client.codahale;

import org.platformlayer.metrics.InstrumentedListener;
import org.platformlayer.metrics.JerseyMetricsHook;
import org.platformlayer.metrics.MetricsSystem;
import org.platformlayer.web.InstrumentedJettyWebServerBuilder;
import org.platformlayer.web.WebServerBuilder;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.matcher.Matchers;
import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.JmxReporter;

/**
 * A Guice module which instruments methods annotated with the {@link com.yammer.metrics.annotation.Metered},
 * {@link com.yammer.metrics.annotation.Timed}, {@link com.yammer.metrics.annotation.Gauge}, and
 * {@link com.yammer.metrics.annotation.ExceptionMetered} annotations.
 * 
 * @see com.yammer.metrics.annotation.Gauge
 * @see com.yammer.metrics.annotation.Metered
 * @see com.yammer.metrics.annotation.Timed
 * @see com.yammer.metrics.annotation.ExceptionMetered
 * @see MeteredInterceptor
 * @see InstrumentedInterceptor
 * @see GaugeInjectionListener
 */
public class CodahaleMetricsModule extends AbstractModule {
	@Override
	protected void configure() {
		final MetricsRegistry metricsRegistry = createMetricsRegistry();

		CodahaleMetricsSystem metricsSystem = new CodahaleMetricsSystem(metricsRegistry);
		requestInjection(metricsSystem);
		bind(MetricsSystem.class).toInstance(metricsSystem);

		bind(MetricsRegistry.class).toInstance(metricsRegistry);
		bind(HealthCheckRegistry.class).toInstance(createHealthCheckRegistry());

		// We don't use the JMX reporter; it puts lots of constraints on the names
		// (it should sanitize them, of course!)
		// bindJmxReporter();
		JmxReporter.shutdownDefault();

		if (ClasspathHelpers.isJerseyOnClasspath()) {
			bind(JerseyMetricsHook.class);
		}

		// bindListener(Matchers.any(), new MeteredListener(metricsRegistry));
		// bindListener(Matchers.any(), new TimedListener(metricsRegistry));
		// bindListener(Matchers.any(), new GaugeListener(metricsRegistry));
		// bindListener(Matchers.any(), new ExceptionMeteredListener(metricsRegistry));

		bindListener(Matchers.any(), new InstrumentedListener(metricsSystem));

		if (ClasspathHelpers.isJettyOnClasspath()) {
			bind(WebServerBuilder.class).to(InstrumentedJettyWebServerBuilder.class);
		}
	}

	/**
	 * Override to provide a custom binding for {@link JmxReporter}
	 */
	protected void bindJmxReporter() {
		bind(JmxReporter.class).toProvider(JmxReporterProvider.class).in(Scopes.SINGLETON);
	}

	/**
	 * Override to provide a custom {@link HealthCheckRegistry}
	 */
	protected HealthCheckRegistry createHealthCheckRegistry() {
		return HealthChecks.defaultRegistry();
	}

	/**
	 * Override to provide a custom {@link MetricsRegistry}
	 */
	protected MetricsRegistry createMetricsRegistry() {
		return Metrics.defaultRegistry();
	}
}