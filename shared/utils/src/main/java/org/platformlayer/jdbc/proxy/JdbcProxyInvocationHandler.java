package org.platformlayer.jdbc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import org.platformlayer.jdbc.JdbcConnection;
import org.platformlayer.jdbc.JdbcUtils;
import org.platformlayer.jdbc.simplejpa.JoinedQueryResult;
import org.platformlayer.jdbc.simplejpa.JoinedQueryResult.ObjectList;
import org.platformlayer.jdbc.simplejpa.ResultSetMapper;
import org.platformlayer.jdbc.simplejpa.ResultSetMappers;
import org.platformlayer.metrics.MetricTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class JdbcProxyInvocationHandler<T> implements InvocationHandler {
	private static final Logger log = LoggerFactory.getLogger(JdbcProxyInvocationHandler.class);

	private final Class<T> interfaceType;
	private final JdbcConnection jdbcConnection;
	private final Provider<ResultSetMappers> resultSetMappersProvider;
	private final JdbcClassProxy<T> classProxy;

	JdbcProxyInvocationHandler(Provider<ResultSetMappers> resultSetMappersProvider, JdbcConnection jdbcConnection,
			Class<T> interfaceType, JdbcClassProxy<T> classProxy) {
		this.resultSetMappersProvider = resultSetMappersProvider;
		this.jdbcConnection = jdbcConnection;
		this.interfaceType = interfaceType;
		this.classProxy = classProxy;
	}

	@Override
	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
		return invokeQuery(m, args);
	}

	private Object invokeQuery(Method m, Object[] args) throws SQLException {
		QueryDescriptor queryDescriptor = QueryDescriptor.getQueryDescriptor(m);

		SqlWithParameters sql = queryDescriptor.buildSql(args);

		MetricTimer.Context timerContext = null;

		MetricTimer timer = classProxy.getTimer(m, queryDescriptor);
		if (timer != null) {
			timerContext = timer.start();
		}

		boolean batchExecute = queryDescriptor.isBatchExecute();
		PreparedStatement ps = null;
		try {
			log.debug("Executing SQL: " + sql);

			if (batchExecute) {
				ps = jdbcConnection.prepareBatchStatement(sql.getSql());
			} else {
				ps = jdbcConnection.prepareStatement(sql.getSql());
			}

			sql.setParameters(ps);

			if (batchExecute) {
				ps.addBatch();
				Class<?> returnType = m.getReturnType();
				if (returnType.equals(Void.class) || returnType.equals(void.class)) {
					return null;
				} else {
					throw new IllegalStateException("@BatchExecute must have return type of void");
				}
			} else {
				boolean isResultSet = ps.execute();
				if (isResultSet) {
					return marshalQueryset(m, ps);
				} else {
					Class<?> returnType = m.getReturnType();

					int updateCount = ps.getUpdateCount();
					if (returnType.equals(Void.class) || returnType.equals(void.class)) {
						return null;
					} else if (returnType.equals(Integer.class) || returnType.equals(int.class)) {
						return updateCount;
					} else {
						throw new IllegalArgumentException();
					}
				}
			}
		} finally {
			if (!batchExecute) {
				JdbcUtils.safeClose(ps);
			}

			if (timerContext != null) {
				timerContext.stop();
			}
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
				if (values.size() == 0) {
					return null;
				}
				if (values.size() != 1) {
					throw new SQLException("Multiple rows returned where not expected");
				}
				return values.get(0);
			}
		}

		ResultSetMappers mappers = resultSetMappersProvider.get();
		ResultSetMapper mapper = new ResultSetMapper(mappers);
		JoinedQueryResult result = mapper.doMap(jdbcConnection.getConnectionMetadata(), ps);

		if (returnType.equals(List.class)) {
			Map<Class<?>, ObjectList<?>> types = result.types;
			if (types.size() != 1) {
				throw new UnsupportedOperationException();
			}
			Class<?> key = Iterables.getOnlyElement(types.keySet());
			Collection<?> all = result.getAll(key);
			if (all instanceof List) {
				return all;
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
		// ResultSetSingleClassMapper resultSetMapper = new ResultSetSingleClassMapper(DatabaseNameMapping.POSTGRESQL,
		// returnType);
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
