package org.platformlayer.metrics;

import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApacheTomcatMetricsReporter implements MetricsSource {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ApacheTomcatMetricsReporter.class);

	ConnectionPool pool;

	final org.apache.tomcat.jdbc.pool.DataSource dataSource;

	final MetricKey key;

	ConnectionPool getPool() {
		if (pool == null) {
			pool = getPool(dataSource);
		}
		return pool;
	}

	public ApacheTomcatMetricsReporter(MetricKey key, org.apache.tomcat.jdbc.pool.DataSource dataSource) {
		this.key = key;
		this.dataSource = dataSource;

		this.pool = getPool(dataSource);
	}

	private static ConnectionPool getPool(org.apache.tomcat.jdbc.pool.DataSource dataSource) {
		return dataSource.getPool();
	}

	@Override
	public void addMetrics(MetricTreeObject tree) {
		ConnectionPool pool = getPool();
		if (pool == null) {
			// TODO: Should we handle this properly??
			return;
		}

		MetricTreeObject subtree = tree.getSubtree(key);

		// Statistics stats = pool.getStatistics();
		// subtree.addInt("hitCount", stats.getCacheHits());
		// subtree.addInt("missCount", stats.getCacheMiss());
		// subtree.addInt("connectionsRequested", stats.getConnectionsRequested());
		// subtree.addInt("statementsCached", stats.getStatementsCached());
		// subtree.addInt("statementsExecuted", stats.getStatementsExecuted());
		// subtree.addInt("statementsPrepared", stats.getStatementsPrepared());
		// subtree.addInt("cumulativeConnectionWaitTime", stats.getCumulativeConnectionWaitTime());
		// subtree.addInt("cumulativeStatementExecutionTime", stats.getCumulativeStatementExecutionTime());
		// subtree.addInt("cumulativeStatementPrepareTime", stats.getCumulativeStatementPrepareTime());
		//
		subtree.addInt("activeConnections", pool.getActive());
		subtree.addInt("idleConnections", pool.getIdle());
		subtree.addInt("waitCount", pool.getWaitCount());

	}

}
