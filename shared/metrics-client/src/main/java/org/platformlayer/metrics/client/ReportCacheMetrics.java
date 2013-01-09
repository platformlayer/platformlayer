//package org.platformlayer.metrics.client;
//
//import org.apache.log4j.Logger;
//
//import com.google.common.cache.Cache;
//import com.google.common.cache.CacheStats;
//import com.yammer.metrics.Metrics;
//import com.yammer.metrics.core.Gauge;
//
//public class ReportCacheMetrics {
//	@SuppressWarnings("unused")
//	private static final Logger log = LoggerFactory.getLogger(ReportCacheMetrics.class);
//
//	final Class<?> context;
//	final String prefix;
//	final Cache<?, ?> cache;
//
//	public ReportCacheMetrics(Class<?> context, String prefix, Cache<?, ?> cache) {
//		super();
//		this.context = context;
//		this.prefix = prefix;
//		this.cache = cache;
//	}
//
//	long lastFetch;
//	long lastSize;
//	CacheStats last;
//
//	protected CacheStats getStats() {
//		long now = System.currentTimeMillis();
//		if (last == null || now - lastFetch > 1000) {
//			last = cache.stats();
//			// Size isn't part of the stats? Collect it at the same time
//			lastSize = cache.size();
//			lastFetch = now;
//		}
//		return last;
//	}
//
//	public void init() {
//		String prefix = this.prefix;
//		if (prefix == null) {
//			prefix = "";
//		}
//
//		Metrics.newGauge(context, prefix + "size", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				getStats();
//				return lastSize;
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "evictionCount", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().evictionCount();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "hitCount", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().hitCount();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "loadCount", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().loadCount();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "loadExceptionCount", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().loadExceptionCount();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "loadSuccessCount", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().loadSuccessCount();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "missCount", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().missCount();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "requestCount", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().requestCount();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "totalLoadTime", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().totalLoadTime();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "averageLoadPenalty", new Gauge<Double>() {
//			@Override
//			public Double value() {
//				return getStats().averageLoadPenalty();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "hitRate", new Gauge<Double>() {
//			@Override
//			public Double value() {
//				return getStats().hitRate();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "loadExceptionRate", new Gauge<Double>() {
//			@Override
//			public Double value() {
//				return getStats().loadExceptionRate();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "missRate", new Gauge<Double>() {
//			@Override
//			public Double value() {
//				return getStats().missRate();
//			}
//		});
//	}
//
// }
