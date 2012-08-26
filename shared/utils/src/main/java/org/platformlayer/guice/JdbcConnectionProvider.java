package org.platformlayer.guice;

import java.sql.Connection;

import com.google.inject.Provider;

class JdbcConnectionProvider implements Provider<Connection> {

	@Override
	public Connection get() {
		return JdbcTransactionInterceptor.getConnection();
	}

}
