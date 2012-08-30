package org.platformlayer.cache;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class CacheModule extends AbstractModule {

	@Override
	protected void configure() {
		CacheInterceptor cacheInterceptor = new CacheInterceptor();

		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Memoize.class), cacheInterceptor);
	}
}
