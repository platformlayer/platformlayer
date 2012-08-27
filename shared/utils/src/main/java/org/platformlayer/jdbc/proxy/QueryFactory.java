package org.platformlayer.jdbc.proxy;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.jdbc.JdbcConnection;
import org.platformlayer.jdbc.simplejpa.ResultSetMappers;
import org.platformlayer.metrics.MetricsSystem;

public class QueryFactory {
	@Inject
	Provider<JdbcConnection> connectionProvider;

	@Inject
	Provider<ResultSetMappers> resultSetMappersProvider;

	@Inject
	MetricsSystem metricsSystem;

	public <T> T get(Class<T> interfaceType) {
		JdbcClassProxy<T> proxy = JdbcClassProxy.get(metricsSystem, interfaceType);

		JdbcConnection connection = connectionProvider.get();

		return proxy.buildHandler(resultSetMappersProvider, connection);
	}
}
