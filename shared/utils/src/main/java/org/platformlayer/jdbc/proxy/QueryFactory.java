package org.platformlayer.jdbc.proxy;

import java.sql.Connection;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.jdbc.simplejpa.ResultSetMappers;
import org.platformlayer.metrics.MetricsSystem;

public class QueryFactory {
	@Inject
	Provider<Connection> connection;

	@Inject
	Provider<ResultSetMappers> resultSetMappersProvider;

	@Inject
	MetricsSystem metricsSystem;

	public <T> T get(Class<T> interfaceType) {
		JdbcClassProxy<T> proxy = JdbcClassProxy.get(metricsSystem, interfaceType);

		return proxy.buildHandler(resultSetMappersProvider, connection.get());
	}
}
