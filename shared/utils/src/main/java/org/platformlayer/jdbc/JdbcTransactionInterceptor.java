package org.platformlayer.jdbc;

import java.sql.Connection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.platformlayer.jdbc.simplejpa.ConnectionMetadata;

@Singleton
class JdbcTransactionInterceptor implements MethodInterceptor {

	private static final ThreadLocal<JdbcConnection> threadLocalConnection = new ThreadLocal<JdbcConnection>();

	@Inject
	DataSource dataSource;

	ConnectionMetadata metadata = new ConnectionMetadata();

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		JdbcConnection jdbcConnection = threadLocalConnection.get();

		if (jdbcConnection != null) {
			// Ignore recursive calls
			return methodInvocation.proceed();
		}

		// Yes, this is fairly paranoid...
		try {
			Connection connection = dataSource.getConnection();

			jdbcConnection = new JdbcConnection(metadata, connection);

			threadLocalConnection.set(jdbcConnection);

			try {
				boolean committed = false;
				connection.setAutoCommit(false);

				try {
					Object returnValue = methodInvocation.proceed();
					connection.commit();
					committed = true;
					return returnValue;
				} finally {
					if (!committed) {
						connection.rollback();
					}
				}
			} finally {
				connection.close();
			}
		} finally {
			threadLocalConnection.set(null);
		}
	}

	static JdbcConnection getConnection() {
		JdbcConnection connection = threadLocalConnection.get();
		if (connection == null) {
			throw new IllegalArgumentException("Must decorate transactional methods with @JdbcTransaction attribute");
		}
		return connection;
	}

}
