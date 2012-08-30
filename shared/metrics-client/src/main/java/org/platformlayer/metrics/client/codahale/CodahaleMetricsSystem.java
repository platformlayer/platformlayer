//package org.platformlayer.metrics.client.codahale;
//
//import java.util.concurrent.TimeUnit;
//
//import javax.inject.Inject;
//
//import org.apache.log4j.Logger;
//import org.platformlayer.metrics.DiscoverSingletonMetrics;
//import org.platformlayer.metrics.MetricRegistry;
//import org.platformlayer.metrics.client.MetricClient;
//import org.platformlayer.metrics.client.PlatformlayerMetricsReporter;
//
//import com.google.inject.Injector;
//import com.yammer.metrics.core.MetricsRegistry;
//
//public class CodahaleMetricsRegistry implements MetricRegistry {
//	private static final Logger log = Logger.getLogger(CodahaleMetricsRegistry.class);
//
//	@Inject
//	Injector injector;
//
//	@Inject
//	DiscoverSingletonMetrics metricsDiscovery;
//
//	@Inject
//	MetricClient metricClient;
//
//	@Inject
//	MetricsRegistry registry;
//
//	PlatformlayerMetricsReporter reporter;
//
//	public PlatformlayerMetricsReporter getReporter() {
//		synchronized (this) {
//			if (reporter == null) {
//				reporter = PlatformlayerMetricsReporter.enable(10, TimeUnit.SECONDS, metricClient);
//			}
//			return reporter;
//		}
//	}
//
//	@Override
//	public void addInjected(Class<?> injectedType) {
//		Object instance = injector.getInstance(injectedType);
//
//		discoverMetrics(instance);
//	}
//
//	@Override
//	public void discoverMetrics(Object o) {
//
//	}
//
//	@Override
//	public void init() {
//		metricsDiscovery.discover();
//
//		getReporter();
//	}
//
// }
