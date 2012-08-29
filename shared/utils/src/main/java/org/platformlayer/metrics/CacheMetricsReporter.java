package org.platformlayer.metrics;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;

public class CacheMetricsReporter implements MetricsReporter {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(CacheMetricsReporter.class);

	final MetricKey key;
	final Cache<?, ?> cache;

	public CacheMetricsReporter(MetricKey key, Cache<?, ?> cache) {
		super();
		this.key = key;
		this.cache = cache;
	}

	@Override
	public void addMetrics(MetricTreeObject tree) {
		long size = cache.size();
		CacheStats stats = cache.stats();

		MetricTreeObject subtree = tree.getSubtree(key);

		subtree.addInt("size", size);
		subtree.addInt("evictionCount", stats.evictionCount());
		subtree.addInt("hitCount", stats.hitCount());
		subtree.addInt("loadCount", stats.loadCount());
		subtree.addInt("loadExceptionCount", stats.loadExceptionCount());
		subtree.addInt("loadSuccessCount", stats.loadSuccessCount());
		subtree.addInt("missCount", stats.missCount());
		subtree.addInt("requestCount", stats.requestCount());
		subtree.addInt("totalLoadTime", stats.totalLoadTime());

		subtree.addFloat("averageLoadPenalty", stats.averageLoadPenalty());
		subtree.addFloat("hitRate", stats.hitRate());
		subtree.addFloat("loadExceptionRate", stats.loadExceptionRate());
		subtree.addFloat("missRate", stats.missRate());
	}
}
