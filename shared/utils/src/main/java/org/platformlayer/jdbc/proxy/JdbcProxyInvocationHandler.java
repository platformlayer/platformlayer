package org.platformlayer.jdbc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import org.platformlayer.jdbc.JdbcUtils;
import org.platformlayer.jdbc.simplejpa.JoinedQueryResult;
import org.platformlayer.jdbc.simplejpa.JoinedQueryResult.ObjectList;
import org.platformlayer.jdbc.simplejpa.ResultSetMapper;
import org.platformlayer.jdbc.simplejpa.ResultSetMappers;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class JdbcProxyInvocationHandler implements InvocationHandler {
    private final Class<?> interfaceType;
    private final Connection connection;
    private final Provider<ResultSetMappers> resultSetMappersProvider;

    private JdbcProxyInvocationHandler(Provider<ResultSetMappers> resultSetMappersProvider, Connection connection, Class<?> interfaceType) {
        this.resultSetMappersProvider = resultSetMappersProvider;
        this.connection = connection;
        this.interfaceType = interfaceType;
    }

    public static <T> T newInstance(Provider<ResultSetMappers> resultSetMappersProvider, Connection connection, Class<T> interfaceType) {
        Class[] proxyInterfaces = new Class[] { interfaceType };
        JdbcProxyInvocationHandler backend = new JdbcProxyInvocationHandler(resultSetMappersProvider, connection, interfaceType);
        T frontend = (T) Proxy.newProxyInstance(interfaceType.getClassLoader(), proxyInterfaces, backend);
        return frontend;
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        Query query = m.getAnnotation(Query.class);
        if (query != null) {
            return invokeQuery(query, m, args);
        }
        throw new UnsupportedOperationException();
    }

    private Object invokeQuery(Query query, Method m, Object[] args) throws SQLException {
        String sql = query.value();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }

            boolean isResultSet = ps.execute();
            if (isResultSet) {
                return marshalQueryset(m, ps);
            } else {
                Class<?> returnType = m.getReturnType();

                int updateCount = ps.getUpdateCount();
                if (returnType.equals(Void.class)) {
                    return null;
                } else if (returnType.equals(Integer.class) || returnType.equals(int.class)) {
                    return updateCount;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        } finally {
            JdbcUtils.safeClose(ps);
        }
    }

    private Object marshalQueryset(Method m, PreparedStatement ps) throws SQLException {
        Class<?> returnType = m.getReturnType();

        ResultSet rs = ps.getResultSet();
        ResultSetMetaData rsmd = rs.getMetaData();
        if (rsmd.getColumnCount() == 1) {
            // Special case - single columns are returned as raw lists
            // (I don't think mapping to a domain object is ever useful;
            // if it is, we could force it using an additional property of the annotation)
            List<Object> values = Lists.newArrayList();

            while (rs.next()) {
                Object v = rs.getObject(1);
                values.add(v);
            }

            if (returnType.equals(List.class)) {
                return values;
            } else {
                if (values.size() == 0)
                    return null;
                if (values.size() != 1) {
                    throw new SQLException("Multiple rows returned where not expected");
                }
                return values.get(0);
            }
        }

        ResultSetMappers mappers = resultSetMappersProvider.get();
        ResultSetMapper mapper = new ResultSetMapper(mappers);
        JoinedQueryResult result = mapper.doMap(ps);

        if (returnType.equals(List.class)) {
            Map<Class<?>, ObjectList<?>> types = result.types;
            if (types.size() != 1) {
                throw new UnsupportedOperationException();
            }
            Class<?> key = Iterables.getOnlyElement(types.keySet());
            Collection<?> all = result.getAll(key);
            if (all instanceof List) {
                return (List) all;
            } else {
                return Lists.newArrayList(all);
            }
        } else if (returnType.equals(JoinedQueryResult.class)) {
            return result;
        } else {
            Object val = result.getOneOrNull(returnType);
            return val;
        }
        //
        // ResultSetMetaData rsmd = rs.getMetaData();
        //
        // if (returnType.equals(List.class)) {
        // List<Object> ret = Lists.newArrayList();
        //
        // while (rs.next()) {
        // Object v;
        //
        // if (returnType.equals(String.class)) {
        // if (rsmd.getColumnCount() != 1) {
        // throw new UnsupportedOperationException();
        // }
        // v = rs.getString(0);
        // }
        // else {
        // // TODO: Move to provider
        // ResultSetMappers mappers = new ResultSetMappers(DatabaseNameMapping.POSTGRESQL, returnType);
        // ResultSetMapper mapper = new ResultSetMapper(mappers);
        // mapper.doMap(prep)
        // ResultSetSingleClassMapper resultSetMapper = new ResultSetSingleClassMapper(DatabaseNameMapping.POSTGRESQL, returnType);
        //
        // }
        // ret.add(v);
        // }
        //
        // return ret;
        // } else {
        // throw new UnsupportedOperationException();
        // }
    }
}
