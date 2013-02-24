package org.platformlayer.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.platformlayer.jdbc.simplejpa.ConnectionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class JdbcTransactionInterceptor implements MethodInterceptor {
	private static final Logger log = LoggerFactory.getLogger(JdbcTransactionInterceptor.class);

	private static final ThreadLocal<JdbcConnection> threadLocalConnection = new ThreadLocal<JdbcConnection>();

	private static final String SQLSTATE_CONNECTION_FAILURE = "08006";

	private static final String SQLSTATE_UNABLE_TO_CONNECT = "08001";

	private static final String SQLSTATE_SERIALIZATION_FAILURE = "40001";

	private static final String SQLSTATE_DEADLOCK_DETECTED = "40P01";

	@Inject
	Provider<DataSource> dataSource;

	ConnectionMetadata metadata = new ConnectionMetadata();

	@Inject
	public JdbcTransactionInterceptor(Provider<DataSource> dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		JdbcConnection jdbcConnection = threadLocalConnection.get();

		if (jdbcConnection != null) {
			// Ignore recursive calls
			return methodInvocation.proceed();
		}

		JdbcTransaction transaction = methodInvocation.getMethod().getAnnotation(JdbcTransaction.class);

		int maxRetries = transaction.maxRetries();

		for (int i = 1; i <= maxRetries; i++) {
			boolean calledCommit = false;

			try {
				// Yes, this is fairly paranoid...
				Connection connection = null;
				try {
					connection = dataSource.get().getConnection();

					jdbcConnection = new JdbcConnection(metadata, connection);

					threadLocalConnection.set(jdbcConnection);

					boolean committed = false;
					connection.setAutoCommit(false);

					try {
						Object returnValue = methodInvocation.proceed();
						log.debug("Committing transaction");

						calledCommit = true;
						jdbcConnection.commit();

						committed = true;
						return returnValue;
					} finally {
						if (!committed) {
							log.debug("Rolling back transaction");
							try {
								connection.rollback();
							} catch (Exception e) {
								log.warn("Ignoring error while rolling back transaction", e);
							}
						}
					}
				} finally {
					threadLocalConnection.set(null);

					try {
						if (connection != null) {
							connection.close();
						}
					} catch (Exception e) {
						log.warn("Ignoring error while closing connection", e);
					}
				}

			} catch (Exception e) {
				boolean canRetry;
				if (i == maxRetries) {
					canRetry = false;
				} else {
					canRetry = getShouldRetry(e, calledCommit);
				}

				if (!canRetry) {
					throw e;
				}

				log.info("Automatically retrying transaction after error", e);
			}
		}

		throw new IllegalStateException("Unreachable");
	}

	private boolean getShouldRetry(Throwable e, boolean calledCommit) {
		// TODO: Retry if it is a deadlock

		if (e instanceof SQLException) {
			String sqlState = ((SQLException) e).getSQLState();
			if (sqlState != null) {
				if (sqlState.equals(SQLSTATE_UNABLE_TO_CONNECT)) {
					// TODO: Delay??
					return true;
				}

				if (sqlState.equals(SQLSTATE_CONNECTION_FAILURE)) {
					if (calledCommit) {
						log.warn("Detected connection failure, but already called commit; can't safely auto-retry");
						return false;
					} else {
						return true;
					}
				}

				if (sqlState.equals(SQLSTATE_SERIALIZATION_FAILURE)) {
					return true;
				}

				if (sqlState.equals(SQLSTATE_DEADLOCK_DETECTED)) {
					return true;
				}
			}
		}

		Throwable cause = e.getCause();
		if (cause == null) {
			return false;
		}
		return getShouldRetry(cause, calledCommit);
	}

	static JdbcConnection getConnection() {
		JdbcConnection connection = threadLocalConnection.get();
		if (connection == null) {
			throw new IllegalArgumentException("Must decorate transactional methods with @JdbcTransaction attribute");
		}
		return connection;
	}

}
