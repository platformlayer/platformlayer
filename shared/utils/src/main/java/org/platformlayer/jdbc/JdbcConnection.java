package org.platformlayer.jdbc;

import java.sql.Connection;
import java.util.concurrent.Callable;

import org.platformlayer.jdbc.simplejpa.ConnectionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcConnection {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(JdbcConnection.class);

	final ConnectionMetadata metadata;
	final Connection connection;

	JdbcConnection(ConnectionMetadata metadata, Connection connection) {
		super();
		this.metadata = metadata;
		this.connection = connection;
	}

	public Connection getConnection() {
		return connection;
	}

	public ConnectionMetadata getConnectionMetadata() {
		return metadata;
	}

	public <K, V> V getCacheable(final K key, final Callable<V> fn) {
		return metadata.getCacheable(key, fn);
	}

}
