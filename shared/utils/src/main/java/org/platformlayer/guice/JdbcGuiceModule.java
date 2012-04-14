package org.platformlayer.guice;

import java.sql.Connection;

import org.platformlayer.jdbc.JdbcTransaction;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class JdbcGuiceModule extends AbstractModule {
	@Override
	protected void configure() {
		JdbcTransactionInterceptor interceptor = new JdbcTransactionInterceptor();
		requestInjection(interceptor);
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(JdbcTransaction.class), interceptor);

		bind(Connection.class).toProvider(JdbcGuiceProviders.class);
	}
}
