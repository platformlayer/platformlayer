//package org.platformlayer.jdbc;
//
//import java.lang.reflect.Field;
//
//import org.apache.log4j.Logger;
//
//import com.jolbox.bonecp.BoneCP;
//import com.jolbox.bonecp.BoneCPDataSource;
//import com.jolbox.bonecp.Statistics;
//import com.yammer.metrics.Metrics;
//import com.yammer.metrics.core.Gauge;
//
//public class ReportConnectionPoolMetrics {
//	@SuppressWarnings("unused")
//	private static final Logger log = Logger.getLogger(ReportConnectionPoolMetrics.class);
//
//	final Class<?> context;
//	final String prefix;
//
//	BoneCP pool;
//
//	final BoneCPDataSource dataSource;
//
//	BoneCP getPool() {
//		if (pool == null) {
//			pool = getPool(dataSource);
//		}
//		return pool;
//	}
//
//	public ReportConnectionPoolMetrics(String prefix, BoneCPDataSource dataSource) {
//		super();
//		this.dataSource = dataSource;
//		this.context = BoneCP.class;
//		this.prefix = prefix;
//
//		this.pool = getPool(dataSource);
//	}
//
//	long lastFetch;
//	Statistics last;
//
//	private static BoneCP getPool(BoneCPDataSource dataSource) {
//		// TODO: This is fixed in 0.8
//		try {
//			Field field = BoneCPDataSource.class.getDeclaredField("pool");
//			field.setAccessible(true);
//			return (BoneCP) field.get(dataSource);
//		} catch (IllegalAccessException e) {
//			throw new IllegalArgumentException("Error getting pool", e);
//		} catch (SecurityException e) {
//			throw new IllegalArgumentException("Error getting pool", e);
//		} catch (NoSuchFieldException e) {
//			throw new IllegalArgumentException("Error getting pool", e);
//		}
//	}
//
//	protected Statistics getStats() {
//		long now = System.currentTimeMillis();
//		if (last == null || now - lastFetch > 1000) {
//			BoneCP pool = getPool();
//			if (pool == null) {
//				// TODO: Should we handle this properly??
//				return null;
//			}
//
//			last = pool.getStatistics();
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
//		Metrics.newGauge(context, prefix + "hitCount", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().getCacheHits();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "missCount", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().getCacheMiss();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "statementsCached", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().getStatementsCached();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "connectionsRequested", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().getConnectionsRequested();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "statementsExecuted", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().getStatementsExecuted();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "statementsPrepared", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().getStatementsPrepared();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "cumulativeConnectionWaitTime", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().getCumulativeConnectionWaitTime();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "cumulativeStatementExecutionTime", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().getCumulativeStatementExecutionTime();
//			}
//		});
//
//		Metrics.newGauge(context, prefix + "cumulativeStatementPrepareTime", new Gauge<Long>() {
//			@Override
//			public Long value() {
//				return getStats().getCumulativeStatementPrepareTime();
//			}
//		});
//
//	}
//
// }
