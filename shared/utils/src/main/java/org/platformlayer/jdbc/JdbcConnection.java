package org.platformlayer.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.platformlayer.jdbc.simplejpa.ConnectionMetadata;
import org.postgresql.jdbc2.AbstractJdbc2Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.jolbox.bonecp.PreparedStatementHandle;

public class JdbcConnection {
	private static final Logger log = LoggerFactory.getLogger(JdbcConnection.class);

	final ConnectionMetadata metadata;
	final Connection connection;

	final Map<String, PreparedStatement> batches = Maps.newHashMap();

	JdbcConnection(ConnectionMetadata metadata, Connection connection) {
		super();
		this.metadata = metadata;
		this.connection = connection;
	}

	private Connection getConnection() {
		return connection;
	}

	public ConnectionMetadata getConnectionMetadata() {
		return metadata;
	}

	public <K, V> V getCacheable(final K key, final Callable<V> fn) {
		return metadata.getCacheable(key, fn);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		PreparedStatement preparedStatement = getConnection().prepareStatement(sql);

		ensurePrepared(preparedStatement);

		return preparedStatement;
	}

	private void ensurePrepared(PreparedStatement preparedStatement) throws SQLException {
		PreparedStatement actual = preparedStatement;

		if (actual instanceof PreparedStatementHandle) {
			PreparedStatementHandle handle = (PreparedStatementHandle) actual;
			actual = handle.getInternalPreparedStatement();
		}

		if (actual instanceof AbstractJdbc2Statement) {
			// Make sure that we actually prepare the statement
			((AbstractJdbc2Statement) actual).setPrepareThreshold(1);
		}
	}

	public PreparedStatement prepareBatchStatement(String sql) throws SQLException {
		PreparedStatement ps = batches.get(sql);
		if (ps == null) {
			ps = prepareStatement(sql);
			batches.put(sql, ps);
		}

		return ps;
	}

	public void commit() throws SQLException {
		if (!batches.isEmpty()) {
			for (Entry<String, PreparedStatement> entry : batches.entrySet()) {
				String sql = entry.getKey();
				PreparedStatement preparedStatement = entry.getValue();
				log.debug("Flushing batch operation: " + sql);

				int[] rowCounts = preparedStatement.executeBatch();
				for (int rowCount : rowCounts) {
					if (rowCount < 0) {
						throw new SQLException("Error flushing batch operation: " + sql);
					}

					if (rowCount != 1) {
						throw new SQLException("Unexpected row count from batch operation: " + sql);
					}
				}

				preparedStatement.close();
			}
			batches.clear();
		}
		connection.commit();
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		PreparedStatement ps = connection.prepareStatement(sql, columnNames);

		ensurePrepared(ps);

		return ps;
	}

}
