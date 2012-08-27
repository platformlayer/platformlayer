package org.platformlayer.jdbc;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.platformlayer.jdbc.simplejpa.ConnectionMetadata;

public class JdbcConnection {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(JdbcConnection.class);

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

}
