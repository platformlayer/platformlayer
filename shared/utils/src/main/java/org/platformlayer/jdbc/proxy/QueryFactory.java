package org.platformlayer.jdbc.proxy;

import java.sql.Connection;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.jdbc.simplejpa.ResultSetMappers;

public class QueryFactory {
    @Inject
    Provider<Connection> connection;

    @Inject
    Provider<ResultSetMappers> resultSetMappersProvider;

    public <T> T get(Class<T> interfaceType) {
        return JdbcProxyInvocationHandler.newInstance(resultSetMappersProvider, connection.get(), interfaceType);
    }
}
