package org.platformlayer.jdbc;

import javax.sql.DataSource;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class JdbcGuiceModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(DatabaseStatistics.class).asEagerSingleton();

		JdbcTransactionInterceptor interceptor = new JdbcTransactionInterceptor(getProvider(DataSource.class));
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(JdbcTransaction.class), interceptor);

		bind(JdbcConnection.class).toProvider(JdbcConnectionProvider.class);
	}
}
