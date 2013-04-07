package org.platformlayer.cache;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class CacheInterceptor implements MethodInterceptor {
	private static final Logger log = LoggerFactory.getLogger(CacheInterceptor.class);

	final Cache<Method, CacheStore> caches = CacheBuilder.newBuilder().build();

	static final Object CACHED_NULL = new Object();

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

		final CacheKey cacheKey = new CacheKey(invocation.getArguments());

		// for (Entry<CacheKey, Object> entry : cacheStore.cache.asMap().entrySet()) {
		// log.info(entry.getKey() + " => " + entry.getValue());
		// log.info(" this = " + cacheKey + " equals=" + cacheKey.equals(entry.getKey()));
		// }

		Object v = cacheStore.cache.get(cacheKey, new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				try {
					log.debug("Cache miss on " + cacheKey);
					Object ret = invocation.proceed();
					if (ret == null) {
						// Guice cache can't cache nulls, so return a marker instead
						return CACHED_NULL;
					}
					return ret;
				} catch (Exception e) {
					throw e;
				} catch (Throwable e) {
					throw new ExecutionException("Error executing memoized function", e);
				}
			}
		});

		if (v == CACHED_NULL) {
			v = null;
		}

		return v;
	}
}