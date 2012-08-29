package org.platformlayer.cache;

import java.lang.reflect.Method;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class CacheInterceptor implements MethodInterceptor {
	private static final Logger log = Logger.getLogger(CacheInterceptor.class);

	final Cache<Method, CacheStore> caches = CacheBuilder.newBuilder().build();

	static class CacheStore {
		private final Method method;

		final Cache<CacheKey, Object> cache;

		public CacheStore(Method method) {
			this.method = method;

			Memoize annotation = method.getAnnotation(Memoize.class);
			CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();

			int ttl = annotation.ttl();
			cacheBuilder.expireAfterWrite(ttl, TimeUnit.SECONDS);

			cache = cacheBuilder.build();
		}
	}

	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		final Method method = invocation.getMethod();

		CacheStore cacheStore = caches.get(method, new Callable<CacheStore>() {
			@Override
			public CacheStore call() throws Exception {
				// TODO: Register with metrics??
				return new CacheStore(method);
			}
		});

		CacheKey cacheKey = new CacheKey(invocation.getArguments());

		for (Entry<CacheKey, Object> entry : cacheStore.cache.asMap().entrySet()) {
			log.info(entry.getKey() + " => " + entry.getValue());
			log.info(" this = " + cacheKey + " equals=" + cacheKey.equals(entry.getKey()));
		}

		return cacheStore.cache.get(cacheKey, new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				try {
					return invocation.proceed();
				} catch (Exception e) {
					throw e;
				} catch (Throwable e) {
					throw new ExecutionException("Error executing memoized function", e);
				}
			}
		});
	}
}