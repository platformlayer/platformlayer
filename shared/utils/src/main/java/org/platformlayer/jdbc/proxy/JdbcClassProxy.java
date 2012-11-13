package org.platformlayer.jdbc.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.inject.Provider;

import org.platformlayer.jdbc.JdbcConnection;
import org.platformlayer.jdbc.simplejpa.ResultSetMappers;
import org.platformlayer.metrics.MetricKey;
import org.platformlayer.metrics.MetricRegistry;
import org.platformlayer.metrics.MetricTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class JdbcClassProxy<T> {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(JdbcClassProxy.class);

	final MetricRegistry metricsSystem;
	final Class<T> interfaceType;

	private JdbcClassProxy(MetricRegistry metricsSystem, Class<T> interfaceType) {
		this.metricsSystem = metricsSystem;
		this.interfaceType = interfaceType;
	}

	static final Cache<Class, JdbcClassProxy> cache = CacheBuilder.newBuilder().build();

	public static <T> JdbcClassProxy<T> get(final MetricRegistry metricsSystem, final Class<T> interfaceType) {
		try {
			return cache.get(interfaceType, new Callable<JdbcClassProxy>() {
				@Override
				public JdbcClassProxy call() throws Exception {
					return new JdbcClassProxy(metricsSystem, interfaceType);
				}
			});
		} catch (ExecutionException e) {
			throw new IllegalStateException("Error building JDBC proxy", e);
		}
	}

	public T buildHandler(Provider<ResultSetMappers> resultSetMappersProvider, JdbcConnection connection) {
		JdbcProxyInvocationHandler<T> backend = new JdbcProxyInvocationHandler<T>(resultSetMappersProvider, connection,
				interfaceType, this);

		Constructor<?> constructor = getProxyConstructor();

		T frontend;
		try {
			frontend = (T) constructor.newInstance(new Object[] { backend });
		} catch (InstantiationException e) {
			throw new IllegalStateException("Error building proxy", e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Error building proxy", e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException("Error building proxy", e);
		}
		return frontend;
	}

	Constructor<?> proxyConstructor;

	private Constructor<?> getProxyConstructor() {
		if (proxyConstructor == null) {
			try {
				Class<?>[] proxyInterfaces = new Class[] { interfaceType };

				Constructor<?> constructor = Proxy.getProxyClass(interfaceType.getClassLoader(), proxyInterfaces)
						.getConstructor(new Class[] { InvocationHandler.class });
				proxyConstructor = constructor;
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException("Error building proxy", e);
			}
		}
		return proxyConstructor;
	}

	static class MethodInfo {
		MetricTimer timer;
	}

	static final Cache<Method, MethodInfo> methodInfoCache = CacheBuilder.newBuilder().build();

	public MetricTimer getTimer(final Method method, final QueryDescriptor query) {
		try {
			MethodInfo methodInfo = methodInfoCache.get(method, new Callable<MethodInfo>() {
				@Override
				public MethodInfo call() throws Exception {
					MethodInfo methodInfo = new MethodInfo();

					String sql = query.getSql();

					MetricKey metricKey = MetricKey.build(interfaceType, sql);
					methodInfo.timer = metricsSystem.getTimer(metricKey);

					return methodInfo;
				}
			});

			return methodInfo.timer;
		} catch (ExecutionException e) {
			throw new IllegalStateException("Error building method info", e);
		}
	}
}
