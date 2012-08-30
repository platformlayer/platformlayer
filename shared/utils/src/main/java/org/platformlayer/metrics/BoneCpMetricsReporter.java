package org.platformlayer.metrics;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPDataSource;
import com.jolbox.bonecp.Statistics;

public class BoneCpMetricsReporter implements MetricsSource {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(BoneCpMetricsReporter.class);

	BoneCP pool;

	final BoneCPDataSource dataSource;

	final MetricKey key;

	BoneCP getPool() {
		if (pool == null) {
			pool = getPool(dataSource);
		}
		return pool;
	}

	public BoneCpMetricsReporter(MetricKey key, BoneCPDataSource dataSource) {
		this.key = key;
		this.dataSource = dataSource;

		this.pool = getPool(dataSource);
	}

	private static BoneCP getPool(BoneCPDataSource dataSource) {
		// TODO: This is fixed in 0.8
		try {
			Field field = BoneCPDataSource.class.getDeclaredField("pool");
			field.setAccessible(true);
			return (BoneCP) field.get(dataSource);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Error getting pool", e);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Error getting pool", e);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("Error getting pool", e);
		}
	}

	@Override
	public void addMetrics(MetricTreeObject tree) {
		BoneCP pool = getPool();
		if (pool == null) {
			// TODO: Should we handle this properly??
			return;
		}

		Statistics stats = pool.getStatistics();

		MetricTreeObject subtree = tree.getSubtree(key);

		subtree.addInt("hitCount", stats.getCacheHits());
		subtree.addInt("missCount", stats.getCacheMiss());
		subtree.addInt("connectionsRequested", stats.getConnectionsRequested());
		subtree.addInt("statementsCached", stats.getStatementsCached());
		subtree.addInt("statementsExecuted", stats.getStatementsExecuted());
		subtree.addInt("statementsPrepared", stats.getStatementsPrepared());
		subtree.addInt("cumulativeConnectionWaitTime", stats.getCumulativeConnectionWaitTime());
		subtree.addInt("cumulativeStatementExecutionTime", stats.getCumulativeStatementExecutionTime());
		subtree.addInt("cumulativeStatementPrepareTime", stats.getCumulativeStatementPrepareTime());
	}

}
