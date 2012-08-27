package org.platformlayer.jdbc;

import com.google.inject.Provider;

class JdbcConnectionProvider implements Provider<JdbcConnection> {

	@Override
	public JdbcConnection get() {
		return JdbcTransactionInterceptor.getConnection();
	}

}
